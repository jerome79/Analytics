/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.financial.forex.definition.ForexDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexNonDeliverableForwardDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborSpreadDefinition;
import com.opengamma.strata.basics.currency.MultiCurrencyAmount;
import com.opengamma.strata.collect.ArgChecker;
import com.opengamma.strata.collect.timeseries.LocalDateDoubleTimeSeries;

/**
 * Returns all of the known pay cash-flows, including floating payments that have fixed.
 * The payments are always positive.
 */
public final class FixedPayCashFlowVisitor extends InstrumentDefinitionVisitorSameValueAdapter<LocalDateDoubleTimeSeries, Map<LocalDate, MultiCurrencyAmount>> {
  private static final Logger s_logger = LoggerFactory.getLogger(FixedPayCashFlowVisitor.class);
  private static final FixedPayCashFlowVisitor INSTANCE = new FixedPayCashFlowVisitor();

  public static FixedPayCashFlowVisitor getInstance() {
    return INSTANCE;
  }

  private FixedPayCashFlowVisitor() {
    super(Collections.<LocalDate, MultiCurrencyAmount>emptyMap());
  }

  /**
   * If the notional is positive (i.e. an amount is to be received), returns an empty map.
   * Otherwise, returns a map containing a single payment date and amount to be paid.
   * @param cash The cash definition, not null
   * @return A map containing the (single) payment date and amount, or an empty map, as appropriate
   */
  @Override
  public Map<LocalDate, MultiCurrencyAmount> visitCashDefinition(final CashDefinition cash) {
    ArgChecker.notNull(cash, "cash");
    if (cash.getNotional() > 0) {
      return Collections.emptyMap();
    }
    final LocalDate endDate = cash.getEndDate().toLocalDate();
    return Collections.singletonMap(endDate, MultiCurrencyAmount.of(cash.getCurrency(), -cash.getInterestAmount()));
  }

  /**
   * If the notional is positive (i.e. an amount is to be received), returns an empty map.
   * Otherwise, returns a map containing a single payment date and amount to be paid.
   * @param cash The cash definition, not null
   * @param indexFixingTimeSeries Not used
   * @return A map containing the (single) payment date and amount, or an empty map, as appropriate
   */
  @Override
  public Map<LocalDate, MultiCurrencyAmount> visitCashDefinition(final CashDefinition cash, final LocalDateDoubleTimeSeries indexFixingTimeSeries) {
    s_logger.info("An index fixing time series was supplied, but will not be used");
    return visitCashDefinition(cash);
  }

  /**
   * If the notional is positive (i.e. the payment will be received), returns an empty map.
   * Otherwise, returns a map containing a single payment date and amount to be paid.
   * @param payment The payment, not null
   * @return A map containing the (single) payment date and amount
   */
  @Override
  public Map<LocalDate, MultiCurrencyAmount> visitPaymentFixedDefinition(final PaymentFixedDefinition payment) {
    ArgChecker.notNull(payment, "payment");
    if (payment.getReferenceAmount() > 0) {
      return Collections.emptyMap();
    }
    final LocalDate endDate = payment.getPaymentDate().toLocalDate();
    return Collections.singletonMap(endDate, MultiCurrencyAmount.of(payment.getCurrency(), -payment.getReferenceAmount()));
  }

  /**
   * If the notional is positive (i.e. the payment will be received), returns an empty map.
   * Otherwise, returns a map containing a single payment date and amount to be paid.
   * @param payment The payment, not null
   * @param indexFixingTimeSeries Not used
   * @return A map containing the (single) payment date and amount
   */
  @Override
  public Map<LocalDate, MultiCurrencyAmount> visitPaymentFixedDefinition(final PaymentFixedDefinition payment, final LocalDateDoubleTimeSeries indexFixingTimeSeries) {
    s_logger.info("An index fixing time series was supplied, but will not be used");
    return visitPaymentFixedDefinition(payment);
  }

  /**
   * If the notional is positive (i.e. the coupon will be received), returns an empty map.
   * Otherwise, returns a map containing a single payment date and amount to be paid.
   * @param coupon The fixed coupon, not null
   * @return A map containing the (single) payment date and amount
   */
  @Override
  public Map<LocalDate, MultiCurrencyAmount> visitCouponFixedDefinition(final CouponFixedDefinition coupon) {
    ArgChecker.notNull(coupon, "coupon");
    if (coupon.getNotional() > 0) {
      return Collections.emptyMap();
    }
    final LocalDate endDate = coupon.getPaymentDate().toLocalDate();
    return Collections.singletonMap(endDate, MultiCurrencyAmount.of(coupon.getCurrency(), -coupon.getAmount()));
  }

  /**
   * If the notional is positive (i.e. the coupon will be received), returns an empty map.
   * Otherwise, returns a map containing a single payment date and amount to be paid.
   * @param coupon The fixed coupon, not null
   * @param indexFixingTimeSeries Not used
   * @return A map containing the (single) payment date and amount
   */
  @Override
  public Map<LocalDate, MultiCurrencyAmount> visitCouponFixedDefinition(final CouponFixedDefinition coupon, final LocalDateDoubleTimeSeries indexFixingTimeSeries) {
    s_logger.info("An index fixing time series was supplied, but will not be used");
    return visitCouponFixedDefinition(coupon);
  }

  @Override
  public Map<LocalDate, MultiCurrencyAmount> visitCouponIborDefinition(final CouponIborDefinition coupon) {
    return visitCouponIborDefinition(coupon, null);
  }

  /**
   * If the notional is positive (i.e. the coupon will be received), returns an empty map.
   * If the fixing date is before the last date in the index fixing time series (i.e. the fixing has taken place),
   * returns a map containing a simple payment date and amount to be paid. Otherwise, returns
   * an empty map.
   * @param coupon The floating coupon, not null
   * @param indexFixingTimeSeries The fixing time series, not null if the coupon is to be paid.
   * @return A map containing the (single) payment date and amount if fixing has taken place, otherwise an empty map
   */
  @Override
  public Map<LocalDate, MultiCurrencyAmount> visitCouponIborDefinition(final CouponIborDefinition coupon, final LocalDateDoubleTimeSeries indexFixingTimeSeries) {
    ArgChecker.notNull(coupon, "coupon");
    if (coupon.getNotional() > 0) {
      return Collections.emptyMap();
    }
    ArgChecker.notNull(indexFixingTimeSeries, "index fixing time series");
    final LocalDate fixingDate = coupon.getFixingDate().toLocalDate();
    if (!indexFixingTimeSeries.getLatestDate().isBefore(fixingDate)) {
      LocalDate endDate = coupon.getPaymentDate().toLocalDate();
      double fixedRate = extractFixingValue(indexFixingTimeSeries, fixingDate);

      double payment = -coupon.getNotional() * coupon.getPaymentYearFraction() * fixedRate;
      return Collections.singletonMap(endDate, MultiCurrencyAmount.of(coupon.getCurrency(), payment));
    }
    return Collections.emptyMap();
  }

  private double extractFixingValue(LocalDateDoubleTimeSeries indexFixingTimeSeries, LocalDate fixingDate) {
    return indexFixingTimeSeries.get(fixingDate)
        .orElseThrow(() -> new IllegalArgumentException("Could not get fixing value for date " + fixingDate));
  }

  @Override
  public Map<LocalDate, MultiCurrencyAmount> visitCouponIborSpreadDefinition(final CouponIborSpreadDefinition coupon) {
    return visitCouponIborSpreadDefinition(coupon, null);
  }

  /**
   * If the notional is positive (i.e. the coupon will be received), returns an empty map.
   * If the fixing date is before the last date in the index fixing time series (i.e. the fixing has taken place),
   * returns a map containing a simple payment date and amount to be paid. Otherwise, returns
   * an empty map.
   * @param coupon The floating coupon, not null
   * @param indexFixingTimeSeries The fixing time series, not null if the coupon is to be paid.
   * @return A map containing the (single) payment date and amount if fixing has taken place, otherwise an empty map
   */
  @Override
  public Map<LocalDate, MultiCurrencyAmount> visitCouponIborSpreadDefinition(final CouponIborSpreadDefinition coupon,
      final LocalDateDoubleTimeSeries indexFixingTimeSeries) {
    ArgChecker.notNull(coupon, "coupon");
    if (coupon.getNotional() > 0) {
      return Collections.emptyMap();
    }
    ArgChecker.notNull(indexFixingTimeSeries, "index fixing time series");
    final LocalDate fixingDate = coupon.getFixingDate().toLocalDate();
    if (!indexFixingTimeSeries.getLatestDate().isBefore(fixingDate)) {
      final LocalDate endDate = coupon.getPaymentDate().toLocalDate();

      double fixedRate = extractFixingValue(indexFixingTimeSeries, fixingDate);
      final double payment = -coupon.getNotional() * coupon.getPaymentYearFraction() * (fixedRate + coupon.getSpread());
      return Collections.singletonMap(endDate, MultiCurrencyAmount.of(coupon.getCurrency(), payment));
    }
    return Collections.emptyMap();
  }

  /**
   * If the FRA is a payer, returns a map containing a single payment. Otherwise, throws an exception (as the index fixing series is needed).
   * @param forwardRateAgreement The FRA, not null
   * @return A map containing the (single) payment date and amount
   */
  @Override
  public Map<LocalDate, MultiCurrencyAmount> visitForwardRateAgreementDefinition(final ForwardRateAgreementDefinition forwardRateAgreement) {
    ArgChecker.notNull(forwardRateAgreement, "FRA");
    ArgChecker.isTrue(forwardRateAgreement.getNotional() > 0, "Pay floating FRAs need an index fixing time series to find pay cash flows");
    return visitForwardRateAgreementDefinition(forwardRateAgreement, null);
  }

  /**
   * If the FRA is a payer, or if the FRA is a receiver and the fixing date is before the last date in the index fixing time series
   * (i.e. the fixing has taken place), returns a map containing a single payment.
   * @param forwardRateAgreement The FRA, not null
   * @param indexFixingTimeSeries The fixing time series for the floating index, not null if the FRA is a receiver
   * @return A map containing the (single) payment date and amount
   */
  @Override
  public Map<LocalDate, MultiCurrencyAmount> visitForwardRateAgreementDefinition(
      ForwardRateAgreementDefinition forwardRateAgreement,
      LocalDateDoubleTimeSeries indexFixingTimeSeries) {

    ArgChecker.notNull(forwardRateAgreement, "FRA");
    final LocalDate endDate = forwardRateAgreement.getPaymentDate().toLocalDate();
    if (forwardRateAgreement.getNotional() < 0) {
      ArgChecker.notNull(indexFixingTimeSeries, "index fixing time series");
      final LocalDate fixingDate = forwardRateAgreement.getFixingDate().toLocalDate();
      if (!indexFixingTimeSeries.getLatestDate().isBefore(fixingDate)) {

        double fixedRate = extractFixingValue(indexFixingTimeSeries, fixingDate);
        final double payment = -forwardRateAgreement.getPaymentYearFraction() * forwardRateAgreement.getNotional() * fixedRate;
        return Collections.singletonMap(endDate, MultiCurrencyAmount.of(forwardRateAgreement.getCurrency(), payment));
      }
      return Collections.emptyMap();
    }
    final double payment = forwardRateAgreement.getReferenceAmount() * forwardRateAgreement.getRate() * forwardRateAgreement.getFixingPeriodAccrualFactor();
    return Collections.singletonMap(endDate, MultiCurrencyAmount.of(forwardRateAgreement.getCurrency(), payment));
  }

  /**
   * Returns a map containing all of the known payments to be made in an annuity. If there are no payments to be made, an empty map is returned.
   * @param annuity The annuity, not null
   * @param indexFixingTimeSeries The fixing time series for the floating index, not null
   * @return A map containing the payment dates and amounts
   */
  @Override
  public Map<LocalDate, MultiCurrencyAmount> visitAnnuityDefinition(final AnnuityDefinition<? extends PaymentDefinition> annuity,
      final LocalDateDoubleTimeSeries indexFixingTimeSeries) {
    ArgChecker.notNull(annuity, "annuity");
    return getDatesAndPaymentsFromAnnuity(annuity, indexFixingTimeSeries);
  }

  /**
   * If the swap is a payer, returns a map containing all of the fixed payments. Otherwise, throws an exception (as the index fixing series is needed).
   * @param swap The swap, not null
   * @return A map containing the fixed payment dates and amounts
   */
  @Override
  public Map<LocalDate, MultiCurrencyAmount> visitSwapFixedIborDefinition(final SwapFixedIborDefinition swap) {
    ArgChecker.notNull(swap, "swap");
    ArgChecker.isTrue(swap.getFixedLeg().isPayer(), "Receiver swaps need an index fixing series to calculate pay cash-flows");
    return visitSwapFixedIborDefinition(swap, null);
  }

  /**
   * If the swap is a payer, returns a map containing all of the fixed payments. If the swap is a receiver, returns a map containing
   * all of the payment amounts that have been fixed.
   * @param swap The swap, not null
   * @param indexFixingTimeSeries The fixing time series for the floating index, not null
   * @return A map containing the payment dates and amounts
   */
  @Override
  public Map<LocalDate, MultiCurrencyAmount> visitSwapFixedIborDefinition(final SwapFixedIborDefinition swap, final LocalDateDoubleTimeSeries indexFixingTimeSeries) {
    ArgChecker.notNull(swap, "swap");
    if (swap.getFixedLeg().isPayer()) {
      return swap.getFixedLeg().accept(this, indexFixingTimeSeries);
    }
    ArgChecker.notNull(indexFixingTimeSeries, "index fixing time series");
    return swap.getIborLeg().accept(this, indexFixingTimeSeries);
  }

  /**
   * If the swap is a payer, returns a map containing all of the fixed payments. Otherwise, throws an exception (as the index fixing series is needed).
   * @param swap The swap, not null
   * @return A map containing the fixed payment dates and amounts
   */
  @Override
  public Map<LocalDate, MultiCurrencyAmount> visitSwapFixedIborSpreadDefinition(final SwapFixedIborSpreadDefinition swap) {
    ArgChecker.notNull(swap, "swap");
    ArgChecker.isTrue(swap.getFixedLeg().isPayer(), "Receiver swaps need an index fixing series to calculate pay cash-flows");
    return visitSwapFixedIborSpreadDefinition(swap, null);
  }

  /**
   * If the swap is a payer, returns a map containing all of the fixed payments. If the swap is a receiver, returns a map containing
   * all of the payments amounts that have been fixed.
   * @param swap The swap, not null
   * @param indexFixingTimeSeries The fixing time series for the floating index, not null
   * @return A map containing the payment dates and amounts
   */
  @Override
  public Map<LocalDate, MultiCurrencyAmount> visitSwapFixedIborSpreadDefinition(final SwapFixedIborSpreadDefinition swap,
      final LocalDateDoubleTimeSeries indexFixingTimeSeries) {
    ArgChecker.notNull(swap, "swap");
    if (swap.getFixedLeg().isPayer()) {
      return swap.getFixedLeg().accept(this, indexFixingTimeSeries);
    }
    ArgChecker.notNull(indexFixingTimeSeries, "index fixing time series");
    return swap.getIborLeg().accept(this, indexFixingTimeSeries);
  }

  /**
   * Returns a map containing a single date and payment.
   * @param fx The FX instrument, not null
   * @return A map containing the (single) payment date and amount
   */
  @Override
  public Map<LocalDate, MultiCurrencyAmount> visitForexDefinition(final ForexDefinition fx) {
    ArgChecker.notNull(fx, "fx");
    if (fx.getPaymentCurrency1().getReferenceAmount() < 0) {
      return fx.getPaymentCurrency1().accept(this);
    }
    return fx.getPaymentCurrency2().accept(this);
  }

  /**
   * Returns a map containing a single date and payment.
   * @param fx The FX instrument, not null
   * @param indexFixingTimeSeries Not used
   * @return A map containing the (single) payment date and amount
   */
  @Override
  public Map<LocalDate, MultiCurrencyAmount> visitForexDefinition(final ForexDefinition fx, final LocalDateDoubleTimeSeries indexFixingTimeSeries) {
    s_logger.info("An index fixing time series was supplied, but will not be used");
    return visitForexDefinition(fx);
  }

  /**
   * If the cash settlement amount is negative (i.e. it must be paid), returns a map containing a single date and payment. Otherwise, returns
   * an empty map.
   * @param ndf The NDF, not null
   * @return A map containing the (single) payment date and amount
   */
  @Override
  public Map<LocalDate, MultiCurrencyAmount> visitForexNonDeliverableForwardDefinition(final ForexNonDeliverableForwardDefinition ndf) {
    ArgChecker.notNull(ndf, "ndf");
    if (ndf.getNotional() > 0) {
      return Collections.emptyMap();
    }
    final LocalDate endDate = ndf.getPaymentDate().toLocalDate();
    return Collections.singletonMap(endDate, MultiCurrencyAmount.of(ndf.getCurrency2(), -ndf.getNotional()));
  }

  /**
   * If the cash settlement amount is negative (i.e. it must be paid), returns a map containing a single date and payment. Otherwise, returns
   * an empty map.
   * @param ndf The NDF, not null
   * @param indexFixingTimeSeries Not used
   * @return A map containing the (single) payment date and amount
   */
  @Override
  public Map<LocalDate, MultiCurrencyAmount> visitForexNonDeliverableForwardDefinition(final ForexNonDeliverableForwardDefinition ndf,
      final LocalDateDoubleTimeSeries indexFixingTimeSeries) {
    s_logger.info("An index fixing time series was supplied but will not be used");
    return visitForexNonDeliverableForwardDefinition(ndf);
  }

  private Map<LocalDate, MultiCurrencyAmount> getDatesAndPaymentsFromAnnuity(final AnnuityDefinition<? extends PaymentDefinition> annuity,
      final LocalDateDoubleTimeSeries indexFixingTimeSeries) {
    final Map<LocalDate, MultiCurrencyAmount> result = new HashMap<>();
    for (final PaymentDefinition payment : annuity.getPayments()) {
      final Map<LocalDate, MultiCurrencyAmount> payments = payment.accept(this, indexFixingTimeSeries);
      for (final Map.Entry<LocalDate, MultiCurrencyAmount> entry : payments.entrySet()) {
        final int scale = entry.getValue().stream().map(ca -> ca.getAmount() < 0 ? -1 : 1).findFirst().orElse(1);
        final MultiCurrencyAmount mca = entry.getValue().multipliedBy(scale);
        final LocalDate key = entry.getKey();
        if (result.containsKey(key)) {
          result.put(key, result.get(key).plus(mca));
        } else {
          result.put(key, mca);
        }
      }
    }
    return result;
  }
}

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

import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.cash.DepositIborDefinition;
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborGearingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapIborIborDefinition;
import com.opengamma.strata.basics.currency.CurrencyAmount;
import com.opengamma.strata.basics.currency.MultiCurrencyAmount;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Returns all of the floating cash-flows of an instrument. The notionals returned are those that will be received
 * (i.e. a semi-annual swap with a notional of $1MM will return notionals of ~$0.5MM)
 */
public final class FloatingReceiveCashFlowVisitor extends InstrumentDefinitionVisitorAdapter<Object, Map<LocalDate, MultiCurrencyAmount>> {
  private static final FloatingReceiveCashFlowVisitor INSTANCE = new FloatingReceiveCashFlowVisitor();

  public static FloatingReceiveCashFlowVisitor getInstance() {
    return INSTANCE;
  }

  /**
   * If the notional is negative (i.e. the amount is to be paid) returns
   * an empty map. Otherwise, returns a map containing a single payment date and the notional amount
   * multiplied by the accrual period.
   * @param deposit The deposit instrument, not null
   * @return A map containing the (single) payment date and amount, or an empty map, as appropriate
   */
  @Override
  public Map<LocalDate, MultiCurrencyAmount> visitDepositIborDefinition(final DepositIborDefinition deposit) {
    ArgChecker.notNull(deposit, "deposit");
    final LocalDate endDate = deposit.getEndDate().toLocalDate();
    if (deposit.getNotional() < 0) {
      return Collections.emptyMap();
    }
    final double amount = deposit.getNotional() * deposit.getRate() * deposit.getAccrualFactor();
    return Collections.singletonMap(endDate, MultiCurrencyAmount.of(CurrencyAmount.of(deposit.getCurrency(), amount)));
  }

  /**
   * If the notional is negative (i.e. the amount is to be paid) returns
   * an empty map. Otherwise, returns a map containing a single payment date and the notional amount
   * multiplied by the accrual period.
   * @param deposit The deposit instrument, not null
   * @param data Not used
   * @return A map containing the (single) payment date and amount, or an empty map, as appropriate
   */
  @Override
  public Map<LocalDate, MultiCurrencyAmount> visitDepositIborDefinition(final DepositIborDefinition deposit, final Object data) {
    return visitDepositIborDefinition(deposit);
  }

  /**
   * If the notional is negative (i.e. the amount is to be paid), returns
   * an empty map. Otherwise, returns a map containing a single payment date and the notional amount
   * multiplied by the accrual period.
   * @param coupon The coupon instrument, not null
   * @return A map containing the (single) payment date and amount, or an empty map, as appropriate
   */
  @Override
  public Map<LocalDate, MultiCurrencyAmount> visitCouponIborDefinition(final CouponIborDefinition coupon) {
    ArgChecker.notNull(coupon, "coupon");
    final LocalDate endDate = coupon.getPaymentDate().toLocalDate();
    if (coupon.getNotional() < 0) {
      return Collections.emptyMap();
    }
    final double amount = coupon.getNotional() * coupon.getFixingPeriodAccrualFactor();
    return Collections.singletonMap(endDate, MultiCurrencyAmount.of(CurrencyAmount.of(coupon.getCurrency(), amount)));
  }

  /**
   * If the notional is negative (i.e. the amount is to be paid), returns
   * an empty map. Otherwise, returns a map containing a single payment date and the notional amount
   * multiplied by the accrual period.
   * @param coupon The coupon instrument, not null
   * @param data Not used
   * @return A map containing the (single) payment date and amount, or an empty map, as appropriate
   */
  @Override
  public Map<LocalDate, MultiCurrencyAmount> visitCouponIborDefinition(final CouponIborDefinition coupon, final Object data) {
    return visitCouponIborDefinition(coupon);
  }

  /**
   * If the notional is negative (i.e. the amount is to be paid), returns
   * an empty map. Otherwise, returns a map containing a single payment date and the notional amount
   * multiplied by the accrual period.
   * @param coupon The coupon instrument, not null
   * @return A map containing the (single) payment date and amount, or an empty map, as appropriate
   */
  @Override
  public Map<LocalDate, MultiCurrencyAmount> visitCouponIborSpreadDefinition(final CouponIborSpreadDefinition coupon) {
    ArgChecker.notNull(coupon, "coupon");
    final LocalDate endDate = coupon.getPaymentDate().toLocalDate();
    if (coupon.getNotional() < 0) {
      return Collections.emptyMap();
    }
    final double amount = coupon.getNotional() * coupon.getFixingPeriodAccrualFactor() * (1 + coupon.getSpread());
    return Collections.singletonMap(endDate, MultiCurrencyAmount.of(CurrencyAmount.of(coupon.getCurrency(), amount)));
  }

  /**
   * If the notional is negative (i.e. the amount is to be paid), returns
   * an empty map. Otherwise, returns a map containing a single payment date and the notional amount
   * multiplied by the accrual period.
   * @param coupon The coupon instrument, not null
   * @param data Not used
   * @return A map containing the (single) payment date and amount, or an empty map, as appropriate
   */
  @Override
  public Map<LocalDate, MultiCurrencyAmount> visitCouponIborSpreadDefinition(final CouponIborSpreadDefinition coupon, final Object data) {
    return visitCouponIborSpreadDefinition(coupon);
  }

  /**
   * If the notional is negative (i.e. the amount is to be paid), returns
   * an empty map. Otherwise, returns a map containing a single payment date and the notional amount
   * multiplied by the accrual period.
   * @param coupon The coupon instrument, not null
   * @return A map containing the (single) payment date and amount, or an empty map, as appropriate
   */
  @Override
  public Map<LocalDate, MultiCurrencyAmount> visitCouponIborGearingDefinition(final CouponIborGearingDefinition coupon) {
    ArgChecker.notNull(coupon, "coupon");
    final LocalDate endDate = coupon.getPaymentDate().toLocalDate();
    if (coupon.getNotional() < 0) {
      return Collections.emptyMap();
    }
    final double amount = coupon.getNotional() * coupon.getFixingPeriodAccrualFactor() * (coupon.getFactor() + coupon.getSpread());
    return Collections.singletonMap(endDate, MultiCurrencyAmount.of(CurrencyAmount.of(coupon.getCurrency(), amount)));
  }

  /**
   * If the notional is negative (i.e. the amount is to be paid), returns
   * an empty map. Otherwise, returns a map containing a single payment date and the notional amount
   * multiplied by the accrual period.
   * @param coupon The coupon instrument, not null
   * @param data Not used
   * @return A map containing the (single) payment date and amount, or an empty map, as appropriate
   */
  @Override
  public Map<LocalDate, MultiCurrencyAmount> visitCouponIborGearingDefinition(final CouponIborGearingDefinition coupon, final Object data) {
    return visitCouponIborGearingDefinition(coupon);
  }

  /**
   * If the notional is negative (i.e. the FRA is a receiver), returns an empty map. Otherwise, returns
   * a map containing a single payment date and the notional amount multiplied by the accrual period
   * @param forwardRateAgreement The FRA, not null
   * @return A map containing the (single) payment date and amount, or an empty map, as appropriate
   */
  @Override
  public Map<LocalDate, MultiCurrencyAmount> visitForwardRateAgreementDefinition(final ForwardRateAgreementDefinition forwardRateAgreement) {
    ArgChecker.notNull(forwardRateAgreement, "FRA");
    final LocalDate endDate = forwardRateAgreement.getPaymentDate().toLocalDate();
    if (forwardRateAgreement.getNotional() < 0) {
      return Collections.emptyMap();
    }
    return Collections.singletonMap(
        endDate,
        MultiCurrencyAmount.of(CurrencyAmount.of(forwardRateAgreement.getCurrency(),
            forwardRateAgreement.getNotional() * forwardRateAgreement.getFixingPeriodAccrualFactor())));
  }

  /**
   * If the notional is negative (i.e. the FRA is a receiver), returns an empty map. Otherwise, returns
   * a map containing a single payment date and the notional amount multiplied by the accrual period
   * @param forwardRateAgreement The FRA, not null
   * @param data Not used
   * @return A map containing the (single) payment date and amount, or an empty map, as appropriate
   */
  @Override
  public Map<LocalDate, MultiCurrencyAmount> visitForwardRateAgreementDefinition(final ForwardRateAgreementDefinition forwardRateAgreement, final Object data) {
    return visitForwardRateAgreementDefinition(forwardRateAgreement);
  }

  /**
   * Returns a map containing all of the floating payments to be received in an annuity. If there are no floating payments to be reeeived,
   * an empty map is returned
   * @param annuity The annuity, not null
   * @return A map containing the payment dates and amounts
   */
  @Override
  public Map<LocalDate, MultiCurrencyAmount> visitAnnuityDefinition(final AnnuityDefinition<? extends PaymentDefinition> annuity) {
    ArgChecker.notNull(annuity, "annuity");
    return getDatesFromAnnuity(annuity);
  }

  /**
   * Returns a map containing all of the floating payments to be received in an annuity. If there are no floating payments to be received,
   * an empty map is returned
   * @param annuity The annuity, not null
   * @param data Not used
   * @return A map containing the payment dates and amounts
   */
  @Override
  public Map<LocalDate, MultiCurrencyAmount> visitAnnuityDefinition(final AnnuityDefinition<? extends PaymentDefinition> annuity, final Object data) {
    return visitAnnuityDefinition(annuity);
  }

  /**
   * If the swap is a receiver, returns an empty map. Otherwise, returns a map containing all of the floating payments to be received.
   * @param swap The swap, not null
   * @return A map containing floating payments, or an empty map, as appropriate
   */
  @Override
  public Map<LocalDate, MultiCurrencyAmount> visitSwapFixedIborDefinition(final SwapFixedIborDefinition swap) {
    ArgChecker.notNull(swap, "swap");
    if (!swap.getIborLeg().isPayer()) {
      return swap.getIborLeg().accept(this);
    }
    return Collections.emptyMap();
  }

  /**
   * If the swap is a receiver, returns an empty map. Otherwise, returns a map containing all of the floating payments to be made
   * @param swap The swap, not null
   * @param data Not used
   * @return A map containing floating payments, or an empty map, as appropriate
   */
  @Override
  public Map<LocalDate, MultiCurrencyAmount> visitSwapFixedIborDefinition(final SwapFixedIborDefinition swap, final Object data) {
    return visitSwapFixedIborDefinition(swap);
  }

  /**
   * If the swap is a receiver, returns an empty map. Otherwise, returns a map containing all of the floating payments to be made
   * @param swap The swap, not null
   * @return A map containing floating payments, or an empty map, as appropriate
   */
  @Override
  public Map<LocalDate, MultiCurrencyAmount> visitSwapFixedIborSpreadDefinition(final SwapFixedIborSpreadDefinition swap) {
    ArgChecker.notNull(swap, "swap");
    if (!swap.getIborLeg().isPayer()) {
      return swap.getIborLeg().accept(this);
    }
    return Collections.emptyMap();
  }

  /**
   * If the swap is a receiver, returns an empty map. Otherwise, returns a map containing all of the floating payments to be made
   * @param swap The swap, not null
   * @param data Not used
   * @return A map containing floating payments, or an empty map, as appropriate
   */
  @Override
  public Map<LocalDate, MultiCurrencyAmount> visitSwapFixedIborSpreadDefinition(final SwapFixedIborSpreadDefinition swap, final Object data) {
    return visitSwapFixedIborSpreadDefinition(swap);
  }

  /**
   * Returns a map containing all of the floating payments in the receive leg
   * @param swap The swap, not null
   * @return A map containing floating payments
   */
  @Override
  public Map<LocalDate, MultiCurrencyAmount> visitSwapIborIborDefinition(final SwapIborIborDefinition swap) {
    ArgChecker.notNull(swap, "swap");
    if (!swap.getFirstLeg().isPayer()) {
      return swap.getFirstLeg().accept(this);
    }
    return swap.getSecondLeg().accept(this);
  }

  /**
   * Returns a map containing all of the floating payments in the receive leg
   * @param swap The swap, not null
   * @param data Not used
   * @return A map containing floating payments
   */
  @Override
  public Map<LocalDate, MultiCurrencyAmount> visitSwapIborIborDefinition(final SwapIborIborDefinition swap, final Object data) {
    return visitSwapIborIborDefinition(swap);
  }

  private Map<LocalDate, MultiCurrencyAmount> getDatesFromAnnuity(final AnnuityDefinition<? extends PaymentDefinition> annuity) {
    final Map<LocalDate, MultiCurrencyAmount> result = new HashMap<>();
    for (final PaymentDefinition payment : annuity.getPayments()) {
      final Map<LocalDate, MultiCurrencyAmount> payments = payment.accept(this);
      for (final Map.Entry<LocalDate, MultiCurrencyAmount> entry : payments.entrySet()) {
        final int scale = entry.getValue().stream().mapToInt(ca -> ca.getAmount() < 0 ? -1 : 1).findFirst().orElse(1);
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

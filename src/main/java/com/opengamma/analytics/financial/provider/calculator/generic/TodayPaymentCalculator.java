/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.generic;

import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.derivative.ForexNonDeliverableForward;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionDigital;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionSingleBarrier;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.forex.derivative.ForexSwap;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositCounterpart;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositZero;
import com.opengamma.analytics.financial.interestrate.fra.derivative.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureTransaction;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedAccruedCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborGearing;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponON;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONCompounded;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.strata.basics.currency.MultiCurrencyAmount;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Calculates the payment amounts due on the valuation date (|time to payment|<small).
 */
public final class TodayPaymentCalculator extends InstrumentDerivativeVisitorAdapter<Void, MultiCurrencyAmount> {
  /**
   * The default time limit below which the payment is consider as being today.
   */
  private static final double DEFAULT_TIME_LIMIT_TODAY = 0.002;
  /**
   * The method unique instance.
   */
  private static final TodayPaymentCalculator INSTANCE = new TodayPaymentCalculator(DEFAULT_TIME_LIMIT_TODAY);

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static TodayPaymentCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Primary constructor
   * Note on negative timeLimits: toDerivative functions often drop past payments so using this calculator with timeLimits < -1dy may have unexpected impacts.
   * @param timeLimit the horizon in years, either positive (forward looking) or negative (backward looking) in which the calculator returns payment amounts
   * @return the calculator
   */
  public static TodayPaymentCalculator getInstance(final double timeLimit) {
    return new TodayPaymentCalculator(timeLimit);
  }

  private final double _timeLimit;

  /**
   * Constructor.
   */
  private TodayPaymentCalculator(final double timeLimit) {
    _timeLimit = timeLimit;
  }

  /**
   * // We wish to add payments if they occur within the time horizon, which may be forward, or backward, in time
   * @param paymentTime the payment time in question
   * @return true if the payment is to be counted as happening within the time limit / horizon
   */
  private boolean isWithinLimit(final double paymentTime) {
    return (Math.abs(paymentTime) < Math.abs(_timeLimit) && _timeLimit * paymentTime >= 0);
  }

  //     -----     Deposit     -----

  @Override
  public MultiCurrencyAmount visitCash(final Cash deposit) {
    ArgChecker.notNull(deposit, "instrument");
    MultiCurrencyAmount cash = MultiCurrencyAmount.of(deposit.getCurrency(), 0.0);
    if (isWithinLimit(deposit.getStartTime())) {
      cash = cash.plus(deposit.getCurrency(), -deposit.getInitialAmount());
    }
    if (isWithinLimit(deposit.getEndTime())) {
      cash = cash.plus(deposit.getCurrency(), deposit.getNotional() + deposit.getInterestAmount());
    }
    return cash;
  }

  @Override
  public MultiCurrencyAmount visitDepositZero(final DepositZero deposit) {
    ArgChecker.notNull(deposit, "instrument");
    MultiCurrencyAmount cash = MultiCurrencyAmount.of(deposit.getCurrency(), 0.0);
    if (isWithinLimit(deposit.getStartTime())) {
      cash = cash.plus(deposit.getCurrency(), -deposit.getInitialAmount());
    }
    if (isWithinLimit(deposit.getEndTime())) {
      cash = cash.plus(deposit.getCurrency(), deposit.getNotional() + deposit.getInterestAmount());
    }
    return cash;
  }

  @Override
  public MultiCurrencyAmount visitDepositCounterpart(final DepositCounterpart deposit) {
    ArgChecker.notNull(deposit, "instrument");
    MultiCurrencyAmount cash = MultiCurrencyAmount.of(deposit.getCurrency(), 0.0);
    if (isWithinLimit(deposit.getStartTime())) {
      cash = cash.plus(deposit.getCurrency(), -deposit.getInitialAmount());
    }
    if (isWithinLimit(deposit.getEndTime())) {
      cash = cash.plus(deposit.getCurrency(), deposit.getNotional() + deposit.getInterestAmount());
    }
    return cash;
  }

  @Override
  public MultiCurrencyAmount visitForwardRateAgreement(final ForwardRateAgreement fra) {
    ArgChecker.notNull(fra, "instrument");
    return MultiCurrencyAmount.of(fra.getCurrency(), 0.0);
  }

  @Override
  public MultiCurrencyAmount visitInterestRateFutureTransaction(final InterestRateFutureTransaction future) {
    ArgChecker.notNull(future, "instrument");
    return MultiCurrencyAmount.of(future.getCurrency(), 0.0);
  }

  @Override
  public MultiCurrencyAmount visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction futureOption) {
    ArgChecker.notNull(futureOption, "instrument");
    return MultiCurrencyAmount.of(futureOption.getUnderlyingSecurity().getCurrency(), 0.0);
  }

  @Override
  public MultiCurrencyAmount visitCouponFixed(final CouponFixed payment) {
    ArgChecker.notNull(payment, "instrument");
    MultiCurrencyAmount cash = MultiCurrencyAmount.of(payment.getCurrency(), 0.0);
    if (isWithinLimit(payment.getPaymentTime())) {
      cash = cash.plus(payment.getCurrency(), payment.getAmount());
    }
    return cash;
  }

  @Override
  public MultiCurrencyAmount visitFixedPayment(final PaymentFixed payment) {
    ArgChecker.notNull(payment, "instrument");
    MultiCurrencyAmount cash = MultiCurrencyAmount.of(payment.getCurrency(), 0.0);
    if (isWithinLimit(payment.getPaymentTime())) {
      cash = cash.plus(payment.getCurrency(), payment.getAmount());
    }
    return cash;
  }

  @Override
  public MultiCurrencyAmount visitCouponIbor(final CouponIbor payment) {
    ArgChecker.notNull(payment, "instrument");
    return MultiCurrencyAmount.of(payment.getCurrency(), 0.0);
  }

  @Override
  public MultiCurrencyAmount visitCouponIborSpread(final CouponIborSpread payment) {
    ArgChecker.notNull(payment, "instrument");
    return MultiCurrencyAmount.of(payment.getCurrency(), 0.0);
  }

  @Override
  public MultiCurrencyAmount visitCouponIborGearing(final CouponIborGearing payment) {
    ArgChecker.notNull(payment, "instrument");
    return MultiCurrencyAmount.of(payment.getCurrency(), 0.0);
  }

  @Override
  public MultiCurrencyAmount visitCouponOIS(final CouponON payment) {
    ArgChecker.notNull(payment, "instrument");
    return MultiCurrencyAmount.of(payment.getCurrency(), 0.0);
  }

  @Override
  public MultiCurrencyAmount visitCouponFixedAccruedCompounding(final CouponFixedAccruedCompounding payment) {
    ArgChecker.notNull(payment, "instrument");
    return MultiCurrencyAmount.of(payment.getCurrency(), 0.0);
  }

  @Override
  public MultiCurrencyAmount visitCouponONCompounded(final CouponONCompounded payment) {
    ArgChecker.notNull(payment, "instrument");
    return MultiCurrencyAmount.of(payment.getCurrency(), 0.0);
  }

  @Override
  public MultiCurrencyAmount visitGenericAnnuity(final Annuity<? extends Payment> annuity) {
    ArgChecker.notNull(annuity, "instrument");
    MultiCurrencyAmount pv = MultiCurrencyAmount.of(annuity.getCurrency(), 0.0);
    for (final Payment p : annuity.getPayments()) {
      pv = pv.plus(p.accept(this));
    }
    return pv;
  }

  @Override
  public MultiCurrencyAmount visitFixedCouponAnnuity(final AnnuityCouponFixed annuity) {
    return visitGenericAnnuity(annuity);
  }

  @Override
  public MultiCurrencyAmount visitSwap(final Swap<?, ?> swap) {
    ArgChecker.notNull(swap, "instrument");
    final MultiCurrencyAmount cash = swap.getFirstLeg().accept(this);
    return cash.plus(swap.getSecondLeg().accept(this));
  }

  @Override
  public MultiCurrencyAmount visitFixedCouponSwap(final SwapFixedCoupon<?> swap) {
    return visitSwap(swap);
  }

  @Override
  public MultiCurrencyAmount visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption) {
    ArgChecker.notNull(swaption, "instrument");
    return MultiCurrencyAmount.of(swaption.getCurrency(), 0.0);
  }

  @Override
  public MultiCurrencyAmount visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption) {
    ArgChecker.notNull(swaption, "instrument");
    return MultiCurrencyAmount.of(swaption.getCurrency(), 0.0);
  }

  @Override
  public MultiCurrencyAmount visitForex(final Forex forex) {
    ArgChecker.notNull(forex, "instrument");
    return visitFixedPayment(forex.getPaymentCurrency1()).plus(visitFixedPayment(forex.getPaymentCurrency2()));
  }

  @Override
  public MultiCurrencyAmount visitForexSwap(final ForexSwap forex) {
    ArgChecker.notNull(forex, "instrument");
    return visitForex(forex.getNearLeg()).plus(visitForex(forex.getFarLeg()));
  }

  @Override
  public MultiCurrencyAmount visitForexOptionVanilla(final ForexOptionVanilla forex) {
    ArgChecker.notNull(forex, "instrument");
    return MultiCurrencyAmount.of(forex.getCurrency1(), 0.0);
  }

  @Override
  public MultiCurrencyAmount visitForexOptionSingleBarrier(final ForexOptionSingleBarrier forex) {
    ArgChecker.notNull(forex, "instrument");
    return MultiCurrencyAmount.of(forex.getCurrency1(), 0.0);
  }

  @Override
  public MultiCurrencyAmount visitForexOptionDigital(final ForexOptionDigital forex) {
    ArgChecker.notNull(forex, "instrument");
    return MultiCurrencyAmount.of(forex.getCurrency1(), 0.0);
  }

  @Override
  public MultiCurrencyAmount visitForexNonDeliverableForward(final ForexNonDeliverableForward forex) {
    ArgChecker.notNull(forex, "instrument");
    return MultiCurrencyAmount.of(forex.getCurrency1(), 0.0);
  }

}

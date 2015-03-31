/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.discounting;

import java.util.stream.IntStream;

import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.derivative.ForexNonDeliverableForward;
import com.opengamma.analytics.financial.forex.derivative.ForexSwap;
import com.opengamma.analytics.financial.forex.provider.ForexDiscountingMethod;
import com.opengamma.analytics.financial.forex.provider.ForexNonDeliverableForwardDiscountingMethod;
import com.opengamma.analytics.financial.forex.provider.ForexSwapDiscountingMethod;
import com.opengamma.analytics.financial.instrument.index.IndexDeposit;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositIbor;
import com.opengamma.analytics.financial.interestrate.cash.provider.CashDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.cash.provider.DepositIborDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.fra.derivative.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.fra.provider.ForwardRateAgreementDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.future.derivative.FederalFundsFutureTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.SwapFuturesPriceDeliverableTransaction;
import com.opengamma.analytics.financial.interestrate.future.provider.FuturesTransactionMulticurveMethod;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedAccruedCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedFxReset;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborAverage;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborAverageFixingDates;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborAverageFixingDatesCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborAverageFixingDatesCompoundingFlatSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompoundingFlatSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompoundingSimpleSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompoundingSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborFxReset;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborGearing;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponON;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONArithmeticAverage;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONArithmeticAverageSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONArithmeticAverageSpreadSimplified;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONCompounded;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.DepositIndexCoupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.InterpolatedStubCoupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponFixedAccruedCompoundingDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponFixedCompoundingDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponFixedDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponFixedFxResetDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponIborAverageDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponIborAverageFixingDatesCompoundingDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponIborAverageFixingDatesCompoundingFlatSpreadDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponIborAverageFixingDatesDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponIborCompoundingDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponIborCompoundingFlatSpreadDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponIborCompoundingSimpleSpreadDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponIborCompoundingSpreadDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponIborDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponIborFxResetDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponIborGearingDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponIborSpreadDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponONArithmeticAverageDiscountingApproxMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponONArithmeticAverageSpreadDiscountingApproxMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponONArithmeticAverageSpreadSimplifiedDiscountingApproxMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponONCompoundedDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponONDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponONSpreadDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.PaymentFixedDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapMultileg;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.strata.basics.currency.MultiCurrencyAmount;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Calculator of the present value as a multiple currency amount using cash-flow discounting and forward estimation.
 */
public final class PresentValueDiscountingCalculator extends 
  InstrumentDerivativeVisitorAdapter<ParameterProviderInterface, MultiCurrencyAmount> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueDiscountingCalculator INSTANCE = new PresentValueDiscountingCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueDiscountingCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PresentValueDiscountingCalculator() {
  }

  /**
   * The methods used by the different instruments.
   */
  private static final CashDiscountingMethod METHOD_DEPOSIT = CashDiscountingMethod.getInstance();
  private static final PaymentFixedDiscountingMethod METHOD_PAY_FIXED = PaymentFixedDiscountingMethod.getInstance();
  private static final DepositIborDiscountingMethod METHOD_DEPOSIT_IBOR = DepositIborDiscountingMethod.getInstance();
  private static final CouponFixedDiscountingMethod METHOD_CPN_FIXED = CouponFixedDiscountingMethod.getInstance();
  private static final CouponFixedCompoundingDiscountingMethod METHOD_CPN_FIXED_COMPOUNDING = 
      CouponFixedCompoundingDiscountingMethod.getInstance();
  private static final CouponFixedFxResetDiscountingMethod METHOD_CPN_FIXED_FXRESET = 
      CouponFixedFxResetDiscountingMethod.getInstance();
  private static final CouponIborFxResetDiscountingMethod METHOD_CPN_IBOR_FXRESET =
      CouponIborFxResetDiscountingMethod.getInstance();
  private static final CouponIborDiscountingMethod METHOD_CPN_IBOR = CouponIborDiscountingMethod.getInstance();
  private static final CouponIborAverageDiscountingMethod METHOD_CPN_IBOR_AVERAGE = 
      CouponIborAverageDiscountingMethod.getInstance();
  private static final CouponIborSpreadDiscountingMethod METHOD_CPN_IBOR_SPREAD = 
      CouponIborSpreadDiscountingMethod.getInstance();
  private static final CouponIborGearingDiscountingMethod METHOD_CPN_IBOR_GEARING = 
      CouponIborGearingDiscountingMethod.getInstance();
  private static final CouponIborCompoundingDiscountingMethod METHOD_CPN_IBOR_COMP = 
      CouponIborCompoundingDiscountingMethod.getInstance();
  private static final CouponIborCompoundingSpreadDiscountingMethod METHOD_CPN_IBOR_COMP_SPREAD = 
      CouponIborCompoundingSpreadDiscountingMethod.getInstance();
  private static final CouponIborCompoundingFlatSpreadDiscountingMethod METHOD_CPN_IBOR_COMP_FLAT_SPREAD = 
      CouponIborCompoundingFlatSpreadDiscountingMethod.getInstance();
  private static final CouponIborCompoundingSimpleSpreadDiscountingMethod METHOD_CPN_IBOR_COMP_SIMPLE_SPREAD = 
      CouponIborCompoundingSimpleSpreadDiscountingMethod.getInstance();
  private static final CouponONDiscountingMethod METHOD_CPN_ON = CouponONDiscountingMethod.getInstance();
  private static final CouponONSpreadDiscountingMethod METHOD_CPN_ON_SPREAD = 
      CouponONSpreadDiscountingMethod.getInstance();
  private static final CouponONArithmeticAverageDiscountingApproxMethod METHOD_CPN_AAON = 
      CouponONArithmeticAverageDiscountingApproxMethod.getInstance();
  private static final CouponONArithmeticAverageSpreadDiscountingApproxMethod METHOD_CPN_AAON_SPREAD = 
      CouponONArithmeticAverageSpreadDiscountingApproxMethod.getInstance();
  private static final CouponONArithmeticAverageSpreadSimplifiedDiscountingApproxMethod METHOD_CPN_ONAA_SPREADSIMPL =
      CouponONArithmeticAverageSpreadSimplifiedDiscountingApproxMethod.getInstance();
  private static final ForwardRateAgreementDiscountingMethod METHOD_FRA = ForwardRateAgreementDiscountingMethod.getInstance();
  private static final ForexDiscountingMethod METHOD_FOREX = ForexDiscountingMethod.getInstance();
  private static final ForexSwapDiscountingMethod METHOD_FOREX_SWAP = ForexSwapDiscountingMethod.getInstance();
  private static final ForexNonDeliverableForwardDiscountingMethod METHOD_FOREX_NDF = 
      ForexNonDeliverableForwardDiscountingMethod.getInstance();
  private static final FuturesTransactionMulticurveMethod METHOD_FUT = new FuturesTransactionMulticurveMethod();
  private static final CouponFixedAccruedCompoundingDiscountingMethod METHOD_CPN_FIXED_ACCRUED_COMPOUNDING = 
      CouponFixedAccruedCompoundingDiscountingMethod.getInstance();
  private static final CouponONCompoundedDiscountingMethod METHOD_CPN_ON_COMPOUNDING = 
      CouponONCompoundedDiscountingMethod.getInstance();
  private static final InterpolatedStubPresentValueDiscountingCalculator METHOD_CPN_INTERP_STUB = 
      InterpolatedStubPresentValueDiscountingCalculator.getInstance();
  private static final CouponIborAverageFixingDatesDiscountingMethod METHOD_CPN_IBOR_AVERAGE_FIXING_DATES = 
      CouponIborAverageFixingDatesDiscountingMethod.getInstance();
  private static final CouponIborAverageFixingDatesCompoundingDiscountingMethod METHOD_CPN_IBOR_AVERAGE_CMP = 
      CouponIborAverageFixingDatesCompoundingDiscountingMethod.getInstance();
  private static final CouponIborAverageFixingDatesCompoundingFlatSpreadDiscountingMethod METHOD_CPN_IBOR_FLAT_CMP_SPREAD =
      CouponIborAverageFixingDatesCompoundingFlatSpreadDiscountingMethod.getInstance();

  //     -----     Deposit     -----

  @Override
  public MultiCurrencyAmount visitCash(final Cash deposit, final ParameterProviderInterface multicurve) {
    return METHOD_DEPOSIT.presentValue(deposit, multicurve.getMulticurveProvider());
  }

  @Override
  public MultiCurrencyAmount visitDepositIbor(final DepositIbor deposit, final ParameterProviderInterface multicurve) {
    return METHOD_DEPOSIT_IBOR.presentValue(deposit, multicurve.getMulticurveProvider());
  }

  // -----     Payment/Coupon     ------

  @Override
  public MultiCurrencyAmount visitFixedPayment(final PaymentFixed payment, final ParameterProviderInterface multicurve) {
    return METHOD_PAY_FIXED.presentValue(payment, multicurve.getMulticurveProvider());
  }

  @Override
  public MultiCurrencyAmount visitCouponFixed(final CouponFixed coupon, final ParameterProviderInterface multicurve) {
    return METHOD_CPN_FIXED.presentValue(coupon, multicurve.getMulticurveProvider());
  }

  @Override
  public MultiCurrencyAmount visitCouponFixedCompounding(final CouponFixedCompounding coupon,
      final ParameterProviderInterface multicurve) {
    return METHOD_CPN_FIXED_COMPOUNDING.presentValue(coupon, multicurve.getMulticurveProvider());
  }

  @Override
  public MultiCurrencyAmount visitCouponFixedFxReset(final CouponFixedFxReset coupon,
      final ParameterProviderInterface multicurve) {
    return METHOD_CPN_FIXED_FXRESET.presentValue(coupon, multicurve.getMulticurveProvider());
  }

  @Override
  public MultiCurrencyAmount visitCouponIborFxReset(final CouponIborFxReset coupon,
      final ParameterProviderInterface multicurve) {
    return METHOD_CPN_IBOR_FXRESET.presentValue(coupon, multicurve.getMulticurveProvider());
  }

  @Override
  public MultiCurrencyAmount visitInterpolatedStubCoupon(
      final InterpolatedStubCoupon<? extends DepositIndexCoupon<? extends IndexDeposit>, ? extends IndexDeposit> payment,
      final ParameterProviderInterface data) {
    return payment.getFullCoupon().accept(METHOD_CPN_INTERP_STUB, InterpolatedStubData.of(data.getMulticurveProvider(), payment));
  }

  @Override
  public MultiCurrencyAmount visitCouponIbor(final CouponIbor coupon, final ParameterProviderInterface multicurve) {
    return METHOD_CPN_IBOR.presentValue(coupon, multicurve.getMulticurveProvider());
  }

  @Override
  public MultiCurrencyAmount visitCouponIborAverage(final CouponIborAverage coupon, final ParameterProviderInterface multicurve) {
    return METHOD_CPN_IBOR_AVERAGE.presentValue(coupon, multicurve.getMulticurveProvider());
  }

  @Override
  public MultiCurrencyAmount visitCouponIborSpread(final CouponIborSpread coupon, final ParameterProviderInterface multicurve) {
    return METHOD_CPN_IBOR_SPREAD.presentValue(coupon, multicurve.getMulticurveProvider());
  }

  @Override
  public MultiCurrencyAmount visitCouponIborGearing(final CouponIborGearing coupon, final ParameterProviderInterface multicurve) {
    return METHOD_CPN_IBOR_GEARING.presentValue(coupon, multicurve.getMulticurveProvider());
  }

  @Override
  public MultiCurrencyAmount visitCouponIborCompounding(final CouponIborCompounding coupon,
      final ParameterProviderInterface multicurve) {
    return METHOD_CPN_IBOR_COMP.presentValue(coupon, multicurve.getMulticurveProvider());
  }

  @Override
  public MultiCurrencyAmount visitCouponIborCompoundingSpread(final CouponIborCompoundingSpread coupon,
      final ParameterProviderInterface multicurve) {
    return METHOD_CPN_IBOR_COMP_SPREAD.presentValue(coupon, multicurve.getMulticurveProvider());
  }

  @Override
  public MultiCurrencyAmount visitCouponIborCompoundingFlatSpread(final CouponIborCompoundingFlatSpread coupon,
      final ParameterProviderInterface multicurve) {
    return METHOD_CPN_IBOR_COMP_FLAT_SPREAD.presentValue(coupon, multicurve.getMulticurveProvider());
  }

  @Override
  public MultiCurrencyAmount visitCouponIborCompoundingSimpleSpread(final CouponIborCompoundingSimpleSpread coupon,
      final ParameterProviderInterface multicurve) {
    return METHOD_CPN_IBOR_COMP_SIMPLE_SPREAD.presentValue(coupon, multicurve.getMulticurveProvider());
  }

  @Override
  public MultiCurrencyAmount visitCouponOIS(final CouponON coupon, final ParameterProviderInterface multicurve) {
    return METHOD_CPN_ON.presentValue(coupon, multicurve.getMulticurveProvider());
  }

  @Override
  public MultiCurrencyAmount visitCouponONSpread(final CouponONSpread coupon, final ParameterProviderInterface multicurve) {
    return METHOD_CPN_ON_SPREAD.presentValue(coupon, multicurve.getMulticurveProvider());
  }

  @Override
  public MultiCurrencyAmount visitCouponONArithmeticAverage(final CouponONArithmeticAverage coupon,
      final ParameterProviderInterface multicurve) {
    return METHOD_CPN_AAON.presentValue(coupon, multicurve.getMulticurveProvider());
  }

  @Override
  public MultiCurrencyAmount visitCouponONArithmeticAverageSpread(CouponONArithmeticAverageSpread coupon,
      ParameterProviderInterface multicurve) {
    return METHOD_CPN_AAON_SPREAD.presentValue(coupon, multicurve.getMulticurveProvider());
  }

  @Override
  public MultiCurrencyAmount visitCouponONArithmeticAverageSpreadSimplified(final CouponONArithmeticAverageSpreadSimplified coupon,
      final ParameterProviderInterface multicurve) {
    return METHOD_CPN_ONAA_SPREADSIMPL.presentValue(coupon, multicurve.getMulticurveProvider());
  }

  @Override
  public MultiCurrencyAmount visitForwardRateAgreement(final ForwardRateAgreement fra, final ParameterProviderInterface multicurve) {
    return METHOD_FRA.presentValue(fra, multicurve.getMulticurveProvider());
  }

  @Override
  public MultiCurrencyAmount visitCouponFixedAccruedCompounding(final CouponFixedAccruedCompounding coupon,
      final ParameterProviderInterface multicurve) {
    return METHOD_CPN_FIXED_ACCRUED_COMPOUNDING.presentValue(coupon, multicurve.getMulticurveProvider());
  }

  @Override
  public MultiCurrencyAmount visitCouponONCompounded(final CouponONCompounded coupon, final ParameterProviderInterface multicurve) {
    return METHOD_CPN_ON_COMPOUNDING.presentValue(coupon, multicurve.getMulticurveProvider());
  }

  @Override
  public MultiCurrencyAmount visitCouponIborAverageFixingDates(final CouponIborAverageFixingDates coupon,
      final ParameterProviderInterface multicurve) {
    return METHOD_CPN_IBOR_AVERAGE_FIXING_DATES.presentValue(coupon, multicurve.getMulticurveProvider());
  }

  @Override
  public MultiCurrencyAmount visitCouponIborAverageCompounding(final CouponIborAverageFixingDatesCompounding coupon,
      final ParameterProviderInterface multicurve) {
    return METHOD_CPN_IBOR_AVERAGE_CMP.presentValue(coupon, multicurve.getMulticurveProvider());
  }

  @Override
  public MultiCurrencyAmount visitCouponIborAverageFlatCompoundingSpread(final CouponIborAverageFixingDatesCompoundingFlatSpread coupon,
      final ParameterProviderInterface multicurve) {
    return METHOD_CPN_IBOR_FLAT_CMP_SPREAD.presentValue(coupon, multicurve.getMulticurveProvider());
  }

  // -----     Annuity     ------

  @Override
  public MultiCurrencyAmount visitGenericAnnuity(final Annuity<? extends Payment> annuity,
      final ParameterProviderInterface multicurve) {
    ArgChecker.notNull(annuity, "Annuity");
    ArgChecker.notNull(multicurve, "multicurve");

    // TODO - this code previously used a mutable object, so there may be a performance overhead
    return IntStream.range(0, annuity.getNumberOfPayments())
        .mapToObj(i -> annuity.getNthPayment(i).accept(this, multicurve))
        .reduce(MultiCurrencyAmount::plus)
        .orElse(MultiCurrencyAmount.of());
  }

  @Override
  public MultiCurrencyAmount visitFixedCouponAnnuity(final AnnuityCouponFixed annuity,
      final ParameterProviderInterface multicurve) {
    return visitGenericAnnuity(annuity, multicurve);
  }

  // -----     Swap     ------

  @Override
  public MultiCurrencyAmount visitSwap(final Swap<?, ?> swap, final ParameterProviderInterface multicurve) {
    final MultiCurrencyAmount pv1 = swap.getFirstLeg().accept(this, multicurve);
    final MultiCurrencyAmount pv2 = swap.getSecondLeg().accept(this, multicurve);
    return pv1.plus(pv2);
  }

  @Override
  public MultiCurrencyAmount visitFixedCouponSwap(final SwapFixedCoupon<?> swap, final ParameterProviderInterface multicurves) {
    return visitSwap(swap, multicurves);
  }

  @Override
  public MultiCurrencyAmount visitSwapMultileg(final SwapMultileg swap, final ParameterProviderInterface multicurve) {
    final int nbLegs = swap.getLegs().length;
    MultiCurrencyAmount pv = swap.getLegs()[0].accept(this, multicurve);
    for (int loopleg = 1; loopleg < nbLegs; loopleg++) {
      pv = pv.plus(swap.getLegs()[loopleg].accept(this, multicurve));
    }
    return pv;
  }

  // -----     Futures     ------

  @Override
  public MultiCurrencyAmount visitFederalFundsFutureTransaction(final FederalFundsFutureTransaction futures,
      final ParameterProviderInterface multicurves) {
    return METHOD_FUT.presentValue(futures, multicurves);
  }

  @Override
  public MultiCurrencyAmount visitInterestRateFutureTransaction(final InterestRateFutureTransaction future,
      final ParameterProviderInterface multicurves) {
    return METHOD_FUT.presentValue(future, multicurves);
  }

  @Override
  public MultiCurrencyAmount visitSwapFuturesPriceDeliverableTransaction(final SwapFuturesPriceDeliverableTransaction future,
      final ParameterProviderInterface multicurves) {
    return METHOD_FUT.presentValue(future, multicurves);
  }

  // -----     Forex     ------

  @Override
  public MultiCurrencyAmount visitForex(final Forex derivative, final ParameterProviderInterface multicurves) {
    return METHOD_FOREX.presentValue(derivative, multicurves.getMulticurveProvider());
  }

  @Override
  public MultiCurrencyAmount visitForexSwap(final ForexSwap derivative, final ParameterProviderInterface multicurves) {
    return METHOD_FOREX_SWAP.presentValue(derivative, multicurves.getMulticurveProvider());
  }

  @Override
  public MultiCurrencyAmount visitForexNonDeliverableForward(final ForexNonDeliverableForward derivative,
      final ParameterProviderInterface multicurves) {
    return METHOD_FOREX_NDF.presentValue(derivative, multicurves.getMulticurveProvider());
  }

}

/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.discounting;

import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.derivative.ForexSwap;
import com.opengamma.analytics.financial.forex.provider.ForexDiscountingMethod;
import com.opengamma.analytics.financial.forex.provider.ForexSwapDiscountingMethod;
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
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Calculator of the present value curve sensitivity as multiple currency interest rate curve sensitivity.
 */

public final class PresentValueCurveSensitivityDiscountingCalculator 
  extends InstrumentDerivativeVisitorAdapter<ParameterProviderInterface, MultipleCurrencyMulticurveSensitivity> {
  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueCurveSensitivityDiscountingCalculator INSTANCE = new PresentValueCurveSensitivityDiscountingCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueCurveSensitivityDiscountingCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PresentValueCurveSensitivityDiscountingCalculator() {
  }

  /**
   * The methods used by the different instruments.
   */
  private static final CashDiscountingMethod METHOD_DEPOSIT = CashDiscountingMethod.getInstance();
  private static final DepositIborDiscountingMethod METHOD_DEPOSIT_IBOR = DepositIborDiscountingMethod.getInstance();
  private static final PaymentFixedDiscountingMethod METHOD_PAY_FIXED = PaymentFixedDiscountingMethod.getInstance();
  private static final CouponFixedDiscountingMethod METHOD_CPN_FIXED = CouponFixedDiscountingMethod.getInstance();
  private static final CouponFixedCompoundingDiscountingMethod METHOD_CPN_FIXED_COMPOUNDING = CouponFixedCompoundingDiscountingMethod.getInstance();
  private static final CouponFixedFxResetDiscountingMethod METHOD_CPN_FIXED_FXRESET = 
      CouponFixedFxResetDiscountingMethod.getInstance();
  private static final CouponIborFxResetDiscountingMethod METHOD_CPN_IBOR_FXRESET =
      CouponIborFxResetDiscountingMethod.getInstance();
  private static final CouponIborDiscountingMethod METHOD_CPN_IBOR = CouponIborDiscountingMethod.getInstance();
  private static final CouponIborAverageDiscountingMethod METHOD_CPN_IBOR_AVERAGE = CouponIborAverageDiscountingMethod.getInstance();
  private static final CouponIborSpreadDiscountingMethod METHOD_CPN_IBOR_SPREAD = CouponIborSpreadDiscountingMethod.getInstance();
  private static final CouponIborGearingDiscountingMethod METHOD_CPN_IBOR_GEARING = CouponIborGearingDiscountingMethod.getInstance();
  private static final CouponIborCompoundingDiscountingMethod METHOD_CPN_IBOR_COMP = CouponIborCompoundingDiscountingMethod.getInstance();
  private static final CouponIborCompoundingSpreadDiscountingMethod METHOD_CPN_IBOR_COMP_SPREAD = CouponIborCompoundingSpreadDiscountingMethod.getInstance();
  private static final CouponIborCompoundingFlatSpreadDiscountingMethod METHOD_CPN_IBOR_COMP_FLAT_SPREAD = CouponIborCompoundingFlatSpreadDiscountingMethod.getInstance();
  private static final CouponIborCompoundingSimpleSpreadDiscountingMethod METHOD_CPN_IBOR_COMP_SIMPLE_SPREAD = CouponIborCompoundingSimpleSpreadDiscountingMethod.getInstance();
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
  private static final FuturesTransactionMulticurveMethod METHOD_FUT = new FuturesTransactionMulticurveMethod();
  private static final CouponFixedAccruedCompoundingDiscountingMethod METHOD_CPN_FIXED_ACCRUED_COMPOUNDING = CouponFixedAccruedCompoundingDiscountingMethod.getInstance();
  private static final CouponONCompoundedDiscountingMethod METHOD_CPN_ON_COMPOUNDING = CouponONCompoundedDiscountingMethod.getInstance();
  private static final CouponIborAverageFixingDatesDiscountingMethod METHOD_CPN_IBOR_AVERAGE_FIXING_DATES = CouponIborAverageFixingDatesDiscountingMethod.getInstance();
  private static final CouponIborAverageFixingDatesCompoundingDiscountingMethod METHOD_CPN_IBOR_AVERAGE_CMP = CouponIborAverageFixingDatesCompoundingDiscountingMethod.getInstance();
  private static final CouponIborAverageFixingDatesCompoundingFlatSpreadDiscountingMethod METHOD_CPN_IBOR_FLAT_CMP_SPREAD = 
      CouponIborAverageFixingDatesCompoundingFlatSpreadDiscountingMethod.getInstance();

  // -----     Deposit     ------

  @Override
  public MultipleCurrencyMulticurveSensitivity visitCash(final Cash cash, final ParameterProviderInterface multicurve) {
    return METHOD_DEPOSIT.presentValueCurveSensitivity(cash, multicurve.getMulticurveProvider());
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visitDepositIbor(final DepositIbor deposit, final ParameterProviderInterface multicurve) {
    return METHOD_DEPOSIT_IBOR.presentValueCurveSensitivity(deposit, multicurve.getMulticurveProvider());
  }

  // -----     Payment/Coupon     ------

  @Override
  public MultipleCurrencyMulticurveSensitivity visitFixedPayment(final PaymentFixed payment, final ParameterProviderInterface multicurve) {
    return METHOD_PAY_FIXED.presentValueCurveSensitivity(payment, multicurve.getMulticurveProvider());
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visitCouponFixed(final CouponFixed payment, final ParameterProviderInterface multicurve) {
    return METHOD_CPN_FIXED.presentValueCurveSensitivity(payment, multicurve.getMulticurveProvider());
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visitCouponFixedCompounding(final CouponFixedCompounding payment, final ParameterProviderInterface multicurve) {
    return METHOD_CPN_FIXED_COMPOUNDING.presentValueCurveSensitivity(payment, multicurve.getMulticurveProvider());
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visitCouponFixedFxReset(final CouponFixedFxReset coupon, 
      final ParameterProviderInterface multicurve) {
    return METHOD_CPN_FIXED_FXRESET.presentValueCurveSensitivity(coupon, multicurve.getMulticurveProvider());
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visitCouponIborFxReset(final CouponIborFxReset coupon,
      final ParameterProviderInterface multicurve) {
    return METHOD_CPN_IBOR_FXRESET.presentValueCurveSensitivity(coupon, multicurve.getMulticurveProvider());
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visitCouponIbor(final CouponIbor payment, final ParameterProviderInterface multicurve) {
    return METHOD_CPN_IBOR.presentValueCurveSensitivity(payment, multicurve.getMulticurveProvider());
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visitCouponIborAverage(final CouponIborAverage payment, final ParameterProviderInterface multicurve) {
    return METHOD_CPN_IBOR_AVERAGE.presentValueCurveSensitivity(payment, multicurve.getMulticurveProvider());
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visitCouponIborSpread(final CouponIborSpread payment, final ParameterProviderInterface multicurve) {
    return METHOD_CPN_IBOR_SPREAD.presentValueCurveSensitivity(payment, multicurve.getMulticurveProvider());
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visitCouponIborGearing(final CouponIborGearing payment, final ParameterProviderInterface multicurve) {
    return METHOD_CPN_IBOR_GEARING.presentValueCurveSensitivity(payment, multicurve.getMulticurveProvider());
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visitCouponIborCompounding(final CouponIborCompounding payment, final ParameterProviderInterface multicurve) {
    return METHOD_CPN_IBOR_COMP.presentValueCurveSensitivity(payment, multicurve.getMulticurveProvider());
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visitCouponIborCompoundingSpread(final CouponIborCompoundingSpread payment, final ParameterProviderInterface multicurve) {
    return METHOD_CPN_IBOR_COMP_SPREAD.presentValueCurveSensitivity(payment, multicurve.getMulticurveProvider());
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visitCouponIborCompoundingFlatSpread(final CouponIborCompoundingFlatSpread coupon, final ParameterProviderInterface multicurve) {
    return METHOD_CPN_IBOR_COMP_FLAT_SPREAD.presentValueCurveSensitivity(coupon, multicurve.getMulticurveProvider());
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visitCouponIborCompoundingSimpleSpread(final CouponIborCompoundingSimpleSpread coupon, final ParameterProviderInterface multicurve) {
    return METHOD_CPN_IBOR_COMP_SIMPLE_SPREAD.presentValueCurveSensitivity(coupon, multicurve.getMulticurveProvider());
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visitCouponOIS(final CouponON payment, final ParameterProviderInterface multicurve) {
    return METHOD_CPN_ON.presentValueCurveSensitivity(payment, multicurve.getMulticurveProvider());
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visitCouponONSpread(final CouponONSpread payment, final ParameterProviderInterface multicurve) {
    return METHOD_CPN_ON_SPREAD.presentValueCurveSensitivity(payment, multicurve.getMulticurveProvider());
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visitCouponONArithmeticAverage(final CouponONArithmeticAverage payment, final ParameterProviderInterface multicurve) {
    return METHOD_CPN_AAON.presentValueCurveSensitivity(payment, multicurve.getMulticurveProvider());
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visitCouponONArithmeticAverageSpread(
      final CouponONArithmeticAverageSpread coupon, final ParameterProviderInterface multicurve) {
    return METHOD_CPN_AAON_SPREAD.presentValueCurveSensitivity(coupon, multicurve.getMulticurveProvider());
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visitCouponONArithmeticAverageSpreadSimplified(final CouponONArithmeticAverageSpreadSimplified coupon, final ParameterProviderInterface multicurve) {
    return METHOD_CPN_ONAA_SPREADSIMPL.presentValueCurveSensitivity(coupon, multicurve.getMulticurveProvider());
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visitForwardRateAgreement(final ForwardRateAgreement fra, final ParameterProviderInterface multicurve) {
    return METHOD_FRA.presentValueCurveSensitivity(fra, multicurve.getMulticurveProvider());
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visitCouponFixedAccruedCompounding(final CouponFixedAccruedCompounding coupon, final ParameterProviderInterface multicurve) {
    return METHOD_CPN_FIXED_ACCRUED_COMPOUNDING.presentValueCurveSensitivity(coupon, multicurve.getMulticurveProvider());
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visitCouponONCompounded(final CouponONCompounded coupon, final ParameterProviderInterface multicurve) {
    return METHOD_CPN_ON_COMPOUNDING.presentValueCurveSensitivity(coupon, multicurve.getMulticurveProvider());
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visitCouponIborAverageFixingDates(final CouponIborAverageFixingDates coupon, final ParameterProviderInterface multicurve) {
    return METHOD_CPN_IBOR_AVERAGE_FIXING_DATES.presentValueCurveSensitivity(coupon, multicurve.getMulticurveProvider());
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visitCouponIborAverageCompounding(final CouponIborAverageFixingDatesCompounding coupon, final ParameterProviderInterface multicurve) {
    return METHOD_CPN_IBOR_AVERAGE_CMP.presentValueCurveSensitivity(coupon, multicurve.getMulticurveProvider());
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visitCouponIborAverageFlatCompoundingSpread(
      final CouponIborAverageFixingDatesCompoundingFlatSpread coupon, final ParameterProviderInterface multicurve) {
    return METHOD_CPN_IBOR_FLAT_CMP_SPREAD.presentValueCurveSensitivity(coupon, multicurve.getMulticurveProvider());
  }

  // -----     Annuity     ------

  @Override
  public MultipleCurrencyMulticurveSensitivity visitGenericAnnuity(final Annuity<? extends Payment> annuity, final ParameterProviderInterface multicurve) {
    ArgChecker.notNull(annuity, "Annuity");
    ArgChecker.notNull(multicurve, "multicurve");
    MultipleCurrencyMulticurveSensitivity cs = annuity.getNthPayment(0).accept(this, multicurve);
    for (int loopp = 1; loopp < annuity.getNumberOfPayments(); loopp++) {
      cs = cs.plus(annuity.getNthPayment(loopp).accept(this, multicurve));
    }
    return cs;
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visitFixedCouponAnnuity(final AnnuityCouponFixed annuity, final ParameterProviderInterface multicurve) {
    return visitGenericAnnuity(annuity, multicurve);
  }

  // -----     Swap     ------

  @Override
  public MultipleCurrencyMulticurveSensitivity visitSwap(final Swap<?, ?> swap, final ParameterProviderInterface multicurve) {
    final MultipleCurrencyMulticurveSensitivity sensitivity1 = swap.getFirstLeg().accept(this, multicurve);
    final MultipleCurrencyMulticurveSensitivity sensitivity2 = swap.getSecondLeg().accept(this, multicurve);
    return sensitivity1.plus(sensitivity2);
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visitFixedCouponSwap(final SwapFixedCoupon<?> swap, final ParameterProviderInterface multicurve) {
    return visitSwap(swap, multicurve);
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visitSwapMultileg(final SwapMultileg swap, final ParameterProviderInterface multicurve) {
    final int nbLegs = swap.getLegs().length;
    MultipleCurrencyMulticurveSensitivity pvcs = swap.getLegs()[0].accept(this, multicurve);
    for (int loopleg = 1; loopleg < nbLegs; loopleg++) {
      pvcs = pvcs.plus(swap.getLegs()[loopleg].accept(this, multicurve));
    }
    return pvcs;
  }

  // -----     Futures     ------

  @Override
  public MultipleCurrencyMulticurveSensitivity visitFederalFundsFutureTransaction(final FederalFundsFutureTransaction futures, final ParameterProviderInterface multicurves) {
    return METHOD_FUT.presentValueCurveSensitivity(futures, multicurves);
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visitInterestRateFutureTransaction(final InterestRateFutureTransaction futures, final ParameterProviderInterface multicurves) {
    return METHOD_FUT.presentValueCurveSensitivity(futures, multicurves);
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visitSwapFuturesPriceDeliverableTransaction(final SwapFuturesPriceDeliverableTransaction future, final ParameterProviderInterface multicurves) {
    return METHOD_FUT.presentValueCurveSensitivity(future, multicurves);
  }

  // -----     Forex     ------

  @Override
  public MultipleCurrencyMulticurveSensitivity visitForex(final Forex derivative, final ParameterProviderInterface multicurves) {
    return METHOD_FOREX.presentValueCurveSensitivity(derivative, multicurves.getMulticurveProvider());
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visitForexSwap(final ForexSwap derivative, final ParameterProviderInterface multicurves) {
    return METHOD_FOREX_SWAP.presentValueCurveSensitivity(derivative, multicurves.getMulticurveProvider());
  }

}

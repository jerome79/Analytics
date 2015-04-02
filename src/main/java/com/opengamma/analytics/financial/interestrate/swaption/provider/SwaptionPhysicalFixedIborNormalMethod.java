/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.provider;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.analytics.convention.daycount.DayCount;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.interestrate.sensitivity.PresentValueSwaptionSurfaceSensitivity;
import com.opengamma.analytics.financial.interestrate.swap.provider.SwapFixedCouponDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.NormalFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.NormalPriceFunction;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.NormalSwaptionProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.strata.basics.currency.MultiCurrencyAmount;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.collect.ArgChecker;
import com.opengamma.strata.collect.tuple.DoublesPair;

/**
 *  Class used to compute the price and sensitivity of a physical delivery swaption with normal (Bachelier) model.
 *  The implied normal volatilities are expiry and underlying maturity dependent (no smile).
 *  The swap underlying the swaption should be a Fixed for Ibor (without spread) swap.
 */
public final class SwaptionPhysicalFixedIborNormalMethod {

  /**
   * The method unique instance.
   */
  private static final SwaptionPhysicalFixedIborNormalMethod INSTANCE = new SwaptionPhysicalFixedIborNormalMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static SwaptionPhysicalFixedIborNormalMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private SwaptionPhysicalFixedIborNormalMethod() {
  }

  /**
   * The calculator and methods.
   */
  private static final ParRateDiscountingCalculator PRDC = ParRateDiscountingCalculator.getInstance();
  private static final ParRateCurveSensitivityDiscountingCalculator PRCSDC = 
      ParRateCurveSensitivityDiscountingCalculator.getInstance();
  private static final SwapFixedCouponDiscountingMethod METHOD_SWAP = SwapFixedCouponDiscountingMethod.getInstance();

  /**
   * Computes the present value of a physical delivery European swaption in the normal model.
   * @param swaption The swaption.
   * @param multicurveParameters Normal volatility for swaption and multi-curve provider.
   * @return The present value.
   */
  public MultiCurrencyAmount presentValue(final SwaptionPhysicalFixedIbor swaption,
      final NormalSwaptionProviderInterface multicurveParameters) {
    ArgChecker.notNull(swaption, "Swaption");
    ArgChecker.notNull(multicurveParameters, "normal volatility for swaption and multicurve");
    MulticurveProviderInterface multicurve = multicurveParameters.getMulticurveProvider();
    GeneratorSwapFixedIbor generatorSwap = multicurveParameters.getGeneratorSwap();
    DayCount dayCountModification = generatorSwap.getFixedLegDayCount();
    HolidayCalendar calendar = generatorSwap.getCalendar();
    double pvbpModified = METHOD_SWAP.presentValueBasisPoint(swaption.getUnderlyingSwap(), dayCountModification,
        calendar, multicurve);
    double forwardModified = PRDC.visitFixedCouponSwap(swaption.getUnderlyingSwap(), dayCountModification, multicurve);
    double strikeModified = METHOD_SWAP.couponEquivalent(swaption.getUnderlyingSwap(), pvbpModified, multicurve);
    double expiry = swaption.getTimeToExpiry();
    double volatility = multicurveParameters.getVolatility(expiry, swaption.getMaturityTime(), strikeModified, 
        forwardModified);
    NormalFunctionData normalData = new NormalFunctionData(forwardModified, pvbpModified, volatility);
    EuropeanVanillaOption option = new EuropeanVanillaOption(strikeModified, expiry, swaption.isCall());
    // Implementation note: option required to pass the strike (in case the swap has non-constant coupon).
    NormalPriceFunction normalFunction = new NormalPriceFunction();
    Function1D<NormalFunctionData, Double> func = normalFunction.getPriceFunction(option);
    double pv = func.evaluate(normalData) * (swaption.isLong() ? 1.0 : -1.0);
    return MultiCurrencyAmount.of(swaption.getCurrency(), pv);
  }

  /**
   * Computes the implied Black volatility of the vanilla swaption.
   * @param swaption The swaption.
   * @param multicurveParameters Normal volatility for swaption and multi-curve provider.
   * @return The implied volatility.
   */
  public double impliedVolatility(final SwaptionPhysicalFixedIbor swaption, 
      final NormalSwaptionProviderInterface multicurveParameters) {
    ArgChecker.notNull(swaption, "Swaption");
    ArgChecker.notNull(multicurveParameters, "normal volatility for swaption and multicurve");
    MulticurveProviderInterface multicurve = multicurveParameters.getMulticurveProvider();
    GeneratorSwapFixedIbor generatorSwap = multicurveParameters.getGeneratorSwap();
    DayCount dayCountModification = generatorSwap.getFixedLegDayCount();
    HolidayCalendar calendar = generatorSwap.getCalendar();
    double pvbpModified = METHOD_SWAP.presentValueBasisPoint(swaption.getUnderlyingSwap(), dayCountModification,
        calendar, multicurve);
    double forwardModified = PRDC.visitFixedCouponSwap(swaption.getUnderlyingSwap(), dayCountModification, multicurve);
    double strikeModified = METHOD_SWAP.couponEquivalent(swaption.getUnderlyingSwap(), pvbpModified, multicurve);
    double expiry = swaption.getTimeToExpiry();
    double volatility = multicurveParameters.getVolatility(expiry, swaption.getMaturityTime(), strikeModified, 
        forwardModified);
    return volatility;
  }

  /**
   * Computes the present value rate sensitivity to rates of a physical delivery European swaption in the SABR model.
   * @param swaption The swaption.
   * @param multicurveParameters Normal volatility for swaption and multi-curve provider.
   * @return The present value curve sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final SwaptionPhysicalFixedIbor swaption, 
      final NormalSwaptionProviderInterface multicurveParameters) {
    ArgChecker.notNull(swaption, "Swaption");
    ArgChecker.notNull(multicurveParameters, "normal volatility for swaption and multicurve");
    MulticurveProviderInterface multicurve = multicurveParameters.getMulticurveProvider();
    GeneratorSwapFixedIbor generatorSwap = multicurveParameters.getGeneratorSwap();
    DayCount dayCountModification = generatorSwap.getFixedLegDayCount();
    HolidayCalendar calendar = generatorSwap.getCalendar();
    double pvbpModified = METHOD_SWAP.presentValueBasisPoint(swaption.getUnderlyingSwap(), dayCountModification,
        calendar, multicurve);
    double forwardModified = PRDC.visitFixedCouponSwap(swaption.getUnderlyingSwap(), dayCountModification, multicurve);
    double strikeModified = METHOD_SWAP.couponEquivalent(swaption.getUnderlyingSwap(), pvbpModified, multicurve);
    double expiry = swaption.getTimeToExpiry();
    double volatility = multicurveParameters.getVolatility(expiry, swaption.getMaturityTime(), strikeModified, 
        forwardModified);
    NormalFunctionData normalData = new NormalFunctionData(forwardModified, 1.0, volatility);
    EuropeanVanillaOption option = new EuropeanVanillaOption(strikeModified, expiry, swaption.isCall());
    // Strictly speaking, the strike equivalent is curve dependent; that dependency is ignored.
    // Option required to pass the strike (in case the swap has non-constant coupon).
    NormalPriceFunction normalFunction = new NormalPriceFunction();
    // Derivative of the forward and pvbp with respect to the rates.
    MulticurveSensitivity forwardModifiedDr = PRCSDC.visitFixedCouponSwap(swaption.getUnderlyingSwap(), 
        dayCountModification, multicurve);
    MulticurveSensitivity pvbpModifiedDr = METHOD_SWAP.presentValueBasisPointCurveSensitivity(
        swaption.getUnderlyingSwap(), dayCountModification, calendar, multicurve);
    double[] normAdjoint = new double[3];
    double pv = normalFunction.getPriceAdjoint(option, normalData, normAdjoint);
    MulticurveSensitivity result = pvbpModifiedDr.multipliedBy(pv);
    result = result.plus(forwardModifiedDr.multipliedBy(pvbpModified * normAdjoint[0]));
    if (!swaption.isLong()) {
      result = result.multipliedBy(-1);
    }
    return MultipleCurrencyMulticurveSensitivity.of(swaption.getCurrency(), result);
  }

  /**
   * Computes the present value sensitivity to the normal volatility (also called vega) of a physical delivery 
   * European swaption in the normal swaption model.
   * @param swaption The swaption.
   * @param multicurveParameters Normal volatility for swaption and multi-curve provider.
   * @return The present value sensitivity to normal volatility.
   */
  public PresentValueSwaptionSurfaceSensitivity presentValueVolatilitySensitivity(final SwaptionPhysicalFixedIbor swaption,
      final NormalSwaptionProviderInterface multicurveParameters) {
    ArgChecker.notNull(swaption, "Swaption");
    ArgChecker.notNull(multicurveParameters, "normal volatility for swaption and multicurve");
    MulticurveProviderInterface multicurve = multicurveParameters.getMulticurveProvider();
    GeneratorSwapFixedIbor generatorSwap = multicurveParameters.getGeneratorSwap();
    DayCount dayCountModification = generatorSwap.getFixedLegDayCount();
    HolidayCalendar calendar = generatorSwap.getCalendar();
    final double pvbpModified = METHOD_SWAP.presentValueBasisPoint(swaption.getUnderlyingSwap(), dayCountModification,
        calendar, multicurve);
    double forwardModified = PRDC.visitFixedCouponSwap(swaption.getUnderlyingSwap(), dayCountModification, multicurve);
    double strikeModified = METHOD_SWAP.couponEquivalent(swaption.getUnderlyingSwap(), pvbpModified, multicurve);
    double expiry = swaption.getTimeToExpiry();
    double tenor = swaption.getMaturityTime();
    double volatility = multicurveParameters.getVolatility(expiry, tenor, strikeModified, forwardModified);
    NormalFunctionData normalData = new NormalFunctionData(forwardModified, 1.0, volatility);
    EuropeanVanillaOption option = new EuropeanVanillaOption(strikeModified, expiry, swaption.isCall());
    // Option required to pass the strike (in case the swap has non-constant coupon).
    NormalPriceFunction normalFunction = new NormalPriceFunction();
    double[] normAdjoint = new double[3];
    normalFunction.getPriceAdjoint(option, normalData, normAdjoint);
    DoublesPair point = DoublesPair.of(expiry, tenor);
    Map<DoublesPair, Double> sensitivity = new HashMap<>();
    sensitivity.put(point, normAdjoint[1] * pvbpModified * (swaption.isLong() ? 1.0 : -1.0));
    return new PresentValueSwaptionSurfaceSensitivity(sensitivity, generatorSwap);
  }

}

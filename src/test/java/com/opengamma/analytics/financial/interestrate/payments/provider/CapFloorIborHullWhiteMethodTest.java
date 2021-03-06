/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;

import static org.testng.AssertJUnit.assertEquals;

import java.time.ZonedDateTime;

import cern.jet.random.engine.MersenneTwister;
import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.payment.CapFloorIborDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.model.interestrate.HullWhiteOneFactorPiecewiseConstantInterestRateModel;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.analytics.financial.montecarlo.provider.HullWhiteMonteCarloMethod;
import com.opengamma.analytics.financial.provider.calculator.hullwhite.PresentValueCurveSensitivityHullWhiteCalculator;
import com.opengamma.analytics.financial.provider.calculator.hullwhite.PresentValueHullWhiteCalculator;
import com.opengamma.analytics.financial.provider.description.HullWhiteDataSets;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.sensitivity.hullwhite.ParameterSensitivityHullWhiteDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.analytics.math.random.NormalRandomNumberGenerator;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.analytics.util.time.DateUtils;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.currency.MultiCurrencyAmount;
import com.opengamma.strata.basics.date.HolidayCalendar;

/**
 * Tests on the Hull-White one factor method to price Cap/Floor on Ibor.
 */
@Test
public class CapFloorIborHullWhiteMethodTest {

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final HullWhiteOneFactorPiecewiseConstantParameters HW_PARAMETERS = HullWhiteDataSets.createHullWhiteParameters();
  private static final IborIndex EURIBOR3M = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd()[0];
  private static final Currency EUR = EURIBOR3M.getCurrency();
  private static final HolidayCalendar CALENDAR = MulticurveProviderDiscountDataSets.getEURCalendar();

  private static final HullWhiteOneFactorProviderDiscount HW_MULTICURVES = new HullWhiteOneFactorProviderDiscount(MULTICURVES, HW_PARAMETERS, EUR);
  // Cap/floor description
  private static final ZonedDateTime FIXING_DATE = DateUtils.getUTCDate(2011, 1, 3);
  private static final double NOTIONAL = 100000000; //100m
  private static final double STRIKE = 0.02;
  private static final boolean IS_CAP = true;
  private static final CapFloorIborDefinition CAP_LONG_DEFINITION = CapFloorIborDefinition.from(NOTIONAL, FIXING_DATE, EURIBOR3M, STRIKE, IS_CAP, CALENDAR);
  private static final CapFloorIborDefinition CAP_SHORT_DEFINITION = CapFloorIborDefinition.from(-NOTIONAL, FIXING_DATE, EURIBOR3M, STRIKE, IS_CAP, CALENDAR);
  private static final CapFloorIborDefinition PUT_LONG_DEFINITION = CapFloorIborDefinition.from(NOTIONAL, FIXING_DATE, EURIBOR3M, STRIKE, !IS_CAP, CALENDAR);
  // To derivative
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2008, 8, 18);
  private static final CapFloorIbor CAP_LONG = (CapFloorIbor) CAP_LONG_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final CapFloorIbor CAP_SHORT = (CapFloorIbor) CAP_SHORT_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final CapFloorIbor PUT_LONG = (CapFloorIbor) PUT_LONG_DEFINITION.toDerivative(REFERENCE_DATE);

  private static final CapFloorIborHullWhiteMethod METHOD_HW = CapFloorIborHullWhiteMethod.getInstance();
  private static final HullWhiteOneFactorPiecewiseConstantInterestRateModel MODEL = new HullWhiteOneFactorPiecewiseConstantInterestRateModel();

  private static final PresentValueHullWhiteCalculator PVHWC = PresentValueHullWhiteCalculator.getInstance();
  private static final PresentValueCurveSensitivityHullWhiteCalculator PVCSHWC = PresentValueCurveSensitivityHullWhiteCalculator.getInstance();

  private static final double SHIFT = 1.0E-6;

  private static final ParameterSensitivityParameterCalculator<HullWhiteOneFactorProviderInterface> PS_HW_C = new ParameterSensitivityParameterCalculator<>(PVCSHWC);
  private static final ParameterSensitivityHullWhiteDiscountInterpolatedFDCalculator PS_HW_FDC = new ParameterSensitivityHullWhiteDiscountInterpolatedFDCalculator(PVHWC, SHIFT);

  private static final int NB_PATH = 12500;
  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_DELTA = 1.0E+0; // 0.01 currency unit for 1bp

  public void presentValueStandard() {
    final double tp = CAP_LONG.getPaymentTime();
    final double t0 = CAP_LONG.getFixingPeriodStartTime();
    final double t1 = CAP_LONG.getFixingPeriodEndTime();
    final double theta = CAP_LONG.getFixingTime();
    final double deltaF = CAP_LONG.getFixingAccrualFactor();
    final double deltaP = CAP_LONG.getPaymentYearFraction();
    final double alpha0 = MODEL.alpha(HW_PARAMETERS, 0.0, theta, tp, t0);
    final double alpha1 = MODEL.alpha(HW_PARAMETERS, 0.0, theta, tp, t1);
    final double ptp = MULTICURVES.getDiscountFactor(EUR, tp);
    final double forward = MULTICURVES.getSimplyCompoundForwardRate(EURIBOR3M, t0, t1, CAP_LONG.getFixingAccrualFactor());
    double kappa = Math.log((1.0 + deltaF * STRIKE) / (1.0 + deltaF * forward));
    kappa += -(alpha1 * alpha1 - alpha0 * alpha0) / 2.0;
    kappa /= alpha1 - alpha0;
    final ProbabilityDistribution<Double> normal = new NormalDistribution(0, 1);
    double priceExpected = (1.0 + deltaF * forward) * normal.getCDF(-kappa - alpha0) - (1.0 + deltaF * STRIKE) * normal.getCDF(-kappa - alpha1);
    priceExpected *= deltaP / deltaF * ptp;
    priceExpected *= NOTIONAL;
    final MultiCurrencyAmount priceMethod = METHOD_HW.presentValue(CAP_LONG, HW_MULTICURVES);
    assertEquals("Cap/floor: Hull-White pricing", priceExpected, priceMethod.getAmount(EUR).getAmount(), TOLERANCE_PV);
  }

  //TODO: present value in arrears

  public void presentValueLongShort() {
    final MultiCurrencyAmount priceLong = METHOD_HW.presentValue(CAP_LONG, HW_MULTICURVES);
    final MultiCurrencyAmount priceShort = METHOD_HW.presentValue(CAP_SHORT, HW_MULTICURVES);
    assertEquals("Cap/floor: Hull-White pricing", priceLong.getAmount(EUR).getAmount(), -priceShort.getAmount(EUR).getAmount(), TOLERANCE_PV);
  }

  /**
   * Tests present value curve sensitivity when the valuation date is on trade date.
   */
  public void presentValueCurveSensitivity() {
    final MultipleCurrencyParameterSensitivity pvpsExact = PS_HW_C.calculateSensitivity(CAP_LONG, HW_MULTICURVES, HW_MULTICURVES.getMulticurveProvider().getAllNames());
    final MultipleCurrencyParameterSensitivity pvpsFD = PS_HW_FDC.calculateSensitivity(CAP_LONG, HW_MULTICURVES);
    AssertSensitivityObjects.assertEquals("SwaptionPhysicalFixedIborSABRMethod: presentValueCurveSensitivity ", pvpsExact, pvpsFD, TOLERANCE_PV_DELTA);
  }

  /**
   * Tests the Hull-White parameters sensitivity.
   */
  public void presentValueHullWhiteSensitivity() {
    presentValueHullWhiteSensitivityInstrument(CAP_LONG);
    presentValueHullWhiteSensitivityInstrument(CAP_SHORT);
    presentValueHullWhiteSensitivityInstrument(PUT_LONG);
  }

  private void presentValueHullWhiteSensitivityInstrument(final CapFloorIbor instrument) {
    final double[] hwSensitivity = METHOD_HW.presentValueHullWhiteSensitivity(instrument, HW_MULTICURVES);
    final int nbVolatility = HW_PARAMETERS.getVolatility().length;
    final double shiftVol = 1.0E-6;
    final double[] volatilityBumped = new double[nbVolatility];
    System.arraycopy(HW_PARAMETERS.getVolatility(), 0, volatilityBumped, 0, nbVolatility);
    final double[] volatilityTime = new double[nbVolatility - 1];
    System.arraycopy(HW_PARAMETERS.getVolatilityTime(), 1, volatilityTime, 0, nbVolatility - 1);
    final double[] pvBumpedPlus = new double[nbVolatility];
    final double[] pvBumpedMinus = new double[nbVolatility];
    final HullWhiteOneFactorPiecewiseConstantParameters parametersBumped = new HullWhiteOneFactorPiecewiseConstantParameters(HW_PARAMETERS.getMeanReversion(), volatilityBumped, volatilityTime);
    final HullWhiteOneFactorProviderDiscount bundleBumped = new HullWhiteOneFactorProviderDiscount(MULTICURVES, parametersBumped, EUR);
    final double[] hwSensitivityExpected = new double[hwSensitivity.length];
    for (int loopvol = 0; loopvol < nbVolatility; loopvol++) {
      volatilityBumped[loopvol] += shiftVol;
      parametersBumped.setVolatility(volatilityBumped);
      pvBumpedPlus[loopvol] = METHOD_HW.presentValue(instrument, bundleBumped).getAmount(EUR).getAmount();
      volatilityBumped[loopvol] -= 2 * shiftVol;
      parametersBumped.setVolatility(volatilityBumped);
      pvBumpedMinus[loopvol] = METHOD_HW.presentValue(instrument, bundleBumped).getAmount(EUR).getAmount();
      hwSensitivityExpected[loopvol] = (pvBumpedPlus[loopvol] - pvBumpedMinus[loopvol]) / (2 * shiftVol);
      assertEquals("Cap/floor Ibor - Hull-White sensitivity adjoint: derivative " + loopvol + " - difference:" + (hwSensitivityExpected[loopvol] - hwSensitivity[loopvol]),
          hwSensitivityExpected[loopvol], hwSensitivity[loopvol], 1.0E-0);
      volatilityBumped[loopvol] = HW_PARAMETERS.getVolatility()[loopvol];
    }
  }

  /**
   * Compare explicit formula with Monte-Carlo and long/short and payer/receiver parities.
   */
  public void monteCarlo() {
    HullWhiteMonteCarloMethod methodMC;
    methodMC = new HullWhiteMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), 10 * NB_PATH);
    // Seed fixed to the DEFAULT_SEED for testing purposes.
    final MultiCurrencyAmount pvExplicit = METHOD_HW.presentValue(CAP_LONG, HW_MULTICURVES);
    final MultiCurrencyAmount pvMC = methodMC.presentValue(CAP_LONG, EUR, HW_MULTICURVES);
    assertEquals("Cap/floor - Hull-White - Monte Carlo", pvExplicit.getAmount(EUR).getAmount(), pvMC.getAmount(EUR).getAmount(), 5.0E+2);
    final double pvMCPreviousRun = 136707.032;
    assertEquals("Swaption physical - Hull-White - Monte Carlo", pvMCPreviousRun, pvMC.getAmount(EUR).getAmount(), TOLERANCE_PV);
    methodMC = new HullWhiteMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), 10 * NB_PATH);
    final MultiCurrencyAmount pvShortMC = methodMC.presentValue(CAP_SHORT, EUR, HW_MULTICURVES);
    assertEquals("Swaption physical - Hull-White - Monte Carlo", -pvMC.getAmount(EUR).getAmount(), pvShortMC.getAmount(EUR).getAmount(), TOLERANCE_PV);
  }

}

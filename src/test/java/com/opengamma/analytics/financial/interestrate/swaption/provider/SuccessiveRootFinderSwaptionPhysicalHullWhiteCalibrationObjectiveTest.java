/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.provider;

import static org.testng.AssertJUnit.assertEquals;

import java.time.Period;
import java.time.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.financial.provider.calculator.sabrswaption.PresentValueSABRSwaptionCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.SABRDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSwaptionProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSwaptionProviderInterface;
import com.opengamma.analytics.financial.provider.method.SuccessiveRootFinderHullWhiteCalibrationEngine;
import com.opengamma.analytics.financial.provider.method.SuccessiveRootFinderHullWhiteCalibrationObjective;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.DateUtils;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.currency.MultiCurrencyAmount;
import com.opengamma.strata.basics.date.BusinessDayConvention;
import com.opengamma.strata.basics.date.BusinessDayConventions;
import com.opengamma.strata.basics.date.HolidayCalendar;

/**
 * Tests related to the calibration engine for Hull-White one factor calibration to European swaptions.
 */
@Test
public class SuccessiveRootFinderSwaptionPhysicalHullWhiteCalibrationObjectiveTest {

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final IborIndex EURIBOR3M = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd()[0];
  private static final Currency EUR = EURIBOR3M.getCurrency();
  private static final HolidayCalendar CALENDAR = MulticurveProviderDiscountDataSets.getEURCalendar();

  private static final SABRInterestRateParameters SABR_PARAMETER = SABRDataSets.createSABR1();
  private static final GeneratorSwapFixedIbor EUR1YEURIBOR3M = GeneratorSwapFixedIborMaster.getInstance().getGenerator("EUR1YEURIBOR3M", CALENDAR);
  private static final SABRSwaptionProviderDiscount SABR_MULTICURVES = new SABRSwaptionProviderDiscount(MULTICURVES, SABR_PARAMETER, EUR1YEURIBOR3M);

  // Swaption description
  private static final boolean IS_LONG = true;
  // Swap 5Y description
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final double NOTIONAL = 100000000; //100m
  //  Fixed leg: Semi-annual bond
  private static final double RATE = 0.0225;
  private static final boolean FIXED_IS_PAYER = true;
  //  Ibor leg: quarterly money
  private static final int SWAP_TENOR_YEAR = 9;
  private static final IndexSwap SWAP_INDEX = new IndexSwap(EUR1YEURIBOR3M, Period.ofYears(SWAP_TENOR_YEAR));
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 8, 18);
  private static final int[] EXPIRY_TENOR = new int[] {1, 2, 3, 4, 5 };
  private static final ZonedDateTime[] EXPIRY_DATE = new ZonedDateTime[EXPIRY_TENOR.length];
  private static final ZonedDateTime[] SETTLEMENT_DATE = new ZonedDateTime[EXPIRY_TENOR.length];
  private static final SwapFixedIborDefinition[] SWAP_PAYER_DEFINITION = new SwapFixedIborDefinition[EXPIRY_TENOR.length];
  private static final SwaptionPhysicalFixedIborDefinition[] SWAPTION_LONG_PAYER_DEFINITION = new SwaptionPhysicalFixedIborDefinition[EXPIRY_TENOR.length];
  static {
    for (int loopexp = 0; loopexp < EXPIRY_TENOR.length; loopexp++) {
      EXPIRY_DATE[loopexp] = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofYears(EXPIRY_TENOR[loopexp]), BUSINESS_DAY, CALENDAR);
      SETTLEMENT_DATE[loopexp] = ScheduleCalculator.getAdjustedDate(EXPIRY_DATE[loopexp], EURIBOR3M.getSpotLag(), CALENDAR);
      SWAP_PAYER_DEFINITION[loopexp] = SwapFixedIborDefinition.from(SETTLEMENT_DATE[loopexp], SWAP_INDEX, NOTIONAL, RATE, FIXED_IS_PAYER, CALENDAR);
      SWAPTION_LONG_PAYER_DEFINITION[loopexp] = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE[loopexp], SWAP_PAYER_DEFINITION[loopexp], FIXED_IS_PAYER, IS_LONG);
    }
  }
  // to derivatives
  private static final SwaptionPhysicalFixedIbor[] SWAPTION_LONG_PAYER = new SwaptionPhysicalFixedIbor[EXPIRY_TENOR.length];
  static {
    for (int loopexp = 0; loopexp < EXPIRY_TENOR.length; loopexp++) {
      SWAPTION_LONG_PAYER[loopexp] = SWAPTION_LONG_PAYER_DEFINITION[loopexp].toDerivative(REFERENCE_DATE);
    }
  }
  private static final PresentValueSABRSwaptionCalculator PVSSC = PresentValueSABRSwaptionCalculator.getInstance();
  private static final SwaptionPhysicalFixedIborHullWhiteMethod METHOD_HW = SwaptionPhysicalFixedIborHullWhiteMethod.getInstance();

  private static final double TOLERANCE_PV = 1.0E-2;

  @Test
  /**
   * Tests the correctness of Hull-White one factor calibration to swaptions with SABR price.
   */
  public void calibration() {
    final double meanReversion = 0.01;
    final HullWhiteOneFactorPiecewiseConstantParameters hwParameters = new HullWhiteOneFactorPiecewiseConstantParameters(meanReversion, new double[] {0.01 }, new double[0]);
    final SuccessiveRootFinderHullWhiteCalibrationObjective objective = new SuccessiveRootFinderHullWhiteCalibrationObjective(hwParameters, EUR);
    final SuccessiveRootFinderHullWhiteCalibrationEngine<SABRSwaptionProviderInterface> calibrationEngine = new SuccessiveRootFinderHullWhiteCalibrationEngine<>(objective);
    for (int loopexp = 0; loopexp < EXPIRY_TENOR.length; loopexp++) {
      calibrationEngine.addInstrument(SWAPTION_LONG_PAYER[loopexp], PVSSC);
    }
    calibrationEngine.calibrate(SABR_MULTICURVES);
    final MultiCurrencyAmount[] pvSabr = new MultiCurrencyAmount[EXPIRY_TENOR.length];
    final MultiCurrencyAmount[] pvHw = new MultiCurrencyAmount[EXPIRY_TENOR.length];
    for (int loopexp = 0; loopexp < EXPIRY_TENOR.length; loopexp++) {
      pvSabr[loopexp] = SWAPTION_LONG_PAYER[loopexp].accept(PVSSC, SABR_MULTICURVES);
      pvHw[loopexp] = METHOD_HW.presentValue(SWAPTION_LONG_PAYER[loopexp], objective.getHwProvider());
      assertEquals("Hull-White calibration: swaption " + loopexp, pvSabr[loopexp].getAmount(EUR).getAmount(), pvHw[loopexp].getAmount(EUR).getAmount(), TOLERANCE_PV);
    }
  }

}

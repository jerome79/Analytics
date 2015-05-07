/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.inflation.provider;

import static org.testng.AssertJUnit.assertEquals;

import java.time.Period;
import java.time.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.inflation.CapFloorInflationZeroCouponMonthlyDefinition;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CapFloorInflationZeroCouponMonthly;
import com.opengamma.analytics.financial.model.interestrate.definition.InflationZeroCouponCapFloorParameters;
import com.opengamma.analytics.financial.model.option.parameters.BlackSmileCapInflationZeroCouponParameters;
import com.opengamma.analytics.financial.provider.description.BlackDataSets;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.inflation.BlackSmileCapInflationZeroCouponProvider;
import com.opengamma.analytics.financial.provider.description.inflation.BlackSmileCapInflationZeroCouponProviderDiscount;
import com.opengamma.analytics.financial.provider.description.inflation.InflationIssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderInterface;
import com.opengamma.analytics.financial.provider.method.SuccessiveRootFinderInflationZeroCouponCapFloorCalibrationEngine;
import com.opengamma.analytics.financial.provider.method.SuccessiveRootFinderInflationZeroCouponCapFloorCalibrationObjective;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.interpolation.Interpolator2D;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.analytics.util.time.DateUtils;
import com.opengamma.analytics.util.timeseries.DoubleTimeSeries;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.currency.MultiCurrencyAmount;
import com.opengamma.strata.basics.date.BusinessDayConvention;
import com.opengamma.strata.basics.date.BusinessDayConventions;
import com.opengamma.strata.basics.date.HolidayCalendar;

/**
 * Tests related to the calibration engine for inflation zero coupon cap/floor calibration.
 */
@Test
public class CapFloorZeroCouponCalibrationObjectiveTest {

  //Cap/floor description details
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final Currency CUR = Currency.EUR;
  private static final ZonedDateTime START_DATE = DateUtils.getUTCDate(2011, 9, 7);
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2011, 9, 9);
  private static final double NOTIONAL = 10000; //100m
  private static final double[] STRIKES = {-.01, .00, .01, .02, .03, .04 };
  private static final boolean IS_CAP = true;
  private static final InflationIssuerProviderDiscount MARKET = MulticurveProviderDiscountDataSets.createMarket1();
  private static final IndexPrice[] PRICE_INDEXES = MARKET.getPriceIndexes().toArray(new IndexPrice[MARKET.getPriceIndexes().size()]);
  private static final IndexPrice PRICE_INDEX_EUR = PRICE_INDEXES[0];
  private static final HolidayCalendar CALENDAR_EUR = MulticurveProviderDiscountDataSets.getEURCalendar();
  private static final int MONTH_LAG = 3;
  private static final ZonedDateTime LAST_KNOWN_FIXING_DATE = DateUtils.getUTCDate(2008, 7, 01);
  private static final InterpolatedDoublesSurface BLACK_SURF = BlackDataSets.createBlackSurfaceExpiryStrike();
  private static final BlackSmileCapInflationZeroCouponParameters BLACK_PARAM = new BlackSmileCapInflationZeroCouponParameters(BLACK_SURF, PRICE_INDEX_EUR);
  private static final BlackSmileCapInflationZeroCouponProviderDiscount BLACK_INFLATION = new BlackSmileCapInflationZeroCouponProviderDiscount(MARKET.getInflationProvider(), BLACK_PARAM);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 9, 7);

  private static final CapFloorInflationZeroCouponMonthlyBlackSmileMethod METHOD = CapFloorInflationZeroCouponMonthlyBlackSmileMethod.getInstance();
  double[][] marketPrices = new double[6][30];

  // volatility matrix first guess details
  private static double[] expiryTimes1 = new double[30];
  private static final double[] strikes = {-.01, .00, .01, .02, .03, .04 };
  private static final double[][] volatilities = { {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 },
    {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 },
    {.01, .01, .01, .01, .01, .01 },
    {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 },
    {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 },
    {.01, .01, .01, .01, .01, .01 },
    {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 },
    {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 },
    {.01, .01, .01, .01, .01, .01 } };
  private static final int[] availabelTenor = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 15, 20, 25, 30 };
  CapFloorInflationZeroCouponMonthlyDefinition[][] CAP_DEFINITIONS = new CapFloorInflationZeroCouponMonthlyDefinition[6][availabelTenor.length];
  CapFloorInflationZeroCouponMonthly[][] CAPS = new CapFloorInflationZeroCouponMonthly[6][availabelTenor.length];

  private static final DoubleTimeSeries<ZonedDateTime> cpiTimeSerie = MulticurveProviderDiscountDataSets.usCpiFrom2009();

  /**
   * Tests the correctness of INFLATION YEAR ON YEAR CAP/FLOOR calibration to market prices.
   */
  public void calibration() {
    // creation of the basket of the calibration instruments.
    for (int loop1 = 0; loop1 < STRIKES.length; loop1++) {
      for (int loop2 = 0; loop2 < availabelTenor.length; loop2++) {
        final Period tenor = Period.ofYears(availabelTenor[loop2]);
        final ZonedDateTime payementDate = ScheduleCalculator.getAdjustedDate(START_DATE, tenor, BUSINESS_DAY, CALENDAR_EUR);
        CAP_DEFINITIONS[loop1][loop2] = CapFloorInflationZeroCouponMonthlyDefinition.from(SETTLEMENT_DATE, payementDate, NOTIONAL, PRICE_INDEX_EUR, MONTH_LAG, MONTH_LAG,
            availabelTenor[loop2], LAST_KNOWN_FIXING_DATE, STRIKES[loop1], IS_CAP);
        CAPS[loop1][loop2] = (CapFloorInflationZeroCouponMonthly) CAP_DEFINITIONS[loop1][loop2].toDerivative(REFERENCE_DATE, cpiTimeSerie);
      }
    }

    // Creation of the expiry vector used for the interpolation in the volatility matrix
    // expiry times = reference end time. (for inflation option)
    for (int loopexp = 0; loopexp < CAPS[0].length; loopexp++) {
      expiryTimes1[loopexp] = CAPS[0][loopexp].getReferenceEndTime();
    }

    // parameters bundle that we want to calibrate
    final InflationZeroCouponCapFloorParameters parameters = new InflationZeroCouponCapFloorParameters(expiryTimes1, strikes, volatilities, PRICE_INDEX_EUR);
    // Objective function that we use in the calibration
    final SuccessiveRootFinderInflationZeroCouponCapFloorCalibrationObjective objective = new SuccessiveRootFinderInflationZeroCouponCapFloorCalibrationObjective(parameters, CUR);
    // Calibration engine
    final SuccessiveRootFinderInflationZeroCouponCapFloorCalibrationEngine<InflationProviderInterface> calibrationEngine = new SuccessiveRootFinderInflationZeroCouponCapFloorCalibrationEngine<>(
        objective);

    // Creation of the market prices we will use in the calibration.
    //For this example we calculate the market prices using a matrix of volatility, but normally market prices should be linked to bloomberg tickers (for example or another data provider)
    for (int loop1 = 0; loop1 < STRIKES.length; loop1++) {
      for (int loop2 = 0; loop2 < availabelTenor.length; loop2++) {
        marketPrices[loop1][loop2] = METHOD.presentValue(CAPS[loop1][loop2], BLACK_INFLATION).getAmount(CUR).getAmount();
      }
    }

    // we add each instruments to the calibration engine
    // here we are calibration all strikes and maturities (it is possible to calibrate on only few instruments but there is no reason to do so)
    for (int loop1 = 0; loop1 < STRIKES.length; loop1++) {
      for (int loop2 = 0; loop2 < availabelTenor.length; loop2++) {
        calibrationEngine.addInstrument(CAPS[loop1][loop2], marketPrices[loop1][loop2]);
      }
    }

    // We do the calibration
    calibrationEngine.calibrate(MARKET.getInflationProvider());

    // We tests if we
    final MultiCurrencyAmount[][] pvCapYearOnYear = new MultiCurrencyAmount[STRIKES.length][CAPS[0].length];
    for (int loop1 = 0; loop1 < STRIKES.length; loop1++) {
      for (int loop2 = 0; loop2 < availabelTenor.length; loop2++) {
        final Interpolator2D interpolator = objective.getInflationCapZeroCouponProvider().getBlackParameters().getVolatilitySurface().getInterpolator();
        final BlackSmileCapInflationZeroCouponParameters CalibratedBlackSmileCapInflationZeroCouponParameters = new BlackSmileCapInflationZeroCouponParameters(
            objective.getInflationCapZeroCouponParameters(), interpolator);
        final BlackSmileCapInflationZeroCouponProvider CalibratedBlackSmileCapInflationYearOnYearProvider = new BlackSmileCapInflationZeroCouponProvider(objective.getInflationCapZeroCouponProvider()
            .getInflationProvider(),
            CalibratedBlackSmileCapInflationZeroCouponParameters);
        pvCapYearOnYear[loop1][loop2] = METHOD.presentValue(CAPS[loop1][loop2], CalibratedBlackSmileCapInflationYearOnYearProvider);
        assertEquals("Inflation year on year calibration: cap/floor " + loop1, pvCapYearOnYear[loop1][loop2].getAmount(CUR).getAmount(), marketPrices[loop1][loop2], 1E-2);
      }
    }
  }
}

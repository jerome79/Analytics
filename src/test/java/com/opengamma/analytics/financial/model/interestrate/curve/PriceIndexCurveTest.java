/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.curve;

import static org.testng.AssertJUnit.assertEquals;

import java.time.Period;
import java.time.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.convention.daycount.DayCount;
import com.opengamma.analytics.convention.daycount.DayCountUtils;
import com.opengamma.analytics.convention.daycount.DayCounts;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.analytics.util.time.DateUtils;
import com.opengamma.strata.basics.date.BusinessDayConvention;
import com.opengamma.strata.basics.date.BusinessDayConventions;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.basics.date.HolidayCalendars;


/**
 * Test.
 */
@Test
public class PriceIndexCurveTest {

  private static double[] INDEX_VALUE = new double[] {108.23, 108.64, 111.0, 115.0};
  private static double[] TIME_VALUE = new double[] {-3.0 / 12.0, -2.0 / 12.0, 9.0 / 12.0, 2.0 + 9.0 / 12.0};
  private static final InterpolatedDoublesCurve CURVE = InterpolatedDoublesCurve.from(TIME_VALUE, INDEX_VALUE, new LinearInterpolator1D());
  private static final PriceIndexCurveSimple PRICE_INDEX_CURVE = new PriceIndexCurveSimple(CURVE);

  private static final HolidayCalendar CALENDAR = HolidayCalendars.SAT_SUN;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final DayCount ACT_ACT = DayCounts.ACT_ACT_ISDA;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurve() {
    new PriceIndexCurveSimple(null);
  }

  @Test
  /**
   * Tests the getter.
   */
  public void getter() {
    assertEquals(CURVE, PRICE_INDEX_CURVE.getCurve());
  }

  @Test
  /**
   * Tests price index.
   */
  public void priceIndex() {
    assertEquals(INDEX_VALUE[0], PRICE_INDEX_CURVE.getPriceIndex(TIME_VALUE[0]), 1.0E-10);
    assertEquals(INDEX_VALUE[2], PRICE_INDEX_CURVE.getPriceIndex(TIME_VALUE[2]), 1.0E-10);
    assertEquals((INDEX_VALUE[2] + INDEX_VALUE[3]) / 2.0, PRICE_INDEX_CURVE.getPriceIndex((TIME_VALUE[2] + TIME_VALUE[3]) / 2.0), 1.0E-10);
  }

  @Test
  /**
   * Tests price index builder from zero-coupon swap rates with start of the month convention.
   */
  public void fromStartOfMonth() {
    ZonedDateTime constructionDate = DateUtils.getUTCDate(2011, 8, 18);
    ZonedDateTime[] indexKnownDate = new ZonedDateTime[] {DateUtils.getUTCDate(2011, 5, 1), DateUtils.getUTCDate(2011, 6, 1)};
    double[] nodeTimeKnown = new double[indexKnownDate.length];
    for (int loopmonth = 0; loopmonth < indexKnownDate.length; loopmonth++) {
      nodeTimeKnown[loopmonth] = -DayCountUtils.yearFraction(ACT_ACT, indexKnownDate[loopmonth], constructionDate);
    }
    int[] swapTenor = new int[] {1, 2, 3, 4, 5, 7, 10, 15, 20, 30};
    double[] swapRate = new double[] {0.02, 0.021, 0.02, 0.025, 0.025, 0.025, 0.025, 0.025, 0.025, 0.025};
    double[] indexKnown = new double[] {113.11, 113.10}; // May / June 2011.
    int monthLag = 3;
    double[] nodeTimeOther = new double[swapTenor.length];
    ZonedDateTime[] referenceDate = new ZonedDateTime[swapTenor.length];
    for (int loopswap = 0; loopswap < swapTenor.length; loopswap++) {
      ZonedDateTime paymentDate = ScheduleCalculator.getAdjustedDate(constructionDate, Period.ofYears(swapTenor[loopswap]), BUSINESS_DAY, CALENDAR);
      referenceDate[loopswap] = paymentDate.minusMonths(monthLag).withDayOfMonth(1);
      nodeTimeOther[loopswap] = DayCountUtils.yearFraction(ACT_ACT, constructionDate, referenceDate[loopswap]);
    }
    PriceIndexCurveSimple priceIndexCurve = PriceIndexCurveSimple.fromStartOfMonth(nodeTimeKnown, indexKnown, nodeTimeOther, swapRate);
    for (int loopswap = 0; loopswap < swapTenor.length; loopswap++) {
      assertEquals("Simple price curve", indexKnown[0] * Math.pow(1 + swapRate[loopswap], swapTenor[loopswap]), priceIndexCurve.getPriceIndex(nodeTimeOther[loopswap]));
    }
  }
}

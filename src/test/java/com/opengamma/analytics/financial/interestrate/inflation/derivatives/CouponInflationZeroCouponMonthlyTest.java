/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.inflation.derivatives;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import java.time.Period;
import java.time.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.convention.daycount.DayCount;
import com.opengamma.analytics.convention.daycount.DayCountUtils;
import com.opengamma.analytics.convention.daycount.DayCounts;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponMonthly;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.DateUtils;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.date.BusinessDayConvention;
import com.opengamma.strata.basics.date.BusinessDayConventions;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.basics.date.HolidayCalendars;

/**
 * Tests the zero-coupon inflation constructors.
 */
@Test
public class CouponInflationZeroCouponMonthlyTest {
  private static final String NAME = "Euro HICP x";
  private static final Currency CUR = Currency.EUR;
  private static final IndexPrice PRICE_INDEX = new IndexPrice(NAME, CUR);
  private static final HolidayCalendar CALENDAR = HolidayCalendars.SAT_SUN;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final ZonedDateTime START_DATE = DateUtils.getUTCDate(2008, 8, 18);
  private static final Period COUPON_TENOR = Period.ofYears(10);
  private static final ZonedDateTime PAYMENT_DATE = ScheduleCalculator.getAdjustedDate(START_DATE, COUPON_TENOR, BUSINESS_DAY, CALENDAR);
  private static final double NOTIONAL = 98765432;
  private static final int MONTH_LAG = 3;
  private static final double INDEX_APRIL_2008 = 108.23; // 3 m before Aug: May / 1 May index = May index: 108.23
  private static final ZonedDateTime REFERENCE_END_DATE = PAYMENT_DATE.minusMonths(MONTH_LAG).withDayOfMonth(1);
  private static final DayCount ACT_ACT = DayCounts.ACT_ACT_ISDA;
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 7, 29);
  private static final double PAYMENT_TIME = DayCountUtils.yearFraction(ACT_ACT, REFERENCE_DATE, PAYMENT_DATE);
  private static final double NATURAL_PAYMENT_TIME = DayCountUtils.yearFraction(ACT_ACT, REFERENCE_DATE, PAYMENT_DATE);
  private static final double REFERENCE_END_TIME = DayCountUtils.yearFraction(ACT_ACT, REFERENCE_DATE, REFERENCE_END_DATE);
  private static final CouponInflationZeroCouponMonthly ZERO_COUPON = new CouponInflationZeroCouponMonthly(CUR, PAYMENT_TIME, 1.0, NOTIONAL, PRICE_INDEX, INDEX_APRIL_2008, REFERENCE_END_TIME,
      NATURAL_PAYMENT_TIME, false);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIndex() {
    new CouponInflationZeroCouponMonthly(CUR, PAYMENT_TIME, 1.0, NOTIONAL, null, INDEX_APRIL_2008, REFERENCE_END_TIME, NATURAL_PAYMENT_TIME, false);
  }

  @Test
  /**
   * Tests the class getter.
   */
  public void getter() {
    assertEquals("Inflation Zero-coupon: getter", PRICE_INDEX, ZERO_COUPON.getPriceIndex());
    assertEquals("Inflation Zero-coupon: getter", INDEX_APRIL_2008, ZERO_COUPON.getIndexStartValue());
    assertEquals("Inflation Zero-coupon: getter", REFERENCE_END_TIME, ZERO_COUPON.getReferenceEndTime());
    assertEquals("Inflation Zero-coupon: getter", NATURAL_PAYMENT_TIME, ZERO_COUPON.getNaturalPaymentTime());
  }

  @Test
  /**
   * Tests the equal and hash-code methods.
   */
  public void equalHash() {
    assertEquals(ZERO_COUPON, ZERO_COUPON);
    CouponInflationZeroCouponMonthly couponDuplicate = new CouponInflationZeroCouponMonthly(CUR, PAYMENT_TIME, 1.0, NOTIONAL, PRICE_INDEX, INDEX_APRIL_2008, REFERENCE_END_TIME,
        NATURAL_PAYMENT_TIME, false);
    assertEquals(ZERO_COUPON, couponDuplicate);
    assertEquals(ZERO_COUPON.hashCode(), couponDuplicate.hashCode());
    CouponInflationZeroCouponMonthly modified;
    modified = new CouponInflationZeroCouponMonthly(CUR, PAYMENT_TIME, 1.0, NOTIONAL, PRICE_INDEX, INDEX_APRIL_2008 + 0.1, REFERENCE_END_TIME, NATURAL_PAYMENT_TIME, false);
    assertFalse(ZERO_COUPON.equals(modified));
    modified = new CouponInflationZeroCouponMonthly(CUR, PAYMENT_TIME, 1.0, NOTIONAL, PRICE_INDEX, INDEX_APRIL_2008, REFERENCE_END_TIME + 0.1, NATURAL_PAYMENT_TIME, false);
    assertFalse(ZERO_COUPON.equals(modified));
    double modifiedNaturalPaymentTime = NATURAL_PAYMENT_TIME + .01;
    modified = new CouponInflationZeroCouponMonthly(CUR, PAYMENT_TIME, 1.0, NOTIONAL, PRICE_INDEX, INDEX_APRIL_2008, REFERENCE_END_TIME, modifiedNaturalPaymentTime, false);
    assertFalse(ZERO_COUPON.equals(modified));
  }

}

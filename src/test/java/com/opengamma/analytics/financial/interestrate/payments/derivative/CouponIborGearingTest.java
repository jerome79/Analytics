/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.derivative;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import java.time.Period;
import java.time.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.convention.daycount.DayCount;
import com.opengamma.analytics.convention.daycount.DayCountUtils;
import com.opengamma.analytics.convention.daycount.DayCounts;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.DateUtils;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.date.BusinessDayConvention;
import com.opengamma.strata.basics.date.BusinessDayConventions;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.basics.date.HolidayCalendars;

/**
 * Tests related to the construction of Ibor coupon with gearing factor and spread.
 */
@Test
public class CouponIborGearingTest {
  // The index: Libor 3m
  private static final Period TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final HolidayCalendar CALENDAR = HolidayCalendars.SAT_SUN;
  private static final DayCount DAY_COUNT_INDEX = DayCounts.ACT_360;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final Currency CUR = Currency.EUR;
  private static final IborIndex INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM, "Ibor");
  // Coupon
  private static final DayCount DAY_COUNT_COUPON = DayCounts.ACT_365F;
  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtils.getUTCDate(2011, 5, 23);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtils.getUTCDate(2011, 8, 22);
  private static final double ACCRUAL_FACTOR = DayCountUtils.yearFraction(DAY_COUNT_COUPON, ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double NOTIONAL = 1000000; //1m
  private static final double FACTOR = 2.0;
  private static final double SPREAD = 0.0050;
  private static final ZonedDateTime FIXING_DATE = ScheduleCalculator.getAdjustedDate(ACCRUAL_START_DATE, -SETTLEMENT_DAYS, CALENDAR);
  private static final ZonedDateTime FIXING_START_DATE = DateUtils.getUTCDate(2011, 5, 23);
  private static final ZonedDateTime FIXING_END_DATE = ScheduleCalculator.getAdjustedDate(FIXING_START_DATE, TENOR, BUSINESS_DAY, CALENDAR);
  // Time
  private static final DayCount ACT_ACT = DayCounts.ACT_ACT_ISDA;
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 12, 27);
  private static final double ACCRUAL_END_TIME = DayCountUtils.yearFraction(ACT_ACT, REFERENCE_DATE, ACCRUAL_END_DATE);
  private static final double FIXING_TIME = DayCountUtils.yearFraction(ACT_ACT, REFERENCE_DATE, FIXING_DATE);
  private static final double FIXING_START_TIME = DayCountUtils.yearFraction(ACT_ACT, REFERENCE_DATE, FIXING_START_DATE);
  private static final double FIXING_END_TIME = DayCountUtils.yearFraction(ACT_ACT, REFERENCE_DATE, FIXING_END_DATE);
  private static final double FIXING_ACCRUAL_FACTOR = DayCountUtils.yearFraction(DAY_COUNT_INDEX, FIXING_START_DATE, FIXING_END_DATE);
  private static final CouponIborGearing COUPON = new CouponIborGearing(CUR, ACCRUAL_END_TIME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME,
      FIXING_END_TIME, FIXING_ACCRUAL_FACTOR, SPREAD, FACTOR);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency() {
    new CouponIborGearing(null, ACCRUAL_END_TIME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL_FACTOR, SPREAD, FACTOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIndex() {
    new CouponIborGearing(CUR, ACCRUAL_END_TIME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, null, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL_FACTOR, SPREAD, FACTOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongCurrency() {
    final Currency otherCurrency = Currency.USD;
    new CouponIborGearing(otherCurrency, ACCRUAL_END_TIME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL_FACTOR, SPREAD,
        FACTOR);
  }

  @Test
  /**
   * Tests the getters.
   */
  public void getter() {
    assertEquals(CUR, COUPON.getCurrency());
    assertEquals(ACCRUAL_END_TIME, COUPON.getPaymentTime());
    assertEquals(FIXING_TIME, COUPON.getFixingTime());
    assertEquals(FIXING_START_TIME, COUPON.getFixingPeriodStartTime());
    assertEquals(FIXING_END_TIME, COUPON.getFixingPeriodEndTime());
    assertEquals(INDEX, COUPON.getIndex());
    assertEquals(SPREAD, COUPON.getSpread());
    assertEquals(SPREAD * ACCRUAL_FACTOR * NOTIONAL, COUPON.getSpreadAmount());
    assertEquals(FACTOR, COUPON.getFactor());
  }

  @Test
  public void testWithNotional() {
    final double notional = NOTIONAL + 100;
    final CouponIborGearing expected = new CouponIborGearing(CUR, ACCRUAL_END_TIME, ACCRUAL_FACTOR, notional, FIXING_TIME, INDEX, FIXING_START_TIME,
        FIXING_END_TIME, FIXING_ACCRUAL_FACTOR, SPREAD, FACTOR);
    assertEquals(expected, COUPON.withNotional(notional));
  }

  @Test
  /**
   * Tests the equal and hash code.
   */
  public void testEqualHash() {
    final CouponIborGearing newCoupon = new CouponIborGearing(CUR, ACCRUAL_END_TIME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_ACCRUAL_FACTOR, SPREAD, FACTOR);
    assertEquals(newCoupon, COUPON);
    assertEquals(newCoupon.hashCode(), COUPON.hashCode());
    CouponIborGearing other;
    other = new CouponIborGearing(CUR, ACCRUAL_END_TIME + 0.1, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL_FACTOR, SPREAD,
        FACTOR);
    assertFalse(COUPON.equals(other));
    other = new CouponIborGearing(CUR, ACCRUAL_END_TIME, ACCRUAL_FACTOR + 0.1, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL_FACTOR, SPREAD,
        FACTOR);
    assertFalse(COUPON.equals(other));
    other = new CouponIborGearing(CUR, ACCRUAL_END_TIME, ACCRUAL_FACTOR, NOTIONAL + 0.1, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL_FACTOR, SPREAD,
        FACTOR);
    assertFalse(COUPON.equals(other));
    other = new CouponIborGearing(CUR, ACCRUAL_END_TIME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME + 0.1, INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL_FACTOR, SPREAD,
        FACTOR);
    assertFalse(COUPON.equals(other));
    other = new CouponIborGearing(CUR, ACCRUAL_END_TIME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME + 0.1, FIXING_END_TIME, FIXING_ACCRUAL_FACTOR, SPREAD,
        FACTOR);
    assertFalse(COUPON.equals(other));
    other = new CouponIborGearing(CUR, ACCRUAL_END_TIME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME + 0.1, FIXING_ACCRUAL_FACTOR, SPREAD,
        FACTOR);
    assertFalse(COUPON.equals(other));
    other = new CouponIborGearing(CUR, ACCRUAL_END_TIME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL_FACTOR + 0.1, SPREAD,
        FACTOR);
    assertFalse(COUPON.equals(other));
    other = new CouponIborGearing(CUR, ACCRUAL_END_TIME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL_FACTOR, SPREAD + 0.1,
        FACTOR);
    assertFalse(COUPON.equals(other));
    other = new CouponIborGearing(CUR, ACCRUAL_END_TIME, ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL_FACTOR, SPREAD,
        FACTOR + 0.1);
    assertFalse(COUPON.equals(other));
  }

}

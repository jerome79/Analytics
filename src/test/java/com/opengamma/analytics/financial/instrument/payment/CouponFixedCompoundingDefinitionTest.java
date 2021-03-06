/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.payment;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.time.Period;
import java.time.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.convention.daycount.DayCountUtils;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.DateUtils;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.basics.date.HolidayCalendars;

/**
 *  Test class for the coupon fixed compounding.
 */
@Test
public class CouponFixedCompoundingDefinitionTest {

  private static final HolidayCalendar NYC = HolidayCalendars.SAT_SUN;
  private static final IndexIborMaster MASTER_IBOR = IndexIborMaster.getInstance();
  private static final IborIndex USDLIBOR1M = MASTER_IBOR.getIndex("USDLIBOR1M");
  private static final Currency CURRENCY = USDLIBOR1M.getCurrency();

  private static final Period TENOR_3M = Period.ofMonths(3);
  private static final ZonedDateTime START_DATE = DateUtils.getUTCDate(2012, 8, 24);
  private static final double NOTIONAL = 123454321;
  private static final double FIXED_RATE = .02;

  private static final ZonedDateTime[] ACCRUAL_END_DATES = ScheduleCalculator.getAdjustedDateSchedule(START_DATE, TENOR_3M, true, false, USDLIBOR1M, NYC);
  private static final int NB_SUB_PERIOD = ACCRUAL_END_DATES.length;
  private static final ZonedDateTime[] ACCRUAL_START_DATES = new ZonedDateTime[NB_SUB_PERIOD];
  private static final double[] PAYMENT_ACCRUAL_FACTORS = new double[NB_SUB_PERIOD];
  private static final double PAYMENT_ACCRUAL_FACTOR;

  static {
    ACCRUAL_START_DATES[0] = START_DATE;
    for (int loopsub = 1; loopsub < NB_SUB_PERIOD; loopsub++) {
      ACCRUAL_START_DATES[loopsub] = ACCRUAL_END_DATES[loopsub - 1];
    }
    double af = 0.0;
    for (int loopsub = 0; loopsub < NB_SUB_PERIOD; loopsub++) {
      PAYMENT_ACCRUAL_FACTORS[loopsub] = DayCountUtils.yearFraction(USDLIBOR1M.getDayCount(), ACCRUAL_START_DATES[loopsub], ACCRUAL_END_DATES[loopsub]);
      af += PAYMENT_ACCRUAL_FACTORS[loopsub];
    }
    PAYMENT_ACCRUAL_FACTOR = af;
  }
  private static final ZonedDateTime[] FIXING_PERIOD_END_DATES = ScheduleCalculator.getAdjustedDate(ACCRUAL_START_DATES, USDLIBOR1M, NYC);
  private static final double[] FIXING_ACCRUAL_FACTORS = new double[NB_SUB_PERIOD];
  static {
    for (int loopsub = 0; loopsub < NB_SUB_PERIOD; loopsub++) {
      FIXING_ACCRUAL_FACTORS[loopsub] = DayCountUtils.yearFraction(USDLIBOR1M.getDayCount(), ACCRUAL_START_DATES[loopsub], FIXING_PERIOD_END_DATES[loopsub]);
    }
  }
  private static final ZonedDateTime PAYMENT_DATE = ACCRUAL_END_DATES[NB_SUB_PERIOD - 1];

  private static final CouponFixedCompoundingDefinition COUPON = CouponFixedCompoundingDefinition.from(CURRENCY, PAYMENT_DATE, ACCRUAL_START_DATES[0], ACCRUAL_END_DATES[NB_SUB_PERIOD - 1],
      PAYMENT_ACCRUAL_FACTOR,
      NOTIONAL, FIXED_RATE, ACCRUAL_START_DATES, ACCRUAL_END_DATES, PAYMENT_ACCRUAL_FACTORS);

  @Test
  /**
   * Tests the getters.
   */
  public void testGetters() {
    assertEquals(COUPON.getPaymentDate(), PAYMENT_DATE);
    assertEquals(COUPON.getAccrualStartDate(), ACCRUAL_START_DATES[0]);
    assertEquals(COUPON.getAccrualEndDate(), ACCRUAL_END_DATES[NB_SUB_PERIOD - 1]);
    assertEquals(COUPON.getPaymentYearFraction(), PAYMENT_ACCRUAL_FACTOR, 1E-10);
    assertEquals(COUPON.getNotional(), NOTIONAL, 1E-2);
    assertEquals(COUPON.getRate(), FIXED_RATE, 1E-10);
    assertArrayEquals(COUPON.getAccrualStartDates(), ACCRUAL_START_DATES);
    assertArrayEquals(COUPON.getAccrualEndDates(), ACCRUAL_END_DATES);
    assertArrayEquals(COUPON.getPaymentAccrualFactors(), PAYMENT_ACCRUAL_FACTORS, 0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCurrency() {
    CouponFixedCompoundingDefinition.from(null, PAYMENT_DATE, ACCRUAL_START_DATES[0], ACCRUAL_END_DATES[NB_SUB_PERIOD - 1],
        PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXED_RATE, ACCRUAL_START_DATES, ACCRUAL_END_DATES, PAYMENT_ACCRUAL_FACTORS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPaymentDate() {
    CouponFixedCompoundingDefinition.from(CURRENCY, null, ACCRUAL_START_DATES[0], ACCRUAL_END_DATES[NB_SUB_PERIOD - 1],
        PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXED_RATE, ACCRUAL_START_DATES, ACCRUAL_END_DATES, PAYMENT_ACCRUAL_FACTORS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativePaymentAccrualFactor() {
    CouponFixedCompoundingDefinition.from(CURRENCY, PAYMENT_DATE, ACCRUAL_START_DATES[0], ACCRUAL_END_DATES[NB_SUB_PERIOD - 1],
        -PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXED_RATE, ACCRUAL_START_DATES, ACCRUAL_END_DATES, PAYMENT_ACCRUAL_FACTORS);
  }

  @Test
  /**
   * Tests the equal and hash code.
   */
  public void testEqualHash() {
    assertEquals("CouponIbor: equal-hash", COUPON, COUPON);
    final CouponFixedCompoundingDefinition other = CouponFixedCompoundingDefinition.from(CURRENCY, PAYMENT_DATE, ACCRUAL_START_DATES[0], ACCRUAL_END_DATES[NB_SUB_PERIOD - 1],
        PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXED_RATE, ACCRUAL_START_DATES, ACCRUAL_END_DATES, PAYMENT_ACCRUAL_FACTORS);
    assertEquals("CouponFixedCompoundingDefinition: equal-hash", other, COUPON);
    assertEquals("CouponFixedCompoundingDefinition: equal-hash", other.hashCode(), COUPON.hashCode());
    CouponFixedCompoundingDefinition modified;
    modified = CouponFixedCompoundingDefinition.from(CURRENCY, PAYMENT_DATE.plusDays(1), ACCRUAL_START_DATES[0], ACCRUAL_END_DATES[NB_SUB_PERIOD - 1],
        PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXED_RATE, ACCRUAL_START_DATES, ACCRUAL_END_DATES, PAYMENT_ACCRUAL_FACTORS);
    assertFalse("CouponIbor: equal-hash", COUPON.equals(modified));
    modified = CouponFixedCompoundingDefinition.from(CURRENCY, PAYMENT_DATE, ACCRUAL_START_DATES[0].plusDays(1), ACCRUAL_END_DATES[NB_SUB_PERIOD - 1],
        PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXED_RATE, ACCRUAL_START_DATES, ACCRUAL_END_DATES, PAYMENT_ACCRUAL_FACTORS);
    assertFalse("CouponIbor: equal-hash", COUPON.equals(modified));
    modified = CouponFixedCompoundingDefinition.from(CURRENCY, PAYMENT_DATE, ACCRUAL_START_DATES[0], ACCRUAL_END_DATES[NB_SUB_PERIOD - 1].plusDays(1),
        PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXED_RATE, ACCRUAL_START_DATES, ACCRUAL_END_DATES, PAYMENT_ACCRUAL_FACTORS);
    assertFalse("CouponIbor: equal-hash", COUPON.equals(modified));
    modified = CouponFixedCompoundingDefinition.from(CURRENCY, PAYMENT_DATE, ACCRUAL_START_DATES[0], ACCRUAL_END_DATES[NB_SUB_PERIOD - 1],
        PAYMENT_ACCRUAL_FACTOR + 1, NOTIONAL, FIXED_RATE, ACCRUAL_START_DATES, ACCRUAL_END_DATES, PAYMENT_ACCRUAL_FACTORS);
    assertFalse("CouponIbor: equal-hash", COUPON.equals(modified));
    modified = CouponFixedCompoundingDefinition.from(CURRENCY, PAYMENT_DATE, ACCRUAL_START_DATES[0], ACCRUAL_END_DATES[NB_SUB_PERIOD - 1],
        PAYMENT_ACCRUAL_FACTOR, NOTIONAL + 1, FIXED_RATE, ACCRUAL_START_DATES, ACCRUAL_END_DATES, PAYMENT_ACCRUAL_FACTORS);
    assertFalse("CouponIbor: equal-hash", COUPON.equals(modified));
    modified = CouponFixedCompoundingDefinition.from(CURRENCY, PAYMENT_DATE, ACCRUAL_START_DATES[0], ACCRUAL_END_DATES[NB_SUB_PERIOD - 1],
        PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXED_RATE + 1, ACCRUAL_START_DATES, ACCRUAL_END_DATES, PAYMENT_ACCRUAL_FACTORS);
    assertFalse("CouponIbor: equal-hash", COUPON.equals(modified));
  }

}

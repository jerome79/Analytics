/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.payment;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import java.time.Period;
import java.time.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.convention.daycount.DayCount;
import com.opengamma.analytics.convention.daycount.DayCountUtils;
import com.opengamma.analytics.convention.daycount.DayCounts;
import com.opengamma.analytics.financial.instrument.index.GeneratorDeposit;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.generator.USDDeposit;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.DateUtils;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.date.BusinessDayConvention;
import com.opengamma.strata.basics.date.BusinessDayConventions;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.basics.date.HolidayCalendars;

/**
 * Tests the constructors and equal/hash for CouponFixedDefinition.
 */
@Test
public class CouponFixedDefinitionTest {
  private static final Currency CUR = Currency.EUR;
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2011, 4, 6);
  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtils.getUTCDate(2011, 1, 5);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtils.getUTCDate(2011, 4, 5);
  private static final DayCount DAY_COUNT = DayCounts.ACT_360;
  private static final double ACCRUAL_FACTOR = DayCountUtils.yearFraction(DAY_COUNT, ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double NOTIONAL = 1000000; //1m
  private static final double RATE = 0.04;
  private static final ZonedDateTime FAKE_DATE = DateUtils.getUTCDate(0, 1, 1);
  private static final HolidayCalendar CALENDAR = HolidayCalendars.SAT_SUN;
  private static final BusinessDayConvention BD_CONVENTION = BusinessDayConventions.FOLLOWING;
  private static final IborIndex INDEX = new IborIndex(CUR, Period.ofMonths(6), 0, DAY_COUNT, BD_CONVENTION, false, "Ibor");
  private static final CouponFloatingDefinition COUPON = new CouponIborDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FAKE_DATE, INDEX, CALENDAR);
  private static final CouponFixedDefinition FIXED_COUPON = new CouponFixedDefinition(COUPON, RATE);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 12, 27); //For conversion to derivative

  @Test
  public void test() {
    assertEquals(FIXED_COUPON.getPaymentDate(), COUPON.getPaymentDate());
    assertEquals(FIXED_COUPON.getAccrualStartDate(), COUPON.getAccrualStartDate());
    assertEquals(FIXED_COUPON.getAccrualEndDate(), COUPON.getAccrualEndDate());
    assertEquals(FIXED_COUPON.getPaymentYearFraction(), COUPON.getPaymentYearFraction(), 1E-10);
    assertEquals(FIXED_COUPON.getNotional(), COUPON.getNotional(), 1E-2);
    assertEquals(FIXED_COUPON.getRate(), RATE, 1E-10);
    assertEquals(FIXED_COUPON.getAmount(), RATE * NOTIONAL * ACCRUAL_FACTOR, 1E-10);
  }

  @Test
  public void fromGeneratorDeposit() {
    final GeneratorDeposit generator = new USDDeposit(CALENDAR);
    final Period tenor = Period.ofMonths(3);
    final CouponFixedDefinition cpnFixed = CouponFixedDefinition.from(ACCRUAL_START_DATE, tenor, generator, NOTIONAL, RATE);
    final ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(ACCRUAL_START_DATE, tenor, generator.getBusinessDayConvention(), CALENDAR, generator.isEndOfMonth());
    final double accrual = DayCountUtils.yearFraction(generator.getDayCount(), ACCRUAL_START_DATE, endDate);
    final CouponFixedDefinition cpnExpected = new CouponFixedDefinition(generator.getCurrency(), endDate, ACCRUAL_START_DATE, endDate, accrual, NOTIONAL, RATE);
    assertEquals("CouponFixedDefinition: from deposit generator", cpnExpected, cpnFixed);
  }

  @Test
  public void testEqualHash() {
    final CouponFixedDefinition comparedCoupon = new CouponFixedDefinition(COUPON, RATE);
    assertEquals(comparedCoupon, FIXED_COUPON);
    assertEquals(comparedCoupon.hashCode(), FIXED_COUPON.hashCode());
    final CouponFixedDefinition modifiedCoupon = new CouponFixedDefinition(COUPON, RATE + 0.01);
    assertFalse(FIXED_COUPON.equals(modifiedCoupon));
  }

  @Test
  public void testToDerivative() {
    final DayCount actAct = DayCounts.ACT_ACT_ISDA;
    final double paymentTime = DayCountUtils.yearFraction(actAct, REFERENCE_DATE, PAYMENT_DATE);
    final CouponFixed couponFixed = new CouponFixed(CUR, paymentTime, ACCRUAL_FACTOR, NOTIONAL, RATE, FIXED_COUPON.getAccrualStartDate(), FIXED_COUPON.getAccrualEndDate());
    final CouponFixed convertedDefinition = FIXED_COUPON.toDerivative(REFERENCE_DATE);
    assertEquals(couponFixed, convertedDefinition);
  }
}

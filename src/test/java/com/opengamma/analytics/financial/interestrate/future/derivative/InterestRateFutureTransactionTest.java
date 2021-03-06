/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.derivative;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneOffset;
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
 * Tests related to the construction of interest rate future security.
 */
@Test
public class InterestRateFutureTransactionTest {

  //EURIBOR 3M Index
  private static final Period TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final HolidayCalendar CALENDAR = HolidayCalendars.SAT_SUN;
  private static final DayCount DAY_COUNT_INDEX = DayCounts.ACT_360;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final Currency CUR = Currency.EUR;
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM, "Ibor");
  // Future
  private static final ZonedDateTime SPOT_LAST_TRADING_DATE = DateUtils.getUTCDate(2012, 9, 19);
  private static final ZonedDateTime LAST_TRADING_DATE = ScheduleCalculator.getAdjustedDate(SPOT_LAST_TRADING_DATE, -SETTLEMENT_DAYS, CALENDAR);
  private static final ZonedDateTime FIXING_END_DATE = ScheduleCalculator.getAdjustedDate(SPOT_LAST_TRADING_DATE, TENOR, BUSINESS_DAY, CALENDAR, IS_EOM);
  private static final double NOTIONAL = 1000000.0; // 1m
  private static final double FUTURE_FACTOR = 0.25;
  private static final double REFERENCE_PRICE = 0.0;
  private static final String NAME = "ERU2";
  private static final int QUANTITY = 123;

  private static final LocalDate REFERENCE_DATE = LocalDate.of(2010, 8, 18);
  private static final DayCount ACT_ACT = DayCounts.ACT_ACT_ISDA;
  private static final ZonedDateTime REFERENCE_DATE_ZONED = ZonedDateTime.of(LocalDateTime.of(REFERENCE_DATE, LocalTime.MIDNIGHT), ZoneOffset.UTC);
  private static final double LAST_TRADING_TIME = DayCountUtils.yearFraction(ACT_ACT, REFERENCE_DATE_ZONED, LAST_TRADING_DATE);
  private static final double FIXING_START_TIME = DayCountUtils.yearFraction(ACT_ACT, REFERENCE_DATE_ZONED, SPOT_LAST_TRADING_DATE);
  private static final double FIXING_END_TIME = DayCountUtils.yearFraction(ACT_ACT, REFERENCE_DATE_ZONED, FIXING_END_DATE);
  private static final double FIXING_ACCRUAL = DayCountUtils.yearFraction(DAY_COUNT_INDEX, SPOT_LAST_TRADING_DATE, FIXING_END_DATE);
  private static final InterestRateFutureSecurity ERU2 = new InterestRateFutureSecurity(LAST_TRADING_TIME, IBOR_INDEX, FIXING_START_TIME,
      FIXING_END_TIME, FIXING_ACCRUAL, NOTIONAL, FUTURE_FACTOR, NAME);
  private static final InterestRateFutureTransaction ERU2_TRA = new InterestRateFutureTransaction(ERU2, REFERENCE_PRICE, QUANTITY);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullUnderlying() {
    new InterestRateFutureTransaction(null, REFERENCE_PRICE, QUANTITY);
  }

  @Test
  public void getter() {
    assertEquals("InterestRateFutureTransaction: equal-hash", ERU2, ERU2_TRA.getUnderlyingSecurity());
    assertEquals(REFERENCE_PRICE, ERU2_TRA.getReferencePrice());
    assertEquals(QUANTITY, ERU2_TRA.getQuantity());
  }

  @Test
  public void equalHash() {
    assertTrue("InterestRateFutureTransaction: equal-hash", ERU2_TRA.equals(ERU2_TRA));
    final InterestRateFutureTransaction other = new InterestRateFutureTransaction(ERU2, REFERENCE_PRICE, QUANTITY);
    assertTrue("InterestRateFutureTransaction: equal-hash", ERU2_TRA.equals(other));
    assertTrue("InterestRateFutureTransaction: equal-hash", ERU2_TRA.hashCode() == other.hashCode());
    assertEquals("InterestRateFutureTransaction: equal-hash", ERU2_TRA.toString(), other.toString());
    InterestRateFutureTransaction modifiedFuture;
    final InterestRateFutureSecurity modifiedSecurity = new InterestRateFutureSecurity(LAST_TRADING_TIME, IBOR_INDEX, FIXING_START_TIME,
        FIXING_END_TIME, FIXING_ACCRUAL, NOTIONAL, FUTURE_FACTOR * 2, NAME);
    modifiedFuture = new InterestRateFutureTransaction(modifiedSecurity, REFERENCE_PRICE, QUANTITY);
    assertFalse("InterestRateFutureTransaction: equal-hash", ERU2_TRA.equals(modifiedFuture));
    modifiedFuture = new InterestRateFutureTransaction(ERU2, REFERENCE_PRICE + 0.1, QUANTITY);
    assertFalse("InterestRateFutureTransaction: equal-hash", ERU2_TRA.equals(modifiedFuture));
    modifiedFuture = new InterestRateFutureTransaction(ERU2, REFERENCE_PRICE, QUANTITY + 1);
    assertFalse("InterestRateFutureTransaction: equal-hash", ERU2_TRA.equals(modifiedFuture));
    assertFalse("InterestRateFutureTransaction: equal-hash", ERU2_TRA.equals(LAST_TRADING_DATE));
    assertFalse("InterestRateFutureTransaction: equal-hash", ERU2_TRA.equals(null));
  }

}

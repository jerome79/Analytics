/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.derivative;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

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
 * Test related to the construction of Cap/floor on Ibor.
 */
@Test
public class CapFloorIborTest {

  private static final Currency CUR = Currency.EUR;
  private static final Period TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final HolidayCalendar CALENDAR = HolidayCalendars.SAT_SUN;
  private static final DayCount DAY_COUNT_INDEX = DayCounts.ACT_360;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final IborIndex INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM, "Ibor");

  private static final double NOTIONAL = 1000000;
  private static final double STRIKE = 0.04;
  private static final boolean IS_CAP = true;
  // The dates are not standard but selected for insure correct testing.
  private static final ZonedDateTime FIXING_DATE = DateUtils.getUTCDate(2011, 1, 3);
  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtils.getUTCDate(2011, 1, 6);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtils.getUTCDate(2011, 4, 4);
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2011, 4, 6);
  private static final ZonedDateTime FIXING_START_DATE = ScheduleCalculator.getAdjustedDate(FIXING_DATE, SETTLEMENT_DAYS, CALENDAR);
  private static final ZonedDateTime FIXING_END_DATE = ScheduleCalculator.getAdjustedDate(FIXING_START_DATE, TENOR, BUSINESS_DAY, CALENDAR);
  private static final DayCount DAY_COUNT_COUPON = DayCounts.ACT_365F;
  private static final double PAYMENT_YEAR_FRACTION = DayCountUtils.yearFraction(DAY_COUNT_COUPON, ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double FIXING_YEAR_FRACTION = DayCountUtils.yearFraction(DAY_COUNT_INDEX, FIXING_START_DATE, FIXING_END_DATE);
  // Reference date and time.
  private static final LocalDate REFERENCE_DATE = LocalDate.of(2010, 12, 27); //For conversion to derivative
  private static final DayCount ACT_ACT = DayCounts.ACT_ACT_ISDA;
  private static final ZonedDateTime REFERENCE_DATE_ZONED = ZonedDateTime.of(LocalDateTime.of(REFERENCE_DATE, LocalTime.MIDNIGHT), ZoneOffset.UTC);
  private static final double PAYMENT_TIME = DayCountUtils.yearFraction(ACT_ACT, REFERENCE_DATE_ZONED, PAYMENT_DATE);
  private static final double FIXING_TIME = DayCountUtils.yearFraction(ACT_ACT, REFERENCE_DATE_ZONED, FIXING_DATE);
  private static final double FIXING_START_TIME = DayCountUtils.yearFraction(ACT_ACT, REFERENCE_DATE_ZONED, FIXING_START_DATE);
  private static final double FIXING_END_TIME = DayCountUtils.yearFraction(ACT_ACT, REFERENCE_DATE_ZONED, FIXING_END_DATE);

  private static final CapFloorIbor CAP = new CapFloorIbor(CUR, PAYMENT_TIME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME,
      FIXING_YEAR_FRACTION, STRIKE, IS_CAP);

  @Test
  public void testGetters() {
    assertEquals("Getter strike", STRIKE, CAP.getStrike());
    assertEquals("Getter cap flag", IS_CAP, CAP.isCap());
    final double fixingRate = 0.05;
    assertEquals("Pay-off", Math.max(fixingRate - STRIKE, 0), CAP.payOff(fixingRate));
  }

  @Test
  public void withStrike() {
    final double otherStrike = STRIKE + 0.01;
    final CapFloorIbor otherCap = new CapFloorIbor(CUR, PAYMENT_TIME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_YEAR_FRACTION, otherStrike, IS_CAP);
    final CapFloorIbor otherCapWith = CAP.withStrike(otherStrike);
    assertEquals("Strike", otherStrike, otherCapWith.getStrike());
    assertEquals("Pay-off", otherCap, otherCapWith);
  }

  @Test
  public void withNotional() {
    final double notional = NOTIONAL + 10000;
    final CapFloorIbor cap = new CapFloorIbor(CUR, PAYMENT_TIME, PAYMENT_YEAR_FRACTION, notional, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_YEAR_FRACTION, STRIKE, IS_CAP);
    assertEquals(cap, CAP.withNotional(notional));
  }

  @Test
  public void testToCoupon() {

  }

  @Test
  public void testHashCodeEquals() {
    final CapFloorIbor cap = new CapFloorIbor(CUR, PAYMENT_TIME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_YEAR_FRACTION, STRIKE, IS_CAP);
    CapFloorIbor other = new CapFloorIbor(CUR, PAYMENT_TIME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_YEAR_FRACTION, STRIKE, IS_CAP);
    assertEquals(cap, other);
    assertEquals(cap.hashCode(), other.hashCode());
    other = new CapFloorIbor(Currency.AUD, PAYMENT_TIME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_YEAR_FRACTION, STRIKE, IS_CAP);
    assertFalse(other.equals(cap));
    other = new CapFloorIbor(CUR, PAYMENT_TIME + 1, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_YEAR_FRACTION, STRIKE, IS_CAP);
    assertFalse(other.equals(cap));
    other = new CapFloorIbor(CUR, PAYMENT_TIME, PAYMENT_YEAR_FRACTION + 1, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_YEAR_FRACTION, STRIKE, IS_CAP);
    assertFalse(other.equals(cap));
    other = new CapFloorIbor(CUR, PAYMENT_TIME, PAYMENT_YEAR_FRACTION, NOTIONAL + 1, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_YEAR_FRACTION, STRIKE, IS_CAP);
    assertFalse(other.equals(cap));
    other = new CapFloorIbor(CUR, PAYMENT_TIME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_TIME - 1e-8, INDEX, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_YEAR_FRACTION, STRIKE, IS_CAP);
    assertFalse(other.equals(cap));
    final IborIndex index = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, DAY_COUNT_INDEX, BUSINESS_DAY, !IS_EOM, "Ibor");
    other = new CapFloorIbor(CUR, PAYMENT_TIME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_TIME, index, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_YEAR_FRACTION, STRIKE, IS_CAP);
    assertFalse(other.equals(cap));
    other = new CapFloorIbor(CUR, PAYMENT_TIME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME - 1e-8, FIXING_END_TIME,
        FIXING_YEAR_FRACTION, STRIKE, IS_CAP);
    assertFalse(other.equals(cap));
    other = new CapFloorIbor(CUR, PAYMENT_TIME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME + 1,
        FIXING_YEAR_FRACTION, STRIKE, IS_CAP);
    assertFalse(other.equals(cap));
    other = new CapFloorIbor(CUR, PAYMENT_TIME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_YEAR_FRACTION + 1, STRIKE, IS_CAP);
    assertFalse(other.equals(cap));
    other = new CapFloorIbor(CUR, PAYMENT_TIME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_YEAR_FRACTION, STRIKE + 1, IS_CAP);
    assertFalse(other.equals(cap));
    other = new CapFloorIbor(CUR, PAYMENT_TIME, PAYMENT_YEAR_FRACTION, NOTIONAL, FIXING_TIME, INDEX, FIXING_START_TIME, FIXING_END_TIME,
        FIXING_YEAR_FRACTION, STRIKE, !IS_CAP);
    assertFalse(other.equals(cap));
  }
}

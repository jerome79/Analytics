/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import java.time.DayOfWeek;
import java.util.Arrays;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.opengamma.analytics.convention.daycount.DayCount;
import com.opengamma.analytics.convention.daycount.DayCounts;
import com.opengamma.strata.basics.date.BusinessDayConvention;
import com.opengamma.strata.basics.date.BusinessDayConventions;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.basics.date.HolidayCalendars;
import com.opengamma.strata.basics.date.ImmutableHolidayCalendar;

/**
 * Test.
 */
@Test
public class ConventionTest {
  private static final int SETTLEMENT_DAYS = 2;
  private static final DayCount DAY_COUNT = DayCounts.ACT_360;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.FOLLOWING;
  private static final HolidayCalendar CALENDAR = HolidayCalendars.SAT_SUN;
  private static final String NAME = "CONVENTION";
  private static final Convention CONVENTION = new Convention(SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, CALENDAR, NAME);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeSettlementDays() {
    new Convention(-SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, CALENDAR, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDayCount() {
    new Convention(SETTLEMENT_DAYS, null, BUSINESS_DAY, CALENDAR, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBusinessDayConvention() {
    new Convention(SETTLEMENT_DAYS, DAY_COUNT, null, CALENDAR, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalendar() {
    new Convention(SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, null, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName() {
    new Convention(SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, CALENDAR, null);
  }

  @Test
  public void testGetters() {
    assertEquals(CONVENTION.getBusinessDayConvention(), BUSINESS_DAY);
    assertEquals(CONVENTION.getDayCount(), DAY_COUNT);
    assertEquals(CONVENTION.getName(), NAME);
    assertEquals(CONVENTION.getSettlementDays(), SETTLEMENT_DAYS);
    assertEquals(CONVENTION.getWorkingDayCalendar(), CALENDAR);
  }

  @Test
  public void testHashCodeAndEquals() {
    Convention other = new Convention(SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, CALENDAR, NAME);
    assertEquals(CONVENTION, other);
    assertEquals(CONVENTION.hashCode(), other.hashCode());
    other = new Convention(SETTLEMENT_DAYS + 1, DAY_COUNT, BUSINESS_DAY, CALENDAR, NAME);
    assertFalse(CONVENTION.equals(other));
    other = new Convention(SETTLEMENT_DAYS, DayCounts.ACT_365F, BUSINESS_DAY, CALENDAR, NAME);
    assertFalse(CONVENTION.equals(other));
    other = new Convention(SETTLEMENT_DAYS, DAY_COUNT, BusinessDayConventions.NO_ADJUST, CALENDAR, NAME);
    assertFalse(CONVENTION.equals(other));
    other = new Convention(SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY,
        ImmutableHolidayCalendar.of("NoWorkingDays", ImmutableList.of(), Arrays.asList(DayOfWeek.values())), NAME);
    assertFalse(CONVENTION.equals(other));
    other = new Convention(SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, CALENDAR, NAME + ")");
    assertFalse(CONVENTION.equals(other));
  }

}

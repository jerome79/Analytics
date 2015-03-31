/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.util.time;

import static org.testng.AssertJUnit.assertEquals;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.basics.date.HolidayCalendars;

/**
 * Test.
 */
@Test
public class TenorUtilsTest {
  /** Empty holiday calendar */
  private static final HolidayCalendar NO_HOLIDAYS = HolidayCalendars.NO_HOLIDAYS;
  /** Holiday calendar containing only weekends */
  private static final HolidayCalendar WEEKEND_CALENDAR = HolidayCalendars.SAT_SUN;
  /** Holiday calendar containing weekends and 1/1/2014 */
  private static final HolidayCalendar CALENDAR = new MyCalendar();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAdjustZonedDateTime1() {
    final ZonedDateTime zonedDateTime = DateUtils.getUTCDate(2013, 12, 31);
    assertEquals(DateUtils.getUTCDate(2014, 12, 31), TenorUtils.adjustDateByTenor(zonedDateTime, ComparableTenor.ONE_YEAR));
    assertEquals(DateUtils.getUTCDate(2014, 1, 31), TenorUtils.adjustDateByTenor(zonedDateTime, ComparableTenor.ONE_MONTH));
    assertEquals(DateUtils.getUTCDate(2014, 1, 1), TenorUtils.adjustDateByTenor(zonedDateTime, ComparableTenor.ONE_DAY));
    TenorUtils.adjustDateByTenor(zonedDateTime, ComparableTenor.ON);
  }

  @Test
  public void testAdjustZonedDateTime2() {
    final int spotDays = 2;
    final ZonedDateTime zonedDateTime = DateUtils.getUTCDate(2013, 12, 31);
    assertEquals(DateUtils.getUTCDate(2014, 12, 31), TenorUtils.adjustDateByTenor(zonedDateTime, ComparableTenor.ONE_YEAR, CALENDAR, spotDays));
    assertEquals(DateUtils.getUTCDate(2014, 1, 31), TenorUtils.adjustDateByTenor(zonedDateTime, ComparableTenor.ONE_MONTH, CALENDAR, spotDays));
    assertEquals(DateUtils.getUTCDate(2014, 1, 1), TenorUtils.adjustDateByTenor(zonedDateTime, ComparableTenor.ONE_DAY, CALENDAR, spotDays));
    assertEquals(DateUtils.getUTCDate(2014, 1, 1), TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2013, 12, 31), ComparableTenor.ON, NO_HOLIDAYS, spotDays));
    assertEquals(DateUtils.getUTCDate(2014, 1, 2), TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2013, 12, 31), ComparableTenor.TN, NO_HOLIDAYS, spotDays));
    assertEquals(DateUtils.getUTCDate(2014, 1, 3), TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2013, 12, 31), ComparableTenor.SN, NO_HOLIDAYS, spotDays));
    assertEquals(DateUtils.getUTCDate(2014, 1, 1), TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2013, 12, 31), ComparableTenor.ON, WEEKEND_CALENDAR, spotDays));
    assertEquals(DateUtils.getUTCDate(2014, 1, 2), TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2013, 12, 31), ComparableTenor.TN, WEEKEND_CALENDAR, spotDays));
    assertEquals(DateUtils.getUTCDate(2014, 1, 3), TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2013, 12, 31), ComparableTenor.SN, WEEKEND_CALENDAR, spotDays));
    assertEquals(DateUtils.getUTCDate(2014, 1, 2), TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2014, 1, 1), ComparableTenor.ON, NO_HOLIDAYS, spotDays));
    assertEquals(DateUtils.getUTCDate(2014, 1, 3), TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2014, 1, 1), ComparableTenor.TN, NO_HOLIDAYS, spotDays));
    assertEquals(DateUtils.getUTCDate(2014, 1, 4), TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2014, 1, 1), ComparableTenor.SN, NO_HOLIDAYS, spotDays));
    assertEquals(DateUtils.getUTCDate(2014, 1, 2), TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2014, 1, 1), ComparableTenor.ON, WEEKEND_CALENDAR, spotDays));
    assertEquals(DateUtils.getUTCDate(2014, 1, 3), TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2014, 1, 1), ComparableTenor.TN, WEEKEND_CALENDAR, spotDays));
    assertEquals(DateUtils.getUTCDate(2014, 1, 6), TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2014, 1, 1), ComparableTenor.SN, WEEKEND_CALENDAR, spotDays));
    assertEquals(DateUtils.getUTCDate(2014, 1, 2), TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2013, 12, 31), ComparableTenor.ON, CALENDAR, spotDays));
    assertEquals(DateUtils.getUTCDate(2014, 1, 3), TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2013, 12, 31), ComparableTenor.TN, CALENDAR, spotDays));
    assertEquals(DateUtils.getUTCDate(2014, 1, 6), TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2013, 12, 31), ComparableTenor.SN, CALENDAR, spotDays));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAdjustLocalDateTime1() {
    final LocalDateTime localDateTime = LocalDateTime.of(2013, 12, 31, 11, 0);
    assertEquals(LocalDateTime.of(2014, 12, 31, 11, 0), TenorUtils.adjustDateByTenor(localDateTime, ComparableTenor.ONE_YEAR));
    assertEquals(LocalDateTime.of(2014, 1, 31, 11, 0), TenorUtils.adjustDateByTenor(localDateTime, ComparableTenor.ONE_MONTH));
    assertEquals(LocalDateTime.of(2014, 1, 1, 11, 0), TenorUtils.adjustDateByTenor(localDateTime, ComparableTenor.ONE_DAY));
    TenorUtils.adjustDateByTenor(localDateTime, ComparableTenor.ON);
  }

  @Test
  public void testAdjustLocalDateTime2() {
    final int spotDays = 2;
    final LocalDateTime localDateTime = LocalDateTime.of(2013, 12, 31, 11, 0);
    assertEquals(LocalDateTime.of(2014, 12, 31, 11, 0), TenorUtils.adjustDateByTenor(localDateTime, ComparableTenor.ONE_YEAR, CALENDAR, spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 31, 11, 0), TenorUtils.adjustDateByTenor(localDateTime, ComparableTenor.ONE_MONTH, CALENDAR, spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 1, 11, 0), TenorUtils.adjustDateByTenor(localDateTime, ComparableTenor.ONE_DAY, CALENDAR, spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 1, 11, 0), TenorUtils.adjustDateByTenor(LocalDateTime.of(2013, 12, 31, 11, 0), ComparableTenor.ON, NO_HOLIDAYS, spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 2, 11, 0), TenorUtils.adjustDateByTenor(LocalDateTime.of(2013, 12, 31, 11, 0), ComparableTenor.TN, NO_HOLIDAYS, spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 3, 11, 0), TenorUtils.adjustDateByTenor(LocalDateTime.of(2013, 12, 31, 11, 0), ComparableTenor.SN, NO_HOLIDAYS, spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 1, 11, 0), TenorUtils.adjustDateByTenor(LocalDateTime.of(2013, 12, 31, 11, 0), ComparableTenor.ON, WEEKEND_CALENDAR, spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 2, 11, 0), TenorUtils.adjustDateByTenor(LocalDateTime.of(2013, 12, 31, 11, 0), ComparableTenor.TN, WEEKEND_CALENDAR, spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 3, 11, 0), TenorUtils.adjustDateByTenor(LocalDateTime.of(2013, 12, 31, 11, 0), ComparableTenor.SN, WEEKEND_CALENDAR, spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 2, 11, 0), TenorUtils.adjustDateByTenor(LocalDateTime.of(2014, 1, 1, 11, 0), ComparableTenor.ON, NO_HOLIDAYS, spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 3, 11, 0), TenorUtils.adjustDateByTenor(LocalDateTime.of(2014, 1, 1, 11, 0), ComparableTenor.TN, NO_HOLIDAYS, spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 4, 11, 0), TenorUtils.adjustDateByTenor(LocalDateTime.of(2014, 1, 1, 11, 0), ComparableTenor.SN, NO_HOLIDAYS, spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 2, 11, 0), TenorUtils.adjustDateByTenor(LocalDateTime.of(2014, 1, 1, 11, 0), ComparableTenor.ON, WEEKEND_CALENDAR, spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 3, 11, 0), TenorUtils.adjustDateByTenor(LocalDateTime.of(2014, 1, 1, 11, 0), ComparableTenor.TN, WEEKEND_CALENDAR, spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 6, 11, 0), TenorUtils.adjustDateByTenor(LocalDateTime.of(2014, 1, 1, 11, 0), ComparableTenor.SN, WEEKEND_CALENDAR, spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 2, 11, 0), TenorUtils.adjustDateByTenor(LocalDateTime.of(2013, 12, 31, 11, 0), ComparableTenor.ON, CALENDAR, spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 3, 11, 0), TenorUtils.adjustDateByTenor(LocalDateTime.of(2013, 12, 31, 11, 0), ComparableTenor.TN, CALENDAR, spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 6, 11, 0), TenorUtils.adjustDateByTenor(LocalDateTime.of(2013, 12, 31, 11, 0), ComparableTenor.SN, CALENDAR, spotDays));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAdjustLocalDate1() {
    final LocalDate localDate = LocalDate.of(2013, 12, 31);
    assertEquals(LocalDate.of(2014, 12, 31), TenorUtils.adjustDateByTenor(localDate, ComparableTenor.ONE_YEAR));
    assertEquals(LocalDate.of(2014, 1, 31), TenorUtils.adjustDateByTenor(localDate, ComparableTenor.ONE_MONTH));
    assertEquals(LocalDate.of(2014, 1, 1), TenorUtils.adjustDateByTenor(localDate, ComparableTenor.ONE_DAY));
    TenorUtils.adjustDateByTenor(localDate, ComparableTenor.ON);
  }

  @Test
  public void testAdjustLocalDate2() {
    final int spotDays = 2;
    final LocalDate localDate = LocalDate.of(2013, 12, 31);
    assertEquals(LocalDate.of(2014, 12, 31), TenorUtils.adjustDateByTenor(localDate, ComparableTenor.ONE_YEAR, CALENDAR, spotDays));
    assertEquals(LocalDate.of(2014, 1, 31), TenorUtils.adjustDateByTenor(localDate, ComparableTenor.ONE_MONTH, CALENDAR, spotDays));
    assertEquals(LocalDate.of(2014, 1, 1), TenorUtils.adjustDateByTenor(localDate, ComparableTenor.ONE_DAY, CALENDAR, spotDays));
    assertEquals(LocalDate.of(2014, 1, 1), TenorUtils.adjustDateByTenor(LocalDate.of(2013, 12, 31), ComparableTenor.ON, NO_HOLIDAYS, spotDays));
    assertEquals(LocalDate.of(2014, 1, 2), TenorUtils.adjustDateByTenor(LocalDate.of(2013, 12, 31), ComparableTenor.TN, NO_HOLIDAYS, spotDays));
    assertEquals(LocalDate.of(2014, 1, 3), TenorUtils.adjustDateByTenor(LocalDate.of(2013, 12, 31), ComparableTenor.SN, NO_HOLIDAYS, spotDays));
    assertEquals(LocalDate.of(2014, 1, 1), TenorUtils.adjustDateByTenor(LocalDate.of(2013, 12, 31), ComparableTenor.ON, WEEKEND_CALENDAR, spotDays));
    assertEquals(LocalDate.of(2014, 1, 2), TenorUtils.adjustDateByTenor(LocalDate.of(2013, 12, 31), ComparableTenor.TN, WEEKEND_CALENDAR, spotDays));
    assertEquals(LocalDate.of(2014, 1, 3), TenorUtils.adjustDateByTenor(LocalDate.of(2013, 12, 31), ComparableTenor.SN, WEEKEND_CALENDAR, spotDays));
    assertEquals(LocalDate.of(2014, 1, 2), TenorUtils.adjustDateByTenor(LocalDate.of(2014, 1, 1), ComparableTenor.ON, NO_HOLIDAYS, spotDays));
    assertEquals(LocalDate.of(2014, 1, 3), TenorUtils.adjustDateByTenor(LocalDate.of(2014, 1, 1), ComparableTenor.TN, NO_HOLIDAYS, spotDays));
    assertEquals(LocalDate.of(2014, 1, 4), TenorUtils.adjustDateByTenor(LocalDate.of(2014, 1, 1), ComparableTenor.SN, NO_HOLIDAYS, spotDays));
    assertEquals(LocalDate.of(2014, 1, 2), TenorUtils.adjustDateByTenor(LocalDate.of(2014, 1, 1), ComparableTenor.ON, WEEKEND_CALENDAR, spotDays));
    assertEquals(LocalDate.of(2014, 1, 3), TenorUtils.adjustDateByTenor(LocalDate.of(2014, 1, 1), ComparableTenor.TN, WEEKEND_CALENDAR, spotDays));
    assertEquals(LocalDate.of(2014, 1, 6), TenorUtils.adjustDateByTenor(LocalDate.of(2014, 1, 1), ComparableTenor.SN, WEEKEND_CALENDAR, spotDays));
    assertEquals(LocalDate.of(2014, 1, 2), TenorUtils.adjustDateByTenor(LocalDate.of(2013, 12, 31), ComparableTenor.ON, CALENDAR, spotDays));
    assertEquals(LocalDate.of(2014, 1, 3), TenorUtils.adjustDateByTenor(LocalDate.of(2013, 12, 31), ComparableTenor.TN, CALENDAR, spotDays));
    assertEquals(LocalDate.of(2014, 1, 6), TenorUtils.adjustDateByTenor(LocalDate.of(2013, 12, 31), ComparableTenor.SN, CALENDAR, spotDays));
  }

  @Test
  public void plus() {
    final ComparableTenor d1 = ComparableTenor.ONE_DAY;
    final ComparableTenor d2 = ComparableTenor.TWO_DAYS;
    final ComparableTenor w1 = ComparableTenor.ONE_WEEK;
    final ComparableTenor m1 = ComparableTenor.ONE_MONTH;
    final ComparableTenor y3 = ComparableTenor.THREE_YEARS;
    final ComparableTenor on = ComparableTenor.ON;
    final ComparableTenor tn = ComparableTenor.TN;
    final ComparableTenor pZ = ComparableTenor.of(Period.ZERO);
    final ComparableTenor p0D = ComparableTenor.of(Period.ofDays(0));
    assertEquals("Tenor: plus", d2, TenorUtils.plus(d1, d1));
    assertEquals("Tenor: plus", ComparableTenor.of(Period.ofDays(8)), TenorUtils.plus(d1, w1));
    assertEquals("Tenor: plus", ComparableTenor.of(Period.of(3, 1, 0)), TenorUtils.plus(m1, y3));
    assertEquals("Tenor: plus", ComparableTenor.of(Period.of(3, 1, 0)), TenorUtils.plus(y3, m1));
    assertEquals("Tenor: plus", tn, TenorUtils.plus(on, on));
    assertEquals("Tenor: plus", pZ, p0D);
    assertEquals("Tenor: plus", y3, TenorUtils.plus(pZ, y3));
    assertEquals("Tenor: plus", y3, TenorUtils.plus(y3, pZ));
    assertEquals("Tenor: plus", y3, TenorUtils.plus(y3, p0D));
  }

  /**
   * HolidayCalendar with weekends and 1-1-2013, 1-1-2014 as holidays
   */
  private static class MyCalendar implements HolidayCalendar {

    /**
     * Default constructor
     */
    protected MyCalendar() {
      super();
    }

    @Override
    public boolean isHoliday(LocalDate date) {
      if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
        return true;
      }
      if (date.equals(LocalDate.of(2014, 1, 1))) {
        return true;
      }
      return false;
    }

    @Override
    public String getName() {
      return "";
    }
  }
}

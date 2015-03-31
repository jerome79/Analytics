/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.businessday;

import static com.opengamma.financial.convention.businessday.BusinessDayDateUtils.getDaysBetween;
import static com.opengamma.financial.convention.businessday.BusinessDayDateUtils.getWorkingDaysInclusive;
import static org.testng.AssertJUnit.assertEquals;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.ExceptionCalendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;


@Test
public class BusinessDayDateUtilTest {
  private static final ZoneId UTC = ZoneId.of("UTC");
  private static final Calendar WEEKEND_CALENDAR = new MondayToFridayCalendar("Weekend");
  private static final Calendar HOLIDAY_CALENDAR = new MyCalendar("Holiday");

  @Test
  public void dayBetweenTest() {

    ZonedDateTime d0 = ZonedDateTime.of(2013, 8, 1, 12, 0, 0, 0, UTC); // Thursday
    ZonedDateTime d1 = ZonedDateTime.of(2014, 7, 21, 12, 0, 0, 0, UTC); // Monday
    ZonedDateTime d2 = ZonedDateTime.of(2014, 7, 26, 12, 0, 0, 0, UTC); // Saturday
    ZonedDateTime d3 = ZonedDateTime.of(2014, 7, 28, 12, 0, 0, 0, UTC); // Monday
    ZonedDateTime d4 = ZonedDateTime.of(2014, 8, 1, 12, 0, 0, 0, UTC); // Friday
    ZonedDateTime d5 = ZonedDateTime.of(2014, 7, 22, 12, 0, 0, 0, UTC); // Tuesday

    //same day
    assertEquals(0, getDaysBetween(d0, d0, WEEKEND_CALENDAR)); 

    assertEquals(1, getDaysBetween(d1, d5, WEEKEND_CALENDAR)); //Monday to Tuesday 
    assertEquals(5, getDaysBetween(d1, d2, WEEKEND_CALENDAR)); //Monday to Saturday
    assertEquals(5, getDaysBetween(d1, d3, WEEKEND_CALENDAR)); //Monday to following Monday
    assertEquals(0, getDaysBetween(d2, d3, WEEKEND_CALENDAR)); //Saturday to Monday
    assertEquals(4, getDaysBetween(d2, d4, WEEKEND_CALENDAR)); //Saturday to Friday
    assertEquals(3, getDaysBetween(d2, d4, HOLIDAY_CALENDAR)); //Saturday to Friday (with Wednesday a holiday) 

    //additive over a weekend 
    assertEquals(getDaysBetween(d1, d3, HOLIDAY_CALENDAR), getDaysBetween(d1, d2, HOLIDAY_CALENDAR) + getDaysBetween(d2, d3, HOLIDAY_CALENDAR));

    //long periods 
    assertEquals(261, getDaysBetween(d0, d4, WEEKEND_CALENDAR));
    assertEquals(259, getDaysBetween(d0, d4, HOLIDAY_CALENDAR));
  }

  @Test
  public void workingDaysTest() {
    ZonedDateTime d1 = ZonedDateTime.of(2014, 7, 21, 12, 0, 0, 0, UTC); // Monday
    ZonedDateTime d2 = ZonedDateTime.of(2014, 7, 26, 12, 0, 0, 0, UTC); // Saturday
    ZonedDateTime d3 = ZonedDateTime.of(2014, 8, 1, 12, 0, 0, 0, UTC); // Friday

    assertEquals(5, getWorkingDaysInclusive(d1, d2, WEEKEND_CALENDAR)); //Monday to Saturday
    assertEquals(10, getWorkingDaysInclusive(d1, d3, WEEKEND_CALENDAR)); //Monday to Friday week
    assertEquals(5, getWorkingDaysInclusive(d2, d3, WEEKEND_CALENDAR)); //Saturday to Friday
  }



  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongWayDatesTest() {
    ZonedDateTime d1 = ZonedDateTime.of(2014, 7, 21, 12, 0, 0, 0, UTC); // Monday
    ZonedDateTime d2 = ZonedDateTime.of(2014, 7, 26, 12, 0, 0, 0, UTC); // Saturday
    getDaysBetween(d2, d1, WEEKEND_CALENDAR);
  }

  private static class MyCalendar extends ExceptionCalendar {
    private static final long serialVersionUID = 1L;
    private static final LocalDate[] HOLIDAYS = new LocalDate[] {LocalDate.of(2014, 7, 30), LocalDate.of(2013, 12, 20) };

    protected MyCalendar(final String name) {
      super(name);
    }

    @Override
    protected boolean isNormallyWorkingDay(final LocalDate date) {
      if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
        return false;
      }
      for (final LocalDate holiday : HOLIDAYS) {
        if (date.equals(holiday)) {
          return false;
        }
      }
      return true;
    }

  }

}

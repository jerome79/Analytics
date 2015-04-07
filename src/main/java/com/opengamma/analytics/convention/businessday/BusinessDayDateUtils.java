/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.convention.businessday;

import java.time.LocalDate;
import java.time.ZonedDateTime;

import com.opengamma.strata.basics.date.BusinessDayConvention;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Utilities for managing the business day convention.
 * <p>
 * This is a thread-safe static utility class.
 */
public class BusinessDayDateUtils {

  /**
   * Restricted constructor.
   */
  protected BusinessDayDateUtils() {
    super();
  }

  //-------------------------------------------------------------------------
  /**
   * Adjusts a {@code ZonedDateTime} based on the specified convention.
   * 
   * @param convention  the business day convention
   * @param zdt  the date and time to adjust
   * @param calendar  the calendar of holidays
   * @return the adjusted date-time
   */
  public static ZonedDateTime applyConvention(
      BusinessDayConvention convention,
      ZonedDateTime zdt,
      HolidayCalendar calendar) {

    ArgChecker.notNull(convention, "convention");
    ArgChecker.notNull(zdt, "dateTime");
    ArgChecker.notNull(calendar, "holidayCalendar");
    return zdt.with(convention.adjust(zdt.toLocalDate(), calendar));
  }

  /**
   * Add a certain number of working days (defined by the holidayCalendar) to a date.
   * 
   * @param startDate The start date
   * @param workingDaysToAdd working days to add
   * @param calendar  the calendar of holidays
   * @return a working day
   */
  public static LocalDate addWorkDays(LocalDate startDate, int workingDaysToAdd, HolidayCalendar calendar) {
    ArgChecker.notNull(startDate, "startDate");
    ArgChecker.notNull(calendar, "calendar");

    int daysLeft = workingDaysToAdd;
    LocalDate temp = startDate;
    while (daysLeft > 0) {
      temp = temp.plusDays(1);
      if (calendar.isBusinessDay(temp)) {
        daysLeft--;
      }
    }
    return temp;
  }

  /**
   * Get the number of business days between two dates.
   * 
   * @param firstDate  the first date
   * @param secondDate  the second date
   * @param calendar  the calendar of holidays
   * @return the number of business (working) days between two dates
   */
  public static int getDaysBetween(LocalDate firstDate, LocalDate secondDate, HolidayCalendar calendar) {
    ArgChecker.notNull(firstDate, "firstDate");
    ArgChecker.notNull(secondDate, "secondDate");
    if (secondDate.isBefore(firstDate)) {
      throw new IllegalArgumentException("d2 must be on or after d1: have d1 = " + firstDate + " and d2 = " + secondDate);
    }
    ArgChecker.notNull(calendar, "calendar");

    int count = 0;
    LocalDate date = firstDate;
    while (date.isBefore(secondDate)) {
      if (calendar.isBusinessDay(date)) {
        count++;
      }
      date = date.plusDays(1);
    }
    return count;
  }

  /**
   * Get the number of business days between two dates. <b>Note:<b> these {@link ZonedDateTime} dates
   * are converted to {@link LocalDate}, so any time-of-day and time zone information is lost.
   * 
   * @param firstDate  the first date
   * @param secondDate  the second date
   * @param calendar  the calendar of holidays
   * @return the number of business (working) days between two dates
   */
  public static int getDaysBetween(ZonedDateTime firstDate, ZonedDateTime secondDate, HolidayCalendar calendar) {
    ArgChecker.notNull(firstDate, "first date");
    ArgChecker.notNull(secondDate, "second date");
    return getDaysBetween(firstDate.toLocalDate(), secondDate.toLocalDate(), calendar);
  }

  /**
   * Get the number of working days (according to the supplied calendar) inclusive of the final date.
   * <p>
   * For example, the number of days between 8/8/2014 (Monday) and 12/8/2014 (Friday) a weekend only
   * calendar is 5 (since Friday is a working day).
   * 
   * @param firstDate  the first date
   * @param secondDate  the second date
   * @param calendar  the calendar of holidays
   * @return the number of business (working) days between two dates, inclusive of the final date
   */
  public static int getWorkingDaysInclusive(LocalDate firstDate, LocalDate secondDate, HolidayCalendar calendar) {
    int res = getDaysBetween(firstDate, secondDate, calendar);
    if (calendar.isBusinessDay(secondDate)) {
      res++;
    }
    return res;
  }

  /**
   * Get the number of working days (according to the supplied calendar) inclusive of the final date.
   * <b>Note:<b> these {@link ZonedDateTime} dates are converted to {@link LocalDate}, so any
   * time-of-day and time zone information is lost
   * <p>
   * For example, the number of days between 8/8/2014 (Monday) and 12/8/2014 (Friday) a weekend only
   * calendar is 5 (since Friday is a working day).
   * 
   * @param firstDate  the first date
   * @param secondDate  the second date
   * @param calendar  the calendar of holidays
   * @return the number of business (working) days between two dates, inclusive of the final date
   */
  public static int getWorkingDaysInclusive(ZonedDateTime firstDate, ZonedDateTime secondDate, HolidayCalendar calendar) {
    ArgChecker.notNull(firstDate, "first date");
    ArgChecker.notNull(secondDate, "second date");
    return getWorkingDaysInclusive(firstDate.toLocalDate(), secondDate.toLocalDate(), calendar);
  }

}

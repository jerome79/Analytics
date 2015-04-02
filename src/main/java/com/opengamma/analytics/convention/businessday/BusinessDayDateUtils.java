/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.convention.businessday;

import java.time.LocalDate;
import java.time.ZonedDateTime;

import com.opengamma.analytics.convention.calendar.Calendar;
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

  // -------------------------------------------------------------------------
  /**
   * Add a certain number of working days (defined by the holidayCalendar) to a date
   * @param startDate The start date
   * @param workingDaysToAdd working days to add
   * @param holidayCalendar Defines what is a non-working day
   * @return a working day
   */
  public static LocalDate addWorkDays(final LocalDate startDate, final int workingDaysToAdd, final Calendar holidayCalendar) {
    ArgChecker.notNull(startDate, "null startDate");
    ArgChecker.notNull(holidayCalendar, "null holidayCalendar");

    int daysLeft = workingDaysToAdd;
    LocalDate temp = startDate;
    while (daysLeft > 0) {
      temp = temp.plusDays(1);
      if (holidayCalendar.isWorkingDay(temp)) {
        daysLeft--;
      }
    }
    return temp;
  }

  /**
   * Get the number of business days between two dates
   * @param firstDate The first date
   * @param secondDate the second date
   * @param calendar Calendar defining what is a working day
   * @return The number of business (working) days between two dates
   */
  public static int getDaysBetween(LocalDate firstDate, LocalDate secondDate, Calendar calendar) {
    ArgChecker.notNull(firstDate, "first date");
    ArgChecker.notNull(secondDate, "second date");
    if (secondDate.isBefore(firstDate)) {
      throw new IllegalArgumentException("d2 must be on or after d1: have d1 = " + firstDate + " and d2 = " + secondDate);
    }
    ArgChecker.notNull(calendar, "calendar");

    int count = 0;
    LocalDate date = firstDate;
    while (date.isBefore(secondDate)) {
      if (calendar.isWorkingDay(date)) {
        count++;
      }
      date = date.plusDays(1);
    }
    return count;
  }

  /**
   * Get the number of business days between two dates. <b>Note:<b> these {@link ZonedDateTime} dates are converted to {@link LocalDate}, so any time-of-day and time zone information is lost
   * @param firstDate The first date
   * @param secondDate the second date
   * @param calendar Calendar defining what is a working day
   * @return The number of business (working) days between two dates
   */
  public static int getDaysBetween(final ZonedDateTime firstDate, final ZonedDateTime secondDate, final Calendar calendar) {
    ArgChecker.notNull(firstDate, "first date");
    ArgChecker.notNull(secondDate, "second date");
    return getDaysBetween(firstDate.toLocalDate(), secondDate.toLocalDate(), calendar);
  }

  /**
   * Get the number of working days (according to the supplied calendar) inclusive of the final date.
   * <p>
   * For example, the number of days between 8/8/2014 (Monday) and 12/8/2014 (Friday) a weekend only calendar is 5 (since Friday is a working day)
   * @param firstDate The first date
   * @param secondDate the second date
   * @param calendar Calendar defining what is a working day
   * @return The number of business (working) days between two dates, inclusive of the final date
   */
  public static int getWorkingDaysInclusive(LocalDate firstDate, LocalDate secondDate, Calendar calendar) {
    int res = getDaysBetween(firstDate, secondDate, calendar);
    if (calendar.isWorkingDay(secondDate)) {
      res++;
    }
    return res;
  }

  /**
   * Get the number of working days (according to the supplied calendar) inclusive of the final date. <b>Note:<b> these {@link ZonedDateTime} dates are converted to {@link LocalDate}, so any
   * time-of-day and time zone information is lost
   * <p>
   * For example, the number of days between 8/8/2014 (Monday) and 12/8/2014 (Friday) a weekend only calendar is 5 (since Friday is a working day)
   * @param firstDate The first date
   * @param secondDate the second date
   * @param calendar Calendar defining what is a working day
   * @return The number of business (working) days between two dates, inclusive of the final date
   */
  public static int getWorkingDaysInclusive(final ZonedDateTime firstDate, final ZonedDateTime secondDate, final Calendar calendar) {
    ArgChecker.notNull(firstDate, "first date");
    ArgChecker.notNull(secondDate, "second date");
    return getWorkingDaysInclusive(firstDate.toLocalDate(), secondDate.toLocalDate(), calendar);
  }
}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.convention.daycount;

import java.time.ZonedDateTime;

import com.opengamma.strata.basics.date.HolidayCalendar;

/**
 * Utilities to work with {@code DayCount}.
 */
public final class DayCountUtils {

  /**
   * Restricted constructor.
   */
  private DayCountUtils() {
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the day count between the specified dates.
   * <p>
   * Given two dates, this method returns the fraction of a year between these dates
   * according to the convention.
   * 
   * @param dayCount  the day count to use
   * @param firstDate  the earlier date, not null
   * @param secondDate  the later date, not null
   * @return the day count fraction
   */
  public static double yearFraction(DayCount dayCount, ZonedDateTime firstDate, ZonedDateTime secondDate) {
    return dayCount.yearFraction(firstDate.toLocalDate(), secondDate.toLocalDate());
  }

  /**
   * Gets the day count between the specified dates using the supplied calendar to provide business days
   * <p>
   * Given two dates, this method returns the fraction of a year between these dates
   * according to the convention.
   *
   * @param dayCount  the day count to use
   * @param firstDate  the earlier date, not null
   * @param secondDate  the later date, not null
   * @param calendar  a calendar
   * @return the day count fraction
   */
  public static double yearFraction(
      DayCount dayCount, ZonedDateTime firstDate, ZonedDateTime secondDate, HolidayCalendar calendar) {
    return dayCount.yearFraction(firstDate.toLocalDate(), secondDate.toLocalDate());
  }

}

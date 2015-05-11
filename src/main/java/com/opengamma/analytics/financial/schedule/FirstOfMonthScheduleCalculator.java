/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.schedule;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

import com.opengamma.strata.collect.ArgChecker;

/**
 * 
 */
public class FirstOfMonthScheduleCalculator extends Schedule {

  @Override
  public LocalDate[] getSchedule(final LocalDate startDate, final LocalDate endDate, final boolean fromEnd, final boolean generateRecursive) {
    return getSchedule(startDate, endDate);
  }

  public LocalDate[] getSchedule(final LocalDate startDate, final LocalDate endDate) {
    ArgChecker.notNull(startDate, "start date");
    ArgChecker.notNull(endDate, "end date");
    ArgChecker.isFalse(startDate.isAfter(endDate), "start date must not be after end date");
    if (startDate.equals(endDate)) {
      if (startDate.getDayOfMonth() == 1) {
        return new LocalDate[] {startDate };
      }
      throw new IllegalArgumentException("Start date and end date were the same but neither was the first day of the month");
    }
    final List<LocalDate> dates = new ArrayList<>();
    LocalDate date = startDate.with(TemporalAdjusters.firstDayOfMonth());
    if (date.isBefore(startDate)) {
      date = date.plusMonths(1);
    }
    while (!date.isAfter(endDate)) {
      dates.add(date);
      date = date.plusMonths(1);
    }
    return dates.toArray(new LocalDate[dates.size()]);
  }

  @Override
  public ZonedDateTime[] getSchedule(final ZonedDateTime startDate, final ZonedDateTime endDate, final boolean fromEnd, final boolean generateRecursive) {
    return getSchedule(startDate, endDate);
  }

  public ZonedDateTime[] getSchedule(final ZonedDateTime startDate, final ZonedDateTime endDate) {
    ArgChecker.notNull(startDate, "start date");
    ArgChecker.notNull(endDate, "end date");
    ArgChecker.isFalse(startDate.isAfter(endDate), "start date must not be after end date");
    if (startDate.equals(endDate)) {
      if (startDate.getDayOfMonth() == 1) {
        return new ZonedDateTime[] {startDate };
      }
      throw new IllegalArgumentException("Start date and end date were the same but neither was the first day of the month");
    }
    final List<ZonedDateTime> dates = new ArrayList<>();
    ZonedDateTime date = startDate.with(TemporalAdjusters.firstDayOfMonth());
    if (date.isBefore(startDate)) {
      date = date.plusMonths(1);
    }
    while (!date.isAfter(endDate)) {
      dates.add(date);
      date = date.plusMonths(1);
    }
    return dates.toArray(EMPTY_ZONED_DATE_TIME_ARRAY);
  }
}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.schedule;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

import com.opengamma.strata.collect.ArgChecker;

/**
 *
 */
public class EndOfMonthScheduleCalculator extends Schedule {

  @Override
  public LocalDate[] getSchedule(final LocalDate startDate, final LocalDate endDate, final boolean fromEnd, final boolean generateRecursive) {
    return getSchedule(startDate, endDate);
  }

  public LocalDate[] getSchedule(final LocalDate startDate, final LocalDate endDate) {
    ArgChecker.notNull(startDate, "start date");
    ArgChecker.notNull(endDate, "end date");
    ArgChecker.isFalse(startDate.isAfter(endDate), "start date must not be after end date");
    if (startDate.equals(endDate)) {
      if (startDate.getDayOfMonth() == startDate.lengthOfMonth()) {
        return new LocalDate[] {startDate };
      }
      throw new IllegalArgumentException("Start date and end date were the same but neither was the last day of the month");
    }
    final List<LocalDate> dates = new ArrayList<>();
    LocalDate date = startDate.with(TemporalAdjusters.lastDayOfMonth());
    while (!date.isAfter(endDate)) {
      dates.add(date);
      date = date.plus(Period.ofMonths(1)).with(TemporalAdjusters.lastDayOfMonth());
    }
    return dates.toArray(EMPTY_LOCAL_DATE_ARRAY);
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
      if (startDate.getDayOfMonth() == startDate.toLocalDate().lengthOfMonth()) {
        return new ZonedDateTime[] {startDate };
      }
      throw new IllegalArgumentException("Start date and end date were the same but neither was the last day of the month");
    }
    final List<ZonedDateTime> dates = new ArrayList<>();
    ZonedDateTime date = startDate.with(TemporalAdjusters.lastDayOfMonth());
    while (!date.isAfter(endDate)) {
      dates.add(date);
      date = date.plus(Period.ofMonths(1)).with(TemporalAdjusters.lastDayOfMonth());
    }
    return dates.toArray(EMPTY_ZONED_DATE_TIME_ARRAY);
  }
}

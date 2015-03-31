/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.schedule;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.opengamma.analytics.convention.calendar.Calendar;
import com.opengamma.strata.collect.ArgChecker;

/**
 * 
 */
public final class HolidayDateRemovalFunction {
  private static final LocalDate[] EMPTY_ARRAY = new LocalDate[0];
  private static final HolidayDateRemovalFunction s_instance = new HolidayDateRemovalFunction();

  public static HolidayDateRemovalFunction getInstance() {
    return s_instance;
  }

  private HolidayDateRemovalFunction() {
  }

  public LocalDate[] getStrippedSchedule(final LocalDate[] dates, final Calendar holidays) {
    ArgChecker.notNull(dates, "date");
    ArgChecker.notNull(holidays, "holidays");
    final List<LocalDate> stripped = new ArrayList<>();
    for (final LocalDate date : dates) {
      if (holidays.isWorkingDay(date)) {
        stripped.add(date);
      }
    }
    return stripped.toArray(EMPTY_ARRAY);
  }
}

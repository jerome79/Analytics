/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.datasets;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.basics.date.ImmutableHolidayCalendar;

/**
 * HolidayCalendar with the dates of the ECB meeting decisions (settlement dates for the change of rate).
 */
public class CalendarECBSettlements implements HolidayCalendar {

  /**
   * The list of settlement dates (for 2013-2014).
   */
  private static final LocalDate SETTLE[] = new LocalDate[] {
    LocalDate.of(2013, 5, 8), LocalDate.of(2013, 10, 9), LocalDate.of(2013, 11, 13), LocalDate.of(2013, 12, 11),
    LocalDate.of(2014, 1, 15), LocalDate.of(2014, 2, 12), LocalDate.of(2014, 3, 12), LocalDate.of(2014, 4, 9),
    LocalDate.of(2014, 5, 14), LocalDate.of(2014, 6, 11),
    LocalDate.of(2014, 7, 9), LocalDate.of(2014, 8, 13), LocalDate.of(2014, 9, 10), LocalDate.of(2014, 10, 8),
    LocalDate.of(2014, 11, 12), LocalDate.of(2014, 12, 10) };

  private static final String NAME = "ECB decision settlement dates";

  private final ImmutableHolidayCalendar underlying;

  /**
   * Creates an instance.
   */
  public CalendarECBSettlements() {
    List<LocalDate> holidays = new ArrayList<>();
    for (int loopy = 0; loopy < SETTLE.length; loopy++) {
      holidays.add(SETTLE[loopy]);
    }
    underlying = ImmutableHolidayCalendar.of(NAME, holidays, ImmutableList.of());
  }

  @Override
  public boolean isHoliday(LocalDate date) {
    return underlying.isHoliday(date);
  }

  @Override
  public String getName() {
    return underlying.getName();
  }

}

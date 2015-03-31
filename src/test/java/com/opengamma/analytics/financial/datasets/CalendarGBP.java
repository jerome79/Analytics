/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.datasets;


import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.basics.date.ImmutableHolidayCalendar;

/**
 * 
 */
public class CalendarGBP implements HolidayCalendar {

  private final ImmutableHolidayCalendar underlying;

  /**
   * Creates an instance.
   * 
   * @param name  the calendar name
   */
  public CalendarGBP(String name) {
    List<LocalDate> holidays = new ArrayList<>();
    int startYear = 2000;
    int endYear = 2063;

    for (int loopy = startYear; loopy <= endYear; loopy++) {
      holidays.add(LocalDate.of(loopy, 1, 1));
      holidays.add(LocalDate.of(loopy, 12, 25));
      holidays.add(LocalDate.of(loopy, 12, 26));
    }
    final LocalDate easter[] = new LocalDate[] {LocalDate.of(2013, 3, 31), LocalDate.of(2014, 4, 20), LocalDate.of(2015, 4, 5),
      LocalDate.of(2016, 3, 27), LocalDate.of(2017, 4, 16), LocalDate.of(2018, 4, 1), LocalDate.of(2019, 4, 21), LocalDate.of(2020, 4, 12),
      LocalDate.of(2021, 4, 4), LocalDate.of(2022, 4, 17), LocalDate.of(2023, 4, 9), LocalDate.of(2024, 3, 31), LocalDate.of(2025, 4, 20),
      LocalDate.of(2026, 4, 5), LocalDate.of(2027, 3, 28), LocalDate.of(2028, 4, 16), LocalDate.of(2029, 4, 1), LocalDate.of(2030, 4, 21),
      LocalDate.of(2031, 4, 13), LocalDate.of(2032, 3, 28), LocalDate.of(2033, 4, 17), LocalDate.of(2034, 4, 9), LocalDate.of(2035, 3, 25),
      LocalDate.of(2036, 4, 13), LocalDate.of(2037, 4, 5), LocalDate.of(2038, 4, 25), LocalDate.of(2039, 4, 10), LocalDate.of(2040, 4, 1),
      LocalDate.of(2041, 4, 21), LocalDate.of(2042, 4, 6), LocalDate.of(2043, 3, 29), LocalDate.of(2044, 4, 17), LocalDate.of(2045, 4, 9),
      LocalDate.of(2046, 3, 25), LocalDate.of(2047, 4, 14), LocalDate.of(2048, 4, 5), LocalDate.of(2049, 4, 18) };
    for (final LocalDate element : easter) {
      holidays.add(element.minusDays(2)); // Easter Friday
      holidays.add(element.plusDays(1)); // Easter Monday
    }
    holidays.add(LocalDate.of(2014, 4, 18));
    holidays.add(LocalDate.of(2014, 4, 21));
    holidays.add(LocalDate.of(2014, 5, 5));
    holidays.add(LocalDate.of(2014, 5, 26));
    holidays.add(LocalDate.of(2014, 8, 25));
    holidays.add(LocalDate.of(2015, 4, 3));
    holidays.add(LocalDate.of(2015, 4, 6));
    holidays.add(LocalDate.of(2015, 5, 4));
    holidays.add(LocalDate.of(2015, 5, 25));
    holidays.add(LocalDate.of(2015, 8, 31));
    holidays.add(LocalDate.of(2015, 12, 28));
    holidays.add(LocalDate.of(2016, 3, 25));
    holidays.add(LocalDate.of(2016, 3, 28));
    holidays.add(LocalDate.of(2016, 5, 2));
    holidays.add(LocalDate.of(2016, 5, 30));
    holidays.add(LocalDate.of(2016, 8, 29));
    holidays.add(LocalDate.of(2016, 12, 27));
    holidays.add(LocalDate.of(2017, 1, 2));
    holidays.add(LocalDate.of(2017, 4, 14));
    holidays.add(LocalDate.of(2017, 4, 17));
    holidays.add(LocalDate.of(2017, 5, 1));
    holidays.add(LocalDate.of(2017, 5, 29));
    holidays.add(LocalDate.of(2017, 8, 28));
    holidays.add(LocalDate.of(2018, 3, 30));
    holidays.add(LocalDate.of(2018, 4, 2));
    holidays.add(LocalDate.of(2018, 5, 7));
    holidays.add(LocalDate.of(2018, 5, 28));
    holidays.add(LocalDate.of(2018, 8, 27));
    holidays.add(LocalDate.of(2019, 4, 19));
    holidays.add(LocalDate.of(2019, 4, 22));
    holidays.add(LocalDate.of(2019, 5, 6));
    holidays.add(LocalDate.of(2019, 5, 27));
    holidays.add(LocalDate.of(2019, 8, 26));
    underlying = ImmutableHolidayCalendar.of(name, holidays, SATURDAY, SUNDAY);
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

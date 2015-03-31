/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.convention.businessday;


import java.time.LocalDate;

import com.opengamma.strata.basics.date.HolidayCalendar;

/**
 * The preceding business day convention.
 * <p>
 * This chooses the latest working day preceding a non-working day.
 */
public class PrecedingBusinessDayConvention extends AbstractBusinessDayConvention {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  @Override
  public LocalDate adjustDate(final HolidayCalendar workingDays, final LocalDate date) {
    LocalDate result = date;
    while (!workingDays.isBusinessDay(result)) {
      result = result.minusDays(1);
    }
    return result;
  }

  @Override
  public String getName() {
    return "Preceding";
  }

}

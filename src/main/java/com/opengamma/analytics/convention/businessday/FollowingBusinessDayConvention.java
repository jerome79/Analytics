/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.convention.businessday;


import java.time.LocalDate;

import com.opengamma.strata.basics.date.HolidayCalendar;

/**
 * The following business day convention.
 * <p>
 * This chooses the next working day following a non-working day.
 */
public class FollowingBusinessDayConvention extends AbstractBusinessDayConvention {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  @Override
  public LocalDate adjustDate(final HolidayCalendar workingDays, final LocalDate date) {
    LocalDate result = date;
    while (!workingDays.isBusinessDay(result)) {
      result = result.plusDays(1);
    }
    return result;
  }

  @Override
  public String getName() {
    return "Following";
  }  

}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.convention.businessday;


import java.time.LocalDate;

import com.opengamma.strata.basics.date.HolidayCalendar;

/**
 * The no adjustment business day convention.
 * <p>
 * This implementation always returns the input date, performing no adjustments.
 */
public class NoAdjustBusinessDayConvention extends AbstractBusinessDayConvention {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  @Override
  public LocalDate adjustDate(final HolidayCalendar workingDayCalendar, final LocalDate date) {
    return date;
  }

  @Override
  public String getName() {
    return "None";
  }

}

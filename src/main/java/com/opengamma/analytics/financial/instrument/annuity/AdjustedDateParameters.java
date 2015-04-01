/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.annuity;

import com.opengamma.strata.basics.date.BusinessDayConvention;
import com.opengamma.strata.basics.date.HolidayCalendar;

/**
 * Parameters required to adjust dates.
 */
public class AdjustedDateParameters {

  /**
   * The calendar used to adjust dates.
   */
  private final HolidayCalendar _calendar;
  
  /**
   * The business day convention used to adjust dates.
   */
  private final BusinessDayConvention _businessDayConvention;
  
  public AdjustedDateParameters(
      HolidayCalendar calendar,
      BusinessDayConvention businessDayConvention) {
    _calendar = calendar;
    _businessDayConvention = businessDayConvention;
  }
  
  /**
   * Returns the calendar used to adjust dates.
   * @return the calendar used to adjust dates.
   */
  public HolidayCalendar getCalendar() {
    return _calendar;
  }
  
  /**
   * Returns the business day convention used to adjust dates.
   * @return the business day convention used to adjust dates.
   */
  public BusinessDayConvention getBusinessDayConvention() {
    return _businessDayConvention;
  }
  
  // TODO equals, hashcode
}

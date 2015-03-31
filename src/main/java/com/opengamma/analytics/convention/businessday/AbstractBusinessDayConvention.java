/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.convention.businessday;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZonedDateTime;

import com.opengamma.analytics.convention.calendar.Calendar;

/**
 * Abstract implementation of a convention for handling business days.
 */
public abstract class AbstractBusinessDayConvention implements BusinessDayConvention, Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  @Override
  public ZonedDateTime adjustDate(final Calendar workingDayCalendar, final ZonedDateTime dateTime) {
    LocalDate adjusted = adjustDate(workingDayCalendar, dateTime.toLocalDate());
    return adjusted.atTime(dateTime.toLocalTime()).atZone(dateTime.getZone());
  }

  @Override
  public String toString() {
    return "BusinessDayConvention [" + getName() + "]";
  }

}

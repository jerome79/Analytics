/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.convention.businessday;

import java.time.LocalDate;
import java.time.ZonedDateTime;

import org.joda.convert.FromStringFactory;
import org.joda.convert.ToString;

import com.opengamma.analytics.convention.NamedInstance;
import com.opengamma.analytics.convention.calendar.Calendar;

/**
 * Convention for handling business days.
 * <p>
 * This provides a mechanism to handle working and non-working days allowing
 * a date to be adjusted when it falls on a non-working day.
 */
@FromStringFactory(factory = BusinessDayConventionFactory.class)
public interface BusinessDayConvention extends NamedInstance {

  /**
   * Adjusts the specified date using the working day calendar.
   *
   * @param workingDayCalendar  the working days, not null
   * @param date  the date to adjust, not null
   * @return the adjusted date, not null
   */
  LocalDate adjustDate(Calendar workingDayCalendar, LocalDate date);

  /**
   * Adjusts the specified date-time using the working day calendar.
   *
   * @param workingDayCalendar  the working days, not null
   * @param dateTime  the date-time to adjust, not null
   * @return the adjusted date-time, not null
   */
  ZonedDateTime adjustDate(final Calendar workingDayCalendar, final ZonedDateTime dateTime);

  /**
   * Gets the name of the convention.
   *
   * @return the name, not null
   */
  @Override
  @ToString
  String getName();

}

/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index.generator;

import com.opengamma.analytics.convention.daycount.DayCounts;
import com.opengamma.analytics.financial.instrument.index.GeneratorDeposit;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.date.BusinessDayConventions;
import com.opengamma.strata.basics.date.HolidayCalendar;

/**
 * Deposit generator with the standard GBP conventions.
 */
public class GBPDeposit extends GeneratorDeposit {

  /**
   * Constructor.
   * @param calendar A GBP calendar.
   */
  public GBPDeposit(final HolidayCalendar calendar) {
    super("GBP Deposit", Currency.GBP, calendar, 0, DayCounts.ACT_365F, BusinessDayConventions.MODIFIED_FOLLOWING, true);
  }

}

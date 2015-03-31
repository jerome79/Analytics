/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index.generator;

import com.opengamma.analytics.convention.businessday.BusinessDayConventions;
import com.opengamma.analytics.convention.daycount.DayCounts;
import com.opengamma.analytics.financial.instrument.index.GeneratorDeposit;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.date.HolidayCalendar;

/**
 * Deposit generator with the standard USD conventions.
 */
public class USDDeposit extends GeneratorDeposit {

  /**
   * Constructor.
   * @param calendar A USD calendar.
   */
  public USDDeposit(final HolidayCalendar calendar) {
    super("USD Deposit", Currency.USD, calendar, 2, DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING, true);
  }

}

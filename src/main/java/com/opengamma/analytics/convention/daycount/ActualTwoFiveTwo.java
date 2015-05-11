/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.convention.daycount;

import java.time.LocalDate;

import com.opengamma.strata.basics.date.HolidayCalendar;

/**
 * The Business/252 day count. The number of good business days between two days is counted and then divided by 252.
 * @deprecated This day count is incorrectly named; it should be "Business/252"
 */
@Deprecated
public class ActualTwoFiveTwo extends StatelessDayCount {
  private static final long serialVersionUID = 1L;
  private static final DayCount DC = DayCounts.BUSINESS_252;

  @Override
  public double yearFraction(final LocalDate firstDate, final LocalDate secondDate) {
    return DC.yearFraction(firstDate, secondDate);
  }

  @Override
  public double yearFraction(final LocalDate firstDate, final LocalDate secondDate, final HolidayCalendar calendar) {
    return DC.yearFraction(firstDate, secondDate, calendar);
  }

  @Override
  public double getAccruedInterest(final LocalDate previousCouponDate, final LocalDate date, final LocalDate nextCouponDate, final double coupon,
      final double paymentsPerYear) {
    return DC.getAccruedInterest(previousCouponDate, date, nextCouponDate, coupon, paymentsPerYear);
  }

  @Override
  public String getName() {
    return "Actual/252";
  }

}

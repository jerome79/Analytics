/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.convention.daycount;

import static com.opengamma.analytics.convention.businessday.BusinessDayDateUtils.getDaysBetween;

import java.time.LocalDate;

import com.opengamma.strata.basics.date.HolidayCalendar;

/**
 * The Business/252 day count. The number of good business days between two days is counted and then divided by 252.
 */
public class BusinessTwoFiveTwo extends StatelessDayCount {

  private static final double TWO_FIVE_TWO = 252.0;
  private static final long serialVersionUID = 1L;

  @Override
  public double yearFraction(final LocalDate firstDate, final LocalDate secondDate) {
    throw new UnsupportedOperationException("Must supply a calendar to calculate the day-count fraction");
  }

  @Override
  public double yearFraction(final LocalDate firstDate, final LocalDate secondDate, final HolidayCalendar calendar) {
    // Arguments are checked in BusinessDays
    return getDaysBetween(firstDate, secondDate, calendar) / TWO_FIVE_TWO;
  }

  @Override
  public double getAccruedInterest(final LocalDate previousCouponDate, final LocalDate date, final LocalDate nextCouponDate, final double coupon, final double paymentsPerYear) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getName() {
    return "Business/252";
  }

}

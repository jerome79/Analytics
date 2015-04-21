/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.convention.daycount;


import java.time.LocalDate;
import java.time.temporal.JulianFields;

/**
 * The 'Actual/360' day count.
 */
public class ActualThreeSixty extends ActualTypeDayCount {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  @Override
  public double yearFraction(final LocalDate firstDate, final LocalDate secondDate) {
    testDates(firstDate, secondDate);
    final long firstJulianDate = firstDate.getLong(JulianFields.MODIFIED_JULIAN_DAY);
    final long secondJulianDate = secondDate.getLong(JulianFields.MODIFIED_JULIAN_DAY);
    return (secondJulianDate - firstJulianDate) / 360.;
  }

  @Override
  public double getAccruedInterest(final LocalDate previousCouponDate, final LocalDate date, final LocalDate nextCouponDate, final double coupon, final double paymentsPerYear) {
    return yearFraction(previousCouponDate, date) * coupon;
  }

  @Override
  public String getName() {
    return "Actual/360";
  }

}

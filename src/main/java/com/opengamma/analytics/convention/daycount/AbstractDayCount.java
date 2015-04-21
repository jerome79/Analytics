/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.convention.daycount;

import java.time.LocalDate;
import java.time.ZonedDateTime;

import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Convention for calculating the day count.
 */
public abstract class AbstractDayCount implements DayCount {

  /**
   * Gets the day count between the specified dates.
   * <p>
   * Given two dates, this method returns the fraction of a year between these dates
   * according to the convention.
   *
   * @param firstDate  the earlier date, not null
   * @param secondDate  the later date, not null
   * @return the day count fraction
   */
  @Override
  public abstract double yearFraction(final LocalDate firstDate, final LocalDate secondDate);

  /**
   * Gets the day count between the specified dates using the supplied calendar to provide business days
   * <p>
   * Given two dates, this method returns the fraction of a year between these dates
   * according to the convention.
   *
   * @param firstDate  the earlier date, not null
   * @param secondDate  the later date, not null
   * @param calendar  a calendar
   * @return the day count fraction
   */
  @Override
  public double yearFraction(final LocalDate firstDate, final LocalDate secondDate, final HolidayCalendar calendar) {
    return yearFraction(firstDate, secondDate);
  }

  /**
   * Calculates the accrued interest for the coupon according to the convention.
   *
   * @param previousCouponDate  the previous coupon date, not null
   * @param date  the evaluated coupon date, not null
   * @param nextCouponDate  the next coupon date, not null
   * @param coupon  the coupon value
   * @param paymentsPerYear  the number of payments per year, one, two, three, four, six or twelve
   * @return the accrued interest
   */
  @Override
  public abstract double getAccruedInterest(final LocalDate previousCouponDate, final LocalDate date, final LocalDate nextCouponDate, final double coupon, final double paymentsPerYear);

  /**
   * Calculates the accrued interest for the coupon according to the convention.
   *
   * @param previousCouponDate  the previous coupon date, not null
   * @param date  the evaluated coupon date, not null
   * @param nextCouponDate  the next coupon date, not null
   * @param coupon  the coupon value
   * @param paymentsPerYear  the number of payments per year, one, two, three, four, six or twelve
   * @return the accrued interest
   */
  @Override
  public double getAccruedInterest(final ZonedDateTime previousCouponDate, final ZonedDateTime date, final ZonedDateTime nextCouponDate, final double coupon, final double paymentsPerYear) {
    ArgChecker.notNull(previousCouponDate, "previous coupon date");
    ArgChecker.notNull(date, "date");
    ArgChecker.notNull(nextCouponDate, "next coupon date");
    return getAccruedInterest(previousCouponDate.toLocalDate(), date.toLocalDate(), nextCouponDate.toLocalDate(), coupon, paymentsPerYear);
  }

  /**
   * Gets the name of the convention.
   *
   * @return the name, not null
   */
  @Override
  public abstract String getName();

}

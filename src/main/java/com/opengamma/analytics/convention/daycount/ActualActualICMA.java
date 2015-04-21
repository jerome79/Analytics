/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.convention.daycount;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.JulianFields;
import java.time.temporal.TemporalAdjusters;

import com.opengamma.strata.basics.schedule.StubConvention;
import com.opengamma.strata.collect.ArgChecker;

/**
 * The 'Actual/Actual ICMA' day count.
 */
public class ActualActualICMA extends ActualTypeDayCount {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  @Override
  public double yearFraction(final LocalDate firstDate, final LocalDate secondDate) {
    throw new UnsupportedOperationException("Cannot get daycount fraction; need information about the coupon and payment frequency");
  }

  @Override
  public double getAccruedInterest(final LocalDate previousCouponDate, final LocalDate date, final LocalDate nextCouponDate, final double coupon, final double paymentsPerYear) {
    return getAccruedInterest(previousCouponDate, date, nextCouponDate, coupon, paymentsPerYear, StubConvention.NONE);
  }

  /**
   * Computes the accrued interest for a specific stub-type.
   * @param previousCouponDate  the previous coupon date, not null
   * @param date  the evaluated coupon date, not null
   * @param nextCouponDate  the next coupon date, not null
   * @param coupon  the coupon value
   * @param paymentsPerYear  the number of payments per year, one, two, three, four, six or twelve
   * @param stubType The stub type.
   * @return The accrued interest.
   */
  public double getAccruedInterest(final ZonedDateTime previousCouponDate, final ZonedDateTime date, final ZonedDateTime nextCouponDate, final double coupon, final double paymentsPerYear,
      final StubConvention stubType) {
    return getAccruedInterest(previousCouponDate.toLocalDate(), date.toLocalDate(), nextCouponDate.toLocalDate(), coupon, paymentsPerYear, stubType);
  }

  public double getAccruedInterest(final LocalDate previousCouponDate, final LocalDate date, final LocalDate nextCouponDate, final double coupon, final double paymentsPerYear,
      final StubConvention stubType) {
    testDates(previousCouponDate, date, nextCouponDate);
    ArgChecker.notNull(stubType, "stub type");

    long daysBetween, daysBetweenCoupons;
    final long previousCouponDateJulian = previousCouponDate.getLong(JulianFields.MODIFIED_JULIAN_DAY);
    final long nextCouponDateJulian = nextCouponDate.getLong(JulianFields.MODIFIED_JULIAN_DAY);
    final long dateJulian = date.getLong(JulianFields.MODIFIED_JULIAN_DAY);
    final int months = (int) (12 / paymentsPerYear);
    switch (stubType) {
      case NONE: {
        daysBetween = dateJulian - previousCouponDateJulian;
        daysBetweenCoupons = nextCouponDate.getLong(JulianFields.MODIFIED_JULIAN_DAY) - previousCouponDateJulian;
        return coupon * daysBetween / daysBetweenCoupons / paymentsPerYear;
      }
      case SHORT_INITIAL: {
        final LocalDate notionalStart = getEOMAdjustedDate(nextCouponDate, nextCouponDate.minusMonths(months));
        daysBetweenCoupons = nextCouponDateJulian - notionalStart.getLong(JulianFields.MODIFIED_JULIAN_DAY);
        daysBetween = dateJulian - previousCouponDateJulian;
        return coupon * daysBetween / daysBetweenCoupons / paymentsPerYear;
      }
      case LONG_INITIAL: {
        final long firstNotionalJulian = getEOMAdjustedDate(nextCouponDate, nextCouponDate.minusMonths(months * 2)).getLong(JulianFields.MODIFIED_JULIAN_DAY);
        final long secondNotionalJulian = getEOMAdjustedDate(nextCouponDate, nextCouponDate.minusMonths(months)).getLong(JulianFields.MODIFIED_JULIAN_DAY);
        final double daysBetweenTwoNotionalCoupons1 = secondNotionalJulian - firstNotionalJulian;
        if (dateJulian > secondNotionalJulian) {
          daysBetween = dateJulian - secondNotionalJulian;
          final long daysBetweenStub = secondNotionalJulian - previousCouponDateJulian;
          final double daysBetweenTwoNotionalCoupons2 = nextCouponDateJulian - secondNotionalJulian;
          return coupon * (daysBetweenStub / daysBetweenTwoNotionalCoupons1 + daysBetween / daysBetweenTwoNotionalCoupons2) / paymentsPerYear;
        }
        daysBetween = dateJulian - previousCouponDateJulian;
        return coupon * (daysBetween / daysBetweenTwoNotionalCoupons1) / paymentsPerYear;
      }
      case SHORT_FINAL: {
        final LocalDate notionalEnd = getEOMAdjustedDate(previousCouponDate, previousCouponDate.plusMonths(months));
        daysBetweenCoupons = notionalEnd.getLong(JulianFields.MODIFIED_JULIAN_DAY) - previousCouponDateJulian;
        daysBetween = dateJulian - previousCouponDateJulian;
        return coupon * daysBetween / daysBetweenCoupons / paymentsPerYear;
      }
      case LONG_FINAL: {
        final long firstNotionalJulian = getEOMAdjustedDate(previousCouponDate, previousCouponDate.plusMonths(months)).getLong(JulianFields.MODIFIED_JULIAN_DAY);
        final long secondNotionalJulian = getEOMAdjustedDate(previousCouponDate, previousCouponDate.plusMonths(2 * months)).getLong(JulianFields.MODIFIED_JULIAN_DAY);
        final long daysBetweenPreviousAndFirstNotional = firstNotionalJulian - previousCouponDateJulian;
        if (dateJulian < firstNotionalJulian) {
          daysBetween = dateJulian - previousCouponDateJulian;
          return coupon * daysBetween / daysBetweenPreviousAndFirstNotional / paymentsPerYear;
        }
        final long daysBetweenStub = dateJulian - firstNotionalJulian;
        final double daysBetweenTwoNotionalCoupons = secondNotionalJulian - firstNotionalJulian;
        return coupon * (1 + daysBetweenStub / daysBetweenTwoNotionalCoupons) / paymentsPerYear;
      }
      default:
        throw new IllegalArgumentException("Cannot handle stub type " + stubType);
    }
  }

  @Override
  public String getName() {
    return "Actual/Actual ICMA";
  }

  // -------------------------------------------------------------------------
  /**
   * Adjusts the date to the last day of month if necessary.
   * 
   * @param comparison  the date to check as to being the last day of month, not null
   * @param date  the date to adjust, not null
   * @return the adjusted date, not null
   */
  private static LocalDate getEOMAdjustedDate(final LocalDate comparison, final LocalDate date) {
    if (comparison.getDayOfMonth() == comparison.lengthOfMonth()) {
      return date.with(TemporalAdjusters.lastDayOfMonth());
    }
    return date;
  }

}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.convention.daycount;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.JulianFields;

import com.opengamma.strata.basics.schedule.StubConvention;
import com.opengamma.strata.collect.ArgChecker;

/**
 * The 'Actual/Actual ICMA Normal' day count.
 */
public class ActualActualICMANormal extends ActualTypeDayCount {

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
        daysBetweenCoupons = nextCouponDateJulian - previousCouponDateJulian;
        return coupon * daysBetween / daysBetweenCoupons / paymentsPerYear;
      }
      case SHORT_INITIAL: {
        final LocalDate notionalStart = nextCouponDate.minusMonths(months);
        daysBetweenCoupons = nextCouponDateJulian - notionalStart.getLong(JulianFields.MODIFIED_JULIAN_DAY);
        daysBetween = dateJulian - previousCouponDateJulian;
        return coupon * daysBetween / daysBetweenCoupons / paymentsPerYear;
      }
      case LONG_INITIAL: {
        final long firstNotionalJulian = nextCouponDate.minusMonths(months * 2).getLong(JulianFields.MODIFIED_JULIAN_DAY);
        final long secondNotionalJulian = nextCouponDate.minusMonths(months).getLong(JulianFields.MODIFIED_JULIAN_DAY);
        final long daysBetweenStub = secondNotionalJulian - previousCouponDateJulian;
        final double daysBetweenTwoNotionalCoupons = secondNotionalJulian - firstNotionalJulian;
        if (dateJulian > secondNotionalJulian) {
          daysBetween = dateJulian - secondNotionalJulian;
          return coupon * (daysBetweenStub / daysBetweenTwoNotionalCoupons + 1) / paymentsPerYear;
        }
        daysBetween = dateJulian - firstNotionalJulian;
        return coupon * (daysBetween / daysBetweenTwoNotionalCoupons) / paymentsPerYear;
      }
      case SHORT_FINAL: {
        final LocalDate notionalEnd = previousCouponDate.plusMonths(months);
        daysBetweenCoupons = notionalEnd.getLong(JulianFields.MODIFIED_JULIAN_DAY) - previousCouponDateJulian;
        daysBetween = dateJulian - previousCouponDateJulian;
        return coupon * daysBetween / daysBetweenCoupons / paymentsPerYear;
      }
      case LONG_FINAL: {
        final long firstNotionalJulian = previousCouponDate.plusMonths(months).getLong(JulianFields.MODIFIED_JULIAN_DAY);
        final long secondNotionalJulian = previousCouponDate.plusMonths(2 * months).getLong(JulianFields.MODIFIED_JULIAN_DAY);
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
    return "Actual/Actual ICMA Normal";
  }

}

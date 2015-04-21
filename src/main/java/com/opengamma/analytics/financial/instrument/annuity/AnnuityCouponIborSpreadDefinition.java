/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.annuity;

import java.time.Period;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.opengamma.analytics.convention.daycount.DayCount;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.payment.CouponIborSpreadDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.timeseries.DoubleTimeSeries;
import com.opengamma.strata.basics.date.BusinessDayConvention;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.basics.schedule.StubConvention;
import com.opengamma.strata.collect.ArgChecker;

/**
 * A wrapper class for an AnnuityDefinition containing CouponIborSpreadDefinition.
 */
public class AnnuityCouponIborSpreadDefinition extends AnnuityCouponDefinition<CouponIborSpreadDefinition> {

  /**
   * The underlying Ibor index.
   */
  private final IborIndex _iborIndex;
  /**
   * The holiday calendar for the ibor index
   * */
  private final HolidayCalendar _calendar;

  /**
   * Constructor from a list of Ibor-like coupons.
   * @param payments The Ibor coupons.
   * @param calendar The calendar
   */
  public AnnuityCouponIborSpreadDefinition(final CouponIborSpreadDefinition[] payments, final HolidayCalendar calendar) {
    super(payments, calendar);
    _iborIndex = payments[0].getIndex();
    _calendar = payments[0].getCalendar();
  }

  /**
   * Annuity builder from the conventions and common characteristics.
   * @param settlementDate The settlement date.
   * @param tenor The tenor.
   * @param notional The notional.
   * @param index The Ibor index.
   * @param spread The common spread.
   * @param isPayer The payer flag.
   * @param calendar The holiday calendar for the ibor leg.
   * @return The Ibor annuity.
   */
  public static AnnuityCouponIborSpreadDefinition from(final ZonedDateTime settlementDate, final Period tenor, final double notional, final IborIndex index, final double spread,
      final boolean isPayer, final HolidayCalendar calendar) {
    ArgChecker.notNull(settlementDate, "settlement date");
    ArgChecker.notNull(index, "index");
    ArgChecker.notNull(tenor, "tenor");
    final AnnuityCouponIborDefinition iborAnnuity = AnnuityCouponIborDefinition.from(settlementDate, tenor, notional, index, isPayer, calendar);
    final CouponIborSpreadDefinition[] coupons = new CouponIborSpreadDefinition[iborAnnuity.getPayments().length];
    for (int loopcpn = 0; loopcpn < iborAnnuity.getPayments().length; loopcpn++) {
      coupons[loopcpn] = CouponIborSpreadDefinition.from(iborAnnuity.getNthPayment(loopcpn), spread);
    }
    return new AnnuityCouponIborSpreadDefinition(coupons, calendar);
  }

  /**
   * Annuity builder from the conventions and common characteristics.
   * @param settlementDate The settlement date.
   * @param maturityDate The annuity maturity date.
   * @param paymentPeriod The payment period.
   * @param notional The notional.
   * @param index The Ibor index.
   * @param isPayer The payer flag.
   * @param businessDayConvention The leg business day convention.
   * @param endOfMonth The leg end-of-month convention.
   * @param dayCount The coupons day count.
   * @param spread The spread rate.
   * @param calendar The holiday calendar for the ibor leg.
   * @return The Ibor annuity.
   */
  public static AnnuityCouponIborSpreadDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final Period paymentPeriod, final double notional, final IborIndex index,
      final boolean isPayer, final BusinessDayConvention businessDayConvention, final boolean endOfMonth, final DayCount dayCount, final double spread, final HolidayCalendar calendar) {
    ArgChecker.notNull(settlementDate, "settlement date");
    ArgChecker.notNull(maturityDate, "maturity date");
    ArgChecker.notNull(paymentPeriod, "payment period");
    ArgChecker.notNull(index, "index");
    ArgChecker.notNull(businessDayConvention, "Business day convention");
    ArgChecker.notNull(dayCount, "Day count convention");
    ArgChecker.isTrue(notional > 0, "notional <= 0");
    final double sign = isPayer ? -1.0 : 1.0;
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, maturityDate, paymentPeriod, true, false, businessDayConvention, calendar, endOfMonth);
    final CouponIborSpreadDefinition[] coupons = new CouponIborSpreadDefinition[paymentDates.length];
    ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(settlementDate, -index.getSpotLag(), calendar);
    coupons[0] = new CouponIborSpreadDefinition(index.getCurrency(), paymentDates[0], settlementDate, paymentDates[0],
        dayCount.yearFraction(settlementDate, paymentDates[0], calendar), sign * notional, fixingDate, index, spread, calendar);
    for (int loopcpn = 1; loopcpn < paymentDates.length; loopcpn++) {
      fixingDate = ScheduleCalculator.getAdjustedDate(paymentDates[loopcpn - 1], -index.getSpotLag(), calendar);
      coupons[loopcpn] = new CouponIborSpreadDefinition(index.getCurrency(), paymentDates[loopcpn], paymentDates[loopcpn - 1], paymentDates[loopcpn],
          dayCount.yearFraction(paymentDates[loopcpn - 1], paymentDates[loopcpn], calendar), sign * notional, fixingDate, index, spread, calendar);
    }
    return new AnnuityCouponIborSpreadDefinition(coupons, calendar);
  }

  /**
   * Annuity builder from the conventions and common characteristics.
   * @param settlementDate The settlement date.
   * @param maturityDate The annuity maturity date.
   * @param paymentPeriod The payment period.
   * @param notional The notional.
   * @param spread The spread rate.
   * @param index The Ibor index.
   * @param isPayer The payer flag.
   * @param businessDayConvention The leg business day convention.
   * @param endOfMonth The leg end-of-month convention.
   * @param dayCount The coupons day count.
   * @param calendar The holiday calendar for the ibor leg.
   * @param stub The stub type.
   * @return The Ibor annuity.
   */
  public static AnnuityCouponIborSpreadDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final Period paymentPeriod, final double notional, final double spread,
      final IborIndex index, final boolean isPayer, final BusinessDayConvention businessDayConvention, final boolean endOfMonth, final DayCount dayCount, final HolidayCalendar calendar,
      final StubConvention stub) {
    ArgChecker.notNull(settlementDate, "settlement date");
    ArgChecker.notNull(maturityDate, "maturity date");
    ArgChecker.notNull(paymentPeriod, "payment period");
    ArgChecker.notNull(index, "index");
    ArgChecker.notNull(businessDayConvention, "Business day convention");
    ArgChecker.notNull(dayCount, "Day count convention");
    ArgChecker.isTrue(notional > 0, "notional <= 0");
    final boolean isStubShort = stub.equals(StubConvention.SHORT_FINAL) || stub.equals(StubConvention.SHORT_INITIAL);
    final boolean isStubStart = stub.equals(StubConvention.LONG_INITIAL) || stub.equals(StubConvention.SHORT_INITIAL); // Implementation note: dates computed from the end.
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, maturityDate, paymentPeriod, isStubShort,
        isStubStart, businessDayConvention, calendar, endOfMonth);
    final double sign = isPayer ? -1.0 : 1.0;
    final CouponIborSpreadDefinition[] coupons = new CouponIborSpreadDefinition[paymentDates.length];
    ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(settlementDate, -index.getSpotLag(), calendar);
    coupons[0] = new CouponIborSpreadDefinition(index.getCurrency(), paymentDates[0], settlementDate, paymentDates[0],
        dayCount.yearFraction(settlementDate, paymentDates[0], calendar), sign * notional, fixingDate, index, spread, calendar);
    for (int loopcpn = 1; loopcpn < paymentDates.length; loopcpn++) {
      fixingDate = ScheduleCalculator.getAdjustedDate(paymentDates[loopcpn - 1], -index.getSpotLag(), calendar);
      coupons[loopcpn] = new CouponIborSpreadDefinition(index.getCurrency(), paymentDates[loopcpn], paymentDates[loopcpn - 1], paymentDates[loopcpn],
          dayCount.yearFraction(paymentDates[loopcpn - 1], paymentDates[loopcpn], calendar), sign * notional, fixingDate, index, spread, calendar);
    }
    return new AnnuityCouponIborSpreadDefinition(coupons, calendar);
  }

  /**
   * Annuity builder from the conventions and common characteristics.
   * @param settlementDate The settlement date.
   * @param maturityDate The annuity maturity date.
   * @param notional The notional.
   * @param index The Ibor index.
   * @param isPayer The payer flag.
   * @param spread The common spread.
   * @param calendar The holiday calendar for the ibor leg.
   * @return The Ibor annuity.
   */
  public static AnnuityCouponIborSpreadDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final double notional, final IborIndex index, final double spread,
      final boolean isPayer, final HolidayCalendar calendar) {
    ArgChecker.notNull(settlementDate, "settlement date");
    ArgChecker.notNull(index, "index");
    ArgChecker.notNull(maturityDate, "maturity date");
    final AnnuityCouponIborDefinition iborAnnuity = AnnuityCouponIborDefinition.from(settlementDate, maturityDate, notional, index, isPayer, calendar);
    final CouponIborSpreadDefinition[] coupons = new CouponIborSpreadDefinition[iborAnnuity.getPayments().length];
    for (int loopcpn = 0; loopcpn < iborAnnuity.getPayments().length; loopcpn++) {
      coupons[loopcpn] = CouponIborSpreadDefinition.from(iborAnnuity.getNthPayment(loopcpn), spread);
    }
    return new AnnuityCouponIborSpreadDefinition(coupons, calendar);
  }

  /**
   * Creates an annuity with zero spread from an {@link AnnuityCouponIborDefinition}
   * @param iborAnnuity The annuity, not null
   * @return An ibor annuity with spread
   */
  public static AnnuityCouponIborSpreadDefinition from(final AnnuityCouponIborDefinition iborAnnuity) {
    ArgChecker.notNull(iborAnnuity, "ibor annuity");
    final CouponIborSpreadDefinition[] coupons = new CouponIborSpreadDefinition[iborAnnuity.getPayments().length];
    for (int loopcpn = 0; loopcpn < iborAnnuity.getPayments().length; loopcpn++) {
      coupons[loopcpn] = CouponIborSpreadDefinition.from(iborAnnuity.getNthPayment(loopcpn), 0);
    }
    return new AnnuityCouponIborSpreadDefinition(coupons, iborAnnuity.getCalendar());
  }

  /**
   * Returns the underlying ibor index
   * @return The underlying ibor index
   */
  public IborIndex getIborIndex() {
    return _iborIndex;
  }

  /**
   * Gets the holiday calendar for the ibor index.
   * @return The calendar
   */
  public HolidayCalendar getIborCalendar() {
    return _calendar;
  }

  @Override
  public Annuity<Coupon> toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> indexFixingTS) {
    ArgChecker.notNull(date, "date");
    final List<Coupon> resultList = new ArrayList<>();
    final CouponIborSpreadDefinition[] payments = getPayments();
    for (int loopcoupon = 0; loopcoupon < payments.length; loopcoupon++) {
      if (!date.isAfter(payments[loopcoupon].getPaymentDate())) {
        resultList.add(payments[loopcoupon].toDerivative(date, indexFixingTS));
      }
    }
    return new Annuity<>(resultList.toArray(new Coupon[resultList.size()]));
  }

  @Override
  public Annuity<Coupon> toDerivative(final ZonedDateTime date) {
    ArgChecker.notNull(date, "date");
    final List<Coupon> resultList = new ArrayList<>();
    for (int loopcoupon = 0; loopcoupon < getPayments().length; loopcoupon++) {
      if (!date.isAfter(getPayments()[loopcoupon].getPaymentDate())) {
        resultList.add(getPayments()[loopcoupon].toDerivative(date));
      }
    }
    return new Annuity<>(resultList.toArray(new Coupon[resultList.size()]));
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _iborIndex.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final AnnuityCouponIborSpreadDefinition other = (AnnuityCouponIborSpreadDefinition) obj;
    if (!Objects.equals(_iborIndex, other._iborIndex)) {
      return false;
    }
    return true;
  }
}

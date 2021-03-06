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
import com.opengamma.analytics.convention.daycount.DayCountUtils;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.timeseries.DoubleTimeSeries;
import com.opengamma.strata.basics.date.BusinessDayConvention;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.basics.schedule.StubConvention;
import com.opengamma.strata.collect.ArgChecker;

/**
 * A wrapper class for a AnnuityDefinition containing CouponIborDefinition.
 */
public class AnnuityCouponIborDefinition extends AnnuityCouponDefinition<CouponIborDefinition> {
  /**
   * The ibor index
   * */
  private final IborIndex _iborIndex;
  /**
   * The holiday calendar for the ibor index
   * */
  private final HolidayCalendar _calendar;

  /**
   * Constructor from a list of Ibor-like coupons.
   * @param payments The Ibor coupons.
   * @param iborIndex The underlying ibor index
   * @param calendar The holiday calendar for the ibor index.
   */
  public AnnuityCouponIborDefinition(final CouponIborDefinition[] payments, final IborIndex iborIndex, final HolidayCalendar calendar) {
    super(payments, calendar);
    ArgChecker.notNull(iborIndex, "ibor index");
    ArgChecker.notNull(calendar, "calendar");
    _iborIndex = iborIndex;
    _calendar = calendar;
  }

  /**
   * Annuity builder from the conventions and common characteristics.
   * @param settlementDate The settlement date.
   * @param tenor The tenor.
   * @param notional The notional.
   * @param index The Ibor index.
   * @param isPayer The payer flag.
   * @param calendar The holiday calendar for the ibor leg.
   * @return The Ibor annuity.
   */
  public static AnnuityCouponIborDefinition from(final ZonedDateTime settlementDate, final Period tenor, final double notional, final IborIndex index, final boolean isPayer,
      final HolidayCalendar calendar) {
    ArgChecker.notNull(settlementDate, "settlement date");
    ArgChecker.notNull(index, "index");
    ArgChecker.notNull(tenor, "tenor");
    ArgChecker.isTrue(notional > 0, "notional <= 0");
    final ZonedDateTime maturityDate = settlementDate.plus(tenor); // Maturity is unadjusted.
    return from(settlementDate, maturityDate, notional, index, isPayer, calendar);
  }

  /**
   * Annuity builder from the conventions and common characteristics. The business day convention and the EOM rule are the one of the index.
   * @param settlementDate The settlement date.
   * @param maturityDate The annuity maturity date.
   * @param notional The notional.
   * @param index The Ibor index.
   * @param isPayer The payer flag.
   * @param calendar The holiday calendar for the ibor leg.
   * @return The Ibor annuity.
   */
  public static AnnuityCouponIborDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final double notional, final IborIndex index, final boolean isPayer,
      final HolidayCalendar calendar) {
    ArgChecker.notNull(index, "index");
    return from(settlementDate, maturityDate, notional, index, isPayer, index.getBusinessDayConvention(), index.isEndOfMonth(), calendar);
  }

  /**
   * Annuity builder from the conventions and common characteristics. The payment period and the day-count convention are the one of the Ibor index.
   * @param settlementDate The settlement date.
   * @param maturityDate The annuity maturity date.
   * @param notional The notional.
   * @param index The Ibor index.
   * @param isPayer The payer flag.
   * @param businessDayConvention The leg business day convention.
   * @param endOfMonth The leg end-of-month convention.
   * @param calendar The holiday calendar for the ibor leg.
   * @return The Ibor annuity.
   */
  public static AnnuityCouponIborDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final double notional, final IborIndex index, final boolean isPayer,
      final BusinessDayConvention businessDayConvention, final boolean endOfMonth, final HolidayCalendar calendar) {
    ArgChecker.notNull(index, "index");
    return from(settlementDate, maturityDate, index.getTenor(), notional, index, isPayer, businessDayConvention, endOfMonth, index.getDayCount(), calendar);
  }

  /**
   * Annuity builder from the conventions and common characteristics. The stub convention is short at the start.
   * @param settlementDate The settlement date.
   * @param maturityDate The annuity maturity date.
   * @param paymentPeriod The payment period.
   * @param notional The notional.
   * @param index The Ibor index.
   * @param isPayer The payer flag.
   * @param businessDayConvention The leg business day convention.
   * @param endOfMonth The leg end-of-month convention.
   * @param dayCount The coupons day count.
   * @param calendar The holiday calendar for the ibor leg.
   * @return The Ibor annuity.
   */
  public static AnnuityCouponIborDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final Period paymentPeriod, final double notional, final IborIndex index,
      final boolean isPayer, final BusinessDayConvention businessDayConvention, final boolean endOfMonth, final DayCount dayCount, final HolidayCalendar calendar) {
    return from(settlementDate, maturityDate, paymentPeriod, notional, index, isPayer, businessDayConvention, endOfMonth, dayCount, calendar, StubConvention.SHORT_INITIAL);
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
   * @param calendar The holiday calendar for the ibor leg.
   * @param stub The stub type.
   * @return The Ibor annuity.
   */
  public static AnnuityCouponIborDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final Period paymentPeriod, final double notional, final IborIndex index,
      final boolean isPayer, final BusinessDayConvention businessDayConvention, final boolean endOfMonth, final DayCount dayCount, final HolidayCalendar calendar, final StubConvention stub) {
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
    final CouponIborDefinition[] coupons = new CouponIborDefinition[paymentDates.length];
    ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(settlementDate, -index.getSpotLag(), calendar);
    coupons[0] = new CouponIborDefinition(index.getCurrency(), paymentDates[0], settlementDate, paymentDates[0],
        DayCountUtils.yearFraction(dayCount, settlementDate, paymentDates[0], calendar), sign * notional, fixingDate, index, calendar);
    for (int loopcpn = 1; loopcpn < paymentDates.length; loopcpn++) {
      fixingDate = ScheduleCalculator.getAdjustedDate(paymentDates[loopcpn - 1], -index.getSpotLag(), calendar);
      coupons[loopcpn] = new CouponIborDefinition(index.getCurrency(), paymentDates[loopcpn], paymentDates[loopcpn - 1], paymentDates[loopcpn],
          DayCountUtils.yearFraction(dayCount, paymentDates[loopcpn - 1], paymentDates[loopcpn], calendar), sign * notional, fixingDate, index, calendar);
    }
    return new AnnuityCouponIborDefinition(coupons, index, calendar);
  }

  /**
   * Annuity builder from the conventions and common characteristics. The accrual dates are unadjusted. Often used for bonds.
   * @param settlementDate The settlement date.
   * @param maturityDate The annuity maturity date.
   * @param notional The notional.
   * @param index The Ibor index.
   * @param isPayer The payer flag.
   * @param calendar The holiday calendar for the ibor leg.
   * @return The Ibor annuity.
   */
  public static AnnuityCouponIborDefinition fromAccrualUnadjusted(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final double notional, final IborIndex index,
      final boolean isPayer, final HolidayCalendar calendar) {
    ArgChecker.notNull(settlementDate, "settlement date");
    ArgChecker.notNull(maturityDate, "maturity date");
    ArgChecker.notNull(index, "index");
    ArgChecker.isTrue(notional > 0, "notional <= 0");
    final ZonedDateTime[] paymentDatesUnadjusted = ScheduleCalculator.getUnadjustedDateSchedule(settlementDate, maturityDate, index.getTenor(), true, false);
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(paymentDatesUnadjusted, index.getBusinessDayConvention(), calendar, false);
    final double sign = isPayer ? -1.0 : 1.0;
    final CouponIborDefinition[] coupons = new CouponIborDefinition[paymentDates.length];
    //First coupon uses settlement date
    CouponFixedDefinition coupon = new CouponFixedDefinition(index.getCurrency(), paymentDates[0], settlementDate, paymentDatesUnadjusted[0],
        DayCountUtils.yearFraction(index.getDayCount(), settlementDate, paymentDatesUnadjusted[0], calendar), sign * notional, 0.0);
    ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(settlementDate, -index.getSpotLag(), calendar);
    coupons[0] = CouponIborDefinition.from(coupon, fixingDate, index, calendar);
    for (int loopcpn = 1; loopcpn < paymentDates.length; loopcpn++) {
      coupon = new CouponFixedDefinition(index.getCurrency(), paymentDates[loopcpn], paymentDatesUnadjusted[loopcpn - 1], paymentDatesUnadjusted[loopcpn],
          DayCountUtils.yearFraction(index.getDayCount(), paymentDatesUnadjusted[loopcpn - 1], paymentDatesUnadjusted[loopcpn], calendar), sign * notional, 0.0);
      fixingDate = ScheduleCalculator.getAdjustedDate(paymentDatesUnadjusted[loopcpn - 1], -index.getSpotLag(), calendar);
      coupons[loopcpn] = CouponIborDefinition.from(coupon, fixingDate, index, calendar);
    }
    return new AnnuityCouponIborDefinition(coupons, index, calendar);
  }

  /**
   * Builder from an Ibor annuity with spread. Ignores the spread.
   * @param annuity The Ibor annuity with spread.
   * @return The annuity.
   */
  public static AnnuityCouponIborDefinition from(final AnnuityCouponIborSpreadDefinition annuity) {
    ArgChecker.notNull(annuity, "annuity");
    final CouponIborDefinition[] coupons = new CouponIborDefinition[annuity.getPayments().length];
    for (int loopcpn = 0; loopcpn < annuity.getPayments().length; loopcpn++) {
      coupons[loopcpn] = CouponIborDefinition.from(annuity.getNthPayment(loopcpn));
    }
    return new AnnuityCouponIborDefinition(coupons, annuity.getIborIndex(), annuity.getIborCalendar());
  }

  /**
   * Creates a new annuity containing the coupons with start accrual date strictly before the given date.
   * @param trimDate The date.
   * @return The trimmed annuity.
   */
  public AnnuityCouponIborDefinition trimStart(final ZonedDateTime trimDate) {
    final List<CouponIborDefinition> list = new ArrayList<>();
    for (final CouponIborDefinition payment : getPayments()) {
      if (!payment.getAccrualStartDate().isBefore(trimDate)) {
        list.add(payment);
      }
    }
    return new AnnuityCouponIborDefinition(list.toArray(new CouponIborDefinition[list.size()]), _iborIndex, _calendar);
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
  public Annuity<? extends Coupon> toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> indexFixingTS) {
    ArgChecker.notNull(date, "date");
    final List<Coupon> resultList = new ArrayList<>();
    final CouponIborDefinition[] payments = getPayments();
    for (int loopcoupon = 0; loopcoupon < payments.length; loopcoupon++) {
      if (!date.isAfter(payments[loopcoupon].getPaymentDate())) {
        resultList.add(payments[loopcoupon].toDerivative(date, indexFixingTS));
      }
    }
    return new Annuity<>(resultList.toArray(new Coupon[resultList.size()]));
  }

  @Override
  public Annuity<? extends Coupon> toDerivative(final ZonedDateTime date) {
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
    final AnnuityCouponIborDefinition other = (AnnuityCouponIborDefinition) obj;
    if (!Objects.equals(_iborIndex, other._iborIndex)) {
      return false;
    }
    return true;
  }

}

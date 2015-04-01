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

import com.opengamma.analytics.convention.daycount.ActualActualICMA;
import com.opengamma.analytics.convention.daycount.ActualActualICMANormal;
import com.opengamma.analytics.convention.daycount.DayCount;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.date.BusinessDayConvention;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.basics.schedule.Frequency;
import com.opengamma.strata.basics.schedule.StubConvention;
import com.opengamma.strata.collect.ArgChecker;

/**
 * A wrapper class for a AnnuityDefinition containing CouponFixedDefinition.
 */
public class AnnuityCouponFixedDefinition extends AnnuityDefinition<CouponFixedDefinition> {

  /**
   * Constructor from a list of fixed coupons.
   * @param payments The fixed coupons.
   * @param calendar The calendar.
   */
  public AnnuityCouponFixedDefinition(final CouponFixedDefinition[] payments, final HolidayCalendar calendar) {
    super(payments, calendar);
  }

  /**
   * Annuity builder from the conventions and common characteristics. The dates are constructed from the settlement date. If required the stub will be short.
   * @param currency The annuity currency.
   * @param settlementDate The settlement date.
   * @param tenor The annuity tenor.
   * @param paymentPeriod The period between payments.
   * @param calendar The calendar.
   * @param dayCount The day count.
   * @param businessDay The business day convention.
   * @param isEOM The end-of-month flag.
   * @param notional The notional.
   * @param fixedRate The fixed rate.
   * @param isPayer The payer flag.
   * @return The fixed annuity.
   */
  public static AnnuityCouponFixedDefinition from(final Currency currency, final ZonedDateTime settlementDate, final Period tenor, final Period paymentPeriod, final HolidayCalendar calendar,
      final DayCount dayCount, final BusinessDayConvention businessDay, final boolean isEOM, final double notional, final double fixedRate, final boolean isPayer) {
    ArgChecker.notNull(settlementDate, "settlement date");
    ArgChecker.notNull(tenor, "Tenor");
    final ZonedDateTime maturityDate = settlementDate.plus(tenor);
    return from(currency, settlementDate, maturityDate, paymentPeriod, calendar, dayCount, businessDay, isEOM, notional, fixedRate, isPayer);
  }

  /**
   * Annuity builder from the conventions and common characteristics. The dates are constructed from the settlement date. The stub convention is short at the start.
   * @param currency The annuity currency.
   * @param settlementDate The settlement date.
   * @param maturityDate The (unadjusted) maturity date of the annuity.
   * @param paymentPeriod The period between payments.
   * @param calendar The calendar.
   * @param dayCount The day count.
   * @param businessDay The business day convention.
   * @param isEOM The end-of-month flag.
   * @param notional The notional.
   * @param fixedRate The fixed rate.
   * @param isPayer The payer flag.
   * @return The fixed annuity.
   */
  public static AnnuityCouponFixedDefinition from(final Currency currency, final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final Period paymentPeriod, final HolidayCalendar calendar,
      final DayCount dayCount, final BusinessDayConvention businessDay, final boolean isEOM, final double notional, final double fixedRate, final boolean isPayer) {
    return from(currency, settlementDate, maturityDate, paymentPeriod, calendar, dayCount, businessDay, isEOM, notional, fixedRate, isPayer, StubConvention.SHORT_INITIAL);
  }

  /**
   * Annuity builder from the conventions and common characteristics. The dates are constructed from the settlement date. If required the stub will be short.
   * @param currency The annuity currency.
   * @param settlementDate The settlement date.
   * @param maturityDate The (unadjusted) maturity date of the annuity.
   * @param paymentPeriod The period between payments.
   * @param calendar The calendar.
   * @param dayCount The day count.
   * @param businessDay The business day convention.
   * @param isEOM The end-of-month flag.
   * @param notional The notional.
   * @param fixedRate The fixed rate.
   * @param isPayer The payer flag.
   * @param stub The stub type.
   * @return The fixed annuity.
   */
  public static AnnuityCouponFixedDefinition from(final Currency currency, final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final Period paymentPeriod, final HolidayCalendar calendar,
      final DayCount dayCount, final BusinessDayConvention businessDay, final boolean isEOM, final double notional, final double fixedRate, final boolean isPayer, final StubConvention stub) {
    ArgChecker.notNull(currency, "currency");
    ArgChecker.notNull(settlementDate, "settlement date");
    ArgChecker.notNull(maturityDate, "Maturity date");
    ArgChecker.notNull(paymentPeriod, "payment period");
    ArgChecker.notNull(calendar, "calendar");
    ArgChecker.notNull(dayCount, "day count");
    ArgChecker.notNull(businessDay, "business day convention");
    final boolean isStubShort = stub.equals(StubConvention.SHORT_FINAL) || stub.equals(StubConvention.SHORT_INITIAL);
    final boolean isStubStart = stub.equals(StubConvention.LONG_INITIAL) || stub.equals(StubConvention.SHORT_INITIAL); // Implementation note: dates computed from the end.
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, maturityDate, paymentPeriod, isStubShort,
        isStubStart, businessDay, calendar, isEOM);
    final double sign = isPayer ? -1.0 : 1.0;
    final CouponFixedDefinition[] coupons = new CouponFixedDefinition[paymentDates.length];
    //First coupon uses settlement date
    coupons[0] = new CouponFixedDefinition(currency, paymentDates[0], settlementDate, paymentDates[0], dayCount.getDayCountFraction(settlementDate, paymentDates[0], calendar),
        sign * notional, fixedRate);
    for (int loopcpn = 1; loopcpn < paymentDates.length; loopcpn++) {
      coupons[loopcpn] = new CouponFixedDefinition(currency, paymentDates[loopcpn], paymentDates[loopcpn - 1], paymentDates[loopcpn],
          dayCount.getDayCountFraction(paymentDates[loopcpn - 1], paymentDates[loopcpn], calendar), sign * notional, fixedRate);
    }
    return new AnnuityCouponFixedDefinition(coupons, calendar);
  }

  /**
   * Annuity builder from the conventions and common characteristics. The dates are constructed from the settlement date. If required the stub will be short.
   * @param currency The annuity currency.
   * @param settlementDate The settlement date.
   * @param maturityDate The (unadjusted) maturity date of the annuity.
   * @param frequency The payment frequency.
   * @param calendar The calendar.
   * @param dayCount The day count.
   * @param businessDay The business day convention.
   * @param isEOM The end-of-month flag.
   * @param notional The notional.
   * @param fixedRate The fixed rate.
   * @param isPayer The payer flag.
   * @return The fixed annuity.
   */
  public static AnnuityCouponFixedDefinition from(final Currency currency, final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final Frequency frequency, final HolidayCalendar calendar,
      final DayCount dayCount, final BusinessDayConvention businessDay, final boolean isEOM, final double notional, final double fixedRate, final boolean isPayer) {
    ArgChecker.notNull(currency, "currency");
    ArgChecker.notNull(settlementDate, "settlement date");
    ArgChecker.notNull(maturityDate, "maturity date");
    ArgChecker.notNull(frequency, "frequency");
    ArgChecker.notNull(calendar, "calendar");
    ArgChecker.notNull(dayCount, "day count");
    ArgChecker.notNull(businessDay, "business day convention");
    final double sign = isPayer ? -1.0 : 1.0;
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, maturityDate, frequency, true, false, businessDay, calendar, isEOM);
    final CouponFixedDefinition[] coupons = new CouponFixedDefinition[paymentDates.length];
    //First coupon uses settlement date
    coupons[0] = new CouponFixedDefinition(currency, paymentDates[0], settlementDate, paymentDates[0], dayCount.getDayCountFraction(settlementDate, paymentDates[0], calendar),
        sign * notional, fixedRate);
    for (int loopcpn = 1; loopcpn < paymentDates.length; loopcpn++) {
      coupons[loopcpn] = new CouponFixedDefinition(currency, paymentDates[loopcpn], paymentDates[loopcpn - 1], paymentDates[loopcpn],
          dayCount.getDayCountFraction(paymentDates[loopcpn - 1], paymentDates[loopcpn], calendar), sign * notional, fixedRate);
    }
    return new AnnuityCouponFixedDefinition(coupons, calendar);
  }

  /**
   * Annuity builder from the conventions and common characteristics.
   * @param currency The annuity currency.
   * @param settlementDate The settlement date.
   * @param paymentDatesUnadjusted The (unadjusted) payment dates of the annuity.
   * @param frequency The payment frequency.
   * @param calendar The calendar.
   * @param dayCount The day count.
   * @param businessDay The business day convention.
   * @param isEOM The end-of-month flag.
   * @param notional The notional.
   * @param fixedRate The fixed rate.
   * @param isPayer The payer flag.
   * @return The fixed annuity.
   */
  public static AnnuityCouponFixedDefinition from(final Currency currency, final ZonedDateTime settlementDate, final ZonedDateTime[] paymentDatesUnadjusted, final Frequency frequency,
      final HolidayCalendar calendar, final DayCount dayCount, final BusinessDayConvention businessDay, final boolean isEOM, final double notional, final double fixedRate, final boolean isPayer) {
    ArgChecker.notNull(currency, "currency");
    ArgChecker.notNull(settlementDate, "settlement date");
    ArgChecker.notNull(paymentDatesUnadjusted, "payment dates");
    ArgChecker.isTrue(paymentDatesUnadjusted.length > 0, "payment dates length");
    ArgChecker.notNull(frequency, "frequency");
    ArgChecker.notNull(calendar, "calendar");
    ArgChecker.notNull(dayCount, "day count");
    ArgChecker.notNull(businessDay, "business day convention");
    final double sign = isPayer ? -1.0 : 1.0;
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(paymentDatesUnadjusted, businessDay, calendar);
    final CouponFixedDefinition[] coupons = new CouponFixedDefinition[paymentDates.length];
    //First coupon uses settlement date
    coupons[0] = new CouponFixedDefinition(currency, paymentDates[0], settlementDate, paymentDates[0], dayCount.getDayCountFraction(settlementDate, paymentDates[0], calendar),
        sign * notional, fixedRate);
    for (int loopcpn = 1; loopcpn < paymentDates.length; loopcpn++) {
      coupons[loopcpn] = new CouponFixedDefinition(currency, paymentDates[loopcpn], paymentDates[loopcpn - 1], paymentDates[loopcpn],
          dayCount.getDayCountFraction(paymentDates[loopcpn - 1], paymentDates[loopcpn], calendar), sign * notional, fixedRate);
    }
    return new AnnuityCouponFixedDefinition(coupons, calendar);
  }

  /**
   * Annuity builder from a swap generator (only the fixed leg part is used).
   * @param settlementDate The settlement date.
   * @param tenor The annuity tenor.
   * @param generator The swap generator.
   * @param notional The annuity notional.
   * @param fixedRate The annuity fixed rate.
   * @param isPayer The payer flag.
   * @return The annuity.
   */
  public static AnnuityCouponFixedDefinition from(final ZonedDateTime settlementDate, final Period tenor, final GeneratorSwapFixedIbor generator, final double notional, final double fixedRate,
      final boolean isPayer) {
    ArgChecker.notNull(generator, "Swap generator");
    return AnnuityCouponFixedDefinition.from(generator.getCurrency(), settlementDate, tenor, generator.getFixedLegPeriod(), generator.getCalendar(), generator.getFixedLegDayCount(), generator
        .getIborIndex().getBusinessDayConvention(), generator.getIborIndex().isEndOfMonth(), notional, fixedRate, isPayer);
  }

  /**
   * Annuity builder from the conventions and common characteristics. The accrual dates are unadjusted. Often used for bonds.
   * @param currency The annuity currency.
   * @param settlementDate The settlement date.
   * @param maturityDate The (unadjusted) maturity date of the annuity.
   * @param period The period between payments.
   * @param stubShort In case the the periods do not fit exactly between start and end date, is the remaining interval shorter (true) or longer (false) than the requested period.
   * @param fromEnd The dates in the schedule can be computed from the end date (true) or from the start date (false).
   * @param calendar The calendar.
   * @param dayCount The day count.
   * @param businessDay The business day convention.
   * @param isEOM The end-of-month flag.
   * @param notional The notional.
   * @param fixedRate The fixed rate.
   * @param isPayer The payer flag.
   * @return The fixed annuity.
   */
  public static AnnuityCouponFixedDefinition fromAccrualUnadjusted(final Currency currency, final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final Period period,
      final boolean stubShort, final boolean fromEnd, final HolidayCalendar calendar, final DayCount dayCount, final BusinessDayConvention businessDay, final boolean isEOM, final double notional,
      final double fixedRate, final boolean isPayer) {
    ArgChecker.notNull(currency, "currency");
    ArgChecker.notNull(settlementDate, "settlement date");
    ArgChecker.notNull(maturityDate, "maturity date");
    ArgChecker.notNull(period, "period");
    ArgChecker.notNull(calendar, "calendar");
    ArgChecker.notNull(dayCount, "day count");
    ArgChecker.notNull(businessDay, "business day convention");
    ArgChecker.isTrue(!(dayCount instanceof ActualActualICMA) | !(dayCount instanceof ActualActualICMANormal), "Coupon per year required for Actua lActual ICMA");
    final double sign = isPayer ? -1.0 : 1.0;
    final ZonedDateTime[] paymentDatesUnadjusted = ScheduleCalculator.getUnadjustedDateSchedule(settlementDate, maturityDate, period, stubShort, fromEnd);
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(paymentDatesUnadjusted, businessDay, calendar);
    final CouponFixedDefinition[] coupons = new CouponFixedDefinition[paymentDates.length];
    //First coupon uses settlement date
    coupons[0] = new CouponFixedDefinition(currency, paymentDates[0], settlementDate, paymentDatesUnadjusted[0],
        dayCount.getDayCountFraction(settlementDate, paymentDates[0], calendar), sign * notional, fixedRate);
    for (int loopcpn = 1; loopcpn < paymentDates.length; loopcpn++) {
      coupons[loopcpn] = new CouponFixedDefinition(currency, paymentDates[loopcpn], paymentDatesUnadjusted[loopcpn - 1], paymentDatesUnadjusted[loopcpn],
          dayCount.getDayCountFraction(paymentDatesUnadjusted[loopcpn - 1], paymentDatesUnadjusted[loopcpn], calendar), sign * notional, fixedRate);
    }

    return new AnnuityCouponFixedDefinition(coupons, calendar);
  }

  /**
   * Annuity builder from the conventions and common characteristics. The accrual dates are unadjusted. Often used for bonds.
   * @param currency The annuity currency.
   * @param settlementDate The settlement date.
   * @param maturityDate The (unadjusted) maturity date of the annuity.
   * @param period The period between payments.
   * @param nbPaymentPerYear The number of coupon per year. Used for some day count conventions.
   * @param stubShort In case the the periods do not fit exactly between start and end date, is the remaining interval shorter (true) or longer (false) than the requested period.
   * @param fromEnd The dates in the schedule can be computed from the end date (true) or from the start date (false).
   * @param calendar The calendar.
   * @param dayCount The day count.
   * @param businessDay The business day convention.
   * @param isEOM The end-of-month flag.
   * @param notional The notional.
   * @param fixedRate The fixed rate.
   * @param isPayer The payer flag.
   * @return The fixed annuity.
   */
  public static AnnuityCouponFixedDefinition fromAccrualUnadjusted(final Currency currency, final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final Period period,
      final int nbPaymentPerYear, final boolean stubShort, final boolean fromEnd, final HolidayCalendar calendar, final DayCount dayCount, final BusinessDayConvention businessDay, final boolean isEOM,
      final double notional, final double fixedRate, final boolean isPayer) {
    ArgChecker.notNull(currency, "currency");
    ArgChecker.notNull(settlementDate, "settlement date");
    ArgChecker.notNull(maturityDate, "maturity date");
    ArgChecker.notNull(period, "period");
    ArgChecker.isTrue(nbPaymentPerYear > 0, "need greater than zero payments per year");
    ArgChecker.notNull(calendar, "calendar");
    ArgChecker.notNull(dayCount, "day count");
    ArgChecker.notNull(businessDay, "business day convention");
    final double sign = isPayer ? -1.0 : 1.0;
    final ZonedDateTime[] paymentDatesUnadjusted = ScheduleCalculator.getUnadjustedDateSchedule(settlementDate, maturityDate, period, stubShort, fromEnd);
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(paymentDatesUnadjusted, businessDay, calendar);
    final CouponFixedDefinition[] coupons = new CouponFixedDefinition[paymentDates.length];
    //First coupon uses settlement date
    coupons[0] = new CouponFixedDefinition(currency, paymentDates[0], settlementDate, paymentDatesUnadjusted[0], dayCount.getAccruedInterest(settlementDate, paymentDates[0], paymentDates[0], 1.0,
        nbPaymentPerYear), sign * notional, fixedRate);
    for (int loopcpn = 1; loopcpn < paymentDates.length; loopcpn++) {
      coupons[loopcpn] = new CouponFixedDefinition(currency, paymentDates[loopcpn], paymentDatesUnadjusted[loopcpn - 1], paymentDatesUnadjusted[loopcpn], dayCount.getAccruedInterest(
          paymentDatesUnadjusted[loopcpn - 1], paymentDatesUnadjusted[loopcpn], paymentDatesUnadjusted[loopcpn], 1.0, nbPaymentPerYear), sign * notional, fixedRate);
    }
    return new AnnuityCouponFixedDefinition(coupons, calendar);
  }

  /**
   * Creates a new annuity. The coupon is the new annuity are those with payment date strictly after the given date.
   * @param trimDate The date.
   * @return The trimmed annuity.
   */
  @Override
  public AnnuityCouponFixedDefinition trimBefore(final ZonedDateTime trimDate) {
    final List<CouponFixedDefinition> list = new ArrayList<>();
    for (final CouponFixedDefinition payment : getPayments()) {
      if (payment.getPaymentDate().isAfter(trimDate)) {
        list.add(payment);
      }
    }
    return new AnnuityCouponFixedDefinition(list.toArray(new CouponFixedDefinition[list.size()]), getCalendar());
  }

  /**
   * Creates a new annuity. The coupon in the new annuity have start accrual date after or on the given date.
   * @param trimDate The date.
   * @return The trimmed annuity.
   */
  public AnnuityCouponFixedDefinition trimStart(final ZonedDateTime trimDate) {
    final List<CouponFixedDefinition> list = new ArrayList<>();
    for (final CouponFixedDefinition payment : getPayments()) {
      if (!payment.getAccrualStartDate().isBefore(trimDate)) {
        list.add(payment);
      }
    }
    return new AnnuityCouponFixedDefinition(list.toArray(new CouponFixedDefinition[list.size()]), getCalendar());
  }

  @Override
  public AnnuityCouponFixed toDerivative(final ZonedDateTime date) {
    final List<CouponFixed> resultList = new ArrayList<>();
    for (int loopcoupon = 0; loopcoupon < getPayments().length; loopcoupon++) {
      if (!date.isAfter(getNthPayment(loopcoupon).getPaymentDate())) {
        resultList.add(getNthPayment(loopcoupon).toDerivative(date));
      }
    }
    return new AnnuityCouponFixed(resultList.toArray(new CouponFixed[resultList.size()]));
  }

  /**
   * Creates a new annuity with the same characteristics, except that the rate of all coupons is the one given.
   * @param rate The rate.
   * @return The new annuity.
   */
  public AnnuityCouponFixedDefinition withRate(final double rate) {
    final CouponFixedDefinition[] cpn = new CouponFixedDefinition[getNumberOfPayments()];
    for (int loopcpn = 0; loopcpn < getNumberOfPayments(); loopcpn++) {
      cpn[loopcpn] = getNthPayment(loopcpn).withRate(rate);
    }
    return new AnnuityCouponFixedDefinition(cpn, getCalendar());
  }


}

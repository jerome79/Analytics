/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.annuity;

import java.time.Period;
import java.time.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.payment.CouponONCompoundedDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.strata.basics.date.BusinessDayConvention;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.collect.ArgChecker;

/**
 *
 */
public class AnnuityCouponONCompoundedDefinition extends AnnuityDefinition<CouponONCompoundedDefinition> {
  /**
   * The overnight reference index
   */
  private final IndexON _index;

  /**
   * Constructor from a list of overnight coupons.
   * @param payments The coupons.
   * @param index The underlying overnight index.
   * @param calendar The holiday calendar
   */
  public AnnuityCouponONCompoundedDefinition(final CouponONCompoundedDefinition[] payments, final IndexON index, final HolidayCalendar calendar) {
    super(payments, calendar);
    _index = index;
  }

  /**
   * Annuity builder from the financial details.
   * @param settlementDate The settlement date, not null
   * @param tenorAnnuity The annuity tenor, not null
   * @param notional The annuity notional.
   * @param generator The overnight generator, not null
   * @param isPayer The flag indicating if the annuity is paying (true) or receiving (false).
   * @return The annuity.
   */
  public static AnnuityCouponONCompoundedDefinition from(final ZonedDateTime settlementDate, final Period tenorAnnuity, final double notional,
      final GeneratorSwapFixedON generator, final boolean isPayer) {
    ArgChecker.notNull(settlementDate, "settlement date");
    ArgChecker.notNull(tenorAnnuity, "tenor annuity");
    ArgChecker.notNull(generator, "generator");
    final ZonedDateTime[] endFixingPeriodDate = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, tenorAnnuity, generator.getLegsPeriod(), generator.getBusinessDayConvention(),
        generator.getOvernightCalendar(), generator.isEndOfMonth());
    return AnnuityCouponONCompoundedDefinition.from(settlementDate, endFixingPeriodDate, notional, isPayer, generator.getIndex(), generator.getPaymentLag(), generator.getOvernightCalendar());
  }

  /**
   * Annuity builder from the financial details.
   * @param settlementDate The settlement date, not null
   * @param maturityDate The maturity date. The maturity date is the end date of the last fixing period, not null
   * @param notional The notional.
   * @param generator The generator, not null.
   * @param isPayer The flag indicating if the annuity is paying (true) or receiving (false).
   * @return The annuity.
   */
  public static AnnuityCouponONCompoundedDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final double notional,
      final GeneratorSwapFixedON generator, final boolean isPayer) {
    ArgChecker.notNull(settlementDate, "settlement date");
    ArgChecker.notNull(maturityDate, "maturity date");
    ArgChecker.notNull(generator, "generator");
    final ZonedDateTime[] endFixingPeriodDate = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, maturityDate, generator.getLegsPeriod(), generator.getBusinessDayConvention(),
        generator.getOvernightCalendar(), generator.isEndOfMonth());
    return AnnuityCouponONCompoundedDefinition.from(settlementDate, endFixingPeriodDate, notional, isPayer, generator.getIndex(), generator.getPaymentLag(),
        generator.getOvernightCalendar());
  }

  /**
   * Build a annuity of overnight coupons from financial details.
   * @param settlementDate The annuity settlement or first fixing date, not null.
   * @param endFixingPeriodDate The end date of the overnight accrual period. Also called the maturity date of the annuity even if the actual payment can take place one or two days later. Not null.
   * @param notional The annuity notional.
   * @param isPayer The flag indicating if the annuity is paying (true) or receiving (false).
   * @param indexON The overnight index.
   * @param paymentLag The payment lag.
   * @param indexCalendar The calendar for the overnight index.
   * @param businessDayConvention The business day convention.
   * @param paymentPeriod The payment period.
   * @param isEOM Is EOM.
   * @return The annuity.
   */
  public static AnnuityCouponONCompoundedDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime endFixingPeriodDate, final double notional,
      final boolean isPayer, final IndexON indexON, final int paymentLag, final HolidayCalendar indexCalendar, final BusinessDayConvention businessDayConvention, final Period paymentPeriod,
      final boolean isEOM) {
    ArgChecker.notNull(settlementDate, "settlement date");
    ArgChecker.notNull(endFixingPeriodDate, "End fixing period date");
    ArgChecker.notNull(indexON, "overnight index");
    ArgChecker.notNull(indexCalendar, "index calendar");
    ArgChecker.notNull(businessDayConvention, "business day convention");
    ArgChecker.notNull(paymentPeriod, "payment period");
    final ZonedDateTime[] endFixingPeriodDates = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, endFixingPeriodDate, paymentPeriod, true,
        false, businessDayConvention, indexCalendar, isEOM); //TODO get rid of hard-codings
    return AnnuityCouponONCompoundedDefinition.from(settlementDate, endFixingPeriodDates, notional, isPayer, indexON, paymentLag, indexCalendar);
  }

  /**
   * Build a annuity of overnight coupons from financial details.
   * @param settlementDate The annuity settlement or first fixing date, not null.
   * @param tenorAnnuity The annuity tenor, not null
   * @param notional The annuity notional.
   * @param isPayer The flag indicating if the annuity is paying (true) or receiving (false).
   * @param indexON The overnight index.
   * @param paymentLag The payment lag.
   * @param indexCalendar The calendar for the overnight index.
   * @param businessDayConvention The business day convention.
   * @param paymentPeriod The payment period.
   * @param isEOM Is EOM.
   * @return The annuity.
   */
  public static AnnuityCouponONCompoundedDefinition from(final ZonedDateTime settlementDate, final Period tenorAnnuity, final double notional, final boolean isPayer,
      final IndexON indexON, final int paymentLag, final HolidayCalendar indexCalendar, final BusinessDayConvention businessDayConvention, final Period paymentPeriod, final boolean isEOM) {
    ArgChecker.notNull(settlementDate, "settlement date");
    ArgChecker.notNull(tenorAnnuity, "tenor annuity");
    ArgChecker.notNull(indexON, "overnight index");
    ArgChecker.notNull(indexCalendar, "index calendar");
    ArgChecker.notNull(businessDayConvention, "business day convention");
    ArgChecker.notNull(paymentPeriod, "payment period");
    final ZonedDateTime[] endFixingPeriodDates = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, tenorAnnuity, paymentPeriod, true,
        false, businessDayConvention, indexCalendar, isEOM); //TODO get rid of hard-codings
    return AnnuityCouponONCompoundedDefinition.from(settlementDate, endFixingPeriodDates, notional, isPayer, indexON, paymentLag, indexCalendar);
  }

  /**
   * Creates an overnight annuity
   * @param settlementDate The settlement date
   * @param endFixingPeriodDates The end fixing period dates
   * @param notional The notional
   * @param isPayer True if the annuity is paid
   * @param indexON The overnight reference index
   * @param paymentLag The payment lag
   * @param indexCalendar The index calendar
   * @return An overnight annuity
   */
  private static AnnuityCouponONCompoundedDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime[] endFixingPeriodDates, final double notional,
      final boolean isPayer, final IndexON indexON, final int paymentLag, final HolidayCalendar indexCalendar) {
    final double sign = isPayer ? -1.0 : 1.0;
    final double notionalSigned = sign * notional;
    final CouponONCompoundedDefinition[] coupons = new CouponONCompoundedDefinition[endFixingPeriodDates.length];
    coupons[0] = CouponONCompoundedDefinition.from(indexON, settlementDate, endFixingPeriodDates[0], notionalSigned, paymentLag, indexCalendar);
    for (int loopcpn = 1; loopcpn < endFixingPeriodDates.length; loopcpn++) {
      coupons[loopcpn] = CouponONCompoundedDefinition.from(indexON, endFixingPeriodDates[loopcpn - 1], endFixingPeriodDates[loopcpn], notionalSigned, paymentLag,
          indexCalendar);
    }
    return new AnnuityCouponONCompoundedDefinition(coupons, indexON, indexCalendar);
  }

  /**
   * Gets the overnight reference index
   * @return The overnight reference index
   */
  public IndexON getIndex() {
    return _index;
  }

}

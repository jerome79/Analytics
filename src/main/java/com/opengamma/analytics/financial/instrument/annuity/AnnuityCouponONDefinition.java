/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.annuity;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import com.opengamma.analytics.convention.businessday.BusinessDayConvention;
import com.opengamma.analytics.financial.instrument.NotionalProvider;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapIborON;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.payment.CouponONDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.timeseries.DoubleTimeSeries;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.collect.ArgChecker;

/**
 * A wrapper class for a {@link AnnuityDefinition} containing {@link CouponONDefinition}.
 */
public class AnnuityCouponONDefinition extends AnnuityCouponDefinition<CouponONDefinition> {

  /**
   * The overnight index.
   */
  private final IndexON _index;

  /**
   * Constructor from a list of overnight coupons.
   * @param payments The coupons.
   * @param index The index, not null
   * @param calendar The holiday calendar
   */
  public AnnuityCouponONDefinition(final CouponONDefinition[] payments, final IndexON index, final HolidayCalendar calendar) {
    super(payments, calendar);
    ArgChecker.notNull(index, "index");
    _index = index;
  }

  /**
   * Build a annuity of overnight coupons from financial details.
   * @param settlementDate The annuity settlement or first fixing date, not null.
   * @param tenorAnnuity The total tenor of the annuity, not null.
   * @param notional The annuity notional.
   * @param generator The overnight generator, not null.
   * @param isPayer The flag indicating if the annuity is paying (true) or receiving (false).
   * @return The annuity.
   */
  public static AnnuityCouponONDefinition from(final ZonedDateTime settlementDate, final Period tenorAnnuity, final double notional, final GeneratorSwapFixedON generator, final boolean isPayer) {
    ArgChecker.notNull(settlementDate, "settlement date");
    ArgChecker.notNull(tenorAnnuity, "tenor annuity");
    ArgChecker.notNull(generator, "generator");
    final ZonedDateTime[] endFixingPeriodDate = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, tenorAnnuity, generator.getLegsPeriod(), generator.isStubShort(), generator.isFromEnd(),
        generator.getBusinessDayConvention(), generator.getOvernightCalendar(), generator.isEndOfMonth());
    return AnnuityCouponONDefinition.from(settlementDate, endFixingPeriodDate, notional, generator, isPayer);
  }

  /**
   * Build a annuity of overnight coupons from financial details.
   * @param settlementDate The annuity settlement or first fixing date, not null.
   * @param tenorAnnuity The total tenor of the annuity, not null.
   * @param notional The annuity notional.
   * @param generator The Ibor/ON generator, not null.
   * @param isPayer The flag indicating if the annuity is paying (true) or receiving (false).
   * @return The annuity.
   */
  public static AnnuityCouponONDefinition from(final ZonedDateTime settlementDate, final Period tenorAnnuity, final double notional, final GeneratorSwapIborON generator, final boolean isPayer) {
    ArgChecker.notNull(settlementDate, "settlement date");
    ArgChecker.notNull(tenorAnnuity, "tenor annuity");
    ArgChecker.notNull(generator, "generator");
    final ZonedDateTime[] endFixingPeriodDate = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, tenorAnnuity, generator.getIndexIbor().getTenor(), generator.isStubShort(),
        generator.isFromEnd(), generator.getBusinessDayConvention(), generator.getOvernightCalendar(), generator.isEndOfMonth());
    return AnnuityCouponONDefinition.from(settlementDate, endFixingPeriodDate, notional, generator, isPayer);
  }

  /**
   * Build a annuity of overnight coupons from financial details.
   * @param settlementDate The annuity settlement or first fixing date, not null.
   * @param endFixingPeriodDate The end date of the overnight accrual period. Also called the maturity date of the annuity even if the actual payment can take place one or two days later. Not null.
   * @param notional The annuity notional.
   * @param generator The overnight generator, not null.
   * @param isPayer The flag indicating if the annuity is paying (true) or receiving (false).
   * @return The annuity.
   */
  public static AnnuityCouponONDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime endFixingPeriodDate, final double notional, final GeneratorSwapFixedON generator,
      final boolean isPayer) {
    ArgChecker.notNull(settlementDate, "settlement date");
    ArgChecker.notNull(endFixingPeriodDate, "End fixing period date");
    ArgChecker.notNull(generator, "generator");
    final ZonedDateTime[] endFixingPeriodDates = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, endFixingPeriodDate, generator.getLegsPeriod(), generator.isStubShort(),
        generator.isFromEnd(), generator.getBusinessDayConvention(), generator.getOvernightCalendar(), generator.isEndOfMonth());
    return AnnuityCouponONDefinition.from(settlementDate, endFixingPeriodDates, notional, generator, isPayer);
  }

  /**
   * Build a annuity of overnight coupons from financial details.
   * @param settlementDate The annuity settlement or first fixing date, not null.
   * @param endFixingPeriodDate The end date of the overnight accrual period. Also called the maturity date of the annuity even if the actual payment can take place one or two days later. Not null.
   * @param notional The annuity notional.
   * @param generator The overnight generator, not null.
   * @param isPayer The flag indicating if the annuity is paying (true) or receiving (false).
   * @return The annuity.
   */
  public static AnnuityCouponONDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime endFixingPeriodDate, final NotionalProvider notional, final GeneratorSwapFixedON generator,
                                               final boolean isPayer) {
    ArgChecker.notNull(settlementDate, "settlement date");
    ArgChecker.notNull(endFixingPeriodDate, "End fixing period date");
    ArgChecker.notNull(generator, "generator");
    final ZonedDateTime[] endFixingPeriodDates = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, endFixingPeriodDate, generator.getLegsPeriod(), generator.isStubShort(),
                                                                                            generator.isFromEnd(), generator.getBusinessDayConvention(), generator.getOvernightCalendar(), generator.isEndOfMonth());
    return AnnuityCouponONDefinition.from(settlementDate, endFixingPeriodDates, notional, generator, isPayer);
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
  public static AnnuityCouponONDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime endFixingPeriodDate, final double notional, final boolean isPayer,
      final IndexON indexON, final int paymentLag, final HolidayCalendar indexCalendar, final BusinessDayConvention businessDayConvention, final Period paymentPeriod, final boolean isEOM) {
    ArgChecker.notNull(settlementDate, "settlement date");
    ArgChecker.notNull(endFixingPeriodDate, "End fixing period date");
    ArgChecker.notNull(indexON, "overnight index");
    ArgChecker.notNull(indexCalendar, "index calendar");
    ArgChecker.notNull(businessDayConvention, "business day convention");
    ArgChecker.notNull(paymentPeriod, "payment period");
    final ZonedDateTime[] endFixingPeriodDates = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, endFixingPeriodDate, paymentPeriod, true,
        false, businessDayConvention, indexCalendar, isEOM); //TODO get rid of hard-codings
    final double sign = isPayer ? -1.0 : 1.0;
    final double notionalSigned = sign * notional;
    final CouponONDefinition[] coupons = new CouponONDefinition[endFixingPeriodDates.length];
    coupons[0] = CouponONDefinition.from(indexON, settlementDate, endFixingPeriodDates[0], notionalSigned, paymentLag, indexCalendar);
    for (int loopcpn = 1; loopcpn < endFixingPeriodDates.length; loopcpn++) {
      coupons[loopcpn] = CouponONDefinition.from(indexON, endFixingPeriodDates[loopcpn - 1], endFixingPeriodDates[loopcpn], notionalSigned, paymentLag,
          indexCalendar);
    }
    return new AnnuityCouponONDefinition(coupons, indexON, indexCalendar);
  }

  /**
   * Build a annuity of overnight coupons from financial details.
   * @param settlementDate The annuity settlement or first fixing date, not null.
   * @param endFixingPeriodDate The end date of the overnight accrual period. Also called the maturity date of the annuity even if the actual payment can take place one or two days later. Not null.
   * @param notional The annuity notional.
   * @param generator The Ibor/ON generator, not null.
   * @param isPayer The flag indicating if the annuity is paying (true) or receiving (false).
   * @return The annuity.
   */
  public static AnnuityCouponONDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime endFixingPeriodDate, final double notional, final GeneratorSwapIborON generator,
      final boolean isPayer) {
    ArgChecker.notNull(settlementDate, "settlement date");
    ArgChecker.notNull(endFixingPeriodDate, "End fixing period date");
    ArgChecker.notNull(generator, "generator");
    final ZonedDateTime[] endFixingPeriodDates = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, endFixingPeriodDate, generator.getIndexIbor().getTenor(), generator.isStubShort(),
        generator.isFromEnd(), generator.getBusinessDayConvention(), generator.getOvernightCalendar(), generator.isEndOfMonth());
    return AnnuityCouponONDefinition.from(settlementDate, endFixingPeriodDates, notional, generator, isPayer);
  }

  /**
   * Build a annuity of overnight coupons from financial details.
   * @param settlementDate The annuity settlement or first fixing date, not null.
   * @param endFixingPeriodDate The end date of the overnight accrual period. Also called the maturity date of the annuity even if the actual payment can take place one or two days later. Not null.
   * @param notional The annuity notional.
   * @param generator The Ibor/ON generator, not null.
   * @param isPayer The flag indicating if the annuity is paying (true) or receiving (false).
   * @return The annuity.
   */
  public static AnnuityCouponONDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime endFixingPeriodDate, final NotionalProvider notional, final GeneratorSwapIborON generator,
                                               final boolean isPayer) {
    ArgChecker.notNull(settlementDate, "settlement date");
    ArgChecker.notNull(endFixingPeriodDate, "End fixing period date");
    ArgChecker.notNull(generator, "generator");
    final ZonedDateTime[] endFixingPeriodDates = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, endFixingPeriodDate, generator.getIndexIbor().getTenor(), generator.isStubShort(),
                                                                                            generator.isFromEnd(), generator.getBusinessDayConvention(), generator.getOvernightCalendar(), generator.isEndOfMonth());
    return AnnuityCouponONDefinition.from(settlementDate, endFixingPeriodDates, notional, generator, isPayer);
  }

  /**
   * Creates an overnight annuity.
   * @param settlementDate The settlement date
   * @param endFixingPeriodDates The end fixing period dates
   * @param notional The notional
   * @param generator A fixed / overnight swap generator
   * @param isPayer True if the annuity is paid
   * @return An overnight annuity
   */
  private static AnnuityCouponONDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime[] endFixingPeriodDates, final double notional, final GeneratorSwapFixedON generator,
      final boolean isPayer) {
    final double sign = isPayer ? -1.0 : 1.0;
    final double notionalSigned = sign * notional;
    final CouponONDefinition[] coupons = new CouponONDefinition[endFixingPeriodDates.length];
    coupons[0] = CouponONDefinition.from(generator.getIndex(), settlementDate, endFixingPeriodDates[0], notionalSigned, generator.getPaymentLag(), generator.getOvernightCalendar());
    for (int loopcpn = 1; loopcpn < endFixingPeriodDates.length; loopcpn++) {
      coupons[loopcpn] = CouponONDefinition.from(generator.getIndex(), endFixingPeriodDates[loopcpn - 1], endFixingPeriodDates[loopcpn], notionalSigned, generator.getPaymentLag(),
          generator.getOvernightCalendar());
    }
    return new AnnuityCouponONDefinition(coupons, generator.getIndex(), generator.getOvernightCalendar());
  }

  /**
   * Creates an overnight annuity.
   * @param settlementDate The settlement date
   * @param endFixingPeriodDates The end fixing period dates
   * @param notional The notional
   * @param generator A fixed / overnight swap generator
   * @param isPayer True if the annuity is paid
   * @return An overnight annuity
   */
  private static AnnuityCouponONDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime[] endFixingPeriodDates, final NotionalProvider notional, final GeneratorSwapFixedON generator,
                                                final boolean isPayer) {
    final double sign = isPayer ? -1.0 : 1.0;
    final CouponONDefinition[] coupons = new CouponONDefinition[endFixingPeriodDates.length];
    coupons[0] = CouponONDefinition.from(generator.getIndex(), settlementDate, endFixingPeriodDates[0], sign * notional.getAmount(settlementDate.toLocalDate()), generator.getPaymentLag(), generator.getOvernightCalendar());
    for (int loopcpn = 1; loopcpn < endFixingPeriodDates.length; loopcpn++) {
      coupons[loopcpn] = CouponONDefinition.from(generator.getIndex(), endFixingPeriodDates[loopcpn - 1], endFixingPeriodDates[loopcpn], sign * notional.getAmount(endFixingPeriodDates[loopcpn - 1].toLocalDate()), generator.getPaymentLag(),
                                                 generator.getOvernightCalendar());
    }
    return new AnnuityCouponONDefinition(coupons, generator.getIndex(), generator.getOvernightCalendar());
  }

  /**
   * Creates an overnight annuity.
   * @param settlementDate The settlement date
   * @param endFixingPeriodDates The end fixing period dates
   * @param notional The notional
   * @param generator A ibor / overnight swap generator
   * @param isPayer True if the annuity is paid
   * @return An overnight annuity
   */
  private static AnnuityCouponONDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime[] endFixingPeriodDates, final double notional, final GeneratorSwapIborON generator,
      final boolean isPayer) {
    final double sign = isPayer ? -1.0 : 1.0;
    final double notionalSigned = sign * notional;
    final CouponONDefinition[] coupons = new CouponONDefinition[endFixingPeriodDates.length];
    coupons[0] = CouponONDefinition.from(generator.getIndexON(), settlementDate, endFixingPeriodDates[0], notionalSigned, generator.getPaymentLag(), generator.getOvernightCalendar());
    for (int loopcpn = 1; loopcpn < endFixingPeriodDates.length; loopcpn++) {
      coupons[loopcpn] = CouponONDefinition.from(generator.getIndexON(), endFixingPeriodDates[loopcpn - 1], endFixingPeriodDates[loopcpn], notionalSigned, generator.getPaymentLag(),
          generator.getOvernightCalendar());
    }
    return new AnnuityCouponONDefinition(coupons, generator.getIndexON(), generator.getOvernightCalendar());
  }

  /**
   * Creates an overnight annuity.
   * @param settlementDate The settlement date
   * @param endFixingPeriodDates The end fixing period dates
   * @param notional The notional
   * @param generator A ibor / overnight swap generator
   * @param isPayer True if the annuity is paid
   * @return An overnight annuity
   */
  private static AnnuityCouponONDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime[] endFixingPeriodDates, final NotionalProvider notional, final GeneratorSwapIborON generator,
                                                final boolean isPayer) {
    final double sign = isPayer ? -1.0 : 1.0;
    final CouponONDefinition[] coupons = new CouponONDefinition[endFixingPeriodDates.length];
    coupons[0] = CouponONDefinition.from(generator.getIndexON(), settlementDate, endFixingPeriodDates[0], sign * notional.getAmount(settlementDate.toLocalDate()), generator.getPaymentLag(), generator.getOvernightCalendar());
    for (int loopcpn = 1; loopcpn < endFixingPeriodDates.length; loopcpn++) {
      coupons[loopcpn] = CouponONDefinition.from(generator.getIndexON(), endFixingPeriodDates[loopcpn - 1], endFixingPeriodDates[loopcpn], sign * notional.getAmount(endFixingPeriodDates[loopcpn - 1].toLocalDate()), generator.getPaymentLag(),
                                                 generator.getOvernightCalendar());
    }
    return new AnnuityCouponONDefinition(coupons, generator.getIndexON(), generator.getOvernightCalendar());
  }

  /**
   * Gets the overnight index.
   * @return The overnight index.
   */
  public IndexON getOvernightIndex() {
    return _index;
  }

  @Override
  public Annuity<? extends Coupon> toDerivative(final ZonedDateTime valZdt, final DoubleTimeSeries<ZonedDateTime> indexFixingTS) {
    ArgChecker.notNull(valZdt, "date");
    ArgChecker.notNull(indexFixingTS, "index fixing time series");
    final List<Coupon> resultList = new ArrayList<>();
    final CouponONDefinition[] payments = getPayments();
    final ZonedDateTime valZdtInPaymentZone = valZdt.withZoneSameInstant(payments[0].getPaymentDate().getZone());
    final LocalDate valDate = valZdtInPaymentZone.toLocalDate();

    for (CouponONDefinition payment : payments) {
      if (!valDate.isAfter(payment.getPaymentDate().toLocalDate())) {
        resultList.add(payment.toDerivative(valZdt, indexFixingTS));
      }
    }
    return new Annuity<>(resultList.toArray(new Coupon[resultList.size()]));
  }
}

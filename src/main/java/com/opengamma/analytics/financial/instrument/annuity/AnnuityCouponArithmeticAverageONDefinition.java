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

import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.payment.CouponONArithmeticAverageDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.timeseries.DoubleTimeSeries;
import com.opengamma.strata.basics.date.BusinessDayConvention;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.basics.schedule.StubConvention;
import com.opengamma.strata.collect.ArgChecker;

/**
 * A wrapper class for an annuity containing overnight arithmetic averaged coupons (i.e. the floating leg for a Fed funds-like
 * rate).
 */
public class AnnuityCouponArithmeticAverageONDefinition extends AnnuityCouponDefinition<CouponONArithmeticAverageDefinition> {

  /**
   * Constructor from a list of overnight arithmetic average coupons.
   * @param payments The coupons.
   * @param calendar The holiday calendar
   */
  public AnnuityCouponArithmeticAverageONDefinition(final CouponONArithmeticAverageDefinition[] payments, final HolidayCalendar calendar) {
    super(payments, calendar);
  }

  /**
   * Build a annuity of overnight arithmetic average coupons from financial details. The stub convention is short at the start.
   * @param settlementDate The annuity settlement or first fixing date, not null.
   * @param endFixingPeriodDate The end date of the overnight arithmetic average coupons accrual period. Also called the maturity date of the annuity even if the
   * actual payment can take place one or two days later. Not null.
   * @param notional The annuity notional, not null
   * @param isPayer The flag indicating if the annuity is paying (true) or receiving (false).
   * @param paymentPeriod The payment period, not null
   * @param indexON The overnight index, not null
   * @param paymentLag The payment lag, not null
   * @param businessDayConvention The business day convention, not null
   * @param isEOM True if the date schedule is EOM.
   * @param indexCalendar The calendar for the overnight index, not null
   * @return The annuity.
   */
  public static AnnuityCouponArithmeticAverageONDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime endFixingPeriodDate, final double notional, final boolean isPayer,
      final Period paymentPeriod, final IndexON indexON, final int paymentLag, final BusinessDayConvention businessDayConvention, final boolean isEOM, final HolidayCalendar indexCalendar) {
    return from(settlementDate, endFixingPeriodDate, notional, isPayer, paymentPeriod, indexON, paymentLag, businessDayConvention, isEOM, indexCalendar, StubConvention.SHORT_INITIAL);
  }

  /**
   * Build a annuity of overnight arithmetic average coupons from financial details.
   * @param settlementDate The annuity settlement or first fixing date, not null.
   * @param endFixingPeriodDate The end date of the overnight arithmetic average coupons accrual period. Also called the maturity date of the annuity even if the
   * actual payment can take place one or two days later. Not null.
   * @param notional The annuity notional, not null
   * @param isPayer The flag indicating if the annuity is paying (true) or receiving (false).
   * @param paymentPeriod The payment period, not null
   * @param indexON The overnight index, not null
   * @param paymentLag The payment lag, not null
   * @param businessDayConvention The business day convention, not null
   * @param isEOM True if the date schedule is EOM.
   * @param indexCalendar The calendar for the overnight index, not null
   * @param stub The stub type.
   * @return The annuity.
   */
  public static AnnuityCouponArithmeticAverageONDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime endFixingPeriodDate, final double notional, final boolean isPayer,
      final Period paymentPeriod, final IndexON indexON, final int paymentLag, final BusinessDayConvention businessDayConvention, final boolean isEOM, final HolidayCalendar indexCalendar,
      final StubConvention stub) {
    ArgChecker.notNull(settlementDate, "settlement date");
    ArgChecker.notNull(endFixingPeriodDate, "End fixing period date");
    ArgChecker.notNull(indexON, "overnight index");
    ArgChecker.notNull(indexCalendar, "index calendar");
    ArgChecker.notNull(businessDayConvention, "business day convention");
    ArgChecker.notNull(paymentPeriod, "payment period");
    final boolean isStubShort = stub.equals(StubConvention.SHORT_FINAL) || stub.equals(StubConvention.SHORT_INITIAL);
    final boolean isStubStart = stub.equals(StubConvention.LONG_INITIAL) || stub.equals(StubConvention.SHORT_INITIAL); // Implementation note: dates computed from the end.
    final ZonedDateTime[] endFixingPeriodDates = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, endFixingPeriodDate, paymentPeriod, isStubShort,
        isStubStart, businessDayConvention, indexCalendar, isEOM);
    final double sign = isPayer ? -1.0 : 1.0;
    final double notionalSigned = sign * notional;
    final CouponONArithmeticAverageDefinition[] coupons = new CouponONArithmeticAverageDefinition[endFixingPeriodDates.length];
    coupons[0] = CouponONArithmeticAverageDefinition.from(indexON, settlementDate, endFixingPeriodDates[0], notionalSigned, paymentLag, indexCalendar);
    for (int loopcpn = 1; loopcpn < endFixingPeriodDates.length; loopcpn++) {
      coupons[loopcpn] = CouponONArithmeticAverageDefinition.from(indexON, endFixingPeriodDates[loopcpn - 1], endFixingPeriodDates[loopcpn], notionalSigned, paymentLag,
          indexCalendar);
    }
    return new AnnuityCouponArithmeticAverageONDefinition(coupons, indexCalendar);
  }

  /**
   * Build a annuity of overnight arithmetic average coupons with rate cut off (the two last fixings in the average are the same : the second last) 
   * from financial details. The stub convention is short at the start.
   * @param settlementDate The annuity settlement or first fixing date, not null.
   * @param endFixingPeriodDate The end date of the overnight arithmetic average coupons accrual period. Also called the maturity date of the annuity even if the
   * actual payment can take place one or two days later. Not null.
   * @param notional The annuity notional, not null
   * @param isPayer The flag indicating if the annuity is paying (true) or receiving (false).
   * @param paymentPeriod The payment period, not null
   * @param indexON The overnight index, not null
   * @param paymentLag The payment lag, not null
   * @param businessDayConvention The business day convention, not null
   * @param isEOM True if the date schedule is EOM.
   * @param indexCalendar The calendar for the overnight index, not null
   * @param rateCutOff The rate cut off should be bigger than 2,and smaller than the number of period (which the number of open days between the two fixing periods)
   * @return The annuity.
   */
  public static AnnuityCouponArithmeticAverageONDefinition withRateCutOff(final ZonedDateTime settlementDate, final ZonedDateTime endFixingPeriodDate, final double notional, final boolean isPayer,
      final Period paymentPeriod, final IndexON indexON, final int paymentLag, final BusinessDayConvention businessDayConvention, final boolean isEOM, final HolidayCalendar indexCalendar,
      final int rateCutOff) {
    return withRateCutOff(settlementDate, endFixingPeriodDate, notional, isPayer, paymentPeriod, indexON, paymentLag, businessDayConvention, isEOM, indexCalendar, StubConvention.SHORT_INITIAL, rateCutOff);
  }

  /**
   * Build a annuity of overnight arithmetic average coupons with rate cut off (the two last fixings in the average are the same : the second last) from financial details.
   * @param settlementDate The annuity settlement or first fixing date, not null.
   * @param endFixingPeriodDate The end date of the overnight arithmetic average coupons accrual period. Also called the maturity date of the annuity even if the
   * actual payment can take place one or two days later. Not null.
   * @param notional The annuity notional, not null
   * @param isPayer The flag indicating if the annuity is paying (true) or receiving (false).
   * @param paymentPeriod The payment period, not null
   * @param indexON The overnight index, not null
   * @param paymentLag The payment lag, not null
   * @param businessDayConvention The business day convention, not null
   * @param isEOM True if the date schedule is EOM.
   * @param indexCalendar The calendar for the overnight index, not null
   * @param stub The stub type.
   * @param rateCutOff The rate cut off should be bigger than 2,and smaller than the number of period (which the number of open days between the two fixing periods)
   * @return The annuity.
   */
  public static AnnuityCouponArithmeticAverageONDefinition withRateCutOff(final ZonedDateTime settlementDate, final ZonedDateTime endFixingPeriodDate, final double notional, final boolean isPayer,
      final Period paymentPeriod, final IndexON indexON, final int paymentLag, final BusinessDayConvention businessDayConvention, final boolean isEOM, final HolidayCalendar indexCalendar,
      final StubConvention stub, final int rateCutOff) {
    ArgChecker.notNull(settlementDate, "settlement date");
    ArgChecker.notNull(endFixingPeriodDate, "End fixing period date");
    ArgChecker.notNull(indexON, "overnight index");
    ArgChecker.notNull(indexCalendar, "index calendar");
    ArgChecker.notNull(businessDayConvention, "business day convention");
    ArgChecker.notNull(paymentPeriod, "payment period");
    final boolean isStubShort = stub.equals(StubConvention.SHORT_FINAL) || stub.equals(StubConvention.SHORT_INITIAL);
    final boolean isStubStart = stub.equals(StubConvention.LONG_INITIAL) || stub.equals(StubConvention.SHORT_INITIAL); // Implementation note: dates computed from the end.
    final ZonedDateTime[] endFixingPeriodDates = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, endFixingPeriodDate, paymentPeriod, isStubShort,
        isStubStart, businessDayConvention, indexCalendar, isEOM);
    final double sign = isPayer ? -1.0 : 1.0;
    final double notionalSigned = sign * notional;
    final CouponONArithmeticAverageDefinition[] coupons = new CouponONArithmeticAverageDefinition[endFixingPeriodDates.length];
    coupons[0] = CouponONArithmeticAverageDefinition.withRateCutOff(indexON, settlementDate, endFixingPeriodDates[0], notionalSigned, paymentLag, indexCalendar, rateCutOff);
    for (int loopcpn = 1; loopcpn < endFixingPeriodDates.length; loopcpn++) {
      coupons[loopcpn] = CouponONArithmeticAverageDefinition.withRateCutOff(indexON, endFixingPeriodDates[loopcpn - 1], endFixingPeriodDates[loopcpn], notionalSigned, paymentLag,
          indexCalendar, rateCutOff);
    }
    return new AnnuityCouponArithmeticAverageONDefinition(coupons, indexCalendar);
  }

  @Override
  public Annuity<? extends Coupon> toDerivative(final ZonedDateTime valZdt, final DoubleTimeSeries<ZonedDateTime> indexFixingTS) {
    ArgChecker.notNull(valZdt, "date");
    ArgChecker.notNull(indexFixingTS, "index fixing time series");
    final List<Coupon> resultList = new ArrayList<>();
    final CouponONArithmeticAverageDefinition[] payments = getPayments();
    final ZonedDateTime valZdtInPaymentZone = valZdt.withZoneSameInstant(payments[0].getPaymentDate().getZone());
    final LocalDate valDate = valZdtInPaymentZone.toLocalDate();

    for (int loopcoupon = 0; loopcoupon < payments.length; loopcoupon++) {
      if (!valDate.isAfter(payments[loopcoupon].getPaymentDate().toLocalDate())) {
        resultList.add(payments[loopcoupon].toDerivative(valZdt, indexFixingTS));
      }
    }
    return new Annuity<>(resultList.toArray(new Coupon[resultList.size()]));
  }
}

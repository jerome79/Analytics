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

import com.opengamma.analytics.convention.StubType;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.payment.CouponONArithmeticAverageSpreadDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.timeseries.DoubleTimeSeries;
import com.opengamma.strata.basics.date.BusinessDayConvention;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.collect.ArgChecker;

/**
 * A wrapper class for an annuity containing overnight arithmetic averaged coupons (i.e. the floating leg for a Fed funds-like
 * rate) with a spread.
 */
public class AnnuityCouponArithmeticAverageONSpreadDefinition extends AnnuityCouponDefinition<CouponONArithmeticAverageSpreadDefinition> {

  /**
   * Constructor from a list of overnight arithmetic average coupons with spread.
   * @param payments The coupons.
   * @param calendar The holiday calendar
   */
  public AnnuityCouponArithmeticAverageONSpreadDefinition(final CouponONArithmeticAverageSpreadDefinition[] payments, final HolidayCalendar calendar) {
    super(payments, calendar);
  }

  /**
   * Build a annuity of overnight arithmetic average coupons with spread from financial details. The stub convention is short at the start.
   * @param settlementDate The annuity settlement or first fixing date, not null.
   * @param endFixingPeriodDate The end date of the overnight arithmetic average coupons accrual period. Also called the maturity date of the annuity even if the
   * actual payment can take place one or two days later. Not null.
   * @param notional The annuity notional, not null
   * @param spread The spread
   * @param isPayer The flag indicating if the annuity is paying (true) or receiving (false).
   * @param paymentPeriod The payment period, not null
   * @param indexON The overnight index, not null
   * @param paymentLag The payment lag, not null
   * @param businessDayConvention The business day convention, not null
   * @param isEOM True if the date schedule is EOM.
   * @param indexCalendar The calendar for the overnight index, not null
   * @return The annuity.
   */
  public static AnnuityCouponArithmeticAverageONSpreadDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime endFixingPeriodDate, final double notional, final double spread,
      final boolean isPayer, final Period paymentPeriod, final IndexON indexON, final int paymentLag, final BusinessDayConvention businessDayConvention, final boolean isEOM,
      final HolidayCalendar indexCalendar) {
    return from(settlementDate, endFixingPeriodDate, notional, spread, isPayer, paymentPeriod, indexON, paymentLag, businessDayConvention, isEOM, indexCalendar, StubType.SHORT_START);
  }

  /**
   * Build a annuity of overnight arithmetic average coupons with spread from financial details.
   * @param settlementDate The annuity settlement or first fixing date, not null.
   * @param endFixingPeriodDate The end date of the overnight arithmetic average coupons accrual period. Also called the maturity date of the annuity even if the
   * actual payment can take place one or two days later. Not null.
   * @param notional The annuity notional, not null
   * @param spread The spread
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
  public static AnnuityCouponArithmeticAverageONSpreadDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime endFixingPeriodDate, final double notional, final double spread,
      final boolean isPayer, final Period paymentPeriod, final IndexON indexON, final int paymentLag, final BusinessDayConvention businessDayConvention, final boolean isEOM,
      final HolidayCalendar indexCalendar, final StubType stub) {
    ArgChecker.notNull(settlementDate, "settlement date");
    ArgChecker.notNull(endFixingPeriodDate, "End fixing period date");
    ArgChecker.notNull(indexON, "overnight index");
    ArgChecker.notNull(indexCalendar, "index calendar");
    ArgChecker.notNull(businessDayConvention, "business day convention");
    ArgChecker.notNull(paymentPeriod, "payment period");
    final boolean isStubShort = stub.equals(StubType.SHORT_END) || stub.equals(StubType.SHORT_START);
    final boolean isStubStart = stub.equals(StubType.LONG_START) || stub.equals(StubType.SHORT_START); // Implementation note: dates computed from the end.
    final ZonedDateTime[] endFixingPeriodDates = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, endFixingPeriodDate, paymentPeriod, isStubShort,
        isStubStart, businessDayConvention, indexCalendar, isEOM);
    final double sign = isPayer ? -1.0 : 1.0;
    final double notionalSigned = sign * notional;
    final CouponONArithmeticAverageSpreadDefinition[] coupons = new CouponONArithmeticAverageSpreadDefinition[endFixingPeriodDates.length];
    coupons[0] = CouponONArithmeticAverageSpreadDefinition.from(indexON, settlementDate, endFixingPeriodDates[0], notionalSigned, paymentLag, spread, indexCalendar);
    for (int loopcpn = 1; loopcpn < endFixingPeriodDates.length; loopcpn++) {
      coupons[loopcpn] = CouponONArithmeticAverageSpreadDefinition.from(indexON, endFixingPeriodDates[loopcpn - 1], endFixingPeriodDates[loopcpn], notionalSigned, paymentLag,
          spread, indexCalendar);
    }
    return new AnnuityCouponArithmeticAverageONSpreadDefinition(coupons, indexCalendar);
  }

  @Override
  public Annuity<? extends Coupon> toDerivative(final ZonedDateTime valZdt, final DoubleTimeSeries<ZonedDateTime> indexFixingTS) {
    ArgChecker.notNull(valZdt, "date");
    ArgChecker.notNull(indexFixingTS, "index fixing time series");
    final List<Coupon> resultList = new ArrayList<>();
    final CouponONArithmeticAverageSpreadDefinition[] payments = getPayments();
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

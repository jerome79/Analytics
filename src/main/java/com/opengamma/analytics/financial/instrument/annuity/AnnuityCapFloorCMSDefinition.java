/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.annuity;

import java.time.Period;
import java.time.ZonedDateTime;

import com.opengamma.analytics.convention.daycount.DayCount;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.analytics.financial.instrument.payment.CapFloorCMSDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.collect.ArgChecker;

/**
 * A wrapper class for a AnnuityDefinition containing CMS cap/floor Definition.
 */
public class AnnuityCapFloorCMSDefinition extends AnnuityDefinition<CapFloorCMSDefinition> {

  /**
   * Constructor from a list of CMS coupons.
   * @param payments The CMS coupons.
   * @param calendar The holiday calendar
   */
  public AnnuityCapFloorCMSDefinition(final CapFloorCMSDefinition[] payments, final HolidayCalendar calendar) {
    super(payments, calendar);
  }

  /**
   * CMS cap/floor (or leg of CMS caplet/floorlet) constructor from standard description. The cap/floor are fixing in advance and payment in arrears.
   * The CMS fixing is done at a standard lag before the coupon start. The date are computed from the settlement date (stub last) with a short stub is necessary.
   * @param settlementDate The settlement date.
   * @param maturityDate The annuity maturity date.
   * @param notional The notional.
   * @param index The CMS index.
   * @param paymentPeriod The payment period of the coupons.
   * @param dayCount The day count of the coupons.
   * @param isPayer Payer (true) / receiver (false) flag.
   * @param strike The common strike.
   * @param isCap The cap (true) / floor (false) flag.
   * @param calendar The holiday calendar for the ibor index.
   * @return The CMS coupon leg.
   */
  public static AnnuityCapFloorCMSDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final double notional, final IndexSwap index, final Period paymentPeriod,
      final DayCount dayCount, final boolean isPayer, final double strike, final boolean isCap, final HolidayCalendar calendar) {
    ArgChecker.notNull(settlementDate, "settlement date");
    ArgChecker.notNull(maturityDate, "maturity date");
    ArgChecker.notNull(index, "index");
    ArgChecker.isTrue(notional > 0, "notional <= 0");
    ArgChecker.notNull(paymentPeriod, "Payment period");
    final ZonedDateTime[] paymentDatesUnadjusted = ScheduleCalculator.getUnadjustedDateSchedule(settlementDate, maturityDate, paymentPeriod, true, false);
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(paymentDatesUnadjusted, index.getIborIndex().getBusinessDayConvention(), calendar, false);
    final double sign = isPayer ? -1.0 : 1.0;
    final CapFloorCMSDefinition[] coupons = new CapFloorCMSDefinition[paymentDates.length];
    coupons[0] = CapFloorCMSDefinition.from(paymentDates[0], settlementDate, paymentDates[0], dayCount.yearFraction(settlementDate, paymentDates[0], calendar),
        sign * notional, index, strike, isCap, calendar);
    for (int loopcpn = 1; loopcpn < paymentDates.length; loopcpn++) {
      coupons[loopcpn] = CapFloorCMSDefinition.from(paymentDates[loopcpn], paymentDates[loopcpn - 1], paymentDates[loopcpn],
          dayCount.yearFraction(paymentDates[loopcpn - 1], paymentDates[loopcpn], calendar), sign * notional, index, strike, isCap, calendar);
    }
    return new AnnuityCapFloorCMSDefinition(coupons, calendar);
  }

}

/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.annuity;

import java.time.Period;
import java.time.ZonedDateTime;

import com.opengamma.analytics.convention.daycount.DayCount;
import com.opengamma.analytics.convention.daycount.DayCountUtils;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.analytics.financial.instrument.payment.CapFloorCMSSpreadDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.collect.ArgChecker;

/**
 * A wrapper class for a AnnuityDefinition containing CMS spread cap/floor Definition.
 */
public class AnnuityCapFloorCMSSpreadDefinition extends AnnuityDefinition<CapFloorCMSSpreadDefinition> {

  /**
   * Constructor from a list of CMS coupons.
   * @param payments The CMS coupons.
   * @param calendar The calendar.
   */
  public AnnuityCapFloorCMSSpreadDefinition(final CapFloorCMSSpreadDefinition[] payments, final HolidayCalendar calendar) {
    super(payments, calendar);
  }

  /**
   * CMS spread cap/floor (or leg of CMS spread caplet/floorlet) constructor from standard description. The cap/floor have fixing in advance and payment in arrears.
   * The CMS fixing is done at a Ibor lag before the coupon start.
   * @param settlementDate The settlement date.
   * @param maturityDate The annuity maturity date.
   * @param notional The notional.
   * @param index1 The first CMS index.
   * @param index2 The second CMS index.
   * @param paymentPeriod The payment period of the coupons.
   * @param dayCount The day count of the coupons.
   * @param isPayer Payer (true) / receiver (false) flag.
   * @param strike The common strike.
   * @param isCap The cap (true) / floor (false) flag.
   * @param calendar1 The holiday calendar for the first ibor index leg.
   * @param calendar2 The holiday calendar for the second ibor index leg.
   * @return The CMS coupon leg.
   */
  public static AnnuityCapFloorCMSSpreadDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final double notional, final IndexSwap index1, final IndexSwap index2,
      final Period paymentPeriod, final DayCount dayCount, final boolean isPayer, final double strike, final boolean isCap, final HolidayCalendar calendar1, final HolidayCalendar calendar2) {
    ArgChecker.notNull(settlementDate, "settlement date");
    ArgChecker.notNull(maturityDate, "maturity date");
    ArgChecker.notNull(index1, "First index");
    ArgChecker.notNull(index2, "Second index");
    ArgChecker.isTrue(notional > 0, "notional <= 0");
    ArgChecker.notNull(paymentPeriod, "Payment period");
    final ZonedDateTime[] paymentDatesUnadjusted = ScheduleCalculator.getUnadjustedDateSchedule(settlementDate, maturityDate, paymentPeriod, true, false);
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(paymentDatesUnadjusted, index1.getIborIndex().getBusinessDayConvention(), calendar1,
        false);
    final double sign = isPayer ? -1.0 : 1.0;
    final CapFloorCMSSpreadDefinition[] coupons = new CapFloorCMSSpreadDefinition[paymentDates.length];
    coupons[0] = CapFloorCMSSpreadDefinition.from(paymentDates[0], settlementDate, paymentDates[0], DayCountUtils.yearFraction(dayCount, settlementDate, paymentDates[0], calendar1),
        sign * notional, index1, index2, strike, isCap, calendar1, calendar2);
    for (int loopcpn = 1; loopcpn < paymentDates.length; loopcpn++) {
      coupons[loopcpn] = CapFloorCMSSpreadDefinition.from(paymentDates[loopcpn], paymentDates[loopcpn - 1], paymentDates[loopcpn],
          DayCountUtils.yearFraction(dayCount, paymentDates[loopcpn - 1], paymentDates[loopcpn], calendar1), sign * notional, index1, index2, strike, isCap, calendar1, calendar2);
    }
    return new AnnuityCapFloorCMSSpreadDefinition(coupons, calendar1);
  }
}

/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.annuity;

import static org.testng.AssertJUnit.assertEquals;

import java.time.Period;
import java.time.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.convention.daycount.DayCount;
import com.opengamma.analytics.convention.daycount.DayCounts;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.util.time.DateUtils;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.date.BusinessDayConvention;
import com.opengamma.strata.basics.date.BusinessDayConventions;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.basics.date.HolidayCalendars;


/**
 * Tests related to the construction of cap/floor Ibor legs..
 */
@Test
public class AnnuityCapFloorIborDefinitionTest {
  private static final HolidayCalendar CALENDAR = HolidayCalendars.SAT_SUN;
  private static final Currency CUR = Currency.EUR;
  // Ibor index
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final Period IBOR_TENOR = Period.ofMonths(3);
  private static final int IBOR_SETTLEMENT_DAYS = 2;
  private static final DayCount IBOR_DAY_COUNT = DayCounts.ACT_360;
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, IBOR_TENOR, IBOR_SETTLEMENT_DAYS, IBOR_DAY_COUNT, BUSINESS_DAY, IS_EOM, "Ibor");
  // Annuity
  private static final ZonedDateTime START_DATE = DateUtils.getUTCDate(2011, 3, 17);
  private static final Period ANNUITY_TENOR = Period.ofYears(5);
  private static final ZonedDateTime MATURITY_DATE = START_DATE.plus(ANNUITY_TENOR);
  private static final double NOTIONAL = 100000000; //100m
  private static final Period LEG_PAYMENT_PERIOD = Period.ofMonths(12);
  private static final DayCount LEG_DAY_COUNT = DayCounts.ACT_365F;
  private static final boolean IS_PAYER = true;
  private static final double STRIKE = 0.04;
  private static final boolean IS_CAP = true;
  private static final AnnuityCapFloorIborDefinition CAP = AnnuityCapFloorIborDefinition.from(START_DATE, MATURITY_DATE, NOTIONAL, IBOR_INDEX, IS_PAYER, STRIKE, IS_CAP, CALENDAR);
  private static final AnnuityCapFloorIborDefinition CAP_12 = AnnuityCapFloorIborDefinition.from(START_DATE, MATURITY_DATE, NOTIONAL, IBOR_INDEX, LEG_DAY_COUNT, LEG_PAYMENT_PERIOD, IS_PAYER, STRIKE,
      IS_CAP, CALENDAR);

  @Test
  public void datesStandard() {
    final AnnuityCouponIborDefinition iborLeg = AnnuityCouponIborDefinition.from(START_DATE, MATURITY_DATE, NOTIONAL, IBOR_INDEX, IS_PAYER, CALENDAR);
    for (int loopcpn = 0; loopcpn < iborLeg.getNumberOfPayments(); loopcpn++) {
      assertEquals(iborLeg.getNthPayment(loopcpn).getAccrualStartDate(), CAP.getNthPayment(loopcpn).getAccrualStartDate());
      assertEquals(iborLeg.getNthPayment(loopcpn).getAccrualEndDate(), CAP.getNthPayment(loopcpn).getAccrualEndDate());
      assertEquals(iborLeg.getNthPayment(loopcpn).getPaymentYearFraction(), CAP.getNthPayment(loopcpn).getPaymentYearFraction());
      assertEquals(iborLeg.getNthPayment(loopcpn).getPaymentDate(), CAP.getNthPayment(loopcpn).getPaymentDate());
      assertEquals(iborLeg.getNthPayment(loopcpn).getFixingDate(), CAP.getNthPayment(loopcpn).getFixingDate());
    }
  }

  @Test
  public void commonStandard() {
    final AnnuityCouponIborDefinition iborLeg = AnnuityCouponIborDefinition.from(START_DATE, MATURITY_DATE, NOTIONAL, IBOR_INDEX, IS_PAYER, CALENDAR);
    for (int loopcpn = 0; loopcpn < iborLeg.getNumberOfPayments(); loopcpn++) {
      assertEquals(STRIKE, CAP.getNthPayment(loopcpn).getStrike());
      assertEquals(IS_CAP, CAP.getNthPayment(loopcpn).isCap());
    }
    final AnnuityCapFloorIborDefinition floor = AnnuityCapFloorIborDefinition.from(START_DATE, MATURITY_DATE, NOTIONAL, IBOR_INDEX, IS_PAYER, STRIKE, !IS_CAP, CALENDAR);
    for (int loopcpn = 0; loopcpn < iborLeg.getNumberOfPayments(); loopcpn++) {
      assertEquals(STRIKE, floor.getNthPayment(loopcpn).getStrike());
      assertEquals(!IS_CAP, floor.getNthPayment(loopcpn).isCap());
    }
    final AnnuityCapFloorIborDefinition capShort = AnnuityCapFloorIborDefinition.from(START_DATE, MATURITY_DATE, NOTIONAL, IBOR_INDEX, !IS_PAYER, STRIKE, IS_CAP, CALENDAR);
    for (int loopcpn = 0; loopcpn < iborLeg.getNumberOfPayments(); loopcpn++) {
      assertEquals(STRIKE, capShort.getNthPayment(loopcpn).getStrike());
      assertEquals(-NOTIONAL * (IS_PAYER ? -1.0 : 1.0), capShort.getNthPayment(loopcpn).getNotional());
    }
  }

  @Test
  public void datesCustom() {
    final IborIndex fakeIborIndex12 = new IborIndex(CUR, LEG_PAYMENT_PERIOD, IBOR_SETTLEMENT_DAYS, LEG_DAY_COUNT, BUSINESS_DAY, IS_EOM, "Ibor");
    final AnnuityCouponIborDefinition iborLeg = AnnuityCouponIborDefinition.from(START_DATE, MATURITY_DATE, NOTIONAL, fakeIborIndex12, IS_PAYER, CALENDAR);
    for (int loopcpn = 0; loopcpn < iborLeg.getNumberOfPayments(); loopcpn++) {
      assertEquals(iborLeg.getNthPayment(loopcpn).getAccrualStartDate(), CAP_12.getNthPayment(loopcpn).getAccrualStartDate());
      assertEquals(iborLeg.getNthPayment(loopcpn).getAccrualEndDate(), CAP_12.getNthPayment(loopcpn).getAccrualEndDate());
      assertEquals(iborLeg.getNthPayment(loopcpn).getPaymentYearFraction(), CAP_12.getNthPayment(loopcpn).getPaymentYearFraction());
      assertEquals(iborLeg.getNthPayment(loopcpn).getPaymentDate(), CAP_12.getNthPayment(loopcpn).getPaymentDate());
      assertEquals(iborLeg.getNthPayment(loopcpn).getFixingDate(), CAP_12.getNthPayment(loopcpn).getFixingDate());
    }
  }

  @Test
  public void commonCustom() {
    for (int loopcpn = 0; loopcpn < CAP_12.getNumberOfPayments(); loopcpn++) {
      assertEquals(STRIKE, CAP_12.getNthPayment(loopcpn).getStrike());
      assertEquals(IS_CAP, CAP_12.getNthPayment(loopcpn).isCap());
    }
    final AnnuityCapFloorIborDefinition floor = AnnuityCapFloorIborDefinition.from(START_DATE, MATURITY_DATE, NOTIONAL, IBOR_INDEX, LEG_DAY_COUNT, LEG_PAYMENT_PERIOD, IS_PAYER, STRIKE, !IS_CAP, CALENDAR);
    for (int loopcpn = 0; loopcpn < CAP_12.getNumberOfPayments(); loopcpn++) {
      assertEquals(STRIKE, floor.getNthPayment(loopcpn).getStrike());
      assertEquals(!IS_CAP, floor.getNthPayment(loopcpn).isCap());
    }
  }

}

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
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.analytics.util.time.DateUtils;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.date.BusinessDayConvention;
import com.opengamma.strata.basics.date.BusinessDayConventions;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.basics.date.HolidayCalendars;


/**
 * Tests related to the construction of CMS legs.
 */
@Test
public class AnnuityCouponCMSDefinitionTest {
  private static final Currency CUR = Currency.EUR;
  private static final HolidayCalendar CALENDAR = HolidayCalendars.SAT_SUN;
  // Ibor index
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final Period IBOR_TENOR = Period.ofMonths(3);
  private static final int IBOR_SETTLEMENT_DAYS = 2;
  private static final DayCount IBOR_DAY_COUNT = DayCounts.ACT_360;
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, IBOR_TENOR, IBOR_SETTLEMENT_DAYS, IBOR_DAY_COUNT, BUSINESS_DAY, IS_EOM, "Ibor");
  //CMS 10Y
  private static final Period CMS_TENOR = Period.ofYears(10);
  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(6);
  private static final DayCount FIXED_DAY_COUNT = DayCounts.THIRTY_U_360;
  private static final IndexSwap CMS_INDEX = new IndexSwap(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, IBOR_INDEX, CMS_TENOR, CALENDAR);
  // Annuity
  private static final ZonedDateTime START_DATE = DateUtils.getUTCDate(2011, 3, 17);
  private static final Period ANNUITY_TENOR = Period.ofYears(5);
  private static final ZonedDateTime MATURITY_DATE = START_DATE.plus(ANNUITY_TENOR);
  private static final double NOTIONAL = 100000000; //100m
  private static final Period LEG_PAYMENT_PERIOD = Period.ofMonths(12);
  private static final DayCount LEG_DAY_COUNT = DayCounts.ACT_365;
  private static final boolean IS_PAYER = true;
  private static final AnnuityCouponCMSDefinition CMS_LEG = AnnuityCouponCMSDefinition.from(START_DATE, MATURITY_DATE, NOTIONAL, CMS_INDEX, LEG_PAYMENT_PERIOD, LEG_DAY_COUNT, IS_PAYER, CALENDAR);

  @Test
  public void dates() {
    final IborIndex fakeIborIndex12 = new IborIndex(CUR, LEG_PAYMENT_PERIOD, IBOR_SETTLEMENT_DAYS, LEG_DAY_COUNT, BUSINESS_DAY, IS_EOM, "Ibor");
    final AnnuityCouponIborDefinition iborLeg = AnnuityCouponIborDefinition.from(START_DATE, MATURITY_DATE, NOTIONAL, fakeIborIndex12, IS_PAYER, CALENDAR);
    for (int loopcpn = 0; loopcpn < iborLeg.getNumberOfPayments(); loopcpn++) {
      assertEquals(iborLeg.getNthPayment(loopcpn).getAccrualStartDate(), CMS_LEG.getNthPayment(loopcpn).getAccrualStartDate());
      assertEquals(iborLeg.getNthPayment(loopcpn).getAccrualEndDate(), CMS_LEG.getNthPayment(loopcpn).getAccrualEndDate());
      assertEquals(iborLeg.getNthPayment(loopcpn).getPaymentYearFraction(), CMS_LEG.getNthPayment(loopcpn).getPaymentYearFraction());
      assertEquals(iborLeg.getNthPayment(loopcpn).getPaymentDate(), CMS_LEG.getNthPayment(loopcpn).getPaymentDate());
      assertEquals(iborLeg.getNthPayment(loopcpn).getFixingDate(), CMS_LEG.getNthPayment(loopcpn).getFixingDate());
    }
  }

  @Test
  public void index() {
    for (int loopcpn = 0; loopcpn < CMS_LEG.getNumberOfPayments(); loopcpn++) {
      assertEquals(CMS_INDEX, CMS_LEG.getNthPayment(loopcpn).getCMSIndex());
      assertEquals(NOTIONAL * (IS_PAYER ? -1.0 : 1.0), CMS_LEG.getNthPayment(loopcpn).getNotional());
    }
    final AnnuityCouponCMSDefinition cmsLegReceive = AnnuityCouponCMSDefinition.from(START_DATE, MATURITY_DATE, NOTIONAL, CMS_INDEX, LEG_PAYMENT_PERIOD, LEG_DAY_COUNT, !IS_PAYER, CALENDAR);
    for (int loopcpn = 0; loopcpn < CMS_LEG.getNumberOfPayments(); loopcpn++) {
      assertEquals(CMS_INDEX, cmsLegReceive.getNthPayment(loopcpn).getCMSIndex());
      assertEquals(-NOTIONAL * (IS_PAYER ? -1.0 : 1.0), cmsLegReceive.getNthPayment(loopcpn).getNotional());
    }
  }

}

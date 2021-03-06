/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.definition;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import java.time.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.convention.daycount.DayCount;
import com.opengamma.analytics.convention.daycount.DayCountUtils;
import com.opengamma.analytics.convention.daycount.DayCounts;
import com.opengamma.analytics.convention.yield.YieldConvention;
import com.opengamma.analytics.convention.yield.YieldConventionFactory;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.DateUtils;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.basics.date.HolidayCalendars;

/**
 * Tests related to the construction of bills security.
 */
@Test
public class BillSecurityTest {

  private final static Currency EUR = Currency.EUR;
  private static final HolidayCalendar CALENDAR = HolidayCalendars.SAT_SUN;
  private final static ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2012, 1, 16);

  private static final DayCount ACT360 = DayCounts.ACT_360;
  private static final int SETTLEMENT_DAYS = 2;
  private static final YieldConvention YIELD_CONVENTION = YieldConventionFactory.INSTANCE.getYieldConvention("INTEREST@MTY");

  private final static String ISSUER_BEL = "BELGIUM GOVT";
  private final static String ISSUER_GER = "GERMANY GOVT";
  private final static ZonedDateTime END_DATE = DateUtils.getUTCDate(2012, 2, 29);
  private final static double NOTIONAL = 1000;

  private final static ZonedDateTime SETTLE_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, SETTLEMENT_DAYS, CALENDAR);
  private final static double SETTLE_TIME = TimeCalculator.getTimeBetween(REFERENCE_DATE, SETTLE_DATE);
  private final static double END_TIME = TimeCalculator.getTimeBetween(REFERENCE_DATE, END_DATE);
  private final static double ACCRUAL_FACTOR = DayCountUtils.yearFraction(ACT360, SETTLE_DATE, END_DATE);
  private final static BillSecurity BILL_SEC = new BillSecurity(EUR, SETTLE_TIME, END_TIME, NOTIONAL, YIELD_CONVENTION, ACCRUAL_FACTOR, ISSUER_BEL);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCurrency() {
    new BillSecurity(null, SETTLE_TIME, END_TIME, NOTIONAL, YIELD_CONVENTION, ACCRUAL_FACTOR, ISSUER_BEL);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullYield() {
    new BillSecurity(EUR, SETTLE_TIME, END_TIME, NOTIONAL, null, ACCRUAL_FACTOR, ISSUER_BEL);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullIssue1() {
    new BillSecurity(EUR, SETTLE_TIME, END_TIME, NOTIONAL, YIELD_CONVENTION, ACCRUAL_FACTOR, (String) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullIssuer2() {
    new BillSecurity(EUR, SETTLE_TIME, END_TIME, NOTIONAL, YIELD_CONVENTION, ACCRUAL_FACTOR, (LegalEntity) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void notionalPositive() {
    new BillSecurity(EUR, SETTLE_TIME, END_TIME, -NOTIONAL, YIELD_CONVENTION, ACCRUAL_FACTOR, ISSUER_BEL);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void times() {
    new BillSecurity(EUR, END_TIME, SETTLE_TIME, NOTIONAL, YIELD_CONVENTION, ACCRUAL_FACTOR, ISSUER_BEL);
  }

  @Test
  /**
   * Tests the bill getters.
   */
  public void getters() {
    assertEquals("Bill Security: getter", EUR, BILL_SEC.getCurrency());
    assertEquals("Bill Security: getter", SETTLE_TIME, BILL_SEC.getSettlementTime());
    assertEquals("Bill Security: getter", END_TIME, BILL_SEC.getEndTime());
    assertEquals("Bill Security: getter", NOTIONAL, BILL_SEC.getNotional());
    assertEquals("Bill Security: getter", YIELD_CONVENTION, BILL_SEC.getYieldConvention());
    assertEquals("Bill Security: getter", ACCRUAL_FACTOR, BILL_SEC.getAccrualFactor());
    assertEquals("Bill Security: getter", new LegalEntity(null, ISSUER_BEL, null, null, null), BILL_SEC.getIssuerEntity());
  }

  @Test
  /**
   * Tests the equal and hash-code methods.
   */
  public void equalHash() {
    assertEquals("Bill Security: equal-hash code", BILL_SEC, BILL_SEC);
    final BillSecurity other = new BillSecurity(EUR, SETTLE_TIME, END_TIME, NOTIONAL, YIELD_CONVENTION, ACCRUAL_FACTOR, ISSUER_BEL);
    assertEquals("Bill Security: equal-hash code", BILL_SEC, other);
    assertEquals("Bill Security: equal-hash code", BILL_SEC.hashCode(), other.hashCode());
    BillSecurity modified;
    modified = new BillSecurity(Currency.USD, SETTLE_TIME, END_TIME, NOTIONAL, YIELD_CONVENTION, ACCRUAL_FACTOR, ISSUER_BEL);
    assertFalse("Bill Security: equal-hash code", BILL_SEC.equals(modified));
    modified = new BillSecurity(EUR, SETTLE_TIME + 0.01, END_TIME, NOTIONAL, YIELD_CONVENTION, ACCRUAL_FACTOR, ISSUER_BEL);
    assertFalse("Bill Security: equal-hash code", BILL_SEC.equals(modified));
    modified = new BillSecurity(EUR, SETTLE_TIME, END_TIME + 0.01, NOTIONAL, YIELD_CONVENTION, ACCRUAL_FACTOR, ISSUER_BEL);
    assertFalse("Bill Security: equal-hash code", BILL_SEC.equals(modified));
    modified = new BillSecurity(EUR, SETTLE_TIME, END_TIME, NOTIONAL + 1.0, YIELD_CONVENTION, ACCRUAL_FACTOR, ISSUER_BEL);
    assertFalse("Bill Security: equal-hash code", BILL_SEC.equals(modified));
    modified = new BillSecurity(EUR, SETTLE_TIME, END_TIME, NOTIONAL, YieldConventionFactory.INSTANCE.getYieldConvention("DISCOUNT"), ACCRUAL_FACTOR, ISSUER_BEL);
    assertFalse("Bill Security: equal-hash code", BILL_SEC.equals(modified));
    modified = new BillSecurity(EUR, SETTLE_TIME, END_TIME, NOTIONAL, YIELD_CONVENTION, ACCRUAL_FACTOR + 0.05, ISSUER_BEL);
    assertFalse("Bill Security: equal-hash code", BILL_SEC.equals(modified));
    modified = new BillSecurity(EUR, SETTLE_TIME, END_TIME, NOTIONAL, YIELD_CONVENTION, ACCRUAL_FACTOR, ISSUER_GER);
    assertFalse("Bill Security: equal-hash code", BILL_SEC.equals(modified));
  }

}

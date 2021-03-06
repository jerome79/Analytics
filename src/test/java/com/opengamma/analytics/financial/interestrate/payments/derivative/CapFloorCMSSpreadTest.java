/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.derivative;

import static org.testng.AssertJUnit.assertEquals;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.convention.daycount.DayCount;
import com.opengamma.analytics.convention.daycount.DayCountUtils;
import com.opengamma.analytics.convention.daycount.DayCounts;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.util.time.DateUtils;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.date.BusinessDayConvention;
import com.opengamma.strata.basics.date.BusinessDayConventions;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.basics.date.HolidayCalendars;

/**
 * Tests related to the construction of CapFloorCMSSpread.
 */
@Test
public class CapFloorCMSSpreadTest {

  //Swaps
  private static final Currency CUR = Currency.EUR;
  private static final HolidayCalendar CALENDAR = HolidayCalendars.SAT_SUN;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2011, 3, 17);
  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(6);
  private static final DayCount FIXED_DAY_COUNT = DayCounts.THIRTY_U_360;
  private static final boolean FIXED_IS_PAYER = true; // Irrelevant for the underlying
  private static final double RATE = 0.0; // Irrelevant for the underlying
  private static final Period INDEX_TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final DayCount DAY_COUNT = DayCounts.ACT_360;
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, INDEX_TENOR, SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, IS_EOM, "Ibor");
  // Swap 10Y
  private static final Period ANNUITY_TENOR_1 = Period.ofYears(10);
  private static final IndexSwap CMS_INDEX_1 = new IndexSwap(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, IBOR_INDEX, ANNUITY_TENOR_1, CALENDAR);
  private static final SwapFixedIborDefinition SWAP_DEFINITION_1 = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX_1, 1.0, RATE, FIXED_IS_PAYER, CALENDAR);
  // Swap 2Y
  private static final Period ANNUITY_TENOR_2 = Period.ofYears(2);
  private static final IndexSwap CMS_INDEX_2 = new IndexSwap(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, IBOR_INDEX, ANNUITY_TENOR_2, CALENDAR);
  private static final SwapFixedIborDefinition SWAP_DEFINITION_2 = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX_2, 1.0, RATE, FIXED_IS_PAYER, CALENDAR);
  // CMS spread coupon
  private static final double NOTIONAL = 10000000;
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2011, 4, 6);
  private static final ZonedDateTime FIXING_DATE = DateUtils.getUTCDate(2010, 12, 30);
  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtils.getUTCDate(2011, 1, 5);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtils.getUTCDate(2011, 4, 5);
  private static final DayCount PAYMENT_DAY_COUNT = DayCounts.ACT_360;
  private static final double PAYMENT_ACCRUAL_FACTOR = DayCountUtils.yearFraction(PAYMENT_DAY_COUNT, ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double STRIKE = 0.0050; // 50 bps
  private static final boolean IS_CAP = true;
  // to derivatives
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 8, 18);
  private static final SwapFixedCoupon<? extends Payment> SWAP_1 = SWAP_DEFINITION_1.toDerivative(REFERENCE_DATE);
  private static final SwapFixedCoupon<? extends Payment> SWAP_2 = SWAP_DEFINITION_2.toDerivative(REFERENCE_DATE);
  private static final DayCount ACT_ACT = DayCounts.ACT_ACT_ISDA;
  private static final ZonedDateTime REFERENCE_DATE_ZONED = ZonedDateTime.of(LocalDateTime.of(REFERENCE_DATE.toLocalDate(), LocalTime.MIDNIGHT), ZoneOffset.UTC);
  private static final double PAYMENT_TIME = DayCountUtils.yearFraction(ACT_ACT, REFERENCE_DATE_ZONED, PAYMENT_DATE);
  private static final double FIXING_TIME = DayCountUtils.yearFraction(ACT_ACT, REFERENCE_DATE_ZONED, FIXING_DATE);
  private static final double SETTLEMENT_TIME = DayCountUtils.yearFraction(ACT_ACT, REFERENCE_DATE_ZONED, SWAP_DEFINITION_1.getFixedLeg().getNthPayment(0).getAccrualStartDate());

  private static final CapFloorCMSSpread CMS_SPREAD = new CapFloorCMSSpread(CUR, PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, SWAP_1, CMS_INDEX_1, SWAP_2, CMS_INDEX_2,
      SETTLEMENT_TIME, STRIKE, IS_CAP);

  @Test
  public void testGetter() {
    assertEquals(SWAP_1, CMS_SPREAD.getUnderlyingSwap1());
    assertEquals(CMS_INDEX_1, CMS_SPREAD.getCmsIndex1());
    assertEquals(SWAP_2, CMS_SPREAD.getUnderlyingSwap2());
    assertEquals(CMS_INDEX_2, CMS_SPREAD.getCmsIndex2());
    assertEquals(STRIKE, CMS_SPREAD.getStrike(), 1E-10);
    assertEquals(IS_CAP, CMS_SPREAD.isCap());
  }

  @Test
  public void testWithNotional() {
    final double notional = NOTIONAL + 1000;
    final CapFloorCMSSpread cmsSpread = new CapFloorCMSSpread(CUR, PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, notional, FIXING_TIME, SWAP_1, CMS_INDEX_1, SWAP_2, CMS_INDEX_2, SETTLEMENT_TIME, STRIKE,
        IS_CAP);
    assertEquals(cmsSpread, CMS_SPREAD.withNotional(notional));
  }

  @Test
  public void testEqualHash() {
    final CapFloorCMSSpread newCMSSpread = new CapFloorCMSSpread(CUR, PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, SWAP_1, CMS_INDEX_1, SWAP_2, CMS_INDEX_2, SETTLEMENT_TIME, STRIKE,
        IS_CAP);
    assertEquals(newCMSSpread.equals(CMS_SPREAD), true);
    assertEquals(newCMSSpread.hashCode() == CMS_SPREAD.hashCode(), true);
    final Currency newCur = Currency.USD;
    CapFloorCMSSpread modifiedCMSSpread;
    modifiedCMSSpread = new CapFloorCMSSpread(newCur, PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, SWAP_1, CMS_INDEX_1, SWAP_2, CMS_INDEX_2, SETTLEMENT_TIME, STRIKE, IS_CAP);
    assertEquals(modifiedCMSSpread.equals(CMS_SPREAD), false);
    modifiedCMSSpread = new CapFloorCMSSpread(CUR, PAYMENT_TIME + 1.0, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, SWAP_1, CMS_INDEX_1, SWAP_2, CMS_INDEX_2, SETTLEMENT_TIME, STRIKE, IS_CAP);
    assertEquals(modifiedCMSSpread.equals(CMS_SPREAD), false);
    modifiedCMSSpread = new CapFloorCMSSpread(CUR, PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR + 1.0, NOTIONAL, FIXING_TIME, SWAP_1, CMS_INDEX_1, SWAP_2, CMS_INDEX_2, SETTLEMENT_TIME, STRIKE, IS_CAP);
    assertEquals(modifiedCMSSpread.equals(CMS_SPREAD), false);
    modifiedCMSSpread = new CapFloorCMSSpread(CUR, PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL + 1.0, FIXING_TIME, SWAP_1, CMS_INDEX_1, SWAP_2, CMS_INDEX_2, SETTLEMENT_TIME, STRIKE, IS_CAP);
    assertEquals(modifiedCMSSpread.equals(CMS_SPREAD), false);
    modifiedCMSSpread = new CapFloorCMSSpread(CUR, PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME + 1.0, SWAP_1, CMS_INDEX_1, SWAP_2, CMS_INDEX_2, SETTLEMENT_TIME, STRIKE, IS_CAP);
    assertEquals(modifiedCMSSpread.equals(CMS_SPREAD), false);
    modifiedCMSSpread = new CapFloorCMSSpread(CUR, PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, SWAP_1, CMS_INDEX_1, SWAP_2, CMS_INDEX_2, SETTLEMENT_TIME + 1.0, STRIKE, IS_CAP);
    assertEquals(modifiedCMSSpread.equals(CMS_SPREAD), false);
    modifiedCMSSpread = new CapFloorCMSSpread(CUR, PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, SWAP_1, CMS_INDEX_1, SWAP_2, CMS_INDEX_2, SETTLEMENT_TIME, STRIKE + 1.0, IS_CAP);
    assertEquals(modifiedCMSSpread.equals(CMS_SPREAD), false);
    modifiedCMSSpread = new CapFloorCMSSpread(CUR, PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, SWAP_1, CMS_INDEX_1, SWAP_2, CMS_INDEX_2, SETTLEMENT_TIME, STRIKE, !IS_CAP);
    assertEquals(modifiedCMSSpread.equals(CMS_SPREAD), false);
    modifiedCMSSpread = new CapFloorCMSSpread(CUR, PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, SWAP_2, CMS_INDEX_1, SWAP_2, CMS_INDEX_2, SETTLEMENT_TIME, STRIKE, IS_CAP);
    assertEquals(modifiedCMSSpread.equals(CMS_SPREAD), false);
    modifiedCMSSpread = new CapFloorCMSSpread(CUR, PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, SWAP_1, CMS_INDEX_2, SWAP_2, CMS_INDEX_2, SETTLEMENT_TIME, STRIKE, IS_CAP);
    assertEquals(modifiedCMSSpread.equals(CMS_SPREAD), false);
    modifiedCMSSpread = new CapFloorCMSSpread(CUR, PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, SWAP_1, CMS_INDEX_1, SWAP_1, CMS_INDEX_2, SETTLEMENT_TIME, STRIKE, IS_CAP);
    assertEquals(modifiedCMSSpread.equals(CMS_SPREAD), false);
    modifiedCMSSpread = new CapFloorCMSSpread(CUR, PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, SWAP_1, CMS_INDEX_1, SWAP_2, CMS_INDEX_1, SETTLEMENT_TIME, STRIKE, IS_CAP);
    assertEquals(modifiedCMSSpread.equals(CMS_SPREAD), false);
  }
}

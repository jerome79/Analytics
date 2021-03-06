package com.opengamma.analytics.financial.instrument.payment;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import java.time.Period;
import java.time.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.convention.daycount.DayCountUtils;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.index.IndexONMaster;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONArithmeticAverageSpreadSimplified;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.DateUtils;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.date.BusinessDayConvention;
import com.opengamma.strata.basics.date.BusinessDayConventions;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.basics.date.HolidayCalendars;

/**
 * Test.
 */
@Test
public class CouponONArithmeticAverageSpreadSimplifiedDefinitionTest {
  private static final int US_SETTLEMENT_DAYS = 2;
  private static final BusinessDayConvention MOD_FOL = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final HolidayCalendar NYC = HolidayCalendars.SAT_SUN;
  private static final IndexON FEDFUND = IndexONMaster.getInstance().getIndex("FED FUND");
  private static final IborIndex USDLIBOR3M = IndexIborMaster.getInstance().getIndex("USDLIBOR3M");
  private static final ZonedDateTime TRADE_DATE = DateUtils.getUTCDate(2011, 5, 23);
  private static final ZonedDateTime SPOT_DATE = ScheduleCalculator.getAdjustedDate(TRADE_DATE, US_SETTLEMENT_DAYS, NYC);
  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtils.getUTCDate(2011, 5, 23);
  private static final Period TENOR_3M = Period.ofMonths(3);
  private static final double NOTIONAL = 100000000; // 100m
  private static final double SPREAD = 0.0010; // 10 bps
  private static final int PAYMENT_LAG = 2;
  private static final ZonedDateTime ACCRUAL_END_DATE = ScheduleCalculator.getAdjustedDate(ACCRUAL_START_DATE, TENOR_3M, USDLIBOR3M, NYC);
  private static final double ACCURAL_FACTOR = DayCountUtils.yearFraction(USDLIBOR3M.getDayCount(), ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final ZonedDateTime PAYMENT_DATE = ScheduleCalculator.getAdjustedDate(ACCRUAL_END_DATE, -1 + FEDFUND.getPublicationLag() + PAYMENT_LAG, NYC);
  private static final CouponONArithmeticAverageSpreadSimplifiedDefinition FEDFUND_CPN_3M_DEF = new CouponONArithmeticAverageSpreadSimplifiedDefinition(Currency.USD, PAYMENT_DATE, ACCRUAL_START_DATE,
      ACCRUAL_END_DATE, ACCURAL_FACTOR, NOTIONAL, FEDFUND, SPREAD);

  private static final CouponONArithmeticAverageSpreadSimplifiedDefinition FEDFUND_CPN_3M_FROM_DEF = CouponONArithmeticAverageSpreadSimplifiedDefinition.from(FEDFUND, ACCRUAL_START_DATE, TENOR_3M,
      NOTIONAL, PAYMENT_LAG, MOD_FOL, true, SPREAD, NYC);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCurrency() {
    new CouponONArithmeticAverageSpreadSimplifiedDefinition(null, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCURAL_FACTOR, NOTIONAL, FEDFUND, SPREAD);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullPayDate() {
    new CouponONArithmeticAverageSpreadSimplifiedDefinition(Currency.USD, null, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCURAL_FACTOR, NOTIONAL, FEDFUND, SPREAD);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullStartAccural() {
    new CouponONArithmeticAverageSpreadSimplifiedDefinition(Currency.USD, PAYMENT_DATE, null, ACCRUAL_END_DATE, ACCURAL_FACTOR, NOTIONAL, FEDFUND, SPREAD);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullEndAccural() {
    new CouponONArithmeticAverageSpreadSimplifiedDefinition(Currency.USD, PAYMENT_DATE, ACCRUAL_START_DATE, null, ACCURAL_FACTOR, NOTIONAL, FEDFUND, SPREAD);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullIndex() {
    new CouponONArithmeticAverageSpreadSimplifiedDefinition(Currency.USD, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCURAL_FACTOR, NOTIONAL, null, SPREAD);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongCurrency() {
    new CouponONArithmeticAverageSpreadSimplifiedDefinition(Currency.AUD, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCURAL_FACTOR, NOTIONAL, FEDFUND, SPREAD);
  }

  @Test
  public void getter() {
    assertEquals("CouponArithmeticAverageONSpreadSimplifiedDefinition: getter", FEDFUND_CPN_3M_DEF.getPaymentDate(), PAYMENT_DATE);
    assertEquals("CouponArithmeticAverageONSpreadSimplifiedDefinition: getter", FEDFUND_CPN_3M_DEF.getAccrualStartDate(), ACCRUAL_START_DATE);
    assertEquals("CouponArithmeticAverageONSpreadSimplifiedDefinition: getter", FEDFUND_CPN_3M_DEF.getAccrualEndDate(), ACCRUAL_END_DATE);
    assertEquals("CouponArithmeticAverageONSpreadSimplifiedDefinition: getter", FEDFUND_CPN_3M_DEF.getCurrency(), FEDFUND.getCurrency());
    assertEquals("CouponArithmeticAverageONSpreadSimplifiedDefinition: getter", FEDFUND_CPN_3M_DEF.getIndex(), FEDFUND);
    assertEquals("CouponArithmeticAverageONSpreadSimplifiedDefinition: getter", FEDFUND_CPN_3M_DEF.getSpread(), SPREAD);
    assertEquals("CouponArithmeticAverageONSpreadSimplifiedDefinition: getter", FEDFUND_CPN_3M_DEF.getSpreadAmount(), SPREAD * ACCURAL_FACTOR * NOTIONAL);
  }

  @Test
  public void from() {
    assertEquals("CouponArithmeticAverageONSpreadDefinition: from", FEDFUND_CPN_3M_DEF, FEDFUND_CPN_3M_FROM_DEF);
  }

  @Test
  public void equalHash() {
    assertEquals("CouponArithmeticAverageON: equal-hash", FEDFUND_CPN_3M_DEF, FEDFUND_CPN_3M_DEF);
    final CouponONArithmeticAverageSpreadSimplifiedDefinition other = CouponONArithmeticAverageSpreadSimplifiedDefinition.from(FEDFUND, ACCRUAL_START_DATE, TENOR_3M,
        NOTIONAL, PAYMENT_LAG, MOD_FOL, true, SPREAD, NYC);
    assertEquals("CouponArithmeticAverageON: equal-hash", FEDFUND_CPN_3M_DEF, other);
    assertEquals("CouponArithmeticAverageON: equal-hash", FEDFUND_CPN_3M_DEF.hashCode(), other.hashCode());
    CouponONArithmeticAverageSpreadSimplifiedDefinition modified;
    final IndexON modifiedIndex = IndexONMaster.getInstance().getIndex("EONIA");
    modified = CouponONArithmeticAverageSpreadSimplifiedDefinition.from(modifiedIndex, ACCRUAL_START_DATE, TENOR_3M, NOTIONAL, PAYMENT_LAG, MOD_FOL, true, SPREAD, NYC);
    assertFalse("CouponArithmeticAverageON: equal-hash", FEDFUND_CPN_3M_DEF.equals(modified));
    modified = CouponONArithmeticAverageSpreadSimplifiedDefinition.from(FEDFUND, ACCRUAL_START_DATE.plusDays(1), TENOR_3M, NOTIONAL, PAYMENT_LAG, MOD_FOL, true, SPREAD, NYC);
    assertFalse("CouponArithmeticAverageON: equal-hash", FEDFUND_CPN_3M_DEF.equals(modified));
    modified = new CouponONArithmeticAverageSpreadSimplifiedDefinition(Currency.USD, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE.plusDays(1), ACCURAL_FACTOR, NOTIONAL, FEDFUND, SPREAD);
    assertFalse("CouponArithmeticAverageON: equal-hash", FEDFUND_CPN_3M_DEF.equals(modified));
    modified = CouponONArithmeticAverageSpreadSimplifiedDefinition.from(FEDFUND, ACCRUAL_START_DATE, ACCRUAL_END_DATE, NOTIONAL + 1000, PAYMENT_LAG, SPREAD, NYC);
    assertFalse("CouponArithmeticAverageON: equal-hash", FEDFUND_CPN_3M_DEF.equals(modified));
    modified = CouponONArithmeticAverageSpreadSimplifiedDefinition.from(FEDFUND, ACCRUAL_START_DATE, ACCRUAL_END_DATE, NOTIONAL, PAYMENT_LAG + 1, SPREAD, NYC);
    assertFalse("CouponArithmeticAverageON: equal-hash", FEDFUND_CPN_3M_DEF.equals(modified));
    modified = CouponONArithmeticAverageSpreadSimplifiedDefinition.from(FEDFUND, ACCRUAL_START_DATE, ACCRUAL_END_DATE, NOTIONAL, PAYMENT_LAG, SPREAD + 0.0010, NYC);
    assertFalse("CouponArithmeticAverageON: equal-hash", FEDFUND_CPN_3M_DEF.equals(modified));
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivative() {
    final CouponONArithmeticAverageSpreadSimplified cpnConverted = FEDFUND_CPN_3M_DEF.toDerivative(TRADE_DATE);
    final double paymentTime = TimeCalculator.getTimeBetween(TRADE_DATE, PAYMENT_DATE);
    final double fixingStartTime = TimeCalculator.getTimeBetween(TRADE_DATE, ACCRUAL_START_DATE);
    final double fixingEndTime = TimeCalculator.getTimeBetween(TRADE_DATE, ACCRUAL_END_DATE);
    final CouponONArithmeticAverageSpreadSimplified cpnExpected = CouponONArithmeticAverageSpreadSimplified
        .from(paymentTime, ACCURAL_FACTOR, NOTIONAL, FEDFUND, fixingStartTime, fixingEndTime, FEDFUND_CPN_3M_DEF.getPaymentYearFraction(), SPREAD);
    assertEquals("CouponOISSimplified definition: toDerivative", cpnExpected, cpnConverted);
  }
}

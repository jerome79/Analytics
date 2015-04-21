/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.payment;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.time.Period;
import java.time.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.convention.daycount.DayCount;
import com.opengamma.analytics.convention.daycount.DayCounts;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.DateUtils;
import com.opengamma.analytics.util.timeseries.DoubleTimeSeries;
import com.opengamma.analytics.util.timeseries.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.date.BusinessDayConvention;
import com.opengamma.strata.basics.date.BusinessDayConventions;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.basics.date.HolidayCalendars;


/**
 * Test.
 */
@Test
public class CapFloorIborDefinitionTest {
  private static final Period TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final HolidayCalendar CALENDAR = HolidayCalendars.SAT_SUN;
  private static final DayCount DAY_COUNT_INDEX = DayCounts.ACT_360;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final Currency CUR = Currency.EUR;
  private static final IborIndex INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM, "Ibor");

  private static final ZonedDateTime FIXING_DATE = DateUtils.getUTCDate(2011, 1, 3);
  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtils.getUTCDate(2011, 1, 6);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtils.getUTCDate(2011, 4, 4);
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2011, 4, 6);
  // The above dates are not standard but selected for insure correct testing.
  private static final ZonedDateTime FIXING_START_DATE = ScheduleCalculator.getAdjustedDate(FIXING_DATE, SETTLEMENT_DAYS, CALENDAR);
  private static final ZonedDateTime FIXING_END_DATE = ScheduleCalculator.getAdjustedDate(FIXING_START_DATE, TENOR, BUSINESS_DAY, CALENDAR, IS_EOM);

  private static final DayCount DAY_COUNT_PAYMENT = DayCounts.ACT_365F;
  private static final double ACCRUAL_FACTOR = DAY_COUNT_PAYMENT.yearFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double ACCRUAL_FACTOR_FIXING = DAY_COUNT_INDEX.yearFraction(FIXING_START_DATE, FIXING_END_DATE);
  private static final double NOTIONAL = 1000000; //1m

  private static final double STRIKE = 0.02;
  private static final boolean IS_CAP = true;
  private static final double HIGH_FIXING_RATE = 0.04;
  private static final DoubleTimeSeries<ZonedDateTime> HIGH_FIXING_TS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[]{FIXING_DATE}, new double[]{HIGH_FIXING_RATE});
  private static final double LOW_FIXING_RATE = 0.02;
  private static final DoubleTimeSeries<ZonedDateTime> LOW_FIXING_TS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {FIXING_DATE }, new double[] {LOW_FIXING_RATE });
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 12, 27); //For conversion to derivative

  // Coupon with standard payment and accrual dates.
  private static final CouponIborDefinition IBOR_COUPON_2 = CouponIborDefinition.from(NOTIONAL, FIXING_DATE, INDEX, CALENDAR);
  private static final CapFloorIborDefinition IBOR_CAP = CapFloorIborDefinition.from(PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, INDEX, STRIKE, true, CALENDAR);
  private static final CapFloorIborDefinition IBOR_CAP_2 = CapFloorIborDefinition.from(IBOR_COUPON_2, STRIKE, true);
  private static final CapFloorIborDefinition IBOR_FLOOR = CapFloorIborDefinition.from(PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, INDEX, STRIKE, false, CALENDAR);

  @Test
  public void test() {
    assertEquals(IBOR_CAP.getPaymentDate(), PAYMENT_DATE);
    assertEquals(IBOR_CAP.getAccrualStartDate(), ACCRUAL_START_DATE);
    assertEquals(IBOR_CAP.getAccrualEndDate(), ACCRUAL_END_DATE);
    assertEquals(IBOR_CAP.getPaymentYearFraction(), ACCRUAL_FACTOR, 1E-10);
    assertEquals(IBOR_CAP.getNotional(), NOTIONAL, 1E-2);
    assertEquals(IBOR_CAP.getFixingDate(), FIXING_DATE);
    assertEquals(IBOR_CAP.getFixingPeriodStartDate(), FIXING_START_DATE);
    assertEquals(IBOR_CAP.getFixingPeriodEndDate(), FIXING_END_DATE);
    assertEquals(IBOR_CAP.getFixingPeriodAccrualFactor(), ACCRUAL_FACTOR_FIXING, 1E-10);
    assertTrue(IBOR_CAP.isCap());
    assertEquals(IBOR_CAP.getStrike(), STRIKE);
    assertEquals(IBOR_CAP_2.getPaymentDate(), FIXING_END_DATE);
    assertEquals(IBOR_CAP_2.getAccrualStartDate(), FIXING_START_DATE);
    assertEquals(IBOR_CAP_2.getAccrualEndDate(), FIXING_END_DATE);
    assertEquals(IBOR_CAP_2.getPaymentYearFraction(), ACCRUAL_FACTOR_FIXING, 1E-10);
    assertEquals(IBOR_CAP_2.getNotional(), NOTIONAL, 1E-2);
    assertEquals(IBOR_CAP_2.getFixingDate(), FIXING_DATE);
    assertEquals(IBOR_CAP_2.getFixingPeriodStartDate(), FIXING_START_DATE);
    assertEquals(IBOR_CAP_2.getFixingPeriodEndDate(), FIXING_END_DATE);
    assertEquals(IBOR_CAP_2.getFixingPeriodAccrualFactor(), ACCRUAL_FACTOR_FIXING, 1E-10);
    assertTrue(IBOR_CAP_2.isCap());
    assertEquals(IBOR_CAP_2.getStrike(), STRIKE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPaymentDate() {
    CapFloorIborDefinition.from(null, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, INDEX, STRIKE, IS_CAP, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAccrualStartDate() {
    CapFloorIborDefinition.from(PAYMENT_DATE, null, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, INDEX, STRIKE, IS_CAP, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAccrualEndDate() {
    CapFloorIborDefinition.from(PAYMENT_DATE, ACCRUAL_START_DATE, null, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, INDEX, STRIKE, IS_CAP, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFixingDate() {
    CapFloorIborDefinition.from(PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, null, INDEX, STRIKE, IS_CAP, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIndex() {
    CapFloorIborDefinition.from(PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, null, STRIKE, IS_CAP, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFromNullCoupon() {
    CapFloorIborDefinition.from(null, STRIKE, IS_CAP);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDateInConversion1() {
    IBOR_CAP.toDerivative(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDateInConversion2() {
    IBOR_CAP.toDerivative(null, HIGH_FIXING_TS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDateAfterFixingNoTS() {
    IBOR_CAP.toDerivative(FIXING_DATE.plusDays(3));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTS() {
    IBOR_CAP.toDerivative(FIXING_DATE, (DoubleTimeSeries<ZonedDateTime>) null);
  }

  @Test
  public void testToDerivativeBeforeFixing() {
    final DayCount actAct = DayCounts.ACT_ACT_ISDA;
    final double paymentTime = actAct.yearFraction(REFERENCE_DATE, PAYMENT_DATE);
    final double fixingTime = actAct.yearFraction(REFERENCE_DATE, FIXING_DATE);
    final double fixingPeriodStartTime = actAct.yearFraction(REFERENCE_DATE, IBOR_CAP.getFixingPeriodStartDate());
    final double fixingPeriodEndTime = actAct.yearFraction(REFERENCE_DATE, IBOR_CAP.getFixingPeriodEndDate());
    final CapFloorIbor expectedCapIbor = new CapFloorIbor(CUR, paymentTime, ACCRUAL_FACTOR, NOTIONAL, fixingTime, INDEX, fixingPeriodStartTime, fixingPeriodEndTime,
        ACCRUAL_FACTOR_FIXING, STRIKE, IS_CAP);
    final CapFloorIbor convertedCapIborDefinition = (CapFloorIbor) IBOR_CAP.toDerivative(REFERENCE_DATE);
    assertEquals(expectedCapIbor, convertedCapIborDefinition);
    assertEquals(expectedCapIbor, IBOR_CAP.toDerivative(REFERENCE_DATE, HIGH_FIXING_TS));
  }

  @Test
  public void testToDerivativeAfterFixing() {
    final ZonedDateTime date = FIXING_DATE.plusDays(3);
    final DayCount actAct = DayCounts.ACT_ACT_ISDA;
    double paymentTime = actAct.yearFraction(date, PAYMENT_DATE);
    CouponFixed expectedFixedCoupon = new CouponFixed(CUR, paymentTime, ACCRUAL_FACTOR, NOTIONAL, HIGH_FIXING_RATE - STRIKE);
    assertEquals(expectedFixedCoupon, IBOR_CAP.toDerivative(date, HIGH_FIXING_TS));
    expectedFixedCoupon = new CouponFixed(CUR, paymentTime, ACCRUAL_FACTOR, NOTIONAL, 0);
    assertEquals(expectedFixedCoupon, IBOR_CAP.toDerivative(date, LOW_FIXING_TS));
    expectedFixedCoupon = new CouponFixed(CUR, paymentTime, ACCRUAL_FACTOR, NOTIONAL, 0);
    assertEquals(expectedFixedCoupon, IBOR_FLOOR.toDerivative(date, HIGH_FIXING_TS));
    expectedFixedCoupon = new CouponFixed(CUR, paymentTime, ACCRUAL_FACTOR, NOTIONAL, STRIKE - LOW_FIXING_RATE);
    assertEquals(expectedFixedCoupon, IBOR_FLOOR.toDerivative(date, LOW_FIXING_TS));
    paymentTime = actAct.yearFraction(FIXING_DATE, PAYMENT_DATE);
    expectedFixedCoupon = new CouponFixed(CUR, paymentTime, ACCRUAL_FACTOR, NOTIONAL, HIGH_FIXING_RATE - STRIKE);
    assertEquals(expectedFixedCoupon, IBOR_CAP.toDerivative(FIXING_DATE, HIGH_FIXING_TS));
    expectedFixedCoupon = new CouponFixed(CUR, paymentTime, ACCRUAL_FACTOR, NOTIONAL, 0);
    assertEquals(expectedFixedCoupon, IBOR_CAP.toDerivative(FIXING_DATE, LOW_FIXING_TS));
    expectedFixedCoupon = new CouponFixed(CUR, paymentTime, ACCRUAL_FACTOR, NOTIONAL, 0);
    assertEquals(expectedFixedCoupon, IBOR_FLOOR.toDerivative(FIXING_DATE, HIGH_FIXING_TS));
    expectedFixedCoupon = new CouponFixed(CUR, paymentTime, ACCRUAL_FACTOR, NOTIONAL, STRIKE - LOW_FIXING_RATE);
    assertEquals(expectedFixedCoupon, IBOR_FLOOR.toDerivative(FIXING_DATE, LOW_FIXING_TS));
  }
}

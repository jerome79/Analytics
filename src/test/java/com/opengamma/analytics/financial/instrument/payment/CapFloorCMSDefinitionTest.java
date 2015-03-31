/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.payment;

import static org.testng.AssertJUnit.assertEquals;

import java.time.Period;
import java.time.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorCMS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponCMS;
import com.opengamma.analytics.util.time.DateUtils;
import com.opengamma.analytics.util.timeseries.DoubleTimeSeries;
import com.opengamma.analytics.util.timeseries.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.strata.basics.currency.Currency;


/**
 * Test related to CapFloorCMSDefinition construction.
 */
@Test
public class CapFloorCMSDefinitionTest {
  //Swap 5Y
  private static final Currency CUR = Currency.EUR;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final Period ANNUITY_TENOR = Period.ofYears(5);
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2011, 3, 17);
  //Fixed leg: Semi-annual bond
  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(6);
  private static final DayCount FIXED_DAY_COUNT = DayCounts.THIRTY_U_360;
  private static final double RATE = 0.0325;
  private static final boolean FIXED_IS_PAYER = true;
  private static final AnnuityCouponFixedDefinition FIXED_ANNUITY = AnnuityCouponFixedDefinition.from(CUR, SETTLEMENT_DATE, ANNUITY_TENOR, FIXED_PAYMENT_PERIOD, CALENDAR, FIXED_DAY_COUNT,
      BUSINESS_DAY, IS_EOM, 1.0, RATE, FIXED_IS_PAYER);
  //Ibor leg: quarterly money
  private static final Period INDEX_TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final DayCount DAY_COUNT = DayCounts.ACT_360;
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, INDEX_TENOR, SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, IS_EOM, "Ibor");
  private static final AnnuityCouponIborDefinition IBOR_ANNUITY = AnnuityCouponIborDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, 1.0, IBOR_INDEX, !FIXED_IS_PAYER, CALENDAR);
  // CMS coupon construction
  private static final IndexSwap CMS_INDEX = new IndexSwap(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, IBOR_INDEX, ANNUITY_TENOR, CALENDAR);
  private static final SwapFixedIborDefinition SWAP_DEFINITION = new SwapFixedIborDefinition(FIXED_ANNUITY, IBOR_ANNUITY);
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2011, 4, 6);
  private static final ZonedDateTime FIXING_DATE = DateUtils.getUTCDate(2010, 12, 30);
  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtils.getUTCDate(2011, 1, 5);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtils.getUTCDate(2011, 4, 5);
  private static final DayCount PAYMENT_DAY_COUNT = DayCounts.ACT_360;
  private static final double ACCRUAL_FACTOR = PAYMENT_DAY_COUNT.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double NOTIONAL = 1000000; //1m
  private static final ZonedDateTime FAKE_DATE = DateUtils.getUTCDate(0, 1, 1);
  private static final CouponFloatingDefinition COUPON = new CouponIborDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FAKE_DATE, IBOR_INDEX, CALENDAR);
  private static final CouponFloatingDefinition FLOAT_COUPON = CouponIborDefinition.from(COUPON, FIXING_DATE, IBOR_INDEX, CALENDAR);
  private static final CouponCMSDefinition CMS_COUPON_DEFINITION = CouponCMSDefinition.from(FLOAT_COUPON, SWAP_DEFINITION, CMS_INDEX);
  // CMS cap
  private static final double STRIKE = 0.04;
  private static final double HIGH_FIXING_RATE = STRIKE + 0.01;
  private static final DoubleTimeSeries<ZonedDateTime> HIGH_FIXING_TS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[]{FIXING_DATE}, new double[]{HIGH_FIXING_RATE});
  //  private static final double LOW_FIXING_RATE = STRIKE - 0.01;
  //  private static final DoubleTimeSeries<ZonedDateTime> LOW_FIXING_TS = ImmutableZonedDateTimeDoubleTimeSeries.of(new ZonedDateTime[] {FIXING_DATE}, new double[] {LOW_FIXING_RATE});
  private static final boolean IS_CAP = true;
  private static final CapFloorCMSDefinition CMS_CAP_DEFINITION = CapFloorCMSDefinition.from(CMS_COUPON_DEFINITION, STRIKE, IS_CAP);
  // to derivatives
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 8, 18);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCoupon() {
    CapFloorCMSDefinition.from(null, STRIKE, IS_CAP);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConversionNullDate1() {
    CMS_CAP_DEFINITION.toDerivative(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConversionNullDate2() {
    CMS_CAP_DEFINITION.toDerivative(null, HIGH_FIXING_TS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConversionNoTS() {
    CMS_CAP_DEFINITION.toDerivative(FIXING_DATE.plusDays(1));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConversionNullTS() {
    CMS_CAP_DEFINITION.toDerivative(FIXING_DATE.plusDays(1), (DoubleTimeSeries<ZonedDateTime>) null);
  }

  @Test
  public void testGetter() {
    final CapFloorCMS cmsCap = (CapFloorCMS) CMS_CAP_DEFINITION.toDerivative(REFERENCE_DATE);
    assertEquals(STRIKE, cmsCap.getStrike(), 1E-10);
    assertEquals(IS_CAP, cmsCap.isCap());
  }

  @Test
  public void testEqual() {
    final CapFloorCMSDefinition floor = CapFloorCMSDefinition.from(CMS_COUPON_DEFINITION, STRIKE, !IS_CAP);
    assertEquals(floor == CMS_CAP_DEFINITION, false);
    final CapFloorCMSDefinition capPlus = CapFloorCMSDefinition.from(CMS_COUPON_DEFINITION, STRIKE + 0.01, IS_CAP);
    assertEquals(capPlus == CMS_CAP_DEFINITION, false);
  }

  //TODO test
  @Test
  public void testToDerivative() {
    final CapFloorCMS cmsCap = (CapFloorCMS) CMS_CAP_DEFINITION.toDerivative(REFERENCE_DATE);
    final CouponCMS cmsCoupon = (CouponCMS) CMS_COUPON_DEFINITION.toDerivative(REFERENCE_DATE);
    final CapFloorCMS capDirect = CapFloorCMS.from(cmsCoupon, STRIKE, IS_CAP);
    assertEquals(capDirect, cmsCap);
  }
}

/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.inflation.provider;

import static org.testng.AssertJUnit.assertEquals;

import java.time.Period;
import java.time.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponInterpolationGearingDefinition;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponInterpolationGearing;
import com.opengamma.analytics.financial.provider.calculator.inflation.NetAmountInflationCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflation.PresentValueCurveSensitivityDiscountingInflationCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflation.PresentValueDiscountingInflationCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.inflation.InflationIssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.inflation.ParameterInflationProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.MultipleCurrencyInflationSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.ParameterSensitivityInflationMulticurveDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.ParameterSensitivityInflationParameterCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.analytics.util.time.DateUtils;
import com.opengamma.strata.basics.currency.MultiCurrencyAmount;
import com.opengamma.strata.basics.date.BusinessDayConvention;
import com.opengamma.strata.basics.date.BusinessDayConventions;
import com.opengamma.strata.basics.date.HolidayCalendar;

/**
 * Tests the present value and its sensitivities for zero-coupon with reference index interpolated between months.
 */
@Test
public class CouponInflationZeroCouponInterpolationGearingDiscountingMethodTest {
  private static final InflationIssuerProviderDiscount MARKET = MulticurveProviderDiscountDataSets.createMarket1();
  private static final IndexPrice[] PRICE_INDEXES = MARKET.getPriceIndexes().toArray(new IndexPrice[MARKET.getPriceIndexes().size()]);
  private static final IndexPrice PRICE_INDEX_EUR = PRICE_INDEXES[0];
  private static final HolidayCalendar CALENDAR_EUR = MulticurveProviderDiscountDataSets.getEURCalendar();
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final ZonedDateTime START_DATE = DateUtils.getUTCDate(2008, 8, 18);
  private static final Period COUPON_TENOR = Period.ofYears(10);
  private static final ZonedDateTime PAYMENT_DATE = ScheduleCalculator.getAdjustedDate(START_DATE, COUPON_TENOR, BUSINESS_DAY, CALENDAR_EUR);
  private static final double NOTIONAL = 98765432;
  private static final int MONTH_LAG = 3;
  private static final double INDEX_MAY_2008_INT = 108.4548387; // May index: 108.23 - June Index = 108.64
  private static final double FACTOR = 0.75;

  private static final double SHIFT_FD = 1.0E-7;
  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_DELTA = 1.0E+2;

  private static final CouponInflationZeroCouponInterpolationGearingDefinition ZERO_COUPON_1_DEFINITION = CouponInflationZeroCouponInterpolationGearingDefinition.from(START_DATE, PAYMENT_DATE,
      NOTIONAL, PRICE_INDEX_EUR, INDEX_MAY_2008_INT, MONTH_LAG, MONTH_LAG, false, FACTOR);
  private static final ZonedDateTime PRICING_DATE = DateUtils.getUTCDate(2011, 8, 3);
  private static final CouponInflationZeroCouponInterpolationGearing ZERO_COUPON_1 = ZERO_COUPON_1_DEFINITION.toDerivative(PRICING_DATE);

  private static final CouponInflationZeroCouponInterpolationGearingDiscountingMethod METHOD = new CouponInflationZeroCouponInterpolationGearingDiscountingMethod();
  private static final PresentValueDiscountingInflationCalculator PVIC = PresentValueDiscountingInflationCalculator.getInstance();
  private static final NetAmountInflationCalculator NAIC = NetAmountInflationCalculator.getInstance();

  private static final PresentValueCurveSensitivityDiscountingInflationCalculator PVCSDC = PresentValueCurveSensitivityDiscountingInflationCalculator.getInstance();
  private static final ParameterSensitivityInflationParameterCalculator<ParameterInflationProviderInterface> PSC = new ParameterSensitivityInflationParameterCalculator<>(PVCSDC);
  private static final ParameterSensitivityInflationMulticurveDiscountInterpolatedFDCalculator PS_PV_FDC = new ParameterSensitivityInflationMulticurveDiscountInterpolatedFDCalculator(PVIC, SHIFT_FD);

  /**
   * Tests the present value.
   */
  @Test
  public void presentValueInterpolation() {
    final MultiCurrencyAmount pv = METHOD.presentValue(ZERO_COUPON_1, MARKET.getInflationProvider());
    final double df = MARKET.getCurve(ZERO_COUPON_1.getCurrency()).getDiscountFactor(ZERO_COUPON_1.getPaymentTime());
    final double indexMonth0 = MARKET.getCurve(PRICE_INDEX_EUR).getPriceIndex(ZERO_COUPON_1.getReferenceEndTime()[0]);
    final double indexMonth1 = MARKET.getCurve(PRICE_INDEX_EUR).getPriceIndex(ZERO_COUPON_1.getReferenceEndTime()[1]);
    final double finalIndex = ZERO_COUPON_1_DEFINITION.getWeight() * indexMonth0 + (1 - ZERO_COUPON_1_DEFINITION.getWeight()) * indexMonth1;
    final double pvExpected = FACTOR * (finalIndex / INDEX_MAY_2008_INT - 1) * df * NOTIONAL;
    assertEquals("Zero-coupon inflation DiscountingMethod: Present value", pvExpected, pv.getAmount(ZERO_COUPON_1.getCurrency()).getAmount(), TOLERANCE_PV);
  }

  /**
   * Tests the net amount.
   */
  @Test
  public void netAmountInterpolation() {
    final MultiCurrencyAmount pv = METHOD.netAmount(ZERO_COUPON_1, MARKET.getInflationProvider());
    final double indexMonth0 = MARKET.getCurve(PRICE_INDEX_EUR).getPriceIndex(ZERO_COUPON_1.getReferenceEndTime()[0]);
    final double indexMonth1 = MARKET.getCurve(PRICE_INDEX_EUR).getPriceIndex(ZERO_COUPON_1.getReferenceEndTime()[1]);
    final double finalIndex = ZERO_COUPON_1_DEFINITION.getWeight() * indexMonth0 + (1 - ZERO_COUPON_1_DEFINITION.getWeight()) * indexMonth1;
    final double pvExpected = FACTOR * (finalIndex / INDEX_MAY_2008_INT - 1) * NOTIONAL;
    assertEquals("Zero-coupon inflation DiscountingMethod: net amount", pvExpected, pv.getAmount(ZERO_COUPON_1.getCurrency()).getAmount(), TOLERANCE_PV);
  }

  /**
   * Tests the present value: Method vs Calculator.
   */
  @Test
  public void presentValueMethodVsCalculator() {
    final MultiCurrencyAmount pvMethod = METHOD.presentValue(ZERO_COUPON_1, MARKET.getInflationProvider());
    final MultiCurrencyAmount pvCalculator = ZERO_COUPON_1.accept(PVIC, MARKET.getInflationProvider());
    assertEquals("Zero-coupon inflation DiscountingMethod: Present value", pvMethod, pvCalculator);
  }

  /**
   * Tests the net amount: Method vs Calculator.
   */
  @Test
  public void netAmountMethodVsCalculator() {
    final MultiCurrencyAmount naMethod = METHOD.netAmount(ZERO_COUPON_1, MARKET.getInflationProvider());
    final MultiCurrencyAmount naCalculator = ZERO_COUPON_1.accept(NAIC, MARKET.getInflationProvider());
    assertEquals("Zero-coupon inflation DiscountingMethod: Net amount", naMethod, naCalculator);
  }

  /**
   * Test the present value curves sensitivity.
   */
  @Test
  public void presentValueCurveSensitivityWithNotional() {

    final MultipleCurrencyParameterSensitivity pvicsFD = PS_PV_FDC.calculateSensitivity(ZERO_COUPON_1, MARKET.getInflationProvider());
    final MultipleCurrencyParameterSensitivity pvicsExact = PSC.calculateSensitivity(ZERO_COUPON_1, MARKET.getInflationProvider());

    AssertSensitivityObjects.assertEquals("Zero-coupon inflation DiscountingMethod: presentValueCurveSensitivity ", pvicsExact, pvicsFD, TOLERANCE_PV_DELTA);

  }

  @Test
  public void presentValueMarketSensitivityMethodVsCalculatorNoNotional() {
    final MultipleCurrencyInflationSensitivity pvcisMethod = METHOD.presentValueCurveSensitivity(ZERO_COUPON_1, MARKET.getInflationProvider());
    final MultipleCurrencyInflationSensitivity pvcisCalculator = ZERO_COUPON_1.accept(PVCSDC, MARKET.getInflationProvider());
    AssertSensitivityObjects.assertEquals("Zero-coupon inflation DiscountingMethod: presentValueMarketSensitivity", pvcisMethod, pvcisCalculator, TOLERANCE_PV_DELTA);
  }

}

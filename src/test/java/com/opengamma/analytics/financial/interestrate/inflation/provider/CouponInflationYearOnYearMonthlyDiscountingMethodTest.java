/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.inflation.provider;

import static org.testng.AssertJUnit.assertEquals;

import java.time.Period;
import java.time.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationYearOnYearMonthlyDefinition;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationYearOnYearMonthly;
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
 * Tests the present value and its sensitivities for year on year with reference index on the first of the month.
 */
@Test
public class CouponInflationYearOnYearMonthlyDiscountingMethodTest {

  private static final InflationIssuerProviderDiscount MARKET = MulticurveProviderDiscountDataSets.createMarket1();
  private static final IndexPrice[] PRICE_INDEXES = MARKET.getPriceIndexes().toArray(new IndexPrice[MARKET.getPriceIndexes().size()]);
  private static final IndexPrice PRICE_INDEX_EUR = PRICE_INDEXES[0];
  private static final HolidayCalendar CALENDAR_EUR = MulticurveProviderDiscountDataSets.getEURCalendar();
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final ZonedDateTime START_DATE = DateUtils.getUTCDate(2008, 8, 18);
  private static final Period COUPON_TENOR = Period.ofYears(10);
  private static final ZonedDateTime PAYMENT_DATE = ScheduleCalculator.getAdjustedDate(START_DATE, COUPON_TENOR, BUSINESS_DAY, CALENDAR_EUR);
  private static final ZonedDateTime PAYMENT_DATE_MINUS1 = ScheduleCalculator.getAdjustedDate(START_DATE, Period.ofYears(9), BUSINESS_DAY, CALENDAR_EUR);
  private static final double NOTIONAL = 98765432;
  private static final int MONTH_LAG = 3;
  private static final ZonedDateTime PRICING_DATE = DateUtils.getUTCDate(2011, 8, 3);

  private static final double SHIFT_FD = 1.0E-7;
  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_DELTA = 1.0E+2;

  private static final CouponInflationYearOnYearMonthlyDefinition YEAR_ON_YEAR_NO_DEFINITION = CouponInflationYearOnYearMonthlyDefinition.from(PAYMENT_DATE_MINUS1, PAYMENT_DATE, NOTIONAL,
      PRICE_INDEX_EUR,
      MONTH_LAG, false);
  private static final CouponInflationYearOnYearMonthly YEAR_ON_YEAR_NO = YEAR_ON_YEAR_NO_DEFINITION.toDerivative(PRICING_DATE);
  private static final CouponInflationYearOnYearMonthlyDefinition YEAR_ON_YEAR_WITH_DEFINITION = CouponInflationYearOnYearMonthlyDefinition.from(PAYMENT_DATE_MINUS1, PAYMENT_DATE, NOTIONAL,
      PRICE_INDEX_EUR,
      MONTH_LAG, true);
  private static final CouponInflationYearOnYearMonthly YEAR_ON_YEAR_WITH = YEAR_ON_YEAR_WITH_DEFINITION.toDerivative(PRICING_DATE);
  private static final CouponInflationYearOnYearMonthlyDiscountingMethod METHOD = new CouponInflationYearOnYearMonthlyDiscountingMethod();
  private static final PresentValueDiscountingInflationCalculator PVIC = PresentValueDiscountingInflationCalculator.getInstance();
  private static final NetAmountInflationCalculator NAIC = NetAmountInflationCalculator.getInstance();

  private static final PresentValueCurveSensitivityDiscountingInflationCalculator PVCSDC = PresentValueCurveSensitivityDiscountingInflationCalculator.getInstance();
  private static final ParameterSensitivityInflationParameterCalculator<ParameterInflationProviderInterface> PSC = new ParameterSensitivityInflationParameterCalculator<>(PVCSDC);
  private static final ParameterSensitivityInflationMulticurveDiscountInterpolatedFDCalculator PS_PV_FDC = new ParameterSensitivityInflationMulticurveDiscountInterpolatedFDCalculator(PVIC, SHIFT_FD);

  /**
   * Tests the present value.
   */
  @Test
  public void presentValueNoNotional() {
    final MultiCurrencyAmount pv = METHOD.presentValue(YEAR_ON_YEAR_NO, MARKET.getInflationProvider());
    final double df = MARKET.getCurve(YEAR_ON_YEAR_NO.getCurrency()).getDiscountFactor(YEAR_ON_YEAR_NO.getPaymentTime());
    final double finalIndex = MARKET.getCurve(PRICE_INDEX_EUR).getPriceIndex(YEAR_ON_YEAR_NO.getReferenceEndTime());
    final double initialIndex = MARKET.getCurve(PRICE_INDEX_EUR).getPriceIndex(YEAR_ON_YEAR_NO.getReferenceStartTime());
    final double pvExpected = (finalIndex / initialIndex - 1) * df * NOTIONAL;
    assertEquals("Year on year coupon inflation DiscountingMethod: Present value", pvExpected, pv.getAmount(YEAR_ON_YEAR_NO.getCurrency()).getAmount(), TOLERANCE_PV);
  }

  /**
   * Tests the net amount.
   */
  @Test
  public void netAmountNoNotional() {
    final MultiCurrencyAmount pv = METHOD.netAmount(YEAR_ON_YEAR_NO, MARKET.getInflationProvider());
    final double finalIndex = MARKET.getCurve(PRICE_INDEX_EUR).getPriceIndex(YEAR_ON_YEAR_NO.getReferenceEndTime());
    final double initialIndex = MARKET.getCurve(PRICE_INDEX_EUR).getPriceIndex(YEAR_ON_YEAR_NO.getReferenceStartTime());
    final double pvExpected = (finalIndex / initialIndex - 1) * NOTIONAL;
    assertEquals("Year on year coupon inflation DiscountingMethod: net amount", pvExpected, pv.getAmount(YEAR_ON_YEAR_NO.getCurrency()).getAmount(), TOLERANCE_PV);
  }

  /**
   * Tests the present value: Method vs Calculator.
   */
  @Test
  public void presentValueMethodVsCalculator() {
    final MultiCurrencyAmount pvMethod = METHOD.presentValue(YEAR_ON_YEAR_NO, MARKET.getInflationProvider());
    final MultiCurrencyAmount pvCalculator = YEAR_ON_YEAR_NO.accept(PVIC, MARKET.getInflationProvider());
    assertEquals("Year on year coupon inflation DiscountingMethod: Present value", pvMethod, pvCalculator);
  }

  /**
   * Tests the present value.
   */
  @Test
  public void presentValueWithNotional() {
    final MultiCurrencyAmount pv = METHOD.presentValue(YEAR_ON_YEAR_WITH, MARKET.getInflationProvider());
    final double df = MARKET.getCurve(YEAR_ON_YEAR_WITH.getCurrency()).getDiscountFactor(YEAR_ON_YEAR_WITH.getPaymentTime());
    final double finalIndex = MARKET.getCurve(PRICE_INDEX_EUR).getPriceIndex(YEAR_ON_YEAR_WITH.getReferenceEndTime());
    final double initialIndex = MARKET.getCurve(PRICE_INDEX_EUR).getPriceIndex(YEAR_ON_YEAR_WITH.getReferenceStartTime());
    final double pvExpected = (finalIndex / initialIndex) * df * NOTIONAL;
    assertEquals("Year on year coupon inflation DiscountingMethod: Present value", pvExpected, pv.getAmount(YEAR_ON_YEAR_WITH.getCurrency()).getAmount(), TOLERANCE_PV);
  }

  /**
   * Tests the net amount.
   */
  @Test
  public void netAmountWithNotional() {
    final MultiCurrencyAmount pv = METHOD.netAmount(YEAR_ON_YEAR_WITH, MARKET.getInflationProvider());
    final double finalIndex = MARKET.getCurve(PRICE_INDEX_EUR).getPriceIndex(YEAR_ON_YEAR_WITH.getReferenceEndTime());
    final double initialIndex = MARKET.getCurve(PRICE_INDEX_EUR).getPriceIndex(YEAR_ON_YEAR_NO.getReferenceStartTime());
    final double pvExpected = (finalIndex / initialIndex) * NOTIONAL;
    assertEquals("Year on year coupon inflation DiscountingMethod: net amount", pvExpected, pv.getAmount(YEAR_ON_YEAR_WITH.getCurrency()).getAmount(), TOLERANCE_PV);
  }

  /**
   * Tests the net amount: Method vs Calculator.
   */
  @Test
  public void netAmountMethodVsCalculator() {
    final MultiCurrencyAmount pvMethod = METHOD.netAmount(YEAR_ON_YEAR_NO, MARKET.getInflationProvider());
    final MultiCurrencyAmount pvCalculator = YEAR_ON_YEAR_NO.accept(NAIC, MARKET.getInflationProvider());
    assertEquals("Year on year coupon inflation DiscountingMethod: Net amount", pvMethod, pvCalculator);
  }

  /**
   * Test the present value curves sensitivity.
   */
  @Test
  public void presentValueCurveSensitivityWithNotional() {

    final MultipleCurrencyParameterSensitivity pvicsFD = PS_PV_FDC.calculateSensitivity(YEAR_ON_YEAR_WITH, MARKET.getInflationProvider());
    final MultipleCurrencyParameterSensitivity pvicsExact = PSC.calculateSensitivity(YEAR_ON_YEAR_WITH, MARKET.getInflationProvider());

    AssertSensitivityObjects.assertEquals("Year on year coupon inflation DiscountingMethod: presentValueCurveSensitivity ", pvicsExact, pvicsFD, TOLERANCE_PV_DELTA);

  }

  /**
   * Test the present value curves sensitivity.
   */
  @Test
  public void presentValueCurveSensitivityNoNotional() {

    final MultipleCurrencyParameterSensitivity pvicsFD = PS_PV_FDC.calculateSensitivity(YEAR_ON_YEAR_NO, MARKET.getInflationProvider());
    final MultipleCurrencyParameterSensitivity pvicsExact = PSC.calculateSensitivity(YEAR_ON_YEAR_NO, MARKET.getInflationProvider());

    AssertSensitivityObjects.assertEquals("Year on year coupon inflation DiscountingMethod: presentValueCurveSensitivity ", pvicsExact, pvicsFD, TOLERANCE_PV_DELTA);
  }

  @Test
  public void presentValueMarketSensitivityMethodVsCalculatorNoNotional() {
    final MultipleCurrencyInflationSensitivity pvcisMethod = METHOD.presentValueCurveSensitivity(YEAR_ON_YEAR_NO, MARKET.getInflationProvider());
    final MultipleCurrencyInflationSensitivity pvcisCalculator = YEAR_ON_YEAR_NO.accept(PVCSDC, MARKET.getInflationProvider());
    AssertSensitivityObjects.assertEquals("Year on year coupon inflation DiscountingMethod: presentValueMarketSensitivity", pvcisMethod, pvcisCalculator, TOLERANCE_PV_DELTA);
  }

  @Test
  public void presentValueMarketSensitivityMethodVsCalculatorWithNotional() {
    final MultipleCurrencyInflationSensitivity pvcisMethod = METHOD.presentValueCurveSensitivity(YEAR_ON_YEAR_WITH, MARKET.getInflationProvider());
    final MultipleCurrencyInflationSensitivity pvcisCalculator = YEAR_ON_YEAR_WITH.accept(PVCSDC, MARKET.getInflationProvider());
    AssertSensitivityObjects.assertEquals("Year on year coupon inflation DiscountingMethod: presentValueMarketSensitivity", pvcisMethod, pvcisCalculator, TOLERANCE_PV_DELTA);
  }

}

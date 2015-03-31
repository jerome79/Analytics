/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;

import static org.testng.AssertJUnit.assertEquals;

import java.time.Period;
import java.time.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.convention.calendar.Calendar;
import com.opengamma.analytics.convention.calendar.MondayToFridayCalendar;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedONMaster;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.payment.CouponONSpreadSimplifiedDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONSpread;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ParameterSensitivityMulticurveDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.analytics.util.time.DateUtils;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.currency.MultiCurrencyAmount;

/**
 * Tests related to the pricing methods for overnight indexed coupon with spread in the discounting method.
 */
@Test
public class CouponONSpreadDiscountingMethodTest {

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();

  private static final Calendar TARGET = new MondayToFridayCalendar("TARGET");
  private static final GeneratorSwapFixedON GENERATOR_SWAP_EONIA = GeneratorSwapFixedONMaster.getInstance().getGenerator("EUR1YEONIA", TARGET);
  private static final IndexON EONIA = MulticurveProviderDiscountDataSets.getIndexesON()[0];
  private static final Currency EUR = EONIA.getCurrency();
  private static final Calendar CALENDAR = MulticurveProviderDiscountDataSets.getEURCalendar();
  private static final ZonedDateTime EFFECTIVE_DATE = DateUtils.getUTCDate(2011, 5, 23);
  private static final Period TENOR = Period.ofMonths(3);
  private static final double NOTIONAL = 100000000; // 100m
  private static final double SPREAD = 0.0012;
  private static final CouponONSpreadSimplifiedDefinition CPN_OIS_DEFINITION = CouponONSpreadSimplifiedDefinition.from(EONIA, EFFECTIVE_DATE, TENOR, NOTIONAL, SPREAD, 2,
      GENERATOR_SWAP_EONIA.getBusinessDayConvention(), GENERATOR_SWAP_EONIA.isEndOfMonth(), CALENDAR);
  private static final double SPREAD_AMOUNT = SPREAD * NOTIONAL * CPN_OIS_DEFINITION.getPaymentYearFraction();

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 12, 27);
  private static final CouponONSpread CPN_OIS = CPN_OIS_DEFINITION.toDerivative(REFERENCE_DATE);

  private static final CouponONSpreadDiscountingMethod METHOD_CPN_ON = CouponONSpreadDiscountingMethod.getInstance();
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC = PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  private static final ParameterSensitivityParameterCalculator<ParameterProviderInterface> PSC = new ParameterSensitivityParameterCalculator<>(PVCSDC);
  private static final double SHIFT = 1.0E-6;
  private static final ParameterSensitivityMulticurveDiscountInterpolatedFDCalculator PSC_DSC_FD = new ParameterSensitivityMulticurveDiscountInterpolatedFDCalculator(PVDC, SHIFT);

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_DELTA = 1.0E+2; //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move.

  @Test
  public void presentValue() {
    final MultiCurrencyAmount pvComputed = METHOD_CPN_ON.presentValue(CPN_OIS, MULTICURVES);
    final double forward = MULTICURVES.getSimplyCompoundForwardRate(EONIA, CPN_OIS.getFixingPeriodStartTime(), CPN_OIS.getFixingPeriodEndTime(), CPN_OIS.getFixingPeriodAccrualFactor());
    final double pvExpected = (NOTIONAL * CPN_OIS.getFixingPeriodAccrualFactor() * forward + SPREAD_AMOUNT) * MULTICURVES.getDiscountFactor(CPN_OIS.getCurrency(), CPN_OIS.getPaymentTime());
    assertEquals("CouponOISDiscountingMarketMethod: present value", pvExpected, pvComputed.getAmount(EONIA.getCurrency()).getAmount(), TOLERANCE_PV);
  }

  @Test
  public void presentValueMethodVsCalculator() {
    final MultiCurrencyAmount pvMethod = METHOD_CPN_ON.presentValue(CPN_OIS, MULTICURVES);
    final MultiCurrencyAmount pvCalculator = CPN_OIS.accept(PVDC, MULTICURVES);
    assertEquals("CouponFixedDiscountingMarketMethod: present value", pvMethod.getAmount(EUR).getAmount(), pvCalculator.getAmount(EUR).getAmount(), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests present value curve sensitivity when the valuation date is on trade date.
   */
  public void presentValueCurveSensitivity() {
    final MultipleCurrencyParameterSensitivity pvpsDepositExact = PSC.calculateSensitivity(CPN_OIS, MULTICURVES, MULTICURVES.getAllNames());
    final MultipleCurrencyParameterSensitivity pvpsDepositFD = PSC_DSC_FD.calculateSensitivity(CPN_OIS, MULTICURVES);
    AssertSensitivityObjects.assertEquals("CashDiscountingProviderMethod: presentValueCurveSensitivity ", pvpsDepositExact, pvpsDepositFD, TOLERANCE_PV_DELTA);
  }

  @Test
  public void presentValueMarketSensitivityMethodVsCalculator() {
    final MultipleCurrencyMulticurveSensitivity pvcsMethod = METHOD_CPN_ON.presentValueCurveSensitivity(CPN_OIS, MULTICURVES);
    final MultipleCurrencyMulticurveSensitivity pvcsCalculator = CPN_OIS.accept(PVCSDC, MULTICURVES);
    AssertSensitivityObjects.assertEquals("CouponFixedDiscountingMarketMethod: presentValueMarketSensitivity", pvcsMethod, pvcsCalculator, TOLERANCE_PV_DELTA);
  }

}

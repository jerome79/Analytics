/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.time.Period;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.future.InterestRateFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureTransactionDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedONMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.interestrate.datasets.StandardDataSetsMulticurveEUR;
import com.opengamma.analytics.financial.interestrate.datasets.StandardDataSetsMulticurveFuturesEUR;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureTransaction;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.provider.calculator.discounting.MarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.MarketQuoteSensitivityBlockCalculator;
import com.opengamma.analytics.financial.provider.calculator.hullwhite.MarketQuoteHullWhiteCalculator;
import com.opengamma.analytics.financial.provider.calculator.hullwhite.PresentValueCurveSensitivityHullWhiteCalculator;
import com.opengamma.analytics.financial.provider.calculator.hullwhite.PresentValueHullWhiteCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.util.time.DateUtils;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.currency.MultiCurrencyAmount;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.collect.tuple.Pair;

/**
 * Tests the STIR Futures Hull-White method with curve calibrated on OIS and futures.
 */
@Test
public class STIRFuturesTransactionHullWhiteMethodE2ETest {

  /** Data */
  private static final ZonedDateTime VALUATION_DATE = DateUtils.getUTCDate(2014, 9, 2);
  private static final IborIndex[] INDEX_IBOR_LIST = StandardDataSetsMulticurveEUR.indexIborArrayEUROisE3();
  private static final IborIndex EUREURIBOR3M = INDEX_IBOR_LIST[0];
  private static final HolidayCalendar CALENDAR = StandardDataSetsMulticurveEUR.calendarArray()[0];
  private static final Currency EUR = EUREURIBOR3M.getCurrency();
  private static final double[] MQ_OIS_CAL = StandardDataSetsMulticurveFuturesEUR.oisMarketData();
  private static final double[] MQ_3M_CAL = StandardDataSetsMulticurveFuturesEUR.futIrs3MMarketData();

  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_DSC_PAIR =
      StandardDataSetsMulticurveFuturesEUR.getCurvesUSDOisFutL3Discounting(VALUATION_DATE);
  private static final MulticurveProviderDiscount MULTICURVE_DSC = MULTICURVE_DSC_PAIR.getFirst();
  private static final CurveBuildingBlockBundle BLOCK_DSC = MULTICURVE_DSC_PAIR.getSecond();

  private static final Pair<HullWhiteOneFactorProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_HW_PAIR =
      StandardDataSetsMulticurveFuturesEUR.getCurvesUSDOisFutL3HullWhite(VALUATION_DATE);
  private static final HullWhiteOneFactorProviderDiscount MULTICURVE_HW = MULTICURVE_HW_PAIR.getFirst();
  private static final CurveBuildingBlockBundle BLOCK_HW = MULTICURVE_HW_PAIR.getSecond();

  /** Instruments */
  private static final double NOTIONAL = 1000000.0; // 1m
  private static final double FUTURE_FACTOR = 0.25;
  private static final ZonedDateTime LAST_TRADING_DATE_ERZ4 = DateUtils.getUTCDate(2014, 12, 15);
  private static final String NAME_ERZ4 = "ERZ4";
  private static final InterestRateFutureSecurityDefinition ERZ4_SEC_DEFINITION =
      new InterestRateFutureSecurityDefinition(LAST_TRADING_DATE_ERZ4, EUREURIBOR3M,
          NOTIONAL, FUTURE_FACTOR, NAME_ERZ4, CALENDAR);
  private static final long QUANTITY_ERZ4 = -125;
  private static final ZonedDateTime TRADE_DATE_ERZ4 = DateUtils.getUTCDate(2013, 5, 7);
  private static final double TRADE_PRICE_ERZ4 = 0.999;
  private static final InterestRateFutureTransactionDefinition ERZ4_TRA_DEFINITION =
      new InterestRateFutureTransactionDefinition(ERZ4_SEC_DEFINITION, QUANTITY_ERZ4, TRADE_DATE_ERZ4, TRADE_PRICE_ERZ4);
  private static final double LAST_MARGIN_PRICE_ERZ4 = 0.9988;
  private static final InterestRateFutureTransaction ERZ4_TRA =
      ERZ4_TRA_DEFINITION.toDerivative(VALUATION_DATE, LAST_MARGIN_PRICE_ERZ4);

  private static final ZonedDateTime LAST_TRADING_DATE_ERF5 = DateUtils.getUTCDate(2015, 1, 19);
  private static final String NAME_ERF5 = "ERF5";
  private static final InterestRateFutureSecurityDefinition ERF5_SEC_DEFINITION =
      new InterestRateFutureSecurityDefinition(LAST_TRADING_DATE_ERF5, EUREURIBOR3M,
          NOTIONAL, FUTURE_FACTOR, NAME_ERF5, CALENDAR);
  private static final long QUANTITY_ERF5 = -125;
  private static final ZonedDateTime TRADE_DATE_ERF5 = DateUtils.getUTCDate(2013, 5, 7);
  private static final double TRADE_PRICE_ERF5 = 0.998;
  private static final InterestRateFutureTransactionDefinition ERF5_TRA_DEFINITION =
      new InterestRateFutureTransactionDefinition(ERF5_SEC_DEFINITION, QUANTITY_ERF5, TRADE_DATE_ERF5, TRADE_PRICE_ERF5);
  private static final double LAST_MARGIN_PRICE_ERF5 = 0.9975;
  private static final InterestRateFutureTransaction ERF5_TRA =
      ERF5_TRA_DEFINITION.toDerivative(VALUATION_DATE, LAST_MARGIN_PRICE_ERF5);

  private static final GeneratorSwapFixedON EUR1YEONIA =
      GeneratorSwapFixedONMaster.getInstance().getGenerator("EUR1YEONIA", CALENDAR);
  private static final ZonedDateTime TRADE_DATE_ON = DateUtils.getUTCDate(2015, 9, 2);
  private static final Period TENOR_SWAP_ON = Period.ofYears(1);
  private static final double FIXED_RATE_ON = 0.0025;
  private static final GeneratorAttributeIR ATTRIBUTE_ON = new GeneratorAttributeIR(TENOR_SWAP_ON);
  private static final SwapDefinition SWAP_FIXED_ON_DEFINITION =
      EUR1YEONIA.generateInstrument(TRADE_DATE_ON, FIXED_RATE_ON, NOTIONAL, ATTRIBUTE_ON);
  private static final Swap<? extends Payment, ? extends Payment> SWAP_FIXED_ON =
      SWAP_FIXED_ON_DEFINITION.toDerivative(VALUATION_DATE);

  /** Calculators */
  private static final MarketQuoteDiscountingCalculator MQDC = MarketQuoteDiscountingCalculator.getInstance();
  private static final MarketQuoteHullWhiteCalculator MQHWC = MarketQuoteHullWhiteCalculator.getInstance();

  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueHullWhiteCalculator PVHWC = PresentValueHullWhiteCalculator.getInstance();

  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC =
      PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  private static final ParameterSensitivityParameterCalculator<ParameterProviderInterface> PSDC =
      new ParameterSensitivityParameterCalculator<>(PVCSDC);
  private static final MarketQuoteSensitivityBlockCalculator<ParameterProviderInterface> MQSBC =
      new MarketQuoteSensitivityBlockCalculator<>(PSDC);

  private static final PresentValueCurveSensitivityHullWhiteCalculator PVCSHWC =
      PresentValueCurveSensitivityHullWhiteCalculator.getInstance();
  private static final ParameterSensitivityParameterCalculator<HullWhiteOneFactorProviderInterface> PSHWC =
      new ParameterSensitivityParameterCalculator<>(PVCSHWC);
  private static final MarketQuoteSensitivityBlockCalculator<HullWhiteOneFactorProviderInterface> MQSBHWC =
      new MarketQuoteSensitivityBlockCalculator<>(PSHWC);

  private static final double TOLERANCE_PV = 1.0E-4;
  private static final double TOLERANCE_PRICE = 1.0E-8;
  private static final double TOLERANCE_PV_DELTA = 1.0E-2;
  private static final double BP1 = 1.0E-4;

  /** Test market quote with curves calibrated without convexity adjustment. */
  @Test
  public void priceNoConvexityAjustment() {
    double mqExpected = MQ_3M_CAL[2]; // Dec 14 price
    Double mqComputedDsc = ERZ4_TRA.getUnderlyingSecurity().accept(MQDC, MULTICURVE_DSC);
    assertEquals("STIRFuturesTransactionHullWhiteMethodE2ETest: price - discounting",
        mqExpected, mqComputedDsc, TOLERANCE_PRICE);
  }

  /** Test market quote with curves calibrated with convexity adjustment. */
  @Test
  public void priceConvexityAdjustmentHullWhite() {
    double mqExpected = MQ_3M_CAL[2]; // Dec 14 price
    Double mqComputedHw = ERZ4_TRA.getUnderlyingSecurity().accept(MQHWC, MULTICURVE_HW);
    assertEquals("STIRFuturesTransactionHullWhiteMethodE2ETest: price - Hull-White",
        mqExpected, mqComputedHw, TOLERANCE_PRICE);
  }

  /** Test market quote with curves calibrated with convexity adjustment v hard-coded number. */
  @Test
  public void priceF5ConvexityAdjustmentHullWhite() {
    double mqExpected = 0.9988842362;
    Double mqComputedHw = ERF5_TRA.getUnderlyingSecurity().accept(MQHWC, MULTICURVE_HW);
    assertEquals("STIRFuturesTransactionHullWhiteMethodE2ETest: price - Hull-White",
        mqExpected, mqComputedHw, TOLERANCE_PRICE);
  }

  /** Compare market quote for curves calibrated with and without convexity adjustment. */
  @Test
  public void priceF5Comparison() {
    Double mqHw = ERF5_TRA.getUnderlyingSecurity().accept(MQHWC, MULTICURVE_HW);
    Double mqDsc = ERF5_TRA.getUnderlyingSecurity().accept(MQDC, MULTICURVE_DSC);
    assertFalse("STIRFuturesTransactionHullWhiteMethodE2ETest: price - Hull-White",
        Math.abs(mqHw - mqDsc) < TOLERANCE_PRICE);
  }

  /** Test present value with curves calibrated without convexity adjustment. */
  @Test
  public void presentValueNoConvexityAjustment() {
    double mqExpected = MQ_3M_CAL[2]; // Dec 14 price
    double pvExpected = (mqExpected - LAST_MARGIN_PRICE_ERZ4) * NOTIONAL * FUTURE_FACTOR * QUANTITY_ERZ4;
    MultiCurrencyAmount pvComputedDsc = ERZ4_TRA.accept(PVDC, MULTICURVE_DSC);
    assertEquals("STIRFuturesTransactionHullWhiteMethodE2ETest: present value - discounting",
        pvExpected, pvComputedDsc.getAmount(EUR).getAmount(), TOLERANCE_PV);
  }

  /** Test present value with curves calibrated with convexity adjustment. */
  @Test
  public void presentValueConvexityAdjustmentHullWhite() {
    double mqExpected = MQ_3M_CAL[2]; // Dec 14 price
    double pvExpected = (mqExpected - LAST_MARGIN_PRICE_ERZ4) * NOTIONAL * FUTURE_FACTOR * QUANTITY_ERZ4;
    MultiCurrencyAmount pvComputedHw = ERZ4_TRA.accept(PVHWC, MULTICURVE_HW);
    assertEquals("STIRFuturesTransactionHullWhiteMethodE2ETest: present value - Hull-White",
        pvExpected, pvComputedHw.getAmount(EUR).getAmount(), TOLERANCE_PV);
  }

  /** Test present value with curves calibrated with convexity adjustment v hard-coded numbers. */
  @Test
  public void presentValueF5ConvexityAdjustmentHullWhite() {
    double pvExpected = -43257.380512;
    MultiCurrencyAmount pvComputedHw = ERF5_TRA.accept(PVHWC, MULTICURVE_HW);
    assertEquals("STIRFuturesTransactionHullWhiteMethodE2ETest: present value - Hull-White",
        pvExpected, pvComputedHw.getAmount(EUR).getAmount(), TOLERANCE_PV);
  }

  /** Tests that swaps not impacted by the futures have the same pv with both calibrations. */
  @Test
  public void presentValueOisWithAndWithoutConvexity() {
    MultiCurrencyAmount pvHw = SWAP_FIXED_ON.accept(PVHWC, MULTICURVE_HW);
    MultiCurrencyAmount pvDsc = SWAP_FIXED_ON.accept(PVDC, MULTICURVE_DSC);
    assertTrue("STIRFuturesTransactionHullWhiteMethodE2ETest: price - Hull-White v Discounting",
        Math.abs(pvHw.getAmount(EUR).getAmount() - pvDsc.getAmount(EUR).getAmount()) < TOLERANCE_PV);
  }

  /** Test present value with curves calibrated with and without convexity adjustment. */
  @Test
  public void marketQuoteBucketedPV01NoConvexityAjustment() {
    double deltaUnderlying = QUANTITY_ERZ4 * FUTURE_FACTOR * NOTIONAL * BP1; // Sensitivity to the underlying
    final double[] deltaDsc = new double[MQ_OIS_CAL.length];
    final double[] deltaFwd = new double[MQ_3M_CAL.length];
    deltaFwd[2] = deltaUnderlying;
    final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> sensitivity = new LinkedHashMap<>();
    sensitivity.put(Pair.of(MULTICURVE_DSC.getName(EUR), EUR), new DoubleMatrix1D(deltaDsc));
    sensitivity.put(Pair.of(MULTICURVE_DSC.getName(EUREURIBOR3M), EUR), new DoubleMatrix1D(deltaFwd));
    final MultipleCurrencyParameterSensitivity pvpsExpected = new MultipleCurrencyParameterSensitivity(sensitivity);
    final MultipleCurrencyParameterSensitivity pvpsComputedDsc = MQSBC.fromInstrument(ERZ4_TRA, MULTICURVE_DSC, BLOCK_DSC).
        multipliedBy(BP1);
    AssertSensitivityObjects.assertEquals("STIRFuturesTransactionDiscountingMethodE2ETest: bucketed deltas from standard curves",
        pvpsExpected, pvpsComputedDsc, TOLERANCE_PV_DELTA);
  }

  /** Test present value with curves calibrated with and without convexity adjustment. */
  @Test
  public void marketQuoteBucketedPV01ConvexityAdjustmentHullWhite() {
    double deltaUnderlying = QUANTITY_ERZ4 * FUTURE_FACTOR * NOTIONAL * BP1; // Sensitivity to the underlying
    final double[] deltaDsc = new double[MQ_OIS_CAL.length];
    final double[] deltaFwd = new double[MQ_3M_CAL.length];
    deltaFwd[2] = deltaUnderlying;
    final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> sensitivity = new LinkedHashMap<>();
    sensitivity.put(Pair.of(MULTICURVE_DSC.getName(EUR), EUR), new DoubleMatrix1D(deltaDsc));
    sensitivity.put(Pair.of(MULTICURVE_DSC.getName(EUREURIBOR3M), EUR), new DoubleMatrix1D(deltaFwd));
    final MultipleCurrencyParameterSensitivity pvpsExpected = new MultipleCurrencyParameterSensitivity(sensitivity);
    final MultipleCurrencyParameterSensitivity pvpsComputedHw = MQSBHWC.fromInstrument(ERZ4_TRA, MULTICURVE_HW, BLOCK_HW).
        multipliedBy(BP1);
    AssertSensitivityObjects.assertEquals("STIRFuturesTransactionDiscountingMethodE2ETest: bucketed deltas from standard curves",
        pvpsExpected, pvpsComputedHw, TOLERANCE_PV_DELTA);
  }

  /** Test present value with curves calibrated with and without convexity adjustment v hard-coded numbers. */
  @Test
  public void marketQuoteBucketedPV01F5ConvexityAdjustmentHullWhite() {
    final double[] deltaDsc = new double[MQ_OIS_CAL.length];
    final double[] deltaFwd = {-29.3323, 177.9612, -2364.2404, -968.0481, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000,
      0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000 };
    final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> sensitivity = new LinkedHashMap<>();
    sensitivity.put(Pair.of(MULTICURVE_DSC.getName(EUR), EUR), new DoubleMatrix1D(deltaDsc));
    sensitivity.put(Pair.of(MULTICURVE_DSC.getName(EUREURIBOR3M), EUR), new DoubleMatrix1D(deltaFwd));
    final MultipleCurrencyParameterSensitivity pvpsExpected = new MultipleCurrencyParameterSensitivity(sensitivity);
    final MultipleCurrencyParameterSensitivity pvpsComputedHw = MQSBHWC.fromInstrument(ERF5_TRA, MULTICURVE_HW, BLOCK_HW).
        multipliedBy(BP1);
    AssertSensitivityObjects.assertEquals("STIRFuturesTransactionDiscountingMethodE2ETest: bucketed deltas from standard curves",
        pvpsExpected, pvpsComputedHw, TOLERANCE_PV_DELTA);
  }

}

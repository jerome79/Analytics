/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swap.provider;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.time.Period;
import java.time.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapIborIborDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.datasets.StandardDataSetsMulticurveUSD;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.calculator.discounting.CrossGammaSingleCurveCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PV01CurveParametersCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.MarketQuoteSensitivityBlockCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.TodayPaymentCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterAbstractCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityUnderlyingParameterCalculator;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.util.amount.ReferenceAmount;
import com.opengamma.analytics.util.time.DateUtils;
import com.opengamma.analytics.util.timeseries.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.analytics.util.timeseries.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.currency.MultiCurrencyAmount;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.collect.ArgChecker;
import com.opengamma.strata.collect.tuple.Pair;

/**
 * Tests related to swap calculators.
 */
@Test
public class SwapCalculatorTest {

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final IborIndex[] INDEX_LIST = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd();
  private static final IborIndex USDLIBOR3M = INDEX_LIST[2];
  private static final IborIndex USDLIBOR6M = INDEX_LIST[3];
  private static final Currency USD = USDLIBOR3M.getCurrency();
  private static final HolidayCalendar NYC = MulticurveProviderDiscountDataSets.getUSDCalendar();

  private static final GeneratorSwapFixedIborMaster GENERATOR_SWAP_MASTER = GeneratorSwapFixedIborMaster.getInstance();

  /** IRS Fixed v LIBOR3M */
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GENERATOR_SWAP_MASTER.getGenerator("USD6MLIBOR3M", NYC);
  private static final Period SWAP_TENOR = Period.ofYears(10);
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2012, 5, 17);
  private static final double NOTIONAL = 100000000; //100m
  private static final double RATE_FIXED = 0.025;
  private static final SwapFixedIborDefinition SWAP_FIXED_IBOR_DEFINITION = 
      SwapFixedIborDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, USD6MLIBOR3M, NOTIONAL, RATE_FIXED, true);
  private static final Period SWAP_TENOR_2 = Period.ofYears(15); // Tenor more than 10Y to be after the last node on the discounting curve (10Y).
  private static final SwapFixedIborDefinition SWAP_FIXED_IBOR_2_DEFINITION = 
      SwapFixedIborDefinition.from(SETTLEMENT_DATE, SWAP_TENOR_2, USD6MLIBOR3M, NOTIONAL, RATE_FIXED, true);
  /** Basis Swap LIBOR3M v LIBOR6M */
  private static final double SPREAD3 = 0.0020;
  private static final double SPREAD6 = 0.0005;
  private static final SwapIborIborDefinition SWAP_IBORSPREAD_IBORSPREAD_DEFINITION = 
      new SwapIborIborDefinition(AnnuityCouponIborSpreadDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, NOTIONAL,
      USDLIBOR3M, SPREAD3, true, NYC), AnnuityCouponIborSpreadDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, NOTIONAL, 
          USDLIBOR6M, SPREAD6, false, NYC));
  private static final SwapDefinition SWAP_IBOR_IBORSPREAD_DEFINITION = 
      new SwapDefinition(AnnuityCouponIborDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, NOTIONAL, USDLIBOR3M, true, NYC),
      AnnuityCouponIborSpreadDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, NOTIONAL, USDLIBOR6M, SPREAD6, false, NYC));

  private static final ZonedDateTimeDoubleTimeSeries FIXING_TS_3 =
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
          new ZonedDateTime[] {DateUtils.getUTCDate(2012, 5, 10), DateUtils.getUTCDate(2012, 5, 14), 
            DateUtils.getUTCDate(2012, 5, 15), DateUtils.getUTCDate(2012, 5, 16), DateUtils.getUTCDate(2012, 8, 15), 
            DateUtils.getUTCDate(2012, 11, 15) }, 
            new double[] {0.0080, 0.0090, 0.0100, 0.0110, 0.0140, 0.0160 });
  private static final ZonedDateTimeDoubleTimeSeries FIXING_TS_6 = 
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[]{DateUtils.getUTCDate(2012, 5, 10),
          DateUtils.getUTCDate(2012, 5, 15), DateUtils.getUTCDate(2012, 5, 16)}, new double[]{0.0095, 0.0120, 0.0130});
  private static final ZonedDateTimeDoubleTimeSeries[] FIXING_TS_3_6 = 
      new ZonedDateTimeDoubleTimeSeries[] {FIXING_TS_3, FIXING_TS_6 };

  /** Calculators */
  private static final ParSpreadMarketQuoteDiscountingCalculator PSMQDC = 
      ParSpreadMarketQuoteDiscountingCalculator.getInstance();
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final ParRateDiscountingCalculator PRDC = ParRateDiscountingCalculator.getInstance();
  private static final TodayPaymentCalculator TPC = TodayPaymentCalculator.getInstance();
  private static final PV01CurveParametersCalculator<ParameterProviderInterface> PV01CPC = 
      new PV01CurveParametersCalculator<>(PresentValueCurveSensitivityDiscountingCalculator.getInstance());
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC = 
      PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  private static final ParameterSensitivityParameterCalculator<ParameterProviderInterface> PSPVC = 
      new ParameterSensitivityParameterCalculator<>(PVCSDC);
  private static final ParameterSensitivityParameterAbstractCalculator<ParameterProviderInterface> PSUC = 
      new ParameterSensitivityUnderlyingParameterCalculator<>(PVCSDC);
  private static final MarketQuoteSensitivityBlockCalculator<ParameterProviderInterface> MQSUBC = 
      new MarketQuoteSensitivityBlockCalculator<>(PSUC);
  private static final CrossGammaSingleCurveCalculator CGC = new CrossGammaSingleCurveCalculator(PVCSDC);

  private static final double TOLERANCE_PV = 1.0E-2; // one cent out of 100m
  private static final double TOLERANCE_PV_DELTA = 1.0E+4;
  private static final double TOLERANCE_PV_GAMMA = 2.0E+0;
  private static final double TOLERANCE_PV_GAMMA_RELATIF = 5.0E-4;

  private static final double BP1 = 1.0E-4; // The size of the scaling: 1 basis point.
  private static final double SHIFT = 1.0E-4;
  private static final double SHIFT_2 = 1.0E-6;

  @Test
  /** 
   * Test the market quote sensitivity in the case of curve calibrated as a spread on an existing curve.
   * The parameter sensitivity calculator is the ParameterSensitivityUnderlyingParameterCalculator which takes into 
   * account the underlying curves up to one level deep.
   * The test is done versus a finite difference computation with full curve re-calibration.
   */
  public void marketQuoteSensitivitySpreadCurve() {
    double[] mqDsc = StandardDataSetsMulticurveUSD.getMarketDataDsc1();
    double[] mqFwd3 = StandardDataSetsMulticurveUSD.getMarketDataFwd31();
    String nameDsc = StandardDataSetsMulticurveUSD.getDscCurveName();
    String nameFwd3 = StandardDataSetsMulticurveUSD.getFwd3CurveName();
    Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> multicurveSpreadPair = 
        StandardDataSetsMulticurveUSD.getCurvesUSDOisSpreadL3();
    MulticurveProviderInterface multicurveSpread = multicurveSpreadPair.getFirst();
    CurveBuildingBlockBundle blockSpread = multicurveSpreadPair.getSecond();
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 5, 14);
    final SwapFixedCoupon<Coupon> swap = SWAP_FIXED_IBOR_2_DEFINITION.toDerivative(referenceDate);
    double pv = swap.accept(PVDC, multicurveSpread).getAmount(USD).getAmount();
    MultipleCurrencyParameterSensitivity mqs = MQSUBC.fromInstrument(swap, multicurveSpread, blockSpread);
    int nbNodDsc = mqDsc.length;
    double[] mqsDscComputed = mqs.getSensitivity(nameDsc, USD).getData();
    double[] mqsDscExpected = new double[nbNodDsc];
    for (int loopnodedsc = 0; loopnodedsc < nbNodDsc; loopnodedsc++) {
      double[] mqDscShifted = mqDsc.clone();
      mqDscShifted[loopnodedsc] += SHIFT_2;
      MulticurveProviderDiscount multicurveShifted = 
          StandardDataSetsMulticurveUSD.getCurvesUSDOisSpreadL3(mqDscShifted, mqFwd3).getFirst();
      double pvShifted = swap.accept(PVDC, multicurveShifted).getAmount(USD).getAmount();
      mqsDscExpected[loopnodedsc] = (pvShifted - pv) / SHIFT_2;
      assertEquals("ParameterSensitivityUnderlyingParameterCalculator - " + loopnodedsc, 
          mqsDscExpected[loopnodedsc], mqsDscComputed[loopnodedsc], TOLERANCE_PV_DELTA);
    }
    int nbNodeFwd3 = mqFwd3.length;
    double[] mqsFwd3Computed = mqs.getSensitivity(nameFwd3, USD).getData();
    double[] mqsFwd3Expected = new double[nbNodeFwd3];
    for (int loopnodefwd = 0; loopnodefwd < nbNodeFwd3; loopnodefwd++) {
      double[] mqFwdShifted = mqFwd3.clone();
      mqFwdShifted[loopnodefwd] += SHIFT_2;
      MulticurveProviderDiscount multicurveShifted = 
          StandardDataSetsMulticurveUSD.getCurvesUSDOisSpreadL3(mqDsc, mqFwdShifted).getFirst();
      double pvShifted = swap.accept(PVDC, multicurveShifted).getAmount(USD).getAmount();
      mqsFwd3Expected[loopnodefwd] = (pvShifted - pv) / SHIFT_2;
      assertEquals("ParameterSensitivityUnderlyingParameterCalculator - " + loopnodefwd, 
          mqsFwd3Expected[loopnodefwd], mqsFwd3Computed[loopnodefwd], TOLERANCE_PV_DELTA);
    }
  }

  @Test
  /** Tests the cross-gamma calculator vs bump and recompute. */
  public void crossGamma() {
    MulticurveProviderDiscount singleCurve = MulticurveProviderDiscountDataSets.createSingleCurveZcUsd();
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 5, 14);
    final SwapFixedCoupon<Coupon> swap = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate);
    String name = singleCurve.getAllNames().iterator().next();
    Currency ccy = singleCurve.getCurrencyForName(name).get(0);
    YieldAndDiscountCurve curve = singleCurve.getCurve(name);
    ArgChecker.isTrue(curve instanceof YieldCurve, "curve should be YieldCurve");
    YieldCurve yieldCurve = (YieldCurve) curve;
    ArgChecker.isTrue(yieldCurve.getCurve() instanceof InterpolatedDoublesCurve,
        "Yield curve should be based on InterpolatedDoublesCurve");
    InterpolatedDoublesCurve interpolatedCurve = (InterpolatedDoublesCurve) yieldCurve.getCurve();
    double[] y = interpolatedCurve.getYDataAsPrimitive();
    double[] x = interpolatedCurve.getXDataAsPrimitive();
    int nbNode = y.length;
    double[][] gammaComputed = CGC.calculateCrossGamma(swap, singleCurve).getData();
    double[][] gammaExpected = new double[nbNode][nbNode];
    for (int i = 0; i < nbNode; i++) {
      for (int j = 0; j < nbNode; j++) {
        double[][] pv = new double[2][2];
        for (int pmi = 0; pmi < 2; pmi++) {
          for (int pmj = 0; pmj < 2; pmj++) {
            final double[] yieldBumpedPP = y.clone();
            yieldBumpedPP[i] += ((pmi == 0) ? SHIFT : -SHIFT);
            yieldBumpedPP[j] += ((pmj == 0) ? SHIFT : -SHIFT);
            final YieldAndDiscountCurve curveBumped = new YieldCurve(name,
                new InterpolatedDoublesCurve(x, yieldBumpedPP, interpolatedCurve.getInterpolator(), true));
            MulticurveProviderDiscount providerBumped = new MulticurveProviderDiscount();
            for (Currency loopccy : singleCurve.getCurrencies()) {
              providerBumped.setCurve(loopccy, curveBumped);
            }
            for (IborIndex loopibor : singleCurve.getIndexesIbor()) {
              providerBumped.setCurve(loopibor, curveBumped);
            }
            pv[pmi][pmj] = swap.accept(PVDC, providerBumped).getAmount(ccy).getAmount();
          }
        }
        gammaExpected[i][j] = (pv[0][0] - pv[1][0] - pv[0][1] + pv[1][1]) / (2 * SHIFT * 2 * SHIFT);
      }
    }
    for (int i = 0; i < nbNode; i++) {
      for (int j = 0; j < nbNode; j++) {
        if (Math.abs(gammaExpected[i][j]) > 1 || Math.abs(gammaComputed[i][j]) > 1) { // Check only the meaningful numbers
          assertTrue("CrossGammaSingleCurveCalculator - " + i + " - " + j + " / " + gammaExpected[i][j] + " - " + gammaComputed[i][j],
              (Math.abs(gammaExpected[i][j] / gammaComputed[i][j] - 1) < TOLERANCE_PV_GAMMA_RELATIF) || // Relative diff small
                  (Math.abs(gammaExpected[i][j] - gammaComputed[i][j]) < TOLERANCE_PV_GAMMA)); // Absolute diff small
        }
      }
    }
  }

  @Test
  public void parSpreadFixedIborBeforeFirstFixing() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 5, 14);
    final SwapFixedCoupon<Coupon> swap = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate);
    final double parSpread = swap.accept(PSMQDC, MULTICURVES);
    final SwapFixedIborDefinition swap0Definition = 
        SwapFixedIborDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, USD6MLIBOR3M, NOTIONAL, RATE_FIXED + parSpread, true);
    final SwapFixedCoupon<Coupon> swap0 = swap0Definition.toDerivative(referenceDate);
    final MultiCurrencyAmount pv = swap0.accept(PVDC, MULTICURVES);
    assertEquals("ParSpreadCalculator: fixed-coupon swap", pv.getAmount(swap.getFirstLeg().getCurrency()).getAmount(), 0, TOLERANCE_PV);
  }

  @Test
  public void parRateFixedIborBeforeFirstFixing() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 5, 14);
    final SwapFixedCoupon<Coupon> swap = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate);
    final double parRate = swap.accept(PRDC, MULTICURVES);
    final SwapFixedIborDefinition swapATMDefinition = 
        SwapFixedIborDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, USD6MLIBOR3M, NOTIONAL, parRate, true);
    final SwapFixedCoupon<Coupon> swapATM = swapATMDefinition.toDerivative(referenceDate);
    final MultiCurrencyAmount pv = swapATM.accept(PVDC, MULTICURVES);
    assertEquals("ParSpreadCalculator: fixed-coupon swap", pv.getAmount(swap.getFirstLeg().getCurrency()).getAmount(), 0, TOLERANCE_PV);
  }

  @Test
  public void parSpreadFixedIborAfterFirstFixing() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 5, 16);
    final SwapFixedCoupon<Coupon> swap = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate, FIXING_TS_3_6);
    final double parSpread = swap.accept(PSMQDC, MULTICURVES);
    final SwapFixedIborDefinition swap0Definition = 
        SwapFixedIborDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, USD6MLIBOR3M, NOTIONAL, RATE_FIXED + parSpread, true);
    final SwapFixedCoupon<Coupon> swap0 = swap0Definition.toDerivative(referenceDate, FIXING_TS_3_6);
    final MultiCurrencyAmount pv = swap0.accept(PVDC, MULTICURVES);
    assertEquals("ParSpreadCalculator: fixed-coupon swap", pv.getAmount(swap.getFirstLeg().getCurrency()).getAmount(), 0, TOLERANCE_PV);
  }

  @Test
  public void parSpreadIborSpreadIborSpreadBeforeFirstFixing() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 5, 14);
    final Swap<Coupon, Coupon> swap = SWAP_IBORSPREAD_IBORSPREAD_DEFINITION.toDerivative(referenceDate);
    final double parSpread = swap.accept(PSMQDC, MULTICURVES);
    final SwapIborIborDefinition swap0Definition = new SwapIborIborDefinition(
        AnnuityCouponIborSpreadDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, NOTIONAL, USDLIBOR3M, SPREAD3 + parSpread, true, NYC),
        AnnuityCouponIborSpreadDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, NOTIONAL, USDLIBOR6M, SPREAD6, false, NYC));
    final Swap<Coupon, Coupon> swap0 = swap0Definition.toDerivative(referenceDate);
    final MultiCurrencyAmount pv = swap0.accept(PVDC, MULTICURVES);
    assertEquals("ParSpreadCalculator: fixed-coupon swap", pv.getAmount(swap.getFirstLeg().getCurrency()).getAmount(), 0, TOLERANCE_PV);
  }

  @Test
  public void parSpreadIborSpreadIborSpreadAfterFirstFixing() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 5, 16);
    final Swap<Coupon, Coupon> swap = SWAP_IBORSPREAD_IBORSPREAD_DEFINITION.toDerivative(referenceDate, FIXING_TS_3_6);
    final double parSpread = swap.accept(PSMQDC, MULTICURVES);
    final SwapIborIborDefinition swap0Definition = new SwapIborIborDefinition(
        AnnuityCouponIborSpreadDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, NOTIONAL, USDLIBOR3M, SPREAD3 + parSpread, true, NYC),
        AnnuityCouponIborSpreadDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, NOTIONAL, USDLIBOR6M, SPREAD6, false, NYC));
    final Swap<Coupon, Coupon> swap0 = swap0Definition.toDerivative(referenceDate, FIXING_TS_3_6);
    final MultiCurrencyAmount pv = swap0.accept(PVDC, MULTICURVES);
    assertEquals("ParSpreadCalculator: fixed-coupon swap", pv.getAmount(swap.getFirstLeg().getCurrency()).getAmount(), 0, TOLERANCE_PV);
  }

  @SuppressWarnings("unchecked")
  @Test
  /** Test for a swap with first leg without spread and par spread computed on that leg. */
  public void parSpreadIborIborBeforeFirstFixing() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 5, 14);
    final Swap<? extends Payment, ? extends Payment> swap = 
        new Swap<>((Annuity<Payment>) SWAP_IBOR_IBORSPREAD_DEFINITION.getFirstLeg().toDerivative(referenceDate),
        (Annuity<Payment>) SWAP_IBOR_IBORSPREAD_DEFINITION.getSecondLeg().toDerivative(referenceDate));
    final double parSpread = swap.accept(PSMQDC, MULTICURVES);
    final SwapIborIborDefinition swap0Definition = 
        new SwapIborIborDefinition(AnnuityCouponIborSpreadDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, NOTIONAL, USDLIBOR3M, parSpread, true, NYC),
        AnnuityCouponIborSpreadDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, NOTIONAL, USDLIBOR6M, SPREAD6, false, NYC));
    final Swap<Coupon, Coupon> swap0 = swap0Definition.toDerivative(referenceDate);
    final MultiCurrencyAmount pv = swap0.accept(PVDC, MULTICURVES);
    assertEquals("ParSpreadCalculator: fixed-coupon swap", pv.getAmount(swap.getFirstLeg().getCurrency()).getAmount(), 0, TOLERANCE_PV);
  }

  @Test
  public void pv01CurveParametersBeforeFirstFixing() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 5, 14);
    final SwapFixedCoupon<Coupon> swap = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate);
    final ReferenceAmount<Pair<String, Currency>> pv01Computed = swap.accept(PV01CPC, MULTICURVES);
    final ReferenceAmount<Pair<String, Currency>> pv01Expected = new ReferenceAmount<>();
    final MultipleCurrencyParameterSensitivity pvps = PSPVC.calculateSensitivity(swap, MULTICURVES, MULTICURVES.getAllNames());
    for (final Pair<String, Currency> nameCcy : pvps.getAllNamesCurrency()) {
      double total = 0.0;
      final double[] array = pvps.getSensitivity(nameCcy).getData();
      for (final double element : array) {
        total += element;
      }
      total *= BP1;
      pv01Expected.add(nameCcy, total);
    }
    assertEquals("PV01CurveParametersCalculator: fixed-coupon swap", pv01Expected, pv01Computed);
  }

  @Test
  public void todayPaymentFixedIborBeforeFirstFixing() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 5, 14);
    final SwapFixedCoupon<Coupon> swap = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate);
    final MultiCurrencyAmount cash = swap.accept(TPC);
    assertEquals("TodayPaymentCalculator: fixed-coupon swap", 0.0, cash.getAmount(USDLIBOR3M.getCurrency()).getAmount(), TOLERANCE_PV);
    assertEquals("TodayPaymentCalculator: fixed-coupon swap", 1, cash.size());
  }

  @Test
  public void todayPaymentFixedIborOnFirstIborPayment() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 8, 17);
    final SwapFixedCoupon<Coupon> swap = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate, FIXING_TS_3_6);
    final MultiCurrencyAmount cash = swap.accept(TPC);
    assertEquals("TodayPaymentCalculator: fixed-coupon swap", 0.0100 * NOTIONAL * 
        SWAP_FIXED_IBOR_DEFINITION.getIborLeg().getNthPayment(0).getPaymentYearFraction(),
        cash.getAmount(USDLIBOR3M.getCurrency()).getAmount(), TOLERANCE_PV);
    assertEquals("TodayPaymentCalculator: fixed-coupon swap", 1, cash.size());
  }

  @Test
  public void todayPaymentFixedIborOnFirstFixedPayment() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 11, 19);
    final SwapFixedCoupon<Coupon> swap = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate, FIXING_TS_3_6);
    final MultiCurrencyAmount cash = swap.accept(TPC);
    assertEquals("TodayPaymentCalculator: fixed-coupon swap", 
        SWAP_FIXED_IBOR_DEFINITION.getFixedLeg().getNthPayment(0).getAmount() + 0.0140 * NOTIONAL
        * SWAP_FIXED_IBOR_DEFINITION.getIborLeg().getNthPayment(1).getPaymentYearFraction(), 
        cash.getAmount(USDLIBOR3M.getCurrency()).getAmount(), TOLERANCE_PV);
    assertEquals("TodayPaymentCalculator: fixed-coupon swap", 1, cash.size());
  }

  @Test
  public void todayPaymentFixedIborBetweenPayments() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 11, 14);
    final SwapFixedCoupon<Coupon> swap = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate, FIXING_TS_3_6);
    final MultiCurrencyAmount cash = swap.accept(TPC);
    assertEquals("TodayPaymentCalculator: fixed-coupon swap", 0.0, cash.getAmount(USDLIBOR3M.getCurrency()).getAmount(), TOLERANCE_PV);
    assertEquals("TodayPaymentCalculator: fixed-coupon swap", 1, cash.size());
  }

}

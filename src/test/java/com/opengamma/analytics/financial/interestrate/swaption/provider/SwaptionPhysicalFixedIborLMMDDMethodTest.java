/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.provider;

import static org.testng.AssertJUnit.assertEquals;

import java.time.Period;
import java.time.ZonedDateTime;
import java.util.List;

import org.testng.annotations.Test;

import cern.jet.random.engine.MersenneTwister;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.interestrate.swap.provider.SwapFixedCouponDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.method.SwaptionPhysicalFixedIborBasketMethod;
import com.opengamma.analytics.financial.model.interestrate.TestsDataSetLiborMarketModelDisplacedDiffusion;
import com.opengamma.analytics.financial.model.interestrate.definition.LiborMarketModelDisplacedDiffusionParameters;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.BlackImpliedVolatilityFormula;
import com.opengamma.analytics.financial.montecarlo.provider.LiborMarketModelMonteCarloMethod;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.libormarket.PresentValueCurveSensitivityLMMDDCalculator;
import com.opengamma.analytics.financial.provider.calculator.libormarket.PresentValueLMMDDCalculator;
import com.opengamma.analytics.financial.provider.calculator.sabrswaption.PresentValueSABRSwaptionCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.SABRDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.LiborMarketModelDisplacedDiffusionProvider;
import com.opengamma.analytics.financial.provider.description.interestrate.LiborMarketModelDisplacedDiffusionProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.LiborMarketModelDisplacedDiffusionProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSwaptionProvider;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSwaptionProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSwaptionProviderInterface;
import com.opengamma.analytics.financial.provider.method.CalibrationEngineWithCalculators;
import com.opengamma.analytics.financial.provider.method.SuccessiveRootFinderLMMDDCalibrationEngine;
import com.opengamma.analytics.financial.provider.method.SuccessiveRootFinderLMMDDCalibrationObjective;
import com.opengamma.analytics.financial.provider.sensitivity.libormarket.ParameterSensitivityLMMDDDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.analytics.math.random.NormalRandomNumberGenerator;
import com.opengamma.analytics.util.time.DateUtils;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.currency.MultiCurrencyAmount;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.collect.tuple.DoublesPair;

/**
 * Tests related to the pricing of physical delivery swaption in LMM displaced diffusion.
 */
@Test
public class SwaptionPhysicalFixedIborLMMDDMethodTest {

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final IborIndex EURIBOR6M = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd()[1];
  private static final Currency EUR = EURIBOR6M.getCurrency();
  private static final HolidayCalendar TARGET = MulticurveProviderDiscountDataSets.getEURCalendar();

  private static final GeneratorSwapFixedIbor EUR1YEURIBOR6M = GeneratorSwapFixedIborMaster.getInstance().getGenerator("EUR1YEURIBOR6M", TARGET);

  // Swaption 5Yx5Y
  private static final int SWAP_TENOR_YEAR = 5;
  private static final Period SWAP_TENOR = Period.ofYears(SWAP_TENOR_YEAR);
  private static final ZonedDateTime EXPIRY_DATE = DateUtils.getUTCDate(2016, 7, 7);
  private static final ZonedDateTime SETTLEMENT_DATE = ScheduleCalculator.getAdjustedDate(EXPIRY_DATE, EURIBOR6M.getSpotLag(), TARGET);
  private static final double NOTIONAL = 100000000; //100m
  private static final double RATE = 0.0200;
  private static final boolean FIXED_IS_PAYER = true;
  private static final SwapFixedIborDefinition SWAP_PAYER_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, EUR1YEURIBOR6M, NOTIONAL, RATE, FIXED_IS_PAYER);
  private static final SwapFixedIborDefinition SWAP_RECEIVER_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, EUR1YEURIBOR6M, NOTIONAL, RATE, !FIXED_IS_PAYER);
  private static final boolean IS_LONG = true;
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_PAYER_LONG_DEFINITION = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_PAYER_DEFINITION, true, IS_LONG);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_RECEIVER_LONG_DEFINITION = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_RECEIVER_DEFINITION, false, IS_LONG);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_PAYER_SHORT_DEFINITION = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_PAYER_DEFINITION, true, !IS_LONG);
  //to derivatives
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 7, 7);

  private static final SwapFixedCoupon<Coupon> SWAP_RECEIVER = SWAP_RECEIVER_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final SwaptionPhysicalFixedIbor SWAPTION_PAYER_LONG = SWAPTION_PAYER_LONG_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final SwaptionPhysicalFixedIbor SWAPTION_RECEIVER_LONG = SWAPTION_RECEIVER_LONG_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final SwaptionPhysicalFixedIbor SWAPTION_PAYER_SHORT = SWAPTION_PAYER_SHORT_DEFINITION.toDerivative(REFERENCE_DATE);
  // Parameters and methods
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final ParRateDiscountingCalculator PRDC = ParRateDiscountingCalculator.getInstance();
  private static final SwaptionPhysicalFixedIborLMMDDMethod METHOD_LMM = SwaptionPhysicalFixedIborLMMDDMethod.getInstance();
  private static final SwaptionPhysicalFixedIborSABRMethod METHOD_SABR = SwaptionPhysicalFixedIborSABRMethod.getInstance();
  private static final PresentValueSABRSwaptionCalculator PVSSC = PresentValueSABRSwaptionCalculator.getInstance();
  private static final PresentValueLMMDDCalculator PVLLC = PresentValueLMMDDCalculator.getInstance();
  private static final PresentValueCurveSensitivityLMMDDCalculator PVCSLLC = PresentValueCurveSensitivityLMMDDCalculator.getInstance();

  private static final double SHIFT = 1.0E-7;
  private static final ParameterSensitivityParameterCalculator<LiborMarketModelDisplacedDiffusionProviderInterface> PS_LL_C = new ParameterSensitivityParameterCalculator<>(
      PVCSLLC);
  private static final ParameterSensitivityLMMDDDiscountInterpolatedFDCalculator PS_LL_FDC = new ParameterSensitivityLMMDDDiscountInterpolatedFDCalculator(PVLLC, SHIFT);

  private static final LiborMarketModelDisplacedDiffusionParameters PARAMETERS_LMM = TestsDataSetLiborMarketModelDisplacedDiffusion.createLMMParameters(REFERENCE_DATE,
      SWAP_PAYER_DEFINITION.getIborLeg());
  private static final LiborMarketModelDisplacedDiffusionProviderDiscount LMM_MULTICURVES = new LiborMarketModelDisplacedDiffusionProviderDiscount(MULTICURVES, PARAMETERS_LMM, EUR);

  private static final SABRInterestRateParameters SABR_PARMETERS = SABRDataSets.createSABR1();
  private static final SABRSwaptionProviderDiscount SABR_MULTICURVES = new SABRSwaptionProviderDiscount(MULTICURVES, SABR_PARMETERS, EUR1YEURIBOR6M);

  private static final int NB_PATH = 12500;
  private static final SwapFixedCouponDiscountingMethod METHOD_SWAP = SwapFixedCouponDiscountingMethod.getInstance();
  private static final double[] MONEYNESS = new double[] {-0.0100, 0, 0.0100 };
  private static final SwaptionPhysicalFixedIborSABRLMMLeastSquareMethod METHOD_SABR_LMM_ATBEST = new SwaptionPhysicalFixedIborSABRLMMLeastSquareMethod(MONEYNESS, PARAMETERS_LMM);
  private static final SwaptionPhysicalFixedIborBasketMethod METHOD_BASKET = SwaptionPhysicalFixedIborBasketMethod.getInstance();

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_DELTA = 1.0E+2;

  @Test
  /**
   * Test the present value.
   */
  public void presentValue() {
    final double pvPreviousRun = 2000842.564; // Mean reversion 0.001: 4367793.468;
    final MultiCurrencyAmount pv = METHOD_LMM.presentValue(SWAPTION_PAYER_LONG, LMM_MULTICURVES);
    assertEquals("Swaption physical - LMM - present value", pvPreviousRun, pv.getAmount(EUR).getAmount(), TOLERANCE_PV);
  }

  @Test
  /**
   * Test the present value: approximated formula vs Monte Carlo.
   */
  public void presentValueMC() {
    LiborMarketModelMonteCarloMethod methodLmmMc;
    methodLmmMc = new LiborMarketModelMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), NB_PATH);
    final MultiCurrencyAmount pvMC = methodLmmMc.presentValue(SWAPTION_PAYER_LONG, EUR, LMM_MULTICURVES);
    final double pvMCPreviousRun = 1997241.514;
    assertEquals("Swaption physical - LMM - present value Monte Carlo", pvMCPreviousRun, pvMC.getAmount(EUR).getAmount(), TOLERANCE_PV);
    final MultiCurrencyAmount pvApprox = METHOD_LMM.presentValue(SWAPTION_PAYER_LONG, LMM_MULTICURVES);
    final double pvbp = METHOD_SWAP.presentValueBasisPoint(SWAP_RECEIVER, MULTICURVES);
    final double forward = SWAP_RECEIVER.accept(PRDC, MULTICURVES);
    final BlackFunctionData data = new BlackFunctionData(forward, pvbp, 0.20);
    final EuropeanVanillaOption option = new EuropeanVanillaOption(RATE, SWAPTION_PAYER_LONG.getTimeToExpiry(), FIXED_IS_PAYER);
    final BlackImpliedVolatilityFormula implied = new BlackImpliedVolatilityFormula();
    final double impliedVolMC = implied.getImpliedVolatility(data, option, pvMC.getAmount(EUR).getAmount());
    final double impliedVolApprox = implied.getImpliedVolatility(data, option, pvApprox.getAmount(EUR).getAmount());
    assertEquals("Swaption physical - LMM - present value Approximation/Monte Carlo", impliedVolMC, impliedVolApprox, 2.0E-3);
  }

  @Test
  /**
   * Tests long/short parity.
   */
  public void longShortParity() {
    final MultiCurrencyAmount pvLong = METHOD_LMM.presentValue(SWAPTION_PAYER_LONG, LMM_MULTICURVES);
    final MultiCurrencyAmount pvShort = METHOD_LMM.presentValue(SWAPTION_PAYER_SHORT, LMM_MULTICURVES);
    assertEquals("Swaption physical - LMM - present value - long/short parity", pvLong.getAmount(EUR).getAmount(), -pvShort.getAmount(EUR).getAmount(), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests payer/receiver/swap parity.
   */
  public void payerReceiverParity() {
    final MultiCurrencyAmount pvReceiverLong = METHOD_LMM.presentValue(SWAPTION_RECEIVER_LONG, LMM_MULTICURVES);
    final MultiCurrencyAmount pvPayerShort = METHOD_LMM.presentValue(SWAPTION_PAYER_SHORT, LMM_MULTICURVES);
    final MultiCurrencyAmount pvSwap = SWAP_RECEIVER.accept(PVDC, MULTICURVES);
    assertEquals("Swaption physical - LMM - present value - payer/receiver/swap parity", pvReceiverLong.getAmount(EUR).getAmount() + pvPayerShort.getAmount(EUR).getAmount(), pvSwap.getAmount(EUR)
        .getAmount(), TOLERANCE_PV);
  }

  @Test
  /**
   * Test the present value LMM volatility parameters sensitivity.
   */
  public void presentValueLMMSensitivity() {
    final double[][] pvLmmSensi = METHOD_LMM.presentValueLMMSensitivity(SWAPTION_PAYER_LONG, LMM_MULTICURVES);

    final double shift = 1.0E-6;
    final LiborMarketModelDisplacedDiffusionParameters parameterShiftPlus = TestsDataSetLiborMarketModelDisplacedDiffusion.createLMMParametersShiftVol(REFERENCE_DATE,
        SWAP_PAYER_DEFINITION.getIborLeg(), shift);
    final LiborMarketModelDisplacedDiffusionProviderDiscount bundleShiftPlus = new LiborMarketModelDisplacedDiffusionProviderDiscount(MULTICURVES, parameterShiftPlus, EUR);
    final MultiCurrencyAmount pvShiftPlus = METHOD_LMM.presentValue(SWAPTION_PAYER_LONG, bundleShiftPlus);
    final LiborMarketModelDisplacedDiffusionParameters parameterShiftMinus = TestsDataSetLiborMarketModelDisplacedDiffusion.createLMMParametersShiftVol(REFERENCE_DATE,
        SWAP_PAYER_DEFINITION.getIborLeg(), -shift);
    final LiborMarketModelDisplacedDiffusionProviderDiscount bundleShiftMinus = new LiborMarketModelDisplacedDiffusionProviderDiscount(MULTICURVES, parameterShiftMinus, EUR);
    final MultiCurrencyAmount pvShiftMinus = METHOD_LMM.presentValue(SWAPTION_PAYER_LONG, bundleShiftMinus);
    final double pvLmmSensiTotExpected = (pvShiftPlus.getAmount(EUR).getAmount() - pvShiftMinus.getAmount(EUR).getAmount()) / (2 * shift);
    double pvLmmSensiTot = 0.0;
    for (final double[] element : pvLmmSensi) {
      for (int loopfact = 0; loopfact < pvLmmSensi[0].length; loopfact++) {
        pvLmmSensiTot += element[loopfact];
      }
    }
    assertEquals("Swaption physical - LMM - present value Lmm parameters sensitivity", pvLmmSensiTotExpected, pvLmmSensiTot, TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Test the present value displaced diffusion parameters sensitivity.
   */
  public void presentValueDDSensitivity() {
    final double[] pvDDSensi = METHOD_LMM.presentValueDDSensitivity(SWAPTION_PAYER_LONG, LMM_MULTICURVES);
    final double shift = 1.0E-6;
    final LiborMarketModelDisplacedDiffusionParameters parameterShiftPlus = TestsDataSetLiborMarketModelDisplacedDiffusion.createLMMParametersShiftDis(REFERENCE_DATE,
        SWAP_PAYER_DEFINITION.getIborLeg(), shift);
    final LiborMarketModelDisplacedDiffusionProviderDiscount bundleShiftPlus = new LiborMarketModelDisplacedDiffusionProviderDiscount(MULTICURVES, parameterShiftPlus, EUR);
    final MultiCurrencyAmount pvShiftPlus = METHOD_LMM.presentValue(SWAPTION_PAYER_LONG, bundleShiftPlus);
    final LiborMarketModelDisplacedDiffusionParameters parameterShiftMinus = TestsDataSetLiborMarketModelDisplacedDiffusion.createLMMParametersShiftDis(REFERENCE_DATE,
        SWAP_PAYER_DEFINITION.getIborLeg(), -shift);
    final LiborMarketModelDisplacedDiffusionProviderDiscount bundleShiftMinus = new LiborMarketModelDisplacedDiffusionProviderDiscount(MULTICURVES, parameterShiftMinus, EUR);
    final MultiCurrencyAmount pvShiftMinus = METHOD_LMM.presentValue(SWAPTION_PAYER_LONG, bundleShiftMinus);
    final double pvDDSensiTotExpected = (pvShiftPlus.getAmount(EUR).getAmount() - pvShiftMinus.getAmount(EUR).getAmount()) / (2 * shift);
    double pvDDSensiTot = 0.0;
    for (final double element : pvDDSensi) {
      pvDDSensiTot += element;
    }
    assertEquals("Swaption physical - LMM - present value displacement parameters sensitivity", pvDDSensiTotExpected, pvDDSensiTot, TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Test the present value curve sensitivity.
   */
  public void presentValueCurveSensitivity() {
    final MultipleCurrencyParameterSensitivity pvpsExact = PS_LL_C.calculateSensitivity(SWAPTION_PAYER_LONG, LMM_MULTICURVES, LMM_MULTICURVES.getMulticurveProvider().getAllNames());
    final MultipleCurrencyParameterSensitivity pvpsFD = PS_LL_FDC.calculateSensitivity(SWAPTION_PAYER_LONG, LMM_MULTICURVES);
    AssertSensitivityObjects.assertEquals("SwaptionPhysicalFixedIborLMMDDMethod: presentValueCurveSensitivity ", pvpsExact, pvpsFD, TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Calibrate and price an amortized swaption.
   */
  public void calibrateExactPriceAmortized() {
    final int[] swapTenorYear = {1, 2, 3, 4, 5 };
    final double[] amortization = new double[] {1.00, 0.80, 0.60, 0.40, 0.20 }; // For 5Y amortization
    final SwapFixedIborDefinition[] swapCalibrationDefinition = new SwapFixedIborDefinition[swapTenorYear.length];
    final SwaptionPhysicalFixedIborDefinition[] swaptionCalibrationDefinition = new SwaptionPhysicalFixedIborDefinition[swapTenorYear.length];
    final SwaptionPhysicalFixedIbor[] swaptionCalibration = new SwaptionPhysicalFixedIbor[swapTenorYear.length];
    for (int loopexp = 0; loopexp < swapTenorYear.length; loopexp++) {
      swapCalibrationDefinition[loopexp] = SwapFixedIborDefinition.from(SETTLEMENT_DATE, Period.ofYears(loopexp + 1), EUR1YEURIBOR6M, NOTIONAL, RATE, FIXED_IS_PAYER);
      swaptionCalibrationDefinition[loopexp] = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, swapCalibrationDefinition[loopexp], FIXED_IS_PAYER, IS_LONG);
      swaptionCalibration[loopexp] = swaptionCalibrationDefinition[loopexp].toDerivative(REFERENCE_DATE);
    }
    final CouponFixed[] cpnFixed = new CouponFixed[swapTenorYear.length];
    final AnnuityCouponFixed legFixed = swaptionCalibration[swapTenorYear.length - 1].getUnderlyingSwap().getFixedLeg();
    final CouponIbor[] cpnIbor = new CouponIbor[2 * swapTenorYear.length];
    @SuppressWarnings("unchecked")
    final Annuity<Payment> legIbor = (Annuity<Payment>) swaptionCalibration[swapTenorYear.length - 1].getUnderlyingSwap().getSecondLeg();
    for (int loopexp = 0; loopexp < swapTenorYear.length; loopexp++) {
      cpnFixed[loopexp] = legFixed.getNthPayment(loopexp).withNotional(legFixed.getNthPayment(loopexp).getNotional() * amortization[loopexp]);
      cpnIbor[2 * loopexp] = ((CouponIbor) legIbor.getNthPayment(2 * loopexp)).withNotional(((CouponIbor) legIbor.getNthPayment(2 * loopexp)).getNotional() * amortization[loopexp]);
      cpnIbor[2 * loopexp + 1] = ((CouponIbor) legIbor.getNthPayment(2 * loopexp + 1)).withNotional(((CouponIbor) legIbor.getNthPayment(2 * loopexp + 1)).getNotional() * amortization[loopexp]);
    }

    final SwapFixedCoupon<Coupon> swapAmortized = new SwapFixedCoupon<>(new AnnuityCouponFixed(cpnFixed), new Annuity<Coupon>(cpnIbor));
    final SwaptionPhysicalFixedIbor swaptionAmortized = SwaptionPhysicalFixedIbor.from(swaptionCalibration[0].getTimeToExpiry(), swapAmortized, swaptionCalibration[0].getSettlementTime(), IS_LONG);

    final SwaptionPhysicalFixedIbor[] swaptionCalibration2 = METHOD_BASKET.calibrationBasketFixedLegPeriod(swaptionAmortized);

    assertEquals("Calibration basket", swaptionCalibration.length, swaptionCalibration2.length);
    for (int loopcal = 0; loopcal < swaptionCalibration.length; loopcal++) {
      assertEquals("Calibration basket: " + loopcal, METHOD_SABR.presentValue(swaptionCalibration[loopcal], SABR_MULTICURVES).getAmount(EUR).getAmount(),
          METHOD_SABR.presentValue(swaptionCalibration2[loopcal], SABR_MULTICURVES).getAmount(EUR).getAmount(), TOLERANCE_PV);
    }
    // Calibration and price
    final LiborMarketModelDisplacedDiffusionParameters lmmParameters = TestsDataSetLiborMarketModelDisplacedDiffusion.createLMMParametersDisplacementAngle(REFERENCE_DATE,
        swapCalibrationDefinition[swapTenorYear.length - 1].getIborLeg(), 0.10, Math.PI / 2);
    final SuccessiveRootFinderLMMDDCalibrationObjective objective = new SuccessiveRootFinderLMMDDCalibrationObjective(lmmParameters, EUR);
    final CalibrationEngineWithCalculators<SABRSwaptionProviderInterface> calibrationEngine = new SuccessiveRootFinderLMMDDCalibrationEngine<>(objective);
    calibrationEngine.addInstrument(swaptionCalibration2, PVSSC);
    calibrationEngine.calibrate(SABR_MULTICURVES);
    final LiborMarketModelDisplacedDiffusionProviderInterface lmm = new LiborMarketModelDisplacedDiffusionProvider(MULTICURVES, lmmParameters, EUR);
    final MultiCurrencyAmount pvAmortized = METHOD_LMM.presentValue(swaptionAmortized, lmm);
    final double pvAmortizedPrevious = 1125007.920;
    assertEquals("LMM Amortized pricing", pvAmortizedPrevious, pvAmortized.getAmount(EUR).getAmount(), TOLERANCE_PV);
    // Method
    final SwaptionPhysicalFixedIborSABRLMMExactMethod method = new SwaptionPhysicalFixedIborSABRLMMExactMethod();
    final MultiCurrencyAmount pvAmortizedMethod = method.presentValue(swaptionAmortized, SABR_MULTICURVES);
    assertEquals("LMM Amortized pricing", pvAmortized.getAmount(EUR).getAmount(), pvAmortizedMethod.getAmount(EUR).getAmount(), TOLERANCE_PV);

    // SABR parameters sensitivity in all-in-one method.
    final List<Object> results = method.presentValueCurveSABRSensitivity(swaptionAmortized, SABR_MULTICURVES);
    final MultipleCurrencyMulticurveSensitivity pvcs1 = (MultipleCurrencyMulticurveSensitivity) results.get(1);
    final PresentValueSABRSensitivityDataBundle pvss1 = (PresentValueSABRSensitivityDataBundle) results.get(2);

    // SABR parameters sensitivity
    final PresentValueSABRSensitivityDataBundle pvss = method.presentValueSABRSensitivity(swaptionAmortized, SABR_MULTICURVES);

    // SABR parameters sensitivity (all-in-one)
    for (final SwaptionPhysicalFixedIbor element : swaptionCalibration) {
      final DoublesPair expiryMaturity = DoublesPair.of(element.getTimeToExpiry(), element.getMaturityTime());
      assertEquals("Sensitivity swaption pv to alpha", pvss1.getAlpha().getMap().get(expiryMaturity), pvss.getAlpha().getMap().get(expiryMaturity), 1E-2);
      assertEquals("Sensitivity swaption pv to rho", pvss1.getRho().getMap().get(expiryMaturity), pvss.getRho().getMap().get(expiryMaturity), 1E-2);
      assertEquals("Sensitivity swaption pv to nu", pvss1.getNu().getMap().get(expiryMaturity), pvss.getNu().getMap().get(expiryMaturity), 1E-2);
    }
    // SABR parameters sensitivity (parallel shift check)
    SABRInterestRateParameters sabrParameterShift;
    SABRSwaptionProviderDiscount sabrBundleShift;
    final LiborMarketModelDisplacedDiffusionParameters lmmParametersShift = TestsDataSetLiborMarketModelDisplacedDiffusion.createLMMParametersDisplacementAngle(REFERENCE_DATE,
        swapCalibrationDefinition[swapTenorYear.length - 1].getIborLeg(), 0.10, Math.PI / 2);
    final SuccessiveRootFinderLMMDDCalibrationObjective objectiveShift = new SuccessiveRootFinderLMMDDCalibrationObjective(lmmParametersShift, EUR);
    final CalibrationEngineWithCalculators<SABRSwaptionProviderInterface> calibrationEngineShift = new SuccessiveRootFinderLMMDDCalibrationEngine<>(objectiveShift);
    calibrationEngineShift.addInstrument(swaptionCalibration2, PVSSC);
    final LiborMarketModelDisplacedDiffusionProvider lmmBundleShift = new LiborMarketModelDisplacedDiffusionProvider(MULTICURVES, lmmParametersShift, EUR);

    double alphaVegaTotalComputed = 0.0;
    assertEquals("Number of alpha sensitivity", pvss.getAlpha().getMap().keySet().size(), swaptionCalibration.length);
    for (final SwaptionPhysicalFixedIbor element : swaptionCalibration) {
      final DoublesPair expiryMaturity = DoublesPair.of(element.getTimeToExpiry(), element.getMaturityTime());
      alphaVegaTotalComputed += pvss.getAlpha().getMap().get(expiryMaturity);
    }
    final double shiftAlpha = 0.00001;
    sabrParameterShift = SABRDataSets.createSABR1AlphaBumped(shiftAlpha);
    sabrBundleShift = new SABRSwaptionProviderDiscount(MULTICURVES, sabrParameterShift, EUR1YEURIBOR6M);
    calibrationEngineShift.calibrate(sabrBundleShift);
    final MultiCurrencyAmount pvAmortizedShiftAlpha = METHOD_LMM.presentValue(swaptionAmortized, lmmBundleShift);
    final double alphaVegaTotalExpected = (pvAmortizedShiftAlpha.getAmount(EUR).getAmount() - pvAmortized.getAmount(EUR).getAmount()) / shiftAlpha;
    assertEquals("Alpha sensitivity value", alphaVegaTotalExpected, alphaVegaTotalComputed, 5 * TOLERANCE_PV_DELTA);

    double rhoVegaTotalComputed = 0.0;
    assertEquals("Number of alpha sensitivity", pvss.getRho().getMap().keySet().size(), swaptionCalibration.length);
    for (final SwaptionPhysicalFixedIbor element : swaptionCalibration) {
      final DoublesPair expiryMaturity = DoublesPair.of(element.getTimeToExpiry(), element.getMaturityTime());
      rhoVegaTotalComputed += pvss.getRho().getMap().get(expiryMaturity);
    }
    final double shiftRho = 0.00001;
    sabrParameterShift = SABRDataSets.createSABR1RhoBumped(shiftRho);
    sabrBundleShift = new SABRSwaptionProviderDiscount(MULTICURVES, sabrParameterShift, EUR1YEURIBOR6M);
    calibrationEngineShift.calibrate(sabrBundleShift);
    final MultiCurrencyAmount pvAmortizedShiftRho = METHOD_LMM.presentValue(swaptionAmortized, lmmBundleShift);
    final double rhoVegaTotalExpected = (pvAmortizedShiftRho.getAmount(EUR).getAmount() - pvAmortized.getAmount(EUR).getAmount()) / shiftRho;
    assertEquals("Rho sensitivity value", rhoVegaTotalExpected, rhoVegaTotalComputed, TOLERANCE_PV_DELTA);

    double nuVegaTotalComputed = 0.0;
    assertEquals("Number of alpha sensitivity", pvss.getNu().getMap().keySet().size(), swaptionCalibration.length);
    for (final SwaptionPhysicalFixedIbor element : swaptionCalibration) {
      final DoublesPair expiryMaturity = DoublesPair.of(element.getTimeToExpiry(), element.getMaturityTime());
      nuVegaTotalComputed += pvss.getNu().getMap().get(expiryMaturity);
    }
    final double shiftNu = 0.00001;
    sabrParameterShift = SABRDataSets.createSABR1NuBumped(shiftNu);
    sabrBundleShift = new SABRSwaptionProviderDiscount(MULTICURVES, sabrParameterShift, EUR1YEURIBOR6M);
    calibrationEngineShift.calibrate(sabrBundleShift);
    final MultiCurrencyAmount pvAmortizedShiftNu = METHOD_LMM.presentValue(swaptionAmortized, lmmBundleShift);
    final double nuVegaTotalExpected = (pvAmortizedShiftNu.getAmount(EUR).getAmount() - pvAmortized.getAmount(EUR).getAmount()) / shiftNu;
    assertEquals("Nu sensitivity value", nuVegaTotalExpected, nuVegaTotalComputed, TOLERANCE_PV_DELTA);

    // Curve sensitivity
    MultipleCurrencyMulticurveSensitivity pvcs = method.presentValueCurveSensitivity(swaptionAmortized, SABR_MULTICURVES);
    pvcs = pvcs.cleaned();
    // Curve sensitivity (all-in-one)
    AssertSensitivityObjects.assertEquals("presentValueCurveSensitivity", pvcs1, pvcs, TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Calibrate and price an amortized swaption.
   */
  public void calibrateAtBestPriceAmortized() {
    final double[] amortization = new double[] {1.00, 0.80, 0.60, 0.40, 0.20 }; // For 5Y amortization
    final int nbPeriods = amortization.length;
    final SwapFixedIborDefinition swapDefinition = SwapFixedIborDefinition.from(SETTLEMENT_DATE, Period.ofYears(nbPeriods), EUR1YEURIBOR6M, NOTIONAL, RATE, FIXED_IS_PAYER);
    final CouponFixedDefinition[] cpnFixed = new CouponFixedDefinition[nbPeriods];
    final AnnuityCouponFixedDefinition legFixed = swapDefinition.getFixedLeg();
    final CouponIborDefinition[] cpnIbor = new CouponIborDefinition[2 * nbPeriods];
    final AnnuityDefinition<? extends PaymentDefinition> legIbor = swapDefinition.getSecondLeg();
    for (int loopexp = 0; loopexp < nbPeriods; loopexp++) {
      cpnFixed[loopexp] = legFixed.getNthPayment(loopexp).withNotional(legFixed.getNthPayment(loopexp).getNotional() * amortization[loopexp]);
      cpnIbor[2 * loopexp] = ((CouponIborDefinition) legIbor.getNthPayment(2 * loopexp))
          .withNotional(((CouponIborDefinition) legIbor.getNthPayment(2 * loopexp)).getNotional() * amortization[loopexp]);
      cpnIbor[2 * loopexp + 1] = ((CouponIborDefinition) legIbor.getNthPayment(2 * loopexp + 1)).withNotional(((CouponIborDefinition) legIbor.getNthPayment(2 * loopexp + 1)).getNotional()
          * amortization[loopexp]);
    }
    final SwapFixedIborDefinition swapAmortizedDefinition = new SwapFixedIborDefinition(new AnnuityCouponFixedDefinition(cpnFixed, TARGET), new AnnuityCouponIborDefinition(cpnIbor, EURIBOR6M, TARGET));
    final SwaptionPhysicalFixedIborDefinition swaptionAmortizedDefinition = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, swapAmortizedDefinition, FIXED_IS_PAYER, IS_LONG);
    final SwaptionPhysicalFixedIbor swaptionAmortized = swaptionAmortizedDefinition.toDerivative(REFERENCE_DATE);

    // SABR parameters sensitivity (parallel shift check). The sensitivities are not exact; in the approximation a small "second order" term is ignored
    final PresentValueSABRSensitivityDataBundle pvss = METHOD_SABR_LMM_ATBEST.presentValueSABRSensitivity(swaptionAmortized, SABR_MULTICURVES);
    final double[] shift = new double[] {0.0001, 0.0001, 0.0001 };
    final double[] toleranceSABRSensi = new double[] {5.0E+4, 5.0E+3, 1.0E+4 };
    final double[] sensiComputed = new double[] {pvss.getAlpha().toSingleValue(), pvss.getRho().toSingleValue(), pvss.getNu().toSingleValue() };
    final double[] sensiExpected = new double[shift.length];
    SABRInterestRateParameters sabrParameterShift;
    SABRSwaptionProvider sabrBundleShift;
    for (int loopp = 0; loopp < shift.length; loopp++) {
      sabrParameterShift = SABRDataSets.createSABR1ParameterBumped(shift[loopp], loopp);
      sabrBundleShift = new SABRSwaptionProvider(MULTICURVES, sabrParameterShift, EUR1YEURIBOR6M);
      final MultiCurrencyAmount pvShiftPlus = METHOD_SABR_LMM_ATBEST.presentValue(swaptionAmortized, sabrBundleShift);
      sabrParameterShift = SABRDataSets.createSABR1ParameterBumped(-shift[loopp], loopp);
      sabrBundleShift = new SABRSwaptionProvider(MULTICURVES, sabrParameterShift, EUR1YEURIBOR6M);
      final MultiCurrencyAmount pvShiftMinus = METHOD_SABR_LMM_ATBEST.presentValue(swaptionAmortized, sabrBundleShift);
      sensiExpected[loopp] = (pvShiftPlus.getAmount(EUR).getAmount() - pvShiftMinus.getAmount(EUR).getAmount()) / (2 * shift[loopp]);
      assertEquals("SwaptionPhysicalFixedIborLMM: Calibration at best - SABR sensitivity " + loopp, sensiExpected[loopp], sensiComputed[loopp], toleranceSABRSensi[loopp]);
    }

  }

}

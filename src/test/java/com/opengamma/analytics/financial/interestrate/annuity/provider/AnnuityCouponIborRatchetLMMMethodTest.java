/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.annuity.provider;

import static org.testng.AssertJUnit.assertEquals;

import java.time.Period;
import java.time.ZonedDateTime;

import cern.jet.random.engine.MersenneTwister;
import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityCapFloorIborDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborRatchetDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponIborRatchet;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.provider.CapFloorIborLMMDDMethod;
import com.opengamma.analytics.financial.model.interestrate.TestsDataSetLiborMarketModelDisplacedDiffusion;
import com.opengamma.analytics.financial.model.interestrate.definition.LiborMarketModelDisplacedDiffusionParameters;
import com.opengamma.analytics.financial.montecarlo.provider.LiborMarketModelMonteCarloMethod;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.LiborMarketModelDisplacedDiffusionProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.math.random.NormalRandomNumberGenerator;
import com.opengamma.analytics.util.time.DateUtils;
import com.opengamma.analytics.util.timeseries.DoubleTimeSeries;
import com.opengamma.analytics.util.timeseries.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.currency.MultiCurrencyAmount;
import com.opengamma.strata.basics.date.HolidayCalendar;

/**
 * Tests the Libor Market Model method for Annuity on Ibor Ratchet.
 */
@Test
public class AnnuityCouponIborRatchetLMMMethodTest {

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final IborIndex EURIBOR3M = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd()[0];
  private static final Currency EUR = EURIBOR3M.getCurrency();
  private static final HolidayCalendar TARGET = MulticurveProviderDiscountDataSets.getEURCalendar();

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 9, 5);

  // Annuity description
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2011, 9, 7);
  private static final int ANNUITY_TENOR_YEAR = 2;
  private static final Period ANNUITY_TENOR = Period.ofYears(ANNUITY_TENOR_YEAR);
  private static final boolean IS_PAYER = false;
  private static final double NOTIONAL = 100000000; // 100m
  private static final double[] MAIN_COEF = new double[] {0.20, 0.80, 0.0010 };
  private static final double[] FLOOR_COEF = new double[] {0.50, 0.00, 0.0200 };
  private static final double[] CAP_COEF = new double[] {1.00, 0.00, 0.0300 };
  private static final double FIRST_CPN_RATE = 0.04;
  private static final AnnuityCouponIborRatchetDefinition ANNUITY_RATCHET_FIXED_DEFINITION = AnnuityCouponIborRatchetDefinition.withFirstCouponFixed(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL,
      EURIBOR3M, IS_PAYER, FIRST_CPN_RATE, MAIN_COEF, FLOOR_COEF, CAP_COEF, TARGET);
  private static final AnnuityCouponIborRatchetDefinition ANNUITY_RATCHET_IBOR_DEFINITION = AnnuityCouponIborRatchetDefinition.withFirstCouponIborGearing(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL,
      EURIBOR3M, IS_PAYER, MAIN_COEF, FLOOR_COEF, CAP_COEF, TARGET);
  // Curves and derivatives
  private static final DoubleTimeSeries<ZonedDateTime> FIXING_TS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {REFERENCE_DATE }, new double[] {FIRST_CPN_RATE });
  private static final AnnuityCouponIborRatchet ANNUITY_RATCHET_FIXED = ANNUITY_RATCHET_FIXED_DEFINITION.toDerivative(REFERENCE_DATE, FIXING_TS);
  // Methods and calculators
  private static final int NB_PATH = 12500;
  private static final LiborMarketModelDisplacedDiffusionParameters PARAMETERS_LMM = TestsDataSetLiborMarketModelDisplacedDiffusion.createLMMParameters(REFERENCE_DATE,
      ANNUITY_RATCHET_FIXED_DEFINITION);
  private static final LiborMarketModelDisplacedDiffusionProviderDiscount LMM_MULTICURVES = new LiborMarketModelDisplacedDiffusionProviderDiscount(MULTICURVES, PARAMETERS_LMM, EUR);

  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_MC = 5.0E+3;

  /**
   * Test the Ratchet present value in the case where the first coupon is fixed. Tested against a previous run number.
   */
  public void presentValueFixed() {
    LiborMarketModelMonteCarloMethod methodMC;
    methodMC = new LiborMarketModelMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), NB_PATH);
    // Seed fixed to the DEFAULT_SEED for testing purposes.
    final MultiCurrencyAmount pvMC = methodMC.presentValue(ANNUITY_RATCHET_FIXED, EUR, LMM_MULTICURVES);
    final double pvMCPreviousRun = 8030175.607;
    assertEquals("Annuity Ratchet Ibor - LMM - Monte Carlo", pvMCPreviousRun, pvMC.getAmount(EUR).getAmount(), TOLERANCE_PV);
  }

  public void presentValueIbor() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 8, 18);
    final AnnuityCouponIborRatchet annuityRatchetIbor = ANNUITY_RATCHET_IBOR_DEFINITION.toDerivative(referenceDate, FIXING_TS);
    final LiborMarketModelDisplacedDiffusionParameters parameterLMM = TestsDataSetLiborMarketModelDisplacedDiffusion.createLMMParameters(referenceDate, ANNUITY_RATCHET_FIXED_DEFINITION);
    final LiborMarketModelDisplacedDiffusionProviderDiscount bundleLMM = new LiborMarketModelDisplacedDiffusionProviderDiscount(MULTICURVES, parameterLMM, EUR);
    final LiborMarketModelMonteCarloMethod methodMC = new LiborMarketModelMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), NB_PATH);
    // Seed fixed to the DEFAULT_SEED for testing purposes.
    final MultiCurrencyAmount pvMC = methodMC.presentValue(annuityRatchetIbor, EUR, bundleLMM);
    final double pvMCPreviousRun = 7675269.115;
    assertEquals("Annuity Ratchet Ibor - LMM - Monte Carlo", pvMCPreviousRun, pvMC.getAmount(EUR).getAmount(), TOLERANCE_PV);
  }

  /**
   * Test the Ratchet present value in the degenerate case where the coupon are fixed (floor=cap).
   */
  public void presentValueFixedLeg() {
    final int nbPath = 12500;
    final LiborMarketModelMonteCarloMethod methodMC = new LiborMarketModelMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), nbPath);
    final double[] mainFixed = new double[] {0.0, 0.0, 0.0 };
    final double[] floorFixed = new double[] {0.0, 0.0, FIRST_CPN_RATE };
    final double[] capFixed = new double[] {0.0, 0.0, FIRST_CPN_RATE };
    final AnnuityCouponIborRatchetDefinition ratchetFixedDefinition = AnnuityCouponIborRatchetDefinition.withFirstCouponFixed(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, EURIBOR3M, IS_PAYER,
        FIRST_CPN_RATE, mainFixed, floorFixed, capFixed, TARGET);
    final AnnuityCouponIborRatchet ratchetFixed = ratchetFixedDefinition.toDerivative(REFERENCE_DATE, FIXING_TS);
    final AnnuityCouponFixedDefinition fixedDefinition = AnnuityCouponFixedDefinition.from(EUR, SETTLEMENT_DATE, ANNUITY_TENOR, EURIBOR3M.getTenor(), TARGET, EURIBOR3M.getDayCount(),
        EURIBOR3M.getBusinessDayConvention(), EURIBOR3M.isEndOfMonth(), NOTIONAL, FIRST_CPN_RATE, IS_PAYER);
    final AnnuityCouponFixed fixed = fixedDefinition.toDerivative(REFERENCE_DATE);
    final MultiCurrencyAmount pvFixedExpected = fixed.accept(PVDC, MULTICURVES);
    final MultiCurrencyAmount pvFixedMC = methodMC.presentValue(ratchetFixed, EUR, LMM_MULTICURVES);
    assertEquals("Annuity Ratchet Ibor - LMM - Monte Carlo - Degenerate in Fixed leg", pvFixedExpected.getAmount(EUR).getAmount(), pvFixedMC.getAmount(EUR).getAmount(), TOLERANCE_PV_MC);
    // For 500,000 path the difference is xxx
  }

  /**
   * Test the Ratchet present value in the degenerate case where the coupon are ibor (no cap/floor, ibor factor=1.0).
   */
  public void presentValueIborLeg() {
    final int nbPath = 12500;
    final double[] mainIbor = new double[] {0.0, 1.0, 0.0 };
    final double[] floorIbor = new double[] {0.0, 0.0, -10.0 };
    final double[] capIbor = new double[] {0.0, 0.0, +50.0 };
    final AnnuityCouponIborRatchetDefinition ratchetFixedDefinition = AnnuityCouponIborRatchetDefinition.withFirstCouponFixed(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, EURIBOR3M, IS_PAYER,
        FIRST_CPN_RATE, mainIbor, floorIbor, capIbor, TARGET);
    final AnnuityCouponIborRatchet ratchetFixed = ratchetFixedDefinition.toDerivative(REFERENCE_DATE, FIXING_TS);
    final AnnuityCouponIborDefinition iborDefinition = AnnuityCouponIborDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, EURIBOR3M, IS_PAYER, TARGET);
    final Annuity<? extends Coupon> ibor = iborDefinition.toDerivative(REFERENCE_DATE, FIXING_TS);
    final Coupon[] iborFirstFixed = new Coupon[ibor.getNumberOfPayments()];
    iborFirstFixed[0] = ratchetFixed.getNthPayment(0);
    for (int loopcpn = 1; loopcpn < ibor.getNumberOfPayments(); loopcpn++) {
      iborFirstFixed[loopcpn] = ibor.getNthPayment(loopcpn);
    }
    final LiborMarketModelDisplacedDiffusionParameters parameterLMM = TestsDataSetLiborMarketModelDisplacedDiffusion.createLMMParameters(REFERENCE_DATE, ratchetFixedDefinition);
    final LiborMarketModelDisplacedDiffusionProviderDiscount bundleLMM = new LiborMarketModelDisplacedDiffusionProviderDiscount(MULTICURVES, parameterLMM, EUR);
    final LiborMarketModelMonteCarloMethod methodMC = new LiborMarketModelMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), nbPath);
    final MultiCurrencyAmount pvIborMC = methodMC.presentValue(ratchetFixed, EUR, bundleLMM);
    final MultiCurrencyAmount pvIborExpected = new Annuity<Payment>(iborFirstFixed).accept(PVDC, MULTICURVES);
    assertEquals("Annuity Ratchet Ibor - Hull-White - Monte Carlo - Degenerate in Ibor leg", pvIborExpected.getAmount(EUR).getAmount(), pvIborMC.getAmount(EUR).getAmount(), TOLERANCE_PV_MC);
    // For 500,000 path the difference is xxx
  }

  /**
   * Test the Ratchet present value in the degenerate case where the coupon are 0.65*Ibor floored.
   */
  public void presentValueFloorFixed() {
    final int nbPath = 12500;
    final double strike = 0.04;
    final double factor = 0.65;
    final double[] mainIbor = new double[] {0.0, factor, 0.0 };
    final double[] floorIbor = new double[] {0.0, 0.0, factor * strike };
    final double[] capIbor = new double[] {0.0, 0.0, +50.0 };
    final AnnuityCouponIborRatchetDefinition ratchetFixedDefinition = AnnuityCouponIborRatchetDefinition.withFirstCouponFixed(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, EURIBOR3M, IS_PAYER,
        FIRST_CPN_RATE, mainIbor, floorIbor, capIbor, TARGET);
    final AnnuityCouponIborRatchet ratchetFixed = ratchetFixedDefinition.toDerivative(REFERENCE_DATE, FIXING_TS);
    final AnnuityCapFloorIborDefinition capDefinition = AnnuityCapFloorIborDefinition.from(SETTLEMENT_DATE, SETTLEMENT_DATE.plus(ANNUITY_TENOR), NOTIONAL, EURIBOR3M, IS_PAYER, strike, true, TARGET);
    final Annuity<? extends Payment> cap = capDefinition.toDerivative(REFERENCE_DATE, FIXING_TS);
    final LiborMarketModelMonteCarloMethod methodMC = new LiborMarketModelMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), nbPath);
    final CapFloorIborLMMDDMethod methodCapLMM = CapFloorIborLMMDDMethod.getInstance();
    final AnnuityCouponFixedDefinition fixedDefinition = AnnuityCouponFixedDefinition.from(EUR, SETTLEMENT_DATE, ANNUITY_TENOR, EURIBOR3M.getTenor(), TARGET, EURIBOR3M.getDayCount(),
        EURIBOR3M.getBusinessDayConvention(), EURIBOR3M.isEndOfMonth(), NOTIONAL, strike, IS_PAYER);
    final AnnuityCouponFixed fixed = fixedDefinition.toDerivative(REFERENCE_DATE);
    MultiCurrencyAmount pvFlooredExpected = MultiCurrencyAmount.of(EUR, 0.0);
    pvFlooredExpected = pvFlooredExpected.plus(ratchetFixed.getNthPayment(0).accept(PVDC, MULTICURVES));
    for (int loopcpn = 1; loopcpn < cap.getNumberOfPayments(); loopcpn++) {
      pvFlooredExpected = pvFlooredExpected.plus(methodCapLMM.presentValue((CapFloorIbor) cap.getNthPayment(loopcpn), LMM_MULTICURVES).multipliedBy(factor));
      pvFlooredExpected = pvFlooredExpected.plus(fixed.getNthPayment(loopcpn).accept(PVDC, MULTICURVES).multipliedBy(factor));
    }
    final MultiCurrencyAmount pvFloorMC = methodMC.presentValue(ratchetFixed, EUR, LMM_MULTICURVES);
    assertEquals("Annuity Ratchet Ibor - Hull-White - LMM - Degenerate in floor leg", pvFlooredExpected.getAmount(EUR).getAmount(), pvFloorMC.getAmount(EUR).getAmount(), TOLERANCE_PV_MC);
    // For 500,000 path the difference is xxx
  }

}

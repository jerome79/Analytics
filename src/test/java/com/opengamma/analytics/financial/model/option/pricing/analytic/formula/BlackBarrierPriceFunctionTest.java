/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic.formula;

import static com.opengamma.strata.collect.TestHelper.assertThrowsIllegalArg;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.time.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.option.definition.Barrier;
import com.opengamma.analytics.financial.model.option.definition.Barrier.BarrierType;
import com.opengamma.analytics.financial.model.option.definition.Barrier.KnockType;
import com.opengamma.analytics.financial.model.option.definition.Barrier.ObservationType;
import com.opengamma.analytics.financial.model.option.definition.EuropeanStandardBarrierOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.financial.model.option.pricing.analytic.AnalyticOptionModel;
import com.opengamma.analytics.financial.model.option.pricing.analytic.EuropeanStandardBarrierOptionModel;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.analytics.util.time.DateUtils;
import com.opengamma.analytics.util.time.Expiry;

/**
 * Test.
 */
@Test
public class BlackBarrierPriceFunctionTest {
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 7, 1);
  private static final ZonedDateTime EXPIRY_DATE = DateUtils.getUTCDate(2015, 1, 2);
  private static final double EXPIRY_TIME = DateUtils.getDifferenceInYears(REFERENCE_DATE, EXPIRY_DATE);
  private static final double STRIKE_MID = 100;
  private static final double STRIKE_HIGH = 120;
  private static final boolean IS_CALL = true;
  private static final EuropeanVanillaOption VANILLA_CALL_K100 = new EuropeanVanillaOption(STRIKE_MID, EXPIRY_TIME, IS_CALL);
  private static final EuropeanVanillaOption VANILLA_PUT_K100 = new EuropeanVanillaOption(STRIKE_MID, EXPIRY_TIME, !IS_CALL);
  private static final EuropeanVanillaOption VANILLA_PUT_KHI = new EuropeanVanillaOption(STRIKE_HIGH, EXPIRY_TIME, !IS_CALL);
  private static final Barrier BARRIER_DOWN_IN = new Barrier(KnockType.IN, BarrierType.DOWN, ObservationType.CONTINUOUS, 90);
  private static final Barrier BARRIER_DOWN_OUT = new Barrier(KnockType.OUT, BarrierType.DOWN, ObservationType.CONTINUOUS, 90);
  private static final Barrier BARRIER_UP_IN = new Barrier(KnockType.IN, BarrierType.UP, ObservationType.CONTINUOUS, 110);
  private static final Barrier BARRIER_UP_OUT = new Barrier(KnockType.OUT, BarrierType.UP, ObservationType.CONTINUOUS, 110);
  private static final double REBATE = 2;
  private static final double SPOT = 105;
  private static final double RATE_DOM = 0.05; // Domestic rate
  private static final double RATE_FOR = 0.02; // Foreign rate
  private static final double COST_OF_CARRY = RATE_DOM - RATE_FOR; // Domestic - Foreign rate
  private static final double VOLATILITY = 0.20;
  private static final BlackBarrierPriceFunction BARRIER_FUNCTION = BlackBarrierPriceFunction.getInstance();

  private static final double DF_FOR = Math.exp(-RATE_FOR * EXPIRY_TIME); // 'Base Ccy
  private static final double DF_DOM = Math.exp(-RATE_DOM * EXPIRY_TIME); // 'Quote Ccy
  private static final double FWD_FX = SPOT * DF_FOR / DF_DOM;
  private static final BlackFunctionData DATA_BLACK = new BlackFunctionData(FWD_FX, DF_DOM, VOLATILITY);
  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();

  @Test
  /** Tests the 'In-Out Parity' condition: Without rebates, the price of a Knock-In plus a Knock-Out of arbitrary barrier level must equal that of the underlying vanilla option */
  public void inOutParityWithoutRebate() {

    // Vanilla
    final Function1D<BlackFunctionData, Double> fcnVanillaCall = BLACK_FUNCTION.getPriceFunction(VANILLA_CALL_K100);
    final double pxVanillaCall = fcnVanillaCall.evaluate(DATA_BLACK);

    // Barriers without rebate
    final double noRebate = 0.0;
    final double priceDownIn = BARRIER_FUNCTION.getPrice(VANILLA_CALL_K100, BARRIER_DOWN_IN, noRebate, SPOT, COST_OF_CARRY, RATE_DOM, VOLATILITY);
    final double priceDownOut = BARRIER_FUNCTION.getPrice(VANILLA_CALL_K100, BARRIER_DOWN_OUT, noRebate, SPOT, COST_OF_CARRY, RATE_DOM, VOLATILITY);
    assertEquals("Knock In-Out Parity fails", 1.0, pxVanillaCall / (priceDownIn + priceDownOut), 1.e-6);
  }

  @Test
  /**
   * Tests the 'In-Out Parity' condition: Knock-In's pay rebate at maturity if barrier isn't hit. Knock-Out pays at moment barrier is hit.
   * The discounting issue can be sidestepped by setting rates to 0.
   */
  public void inOutParityWithRebate() {

    // Vanilla
    final Function1D<BlackFunctionData, Double> fcnVanillaCall = BLACK_FUNCTION.getPriceFunction(VANILLA_CALL_K100);
    final BlackFunctionData zeroRatesMarket = new BlackFunctionData(SPOT, 1.0, VOLATILITY);
    final double pxVanillaCall = fcnVanillaCall.evaluate(zeroRatesMarket);

    // Barriers with rebate
    final double priceDownInRebate = BARRIER_FUNCTION.getPrice(VANILLA_CALL_K100, BARRIER_DOWN_IN, REBATE, SPOT, 0.0, 0.0, VOLATILITY);
    final double priceDownOutRebate = BARRIER_FUNCTION.getPrice(VANILLA_CALL_K100, BARRIER_DOWN_OUT, REBATE, SPOT, 0.0, 0.0, VOLATILITY);
    assertEquals("Knock In-Out Parity fails", 1.0, (pxVanillaCall + REBATE) / (priceDownInRebate + priceDownOutRebate), 1.e-6);
  }

  @Test
  /** Tests the 'In-Out Parity' condition: The price of a Knock-In plus a Knock-Out of arbitrary barrier level must equal that of the underlying vanilla option + value of the rebate */
  public void inOutParityMorePathsWithRebate() {

    // Market with zero rates, domestic and foreign
    final BlackFunctionData zeroRatesMarket = new BlackFunctionData(SPOT, 1.0, VOLATILITY);
    final double rateDomestic = 0.0;
    final double rateForeign = 0.0;
    final double costOfCarry = rateDomestic - rateForeign;

    // Rebate
    final double pxRebate = REBATE;
    // 2 - Vanillas - Call and Put
    final Function1D<BlackFunctionData, Double> fcnVanillaCall = BLACK_FUNCTION.getPriceFunction(VANILLA_CALL_K100);
    final double pxVanillaCall = fcnVanillaCall.evaluate(zeroRatesMarket);
    final Function1D<BlackFunctionData, Double> fcnVanillaPut = BLACK_FUNCTION.getPriceFunction(VANILLA_PUT_K100);
    final double pxVanillaPut = fcnVanillaPut.evaluate(zeroRatesMarket);
    // Barriers: Up and Down, Call and Put, In and Out
    final double pxDownInCall = BARRIER_FUNCTION.getPrice(VANILLA_CALL_K100, BARRIER_DOWN_IN, REBATE, SPOT, costOfCarry, rateDomestic, VOLATILITY);
    final double pxDownOutCall = BARRIER_FUNCTION.getPrice(VANILLA_CALL_K100, BARRIER_DOWN_OUT, REBATE, SPOT, costOfCarry, rateDomestic, VOLATILITY);
    assertEquals("Knock In-Out Parity fails", 1.0, (pxVanillaCall + pxRebate) / (pxDownInCall + pxDownOutCall), 1.e-6);

    final double pxDownInPut = BARRIER_FUNCTION.getPrice(VANILLA_PUT_K100, BARRIER_DOWN_IN, REBATE, SPOT, costOfCarry, rateDomestic, VOLATILITY);
    final double pxDownOutPut = BARRIER_FUNCTION.getPrice(VANILLA_PUT_K100, BARRIER_DOWN_OUT, REBATE, SPOT, costOfCarry, rateDomestic, VOLATILITY);
    assertTrue("Knock In-Out Parity fails", Math.abs((pxVanillaPut + pxRebate) / (pxDownInPut + pxDownOutPut) - 1) < 1.e-6);

    final double pxUpInCall = BARRIER_FUNCTION.getPrice(VANILLA_CALL_K100, BARRIER_UP_IN, REBATE, SPOT, costOfCarry, rateDomestic, VOLATILITY);
    final double pxUpOutCall = BARRIER_FUNCTION.getPrice(VANILLA_CALL_K100, BARRIER_UP_OUT, REBATE, SPOT, costOfCarry, rateDomestic, VOLATILITY);
    assertTrue("Knock In-Out Parity fails", Math.abs((pxVanillaCall + pxRebate) / (pxUpInCall + pxUpOutCall) - 1) < 1.e-6);

    final double pxUpInPut = BARRIER_FUNCTION.getPrice(VANILLA_PUT_K100, BARRIER_UP_IN, REBATE, SPOT, costOfCarry, rateDomestic, VOLATILITY);
    final double pxUpOutPut = BARRIER_FUNCTION.getPrice(VANILLA_PUT_K100, BARRIER_UP_OUT, REBATE, SPOT, costOfCarry, rateDomestic, VOLATILITY);
    assertTrue("Knock In-Out Parity fails", Math.abs((pxVanillaPut + pxRebate) / (pxUpInPut + pxUpOutPut) - 1) < 1.e-6);

    final Function1D<BlackFunctionData, Double> fcnVanillaPutHiK = BLACK_FUNCTION.getPriceFunction(VANILLA_PUT_KHI);
    final double pxVanillaPutHiK = fcnVanillaPutHiK.evaluate(zeroRatesMarket);

    final double pxUpInPutHiK = BARRIER_FUNCTION.getPrice(VANILLA_PUT_KHI, BARRIER_UP_IN, REBATE, SPOT, costOfCarry, rateDomestic, VOLATILITY);
    final double pxUpOutPutHiK = BARRIER_FUNCTION.getPrice(VANILLA_PUT_KHI, BARRIER_UP_OUT, REBATE, SPOT, costOfCarry, rateDomestic, VOLATILITY);
    assertTrue("Knock In-Out Parity fails", Math.abs((pxVanillaPutHiK + pxRebate) / (pxUpInPutHiK + pxUpOutPutHiK) - 1) < 1.e-6);
  }

  @Test
  /** Tests the 'In-Out Parity' condition: The price of a Knock-In plus a Knock-Out of arbitrary barrier level must equal that of the underlying vanilla option + value of the rebate */
  public void impossibleToHitBarrierIsVanilla() {

    final Barrier veryLowKnockIn = new Barrier(KnockType.IN, BarrierType.DOWN, ObservationType.CONTINUOUS, 1e-6);
    final Barrier veryLowKnockOut = new Barrier(KnockType.OUT, BarrierType.DOWN, ObservationType.CONTINUOUS, 1e-6);
    final Barrier veryHighKnockIn = new Barrier(KnockType.IN, BarrierType.UP, ObservationType.CONTINUOUS, 1e6);
    final Barrier veryHighKnockOut = new Barrier(KnockType.OUT, BarrierType.UP, ObservationType.CONTINUOUS, 1e6);

    final double pxRebate = DF_DOM * REBATE;
    final Function1D<BlackFunctionData, Double> fcnVanillaCall = BLACK_FUNCTION.getPriceFunction(VANILLA_CALL_K100);
    final double pxVanillaCall = fcnVanillaCall.evaluate(DATA_BLACK);

    // KnockIn's with impossible to reach barrier's are guaranteed to pay the rebate at maturity
    final double pxDownInPut = BARRIER_FUNCTION.getPrice(VANILLA_PUT_K100, veryLowKnockIn, REBATE, SPOT, COST_OF_CARRY, RATE_DOM, VOLATILITY);
    assertTrue("VeryLowKnockInBarrier doesn't match rebate", pxDownInPut / pxRebate - 1 < 1e-6);
    final double pxDownInCall = BARRIER_FUNCTION.getPrice(VANILLA_CALL_K100, veryLowKnockIn, REBATE, SPOT, COST_OF_CARRY, RATE_DOM, VOLATILITY);
    assertTrue("VeryLowKnockInBarrier doesn't match rebate", pxDownInCall / pxRebate - 1 < 1e-6);
    final double pxUpInCall = BARRIER_FUNCTION.getPrice(VANILLA_CALL_K100, veryHighKnockIn, REBATE, SPOT, COST_OF_CARRY, RATE_DOM, VOLATILITY);
    assertTrue("VeryHighKnockInBarrier doesn't match rebate", pxUpInCall / pxRebate - 1 < 1e-6);

    // KnockOut's with impossible to reach barrier's are guaranteed to pay the value of the underlying vanilla
    final double pxDownOutCall = BARRIER_FUNCTION.getPrice(VANILLA_CALL_K100, veryLowKnockOut, REBATE, SPOT, COST_OF_CARRY, RATE_DOM, VOLATILITY);
    assertTrue("VeryLowKnockInBarrier doesn't match rebate", Math.abs(pxDownOutCall / pxVanillaCall - 1) < 1e-6);

    // Derivatives
    final double[] derivs = new double[7];
    BARRIER_FUNCTION.getPriceAdjoint(VANILLA_CALL_K100, veryLowKnockIn, REBATE, SPOT, COST_OF_CARRY, RATE_DOM, VOLATILITY, derivs);
    assertTrue("Impossible KnockIn: rate sens is incorrect", derivs[2] / Math.abs((-1 * EXPIRY_TIME * DF_DOM * REBATE) - 1) < 1e-6);
    assertEquals("Impossible KnockIn: Encountered derivative, other than d/dr, != 0", 0.0, derivs[0] + derivs[1] + derivs[3] + derivs[4], 1.0e-6);

    BARRIER_FUNCTION.getPriceAdjoint(VANILLA_CALL_K100, veryHighKnockIn, REBATE, SPOT, COST_OF_CARRY, RATE_DOM, VOLATILITY, derivs);
    assertTrue("Impossible KnockIn: rate sens is incorrect", derivs[2] / Math.abs((-1 * EXPIRY_TIME * DF_DOM * REBATE) - 1) < 1e-6);
    assertEquals("Impossible KnockIn: Encountered derivative, other than d/dr, != 0", 0.0, derivs[0] + derivs[1] + derivs[3] + derivs[4], 1.0e-6);

    // Barrier: [0] spot, [1] strike, [2] rate, [3] cost-of-carry, [4] volatility.
    BARRIER_FUNCTION.getPriceAdjoint(VANILLA_CALL_K100, veryLowKnockOut, REBATE, SPOT, COST_OF_CARRY, RATE_DOM, VOLATILITY, derivs);
    // Vanilla: [0] the price, [1] the derivative with respect to the forward, [2] the derivative with respect to the volatility and [3] the derivative with respect to the strike.
    final double[] vanillaDerivs = BLACK_FUNCTION.getPriceAdjoint(VANILLA_CALL_K100, DATA_BLACK);
    assertEquals("Impossible KnockOut: Vega doesn't match vanilla", vanillaDerivs[2], derivs[4], 1e-6);
    assertEquals("Impossible KnockOut: Dual Delta (d/dK) doesn't match vanilla", vanillaDerivs[3], derivs[1], 1e-6);
    assertEquals("Impossible KnockOut: Delta doesn't match vanilla", vanillaDerivs[1] * DF_FOR / DF_DOM, derivs[0], 1e-6);

    BARRIER_FUNCTION.getPriceAdjoint(VANILLA_CALL_K100, veryHighKnockOut, REBATE, SPOT, COST_OF_CARRY, RATE_DOM, VOLATILITY, derivs);
    assertEquals("Impossible KnockOut: Vega doesn't match vanilla", vanillaDerivs[2], derivs[4], 1e-6);
    assertEquals("Impossible KnockOut: Dual Delta (d/dK) doesn't match vanilla", vanillaDerivs[3], derivs[1], 1e-6);
    assertEquals("Impossible KnockOut: Delta doesn't match vanilla", vanillaDerivs[1] * DF_FOR / DF_DOM, derivs[0], 1e-6);
  }

  @Test(enabled = false)
  /**
   * Tests the comparison with the other implementation. This test may be removed when only one version remains.
   */
  public void comparison() {
    final AnalyticOptionModel<EuropeanStandardBarrierOptionDefinition, StandardOptionDataBundle> model = new EuropeanStandardBarrierOptionModel();
    final StandardOptionDataBundle data = new StandardOptionDataBundle(YieldCurve.from(ConstantDoublesCurve.from(RATE_DOM)), COST_OF_CARRY, new VolatilitySurface(
        ConstantDoublesSurface.from(VOLATILITY)),
        SPOT, REFERENCE_DATE);
    final Expiry expiry = new Expiry(EXPIRY_DATE);

    final double priceDI1 = BARRIER_FUNCTION.getPrice(VANILLA_CALL_K100, BARRIER_DOWN_IN, REBATE, SPOT, COST_OF_CARRY, RATE_DOM, VOLATILITY);
    final EuropeanStandardBarrierOptionDefinition optionBarrierDI = new EuropeanStandardBarrierOptionDefinition(STRIKE_MID, expiry, IS_CALL, BARRIER_DOWN_IN, REBATE);
    final double priceDI2 = model.getPricingFunction(optionBarrierDI).evaluate(data);
    assertEquals("Comparison Down In", priceDI2, priceDI1, 1.0E-10);

    final double priceDO1 = BARRIER_FUNCTION.getPrice(VANILLA_CALL_K100, BARRIER_DOWN_OUT, REBATE, SPOT, COST_OF_CARRY, RATE_DOM, VOLATILITY);
    final EuropeanStandardBarrierOptionDefinition optionBarrierDO = new EuropeanStandardBarrierOptionDefinition(STRIKE_MID, expiry, IS_CALL, BARRIER_DOWN_OUT, REBATE);
    final double priceDO2 = model.getPricingFunction(optionBarrierDO).evaluate(data);
    assertEquals("Comparison Down Out", priceDO2, priceDO1, 1.0E-10);

    final double priceUI1 = BARRIER_FUNCTION.getPrice(VANILLA_CALL_K100, BARRIER_UP_IN, REBATE, SPOT, COST_OF_CARRY, RATE_DOM, VOLATILITY);
    final EuropeanStandardBarrierOptionDefinition optionBarrierUI = new EuropeanStandardBarrierOptionDefinition(STRIKE_MID, expiry, IS_CALL, BARRIER_UP_IN, REBATE);
    final double priceUI2 = model.getPricingFunction(optionBarrierUI).evaluate(data);
    assertEquals("Comparison Up In", priceUI2, priceUI1, 1.0E-10);

    final double priceUO1 = BARRIER_FUNCTION.getPrice(VANILLA_CALL_K100, BARRIER_UP_OUT, REBATE, SPOT, COST_OF_CARRY, RATE_DOM, VOLATILITY);
    final EuropeanStandardBarrierOptionDefinition optionBarrierUO = new EuropeanStandardBarrierOptionDefinition(STRIKE_MID, expiry, IS_CALL, BARRIER_UP_OUT, REBATE);
    final double priceUO2 = model.getPricingFunction(optionBarrierUO).evaluate(data);
    assertEquals("Comparison Up Out", priceUO2, priceUO1, 1.0E-10);

    final double vol0 = 0.0;
    final double priceVol01 = BARRIER_FUNCTION.getPrice(VANILLA_CALL_K100, BARRIER_DOWN_IN, REBATE, SPOT, COST_OF_CARRY, RATE_DOM, vol0);
    final StandardOptionDataBundle data0 = new StandardOptionDataBundle(YieldCurve.from(ConstantDoublesCurve.from(RATE_DOM)), COST_OF_CARRY, new VolatilitySurface(ConstantDoublesSurface.from(vol0)),
        SPOT,
        REFERENCE_DATE);
    final double priceVol02 = model.getPricingFunction(optionBarrierDI).evaluate(data0);
    assertEquals(priceVol02, priceVol01, 1.0E-10);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void exceptionDown() {
    BARRIER_FUNCTION.getPrice(VANILLA_CALL_K100, BARRIER_DOWN_IN, REBATE, 85.0, COST_OF_CARRY, RATE_DOM, VOLATILITY);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void exceptionUp() {
    final Barrier barrierUp = new Barrier(KnockType.IN, BarrierType.UP, ObservationType.CONTINUOUS, 90);
    BARRIER_FUNCTION.getPrice(VANILLA_CALL_K100, barrierUp, REBATE, SPOT, COST_OF_CARRY, RATE_DOM, VOLATILITY);
  }

  @Test
  /**
   * Tests the adjoint implementation (with computation of the derivatives).
   */
  public void adjointPrice() {
    final double[] derivatives = new double[7];
    final double priceDI = BARRIER_FUNCTION.getPrice(VANILLA_CALL_K100, BARRIER_DOWN_IN, REBATE, SPOT, COST_OF_CARRY, RATE_DOM, VOLATILITY);
    final double priceDIAdjoint = BARRIER_FUNCTION.getPriceAdjoint(VANILLA_CALL_K100, BARRIER_DOWN_IN, REBATE, SPOT, COST_OF_CARRY, RATE_DOM, VOLATILITY, derivatives);
    assertEquals("Black single barrier: Adjoint price Down In", priceDI, priceDIAdjoint, 1.0E-10);
    final double priceDO = BARRIER_FUNCTION.getPrice(VANILLA_CALL_K100, BARRIER_DOWN_OUT, REBATE, SPOT, COST_OF_CARRY, RATE_DOM, VOLATILITY);
    final double priceDOAdjoint = BARRIER_FUNCTION.getPriceAdjoint(VANILLA_CALL_K100, BARRIER_DOWN_OUT, REBATE, SPOT, COST_OF_CARRY, RATE_DOM, VOLATILITY, derivatives);
    assertEquals("Black single barrier: Adjoint price Down Out", priceDO, priceDOAdjoint, 1.0E-10);
    final double priceUI = BARRIER_FUNCTION.getPrice(VANILLA_CALL_K100, BARRIER_UP_IN, REBATE, SPOT, COST_OF_CARRY, RATE_DOM, VOLATILITY);
    final double priceUIAdjoint = BARRIER_FUNCTION.getPriceAdjoint(VANILLA_CALL_K100, BARRIER_UP_IN, REBATE, SPOT, COST_OF_CARRY, RATE_DOM, VOLATILITY, derivatives);
    assertEquals("Black single barrier: Adjoint price Up In", priceUI, priceUIAdjoint, 1.0E-10);
    final double priceUO = BARRIER_FUNCTION.getPrice(VANILLA_CALL_K100, BARRIER_UP_OUT, REBATE, SPOT, COST_OF_CARRY, RATE_DOM, VOLATILITY);
    final double priceUOAdjoint = BARRIER_FUNCTION.getPriceAdjoint(VANILLA_CALL_K100, BARRIER_UP_OUT, REBATE, SPOT, COST_OF_CARRY, RATE_DOM, VOLATILITY, derivatives);
    assertEquals("Black single barrier: Adjoint price Up Out", priceUO, priceUOAdjoint, 1.0E-10);
  }

  @Test
  /**
   * Tests the adjoint implementation (with computation of the derivatives).
   */
  public void adjointDerivatives() {
    final double shiftSpot = 0.001;
    final double shiftRate = 1.0E-8;
    final double shiftCoC = 1.0E-8;
    final double shiftVol = 1.0E-8;
    final double[] derivatives = new double[7];
    // DOWN-IN
    final double priceDI = BARRIER_FUNCTION.getPriceAdjoint(VANILLA_CALL_K100, BARRIER_DOWN_IN, REBATE, SPOT, COST_OF_CARRY, RATE_DOM, VOLATILITY, derivatives);
    final double priceDISpot = BARRIER_FUNCTION.getPrice(VANILLA_CALL_K100, BARRIER_DOWN_IN, REBATE, SPOT + shiftSpot, COST_OF_CARRY, RATE_DOM, VOLATILITY);
    assertEquals("Black single barrier: Adjoint spot derivative - Down In", (priceDISpot - priceDI) / shiftSpot, derivatives[0], 1.0E-5);
    final double priceDIRate = BARRIER_FUNCTION.getPrice(VANILLA_CALL_K100, BARRIER_DOWN_IN, REBATE, SPOT, COST_OF_CARRY, RATE_DOM + shiftRate, VOLATILITY);
    assertEquals("Black single barrier: Adjoint rate derivative - Down In", (priceDIRate - priceDI) / shiftRate, derivatives[2], 1.0E-5);
    final double priceDICoC = BARRIER_FUNCTION.getPrice(VANILLA_CALL_K100, BARRIER_DOWN_IN, REBATE, SPOT, COST_OF_CARRY + shiftCoC, RATE_DOM, VOLATILITY);
    assertEquals("Black single barrier: Adjoint cost-of-carry derivative - Down In", (priceDICoC - priceDI) / shiftCoC, derivatives[3], 1.0E-5);
    final double priceDIVol = BARRIER_FUNCTION.getPrice(VANILLA_CALL_K100, BARRIER_DOWN_IN, REBATE, SPOT, COST_OF_CARRY, RATE_DOM, VOLATILITY + shiftVol);
    assertEquals("Black single barrier: Adjoint cost-of-carry derivative - Down In", (priceDIVol - priceDI) / shiftVol, derivatives[4], 1.0E-4);
    // DOWN-OUT
    final double priceDO = BARRIER_FUNCTION.getPriceAdjoint(VANILLA_CALL_K100, BARRIER_DOWN_OUT, REBATE, SPOT, COST_OF_CARRY, RATE_DOM, VOLATILITY, derivatives);
    final double priceDOSpot = BARRIER_FUNCTION.getPrice(VANILLA_CALL_K100, BARRIER_DOWN_OUT, REBATE, SPOT + shiftSpot, COST_OF_CARRY, RATE_DOM, VOLATILITY);
    assertEquals("Black single barrier: Adjoint spot derivative - Down Out", (priceDOSpot - priceDO) / shiftSpot, derivatives[0], 2.0E-4);
    final double priceDORate = BARRIER_FUNCTION.getPrice(VANILLA_CALL_K100, BARRIER_DOWN_OUT, REBATE, SPOT, COST_OF_CARRY, RATE_DOM + shiftRate, VOLATILITY);
    assertEquals("Black single barrier: Adjoint rate derivative - Down Out", (priceDORate - priceDO) / shiftRate, derivatives[2], 1.0E-5);
    final double priceDOCoC = BARRIER_FUNCTION.getPrice(VANILLA_CALL_K100, BARRIER_DOWN_OUT, REBATE, SPOT, COST_OF_CARRY + shiftCoC, RATE_DOM, VOLATILITY);
    assertEquals("Black single barrier: Adjoint cost-of-carry derivative - Down Out", (priceDOCoC - priceDO) / shiftCoC, derivatives[3], 1.0E-4);
    final double priceDOVol = BARRIER_FUNCTION.getPrice(VANILLA_CALL_K100, BARRIER_DOWN_OUT, REBATE, SPOT, COST_OF_CARRY, RATE_DOM, VOLATILITY + shiftVol);
    assertEquals("Black single barrier: Adjoint cost-of-carry derivative - Down Out", (priceDOVol - priceDO) / shiftVol, derivatives[4], 1.0E-4);
    // UP-IN
    final double priceUI = BARRIER_FUNCTION.getPriceAdjoint(VANILLA_CALL_K100, BARRIER_UP_IN, REBATE, SPOT, COST_OF_CARRY, RATE_DOM, VOLATILITY, derivatives);
    final double priceUISpot = BARRIER_FUNCTION.getPrice(VANILLA_CALL_K100, BARRIER_UP_IN, REBATE, SPOT + shiftSpot, COST_OF_CARRY, RATE_DOM, VOLATILITY);
    assertEquals("Black single barrier: Adjoint spot derivative - Up In", (priceUISpot - priceUI) / shiftSpot, derivatives[0], 2.0E-4);
    final double priceUIRate = BARRIER_FUNCTION.getPrice(VANILLA_CALL_K100, BARRIER_UP_IN, REBATE, SPOT, COST_OF_CARRY, RATE_DOM + shiftRate, VOLATILITY);
    assertEquals("Black single barrier: Adjoint rate derivative - Up In", (priceUIRate - priceUI) / shiftRate, derivatives[2], 1.0E-5);
    final double priceUICoC = BARRIER_FUNCTION.getPrice(VANILLA_CALL_K100, BARRIER_UP_IN, REBATE, SPOT, COST_OF_CARRY + shiftCoC, RATE_DOM, VOLATILITY);
    assertEquals("Black single barrier: Adjoint cost-of-carry derivative - Up In", (priceUICoC - priceUI) / shiftCoC, derivatives[3], 1.0E-4);
    final double priceUIVol = BARRIER_FUNCTION.getPrice(VANILLA_CALL_K100, BARRIER_UP_IN, REBATE, SPOT, COST_OF_CARRY, RATE_DOM, VOLATILITY + shiftVol);
    assertEquals("Black single barrier: Adjoint cost-of-carry derivative - Up In", (priceUIVol - priceUI) / shiftVol, derivatives[4], 1.0E-5);
    // UP-OUT
    final double priceUO = BARRIER_FUNCTION.getPriceAdjoint(VANILLA_CALL_K100, BARRIER_UP_OUT, REBATE, SPOT, COST_OF_CARRY, RATE_DOM, VOLATILITY, derivatives);
    final double priceUOSpot = BARRIER_FUNCTION.getPrice(VANILLA_CALL_K100, BARRIER_UP_OUT, REBATE, SPOT + shiftSpot, COST_OF_CARRY, RATE_DOM, VOLATILITY);
    assertEquals("Black single barrier: Adjoint spot derivative - Up Out", (priceUOSpot - priceUO) / shiftSpot, derivatives[0], 1.0E-4);
    final double priceUORate = BARRIER_FUNCTION.getPrice(VANILLA_CALL_K100, BARRIER_UP_OUT, REBATE, SPOT, COST_OF_CARRY, RATE_DOM + shiftRate, VOLATILITY);
    assertEquals("Black single barrier: Adjoint rate derivative - Up Out", (priceUORate - priceUO) / shiftRate, derivatives[2], 1.0E-5);
    final double priceUOCoC = BARRIER_FUNCTION.getPrice(VANILLA_CALL_K100, BARRIER_UP_OUT, REBATE, SPOT, COST_OF_CARRY + shiftCoC, RATE_DOM, VOLATILITY);
    assertEquals("Black single barrier: Adjoint cost-of-carry derivative - Up Out", (priceUOCoC - priceUO) / shiftCoC, derivatives[3], 1.0E-5);
    final double priceUOVol = BARRIER_FUNCTION.getPrice(VANILLA_CALL_K100, BARRIER_UP_OUT, REBATE, SPOT, COST_OF_CARRY, RATE_DOM, VOLATILITY + shiftVol);
    assertEquals("Black single barrier: Adjoint cost-of-carry derivative - Up Out", (priceUOVol - priceUO) / shiftVol, derivatives[4], 2.0E-5);
  }

  private static final EuropeanVanillaOption[] OPTIONS;
  private static final Barrier[] BARRIERS;
  private static final double[] SPOTS;
  static {
    EuropeanVanillaOption call100 = new EuropeanVanillaOption(STRIKE_MID, EXPIRY_TIME, IS_CALL);
    EuropeanVanillaOption put100 = new EuropeanVanillaOption(STRIKE_MID, EXPIRY_TIME, !IS_CALL);
    OPTIONS = new EuropeanVanillaOption[] {call100, put100 };
    Barrier downAndIn90 = new Barrier(KnockType.IN, BarrierType.DOWN, ObservationType.CONTINUOUS, 90);
    Barrier downAndIn110 = new Barrier(KnockType.IN, BarrierType.DOWN, ObservationType.CONTINUOUS, 110);
    Barrier downAndOut90 = new Barrier(KnockType.OUT, BarrierType.DOWN, ObservationType.CONTINUOUS, 90);
    Barrier downAndOut110 = new Barrier(KnockType.OUT, BarrierType.DOWN, ObservationType.CONTINUOUS, 110);
    Barrier upAndIn90 = new Barrier(KnockType.IN, BarrierType.UP, ObservationType.CONTINUOUS, 90);
    Barrier upAndIn110 = new Barrier(KnockType.IN, BarrierType.UP, ObservationType.CONTINUOUS, 110);
    Barrier upAndOut90 = new Barrier(KnockType.OUT, BarrierType.UP, ObservationType.CONTINUOUS, 90);
    Barrier upAndOut110 = new Barrier(KnockType.OUT, BarrierType.UP, ObservationType.CONTINUOUS, 110);
    BARRIERS = new Barrier[] {
      downAndIn90, downAndIn110, downAndOut90, downAndOut110, upAndIn90, upAndIn110, upAndOut90, upAndOut110 };
    double spot114 = 114.0;
    double spot108 = 108.0;
    double spot100p = 100.01;
    double spot100m = 100.01;
    double spot93 = 93.0;
    double spot88 = 88.0;
    SPOTS = new double[] {spot114, spot108, spot100p, spot100m, spot93, spot88 };
  }

  @Test
  public void test_small_time() {
    double eps = 1.0e-12;
    double time1 = 1.e-17;
    double time2 = 1.e-15;
    EuropeanVanillaOption call100Time1 = new EuropeanVanillaOption(STRIKE_MID, time1, IS_CALL);
    EuropeanVanillaOption call100Time2 = new EuropeanVanillaOption(STRIKE_MID, time2, IS_CALL);
    EuropeanVanillaOption put100Time1 = new EuropeanVanillaOption(STRIKE_MID, EXPIRY_TIME, !IS_CALL);
    EuropeanVanillaOption put100Time2 = new EuropeanVanillaOption(STRIKE_MID, EXPIRY_TIME, !IS_CALL);
    double[] derivatives1 = new double[7];
    double[] derivatives2 = new double[7];
    double[] derivatives3 = new double[7];
    double[] derivatives4 = new double[7];
    for (Barrier barrier : BARRIERS) {
      for (double spot : SPOTS) {
        if (!(barrier.getBarrierType() == BarrierType.DOWN && spot < barrier.getBarrierLevel()) &&
            !(barrier.getBarrierType() == BarrierType.UP && spot > barrier.getBarrierLevel())) {
          //          System.out.println(spot + "\t" + barrier.getBarrierType() + "\t" + barrier.getKnockType() + "\t" +
          //              barrier.getBarrierLevel());
          double call0 = BARRIER_FUNCTION.getPrice(call100Time1, barrier, REBATE, spot, COST_OF_CARRY,
              RATE_DOM, VOLATILITY);
          double call1 = BARRIER_FUNCTION.getPriceAdjoint(call100Time1, barrier, REBATE, spot, COST_OF_CARRY,
              RATE_DOM, VOLATILITY, derivatives1);
          double call2 = BARRIER_FUNCTION.getPriceAdjoint(call100Time2, barrier, REBATE, spot, COST_OF_CARRY,
              RATE_DOM, VOLATILITY, derivatives2);
          assertEquals(call0, call2, eps);
          assertEquals(call1, call2, eps);
          //          System.out.println(call1 + "\t" + call2);
          double put0 = BARRIER_FUNCTION.getPrice(put100Time1, barrier, REBATE, spot, COST_OF_CARRY,
              RATE_DOM, VOLATILITY);
          double put1 = BARRIER_FUNCTION.getPriceAdjoint(put100Time1, barrier, REBATE, spot, COST_OF_CARRY,
              RATE_DOM, VOLATILITY, derivatives3);
          double put2 = BARRIER_FUNCTION.getPriceAdjoint(put100Time2, barrier, REBATE, spot, COST_OF_CARRY,
              RATE_DOM, VOLATILITY, derivatives4);
          assertEquals(put0, put2, eps);
          assertEquals(put1, put2, eps);
          for (int i = 0; i < 7; ++i) {
            //            System.out.println(i);
            assertEquals(derivatives1[i], derivatives2[i], eps);
            assertEquals(derivatives3[i], derivatives4[i], eps);
          }
        }
      }
    }
  }

  @Test
  public void test_small_vol() {
    double eps = 1.0e-12;
    double vol1 = 1.e-17;
    double vol2 = 1.e-15;
    double[] derivatives1 = new double[7];
    double[] derivatives2 = new double[7];
    for (EuropeanVanillaOption option : OPTIONS) {
      for (Barrier barrier : BARRIERS) {
        for (double spot : SPOTS) {
          if (!(barrier.getBarrierType() == BarrierType.DOWN && spot < barrier.getBarrierLevel()) &&
              !(barrier.getBarrierType() == BarrierType.UP && spot > barrier.getBarrierLevel())) {
            double call0 = BARRIER_FUNCTION.getPrice(option, barrier, REBATE, spot, COST_OF_CARRY,
                RATE_DOM, vol1);
            double call1 = BARRIER_FUNCTION.getPriceAdjoint(option, barrier, REBATE, spot, COST_OF_CARRY,
                RATE_DOM, vol1, derivatives1);
            double call2 = BARRIER_FUNCTION.getPriceAdjoint(option, barrier, REBATE, spot, COST_OF_CARRY,
                RATE_DOM, vol2, derivatives2);
            assertEquals(call0, call2, eps);
            assertEquals(call1, call2, eps);
            for (int i = 0; i < 7; ++i) {
              if (Math.abs(derivatives2[i]) < 1d / eps) {
                assertEquals(derivatives1[i], derivatives2[i], eps);
              } else { // handle infinite case
                assertTrue((derivatives1[i] > 1d / eps && derivatives2[i] > 1d / eps)
                    || (derivatives1[i] < -1d / eps && derivatives2[i] < -1d / eps));
              }
            }
          }
        }
      }
    }
  }

  @Test
  public void test_downInParity_all() {
    double eps = 1.0e-12;
    double[] deriv = new double[7];
    Barrier[][] barriers = new Barrier[][] { {BARRIERS[0], BARRIERS[2] }, {BARRIERS[1], BARRIERS[3] },
      {BARRIERS[4], BARRIERS[6] }, {BARRIERS[5], BARRIERS[7] } };
    for (EuropeanVanillaOption option : OPTIONS) {
      for (Barrier[] barrier : barriers) {
        for (double spot : SPOTS) {
          if (barrier[0].getBarrierType() == BarrierType.DOWN && spot < barrier[0].getBarrierLevel() ||
              barrier[0].getBarrierType() == BarrierType.UP && spot > barrier[0].getBarrierLevel()) {
            assertThrowsIllegalArg(() -> BARRIER_FUNCTION.getPrice(
                option, barrier[0], 0d, spot, COST_OF_CARRY, RATE_DOM, VOLATILITY));
            assertThrowsIllegalArg(() -> BARRIER_FUNCTION.getPrice(
                option, barrier[1], 0d, spot, COST_OF_CARRY, RATE_DOM, VOLATILITY));
            assertThrowsIllegalArg(() -> BARRIER_FUNCTION.getPriceAdjoint(
                option, barrier[0], 0d, spot, COST_OF_CARRY, RATE_DOM, VOLATILITY, deriv));
            assertThrowsIllegalArg(() -> BARRIER_FUNCTION.getPriceAdjoint(
                option, barrier[1], 0d, spot, COST_OF_CARRY, RATE_DOM, VOLATILITY, deriv));
          } else {
            double inPlusOut1 =
                BARRIER_FUNCTION.getPrice(option, barrier[0], 0d, spot, COST_OF_CARRY, RATE_DOM, VOLATILITY) +
                    BARRIER_FUNCTION.getPrice(option, barrier[1], 0d, spot, COST_OF_CARRY, RATE_DOM, VOLATILITY);
            double inPlusOut2 = BARRIER_FUNCTION.getPriceAdjoint(
                option, barrier[0], 0d, spot, COST_OF_CARRY, RATE_DOM, VOLATILITY, deriv) +
                BARRIER_FUNCTION.getPriceAdjoint(
                    option, barrier[1], 0d, spot, COST_OF_CARRY, RATE_DOM, VOLATILITY, deriv);
            double forward = spot * Math.exp(COST_OF_CARRY * EXPIRY_TIME);
            double df = Math.exp(-RATE_DOM * EXPIRY_TIME);
            double vanilla = df * BlackFormulaRepository.price(
                forward, STRIKE_MID, EXPIRY_TIME, VOLATILITY, option.isCall());
            assertEquals(inPlusOut1, vanilla, eps);
            assertEquals(inPlusOut2, vanilla, eps);
          }
        }
      }
    }
  }

  @Test
  public void test_getPriceAdjoint_all() {
    double eps = 1.0e-6; // relative shift
    for (EuropeanVanillaOption option : OPTIONS) {
      for (Barrier barrier : BARRIERS) {
        for (double spot : SPOTS) {
          if (barrier.getBarrierType() == BarrierType.DOWN && spot < barrier.getBarrierLevel() ||
              barrier.getBarrierType() == BarrierType.UP && spot > barrier.getBarrierLevel()) {
            assertThrowsIllegalArg(() -> testGreeks(
                option, barrier, spot, REBATE, COST_OF_CARRY, RATE_DOM, VOLATILITY, eps));
          } else {
            testGreeks(option, barrier, spot, REBATE, COST_OF_CARRY, RATE_DOM, VOLATILITY, eps);
          }
        }
      }
    }
  }

  private void testGreeks(EuropeanVanillaOption option, Barrier barrier, double spot, double rebate, double cost,
      double rate, double vol, double eps) {
    double up = 1d + eps;
    double dw = 1d - eps;
    double[] deriv = new double[7];
    double[] derivUp = new double[7];
    double[] derivDw = new double[7];
    BARRIER_FUNCTION.getPriceAdjoint(option, barrier, rebate, spot, cost, rate, vol, deriv);
    // delta, gamma
    double priceUp = BARRIER_FUNCTION.getPriceAdjoint(option, barrier, rebate, spot * up, cost, rate, vol, derivUp);
    double priceDw = BARRIER_FUNCTION.getPriceAdjoint(option, barrier, rebate, spot * dw, cost, rate, vol, derivDw);
    assertEquals(deriv[0], 0.5 * (priceUp - priceDw) / spot / eps, eps);
    assertEquals(deriv[6], 0.5 * (derivUp[0] - derivDw[0]) / spot / eps, eps);
    // dual delta
    double strikeBase = option.getStrike();
    priceUp = BARRIER_FUNCTION.getPrice(option.withStrike(strikeBase * up), barrier, rebate, spot, cost, rate, vol);
    priceDw = BARRIER_FUNCTION.getPrice(option.withStrike(strikeBase * dw), barrier, rebate, spot, cost, rate, vol);
    assertEquals(deriv[1], 0.5 * (priceUp - priceDw) / strikeBase / eps, eps);
    // rho
    priceUp = BARRIER_FUNCTION.getPrice(option, barrier, rebate, spot, cost, rate * up, vol);
    priceDw = BARRIER_FUNCTION.getPrice(option, barrier, rebate, spot, cost, rate * dw, vol);
    assertEquals(deriv[2], 0.5 * (priceUp - priceDw) / rate / eps, eps);
    // carry rho
    priceUp = BARRIER_FUNCTION.getPrice(option, barrier, rebate, spot, cost * up, rate, vol);
    priceDw = BARRIER_FUNCTION.getPrice(option, barrier, rebate, spot, cost * dw, rate, vol);
    assertEquals(deriv[3], 0.5 * (priceUp - priceDw) / cost / eps, eps);
    // vega
    priceUp = BARRIER_FUNCTION.getPrice(option, barrier, rebate, spot, cost, rate, vol * up);
    priceDw = BARRIER_FUNCTION.getPrice(option, barrier, rebate, spot, cost, rate, vol * dw);
    assertEquals(deriv[4], 0.5 * (priceUp - priceDw) / vol / eps, eps);
    // theta
    double timeBase = option.getTimeToExpiry();
    priceUp = BARRIER_FUNCTION.getPrice(option.withTimeToExpiry(timeBase * up), barrier, rebate, spot, cost, rate, vol);
    priceDw = BARRIER_FUNCTION.getPrice(option.withTimeToExpiry(timeBase * dw), barrier, rebate, spot, cost, rate, vol);
    assertEquals(deriv[5], 0.5 * (priceUp - priceDw) / timeBase / eps, eps);
  }
}

/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.provider;

import static org.testng.AssertJUnit.assertEquals;

import java.time.Period;
import java.time.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionCashFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.model.interestrate.TestsDataSetG2pp;
import com.opengamma.analytics.financial.model.interestrate.definition.G2ppPiecewiseConstantParameters;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.G2ppProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.DateUtils;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.currency.MultiCurrencyAmount;
import com.opengamma.strata.basics.date.HolidayCalendar;

/**
 * Tests related to the pricing of physical delivery swaption in G2++ model.
 */
@Test
public class SwaptionCashFixedIborG2ppMethodTest {

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final IborIndex EURIBOR3M = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd()[0];
  private static final Currency EUR = EURIBOR3M.getCurrency();
  private static final HolidayCalendar CALENDAR = MulticurveProviderDiscountDataSets.getEURCalendar();

  private static final G2ppPiecewiseConstantParameters PARAMETERS_G2PP = TestsDataSetG2pp.createG2ppParameters1();
  private static final G2ppProviderDiscount G2PP_MULTICURVES = new G2ppProviderDiscount(MULTICURVES, PARAMETERS_G2PP, EUR);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 7, 7);
  private static final GeneratorSwapFixedIborMaster GENERATOR_SWAP_MASTER = GeneratorSwapFixedIborMaster.getInstance();
  private static final int SPOT_LAG = EURIBOR3M.getSpotLag();
  private static final int SWAP_TENOR_YEAR = 5;
  private static final Period SWAP_TENOR = Period.ofYears(SWAP_TENOR_YEAR);
  private static final GeneratorSwapFixedIbor EUR1YEURIBOR6M = GENERATOR_SWAP_MASTER.getGenerator("EUR1YEURIBOR6M", CALENDAR);
  private static final IndexSwap CMS_INDEX = new IndexSwap(EUR1YEURIBOR6M, SWAP_TENOR);
  private static final ZonedDateTime EXPIRY_DATE = DateUtils.getUTCDate(2016, 7, 7);
  private static final boolean IS_LONG = true;
  private static final ZonedDateTime SETTLEMENT_DATE = ScheduleCalculator.getAdjustedDate(EXPIRY_DATE, SPOT_LAG, CALENDAR);
  private static final double NOTIONAL = 100000000; //100m
  private static final double RATE = 0.0325;
  private static final boolean FIXED_IS_PAYER = true;
  private static final SwapFixedIborDefinition SWAP_PAYER_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE, FIXED_IS_PAYER, CALENDAR);
  private static final SwapFixedIborDefinition SWAP_RECEIVER_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE, !FIXED_IS_PAYER, CALENDAR);

  // Swaption 5Yx5Y
  private static final SwaptionCashFixedIborDefinition SWAPTION_PAYER_LONG_DEFINITION = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP_PAYER_DEFINITION, true, IS_LONG);
  private static final SwaptionCashFixedIborDefinition SWAPTION_RECEIVER_LONG_DEFINITION = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP_RECEIVER_DEFINITION, false, IS_LONG);
  private static final SwaptionCashFixedIborDefinition SWAPTION_PAYER_SHORT_DEFINITION = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP_PAYER_DEFINITION, true, !IS_LONG);
  private static final SwaptionCashFixedIborDefinition SWAPTION_RECEIVER_SHORT_DEFINITION = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP_RECEIVER_DEFINITION, false, !IS_LONG);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_PHYS_PAYER_LONG_DEFINITION = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_PAYER_DEFINITION, FIXED_IS_PAYER, IS_LONG);
  //to derivatives

  private static final SwaptionCashFixedIbor SWAPTION_LONG_PAYER = SWAPTION_PAYER_LONG_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final SwaptionCashFixedIbor SWAPTION_LONG_RECEIVER = SWAPTION_RECEIVER_LONG_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final SwaptionCashFixedIbor SWAPTION_SHORT_PAYER = SWAPTION_PAYER_SHORT_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final SwaptionCashFixedIbor SWAPTION_SHORT_RECEIVER = SWAPTION_RECEIVER_SHORT_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final SwaptionPhysicalFixedIbor SWAPTION_PHYS_PAYER_LONG = SWAPTION_PHYS_PAYER_LONG_DEFINITION.toDerivative(REFERENCE_DATE);

  private static final SwaptionPhysicalFixedIborG2ppApproximationMethod METHOD_G2PP_PHYS_APPROXIMATION = SwaptionPhysicalFixedIborG2ppApproximationMethod.getInstance();
  private static final SwaptionCashFixedIborG2ppNumericalIntegrationMethod METHOD_G2PP_NI = new SwaptionCashFixedIborG2ppNumericalIntegrationMethod();

  private static final double TOLERANCE_PV = 1.0E-2;

  /**
   * Tests the present value vs a physical delivery swaption.
   */
  public void physical() {
    final MultiCurrencyAmount pvPhys = METHOD_G2PP_PHYS_APPROXIMATION.presentValue(SWAPTION_PHYS_PAYER_LONG, G2PP_MULTICURVES);
    final MultiCurrencyAmount pvCash = METHOD_G2PP_NI.presentValue(SWAPTION_LONG_PAYER, G2PP_MULTICURVES);
    assertEquals("Swaption physical - G2++ - present value - hard coded value", pvPhys.getAmount(EUR).getAmount(), pvCash.getAmount(EUR).getAmount(), 2.0E+5);
  }

  /**
   * Test the present value vs a hard-coded value.
   */
  public void presentValue() {
    final MultiCurrencyAmount pv = METHOD_G2PP_NI.presentValue(SWAPTION_LONG_PAYER, G2PP_MULTICURVES);
    final double pvExpected = 1583688.804;
    assertEquals("Swaption physical - G2++ - present value - hard coded value", pvExpected, pv.getAmount(EUR).getAmount(), 1E-2);
  }

  /**
   * Tests long/short parity.
   */
  public void longShortParity() {
    final MultiCurrencyAmount pvPayerLong = METHOD_G2PP_NI.presentValue(SWAPTION_LONG_PAYER, G2PP_MULTICURVES);
    final MultiCurrencyAmount pvPayerShort = METHOD_G2PP_NI.presentValue(SWAPTION_SHORT_PAYER, G2PP_MULTICURVES);
    assertEquals("Swaption physical - G2++ - present value - long/short parity", pvPayerLong.getAmount(EUR).getAmount(), -pvPayerShort.getAmount(EUR).getAmount(), TOLERANCE_PV);
    final MultiCurrencyAmount pvReceiverLong = METHOD_G2PP_NI.presentValue(SWAPTION_LONG_RECEIVER, G2PP_MULTICURVES);
    final MultiCurrencyAmount pvReceiverShort = METHOD_G2PP_NI.presentValue(SWAPTION_SHORT_RECEIVER, G2PP_MULTICURVES);
    assertEquals("Swaption physical - G2++ - present value - long/short parity", pvReceiverLong.getAmount(EUR).getAmount(), -pvReceiverShort.getAmount(EUR).getAmount(), TOLERANCE_PV);
  }

}

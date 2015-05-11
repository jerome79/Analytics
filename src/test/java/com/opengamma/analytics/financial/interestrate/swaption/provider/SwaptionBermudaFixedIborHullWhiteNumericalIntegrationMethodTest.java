/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.provider;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.time.Period;
import java.time.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.convention.daycount.DayCount;
import com.opengamma.analytics.convention.daycount.DayCounts;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionBermudaFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionBermudaFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.analytics.financial.provider.description.HullWhiteDataSets;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.DateUtils;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.currency.MultiCurrencyAmount;
import com.opengamma.strata.basics.date.HolidayCalendar;

/**
 * Test the Bermuda swaption pricing in the Hull-White one factor model.
 */
@Test
public class SwaptionBermudaFixedIborHullWhiteNumericalIntegrationMethodTest {

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final IborIndex EURIBOR3M = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd()[0];
  private static final HolidayCalendar CALENDAR = MulticurveProviderDiscountDataSets.getEURCalendar();
  private static final Currency CUR = EURIBOR3M.getCurrency();

  // General
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 7, 22);
  // Total swap - 5Y semi bond vs quarterly money
  private static final Period FORWARD_TENOR = Period.ofYears(1);
  private static final ZonedDateTime SETTLEMENT_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, FORWARD_TENOR, EURIBOR3M, CALENDAR);
  private static final Period SWAP_TENOR = Period.ofYears(5);
  private static final double NOTIONAL = 123000000;
  private static final boolean FIXED_IS_PAYER = true;
  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(6);
  private static final DayCount FIXED_DAY_COUNT = DayCounts.THIRTY_U_360;
  private static final IndexSwap CMS_INDEX = new IndexSwap(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, EURIBOR3M, SWAP_TENOR, CALENDAR);
  private static final double RATE = 0.0200;
  private static final SwapFixedIborDefinition TOTAL_SWAP_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE, FIXED_IS_PAYER, CALENDAR);
  // Semi-annual expiry
  private static final boolean IS_LONG = true;
  private static final int NB_EXPIRY = TOTAL_SWAP_DEFINITION.getFixedLeg().getNumberOfPayments();
  private static final ZonedDateTime[] EXPIRY_DATE = new ZonedDateTime[NB_EXPIRY];
  private static final SwapFixedIborDefinition[] EXPIRY_SWAP_DEFINITION = new SwapFixedIborDefinition[NB_EXPIRY];
  static {
    for (int loopexp = 0; loopexp < NB_EXPIRY; loopexp++) {
      EXPIRY_DATE[loopexp] = ScheduleCalculator.getAdjustedDate(TOTAL_SWAP_DEFINITION.getFixedLeg().getNthPayment(loopexp).getAccrualStartDate(), -EURIBOR3M.getSpotLag(), CALENDAR);
      EXPIRY_SWAP_DEFINITION[loopexp] = TOTAL_SWAP_DEFINITION.trimStart(EXPIRY_DATE[loopexp]);
    }
  }
  private static final SwaptionBermudaFixedIborDefinition BERMUDA_SWAPTION_DEFINITION = new SwaptionBermudaFixedIborDefinition(EXPIRY_SWAP_DEFINITION, IS_LONG, EXPIRY_DATE);
  // to derivatives
  private static final HullWhiteOneFactorPiecewiseConstantParameters HW_PARAMETERS = HullWhiteDataSets.createHullWhiteParameters();
  private static final HullWhiteOneFactorProviderDiscount HW_MULTICURVES = new HullWhiteOneFactorProviderDiscount(MULTICURVES, HW_PARAMETERS, CUR);

  private static final SwaptionBermudaFixedIborHullWhiteNumericalIntegrationMethod METHOD_BERMUDA = SwaptionBermudaFixedIborHullWhiteNumericalIntegrationMethod.getInstance();
  private static final SwaptionPhysicalFixedIborHullWhiteMethod METHOD_VANILLA = SwaptionPhysicalFixedIborHullWhiteMethod.getInstance();

  private static final SwaptionBermudaFixedIbor BERMUDA_SWAPTION = BERMUDA_SWAPTION_DEFINITION.toDerivative(REFERENCE_DATE);

  private static final double TOLERANCE_PV = 1.0E-2;

  /**
   * Test the present value against European swaptions.
   */
  public void presentValue() {
    final MultiCurrencyAmount pv = METHOD_BERMUDA.presentValue(BERMUDA_SWAPTION, HW_MULTICURVES);
    final double pvPrevious = 4477405.551; // Hard-coded - previous run
    assertEquals("Bermuda swaption vs European", pvPrevious, pv.getAmount(CUR).getAmount(), TOLERANCE_PV);
    // European swaptions
    final SwaptionPhysicalFixedIborDefinition[] swaptionEuropeanDefinition = new SwaptionPhysicalFixedIborDefinition[NB_EXPIRY];
    final SwaptionPhysicalFixedIbor[] swaptionEuropean = new SwaptionPhysicalFixedIbor[NB_EXPIRY];
    final MultiCurrencyAmount[] pvEuropean = new MultiCurrencyAmount[NB_EXPIRY];
    for (int loopexp = 0; loopexp < NB_EXPIRY; loopexp++) {
      swaptionEuropeanDefinition[loopexp] = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE[loopexp], EXPIRY_SWAP_DEFINITION[loopexp], FIXED_IS_PAYER, IS_LONG);
      swaptionEuropean[loopexp] = swaptionEuropeanDefinition[loopexp].toDerivative(REFERENCE_DATE);
      pvEuropean[loopexp] = METHOD_VANILLA.presentValue(swaptionEuropean[loopexp], HW_MULTICURVES);
      assertTrue("Bermuda swaption vs European", pv.getAmount(CUR).getAmount() >= pvEuropean[loopexp].getAmount(CUR).getAmount());
    }
  }

  //TODO: test present value with external values

  /**
   * Test the present value long/short parity.
   */
  public void longShortParity() {
    final MultiCurrencyAmount pvLong = METHOD_BERMUDA.presentValue(BERMUDA_SWAPTION, HW_MULTICURVES);
    final SwaptionBermudaFixedIborDefinition bermudaShortDefinition = new SwaptionBermudaFixedIborDefinition(EXPIRY_SWAP_DEFINITION, !IS_LONG, EXPIRY_DATE);
    final SwaptionBermudaFixedIbor bermudShort = bermudaShortDefinition.toDerivative(REFERENCE_DATE);
    final MultiCurrencyAmount pvShort = METHOD_BERMUDA.presentValue(bermudShort, HW_MULTICURVES);
    assertEquals("Bermuda swaption pv: short/long parity", pvLong.getAmount(CUR).getAmount(), -pvShort.getAmount(CUR).getAmount(), TOLERANCE_PV);
  }

}

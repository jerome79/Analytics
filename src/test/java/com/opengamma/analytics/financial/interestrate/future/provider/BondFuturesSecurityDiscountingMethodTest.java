/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import static org.testng.AssertJUnit.assertEquals;

import java.time.Period;
import java.time.ZonedDateTime;

import org.apache.commons.math3.stat.descriptive.rank.Min;
import org.testng.annotations.Test;

import com.opengamma.analytics.convention.daycount.DayCount;
import com.opengamma.analytics.convention.daycount.DayCounts;
import com.opengamma.analytics.convention.yield.YieldConvention;
import com.opengamma.analytics.convention.yield.YieldConventionFactory;
import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondSecurityDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesSecurity;
import com.opengamma.analytics.financial.provider.description.IssuerProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.analytics.util.time.DateUtils;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.date.BusinessDayConvention;
import com.opengamma.strata.basics.date.BusinessDayConventions;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.basics.date.HolidayCalendars;

/**
 * Tests related to the bond future figures computed by discounting.
 */
@Test
public class BondFuturesSecurityDiscountingMethodTest {

  private final static IssuerProviderDiscount ISSUER_MULTICURVES = IssuerProviderDiscountDataSets.getIssuerSpecificProvider();
  private final static String[] ISSUER_NAMES = IssuerProviderDiscountDataSets.getIssuerNames();

  // 5-Year U.S. Treasury Note Futures: FVU1
  private static final Currency USD = Currency.USD;
  private static final Period PAYMENT_TENOR = Period.ofMonths(6);
  private static final HolidayCalendar CALENDAR = HolidayCalendars.SAT_SUN;
  private static final String US_GOVT = ISSUER_NAMES[0];
  private static final DayCount DAY_COUNT = DayCounts.ACT_ACT_ICMA;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.FOLLOWING;
  private static final boolean IS_EOM = false;
  private static final int SETTLEMENT_DAYS = 1;
  private static final YieldConvention YIELD_CONVENTION = YieldConventionFactory.INSTANCE.getYieldConvention("STREET CONVENTION");
  private static final int NB_BOND = 7;
  private static final Period[] BOND_TENOR = new Period[] {Period.ofYears(5), Period.ofYears(5), Period.ofYears(5), Period.ofYears(8), Period.ofYears(5), Period.ofYears(5), Period.ofYears(5) };
  private static final ZonedDateTime[] START_ACCRUAL_DATE = new ZonedDateTime[] {DateUtils.getUTCDate(2010, 11, 30), DateUtils.getUTCDate(2010, 12, 31), DateUtils.getUTCDate(2011, 1, 31),
    DateUtils.getUTCDate(2008, 2, 29), DateUtils.getUTCDate(2011, 3, 31), DateUtils.getUTCDate(2011, 4, 30), DateUtils.getUTCDate(2011, 5, 31) };
  private static final double[] RATE = new double[] {0.01375, 0.02125, 0.0200, 0.02125, 0.0225, 0.0200, 0.0175 };
  private static final double[] CONVERSION_FACTOR = new double[] {.8317, .8565, .8493, .8516, .8540, .8417, .8292 };
  private static final ZonedDateTime[] MATURITY_DATE = new ZonedDateTime[NB_BOND];
  private static final BondFixedSecurityDefinition[] BASKET_DEFINITION = new BondFixedSecurityDefinition[NB_BOND];
  static {
    for (int loopbasket = 0; loopbasket < NB_BOND; loopbasket++) {
      MATURITY_DATE[loopbasket] = START_ACCRUAL_DATE[loopbasket].plus(BOND_TENOR[loopbasket]);
      BASKET_DEFINITION[loopbasket] = BondFixedSecurityDefinition.from(USD, MATURITY_DATE[loopbasket], START_ACCRUAL_DATE[loopbasket], PAYMENT_TENOR, RATE[loopbasket], SETTLEMENT_DAYS, CALENDAR,
          DAY_COUNT, BUSINESS_DAY, YIELD_CONVENTION, IS_EOM, US_GOVT);
    }
  }
  private static final ZonedDateTime LAST_TRADING_DATE = DateUtils.getUTCDate(2011, 9, 30);
  private static final ZonedDateTime FIRST_NOTICE_DATE = DateUtils.getUTCDate(2011, 8, 31);
  private static final ZonedDateTime LAST_NOTICE_DATE = DateUtils.getUTCDate(2011, 10, 4);
  private static final ZonedDateTime FIRST_DELIVERY_DATE = ScheduleCalculator.getAdjustedDate(FIRST_NOTICE_DATE, SETTLEMENT_DAYS, CALENDAR);
  private static final ZonedDateTime LAST_DELIVERY_DATE = ScheduleCalculator.getAdjustedDate(LAST_NOTICE_DATE, SETTLEMENT_DAYS, CALENDAR);
  private static final double NOTIONAL = 100000;
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 6, 20);
  private static final double LAST_TRADING_TIME = TimeCalculator.getTimeBetween(REFERENCE_DATE, LAST_TRADING_DATE);
  private static final double FIRST_NOTICE_TIME = TimeCalculator.getTimeBetween(REFERENCE_DATE, FIRST_NOTICE_DATE);
  private static final double LAST_NOTICE_TIME = TimeCalculator.getTimeBetween(REFERENCE_DATE, LAST_NOTICE_DATE);
  private static final double FIRST_DELIVERY_TIME = TimeCalculator.getTimeBetween(REFERENCE_DATE, FIRST_DELIVERY_DATE);
  private static final double LAST_DELIVERY_TIME = TimeCalculator.getTimeBetween(REFERENCE_DATE, LAST_DELIVERY_DATE);
  private static final BondFixedSecurity[] BASKET = new BondFixedSecurity[NB_BOND];
  private static final BondFixedSecurity[] STANDARD = new BondFixedSecurity[NB_BOND];
  static {
    for (int loopbasket = 0; loopbasket < NB_BOND; loopbasket++) {
      BASKET[loopbasket] = BASKET_DEFINITION[loopbasket].toDerivative(REFERENCE_DATE, LAST_DELIVERY_DATE);
      STANDARD[loopbasket] = BASKET_DEFINITION[loopbasket].toDerivative(REFERENCE_DATE);
    }
  }
  private static final BondFuturesSecurity BOND_FUTURE = new BondFuturesSecurity(LAST_TRADING_TIME, FIRST_NOTICE_TIME, LAST_NOTICE_TIME, FIRST_DELIVERY_TIME, LAST_DELIVERY_TIME,
      NOTIONAL, BASKET, STANDARD, CONVERSION_FACTOR);
  private static final BondFuturesSecurityDiscountingMethod METHOD_FUT_SEC_DSC = BondFuturesSecurityDiscountingMethod.getInstance();
  private static final BondSecurityDiscountingMethod METHOD_BOND_SEC = BondSecurityDiscountingMethod.getInstance();

  private static final Min MIN_FUNCTION = new Min();
  private static final double TOLERANCE_PRICE = 1.0E-8;
  private static final double TOLERANCE_PRICE_DELTA = 1.0E-6;

  @Test
  public void price() {
    final double priceComputed = METHOD_FUT_SEC_DSC.price(BOND_FUTURE, ISSUER_MULTICURVES);
    final double[] bondForwardPrice = new double[NB_BOND];
    final double[] bondForwardPriceAdjusted = new double[NB_BOND];
    double priceExpected = 2.0;
    for (int loopbasket = 0; loopbasket < NB_BOND; loopbasket++) {
      bondForwardPrice[loopbasket] = METHOD_BOND_SEC.cleanPriceFromCurves(BASKET[loopbasket], ISSUER_MULTICURVES);
      bondForwardPriceAdjusted[loopbasket] = bondForwardPrice[loopbasket] / CONVERSION_FACTOR[loopbasket];
      priceExpected = Math.min(priceExpected, bondForwardPriceAdjusted[loopbasket]);
    }
    assertEquals("Bond future security Discounting Method: price from curves", priceExpected, priceComputed, TOLERANCE_PRICE);
  }

  @Test
  /**
   * Tests the computation of the price curve sensitivity.
   */
  public void priceCurveSensitivity() {
    MulticurveSensitivity sensiFuture = METHOD_FUT_SEC_DSC.priceCurveSensitivity(BOND_FUTURE, ISSUER_MULTICURVES);
    final double[] bondForwardPrice = new double[NB_BOND];
    final double[] bondFuturePrice = new double[NB_BOND];
    double minPrice = 100.0;
    int minIndex = 0;
    for (int loopbasket = 0; loopbasket < NB_BOND; loopbasket++) {
      bondForwardPrice[loopbasket] = METHOD_BOND_SEC.dirtyPriceFromCurves(BASKET[loopbasket], ISSUER_MULTICURVES);
      bondFuturePrice[loopbasket] = (bondForwardPrice[loopbasket] - BASKET[loopbasket].getAccruedInterest()) / CONVERSION_FACTOR[loopbasket];
      if (bondFuturePrice[loopbasket] < minPrice) {
        minPrice = (bondForwardPrice[loopbasket] - BASKET[loopbasket].getAccruedInterest()) / CONVERSION_FACTOR[loopbasket];
        minIndex = loopbasket;
      }
    }
    MulticurveSensitivity sensiBond = METHOD_BOND_SEC.dirtyPriceCurveSensitivity(BASKET[minIndex], ISSUER_MULTICURVES);
    sensiBond = sensiBond.multipliedBy(1.0 / CONVERSION_FACTOR[minIndex]);
    sensiFuture = sensiFuture.cleaned();
    sensiBond = sensiBond.cleaned();
    AssertSensitivityObjects.assertEquals("BondFutureSecurityDiscountingMethod: priceCurveSensitivity", sensiBond, sensiFuture, TOLERANCE_PRICE_DELTA);
  }

  @Test
  /**
   * Tests the net basis of all bonds computed from the curves.
   */
  public void netBasisAllBonds() {
    final double priceFuture = METHOD_FUT_SEC_DSC.price(BOND_FUTURE, ISSUER_MULTICURVES);
    final double[] netBasisComputed = METHOD_FUT_SEC_DSC.netBasisAllBonds(BOND_FUTURE, ISSUER_MULTICURVES, priceFuture);
    final double[] netBasisExpected = new double[NB_BOND];
    for (int loopbasket = 0; loopbasket < NB_BOND; loopbasket++) {
      final double bondDirtyPriceForward = METHOD_BOND_SEC.dirtyPriceFromCurves(BOND_FUTURE.getDeliveryBasketAtDeliveryDate()[loopbasket], ISSUER_MULTICURVES);
      netBasisExpected[loopbasket] = bondDirtyPriceForward - (priceFuture * CONVERSION_FACTOR[loopbasket] + BOND_FUTURE.getDeliveryBasketAtDeliveryDate()[loopbasket].getAccruedInterest());
      assertEquals("Bond future security Discounting Method: netBasis", netBasisExpected[loopbasket], netBasisComputed[loopbasket], 1.0E-10);
    }
    final Min minFunction = new Min();
    final double netBasisMin = minFunction.evaluate(netBasisComputed);
    final double priceFutureFromNetBasis = METHOD_FUT_SEC_DSC.priceFromNetBasis(BOND_FUTURE, ISSUER_MULTICURVES, netBasisMin);
    assertEquals("Bond future security Discounting Method: netBasis", priceFuture, priceFutureFromNetBasis, 1.0E-10);
  }

  @Test
  /**
   * Tests the net basis of the cheapest to deliver computed from the curves.
   */
  public void netBasisCheapest() {
    final double netBasisInput = 0.0001;
    final double priceFuture = METHOD_FUT_SEC_DSC.price(BOND_FUTURE, ISSUER_MULTICURVES) + netBasisInput;
    final double netBasisCheapest = METHOD_FUT_SEC_DSC.netBasisCheapest(BOND_FUTURE, ISSUER_MULTICURVES, priceFuture);
    final double[] netBasisAll = METHOD_FUT_SEC_DSC.netBasisAllBonds(BOND_FUTURE, ISSUER_MULTICURVES, priceFuture);
    assertEquals("Bond future security Discounting Method: netBasis", MIN_FUNCTION.evaluate(netBasisAll), netBasisCheapest, TOLERANCE_PRICE);
  }

  @Test
  /**
   * Tests the cheapest to deliver figures: yield, modified duration and gross basis.
   */
  public void cheapestToDeliver() {
    final double yieldTest = 0.01345;
    final double priceTest = 1.03414063;
    final double mdTest = 4.271;
    final double grossBasisTest = 20.718; // Quoted in 32ds of %
    final double futurePriceTest = 1.19984375;
    final double[] priceCTD = new double[NB_BOND];
    final double[] mdCTD = new double[NB_BOND];
    final double[] yieldCTD = new double[NB_BOND];
    for (int loopbasket = 0; loopbasket < NB_BOND; loopbasket++) {
      priceCTD[loopbasket] = priceTest;
      mdCTD[loopbasket] = METHOD_BOND_SEC.modifiedDurationFromYield(STANDARD[loopbasket], yieldTest);
      yieldCTD[loopbasket] = METHOD_BOND_SEC.yieldFromCleanPrice(STANDARD[loopbasket], priceTest);
    }
    final double[] grossBasis = METHOD_FUT_SEC_DSC.grossBasisFromPrices(BOND_FUTURE, priceCTD, futurePriceTest);
    final int ctdIndex = 1;
    assertEquals("Bond future security: CTD - yield from price", yieldTest, yieldCTD[ctdIndex], 1.0E-4);
    assertEquals("Bond future security: CTD - modified duration from yield", mdTest, mdCTD[ctdIndex], 1.0E-3);
    assertEquals("Bond future security: CTD - gross basis from price", grossBasisTest / 100.0 / 32.0, grossBasis[ctdIndex], 1.0E-7);
  }

  @Test
  /**
   * Tests the gross basis computed from clean prices
   */
  public void grossBasis() {
    final double futurePriceTest = 1.19984375;
    final double[] pricesTest = new double[] {0.86, 0.885, 0.88, 0.8825, 0.885, 0.8725, 0.86 };
    final double[] pricesCurvesAtSpot = new double[NB_BOND];
    for (int loopbasket = 0; loopbasket < NB_BOND; loopbasket++) {
      pricesCurvesAtSpot[loopbasket] = METHOD_BOND_SEC.cleanPriceFromCurves(BOND_FUTURE.getDeliveryBasketAtSpotDate()[loopbasket], ISSUER_MULTICURVES);
    }
    final double[] basisComputedTest = METHOD_FUT_SEC_DSC.grossBasisFromPrices(BOND_FUTURE, pricesTest, futurePriceTest);
    final double[] basisComputedCurves = METHOD_FUT_SEC_DSC.grossBasisFromPrices(BOND_FUTURE, pricesCurvesAtSpot, futurePriceTest);
    final double[] basisExpectedTest = new double[NB_BOND];
    final double[] basisExpectedCurves = new double[NB_BOND];
    for (int loopbasket = 0; loopbasket < NB_BOND; loopbasket++) {
      basisExpectedTest[loopbasket] = (pricesTest[loopbasket] - futurePriceTest * CONVERSION_FACTOR[loopbasket]);
      basisExpectedCurves[loopbasket] = (pricesCurvesAtSpot[loopbasket] - futurePriceTest * CONVERSION_FACTOR[loopbasket]);
      assertEquals("Gross basis from prices", basisExpectedTest[loopbasket], basisComputedTest[loopbasket], TOLERANCE_PRICE);
      assertEquals("Gross basis from curves", basisExpectedCurves[loopbasket], basisComputedCurves[loopbasket], TOLERANCE_PRICE);
    }
  }

}

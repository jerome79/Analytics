/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import static org.testng.AssertJUnit.assertEquals;

import java.time.Period;
import java.time.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.convention.daycount.DayCount;
import com.opengamma.analytics.convention.daycount.DayCounts;
import com.opengamma.analytics.convention.yield.YieldConvention;
import com.opengamma.analytics.convention.yield.YieldConventionFactory;
import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesTransaction;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.analytics.financial.provider.calculator.hullwhite.PresentValueCurveSensitivityHullWhiteIssuerCalculator;
import com.opengamma.analytics.financial.provider.calculator.hullwhite.PresentValueHullWhiteIssuerCalculator;
import com.opengamma.analytics.financial.provider.description.HullWhiteDataSets;
import com.opengamma.analytics.financial.provider.description.IssuerProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteIssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteIssuerProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.sensitivity.hullwhite.ParameterSensitivityHullWhiteIssuerDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.issuer.ParameterSensitivityIssuerCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.analytics.util.time.DateUtils;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.currency.MultiCurrencyAmount;
import com.opengamma.strata.basics.date.BusinessDayConvention;
import com.opengamma.strata.basics.date.BusinessDayConventions;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.basics.date.HolidayCalendars;

/**
 * Tests related to the bond future figures computed by discounting.
 */
@Test
public class BondFuturesTransactionHullWhiteMethodTest {

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
  private static final BondFixedSecurity[] BASKET_AT_DELIVERY = new BondFixedSecurity[NB_BOND];
  private static final BondFixedSecurity[] BASKET_AT_SPOT = new BondFixedSecurity[NB_BOND];
  static {
    for (int loopbasket = 0; loopbasket < NB_BOND; loopbasket++) {
      BASKET_AT_DELIVERY[loopbasket] = BASKET_DEFINITION[loopbasket].toDerivative(REFERENCE_DATE, LAST_DELIVERY_DATE);
      BASKET_AT_SPOT[loopbasket] = BASKET_DEFINITION[loopbasket].toDerivative(REFERENCE_DATE);
    }
  }
  private static final BondFuturesSecurity BOND_FUTURES_SEC = new BondFuturesSecurity(LAST_TRADING_TIME, FIRST_NOTICE_TIME, LAST_NOTICE_TIME, FIRST_DELIVERY_TIME, LAST_DELIVERY_TIME, NOTIONAL,
      BASKET_AT_DELIVERY, BASKET_AT_SPOT, CONVERSION_FACTOR);
  private static final int QUANTITY = 1234;
  private static final double PRICE_REFERENCE = 1.2345;
  private static final BondFuturesTransaction BOND_FUTURES_TRA = new BondFuturesTransaction(BOND_FUTURES_SEC, QUANTITY, PRICE_REFERENCE);

  private static final HullWhiteOneFactorPiecewiseConstantParameters PARAMETERS_HW = HullWhiteDataSets.createHullWhiteParameters();
  private static final HullWhiteIssuerProviderDiscount MULTICURVES_HW_ISSUER = new HullWhiteIssuerProviderDiscount(ISSUER_MULTICURVES, PARAMETERS_HW);

  private static final BondFuturesTransactionHullWhiteMethod METHOD_FUT_TRA_HW = BondFuturesTransactionHullWhiteMethod.getInstance();
  private static final BondFuturesSecurityHullWhiteMethod METHOD_FUT_SEC_HW = BondFuturesSecurityHullWhiteMethod.getInstance();
  private static final PresentValueHullWhiteIssuerCalculator PVHWIC = PresentValueHullWhiteIssuerCalculator.getInstance();
  private static final PresentValueCurveSensitivityHullWhiteIssuerCalculator PVCSHWIC = PresentValueCurveSensitivityHullWhiteIssuerCalculator.getInstance();

  private static final ParameterSensitivityIssuerCalculator<HullWhiteIssuerProviderInterface> PSC = new ParameterSensitivityIssuerCalculator<HullWhiteIssuerProviderInterface>(PVCSHWIC);
  private static final double SHIFT = 1.0E-7;
  private static final ParameterSensitivityHullWhiteIssuerDiscountInterpolatedFDCalculator PSC_DSC_FD =
      new ParameterSensitivityHullWhiteIssuerDiscountInterpolatedFDCalculator(PVHWIC, SHIFT);

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_DELTA = 1.0E+2;

  @Test
  /**
   * Tests the present value method for bond futures.
   */
  public void presentValueFromPrice() {
    final double quotedPrice = 1.05;
    final MultiCurrencyAmount pvComputed = METHOD_FUT_TRA_HW.presentValueFromPrice(BOND_FUTURES_TRA, quotedPrice);
    final double pvExpected = (quotedPrice - PRICE_REFERENCE) * NOTIONAL * QUANTITY;
    assertEquals("Bond future Method: present value from price", pvExpected, pvComputed.getAmount(USD).getAmount(), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the present value method for bond futures.
   */
  public void presentValue() {
    final MultiCurrencyAmount pvComputed = METHOD_FUT_TRA_HW.presentValue(BOND_FUTURES_TRA, MULTICURVES_HW_ISSUER);
    final double priceFuture = METHOD_FUT_SEC_HW.price(BOND_FUTURES_SEC, MULTICURVES_HW_ISSUER);
    final double pvExpected = (priceFuture - PRICE_REFERENCE) * NOTIONAL * QUANTITY;
    assertEquals("Bond future Discounting Method: present value amount", pvExpected, pvComputed.getAmount(USD).getAmount(), TOLERANCE_PV);
    final MultiCurrencyAmount presentValueCalculator = BOND_FUTURES_TRA.accept(PVHWIC, MULTICURVES_HW_ISSUER);
    assertEquals("Bond future Discounting Method: present value from price", pvComputed.getAmount(USD).getAmount(), presentValueCalculator.getAmount(USD).getAmount(), TOLERANCE_PV);
  }

  @Test
  public void presentValueCurveSensitivity() {
    final MultipleCurrencyParameterSensitivity pvpsDepositExact = PSC.calculateSensitivity(BOND_FUTURES_TRA, MULTICURVES_HW_ISSUER, ISSUER_MULTICURVES.getAllNames());
    final MultipleCurrencyParameterSensitivity pvpsDepositFD = PSC_DSC_FD.calculateSensitivity(BOND_FUTURES_TRA, MULTICURVES_HW_ISSUER);
    AssertSensitivityObjects.assertEquals("CashDiscountingProviderMethod: presentValueCurveSensitivity ", pvpsDepositExact, pvpsDepositFD, TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Tests the present value curve sensitivity method for bond futures.
   */
  public void presentValueCurveSensitivityVsPrice() {
    final MultipleCurrencyMulticurveSensitivity pvcsComputed = METHOD_FUT_TRA_HW.presentValueCurveSensitivity(BOND_FUTURES_TRA, MULTICURVES_HW_ISSUER).cleaned();
    final MulticurveSensitivity pcs = METHOD_FUT_SEC_HW.priceCurveSensitivity(BOND_FUTURES_SEC, MULTICURVES_HW_ISSUER);
    final MultipleCurrencyMulticurveSensitivity pvcsExpected = MultipleCurrencyMulticurveSensitivity.of(USD, pcs.multipliedBy(NOTIONAL * QUANTITY).cleaned());
    AssertSensitivityObjects.assertEquals("Bond future Discounting Method: pv curve sensitivity", pvcsComputed, pvcsExpected, TOLERANCE_PV_DELTA);
  }

  @Test
  public void presentValueCurveSensitivityMethodVsCalculator() {
    final MultipleCurrencyMulticurveSensitivity pvcsMethod = METHOD_FUT_TRA_HW.presentValueCurveSensitivity(BOND_FUTURES_TRA, MULTICURVES_HW_ISSUER);
    final MultipleCurrencyMulticurveSensitivity pvcsCalculator = BOND_FUTURES_TRA.accept(PVCSHWIC, MULTICURVES_HW_ISSUER);
    AssertSensitivityObjects.assertEquals("CouponFixedDiscountingMarketMethod: presentValueMarketSensitivity", pvcsMethod, pvcsCalculator, TOLERANCE_PV_DELTA);
  }

}

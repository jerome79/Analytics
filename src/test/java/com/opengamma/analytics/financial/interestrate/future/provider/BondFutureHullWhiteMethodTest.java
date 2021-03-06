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
import com.opengamma.analytics.convention.daycount.DayCountUtils;
import com.opengamma.analytics.convention.daycount.DayCounts;
import com.opengamma.analytics.convention.yield.YieldConvention;
import com.opengamma.analytics.convention.yield.YieldConventionFactory;
import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuture;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.analytics.financial.provider.description.HullWhiteDataSets;
import com.opengamma.analytics.financial.provider.description.IssuerProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteIssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.analytics.util.time.DateUtils;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.currency.MultiCurrencyAmount;
import com.opengamma.strata.basics.date.BusinessDayConvention;
import com.opengamma.strata.basics.date.BusinessDayConventions;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.basics.date.HolidayCalendars;

/**
 * Tests related to the bond future figures computed with the Hull-White one factor model for the delivery option.
 */
@SuppressWarnings("deprecation")
@Test
public class BondFutureHullWhiteMethodTest {

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
  private static final double REF_PRICE = 0.0;
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 6, 20);
  private static final DayCount ACT_ACT = DayCounts.ACT_ACT_ISDA;
  private static final double LAST_TRADING_TIME = DayCountUtils.yearFraction(ACT_ACT, REFERENCE_DATE, LAST_TRADING_DATE);
  private static final double FIRST_NOTICE_TIME = DayCountUtils.yearFraction(ACT_ACT, REFERENCE_DATE, FIRST_NOTICE_DATE);
  private static final double LAST_NOTICE_TIME = DayCountUtils.yearFraction(ACT_ACT, REFERENCE_DATE, LAST_NOTICE_DATE);
  private static final double FIRST_DELIVERY_TIME = DayCountUtils.yearFraction(ACT_ACT, REFERENCE_DATE, FIRST_DELIVERY_DATE);
  private static final double LAST_DELIVERY_TIME = DayCountUtils.yearFraction(ACT_ACT, REFERENCE_DATE, LAST_DELIVERY_DATE);
  private static final BondFixedSecurity[] BASKET = new BondFixedSecurity[NB_BOND];
  private static final BondFixedSecurity[] STANDARD = new BondFixedSecurity[NB_BOND];
  static {
    for (int loopbasket = 0; loopbasket < NB_BOND; loopbasket++) {
      BASKET[loopbasket] = BASKET_DEFINITION[loopbasket].toDerivative(REFERENCE_DATE, LAST_DELIVERY_DATE);
      STANDARD[loopbasket] = BASKET_DEFINITION[loopbasket].toDerivative(REFERENCE_DATE);
    }
  }

  private static final BondFuture BOND_FUTURE_DERIV = new BondFuture(LAST_TRADING_TIME, FIRST_NOTICE_TIME, LAST_NOTICE_TIME, FIRST_DELIVERY_TIME, LAST_DELIVERY_TIME, NOTIONAL, BASKET,
      CONVERSION_FACTOR, REF_PRICE);
  private static final HullWhiteOneFactorPiecewiseConstantParameters PARAMETERS_HW = HullWhiteDataSets.createHullWhiteParameters();
  private static final HullWhiteIssuerProviderDiscount HW_ISSUER = new HullWhiteIssuerProviderDiscount(ISSUER_MULTICURVES, PARAMETERS_HW);
  private static final BondFutureHullWhiteMethod METHOD_HW = BondFutureHullWhiteMethod.getInstance();

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_DELTA = 1.0E-0;

  public void price() {
    final HullWhiteIssuerProviderDiscount hwIssuer6 = new HullWhiteIssuerProviderDiscount(IssuerProviderDiscountDataSets.createIssuerProvider6(), PARAMETERS_HW);
    final double priceComputed = METHOD_HW.price(BOND_FUTURE_DERIV, hwIssuer6);
    final double priceExpected = 1.00; // Rates are at 6%
    assertEquals("Bond future security Discounting Method: price from curves", priceExpected, priceComputed, 5.0E-3);
  }

  /**
   * Tests the present value method for bond futures transactions. 
   */
  public void presentValue() {
    final MultiCurrencyAmount pvComputed = METHOD_HW.presentValue(BOND_FUTURE_DERIV, HW_ISSUER);
    final double priceFuture = METHOD_HW.price(BOND_FUTURE_DERIV, HW_ISSUER);
    final double pvExpected = (priceFuture - REF_PRICE) * NOTIONAL;
    assertEquals("Bond future HW Method: present value amount", pvExpected, pvComputed.getAmount(USD).getAmount(), TOLERANCE_PV);
  }

  // TODO: Fix now PresentValueFromFuturePriceCalculator removed
//  /**
//   * Tests the present value method for bond futures transactions.
//   */
//  public void presentValueFromPrice() {
//    final double quotedPrice = 1.05;
//    final MultiCurrencyAmount presentValueMethod = METHOD_HW.presentValueFromPrice(BOND_FUTURE_DERIV, quotedPrice);
//    assertEquals("Bond future transaction Method: present value from price", (quotedPrice - REF_PRICE) * NOTIONAL, presentValueMethod.getAmount(USD).getAmount());
//    final PresentValueFromFuturePriceCalculator calculator = PresentValueFromFuturePriceCalculator.getInstance();
//    final double presentValueCalculator = BOND_FUTURE_DERIV.accept(calculator, quotedPrice);
//    assertEquals("Bond future transaction Method: present value from price", presentValueMethod.getAmount(USD).getAmount(), presentValueCalculator);
//  }

  /**
   * Tests the present value curve sensitivity method for bond futures.
   */
  public void presentValueCurveSensitivityRelative() {
    final MultipleCurrencyMulticurveSensitivity pvcsComputed = METHOD_HW.presentValueCurveSensitivity(BOND_FUTURE_DERIV, HW_ISSUER);
    final MulticurveSensitivity pcsSecurity = METHOD_HW.priceCurveSensitivity(BOND_FUTURE_DERIV, HW_ISSUER);
    final MultipleCurrencyMulticurveSensitivity pvcsExpected = MultipleCurrencyMulticurveSensitivity.of(USD, pcsSecurity.multipliedBy(NOTIONAL));
    AssertSensitivityObjects.assertEquals("Bond future transaction Discounting Method: present value curve sensitivity", pvcsExpected.cleaned(), pvcsComputed.cleaned(), TOLERANCE_PV_DELTA);
  }

}

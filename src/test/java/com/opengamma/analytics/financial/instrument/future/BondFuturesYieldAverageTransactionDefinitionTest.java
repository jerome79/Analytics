/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.future;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import java.time.Period;
import java.time.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.convention.daycount.DayCount;
import com.opengamma.analytics.convention.daycount.DayCounts;
import com.opengamma.analytics.convention.yield.SimpleYieldConvention;
import com.opengamma.analytics.convention.yield.YieldConvention;
import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.provider.description.IssuerProviderDiscountDataSets;
import com.opengamma.analytics.util.time.DateUtils;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.date.BusinessDayConvention;
import com.opengamma.strata.basics.date.BusinessDayConventions;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.basics.date.HolidayCalendars;

/**
 * Tests related to the definition of Yield average bond futures (in particular for AUD-SFE futures).
 */
@Test
public class BondFuturesYieldAverageTransactionDefinitionTest {

  // Bonds: Delivery basket SFE 10Y
  private static final Currency AUD = Currency.AUD;
  // AUD defaults
  private static final LegalEntity ISSUER_LEGAL_ENTITY = IssuerProviderDiscountDataSets.getIssuersAUS();
  private static final Period PAYMENT_TENOR = Period.ofMonths(6);
  private static final HolidayCalendar CALENDAR = HolidayCalendars.SAT_SUN;
  private static final DayCount DAY_COUNT = DayCounts.ACT_ACT_ICMA;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.FOLLOWING;
  private static final boolean IS_EOM = false;
  private static final int SETTLEMENT_DAYS = 3;
  private static final int EX_DIVIDEND_DAYS = 7;
  private static final YieldConvention YIELD_CONVENTION = SimpleYieldConvention.AUSTRALIA_EX_DIVIDEND;
  private static final double NOTIONAL_BOND = 100;
  private static final double NOTIONAL_FUTURES = 10000;

  private static final ZonedDateTime LAST_TRADING_DATE = DateUtils.getUTCDate(2014, 3, 17);
  // ASX 10 Year Bond Contract - March 14
  private static final double[] UNDERLYING_COUPON = {0.0575, 0.0550, 0.0275, 0.0325 };
  private static final ZonedDateTime[] UNDERLYING_MATURITY_DATE = new ZonedDateTime[] {DateUtils.getUTCDate(2022, 7, 15), DateUtils.getUTCDate(2023, 4, 15),
    DateUtils.getUTCDate(2024, 4, 15), DateUtils.getUTCDate(2025, 4, 15) };
  private static final int NB_BOND = UNDERLYING_COUPON.length;
  private static final ZonedDateTime[] START_ACCRUAL_DATE = new ZonedDateTime[NB_BOND];
  private static final BondFixedSecurityDefinition[] BASKET_SECURITY_DEFINITION = new BondFixedSecurityDefinition[NB_BOND];
  static {
    for (int loopbond = 0; loopbond < NB_BOND; loopbond++) {
      START_ACCRUAL_DATE[loopbond] = UNDERLYING_MATURITY_DATE[loopbond].minusYears(12);
      BASKET_SECURITY_DEFINITION[loopbond] = BondFixedSecurityDefinition.from(AUD, START_ACCRUAL_DATE[loopbond], UNDERLYING_MATURITY_DATE[loopbond], PAYMENT_TENOR,
          UNDERLYING_COUPON[loopbond], SETTLEMENT_DAYS, NOTIONAL_BOND, EX_DIVIDEND_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, YIELD_CONVENTION, IS_EOM, ISSUER_LEGAL_ENTITY, "Repo");
    }
  }
  private static final double SYNTHETIC_COUPON = 0.06;
  private static final int TENOR = 10;
  private static final BondFuturesYieldAverageSecurityDefinition FUT_SEC_DEFINITION = new BondFuturesYieldAverageSecurityDefinition(LAST_TRADING_DATE,
      BASKET_SECURITY_DEFINITION, SYNTHETIC_COUPON, TENOR, NOTIONAL_FUTURES);
  // Transation
  private static final int QUANTITY = 1234;
  private static final double TRADE_PRICE = 0.95;
  private static final ZonedDateTime TRADE_DATE = DateUtils.getUTCDate(2014, 1, 10);
  private static final BondFuturesYieldAverageTransactionDefinition FUT_TRA_DEFINITION = new BondFuturesYieldAverageTransactionDefinition(FUT_SEC_DEFINITION,
      QUANTITY, TRADE_DATE, TRADE_PRICE);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullSecurity() {
    new BondFuturesYieldAverageTransactionDefinition(null, QUANTITY, TRADE_DATE, TRADE_PRICE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullTradeDate() {
    new BondFuturesYieldAverageTransactionDefinition(FUT_SEC_DEFINITION, QUANTITY, null, TRADE_PRICE);
  }

  @Test
  public void getter() {
    assertEquals("YieldAverageBondFuturesTransactionDefinition: getter", FUT_SEC_DEFINITION, FUT_TRA_DEFINITION.getUnderlyingSecurity());
    assertEquals("YieldAverageBondFuturesTransactionDefinition: getter", QUANTITY, FUT_TRA_DEFINITION.getQuantity());
    assertEquals("YieldAverageBondFuturesTransactionDefinition: getter", TRADE_DATE, FUT_TRA_DEFINITION.getTradeDate());
    assertEquals("YieldAverageBondFuturesTransactionDefinition: getter", TRADE_PRICE, FUT_TRA_DEFINITION.getTradePrice());
  }

  @Test
  public void equalHash() {
    final BondFuturesYieldAverageTransactionDefinition other = new BondFuturesYieldAverageTransactionDefinition(FUT_SEC_DEFINITION,
        QUANTITY, TRADE_DATE, TRADE_PRICE);
    assertEquals("YieldAverageBondFuturesTransactionDefinition: equal - hash", FUT_TRA_DEFINITION, other);
    assertEquals("YieldAverageBondFuturesSecurityDefinition: equal - hash", FUT_TRA_DEFINITION.hashCode(), other.hashCode());
    BondFuturesYieldAverageTransactionDefinition modified;
    final BondFuturesYieldAverageSecurityDefinition futSecDefinitionModified = new BondFuturesYieldAverageSecurityDefinition(LAST_TRADING_DATE,
        BASKET_SECURITY_DEFINITION, SYNTHETIC_COUPON, TENOR + 1, NOTIONAL_FUTURES);
    modified = new BondFuturesYieldAverageTransactionDefinition(futSecDefinitionModified, QUANTITY, TRADE_DATE, TRADE_PRICE);
    assertFalse("YieldAverageBondFuturesSecurityDefinition: equal - hash", FUT_SEC_DEFINITION.equals(modified));
    modified = new BondFuturesYieldAverageTransactionDefinition(FUT_SEC_DEFINITION, QUANTITY + 1, TRADE_DATE, TRADE_PRICE);
    assertFalse("YieldAverageBondFuturesSecurityDefinition: equal - hash", FUT_SEC_DEFINITION.equals(modified));
    modified = new BondFuturesYieldAverageTransactionDefinition(FUT_SEC_DEFINITION, QUANTITY, TRADE_DATE.plusDays(1), TRADE_PRICE);
    assertFalse("YieldAverageBondFuturesSecurityDefinition: equal - hash", FUT_SEC_DEFINITION.equals(modified));
    modified = new BondFuturesYieldAverageTransactionDefinition(FUT_SEC_DEFINITION, QUANTITY, TRADE_DATE, TRADE_PRICE * 0.99);
    assertFalse("YieldAverageBondFuturesSecurityDefinition: equal - hash", FUT_SEC_DEFINITION.equals(modified));
  }

}

/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.future;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.time.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumTransaction;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.DateUtils;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.basics.date.HolidayCalendars;

/**
 * Tests the interest rate future option with margin transaction description.
 */
@Test
public class InterestRateFutureOptionPremiumTransactionDefinitionTest {

  private static final HolidayCalendar CALENDAR = HolidayCalendars.SAT_SUN;
  private static final IborIndex IBOR_INDEX = IndexIborMaster.getInstance().getIndex("EURIBOR3M");

  // Future option mid-curve 1Y
  private static final ZonedDateTime SPOT_LAST_TRADING_DATE = DateUtils.getUTCDate(2012, 9, 19);
  private static final ZonedDateTime LAST_TRADING_DATE = ScheduleCalculator.getAdjustedDate(SPOT_LAST_TRADING_DATE, -IBOR_INDEX.getSpotLag(), CALENDAR);
  private static final double NOTIONAL = 1000000.0; // 1m
  private static final double FUTURE_FACTOR = 0.25;
  private static final String NAME = "ERU2";
  private static final double STRIKE = 0.9895;
  private static final InterestRateFutureSecurityDefinition ERU2 = new InterestRateFutureSecurityDefinition(LAST_TRADING_DATE, IBOR_INDEX, NOTIONAL, FUTURE_FACTOR, NAME, CALENDAR);
  private static final ZonedDateTime EXPIRATION_DATE = DateUtils.getUTCDate(2011, 9, 16);
  private static final boolean IS_CALL = true;
  private static final InterestRateFutureOptionPremiumSecurityDefinition OPTION_EDU2 = new InterestRateFutureOptionPremiumSecurityDefinition(ERU2, EXPIRATION_DATE, STRIKE, IS_CALL);
  // Transaction
  private static final int QUANTITY = -123;
  private static final ZonedDateTime PREMIUM_DATE = DateUtils.getUTCDate(2011, 5, 12);
  private static final double TRADE_PRICE = 0.0050;
  private static final InterestRateFutureOptionPremiumTransactionDefinition OPTION_TRANSACTION = new InterestRateFutureOptionPremiumTransactionDefinition(OPTION_EDU2, QUANTITY, PREMIUM_DATE,
      TRADE_PRICE);
  // Derivative
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 8, 18);
  private static final String DISCOUNTING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES = {DISCOUNTING_CURVE_NAME, FORWARD_CURVE_NAME };

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlying() {
    new InterestRateFutureOptionPremiumTransactionDefinition(null, QUANTITY, PREMIUM_DATE, TRADE_PRICE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTradeDate() {
    new InterestRateFutureOptionPremiumTransactionDefinition(OPTION_EDU2, QUANTITY, null, TRADE_PRICE);
  }

  @Test
  /**
   * Tests the class getters.
   */
  public void getter() {
    assertEquals(OPTION_EDU2, OPTION_TRANSACTION.getUnderlyingOption());
    assertEquals(QUANTITY, OPTION_TRANSACTION.getQuantity());
    assertEquals(PREMIUM_DATE, OPTION_TRANSACTION.getPremium().getPaymentDate());
    assertEquals(TRADE_PRICE, OPTION_TRANSACTION.getTradePrice());
  }

  @Test
  /**
   * Tests the equal and hashCode methods.
   */
  public void equalHash() {
    final InterestRateFutureOptionPremiumTransactionDefinition other = new InterestRateFutureOptionPremiumTransactionDefinition(OPTION_EDU2, QUANTITY, PREMIUM_DATE, TRADE_PRICE);
    assertTrue(OPTION_TRANSACTION.equals(other));
    assertTrue(OPTION_TRANSACTION.hashCode() == other.hashCode());
    InterestRateFutureOptionPremiumTransactionDefinition modifidOption;
    modifidOption = new InterestRateFutureOptionPremiumTransactionDefinition(OPTION_EDU2, QUANTITY + 1, PREMIUM_DATE, TRADE_PRICE);
    assertFalse(OPTION_TRANSACTION.equals(modifidOption));
    modifidOption = new InterestRateFutureOptionPremiumTransactionDefinition(OPTION_EDU2, QUANTITY, LAST_TRADING_DATE, TRADE_PRICE);
    assertFalse(OPTION_TRANSACTION.equals(modifidOption));
    modifidOption = new InterestRateFutureOptionPremiumTransactionDefinition(OPTION_EDU2, QUANTITY, PREMIUM_DATE, TRADE_PRICE - 0.00001);
    assertFalse(OPTION_TRANSACTION.equals(modifidOption));
  }

  @Test
  /**
   * Tests the toDerivative method when the reference date is before the premium settlement.
   */
  public void toDerivativeBeforeSettlement() {
    final InterestRateFutureOptionPremiumTransaction transactionConverted = OPTION_TRANSACTION.toDerivative(REFERENCE_DATE);
    final InterestRateFutureOptionPremiumSecurity security = OPTION_EDU2.toDerivative(REFERENCE_DATE);
    final double premiumTime = TimeCalculator.getTimeBetween(REFERENCE_DATE, PREMIUM_DATE);
    final InterestRateFutureOptionPremiumTransaction transaction = new InterestRateFutureOptionPremiumTransaction(security, QUANTITY, premiumTime, TRADE_PRICE);
    assertEquals("Option on future: to derivative", transaction, transactionConverted);
  }

  @Test
  /**
   * Tests the toDerivative method when the reference date is on the premium settlement.
   */
  public void toDerivativeOnSettlement() {
    final ZonedDateTime referenceDate = PREMIUM_DATE;
    final InterestRateFutureOptionPremiumTransaction transactionConverted = OPTION_TRANSACTION.toDerivative(referenceDate);
    final InterestRateFutureOptionPremiumSecurity security = OPTION_EDU2.toDerivative(referenceDate);
    final double premiumTime = 0.0;
    final InterestRateFutureOptionPremiumTransaction transaction = new InterestRateFutureOptionPremiumTransaction(security, QUANTITY, premiumTime, TRADE_PRICE);
    assertEquals("Option on future: to derivative", transaction, transactionConverted);
  }

  @Test
  /**
   * Tests the toDerivative method when the reference date is after the premium settlement.
   */
  public void toDerivativeAfterSettlement() {
    final ZonedDateTime referenceDate = PREMIUM_DATE.plusDays(1);
    final InterestRateFutureOptionPremiumTransaction transactionConverted = OPTION_TRANSACTION.toDerivative(referenceDate);
    final InterestRateFutureOptionPremiumSecurity security = OPTION_EDU2.toDerivative(referenceDate);
    final double premiumTime = 0.0;
    final double price = 0.0; // The payment is in the past and is represented by a 0 payment today.
    final InterestRateFutureOptionPremiumTransaction transaction = new InterestRateFutureOptionPremiumTransaction(security, QUANTITY, premiumTime, price);
    assertEquals("Option on future: to derivative", transaction, transactionConverted);
  }
}

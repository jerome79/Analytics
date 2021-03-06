/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.volatilityswap;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.AnalyticsTestBase;
import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
import com.opengamma.analytics.financial.volatilityswap.FXVolatilitySwap;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.basics.date.HolidayCalendars;
import com.opengamma.strata.basics.schedule.Frequency;

/**
 * Tests the volatility swap definition object.
 */
@Test
public class FXVolatilitySwapDefinitionTest extends AnalyticsTestBase {
  /** The current date */
  private static final ZonedDateTime NOW = ZonedDateTime.of(2014, 02, 27, 12, 0, 0, 0, ZoneId.of("UTC"));
  /** The settlement date */
  private static final ZonedDateTime T_PLUS_2D = NOW.plusDays(2);
  /** The maturity date */
  private static final ZonedDateTime T_PLUS_5Y = NOW.plusYears(5);
  /** The observation frequency */
  private static final Frequency OBSERVATION_FREQUENCY = Frequency.P1D;
  /** The currency */
  private static final Currency CCY = Currency.EUR;
  /** The base currency */
  private static final Currency BASE = Currency.EUR;
  /** The counter currency */
  private static final Currency COUNTER = Currency.USD;
  /** The calendar */
  private static final HolidayCalendar WEEKENDS = HolidayCalendars.SAT_SUN;
  /** The number of observations per year */
  private static final double OBS_PER_YEAR = 250;
  /** The volatility strike */
  private static final double VOL_STRIKE = 0.25;
  /** The volatility notional */
  private static final double VOL_NOTIONAL = 1.0E6;
  /** A Volatility swap definition */
  private static final FXVolatilitySwapDefinition DEFINITION = new FXVolatilitySwapDefinition(CCY, BASE, COUNTER, VOL_STRIKE, VOL_NOTIONAL, T_PLUS_2D, T_PLUS_5Y,
      T_PLUS_2D, T_PLUS_5Y, OBSERVATION_FREQUENCY, OBS_PER_YEAR, WEEKENDS);

  /**
   * @throws Exception If a FX volatility swap definition cannot be created from the inputs
   */
  private FXVolatilitySwapDefinitionTest() throws Exception {
    super(FXVolatilitySwapDefinition.class,
        new Object[] {CCY, BASE, COUNTER, VOL_STRIKE, VOL_NOTIONAL, T_PLUS_2D, T_PLUS_5Y, NOW, T_PLUS_5Y, OBSERVATION_FREQUENCY, OBS_PER_YEAR, WEEKENDS },
        new Class[] {Currency.class, Currency.class, Currency.class, double.class, double.class,
          ZonedDateTime.class, ZonedDateTime.class, ZonedDateTime.class, ZonedDateTime.class, Frequency.class, double.class,
          HolidayCalendar.class },
        new boolean[] {true, true, true, false, false, true, true, true, true, true, false, true });
  }

  /**
   * Tests the getters.
   */
  @Test
  public void testGetters() {
    assertEquals(OBS_PER_YEAR, DEFINITION.getAnnualizationFactor());
    assertEquals(WEEKENDS, DEFINITION.getCalendar());
    assertEquals(CCY, DEFINITION.getCurrency());
    assertEquals(T_PLUS_5Y, DEFINITION.getObservationEndDate());
    assertEquals(1303, DEFINITION.getNumberOfObservationsExpected());
    assertEquals(OBSERVATION_FREQUENCY, DEFINITION.getObservationFrequency());
    assertEquals(T_PLUS_2D, DEFINITION.getObservationStartDate());
    assertEquals(T_PLUS_2D, DEFINITION.getEffectiveDate());
    assertEquals(T_PLUS_5Y, DEFINITION.getMaturityDate());
    assertEquals(VOL_NOTIONAL, DEFINITION.getVolatilityNotional());
    assertEquals(VOL_STRIKE, DEFINITION.getVolatilityStrike());
    assertEquals(BASE, DEFINITION.getBaseCurrency());
    assertEquals(COUNTER, DEFINITION.getCounterCurrency());
  }

  /**
   * Tests the hashcode and equals methods.
   */
  @Test
  public void testHashCodeEquals() {
    FXVolatilitySwapDefinition other = new FXVolatilitySwapDefinition(CCY, BASE, COUNTER, VOL_STRIKE, VOL_NOTIONAL, T_PLUS_2D, T_PLUS_5Y,
        T_PLUS_2D, T_PLUS_5Y, OBSERVATION_FREQUENCY, OBS_PER_YEAR, WEEKENDS);
    assertEquals(DEFINITION, DEFINITION);
    assertEquals(DEFINITION, other);
    assertEquals(DEFINITION.hashCode(), other.hashCode());
    assertFalse(DEFINITION.equals(null));
    assertFalse(DEFINITION.equals(new CashDefinition(CCY, NOW, T_PLUS_5Y, VOL_NOTIONAL, VOL_STRIKE, 5)));
    other = new FXVolatilitySwapDefinition(Currency.USD, BASE, COUNTER, VOL_STRIKE, VOL_NOTIONAL, T_PLUS_2D, T_PLUS_5Y,
        T_PLUS_2D, T_PLUS_5Y, OBSERVATION_FREQUENCY, OBS_PER_YEAR, WEEKENDS);
    assertFalse(other.equals(DEFINITION));
    other = new FXVolatilitySwapDefinition(CCY, BASE, COUNTER, VOL_STRIKE + 0.01, VOL_NOTIONAL, T_PLUS_2D, T_PLUS_5Y,
        T_PLUS_2D, T_PLUS_5Y, OBSERVATION_FREQUENCY, OBS_PER_YEAR, WEEKENDS);
    assertFalse(other.equals(DEFINITION));
    other = new FXVolatilitySwapDefinition(CCY, Currency.AUD, COUNTER, VOL_STRIKE, VOL_NOTIONAL, T_PLUS_2D, T_PLUS_5Y,
        T_PLUS_2D, T_PLUS_5Y, OBSERVATION_FREQUENCY, OBS_PER_YEAR, WEEKENDS);
    assertFalse(other.equals(DEFINITION));
    other = new FXVolatilitySwapDefinition(CCY, BASE, Currency.AUD, VOL_STRIKE, VOL_NOTIONAL, T_PLUS_2D, T_PLUS_5Y,
        T_PLUS_2D, T_PLUS_5Y, OBSERVATION_FREQUENCY, OBS_PER_YEAR, WEEKENDS);
    assertFalse(other.equals(DEFINITION));
    other = new FXVolatilitySwapDefinition(CCY, BASE, COUNTER, VOL_STRIKE, VOL_NOTIONAL * 10, T_PLUS_2D, T_PLUS_5Y,
        T_PLUS_2D, T_PLUS_5Y, OBSERVATION_FREQUENCY, OBS_PER_YEAR, WEEKENDS);
    assertFalse(other.equals(DEFINITION));
    other = new FXVolatilitySwapDefinition(CCY, BASE, COUNTER, VOL_STRIKE, VOL_NOTIONAL, T_PLUS_2D.plusDays(1), T_PLUS_5Y,
        T_PLUS_2D, T_PLUS_5Y, OBSERVATION_FREQUENCY, OBS_PER_YEAR, WEEKENDS);
    assertFalse(other.equals(DEFINITION));
    other = new FXVolatilitySwapDefinition(CCY, BASE, COUNTER, VOL_STRIKE, VOL_NOTIONAL, T_PLUS_2D, T_PLUS_5Y.plusDays(1),
        T_PLUS_2D, T_PLUS_5Y, OBSERVATION_FREQUENCY, OBS_PER_YEAR, WEEKENDS);
    assertFalse(other.equals(DEFINITION));
    other = new FXVolatilitySwapDefinition(CCY, BASE, COUNTER, VOL_STRIKE, VOL_NOTIONAL, T_PLUS_2D, T_PLUS_5Y,
        T_PLUS_2D.plusDays(1), T_PLUS_5Y, OBSERVATION_FREQUENCY, OBS_PER_YEAR, WEEKENDS);
    assertFalse(other.equals(DEFINITION));
    other = new FXVolatilitySwapDefinition(CCY, BASE, COUNTER, VOL_STRIKE, VOL_NOTIONAL, T_PLUS_2D, T_PLUS_5Y,
        T_PLUS_2D, T_PLUS_5Y.plusDays(1), OBSERVATION_FREQUENCY, OBS_PER_YEAR, WEEKENDS);
    assertFalse(other.equals(DEFINITION));
    other = new FXVolatilitySwapDefinition(CCY, BASE, COUNTER, VOL_STRIKE, VOL_NOTIONAL, T_PLUS_2D, T_PLUS_5Y,
        T_PLUS_2D, T_PLUS_5Y, OBSERVATION_FREQUENCY, OBS_PER_YEAR + 1, WEEKENDS);
    assertFalse(other.equals(DEFINITION));
    other = new FXVolatilitySwapDefinition(CCY, BASE, COUNTER, VOL_STRIKE, VOL_NOTIONAL, T_PLUS_2D, T_PLUS_5Y,
        T_PLUS_2D, T_PLUS_5Y, OBSERVATION_FREQUENCY, OBS_PER_YEAR, HolidayCalendars.NO_HOLIDAYS);
    assertFalse(other.equals(DEFINITION));
  }

  /**
   * Tests creation of a forward-starting volatility swap derivative
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testForwardStarting() {
    final FXVolatilitySwap volatilitySwap = DEFINITION.toDerivative(NOW);
    assertEquals(OBS_PER_YEAR, volatilitySwap.getAnnualizationFactor());
    assertEquals(CCY, volatilitySwap.getCurrency());
    assertEquals(5.174603174603175, volatilitySwap.getTimeToObservationEnd(), 1.e-12);
    assertEquals(2. / 252, volatilitySwap.getTimeToObservationStart(), 0);
    assertEquals(OBSERVATION_FREQUENCY, volatilitySwap.getObservationFrequency());
    assertEquals(5.174603174603175, volatilitySwap.getTimeToMaturity(), 1.e-12);
    assertEquals(VOL_NOTIONAL, volatilitySwap.getVolatilityNotional(), 1.e-12);
    assertEquals(VOL_STRIKE, volatilitySwap.getVolatilityStrike());
    assertEquals(volatilitySwap, DEFINITION.toDerivative(NOW));
    assertEquals(BASE, volatilitySwap.getBaseCurrency());
    assertEquals(COUNTER, volatilitySwap.getCounterCurrency());
  }

  /**
   * Tests creation of a seasoned volatility swap derivative
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testSeasoned() {
    final FXVolatilitySwapDefinition definition = new FXVolatilitySwapDefinition(CCY, BASE, COUNTER, VOL_STRIKE, VOL_NOTIONAL, T_PLUS_2D, T_PLUS_5Y,
        T_PLUS_2D, T_PLUS_5Y, OBSERVATION_FREQUENCY, OBS_PER_YEAR, HolidayCalendars.NO_HOLIDAYS);
    final FXVolatilitySwap volatilitySwap = definition.toDerivative(NOW.plusYears(1));
    assertEquals(OBS_PER_YEAR, volatilitySwap.getAnnualizationFactor());
    assertEquals(CCY, volatilitySwap.getCurrency());
    assertEquals(5.7976190476190474, volatilitySwap.getTimeToObservationEnd(), 1.e-12);
    assertEquals(-1.4404761904761905, volatilitySwap.getTimeToObservationStart(), 1.e-12);
    assertEquals(OBSERVATION_FREQUENCY, volatilitySwap.getObservationFrequency());
    assertEquals(5.7976190476190474, volatilitySwap.getTimeToMaturity(), 1.e-12);
    assertEquals(VOL_NOTIONAL, volatilitySwap.getVolatilityNotional());
    assertEquals(VOL_STRIKE, volatilitySwap.getVolatilityStrike(), 0);
    assertEquals(BASE, volatilitySwap.getBaseCurrency());
    assertEquals(COUNTER, volatilitySwap.getCounterCurrency());
  }

  /**
   * Tests expected failure for observation frequencies other than daily
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWeeklyObservations() {
    final Frequency freqWeek = Frequency.P1W;
    new FXVolatilitySwapDefinition(CCY, BASE, COUNTER, VOL_STRIKE, VOL_NOTIONAL, T_PLUS_2D, T_PLUS_5Y, T_PLUS_2D, T_PLUS_5Y, freqWeek, OBS_PER_YEAR, WEEKENDS);
  }

  /**
   * Tests that the base and counter currency are different.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDifferentFXCurrencies() {
    new FXVolatilitySwapDefinition(CCY, BASE, BASE, VOL_STRIKE, VOL_NOTIONAL, T_PLUS_2D, T_PLUS_5Y, T_PLUS_2D, T_PLUS_5Y, OBSERVATION_FREQUENCY, OBS_PER_YEAR, WEEKENDS);
  }
}

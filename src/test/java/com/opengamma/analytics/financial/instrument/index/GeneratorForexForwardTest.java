/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import static org.testng.AssertJUnit.assertEquals;

import java.time.Period;
import java.time.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.convention.businessday.BusinessDayConvention;
import com.opengamma.analytics.convention.businessday.BusinessDayConventions;
import com.opengamma.analytics.financial.forex.definition.ForexDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.DateUtils;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.currency.FxMatrix;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.basics.date.HolidayCalendars;

/**
 * Tests the constructor and method of GeneratorForexForward.
 */
@Test
public class GeneratorForexForwardTest {
  
  private static final String NAME = "EUR/USD Forward";
  private static final int SETTLEMENT_DAYS = 2;
  private static final HolidayCalendar CALENDAR = HolidayCalendars.SAT_SUN;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final Currency USD = Currency.USD;
  private static final Currency EUR = Currency.EUR;
  private static final GeneratorForexForward GENERATOR_FX_EURUSD = new GeneratorForexForward(NAME, EUR, USD, CALENDAR,
      SETTLEMENT_DAYS, BUSINESS_DAY, IS_EOM);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCurrency1() {
    new GeneratorForexSwap(NAME, null, USD, CALENDAR, SETTLEMENT_DAYS, BUSINESS_DAY, IS_EOM);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCurrency2() {
    new GeneratorForexSwap(NAME, EUR, null, CALENDAR, SETTLEMENT_DAYS, BUSINESS_DAY, IS_EOM);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCalendar() {
    new GeneratorForexSwap(NAME, EUR, USD, null, SETTLEMENT_DAYS, BUSINESS_DAY, IS_EOM);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullBusinessDay() {
    new GeneratorForexSwap(NAME, EUR, USD, CALENDAR, SETTLEMENT_DAYS, null, IS_EOM);
  }

  @Test
  public void getter() {
    assertEquals("Generator Deposit: getter", NAME, GENERATOR_FX_EURUSD.getName());
    assertEquals("Generator Deposit: getter", EUR, GENERATOR_FX_EURUSD.getCurrency1());
    assertEquals("Generator Deposit: getter", USD, GENERATOR_FX_EURUSD.getCurrency2());
    assertEquals("Generator Deposit: getter", CALENDAR, GENERATOR_FX_EURUSD.getCalendar());
    assertEquals("Generator Deposit: getter", SETTLEMENT_DAYS, GENERATOR_FX_EURUSD.getSpotLag());
    assertEquals("Generator Deposit: getter", BUSINESS_DAY, GENERATOR_FX_EURUSD.getBusinessDayConvention());
    assertEquals("Generator Deposit: getter", IS_EOM, GENERATOR_FX_EURUSD.isEndOfMonth());
  }

  @Test
  public void generateInstrument() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 7, 17);
    final Period tenor = Period.ofMonths(6);
    final double pts = 0.01;
    final double eurUsd = 1.25;
    final double notional = 123000000;
    final FxMatrix fxMatrix = FxMatrix.builder().addRate(EUR, USD, eurUsd).build();
    final GeneratorAttributeFX attribute = new GeneratorAttributeFX(tenor, fxMatrix);
    final ForexDefinition insGenerated = GENERATOR_FX_EURUSD.generateInstrument(referenceDate, pts, notional, attribute);
    final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(referenceDate, SETTLEMENT_DAYS, CALENDAR);
    final ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDate, tenor, BUSINESS_DAY, CALENDAR, IS_EOM);
    final ForexDefinition insExpected = new ForexDefinition(EUR, USD, endDate, notional, eurUsd + pts);
    assertEquals("Generator FX Forward: generate instrument", insExpected, insGenerated);
  }

}

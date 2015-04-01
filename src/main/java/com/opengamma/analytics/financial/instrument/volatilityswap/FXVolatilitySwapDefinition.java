/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.volatilityswap;

import java.time.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.convention.daycount.DayCountFactory;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.volatilityswap.FXVolatilitySwap;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.basics.schedule.Frequency;
import com.opengamma.strata.collect.ArgChecker;

/**
 *
 */
public class FXVolatilitySwapDefinition extends VolatilitySwapDefinition {

  /**
   * The base currency.
   */
  private final Currency _baseCurrency;

  /**
   * The counter currency.
   */
  private final Currency _counterCurrency;

  /**
   * @param currency The currency, not null
   * @param baseCurrency The base currency, not null
   * @param counterCurrency The counter currency, not null
   * @param volStrike The volatility strike, not negative
   * @param volNotional The volatility notional
   * @param observationStartDate The observation start date, not null
   * @param observationEndDate The observation end date, not null
   * @param effectiveDate The effective date, not null
   * @param maturityDate The maturity date, not null
   * @param observationFrequency The observation frequency, not null
   * @param annualizationFactor The annualization factor, greater than zero
   * @param calendar The holiday calendar, not null
   */
  public FXVolatilitySwapDefinition(
      Currency currency,
      Currency baseCurrency,
      Currency counterCurrency,
      double volStrike,
      double volNotional,
      ZonedDateTime observationStartDate,
      ZonedDateTime observationEndDate,
      ZonedDateTime effectiveDate,
      ZonedDateTime maturityDate,
      Frequency observationFrequency,
      double annualizationFactor,
      HolidayCalendar calendar) {

    super(currency, volStrike, volNotional, observationStartDate, observationEndDate, effectiveDate, maturityDate, observationFrequency, annualizationFactor, calendar);
    ArgChecker.notNull(baseCurrency, "baseCurrency");
    ArgChecker.notNull(counterCurrency, "counterCurrency");
    ArgChecker.isFalse(baseCurrency.equals(counterCurrency), "base currency and counter currency cannot be equal");
    _baseCurrency = baseCurrency;
    _counterCurrency = counterCurrency;
  }

  /**
   * Gets the base currency.
   * @return the base currency
   */
  public Currency getBaseCurrency() {
    return _baseCurrency;
  }

  /**
   * Gets the counter currency.
   * @return the counter currency
   */
  public Currency getCounterCurrency() {
    return _counterCurrency;
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgChecker.notNull(visitor, "visitor");
    return visitor.visitFXVolatilitySwapDefinition(this);
  }

  @Override
  public FXVolatilitySwap toDerivative(final ZonedDateTime date) {
    ArgChecker.notNull(date, "date");
    final double timeToObservationStart = TimeCalculator.getTimeBetween(date, getObservationStartDate(), DayCountFactory.of("Business/252"), getCalendar());
    final double timeToObservationEnd = TimeCalculator.getTimeBetween(date, getObservationEndDate(), DayCountFactory.of("Business/252"), getCalendar());
    final double timeToMaturity = TimeCalculator.getTimeBetween(date, getMaturityDate(), DayCountFactory.of("Business/252"), getCalendar());
    return new FXVolatilitySwap(timeToObservationStart, timeToObservationEnd, getObservationFrequency(), timeToMaturity,
        getVolatilityStrike(), getVolatilityNotional(), getCurrency(), _baseCurrency, _counterCurrency, getAnnualizationFactor());
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _baseCurrency.hashCode();
    result = prime * result + _counterCurrency.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (!(obj instanceof FXVolatilitySwapDefinition)) {
      return false;
    }
    final FXVolatilitySwapDefinition other = (FXVolatilitySwapDefinition) obj;
    if (!ObjectUtils.equals(_baseCurrency, other._baseCurrency)) {
      return false;
    }
    if (!ObjectUtils.equals(_counterCurrency, other._counterCurrency)) {
      return false;
    }
    return true;
  }

}

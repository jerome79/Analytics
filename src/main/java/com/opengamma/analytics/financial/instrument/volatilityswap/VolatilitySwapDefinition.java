/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.volatilityswap;

import java.time.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.convention.businessday.BusinessDayDateUtils;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.volatilityswap.VolatilitySwap;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.basics.schedule.Frequency;
import com.opengamma.strata.collect.ArgChecker;

/**
 * A volatility swap is a forward contract on the realized volatility of an underlying security.
 */
public class VolatilitySwapDefinition implements InstrumentDefinition<VolatilitySwap> {
  /** The currency */
  private final Currency _currency;
  /** The volatility strike */
  private final double _volStrike;
  /** The volatility notional */
  private final double _volNotional;
  /** The volatility observation period start date */
  private final ZonedDateTime _observationStartDate;
  /** The volatility observation period end date */
  private final ZonedDateTime _observationEndDate;
  /** The effective date */
  private final ZonedDateTime _effectiveDate;
  /** The maturity date */
  private final ZonedDateTime _maturityDate;
  /** The observation frequency */
  private final Frequency _observationFrequency;
  /** The number of observations expected given the observation dates and the holiday calendar */
  private final int _nObservations;
  /** The annualization factor */
  private final double _annualizationFactor;
  /** The holiday calendar */
  private final HolidayCalendar _calendar;

  /**
   * @param currency The currency, not null
   * @param volStrike The volatility strike, not negative
   * @param volNotional The volatility notional
   * @param observationStartDate The observation start date, not null
   * @param observationEndDate The observation end date, not null
   * @param effectiveDate The effective date, not null
   * @param maturityDate The maturity date, not null
   * @param observationFrequency The observation frequency, not null, must be daily 
   * @param annualizationFactor The annualization factor, greater than zero
   * @param calendar The holiday calendar, not null
   */
  public VolatilitySwapDefinition(
      Currency currency,
      double volStrike,
      double volNotional,
      ZonedDateTime observationStartDate,
      ZonedDateTime observationEndDate,
      ZonedDateTime effectiveDate,
      ZonedDateTime maturityDate,
      Frequency observationFrequency,
      double annualizationFactor,
      HolidayCalendar calendar) {

    ArgChecker.notNull(currency, "currency");
    ArgChecker.notNegative(volStrike, "volStrike");
    ArgChecker.notNull(observationStartDate, "observationStartDate");
    ArgChecker.notNull(observationEndDate, "observationEndDate");
    ArgChecker.notNull(effectiveDate, "maturityDate");
    ArgChecker.notNull(maturityDate, "maturityDate");
    ArgChecker.notNull(observationFrequency, "observationFrequency");
    ArgChecker.notNegativeOrZero(annualizationFactor, "annualizationFactor");
    ArgChecker.notNull(calendar, "calendar");
    ArgChecker.isTrue(observationFrequency.equals(Frequency.P1D),
        "Only DAILY observation frequencies are currently supported. obsFreq {} ",
        observationFrequency.toString());
    _currency = currency;
    _volStrike = volStrike;
    _volNotional = volNotional;
    _effectiveDate = effectiveDate;
    _maturityDate = maturityDate;
    _observationStartDate = observationStartDate;
    _observationEndDate = observationEndDate;
    _observationFrequency = observationFrequency;
    _annualizationFactor = annualizationFactor;
    _calendar = calendar;
    _nObservations = BusinessDayDateUtils.getWorkingDaysInclusive(observationStartDate, observationEndDate, calendar);
  }

  /**
   * Gets the currency.
   * @return the currency
   */
  public Currency getCurrency() {
    return _currency;
  }

  /**
   * Gets the volatility strike.
   * @return The volatility strike
   */
  public double getVolatilityStrike() {
    return _volStrike;
  }

  /**
   * Gets the volatility notional.
   * @return The volatility notional
   */
  public double getVolatilityNotional() {
    return _volNotional;
  }

  /**
   * Gets the observation start date.
   * @return The observation start date
   */
  public ZonedDateTime getObservationStartDate() {
    return _observationStartDate;
  }

  /**
   * Gets the observation end date.
   * @return the observation end date
   */
  public ZonedDateTime getObservationEndDate() {
    return _observationEndDate;
  }

  /**
   * Gets the effective date.
   * @return the effective date
   */
  public ZonedDateTime getEffectiveDate() {
    return _effectiveDate;
  }

  /**
   * Gets the maturity date.
   * @return the maturity date
   */
  public ZonedDateTime getMaturityDate() {
    return _maturityDate;
  }

  /**
   * Gets the observation frequency.
   * @return the observation frequency
   */
  public Frequency getObservationFrequency() {
    return _observationFrequency;
  }

  /**
   * Gets the number of expected observations of the underlying.
   * @return The number of expected observations of the underlying
   */
  public int getNumberOfObservationsExpected() {
    return _nObservations;
  }

  /**
   * Gets the annualization factor.
   * @return the annualization factor
   */
  public double getAnnualizationFactor() {
    return _annualizationFactor;
  }

  /**
   * Gets the calendar.
   * @return the calendar
   */
  public HolidayCalendar getCalendar() {
    return _calendar;
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgChecker.notNull(visitor, "visitor");
    return visitor.visitVolatilitySwapDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgChecker.notNull(visitor, "visitor");
    return visitor.visitVolatilitySwapDefinition(this);
  }

  @Override
  public VolatilitySwap toDerivative(final ZonedDateTime date) {
    ArgChecker.notNull(date, "date");
    final double timeToObservationStart = TimeCalculator.getTimeBetween(date, _observationStartDate);
    final double timeToObservationEnd = TimeCalculator.getTimeBetween(date, _observationEndDate);
    final double timeToMaturity = TimeCalculator.getTimeBetween(date, _maturityDate);
    return new VolatilitySwap(timeToObservationStart, timeToObservationEnd, _observationFrequency, timeToMaturity, _volStrike, _volNotional,
        _currency, _annualizationFactor);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_annualizationFactor);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _calendar.hashCode();
    result = prime * result + _currency.hashCode();
    result = prime * result + _nObservations;
    result = prime * result + _observationEndDate.hashCode();
    result = prime * result + _observationFrequency.hashCode();
    result = prime * result + _observationStartDate.hashCode();
    result = prime * result + _effectiveDate.hashCode();
    result = prime * result + _maturityDate.hashCode();
    temp = Double.doubleToLongBits(_volNotional);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_volStrike);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof VolatilitySwapDefinition)) {
      return false;
    }
    final VolatilitySwapDefinition other = (VolatilitySwapDefinition) obj;
    if (Double.compare(_volStrike, other._volStrike) != 0) {
      return false;
    }
    if (Double.compare(_volNotional, other._volNotional) != 0) {
      return false;
    }
    if (Double.doubleToLongBits(_annualizationFactor) != Double.doubleToLongBits(other._annualizationFactor)) {
      return false;
    }
    if (!ObjectUtils.equals(_currency, other._currency)) {
      return false;
    }
    if (_nObservations != other._nObservations) {
      return false;
    }
    if (!ObjectUtils.equals(_effectiveDate, other._effectiveDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_maturityDate, other._maturityDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_observationStartDate, other._observationStartDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_observationEndDate, other._observationEndDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_observationFrequency, other._observationFrequency)) {
      return false;
    }
    if (!ObjectUtils.equals(_calendar, other._calendar)) {
      return false;
    }
    return true;
  }

}

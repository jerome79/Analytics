/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import java.time.ZonedDateTime;
import java.util.Objects;

import com.opengamma.analytics.util.time.DateUtils;
import com.opengamma.analytics.util.time.Expiry;
import com.opengamma.strata.collect.ArgChecker;

/**
 * 
 * Contains the minimum information required to define an option. Descendant
 * classes must define a payoff function, which gives the payoff given a
 * particular spot value, and an exercise function, which calculates whether an
 * option should be exercised.
 * 
 */
public abstract class OptionDefinition {
  private final Double _strike;
  private final Expiry _expiry;
  private final Boolean _isCall;

  /**
   * 
   * @param strike The strike
   * @param expiry The expiry, not null
   * @param isCall Is the option a put or call
   */
  public OptionDefinition(final Double strike, final Expiry expiry, final Boolean isCall) {
    if (strike != null) {
      ArgChecker.notNegative(strike, "strike");
    }
    ArgChecker.notNull(expiry, "expiry");
    _strike = strike;
    _expiry = expiry;
    _isCall = isCall;
  }

  /**
   * 
   * @return Returns the strike.
   */
  public Double getStrike() {
    return _strike;
  }

  /**
   * 
   * @return Returns the expiry.
   */
  public Expiry getExpiry() {
    return _expiry;
  }

  /**
   * 
   * @param date The date
   * @return The time to expiry in years, where a year is defined as having 365.25 days
   */
  public double getTimeToExpiry(final ZonedDateTime date) {
    if (date.isAfter(getExpiry().getExpiry())) {
      throw new IllegalArgumentException("Date " + date + " is after expiry " + getExpiry());
    }
    return DateUtils.getDifferenceInYears(date, getExpiry().getExpiry());
  }

  /**
   * 
   * @return Returns true if the option is a call.
   */
  public Boolean isCall() {
    return _isCall;
  }

  /**
   * @param <T> The data bundle type
   * @return The exercise function.
   */
  public abstract <T extends StandardOptionDataBundle> OptionExerciseFunction<T> getExerciseFunction();

  /**  
   * @param <T> The data bundle type
   * @return The payoff function.
   */
  public abstract <T extends StandardOptionDataBundle> OptionPayoffFunction<T> getPayoffFunction();

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _expiry.hashCode();
    result = prime * result + ((_isCall == null) ? 0 : _isCall.hashCode());
    result = prime * result + ((_strike == null) ? 0 : _strike.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final OptionDefinition other = (OptionDefinition) obj;
    return Objects.equals(_expiry, other._expiry) && Objects.equals(_isCall, other._isCall) && Objects.equals(_strike, other._strike);
  }
}

/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.derivative;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Description of an interest rate future option security.
 */
public abstract class InterestRateFutureOptionSecurity extends FuturesSecurity {

  /**
   * Underlying future security.
   */
  private final InterestRateFutureSecurity _underlyingFuture;
  /**
   * Expiration date.
   */
  private final double _expirationTime;
  /**
   * Cap (true) / floor (false) flag.
   */
  private final boolean _isCall;
  /**
   * Strike price.
   */
  private final double _strike;

  /**
   * Constructor of the option future from the details.
   * @param underlyingFuture The underlying future security.
   * @param expirationTime The time (in year) to expiration.
   * @param strike The option strike.
   * @param isCall The cap (true) / floor (false) flag.
   */
  public InterestRateFutureOptionSecurity(InterestRateFutureSecurity underlyingFuture,
                                          double expirationTime,
                                          double strike,
                                          boolean isCall) {
    super(expirationTime);
    _underlyingFuture = ArgChecker.notNull(underlyingFuture, "underlying future");
    _expirationTime = ArgChecker.notNull(expirationTime, "expirationTime");
    _strike = ArgChecker.notNull(strike, "strike");
    _isCall = ArgChecker.notNull(isCall, "isCall");
  }

  /**
   * Gets the underlying future security.
   * @return The underlying future security.
   */
  public InterestRateFutureSecurity getUnderlyingFuture() {
    return _underlyingFuture;
  }

  /**
   * Gets the expiration date.
   * @return The expiration date.
   */
  public double getExpirationTime() {
    return _expirationTime;
  }

  /**
   * Gets the cap (true) / floor (false) flag.
   * @return The cap/floor flag.
   */
  public boolean isCall() {
    return _isCall;
  }

  /**
   * Gets the option strike.
   * @return The option strike.
   */
  public double getStrike() {
    return _strike;
  }

  /**
   * The future option currency.
   * @return The currency.
   */
  @Override
  public Currency getCurrency() {
    return _underlyingFuture.getCurrency();
  }


  @Override
  public String toString() {
    String result = "Opt. IR future security: ";
    result += "Expiry: " + _expirationTime;
    result += " - Call: " + _isCall;
    result += " - Strike: " + _strike;
    result += " - Underlying: " + _underlyingFuture.toString();
    return result;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_expirationTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + (_isCall ? 1231 : 1237);
    temp = Double.doubleToLongBits(_strike);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _underlyingFuture.hashCode();
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
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final InterestRateFutureOptionSecurity other = (InterestRateFutureOptionSecurity) obj;
    if (Double.doubleToLongBits(_expirationTime) != Double.doubleToLongBits(other._expirationTime)) {
      return false;
    }
    if (_isCall != other._isCall) {
      return false;
    }
    if (Double.doubleToLongBits(_strike) != Double.doubleToLongBits(other._strike)) {
      return false;
    }
    if (!ObjectUtils.equals(_underlyingFuture, other._underlyingFuture)) {
      return false;
    }
    return true;
  }

}

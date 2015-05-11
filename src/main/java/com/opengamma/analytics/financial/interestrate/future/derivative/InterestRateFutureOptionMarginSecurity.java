/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.derivative;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Description of an interest rate future option security with daily margin process
 */
public class InterestRateFutureOptionMarginSecurity extends InterestRateFutureOptionSecurity {

  /**
   * Constructor of the option future from the details.
   * @param underlyingFuture The underlying future security.
   * @param expirationTime The time (in year) to expiration.
   * @param strike The option strike.
   * @param isCall The cap (true) / floor (false) flag.
   */
  public InterestRateFutureOptionMarginSecurity(InterestRateFutureSecurity underlyingFuture,
      double expirationTime,
      double strike,
      boolean isCall) {
    super(underlyingFuture, expirationTime, strike, isCall);
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    ArgChecker.notNull(visitor, "visitor");
    return visitor.visitInterestRateFutureOptionMarginSecurity(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgChecker.notNull(visitor, "visitor");
    return visitor.visitInterestRateFutureOptionMarginSecurity(this);
  }
}

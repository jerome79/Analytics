/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.derivative;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Description of transaction on an interest rate future option with up-front margin security.
 */
public class InterestRateFutureOptionMarginTransaction extends FuturesTransaction<InterestRateFutureOptionMarginSecurity> {

  /**
  * Constructor of the future option transaction from details.
  * @param underlyingOption The underlying option future security.
  * @param quantity The quantity of the transaction. Can be positive or negative.
  * @param referencePrice The reference price.
  */
  public InterestRateFutureOptionMarginTransaction(InterestRateFutureOptionMarginSecurity underlyingOption,
                                                   long quantity,
                                                   double referencePrice) {
    super(underlyingOption, quantity, referencePrice);
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    ArgChecker.notNull(visitor, "visitor");
    return visitor.visitInterestRateFutureOptionMarginTransaction(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgChecker.notNull(visitor, "visitor");
    return visitor.visitInterestRateFutureOptionMarginTransaction(this);
  }

}

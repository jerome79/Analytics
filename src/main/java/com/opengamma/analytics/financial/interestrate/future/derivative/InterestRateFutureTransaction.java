/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.derivative;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Description of an STIR (Short Term Interest Rate) future transaction.
 */
public class InterestRateFutureTransaction extends FuturesTransaction<InterestRateFutureSecurity> {

  /**
   * Constructor from tthe underlying and transaction details.
   * @param underlying The underlying futures security.
   * @param referencePrice The reference price (trading price or last margining price).
   * @param quantity The number of contracts.
   */
  public InterestRateFutureTransaction(final InterestRateFutureSecurity underlying, final double referencePrice, final long quantity) {
    super(underlying, quantity, referencePrice);
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    ArgChecker.notNull(visitor, "visitor");
    return visitor.visitInterestRateFutureTransaction(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgChecker.notNull(visitor, "visitor");
    return visitor.visitInterestRateFutureTransaction(this);
  }

}

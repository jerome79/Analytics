/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.future;

import java.time.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesTransaction;

/**
 * Description of a bond future transaction (definition version).
 */
public class BondFuturesTransactionDefinition extends FuturesTransactionDefinition<BondFuturesSecurityDefinition>
    implements InstrumentDefinitionWithData<BondFuturesTransaction, Double> {

  /**
   * Constructor of the future transaction.
   * @param underlyingFuture Underlying future security.
   * @param quantity Quantity of future. Can be positive or negative.
   * @param tradeDate Transaction date.
   * @param tradePrice Transaction price.
   */
  public BondFuturesTransactionDefinition(final BondFuturesSecurityDefinition underlyingFuture, final long quantity, final ZonedDateTime tradeDate, final double tradePrice) {
    super(underlyingFuture, quantity, tradeDate, tradePrice);
  }

  @Override
  public BondFuturesTransaction toDerivative(final ZonedDateTime date) {
    throw new UnsupportedOperationException("The method toDerivative of BondFutureTransactionDefinition does not support the one argument method (without margin price data).");
  }

  @Override
  public BondFuturesTransaction toDerivative(final ZonedDateTime dateTime, final Double lastMarginPrice) {
    final double referencePrice = referencePrice(dateTime, lastMarginPrice);
    final BondFuturesSecurity underlyingFuture = getUnderlyingSecurity().toDerivative(dateTime);
    return new BondFuturesTransaction(underlyingFuture, getQuantity(), referencePrice);
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    return visitor.visitBondFuturesTransactionDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitBondFuturesTransactionDefinition(this);
  }

}

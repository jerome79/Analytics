/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.future;


import java.time.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.interestrate.future.derivative.SwapFuturesPriceDeliverableSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.SwapFuturesPriceDeliverableTransaction;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Description of an interest rate future security.
 */
public class SwapFuturesPriceDeliverableTransactionDefinition extends FuturesTransactionDefinition<SwapFuturesPriceDeliverableSecurityDefinition>
    implements InstrumentDefinitionWithData<SwapFuturesPriceDeliverableTransaction, Double> {

  /**
   * Constructor.
   * @param underlyingFuture The underlying futures security.
   * @param quantity The quantity of the transaction.
   * @param tradeDate The transaction date.
   * @param tradePrice The transaction price (in the convention of the futures).
   */
  public SwapFuturesPriceDeliverableTransactionDefinition(final SwapFuturesPriceDeliverableSecurityDefinition underlyingFuture, final long quantity,
      final ZonedDateTime tradeDate, final double tradePrice) {
    super(underlyingFuture, quantity, tradeDate, tradePrice);
  }

  /**
   * {@inheritDoc}
   * @param lastMarginPrice The price on which the last margining was done.
   */
  @Override
  public SwapFuturesPriceDeliverableTransaction toDerivative(final ZonedDateTime dateTime, final Double lastMarginPrice) {
    final double referencePrice = referencePrice(dateTime, lastMarginPrice);
    final SwapFuturesPriceDeliverableSecurity underlying = getUnderlyingSecurity().toDerivative(dateTime);
    final SwapFuturesPriceDeliverableTransaction future = new SwapFuturesPriceDeliverableTransaction(underlying, referencePrice, getQuantity());
    return future;
  }

  @Override
  public SwapFuturesPriceDeliverableTransaction toDerivative(final ZonedDateTime date) {
    throw new UnsupportedOperationException("The method toDerivative of " + this.getClass().getSimpleName() + " does not support the one argument method (without margin price data).");
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgChecker.notNull(visitor, "visitor");
    return visitor.visitDeliverableSwapFuturesTransactionDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgChecker.notNull(visitor, "visitor");
    return visitor.visitDeliverableSwapFuturesTransactionDefinition(this);
  }

}

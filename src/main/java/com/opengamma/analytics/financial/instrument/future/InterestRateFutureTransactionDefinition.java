/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.future;

import java.time.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureTransaction;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Description of an interest rate future security.
 */
public class InterestRateFutureTransactionDefinition extends FuturesTransactionDefinition<InterestRateFutureSecurityDefinition>
    implements InstrumentDefinitionWithData<InstrumentDerivative, Double> {

  /**
   * Constructor.
   * @param underlying The underlying futures.
   * @param quantity The quantity/number of contract.
   * @param transactionDate The date at which the transaction was done.
   * @param transactionPrice The price at which the transaction was done.
   */
  public InterestRateFutureTransactionDefinition(final InterestRateFutureSecurityDefinition underlying, final long quantity, final ZonedDateTime transactionDate,
      final double transactionPrice) {
    super(underlying, quantity, transactionDate, transactionPrice);
  }

  public InterestRateFutureTransactionDefinition withNewNotionalAndTransactionPrice(final double notional, final double transactionPrice) {
    final InterestRateFutureSecurityDefinition sec = new InterestRateFutureSecurityDefinition(getUnderlyingSecurity().getLastTradingDate(),
        getUnderlyingSecurity().getIborIndex(), notional, getUnderlyingSecurity().getPaymentAccrualFactor(), getUnderlyingSecurity().getName(),
        getUnderlyingSecurity().getCalendar());
    return new InterestRateFutureTransactionDefinition(sec, getQuantity(), getTradeDate(), transactionPrice);
  }

  @Override
  public InstrumentDerivative toDerivative(final ZonedDateTime date) {
    throw new UnsupportedOperationException("The method toDerivative of " + this.getClass().getSimpleName() + " does not support the two argument method (without margin price data).");
  }

  /**
   * {@inheritDoc}
   * @param lastMarginPrice The price on which the last margining was done.
   */
  @Override
  public InterestRateFutureTransaction toDerivative(final ZonedDateTime dateTime, final Double lastMarginPrice) {
    final double referencePrice = referencePrice(dateTime, lastMarginPrice);
    final InterestRateFutureSecurity underlying = getUnderlyingSecurity().toDerivative(dateTime);
    final InterestRateFutureTransaction future = new InterestRateFutureTransaction(underlying, referencePrice, getQuantity());
    return future;
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgChecker.notNull(visitor, "visitor");
    return visitor.visitInterestRateFutureTransactionDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgChecker.notNull(visitor, "visitor");
    return visitor.visitInterestRateFutureTransactionDefinition(this);
  }

}

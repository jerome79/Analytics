/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.future;


import java.time.LocalDate;
import java.time.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.interestrate.future.derivative.FederalFundsFutureSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.FederalFundsFutureTransaction;
import com.opengamma.analytics.util.timeseries.DoubleTimeSeries;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Description of an transaction on a Federal Funds Futures.
 */
//CSOFF Check style seems to have a problem with >[]>
public class FederalFundsFutureTransactionDefinition extends FuturesTransactionDefinition<FederalFundsFutureSecurityDefinition>
    implements InstrumentDefinitionWithData<FederalFundsFutureTransaction, DoubleTimeSeries<ZonedDateTime>[]> {
  //CSON

  /**
   * Constructor.
   * @param underlyingFuture The underlying future security.
   * @param quantity The quantity of the transaction. Can be positive or negative.
   * @param tradeDate The transaction date.
   * @param tradePrice The transaction price. The price is in relative number and not in percent. This is the quoted price of the future.
   */
  public FederalFundsFutureTransactionDefinition(final FederalFundsFutureSecurityDefinition underlyingFuture, final long quantity, final ZonedDateTime tradeDate, final double tradePrice) {
    super(underlyingFuture, quantity, tradeDate, tradePrice);
  }

  @Override
  public FederalFundsFutureTransaction toDerivative(final ZonedDateTime date) {
    throw new UnsupportedOperationException("The method toDerivative of FederalFundsFutureTransactionDefinition does not support the two argument method (without ON fixing and margin price data).");
  }

  /**
   * {@inheritDoc}
   * @param dateTime The reference date and time.
   * @param data One or two time series. The first one is mandatory (not null) and contains with the ON index fixing.
   * The second one should be present if the reference "dateTime" is not hte trading date; the it contains the the future settlement (margining) prices.
   * The last closing price at a date strictly before "date" is used as last closing.
   * @return The derivative form
   */
  @Override
  public FederalFundsFutureTransaction toDerivative(final ZonedDateTime dateTime, final DoubleTimeSeries<ZonedDateTime>[] data) {
    ArgChecker.notNull(dateTime, "Date");
    ArgChecker.isTrue(data.length >= 1, "At least one time series: ON index fixing");
    final FederalFundsFutureSecurity underlying = getUnderlyingSecurity().toDerivative(dateTime, data[0]);
    final LocalDate dateLocal = dateTime.toLocalDate();
    final LocalDate transactionDateLocal = getTradeDate().toLocalDate();
    if (transactionDateLocal.equals(dateLocal)) { // Transaction is on valuation date.
      return new FederalFundsFutureTransaction(underlying, getQuantity(), getTradePrice());
    }
    ArgChecker.isTrue(data.length >= 2, "When not on the trading date, at least a second time series: futures settlement price.");
    ArgChecker.notNull(data[1], "future settlement price not null.");
    final DoubleTimeSeries<ZonedDateTime> pastClosing = data[1].subSeries(dateTime.minusMonths(1), dateTime);
    ArgChecker.isFalse(pastClosing.isEmpty(), "No closing price"); // There should be at least one recent margining.
    final double lastMargin = pastClosing.getLatestValue();
    return new FederalFundsFutureTransaction(underlying, getQuantity(), lastMargin);
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgChecker.notNull(visitor, "visitor");
    return visitor.visitFederalFundsFutureTransactionDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgChecker.notNull(visitor, "visitor");
    return visitor.visitFederalFundsFutureTransactionDefinition(this);
  }

}

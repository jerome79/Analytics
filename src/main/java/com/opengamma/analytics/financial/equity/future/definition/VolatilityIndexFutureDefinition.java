/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.future.definition;

import java.time.ZonedDateTime;

import com.opengamma.analytics.financial.equity.future.derivative.VolatilityIndexFuture;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.collect.ArgChecker;
import com.opengamma.strata.collect.id.StandardId;

/**
 * Volatility index future definition. An IndexFuture is always cash-settled.
 * eg UXH5 Index, http://cfe.cboe.com/products/Products_VIX.aspx
 */
public class VolatilityIndexFutureDefinition extends IndexFutureDefinition {

  /**
   * Constructor for Equity Index Futures, always cash-settled.
   *
   * @param expiryDate  the time and the day that a particular delivery month of a futures contract stops trading, as well as the final settlement price for that contract
   * @param settlementDate settlement date
   * @param strikePrice reference price
   * @param currency currency
   * @param unitAmount  size of a unit
   * @param underlying  identifier of the underlying
   */
  public VolatilityIndexFutureDefinition(final ZonedDateTime expiryDate, final ZonedDateTime settlementDate, final double strikePrice,
      final Currency currency, final double unitAmount, final StandardId underlying) {
    super(expiryDate, settlementDate, strikePrice, currency, unitAmount, underlying);
  }

  @Override
  public VolatilityIndexFuture toDerivative(final ZonedDateTime date) {
    ArgChecker.notNull(date, "date");
    final double timeToFixing = TimeCalculator.getTimeBetween(date, getExpiryDate());
    final double timeToDelivery = TimeCalculator.getTimeBetween(date, getSettlementDate());
    return new VolatilityIndexFuture(timeToFixing, timeToDelivery, getReferencePrice(), getCurrency(), getUnitAmount());
  }

  @Override
  public VolatilityIndexFuture toDerivative(final ZonedDateTime date, final Double referencePrice) {
    ArgChecker.notNull(date, "date");
    if (referencePrice == null) {
      return toDerivative(date);
    }
    final double timeToFixing = TimeCalculator.getTimeBetween(date, getExpiryDate());
    final double timeToDelivery = TimeCalculator.getTimeBetween(date, getSettlementDate());
    return new VolatilityIndexFuture(timeToFixing, timeToDelivery, referencePrice, getCurrency(), getUnitAmount());
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgChecker.notNull(visitor, "visitor");
    return visitor.visitVolatilityIndexFutureDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgChecker.notNull(visitor, "visitor");
    return visitor.visitVolatilityIndexFutureDefinition(this);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    return obj instanceof VolatilityIndexFutureDefinition && super.equals(obj);
  }
}

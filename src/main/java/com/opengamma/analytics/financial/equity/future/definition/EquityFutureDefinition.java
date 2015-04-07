/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.future.definition;

import java.time.ZonedDateTime;
import java.util.Objects;

import com.opengamma.analytics.financial.equity.future.derivative.EquityFuture;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.collect.ArgChecker;

/**
 * 
 */
public class EquityFutureDefinition implements InstrumentDefinitionWithData<EquityFuture, Double> {

  private final ZonedDateTime _expiryDate;
  private final ZonedDateTime _settlementDate;
  private final double _strikePrice;
  private final Currency _currency;
  private final double _unitAmount;

  /**
   * Basic setup for an Equity Future. TODO resolve conventions; complete param set
   * @param expiryDate The date-time at which the reference rate is fixed and the future is cash settled
   * @param settlementDate The date on which exchange is made, whether physical asset or cash equivalent
   * @param strikePrice The reference price at which the future will be settled
   * @param currency The reporting currency of the future
   * @param unitValue The currency value that the price of one contract will move by when the asset's price moves by one point
   */
  public EquityFutureDefinition(
      final ZonedDateTime expiryDate,
      final ZonedDateTime settlementDate,
      final double strikePrice,
      final Currency currency,
      final double unitValue) {
    ArgChecker.notNull(expiryDate, "expiry");
    ArgChecker.notNull(settlementDate, "settlement date");
    ArgChecker.notNull(currency, "currency");
    _expiryDate = expiryDate;
    _settlementDate = settlementDate;
    _strikePrice = strikePrice;
    _currency = currency;
    _unitAmount = unitValue;
  }

  /**
   * Gets the _expiryDate.
   * @return the _expiryDate
   */
  public ZonedDateTime getExpiryDate() {
    return _expiryDate;
  }

  /**
   * Gets the _settlementDate.
   * @return the _settlementDate
   */
  public ZonedDateTime getSettlementDate() {
    return _settlementDate;
  }

  /**
   * Gets the _strikePrice.
   * @return the _strikePrice
   */
  public double getStrikePrice() {
    return _strikePrice;
  }

  /**
   * Gets the _currency.
   * @return the _currency
   */
  public Currency getCurrency() {
    return _currency;
  }

  /**
   * Gets the _unitAmount. This represents the PNL of a single long contract if its price increases by 1.0. Also known as the 'Point Value'. 
   * @return the _unitAmount
   */
  public double getUnitAmount() {
    return _unitAmount;
  }

  @Override
  public EquityFuture toDerivative(final ZonedDateTime date) {
    ArgChecker.notNull(date, "date");
    final double timeToFixing = TimeCalculator.getTimeBetween(date, _expiryDate);
    final double timeToDelivery = TimeCalculator.getTimeBetween(date, _settlementDate);
    return new EquityFuture(timeToFixing, timeToDelivery, _strikePrice, _currency, _unitAmount);
  }

  @Override
  public EquityFuture toDerivative(final ZonedDateTime date, final Double referencePrice) {
    ArgChecker.notNull(date, "date");
    if (referencePrice == null) {
      return toDerivative(date);
    }
    final double timeToFixing = TimeCalculator.getTimeBetween(date, _expiryDate);
    final double timeToDelivery = TimeCalculator.getTimeBetween(date, _settlementDate);
    return new EquityFuture(timeToFixing, timeToDelivery, referencePrice, _currency, _unitAmount);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _currency.hashCode();
    result = prime * result + _expiryDate.hashCode();
    result = prime * result + _settlementDate.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_strikePrice);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_unitAmount);
    result = prime * result + (int) (temp ^ (temp >>> 32));
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
    final EquityFutureDefinition other = (EquityFutureDefinition) obj;
    if (Double.doubleToLongBits(_strikePrice) != Double.doubleToLongBits(other._strikePrice)) {
      return false;
    }
    if (!Objects.equals(_expiryDate, other._expiryDate)) {
      return false;
    }
    if (!Objects.equals(_settlementDate, other._settlementDate)) {
      return false;
    }
    if (!Objects.equals(_currency, other._currency)) {
      return false;
    }
    if (Double.doubleToLongBits(_unitAmount) != Double.doubleToLongBits(other._unitAmount)) {
      return false;
    }
    return true;
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgChecker.notNull(visitor, "visitor");
    return visitor.visitEquityFutureDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgChecker.notNull(visitor, "visitor");
    return visitor.visitEquityFutureDefinition(this);
  }

}

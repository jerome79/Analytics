/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity;

import java.time.ZonedDateTime;
import java.util.Objects;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.collect.ArgChecker;

/**
 *
 */
public class EquityDefinition implements InstrumentDefinition<Equity> {
  /** The entity */
  private final LegalEntity _entity;
  /** The currency */
  private final Currency _currency;
  /** The number of shares */
  private final double _numberOfShares;

  /**
   * @param entity The entity, not null
   * @param currency The currency, not null
   * @param numberOfShares The number of shares
   */
  public EquityDefinition(final LegalEntity entity, final Currency currency, final double numberOfShares) {
    ArgChecker.notNull(entity, "entity");
    ArgChecker.notNull(currency, "currency");
    _entity = entity;
    _currency = currency;
    _numberOfShares = numberOfShares;
  }

  /**
   * Gets the entity.
   * @return The entity
   */
  public LegalEntity getEntity() {
    return _entity;
  }

  /**
   * Gets the currency.
   * @return The currency
   */
  public Currency getCurrency() {
    return _currency;
  }

  /**
   * Gets the number of shares.
   * @return The number of shares.
   */
  public double getNumberOfShares() {
    return _numberOfShares;
  }

  @Override
  public Equity toDerivative(final ZonedDateTime date) {
    return new Equity(_entity, _currency, _numberOfShares);
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgChecker.notNull(visitor, "visitor");
    return visitor.visitEquityDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgChecker.notNull(visitor, "visitor");
    return visitor.visitEquityDefinition(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _currency.hashCode();
    result = prime * result + _entity.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_numberOfShares);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof EquityDefinition)) {
      return false;
    }
    final EquityDefinition other = (EquityDefinition) obj;
    if (Double.compare(_numberOfShares, other._numberOfShares) != 0) {
      return false;
    }
    if (!Objects.equals(_currency, other._currency)) {
      return false;
    }
    if (!Objects.equals(_entity, other._entity)) {
      return false;
    }
    return true;
  }

}

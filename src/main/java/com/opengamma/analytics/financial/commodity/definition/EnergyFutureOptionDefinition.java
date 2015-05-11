/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.definition;

import java.time.ZonedDateTime;

import com.opengamma.analytics.financial.ExerciseDecisionType;
import com.opengamma.analytics.financial.commodity.derivative.EnergyFutureOption;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Energy future option definition
 */
public class EnergyFutureOptionDefinition extends CommodityFutureOptionDefinition<EnergyFutureDefinition, EnergyFutureOption> {

  /**
   * Constructor for future options
   *
   * @param expiryDate is the time and the day that a particular delivery month of a futures contract stops trading, as well as the final settlement price for that contract.
   * @param underlying Underlying future
   * @param strike Strike price
   * @param exerciseType Exercise type - European or American
   * @param isCall Call if true, Put if false
   */
  public EnergyFutureOptionDefinition(final ZonedDateTime expiryDate, final EnergyFutureDefinition underlying, final double strike, final ExerciseDecisionType exerciseType,
      final boolean isCall) {
    super(expiryDate, underlying, strike, exerciseType, isCall);
  }

  @Override
  public EnergyFutureOption toDerivative(final ZonedDateTime date) {
    ArgChecker.inOrderOrEqual(date, this.getExpiryDate(), "date", "expiry date");
    final double timeToFixing = TimeCalculator.getTimeBetween(date, this.getExpiryDate());
    return new EnergyFutureOption(timeToFixing, getUnderlying().toDerivative(date), getStrike(), getExerciseType(), isCall());
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgChecker.notNull(visitor, "visitor");
    return visitor.visitEnergyFutureOptionDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgChecker.notNull(visitor, "visitor");
    return visitor.visitEnergyFutureOptionDefinition(this);
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
    if (!(obj instanceof EnergyFutureOptionDefinition)) {
      return false;
    }
    return super.equals(obj);
  }

}

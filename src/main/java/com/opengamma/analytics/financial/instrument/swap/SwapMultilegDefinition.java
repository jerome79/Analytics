/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.swap;

import java.time.ZonedDateTime;
import java.util.Arrays;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapMultileg;
import com.opengamma.analytics.util.timeseries.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Class describing a generic swap with multiple legs.
 */
public class SwapMultilegDefinition implements InstrumentDefinitionWithData<SwapMultileg, ZonedDateTimeDoubleTimeSeries[]> {

  /**
   * The swap legs.
   */
  private final AnnuityDefinition<? extends PaymentDefinition>[] _legs;

  public SwapMultilegDefinition(final AnnuityDefinition<? extends PaymentDefinition>[] legs) {
    ArgChecker.noNulls(legs, "legs");
    ArgChecker.isTrue(legs.length > 0, "SwapMultileg should have at least one leg");
    _legs = legs;
  }

  /**
   * Returns the legs.
   * @return The legs.
   */
  public AnnuityDefinition<? extends PaymentDefinition>[] getLegs() {
    return _legs;
  }

  @Override
  public SwapMultileg toDerivative(ZonedDateTime date) {
    ArgChecker.notNull(date, "date");
    final int nbLegs = _legs.length;
    @SuppressWarnings("unchecked")
    final Annuity<? extends Payment>[] legs = new Annuity[nbLegs];
    for (int loopleg = 0; loopleg < nbLegs; loopleg++) {
      legs[loopleg] = _legs[loopleg].toDerivative(date);
    }
    return new SwapMultileg(legs);
  }

  @Override
  public SwapMultileg toDerivative(ZonedDateTime date, ZonedDateTimeDoubleTimeSeries[] data) {
    ArgChecker.notNull(date, "date");
    ArgChecker.noNulls(data, "times series");
    final int nbLegs = _legs.length;
    ArgChecker.isTrue(data.length == nbLegs, "Data has not the same length as number of legs.");
    @SuppressWarnings("unchecked")
    final Annuity<? extends Payment>[] legs = new Annuity[nbLegs];
    for (int loopleg = 0; loopleg < nbLegs; loopleg++) {
      legs[loopleg] = _legs[loopleg].toDerivative(date, data[loopleg]);
    }
    return new SwapMultileg(legs);
  }

  @Override
  public <U, V> V accept(InstrumentDefinitionVisitor<U, V> visitor, U data) {
    ArgChecker.notNull(visitor, "visitor");
    return visitor.visitSwapMultilegDefinition(this, data);
  }

  @Override
  public <V> V accept(InstrumentDefinitionVisitor<?, V> visitor) {
    ArgChecker.notNull(visitor, "visitor");
    return visitor.visitSwapMultilegDefinition(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_legs);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    SwapMultilegDefinition other = (SwapMultilegDefinition) obj;
    if (!Arrays.equals(_legs, other._legs)) {
      return false;
    }
    return true;
  }

}

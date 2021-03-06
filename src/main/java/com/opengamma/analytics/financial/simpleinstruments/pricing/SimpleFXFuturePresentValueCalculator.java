/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.simpleinstruments.pricing;

import com.opengamma.analytics.financial.simpleinstruments.derivative.SimpleFXFuture;
import com.opengamma.analytics.financial.simpleinstruments.derivative.SimpleFuture;
import com.opengamma.analytics.financial.simpleinstruments.derivative.SimpleInstrument;
import com.opengamma.analytics.financial.simpleinstruments.derivative.SimpleInstrumentVisitor;
import com.opengamma.strata.basics.currency.CurrencyAmount;
import com.opengamma.strata.collect.ArgChecker;

/**
 * 
 */
public class SimpleFXFuturePresentValueCalculator implements SimpleInstrumentVisitor<SimpleFXFutureDataBundle, CurrencyAmount> {

  @Override
  public CurrencyAmount visit(final SimpleInstrument derivative, final SimpleFXFutureDataBundle data) {
    ArgChecker.notNull(derivative, "derivative");
    ArgChecker.notNull(data, "data");
    return derivative.accept(this, data);
  }

  @Override
  public CurrencyAmount visitSimpleFuture(final SimpleFuture future, final SimpleFXFutureDataBundle data) {
    throw new UnsupportedOperationException("Cannot price simple FX future with this calculator");
  }

  @Override
  public CurrencyAmount visitSimpleFXFuture(final SimpleFXFuture future, final SimpleFXFutureDataBundle data) {
    final double t = future.getExpiry();
    final double payRate = data.getPayCurve().getInterestRate(t);
    final double receiveRate = data.getReceiveCurve().getInterestRate(t);
    return CurrencyAmount.of(future.getReceiveCurrency(), future.getUnitAmount() * data.getSpot() * Math.exp(t * (receiveRate - payRate)));
  }

  @Override
  public CurrencyAmount visit(final SimpleInstrument derivative) {
    throw new UnsupportedOperationException("Cannot price simple future without data");
  }

  @Override
  public CurrencyAmount visitSimpleFuture(final SimpleFuture future) {
    throw new UnsupportedOperationException("Cannot price simple future without data");
  }

  @Override
  public CurrencyAmount visitSimpleFXFuture(final SimpleFXFuture future) {
    throw new UnsupportedOperationException("Cannot price simple future without data");
  }
}

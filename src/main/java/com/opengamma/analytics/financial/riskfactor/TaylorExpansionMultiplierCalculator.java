/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.riskfactor;

import java.util.Map;

import static org.apache.commons.math3.util.CombinatoricsUtils.factorial;

import com.opengamma.analytics.financial.greeks.MixedOrderUnderlying;
import com.opengamma.analytics.financial.greeks.NthOrderUnderlying;
import com.opengamma.analytics.financial.greeks.Underlying;
import com.opengamma.analytics.financial.pnl.UnderlyingType;
import com.opengamma.analytics.util.timeseries.DoubleTimeSeries;
import com.opengamma.strata.collect.ArgChecker;

/**
 * 
 */
public class TaylorExpansionMultiplierCalculator {

  public static double getMultiplier(final Underlying underlying) {
    ArgChecker.notNull(underlying, "underlying");
    if (underlying instanceof NthOrderUnderlying) {
      final NthOrderUnderlying nthOrder = (NthOrderUnderlying) underlying;
      final int n = nthOrder.getOrder();
      if (n == 0) {
        return 1;
      }
      return 1. / factorial(n);
    } else if (underlying instanceof MixedOrderUnderlying) {
      final MixedOrderUnderlying mixedOrder = (MixedOrderUnderlying) underlying;
      double result = 1;
      for (final NthOrderUnderlying underlyingOrder : mixedOrder.getUnderlyingOrders()) {
        result *= getMultiplier(underlyingOrder);
      }
      return result;
    }
    throw new IllegalArgumentException("Order was neither NthOrderUnderlying nor MixedOrderUnderlying: have " + underlying.getClass());
  }

  public static double getValue(final Map<UnderlyingType, Double> underlyingData, final Underlying underlying) {
    ArgChecker.notNull(underlying, "underlying");
    ArgChecker.notNull(underlyingData, "underlying data");
    ArgChecker.notEmpty(underlyingData, "underlying data");
    ArgChecker.noNulls(underlyingData.keySet(), "underlying data keys");
    ArgChecker.noNulls(underlyingData.values(), "underlying data values");
    if (underlying instanceof NthOrderUnderlying) {
      final NthOrderUnderlying nthOrder = (NthOrderUnderlying) underlying;
      final int n = nthOrder.getOrder();
      if (n == 0) {
        return 1;
      }
      final UnderlyingType type = nthOrder.getUnderlying();
      ArgChecker.isTrue(underlyingData.containsKey(type), "underlying data must contain key for type");
      final double value = Math.pow(underlyingData.get(type), n);
      return value * getMultiplier(underlying);
    } else if (underlying instanceof MixedOrderUnderlying) {
      final MixedOrderUnderlying mixedOrder = (MixedOrderUnderlying) underlying;
      Double result = null;
      double multiplier;
      for (final NthOrderUnderlying underlyingOrder : mixedOrder.getUnderlyingOrders()) {
        if (result == null) {
          result = getValue(underlyingData, underlyingOrder);
        } else {
          multiplier = getValue(underlyingData, underlyingOrder);
          result = result * multiplier;
        }
      }
      if (result != null) {
        return result;
      }
    }
    throw new IllegalArgumentException("Order was neither NthOrderUnderlying nor MixedOrderUnderlying: have " + underlying.getClass());
  }

  public static DoubleTimeSeries<?> getTimeSeries(final Map<UnderlyingType, DoubleTimeSeries<?>> underlyingData, final Underlying underlying) {
    ArgChecker.notNull(underlying, "underlying");
    ArgChecker.notNull(underlyingData, "underlying data");
    ArgChecker.notEmpty(underlyingData, "underlying data");
    ArgChecker.noNulls(underlyingData.keySet(), "underlying data keys");
    ArgChecker.noNulls(underlyingData.values(), "underlying data values");
    if (underlying instanceof NthOrderUnderlying) {
      final NthOrderUnderlying nthOrder = (NthOrderUnderlying) underlying;
      final int n = nthOrder.getOrder();
      if (n == 0) {
        throw new UnsupportedOperationException();
      }
      final UnderlyingType type = nthOrder.getUnderlying();
      ArgChecker.isTrue(underlyingData.containsKey(type), "underlying data must contain key for type");
      DoubleTimeSeries<?> ts = underlyingData.get(type);
      ts = ts.power(n);
      return ts.multiply(getMultiplier(underlying));
    } else if (underlying instanceof MixedOrderUnderlying) {
      final MixedOrderUnderlying mixedOrder = (MixedOrderUnderlying) underlying;
      DoubleTimeSeries<?> result = null;
      DoubleTimeSeries<?> multiplier = null;
      int size = 0;
      for (final NthOrderUnderlying underlyingOrder : mixedOrder.getUnderlyingOrders()) {
        if (result == null) {
          result = getTimeSeries(underlyingData, underlyingOrder);
          size = result.size();
        } else {
          multiplier = getTimeSeries(underlyingData, underlyingOrder);
          if (multiplier.size() != size) {
            throw new IllegalArgumentException("Time series in map were not the same length");
          }
          result = result.multiply(multiplier);
          if (result.size() != size) {
            throw new IllegalArgumentException("Time series in map did not contain the same times");
          }
        }
      }
      return result;
    }
    throw new IllegalArgumentException("Order was neither NthOrderUnderlying nor MixedOrderUnderlying: have " + underlying.getClass());
  }
}

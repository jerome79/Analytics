/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.discrete;

import com.opengamma.analytics.financial.model.volatility.surface.InterpolatedVolatilityTermStructureProvider;
import com.opengamma.analytics.math.function.InterpolatedVectorFunctionProvider;
import com.opengamma.analytics.math.function.VectorFunction;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.strata.collect.ArgChecker;
import com.opengamma.strata.collect.tuple.DoublesPair;

/**
 * This is the discrete equivalent of {@link InterpolatedVolatilityTermStructureProvider}. The volatility depends on
 * time-to-expiry only, and is described by an interpolated curve. 
 */
public class DiscreteVolatilityFunctionProviderFromInterpolatedTermStructure extends DiscreteVolatilityFunctionProvider {

  private final InterpolatedVectorFunctionProvider _funcPro;

  /**
   * set up the {@link DiscreteVolatilityFunctionProvider}
   * @param knotPoints position of knots 
   * @param interpolator The interpolator 
   */
  public DiscreteVolatilityFunctionProviderFromInterpolatedTermStructure(final double[] knotPoints, final Interpolator1D interpolator) {
    _funcPro = new InterpolatedVectorFunctionProvider(interpolator, knotPoints);

  }

  /**
   * {@inheritDoc}
   * <b>Note:</b> The strike values are ignored.
   */
  @Override
  public DiscreteVolatilityFunction from(final DoublesPair[] expiryStrikePoints) {
    ArgChecker.noNulls(expiryStrikePoints, "expiryStrikePoints");
    final int n = expiryStrikePoints.length;
    final Double[] expiries = new Double[n];
    for (int i = 0; i < n; i++) {
      expiries[i] = expiryStrikePoints[i].getFirst();
    }
    final VectorFunction vf = _funcPro.from(expiries);

    return new DiscreteVolatilityFunction() {

      @Override
      public int getLengthOfRange() {
        return vf.getLengthOfRange();
      }

      @Override
      public int getLengthOfDomain() {
        return vf.getLengthOfDomain();
      }

      @Override
      public DoubleMatrix2D calculateJacobian(final DoubleMatrix1D x) {
        return vf.calculateJacobian(x);
      }

      @Override
      public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
        return vf.evaluate(x);
      }
    };
  }

}

/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.surface;

import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.function.Function2D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;
import com.opengamma.analytics.math.surface.FunctionalSurface;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.strata.collect.ArgChecker;
import com.opengamma.strata.collect.tuple.Pair;

/**
 *Produces a volatility surface that is backed by a single interpolated curve in the expiry dimension, i.e. there is no 
 *strike variation 
 */
public class InterpolatedVolatilityTermStructureProvider implements VolatilitySurfaceProvider {

  private final double[] _knots;
  private final Interpolator1D _interpolator;

  public InterpolatedVolatilityTermStructureProvider(final double[] knotPoints, final Interpolator1D interpolator) {
    ArgChecker.notEmpty(knotPoints, "null or empty knotPoints");
    ArgChecker.notNull(interpolator, "null interpolator");
    final int n = knotPoints.length;

    for (int i = 1; i < n; i++) {
      ArgChecker.isTrue(knotPoints[i] > knotPoints[i - 1], "knot points must be strictly ascending");
    }
    _knots = knotPoints.clone();
    _interpolator = interpolator;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public VolatilitySurface getVolSurface(final DoubleMatrix1D modelParameters) {
    ArgChecker.notNull(modelParameters, "modelParameters");
    //InterpolatedDoublesCurve checks length of modelParameters, so is not repeated here 
    final InterpolatedDoublesCurve curve = new InterpolatedDoublesCurve(_knots, modelParameters.getData(), _interpolator, true);

    final Function<Double, Double> function = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... tk) {
        return curve.getYValue(tk[0]);
      }
    };

    final FunctionalDoublesSurface surface = new FunctionalDoublesSurface(function);
    return new VolatilitySurface(surface);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Surface<Double, Double, DoubleMatrix1D> getParameterSensitivitySurface(final DoubleMatrix1D modelParameters) {
    ArgChecker.notNull(modelParameters, "modelParameters");
    //InterpolatedDoublesCurve checks length of modelParameters, so is not repeated here 
    final InterpolatedDoublesCurve curve = new InterpolatedDoublesCurve(_knots, modelParameters.getData(), _interpolator, true);
    final Function2D<Double, DoubleMatrix1D> func = new Function2D<Double, DoubleMatrix1D>() {
      @Override
      public DoubleMatrix1D evaluate(final Double t, final Double k) {
        final Double[] sense = curve.getYValueParameterSensitivity(t);
        return new DoubleMatrix1D(sense);
      }
    };

    return new FunctionalSurface<>(func);
  }

  @Override
  public Surface<Double, Double, Pair<Double, DoubleMatrix1D>> getVolAndParameterSensitivitySurface(final DoubleMatrix1D modelParameters) {
    ArgChecker.notNull(modelParameters, "modelParameters");
    //InterpolatedDoublesCurve checks length of modelParameters, so is not repeated here 
    final InterpolatedDoublesCurve curve = new InterpolatedDoublesCurve(_knots, modelParameters.getData(), _interpolator, true);
    final Function2D<Double, Pair<Double, DoubleMatrix1D>> func = new Function2D<Double, Pair<Double, DoubleMatrix1D>>() {
      @Override
      public Pair<Double, DoubleMatrix1D> evaluate(final Double t, final Double k) {
        final Double vol = curve.getYValue(t);
        final DoubleMatrix1D sense = new DoubleMatrix1D(curve.getYValueParameterSensitivity(t));
        return Pair.of(vol, sense);
      }
    };

    return new FunctionalSurface<>(func);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getNumModelParameters() {
    return _knots.length;
  }

}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.surface;

import com.opengamma.analytics.math.function.Function;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Shifts a {@link FunctionalDoublesSurface}. Only parallel shifts of the surface are supported.
 */
public class FunctionalSurfaceAdditiveShiftFunction implements SurfaceShiftFunction<FunctionalDoublesSurface> {

  /**
   * {@inheritDoc}
   */
  @Override
  public FunctionalDoublesSurface evaluate(final FunctionalDoublesSurface surface, final double shift) {
    ArgChecker.notNull(surface, "surface");
    return evaluate(surface, shift, "PARALLEL_SHIFT_" + surface.getName());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public FunctionalDoublesSurface evaluate(final FunctionalDoublesSurface surface, final double shift, final String newName) {
    ArgChecker.notNull(surface, "surface");
    final Function<Double, Double> f = surface.getFunction();
    final Function<Double, Double> shiftedFunction = new ShiftedFunction(shift, f);
    return FunctionalDoublesSurface.from(shiftedFunction, newName);
  }

  private static class ShiftedFunction implements Function<Double, Double> {

    private final double _shift;
    private final Function<Double, Double> _f;

    public ShiftedFunction(double shift, Function<Double, Double> f) {
      this._shift = shift;
      this._f = f;
    }

    @Override
    public Double evaluate(final Double... xy) {
      return _f.evaluate(xy) + _shift;
    }

  }

  /**
   * {@inheritDoc}
   * @return Not supported
   * @throws UnsupportedOperationException always
   */
  @Override
  public FunctionalDoublesSurface evaluate(final FunctionalDoublesSurface surface, final double x, final double y, final double shift) {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   * @return Not supported
   * @throws UnsupportedOperationException always
   */
  @Override
  public FunctionalDoublesSurface evaluate(final FunctionalDoublesSurface surface, final double x, final double y, final double shift, final String newName) {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   * @return Not supported
   * @throws UnsupportedOperationException always
   */
  @Override
  public FunctionalDoublesSurface evaluate(final FunctionalDoublesSurface surface, final double[] xShift, final double[] yShift, final double[] shift) {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   * @return Not supported
   * @throws UnsupportedOperationException always
   */
  @Override
  public FunctionalDoublesSurface evaluate(final FunctionalDoublesSurface surface, final double[] xShift, final double[] yShift, final double[] shift, final String newName) {
    throw new UnsupportedOperationException();
  }

}

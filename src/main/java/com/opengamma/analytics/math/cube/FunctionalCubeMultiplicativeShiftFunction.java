/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.cube;

import com.opengamma.analytics.math.function.Function;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Shifts a {@link FunctionalDoublesCube}. Only parallel shifts of the cube are supported.
 */
public class FunctionalCubeMultiplicativeShiftFunction implements CubeShiftFunction<FunctionalDoublesCube> {

  /**
   * {@inheritDoc}
   */
  @Override
  public FunctionalDoublesCube evaluate(final FunctionalDoublesCube cube, final double percentage) {
    ArgChecker.notNull(cube, "cube");
    return evaluate(cube, percentage, "CONSTANT_MULTIPLIER_" + cube.getName());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public FunctionalDoublesCube evaluate(final FunctionalDoublesCube cube, final double percentage, final String newName) {
    ArgChecker.notNull(cube, "cube");
    final Function<Double, Double> f = cube.getFunction();
    final Function<Double, Double> shiftedFunction = new Function<Double, Double>() {

      @Override
      public Double evaluate(final Double... xy) {
        return f.evaluate(xy) * (1 + percentage);
      }

    };
    return FunctionalDoublesCube.from(shiftedFunction, newName);
  }

  /**
   * {@inheritDoc}
   * @return Not supported
   * @throws UnsupportedOperationException always
   */
  @Override
  public FunctionalDoublesCube evaluate(final FunctionalDoublesCube cube, final double x, final double y, final double z, final double percentage) {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   * @return Not supported
   * @throws UnsupportedOperationException always
   */
  @Override
  public FunctionalDoublesCube evaluate(final FunctionalDoublesCube cube, final double x, final double y, final double z, final double percentage, final String newName) {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   * @return Not supported
   * @throws UnsupportedOperationException always
   */
  @Override
  public FunctionalDoublesCube evaluate(final FunctionalDoublesCube cube, final double[] xShift, final double[] yShift, final double[] zShift, final double[] percentage) {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   * @return Not supported
   * @throws UnsupportedOperationException always
   */
  @Override
  public FunctionalDoublesCube evaluate(final FunctionalDoublesCube cube, final double[] xShift, final double[] yShift, final double[] zShift, final double[] percentage, final String newName) {
    throw new UnsupportedOperationException();
  }

}

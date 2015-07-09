/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.curve;

import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Shifts a {@link FunctionalDoublesCurve}. Only parallel shifts of the curve are supported.
 */
public class FunctionalCurveShiftFunction implements CurveShiftFunction<FunctionalDoublesCurve> {

  /**
   * {@inheritDoc}
   */
  @Override
  public FunctionalDoublesCurve evaluate(final FunctionalDoublesCurve curve, final double shift) {
    ArgChecker.notNull(curve, "curve");
    return evaluate(curve, shift, "PARALLEL_SHIFT_" + curve.getName());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public FunctionalDoublesCurve evaluate(final FunctionalDoublesCurve curve, final double shift, final String newName) {
    ArgChecker.notNull(curve, "curve");
    final Function<Double, Double> f = curve.getFunction();
    final Function1D<Double, Double> shiftedFunction = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double x) {
        return f.evaluate(x) + shift;
      }

    };
    return FunctionalDoublesCurve.from(shiftedFunction, curve.getFirstDerivativeFunction(), newName);
  }

  /**
   * {@inheritDoc}
   * @return Not supported
   * @throws UnsupportedOperationException always
   */
  @Override
  public FunctionalDoublesCurve evaluate(final FunctionalDoublesCurve curve, final double x, final double shift) {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   * @return Not supported
   * @throws UnsupportedOperationException always
   */
  @Override
  public FunctionalDoublesCurve evaluate(final FunctionalDoublesCurve curve, final double x, final double shift, final String newName) {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   * @return Not supported
   * @throws UnsupportedOperationException always
   */
  @Override
  public FunctionalDoublesCurve evaluate(final FunctionalDoublesCurve curve, final double[] xShift, final double[] yShift) {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   * @return Not supported
   * @throws UnsupportedOperationException always
   */
  @Override
  public FunctionalDoublesCurve evaluate(final FunctionalDoublesCurve curve, final double[] xShift, final double[] yShift, final String newName) {
    throw new UnsupportedOperationException();
  }

}

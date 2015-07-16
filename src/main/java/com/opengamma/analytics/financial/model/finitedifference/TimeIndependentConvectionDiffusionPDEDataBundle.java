/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Describes a partial differential for a function $V(t,x)$, with the initial
 * $\frac{\partial V}{\partial t} + a(x) \frac{\partial^2 V}{\partial x^2} + b(x) \frac{\partial V}{\partial x} + c(x)V = 0$
 * Note that $a$, $b$ and $c$ are functions of $x$ only so the matrix system of the PDE
 * solver need only be solved once (provided that the boundary conditions are
 * only time independent) 
 */
public class TimeIndependentConvectionDiffusionPDEDataBundle {

  private final DoublesCurve _a;
  private final DoublesCurve _b;
  private final DoublesCurve _c;
  private final Function1D<Double, Double> _initialCondition;

  public TimeIndependentConvectionDiffusionPDEDataBundle(
      DoublesCurve a,
      DoublesCurve b,
      DoublesCurve c,
      Function1D<Double, Double> initialCondition) {
    ArgChecker.notNull(a, "null a");
    ArgChecker.notNull(b, "null b");
    ArgChecker.notNull(c, "null c");
    ArgChecker.notNull(initialCondition, "null initial Condition");
    _a = a;
    _b = b;
    _c = c;
    _initialCondition = initialCondition;
  }

  public double getA(double x) {
    return _a.getYValue(x);
  }

  public double getB(double x) {
    return _b.getYValue(x);
  }

  public double getC(double x) {
    return _c.getYValue(x);
  }

  public double getInitialValue(double x) {
    return _initialCondition.evaluate(x);
  }

}

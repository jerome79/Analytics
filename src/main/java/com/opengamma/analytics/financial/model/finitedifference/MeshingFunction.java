/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.strata.collect.ArgChecker;

/**
 *
 */
public abstract class MeshingFunction extends Function1D<Integer, Double> {

  private final int _nPoints;

  protected MeshingFunction(final int nPoints) {
    ArgChecker.isTrue(nPoints > 1, "Need more than 1 point for a mesh");
    _nPoints = nPoints;
  }

  public int getNumberOfPoints() {
    return _nPoints;
  }

  public double[] getPoints() {
    final double[] res = new double[_nPoints];
    for (int i = 0; i < _nPoints; i++) {
      res[i] = evaluate(i);
    }
    return res;
  }

}

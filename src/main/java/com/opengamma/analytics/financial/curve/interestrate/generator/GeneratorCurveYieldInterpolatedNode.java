/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.curve.interestrate.generator;

import java.util.Arrays;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Store the details and generate the required curve. The curve is interpolated on the rate (continuously compounded).
 */
@SuppressWarnings("deprecation")
public class GeneratorCurveYieldInterpolatedNode extends GeneratorYDCurve {

  /**
   * The nodes (times) on which the interpolated curves is constructed.
   */
  private final double[] _nodePoints;
  /**
   * The interpolator used for the curve.
   */
  private final Interpolator1D _interpolator;
  /**
   * The number of points (or nodes). Is the length of _nodePoints.
   */
  private final int _nbPoints;

  /**
   * Constructor.
   * @param nodePoints The node points (X) used to define the interpolated curve.
   * @param interpolator The interpolator.
   */
  public GeneratorCurveYieldInterpolatedNode(final double[] nodePoints, final Interpolator1D interpolator) {
    ArgChecker.notNull(nodePoints, "Node points");
    ArgChecker.notNull(interpolator, "Interpolator");
    _nodePoints = nodePoints;
    // Check that node points are sorted
    final double[] nodePointsSorted = nodePoints.clone();
    Arrays.sort(nodePointsSorted);
    ArgChecker.isTrue(Arrays.equals(nodePoints, nodePointsSorted), "Node points not sorted");
    _nbPoints = _nodePoints.length;
    _interpolator = interpolator;
  }

  @Override
  public int getNumberOfParameter() {
    return _nbPoints;
  }

  @Override
  public YieldAndDiscountCurve generateCurve(final String name, final double[] x) {
    ArgChecker.isTrue(x.length == _nbPoints, "Incorrect dimension for the rates");
    return new YieldCurve(name, new InterpolatedDoublesCurve(_nodePoints, x, _interpolator, true, name));
  }

  @Override
  public YieldAndDiscountCurve generateCurve(final String name, final MulticurveProviderInterface multicurve, final double[] parameters) {
    return generateCurve(name, parameters);
  }

}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.example.curveconstruction;

// @export "imports"
import java.io.PrintStream;

import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.curve.Curve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.analytics.math.interpolation.Extrapolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.LinearExtrapolator1D;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;

/**
 * Example for curves.
 */
public class CurveExample {

  public static void constantDoublesCurveDemo(PrintStream out) {
    Curve<Double, Double> curve = new ConstantDoublesCurve(5.0);

    out.println(curve.getYValue(0.0));
    out.println(curve.getYValue(10.0));
    out.println(curve.getYValue(-10.0));
  }

  public static void interpolatedDoublesCurveDemo(PrintStream out) {
    double[] xdata = {1.0, 2.0, 3.0 };
    double[] ydata = {2.0, 4.0, 6.0 };
    LinearInterpolator1D interpolator = new LinearInterpolator1D();
    Curve<Double, Double> curve = new InterpolatedDoublesCurve(xdata, ydata, interpolator, true);

    out.println(curve.getYValue(1.0));
    out.println(curve.getYValue(2.0));
    out.println(curve.getYValue(3.0));

    out.println(curve.getYValue(1.5));
    try {
      out.println("Trying to get y value for too large an x...");
      curve.getYValue(4.0);
    } catch (IllegalArgumentException e) {
      out.println("IllegalArgumentException called");
    }
  }

  public static void interpolatorExtrapolatorDoublesCurveDemo(PrintStream out) {
    double[] xdata = {1.0, 2.0, 3.0 };
    double[] ydata = {2.0, 4.0, 6.0 };

    Interpolator1D interpolator = new LinearInterpolator1D();
    Extrapolator1D leftExtrapolator = new LinearExtrapolator1D();
    Extrapolator1D rightExtrapolator = new LinearExtrapolator1D();
    Interpolator1D combined = new CombinedInterpolatorExtrapolator(interpolator, leftExtrapolator, rightExtrapolator);

    Curve<Double, Double> curve = new InterpolatedDoublesCurve(xdata, ydata, combined, true);

    out.println(curve.getYValue(1.0));
    out.println(curve.getYValue(2.0));
    out.println(curve.getYValue(3.0));

    out.println(curve.getYValue(1.5));
    out.println(curve.getYValue(4.0));
  }

}

/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.function.PiecewisePolynomialFunction1D;

/**
 * Test.
 */
@Test
public class MonotonicityPreservingQuinticSplineInterpolatorTest {

  private static final double INF = 1. / 0.;

  /**
   * 
   */
  public void localMonotonicityIncTest() {
    final double[] xValues = new double[] {2., 3., 5., 8., 9., 13. };
    final double[] yValues = new double[] {1., 1.01, 2., 2.1, 2.2, 2.201 };

    PiecewisePolynomialInterpolator interp = new NaturalSplineInterpolator();
    PiecewisePolynomialResult result = interp.interpolate(xValues, yValues);

    PiecewisePolynomialFunction1D function = new PiecewisePolynomialFunction1D();

    PiecewisePolynomialInterpolator interpPos = new MonotonicityPreservingQuinticSplineInterpolator(interp);
    PiecewisePolynomialResult resultPos = interpPos.interpolate(xValues, yValues);

    assertEquals(resultPos.getDimensions(), result.getDimensions());
    assertEquals(resultPos.getNumberOfIntervals(), result.getNumberOfIntervals());
    assertEquals(resultPos.getOrder(), 6);

    final int nKeys = 111;
    double key0 = 2.;
    for (int i = 1; i < nKeys; ++i) {
      final double key = 2. + 11. / (nKeys - 1) * i;
      assertTrue(function.evaluate(resultPos, key).getData()[0] - function.evaluate(resultPos, key0).getData()[0] >= 0.);

      key0 = 2. + 11. / (nKeys - 1) * i;
    }
  }

  /**
   * 
   */
  public void localMonotonicityClampedTest() {
    final double[] xValues = new double[] {-2., 3., 4., 8., 9.1, 10. };
    final double[] yValues = new double[] {0., 10., 9.5, 2., 1.1, -2.2, -2.6, 0. };

    PiecewisePolynomialInterpolator interp = new CubicSplineInterpolator();
    PiecewisePolynomialResult result = interp.interpolate(xValues, yValues);

    PiecewisePolynomialFunction1D function = new PiecewisePolynomialFunction1D();

    PiecewisePolynomialInterpolator interpPos = new MonotonicityPreservingQuinticSplineInterpolator(interp);
    PiecewisePolynomialResult resultPos = interpPos.interpolate(xValues, yValues);

    assertEquals(resultPos.getDimensions(), result.getDimensions());
    assertEquals(resultPos.getNumberOfIntervals(), result.getNumberOfIntervals());
    assertEquals(resultPos.getOrder(), 6);

    final int nKeys = 121;
    double key0 = -2.;
    for (int i = 1; i < nKeys; ++i) {
      final double key = -2. + 12. / (nKeys - 1) * i;
      assertTrue(function.evaluate(resultPos, key).getData()[0] - function.evaluate(resultPos, key0).getData()[0] <= 0.);

      key0 = -2. + 11. / (nKeys - 1) * i;
    }
  }

  /**
   * 
   */
  public void localMonotonicityClampedMultiTest() {
    final double[] xValues = new double[] {-2., 3., 4., 8., 9.1, 10. };
    final double[][] yValues = new double[][] { {0., 10., 9.5, 2., 1.1, -2.2, -2.6, 0. }, {10., 10., 9.5, 2., 1.1, -2.2, -2.6, 10. } };

    PiecewisePolynomialInterpolator interp = new CubicSplineInterpolator();
    PiecewisePolynomialResult result = interp.interpolate(xValues, yValues);

    PiecewisePolynomialFunction1D function = new PiecewisePolynomialFunction1D();

    PiecewisePolynomialInterpolator interpPos = new MonotonicityPreservingQuinticSplineInterpolator(interp);
    PiecewisePolynomialResult resultPos = interpPos.interpolate(xValues, yValues);

    assertEquals(resultPos.getDimensions(), result.getDimensions());
    assertEquals(resultPos.getNumberOfIntervals(), result.getNumberOfIntervals());
    assertEquals(resultPos.getOrder(), 6);

    final int nKeys = 62;
    double key0 = 3.;
    for (int i = 1; i < nKeys; ++i) {
      final double key = 3. + 6.1 / (nKeys - 1) * i;
      assertTrue(function.evaluate(resultPos, key).getData()[0] - function.evaluate(resultPos, key0).getData()[0] <= 0.);

      key0 = 3. + 6.1 / (nKeys - 1) * i;
    }
    key0 = 3.;
    for (int i = 1; i < nKeys; ++i) {
      final double key = 3. + 6.1 / (nKeys - 1) * i;

      assertTrue(function.evaluate(resultPos, key).getData()[1] - function.evaluate(resultPos, key0).getData()[1] <= 0.);

      key0 = 3. + 6.1 / (nKeys - 1) * i;
    }
  }

  /**
   * 
   */
  public void localMonotonicityDecTest() {
    final double[] xValues = new double[] {-2., 3., 4., 8., 9.1, 10. };
    final double[] yValues = new double[] {10., 9.5, 2., 1.1, -2.2, -2.6 };

    PiecewisePolynomialInterpolator interp = new CubicSplineInterpolator();
    PiecewisePolynomialResult result = interp.interpolate(xValues, yValues);

    PiecewisePolynomialFunction1D function = new PiecewisePolynomialFunction1D();

    PiecewisePolynomialInterpolator interpPos = new MonotonicityPreservingQuinticSplineInterpolator(interp);
    PiecewisePolynomialResult resultPos = interpPos.interpolate(xValues, yValues);

    assertEquals(resultPos.getDimensions(), result.getDimensions());
    assertEquals(resultPos.getNumberOfIntervals(), result.getNumberOfIntervals());
    assertEquals(resultPos.getOrder(), 6);

    final int nKeys = 71;
    double key0 = 3.;
    for (int i = 1; i < nKeys; ++i) {
      final double key = 3. + 7. / (nKeys - 1) * i;
      assertTrue(function.evaluate(resultPos, key).getData()[0] - function.evaluate(resultPos, key0).getData()[0] <= 0.);

      key0 = 3. + 7. / (nKeys - 1) * i;
    }
  }

  /**
   * 
   */
  public void extremumTest() {
    final double[] xValues = new double[] {1., 2., 3., 4., 5., 6., 7., 8 };
    final double[][] yValues = new double[][] { {1., 1., 2., 4., 4., 2., 1., 1. }, {10., 10., 6., 4., 4., 6., 10., 10. } };

    PiecewisePolynomialInterpolator interp = new CubicSplineInterpolator();
    PiecewisePolynomialResult result = interp.interpolate(xValues, yValues);

    PiecewisePolynomialFunction1D function = new PiecewisePolynomialFunction1D();

    PiecewisePolynomialInterpolator interpPos = new MonotonicityPreservingQuinticSplineInterpolator(interp);
    PiecewisePolynomialResult resultPos = interpPos.interpolate(xValues, yValues);

    assertEquals(resultPos.getDimensions(), result.getDimensions());
    assertEquals(resultPos.getNumberOfIntervals(), result.getNumberOfIntervals());
    assertEquals(resultPos.getOrder(), 6);

    final int nKeys = 41;
    double key0 = 1.;
    for (int i = 1; i < nKeys; ++i) {
      final double key = 1. + 3. / (nKeys - 1) * i;
      assertTrue(function.evaluate(resultPos, key).getData()[0] - function.evaluate(resultPos, key0).getData()[0] >= 0.);

      key0 = 1. + 3. / (nKeys - 1) * i;
    }
    key0 = 1.;
    for (int i = 1; i < nKeys; ++i) {
      final double key = 1. + 3. / (nKeys - 1) * i;
      assertTrue(function.evaluate(resultPos, key).getData()[1] - function.evaluate(resultPos, key0).getData()[1] <= 0.);

      key0 = 1. + 3. / (nKeys - 1) * i;
    }
    key0 = 5.;
    for (int i = 1; i < nKeys; ++i) {
      final double key = 5. + 3. / (nKeys - 1) * i;
      assertTrue(function.evaluate(resultPos, key).getData()[0] - function.evaluate(resultPos, key0).getData()[0] <= 0.);

      key0 = 5. + 3. / (nKeys - 1) * i;
    }
    key0 = 5.;
    for (int i = 1; i < nKeys; ++i) {
      final double key = 5. + 3. / (nKeys - 1) * i;
      assertTrue(function.evaluate(resultPos, key).getData()[1] - function.evaluate(resultPos, key0).getData()[1] >= 0.);

      key0 = 5. + 3. / (nKeys - 1) * i;
    }
  }

  /**
   * 
   */
  public void intervalModifiedTest() {
    final double[] xValues = new double[] {1., 2., 3., 4., 5., 6., 7. };
    final double[] yValues = new double[] {19., 17., 19., 2., 4., 5., 18. };

    PiecewisePolynomialInterpolator interp = new CubicSplineInterpolator();
    PiecewisePolynomialResult result = interp.interpolate(xValues, yValues);

    PiecewisePolynomialFunction1D function = new PiecewisePolynomialFunction1D();

    PiecewisePolynomialInterpolator interpPos = new MonotonicityPreservingQuinticSplineInterpolator(interp);
    PiecewisePolynomialResult resultPos = interpPos.interpolate(xValues, yValues);

    assertEquals(resultPos.getDimensions(), result.getDimensions());
    assertEquals(resultPos.getNumberOfIntervals(), result.getNumberOfIntervals());
    assertEquals(resultPos.getOrder(), 6);

    final int len = 21;
    double key0 = 5.;
    for (int i = 1; i < len; ++i) {
      final double key = 5. + 1. / (len - 1) * i;
      assertTrue(function.evaluate(resultPos, key).getData()[0] - function.evaluate(resultPos, key0).getData()[0] >= 0.);
      key0 = 5. + 1. / (len - 1) * i;
    }
  }

  /**
   * 
   */
  public void intervalModifiedMultiTest() {
    final double[] xValues = new double[] {1., 2., 3., 4., 5., 6., 7. };
    final double[][] yValues = new double[][] { {19., 17., 19., 2., 4., 5., 18. }, {19.0, 15.0, 16.0, 6.0, 12.0, 16.0, 8.0 } };

    PiecewisePolynomialInterpolator interp = new CubicSplineInterpolator();
    PiecewisePolynomialResult result = interp.interpolate(xValues, yValues);

    PiecewisePolynomialFunction1D function = new PiecewisePolynomialFunction1D();

    PiecewisePolynomialInterpolator interpPos = new MonotonicityPreservingQuinticSplineInterpolator(interp);
    PiecewisePolynomialResult resultPos = interpPos.interpolate(xValues, yValues);

    assertEquals(resultPos.getDimensions(), result.getDimensions());
    assertEquals(resultPos.getNumberOfIntervals(), result.getNumberOfIntervals());
    assertEquals(resultPos.getOrder(), 6);

    final int len = 21;
    double key0 = 5.;
    for (int i = 1; i < len; ++i) {
      final double key = 5. + 1. / (len - 1) * i;
      assertTrue(function.evaluate(resultPos, key).getData()[0] - function.evaluate(resultPos, key0).getData()[0] >= 0.);
      key0 = 5. + 1. / (len - 1) * i;
    }
    key0 = 5.;
    for (int i = 1; i < len; ++i) {
      final double key = 5. + 1. / (len - 1) * i;
      assertTrue(function.evaluate(resultPos, key).getData()[1] - function.evaluate(resultPos, key0).getData()[1] >= 0.);
      key0 = 5. + 1. / (len - 1) * i;
    }

  }

  /*
   * Error tests
   */
  /**
   * Primary interpolation method should have second derivative. 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void lowDegreeTest() {
    final double[] xValues = new double[] {1., 2., 3. };
    final double[] yValues = new double[] {0., 0.1, 0.05 };

    PiecewisePolynomialInterpolator interp = new LinearInterpolator();
    PiecewisePolynomialInterpolator interpPos = new MonotonicityPreservingQuinticSplineInterpolator(interp);
    interpPos.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void lowDegreeMultiTest() {
    final double[] xValues = new double[] {1., 2., 3. };
    final double[][] yValues = new double[][] { {0., 0.1, 0.05 }, {0., 0.1, 1.05 } };

    PiecewisePolynomialInterpolator interp = new LinearInterpolator();
    PiecewisePolynomialInterpolator interpPos = new MonotonicityPreservingQuinticSplineInterpolator(interp);
    interpPos.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void dataShortTest() {
    final double[] xValues = new double[] {1., 2. };
    final double[] yValues = new double[] {0., 0.1 };

    PiecewisePolynomialInterpolator interp = new CubicSplineInterpolator();
    PiecewisePolynomialInterpolator interpPos = new MonotonicityPreservingQuinticSplineInterpolator(interp);
    interpPos.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void dataShortMultiTest() {
    final double[] xValues = new double[] {1., 2., };
    final double[][] yValues = new double[][] { {0., 0.1 }, {0., 0.1 } };

    PiecewisePolynomialInterpolator interp = new PiecewiseCubicHermiteSplineInterpolator();
    PiecewisePolynomialInterpolator interpPos = new MonotonicityPreservingQuinticSplineInterpolator(interp);
    interpPos.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void dataDiffTest() {
    final double[] xValues = new double[] {1., 2., 3., 4. };
    final double[] yValues = new double[] {0., 0.1, 3. };

    PiecewisePolynomialInterpolator interp = new NaturalSplineInterpolator();
    PiecewisePolynomialInterpolator interpPos = new MonotonicityPreservingQuinticSplineInterpolator(interp);
    interpPos.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void dataDiffMultiTest() {
    final double[] xValues = new double[] {1., 2., 3., 4. };
    final double[][] yValues = new double[][] { {0., 0.1, 3. }, {0., 0.1, 3. } };

    PiecewisePolynomialInterpolator interp = new PiecewiseCubicHermiteSplineInterpolator();
    PiecewisePolynomialInterpolator interpPos = new MonotonicityPreservingQuinticSplineInterpolator(interp);
    interpPos.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void coincideDataTest() {
    final double[] xValues = new double[] {1., 1., 3. };
    final double[] yValues = new double[] {0., 0.1, 0.05 };

    PiecewisePolynomialInterpolator interp = new CubicSplineInterpolator();
    PiecewisePolynomialInterpolator interpPos = new MonotonicityPreservingQuinticSplineInterpolator(interp);
    interpPos.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void coincideDataMultiTest() {
    final double[] xValues = new double[] {1., 2., 2. };
    final double[][] yValues = new double[][] { {2., 0., 0.1, 0.05, 2. }, {1., 0., 0.1, 1.05, 2. } };

    PiecewisePolynomialInterpolator interp = new CubicSplineInterpolator();
    PiecewisePolynomialInterpolator interpPos = new MonotonicityPreservingQuinticSplineInterpolator(interp);
    interpPos.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullXdataTest() {
    double[] xValues = new double[] {1., 2., 3., 4. };
    double[] yValues = new double[] {0., 0.1, 0.05, 0.2 };
    xValues = null;

    PiecewisePolynomialInterpolator interp = new CubicSplineInterpolator();
    PiecewisePolynomialInterpolator interpPos = new MonotonicityPreservingQuinticSplineInterpolator(interp);
    interpPos.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullYdataTest() {
    double[] xValues = new double[] {1., 2., 3., 4. };
    double[] yValues = new double[] {0., 0.1, 0.05, 0.2 };
    yValues = null;

    PiecewisePolynomialInterpolator interp = new CubicSplineInterpolator();
    PiecewisePolynomialInterpolator interpPos = new MonotonicityPreservingQuinticSplineInterpolator(interp);
    interpPos.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullXdataMultiTest() {
    double[] xValues = new double[] {1., 2., 3., 4. };
    double[][] yValues = new double[][] { {0., 0.1, 0.05, 0.2 }, {0., 0.1, 0.05, 0.2 } };
    xValues = null;

    PiecewisePolynomialInterpolator interp = new CubicSplineInterpolator();
    PiecewisePolynomialInterpolator interpPos = new MonotonicityPreservingQuinticSplineInterpolator(interp);
    interpPos.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullYdataMultiTest() {
    double[] xValues = new double[] {1., 2., 3., 4. };
    double[][] yValues = new double[][] { {0., 0.1, 0.05, 0.2 }, {0., 0.1, 0.05, 0.2 } };
    yValues = null;

    PiecewisePolynomialInterpolator interp = new CubicSplineInterpolator();
    PiecewisePolynomialInterpolator interpPos = new MonotonicityPreservingQuinticSplineInterpolator(interp);
    interpPos.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void infXdataTest() {
    double[] xValues = new double[] {1., 2., 3., INF };
    double[] yValues = new double[] {0., 0.1, 0.05, 0.2 };

    PiecewisePolynomialInterpolator interp = new CubicSplineInterpolator();
    PiecewisePolynomialInterpolator interpPos = new MonotonicityPreservingQuinticSplineInterpolator(interp);
    interpPos.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void infYdataTest() {
    double[] xValues = new double[] {1., 2., 3., 4. };
    double[] yValues = new double[] {0., 0., 0.1, 0.05, 0.2, INF };

    PiecewisePolynomialInterpolator interp = new CubicSplineInterpolator();
    PiecewisePolynomialInterpolator interpPos = new MonotonicityPreservingQuinticSplineInterpolator(interp);
    interpPos.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nanXdataTest() {
    double[] xValues = new double[] {1., 2., 3., Double.NaN };
    double[] yValues = new double[] {0., 0.1, 0.05, 0.2 };

    PiecewisePolynomialInterpolator interp = new CubicSplineInterpolator();
    PiecewisePolynomialInterpolator interpPos = new MonotonicityPreservingQuinticSplineInterpolator(interp);
    interpPos.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nanYdataTest() {
    double[] xValues = new double[] {1., 2., 3., 4. };
    double[] yValues = new double[] {0., 0., 0.1, 0.05, 0.2, Double.NaN };

    PiecewisePolynomialInterpolator interp = new CubicSplineInterpolator();
    PiecewisePolynomialInterpolator interpPos = new MonotonicityPreservingQuinticSplineInterpolator(interp);
    interpPos.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void infXdataMultiTest() {
    double[] xValues = new double[] {1., 2., 3., INF };
    double[][] yValues = new double[][] { {0., 0.1, 0.05, 0.2 }, {0., 0.1, 0.05, 0.2 } };

    PiecewisePolynomialInterpolator interp = new CubicSplineInterpolator();
    PiecewisePolynomialInterpolator interpPos = new MonotonicityPreservingQuinticSplineInterpolator(interp);
    interpPos.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void infYdataMultiTest() {
    double[] xValues = new double[] {1., 2., 3., 4. };
    double[][] yValues = new double[][] { {0., 0., 0.1, 0.05, 0.2, 1. }, {0., 0., 0.1, 0.05, 0.2, INF } };

    PiecewisePolynomialInterpolator interp = new CubicSplineInterpolator();
    PiecewisePolynomialInterpolator interpPos = new MonotonicityPreservingQuinticSplineInterpolator(interp);
    interpPos.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nanXdataMultiTest() {
    double[] xValues = new double[] {1., 2., 3., Double.NaN };
    double[][] yValues = new double[][] { {0., 0.1, 0.05, 0.2 }, {0., 0.1, 0.05, 0.2 } };

    PiecewisePolynomialInterpolator interp = new CubicSplineInterpolator();
    PiecewisePolynomialInterpolator interpPos = new MonotonicityPreservingQuinticSplineInterpolator(interp);
    interpPos.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nanYdataMultiTest() {
    double[] xValues = new double[] {1., 2., 3., 4. };
    double[][] yValues = new double[][] { {0., 0., 0.1, 0.05, 0.2, 1.1 }, {0., 0., 0.1, 0.05, 0.2, Double.NaN } };

    PiecewisePolynomialInterpolator interp = new CubicSplineInterpolator();
    PiecewisePolynomialInterpolator interpPos = new MonotonicityPreservingQuinticSplineInterpolator(interp);
    interpPos.interpolate(xValues, yValues);
  }

}

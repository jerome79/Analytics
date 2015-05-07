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
public class NonnegativityPreservingQuinticSplineInterpolatorTest {

  private static final double INF = 1. / 0.;

  /**
   * 
   */
  public void positivityClampedTest() {
    final double[] xValues = new double[] {1., 2., 3., 4., 5. };
    final double[] yValues = new double[] {0., 0.1, 1., 1., 20., 5., 0. };

    PiecewisePolynomialInterpolator interp = new CubicSplineInterpolator();
    PiecewisePolynomialResult result = interp.interpolate(xValues, yValues);

    PiecewisePolynomialFunction1D function = new PiecewisePolynomialFunction1D();

    PiecewisePolynomialInterpolator interpPos = new NonnegativityPreservingQuinticSplineInterpolator(interp);
    PiecewisePolynomialResult resultPos = interpPos.interpolate(xValues, yValues);

    assertEquals(resultPos.getDimensions(), result.getDimensions());
    assertEquals(resultPos.getNumberOfIntervals(), result.getNumberOfIntervals());
    assertEquals(resultPos.getOrder(), 6);

    final int nPts = 101;
    for (int i = 0; i < 101; ++i) {
      final double key = 1. + 4. / (nPts - 1) * i;
      assertTrue(function.evaluate(resultPos, key).getData()[0] >= 0.);
    }

    final int nData = xValues.length;
    for (int i = 1; i < nData - 2; ++i) {
      final double tau = Math.signum(resultPos.getCoefMatrix().getData()[i][5]);
      assertTrue(resultPos.getCoefMatrix().getData()[i][4] * tau >= -5. * yValues[i + 1] * tau / (xValues[i + 1] - xValues[i]));
      assertTrue(resultPos.getCoefMatrix().getData()[i][4] * tau <= 5. * yValues[i + 1] * tau / (xValues[i] - xValues[i - 1]));
      assertTrue(resultPos.getCoefMatrix().getData()[i][3] * tau * 2. >= tau *
          Math.max(8. * resultPos.getCoefMatrix().getData()[i][4] / (xValues[i] - xValues[i - 1]) - 20. * yValues[i + 1] / (xValues[i] - xValues[i - 1]) / (xValues[i] - xValues[i - 1]),
              -8. * resultPos.getCoefMatrix().getData()[i][4] / (xValues[i + 1] - xValues[i]) - 20. * yValues[i + 1] / (xValues[i + 1] - xValues[i]) / (xValues[i + 1] - xValues[i])));
    }
  }

  /**
   * 
   */
  public void positivityClampedMultiTest() {
    final double[] xValues = new double[] {1., 2., 3., 4., 5. };
    final double[][] yValues = new double[][] { {0., 0.1, 1., 1., 20., 5., 0. }, {-10., 0.1, 1., 1., 20., 5., 0. } };

    PiecewisePolynomialInterpolator interp = new CubicSplineInterpolator();
    PiecewisePolynomialResult result = interp.interpolate(xValues, yValues);

    PiecewisePolynomialFunction1D function = new PiecewisePolynomialFunction1D();

    PiecewisePolynomialInterpolator interpPos = new NonnegativityPreservingQuinticSplineInterpolator(interp);
    PiecewisePolynomialResult resultPos = interpPos.interpolate(xValues, yValues);

    assertEquals(resultPos.getDimensions(), result.getDimensions());
    assertEquals(resultPos.getNumberOfIntervals(), result.getNumberOfIntervals());
    assertEquals(resultPos.getOrder(), 6);

    final int nPts = 101;
    for (int i = 0; i < 101; ++i) {
      final double key = 1. + 4. / (nPts - 1) * i;
      assertTrue(function.evaluate(resultPos, key).getData()[0] >= 0.);
    }

    final int dim = yValues.length;
    final int nData = xValues.length;
    for (int j = 0; j < dim; ++j) {
      for (int i = 1; i < nData - 2; ++i) {
        final double tau = Math.signum(resultPos.getCoefMatrix().getData()[dim * i + j][5]);
        assertTrue(resultPos.getCoefMatrix().getData()[dim * i + j][4] * tau >= -5. * yValues[j][i + 1] * tau / (xValues[i + 1] - xValues[i]));
        assertTrue(resultPos.getCoefMatrix().getData()[dim * i + j][4] * tau <= 5. * yValues[j][i + 1] * tau / (xValues[i] - xValues[i - 1]));
        assertTrue(resultPos.getCoefMatrix().getData()[dim * i + j][3] * tau * 2. >= tau *
            Math.max(
                8. * resultPos.getCoefMatrix().getData()[dim * i + j][4] / (xValues[i] - xValues[i - 1]) - 20. * yValues[j][i + 1] / (xValues[i] - xValues[i - 1]) / (xValues[i] - xValues[i - 1]),
                -8. * resultPos.getCoefMatrix().getData()[dim * i + j][4] / (xValues[i + 1] - xValues[i]) - 20. * yValues[j][i + 1] / (xValues[i + 1] - xValues[i]) / (xValues[i + 1] - xValues[i])));
      }
    }
  }

  /**
   * 
   */
  public void positivityNotAKnotTest() {
    final double[] xValues = new double[] {1., 2., 3., 4., 5. };
    final double[] yValues = new double[] {0.1, 1., 1., 20., 5. };

    PiecewisePolynomialInterpolator interp = new CubicSplineInterpolator();
    PiecewisePolynomialResult result = interp.interpolate(xValues, yValues);

    PiecewisePolynomialFunction1D function = new PiecewisePolynomialFunction1D();

    PiecewisePolynomialInterpolator interpPos = new NonnegativityPreservingQuinticSplineInterpolator(interp);
    PiecewisePolynomialResult resultPos = interpPos.interpolate(xValues, yValues);

    assertEquals(resultPos.getDimensions(), result.getDimensions());
    assertEquals(resultPos.getNumberOfIntervals(), result.getNumberOfIntervals());
    assertEquals(resultPos.getOrder(), 6);

    final int nPts = 101;
    for (int i = 0; i < 101; ++i) {
      final double key = 1. + 4. / (nPts - 1) * i;
      assertTrue(function.evaluate(resultPos, key).getData()[0] >= 0.);
    }

    final int nData = xValues.length;
    for (int i = 1; i < nData - 2; ++i) {
      final double tau = Math.signum(resultPos.getCoefMatrix().getData()[i][5]);
      assertTrue(resultPos.getCoefMatrix().getData()[i][4] * tau >= -5. * yValues[i] * tau / (xValues[i + 1] - xValues[i]));
      assertTrue(resultPos.getCoefMatrix().getData()[i][4] * tau <= 5. * yValues[i] * tau / (xValues[i] - xValues[i - 1]));
      assertTrue(resultPos.getCoefMatrix().getData()[i][3] * tau * 2. >= tau *
          Math.max(8. * resultPos.getCoefMatrix().getData()[i][4] / (xValues[i] - xValues[i - 1]) - 20. * yValues[i] / (xValues[i] - xValues[i - 1]) / (xValues[i] - xValues[i - 1]),
              -8. * resultPos.getCoefMatrix().getData()[i][4] / (xValues[i + 1] - xValues[i]) - 20. * yValues[i] / (xValues[i + 1] - xValues[i]) / (xValues[i + 1] - xValues[i])));
    }
  }

  /**
   * 
   */
  public void positivityEndIntervalsTest() {
    final double[] xValues = new double[] {1., 2., 3., 4., 5., 6. };
    final double[][] yValues = new double[][] { {0.01, 0.01, 0.01, 10., 20., 1. }, {0.01, 0.01, 10., 10., 0.01, 0.01 } };

    PiecewisePolynomialInterpolator interp = new NaturalSplineInterpolator();
    PiecewisePolynomialResult result = interp.interpolate(xValues, yValues);

    PiecewisePolynomialFunction1D function = new PiecewisePolynomialFunction1D();

    PiecewisePolynomialInterpolator interpPos = new NonnegativityPreservingQuinticSplineInterpolator(interp);
    PiecewisePolynomialResult resultPos = interpPos.interpolate(xValues, yValues);

    assertEquals(resultPos.getDimensions(), result.getDimensions());
    assertEquals(resultPos.getNumberOfIntervals(), result.getNumberOfIntervals());
    assertEquals(resultPos.getOrder(), 6);

    final int nPts = 101;
    for (int i = 0; i < 101; ++i) {
      final double key = 1. + 5. / (nPts - 1) * i;
      assertTrue(function.evaluate(resultPos, key).getData()[0] >= 0.);
    }

    final int dim = yValues.length;
    final int nData = xValues.length;
    for (int j = 0; j < dim; ++j) {
      for (int i = 1; i < nData - 2; ++i) {
        final double tau = Math.signum(resultPos.getCoefMatrix().getData()[dim * i + j][5]);
        assertTrue(resultPos.getCoefMatrix().getData()[dim * i + j][4] * tau >= -5. * yValues[j][i] * tau / (xValues[i + 1] - xValues[i]));
        assertTrue(resultPos.getCoefMatrix().getData()[dim * i + j][4] * tau <= 5. * yValues[j][i] * tau / (xValues[i] - xValues[i - 1]));
        assertTrue(resultPos.getCoefMatrix().getData()[dim * i + j][3] * tau * 2. >= tau *
            Math.max(
                8. * resultPos.getCoefMatrix().getData()[dim * i + j][4] / (xValues[i] - xValues[i - 1]) - 20. * yValues[j][i] / (xValues[i] - xValues[i - 1]) / (xValues[i] - xValues[i - 1]),
                -8. * resultPos.getCoefMatrix().getData()[dim * i + j][4] / (xValues[i + 1] - xValues[i]) - 20. * yValues[j][i] / (xValues[i + 1] - xValues[i]) / (xValues[i + 1] - xValues[i])));
      }
    }
  }

  /**
   * 
   */
  public void flipTest() {
    final double[] xValues = new double[] {1., 2., 3., 4., 5., 6. };
    final double[] yValues = new double[] {3., 0.1, 0.01, 0.01, 0.1, 3. };

    final double[] xValuesFlip = new double[] {6., 2., 3., 5., 4., 1. };
    final double[] yValuesFlip = new double[] {3., 0.1, 0.01, 0.1, 0.01, 3. };

    PiecewisePolynomialInterpolator interp = new NaturalSplineInterpolator();

    PiecewisePolynomialFunction1D function = new PiecewisePolynomialFunction1D();

    PiecewisePolynomialInterpolator interpPos = new NonnegativityPreservingQuinticSplineInterpolator(interp);
    PiecewisePolynomialResult resultPos = interpPos.interpolate(xValues, yValues);
    PiecewisePolynomialResult resultPosFlip = interpPos.interpolate(xValuesFlip, yValuesFlip);

    assertEquals(resultPos.getDimensions(), resultPosFlip.getDimensions());
    assertEquals(resultPos.getNumberOfIntervals(), resultPosFlip.getNumberOfIntervals());
    assertEquals(resultPos.getOrder(), resultPosFlip.getOrder());

    final int nPts = 101;
    for (int i = 0; i < 101; ++i) {
      final double key = 2. + 3. / (nPts - 1) * i;

      assertTrue(function.evaluate(resultPos, key).getData()[0] >= 0.);
    }

    final int nData = xValues.length;
    for (int i = 0; i < nData - 1; ++i) {
      for (int k = 0; k < 6; ++k)
        assertEquals(resultPos.getCoefMatrix().getData()[i][k], resultPosFlip.getCoefMatrix().getData()[i][k]);
    }
  }

  /**
   * 
   */
  public void flipMultiTest() {
    final double[] xValues = new double[] {1., 2., 3., 4., 5., 6. };
    final double[][] yValues = new double[][] { {3., 0.1, 0.01, 0.01, 0.1, 3. }, {3., 0.1, 0.01, 0.001, 2., 3. } };

    final double[] xValuesFlip = new double[] {1., 2., 3., 5., 4., 6. };
    final double[][] yValuesFlip = new double[][] { {3., 0.1, 0.01, 0.1, 0.01, 3. }, {3., 0.1, 0.01, 2., 0.001, 3. } };

    PiecewisePolynomialInterpolator interp = new NaturalSplineInterpolator();

    PiecewisePolynomialFunction1D function = new PiecewisePolynomialFunction1D();

    PiecewisePolynomialInterpolator interpPos = new NonnegativityPreservingQuinticSplineInterpolator(interp);
    PiecewisePolynomialResult resultPos = interpPos.interpolate(xValues, yValues);
    PiecewisePolynomialResult resultPosFlip = interpPos.interpolate(xValuesFlip, yValuesFlip);

    assertEquals(resultPos.getDimensions(), resultPosFlip.getDimensions());
    assertEquals(resultPos.getNumberOfIntervals(), resultPosFlip.getNumberOfIntervals());
    assertEquals(resultPos.getOrder(), resultPosFlip.getOrder());

    final int nPts = 101;
    for (int i = 0; i < 101; ++i) {
      final double key = 2. + 3. / (nPts - 1) * i;
      assertTrue(function.evaluate(resultPos, key).getData()[0] >= 0.);
      assertTrue(function.evaluate(resultPos, key).getData()[1] >= 0.);
    }

    final int dim = yValues.length;
    final int nData = xValues.length;
    for (int j = 0; j < dim; ++j) {
      for (int i = 0; i < nData - 1; ++i) {
        for (int k = 0; k < 6; ++k)
          assertEquals(resultPos.getCoefMatrix().getData()[dim * i + j][k], resultPosFlip.getCoefMatrix().getData()[dim * i + j][k]);
      }
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
    PiecewisePolynomialInterpolator interpPos = new NonnegativityPreservingQuinticSplineInterpolator(interp);
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
    PiecewisePolynomialInterpolator interpPos = new NonnegativityPreservingQuinticSplineInterpolator(interp);
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
    PiecewisePolynomialInterpolator interpPos = new NonnegativityPreservingQuinticSplineInterpolator(interp);
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
    PiecewisePolynomialInterpolator interpPos = new NonnegativityPreservingQuinticSplineInterpolator(interp);
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
    PiecewisePolynomialInterpolator interpPos = new NonnegativityPreservingQuinticSplineInterpolator(interp);
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
    PiecewisePolynomialInterpolator interpPos = new NonnegativityPreservingQuinticSplineInterpolator(interp);
    interpPos.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void diffDataTest() {
    final double[] xValues = new double[] {1., 2., 3., 4. };
    final double[] yValues = new double[] {0., 0.1, 0.05 };

    PiecewisePolynomialInterpolator interp = new NaturalSplineInterpolator();
    PiecewisePolynomialInterpolator interpPos = new NonnegativityPreservingQuinticSplineInterpolator(interp);
    interpPos.interpolate(xValues, yValues);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void diffDataMultiTest() {
    final double[] xValues = new double[] {1., 2., 3., 4. };
    final double[][] yValues = new double[][] { {2., 0., 0.1, 0.05, 2. }, {1., 0., 0.1, 1.05, 2. } };

    PiecewisePolynomialInterpolator interp = new NaturalSplineInterpolator();
    PiecewisePolynomialInterpolator interpPos = new NonnegativityPreservingQuinticSplineInterpolator(interp);
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
    PiecewisePolynomialInterpolator interpPos = new NonnegativityPreservingQuinticSplineInterpolator(interp);
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
    PiecewisePolynomialInterpolator interpPos = new NonnegativityPreservingQuinticSplineInterpolator(interp);
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
    PiecewisePolynomialInterpolator interpPos = new NonnegativityPreservingQuinticSplineInterpolator(interp);
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
    PiecewisePolynomialInterpolator interpPos = new NonnegativityPreservingQuinticSplineInterpolator(interp);
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
    PiecewisePolynomialInterpolator interpPos = new NonnegativityPreservingQuinticSplineInterpolator(interp);
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
    PiecewisePolynomialInterpolator interpPos = new NonnegativityPreservingQuinticSplineInterpolator(interp);
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
    PiecewisePolynomialInterpolator interpPos = new NonnegativityPreservingQuinticSplineInterpolator(interp);
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
    PiecewisePolynomialInterpolator interpPos = new NonnegativityPreservingQuinticSplineInterpolator(interp);
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
    PiecewisePolynomialInterpolator interpPos = new NonnegativityPreservingQuinticSplineInterpolator(interp);
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
    PiecewisePolynomialInterpolator interpPos = new NonnegativityPreservingQuinticSplineInterpolator(interp);
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
    PiecewisePolynomialInterpolator interpPos = new NonnegativityPreservingQuinticSplineInterpolator(interp);
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
    PiecewisePolynomialInterpolator interpPos = new NonnegativityPreservingQuinticSplineInterpolator(interp);
    interpPos.interpolate(xValues, yValues);
  }

}

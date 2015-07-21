/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.function.PiecewisePolynomialFunction1D;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.analytics.math.minimization.DoubleRangeLimitTransform;

/**
 * Test.
 */
@Test
public class Interpolator1DTest {

  private static final double EPS = 1e-6;

  /**
   *  
   */
  public void firstDerivativeInterpolatorsTest() {
    final double[][] xValues = new double[][] { {1., 2., 3., 4., 5., 6. }, {2., 2.1, 4., 5.1, 5.22, 6.8 } };
    final double[][] yValues = new double[][] { {1., 1.1, 2., 3., 5.9, 6. }, {1., 2.1, 3., 3.1, 3.22, 4.8 } };
    final int dim = xValues.length;
    final int nData = xValues[0].length;
    final int nKeys = 11 * nData;
    final Interpolator1D[] interp = new Interpolator1D[] {
        new DoubleQuadraticInterpolator1D(),
        new ExponentialInterpolator1D(),
        new LinearInterpolator1D(),
        new LogLinearInterpolator1D(),
        new NaturalCubicSplineInterpolator1D(),
        new PCHIPInterpolator1D(),
        new LogNaturalCubicMonotonicityPreservingInterpolator1D(),
        new StepInterpolator1D(),
        new StepUpperInterpolator1D(),
        new TimeSquareInterpolator1D()};
    final int nMethods = interp.length;
    for (int j = 0; j < dim; ++j) {
      for (int i = 0; i < nMethods; ++i) {

        Interpolator1DDataBundle data = interp[i].getDataBundleFromSortedArrays(xValues[j], yValues[j]);
        final double xMin = xValues[j][0];
        final double xMax = xValues[j][nData - 1];

        for (int m = 0; m < nKeys - 1; ++m) {
          final double xKey = xMin + (xMax - xMin) * (m + 1.) / nKeys;

          assertEquals(interp[i].firstDerivative(data, xKey), 0.5 * (interp[i].interpolate(data, xKey + EPS) - interp[i].interpolate(data, xKey - EPS)) / EPS, EPS);
        }
      }
    }
  }

  /**
   *  Data length = 3
   */
  public void firstDerivativeInterpolatorsThreePointsTest() {
    final double[][] xValues = new double[][] { {1., 2., 3. }, {2., 2.1, 4. } };
    final double[][] yValues = new double[][] { {1., 1.1, 2. }, {1., 2.1, 3. } };
    final int dim = xValues.length;
    final int nData = xValues[0].length;
    final int nKeys = 11 * nData;
    final Interpolator1D[] interp = new Interpolator1D[] {
        new DoubleQuadraticInterpolator1D(),
        new ExponentialInterpolator1D(),
        new LinearInterpolator1D(),
        new LogLinearInterpolator1D(),
        new NaturalCubicSplineInterpolator1D(),
        new PCHIPInterpolator1D(),
        new StepInterpolator1D(),
        new StepUpperInterpolator1D(),
        new TimeSquareInterpolator1D()};
    final int nMethods = interp.length;
    for (int j = 0; j < dim; ++j) {
      for (int i = 0; i < nMethods; ++i) {

        Interpolator1DDataBundle data = interp[i].getDataBundleFromSortedArrays(xValues[j], yValues[j]);
        final double xMin = xValues[j][0];
        final double xMax = xValues[j][nData - 1];

        for (int m = 0; m < nKeys - 1; ++m) {
          final double xKey = xMin + (xMax - xMin) * (m + 1.) / nKeys;

          assertEquals(interp[i].firstDerivative(data, xKey), 0.5 * (interp[i].interpolate(data, xKey + EPS) - interp[i].interpolate(data, xKey - EPS)) / EPS, EPS);
        }
      }
    }
  }

  /**
   *  Data length = 2
   */
  public void firstDerivativeInterpolatorsTwoPointsTest2() {
    final double[][] xValues = new double[][] { {1., 2. }, {2., 2.1 } };
    final double[][] yValues = new double[][] { {1., 1.1 }, {1., 2.1 } };
    final int dim = xValues.length;
    final int nData = xValues[0].length;
    final int nKeys = 11 * nData;
    final Interpolator1D[] interp = new Interpolator1D[] {
        new DoubleQuadraticInterpolator1D(),
        new ExponentialInterpolator1D(),
        new LinearInterpolator1D(),
        new LogLinearInterpolator1D(),
        new NaturalCubicSplineInterpolator1D(),
        new StepInterpolator1D(),
        new StepUpperInterpolator1D(),
        new TimeSquareInterpolator1D()};
    final int nMethods = interp.length;
    for (int j = 0; j < dim; ++j) {
      for (int i = 0; i < nMethods; ++i) {

        Interpolator1DDataBundle data = interp[i].getDataBundleFromSortedArrays(xValues[j], yValues[j]);
        final double xMin = xValues[j][0];
        final double xMax = xValues[j][nData - 1];

        for (int m = 0; m < nKeys - 1; ++m) {
          final double xKey = xMin + (xMax - xMin) * (m + 1.) / nKeys;

          assertEquals(interp[i].firstDerivative(data, xKey), 0.5 * (interp[i].interpolate(data, xKey + EPS) - interp[i].interpolate(data, xKey - EPS)) / EPS, EPS);
        }
      }
    }
  }

  /**
   * Test for interpolators calling another interpolator
   */
  public void firstDerivativeInterpolatorsSecondaryTest() {
    final double[][] xValues = new double[][] { {1., 2., 3., 4., 5., 6. }, {1.1, 1.3, 3.8, 4.1, 5.9, 6. } };
    final double[][] yValues = new double[][] { {1., 1.1, 2., 3., 5.9, 6. }, {1., 1.12, 1., 3.4, 5.9, 3.2 } };
    final int dim = xValues.length;
    final int nData = xValues[0].length;
    final int nKeys = 11 * nData;
    final Interpolator1D interpPre = new DoubleQuadraticInterpolator1D();
    final Interpolator1D[] interp = new Interpolator1D[] {new CombinedInterpolatorExtrapolator(interpPre), new TransformedInterpolator1D(interpPre, new DoubleRangeLimitTransform(0, 1)) };
    final int nMethods = interp.length;
    for (int j = 0; j < dim; ++j) {
      for (int i = 0; i < nMethods; ++i) {
        Interpolator1DDataBundle data = interp[i].getDataBundleFromSortedArrays(xValues[j], yValues[j]);
        final double xMin = xValues[j][0];
        final double xMax = xValues[j][nData - 1];

        for (int m = 0; m < nKeys - 1; ++m) {
          final double xKey = xMin + (xMax - xMin) * (m + 1.) / nKeys;
          assertEquals(interp[i].firstDerivative(data, xKey), 0.5 * (interp[i].interpolate(data, xKey + EPS) - interp[i].interpolate(data, xKey - EPS)) / EPS, EPS);
        }
      }
    }
  }

  /**
   * Test for PiecewisePolynomialInterpolator1D
   */
  public void piecewisePolynomialTest() {
    final int nData = 10;
    final int dim = 3;
    final double[] xValues = new double[nData];
    final double[][] yValues = new double[dim][nData];
    final double[][] yValuesForClamped = new double[dim][nData + 2];
    final int nKeys = 10 * nData;
    final double[] xKeys = new double[nKeys];

    for (int i = 0; i < nData; ++i) {
      xValues[i] = i * i + i - 1.;
      yValues[0][i] = 0.5 * xValues[i] * xValues[i] * xValues[i] - 1.5 * xValues[i] * xValues[i] + xValues[i] - 2.;
      yValues[1][i] = Math.exp(0.1 * xValues[i] - 6.);
      yValues[2][i] = (2. * xValues[i] * xValues[i] + xValues[i]) / (xValues[i] * xValues[i] + xValues[i] * xValues[i] * xValues[i] + 5. * xValues[i] + 2.);
      yValuesForClamped[0][i + 1] = 0.5 * xValues[i] * xValues[i] * xValues[i] - 1.5 * xValues[i] * xValues[i] + xValues[i] - 2.;
      yValuesForClamped[1][i + 1] = Math.exp(0.1 * xValues[i] - 6.);
      yValuesForClamped[2][i + 1] = (2. * xValues[i] * xValues[i] + xValues[i]) / (xValues[i] * xValues[i] + xValues[i] * xValues[i] * xValues[i] + 5. * xValues[i] + 2.);
    }
    yValuesForClamped[0][0] = 0.;
    yValuesForClamped[1][0] = 0.;
    yValuesForClamped[2][0] = 0.;
    yValuesForClamped[0][nData + 1] = 0.;
    yValuesForClamped[1][nData + 1] = 0.;
    yValuesForClamped[2][nData + 1] = 0.;
    final double xMin = xValues[0];
    final double xMax = xValues[nData - 1];
    for (int i = 0; i < nKeys; ++i) {
      xKeys[i] = xMin + (xMax - xMin) / (10 * nData - 1) * i;
    }

    final PiecewisePolynomialInterpolator[] bareInterp = new PiecewisePolynomialInterpolator[] {
        new CubicSplineInterpolator(),
        new ConstrainedCubicSplineInterpolator(),
        new MonotonicityPreservingCubicSplineInterpolator(new NaturalSplineInterpolator()),
        new NaturalSplineInterpolator(),
        new NonnegativityPreservingCubicSplineInterpolator(new NaturalSplineInterpolator()),
        new CubicSplineInterpolator(), new SemiLocalCubicSplineInterpolator()};
    final Interpolator1D[] wrappedInterp = new PiecewisePolynomialInterpolator1D[] {
        new ClampedCubicSplineInterpolator1D(),
        new ConstrainedCubicSplineInterpolator1D(),
        new NonnegativityPreservingCubicSplineInterpolator1D(),
        new SemiLocalCubicSplineInterpolator1D()};
    final int nMethods = bareInterp.length;
    final PiecewisePolynomialFunction1D function = new PiecewisePolynomialFunction1D();

    for (int i = 0; i < dim; ++i) {
      final double[] bareResClamp = function.differentiate(bareInterp[0].interpolate(xValues, yValuesForClamped[i]), xKeys).getData()[0];
      for (int j = 0; j < nKeys; ++j) {
        final Interpolator1DDataBundle dataBundleClamp = wrappedInterp[0].getDataBundleFromSortedArrays(xValues, yValues[i]);
        final double wrappedResClamp = wrappedInterp[0].firstDerivative(dataBundleClamp, xKeys[j]);
        assertEquals(wrappedResClamp, bareResClamp[j], Math.max(Math.abs(bareResClamp[j]), 1.) * 1.e-15);
      }

      for (int k = 1; k < nMethods; ++k) {
        final double[] bareRes = function.differentiate(bareInterp[k].interpolate(xValues, yValues[i]), xKeys).getData()[0];
        for (int j = 0; j < nKeys; ++j) {
          final Interpolator1DDataBundle dataBundle = wrappedInterp[k].getDataBundleFromSortedArrays(xValues, yValues[i]);
          final double wrappedRes = wrappedInterp[k].firstDerivative(dataBundle, xKeys[j]);
          assertEquals(wrappedRes, bareRes[j], Math.max(Math.abs(bareRes[j]), 1.) * 1.e-15);
        }
      }
    }

  }

  /**
   * Test for extrapolator
   */
  public void firstDerivativeExtrapolatorstest() {
    final double[][] xValues = new double[][] { {1., 2., 3., 4., 5., 6. }, {2., 2.1, 4., 5.1, 5.22, 6.8 } };
    final double[][] yValues = new double[][] { {1., 1.1, 2., 3., 5.9, 6. }, {1., -1.1, 2.6, -3., -3.9, 3. } };
    final int dim = xValues.length;

    Interpolator1D interp = new PCHIPInterpolator1D();

    final Extrapolator1D[] extrap = {
        new ExponentialExtrapolator1D(),
        new FlatExtrapolator1D(),
        new LinearExtrapolator1D()};

    for (int j = 0; j < dim; ++j) {
      Interpolator1DDataBundle data = interp.getDataBundleFromSortedArrays(xValues[j], yValues[j]);

      for (Extrapolator1D anExtrap : extrap) {
        assertEquals(
            anExtrap.firstDerivative(data, .2, interp),
            0.5 * (anExtrap.extrapolate(data, .2 + EPS, interp) -
                anExtrap.extrapolate(data, .2 - EPS, interp)) / EPS, EPS);
        assertEquals(
            anExtrap.firstDerivative(data, 7.2, interp),
            0.5 * (anExtrap.extrapolate(data, 7.2 + EPS, interp) -
                anExtrap.extrapolate(data, 7.2 - EPS, interp)) / EPS, EPS);
      }
      CombinedInterpolatorExtrapolator interpolatorExtrapolator = new CombinedInterpolatorExtrapolator(
          interp,
          new ExponentialExtrapolator1D(),
          new FlatExtrapolator1D());
      assertEquals(
          interpolatorExtrapolator.firstDerivative(data, 7.2),
          0.5 * (interpolatorExtrapolator.interpolate(data, 7.2 + EPS) -
              interpolatorExtrapolator.interpolate(data, 7.2 - EPS)) / EPS, EPS);
    }
  }

}

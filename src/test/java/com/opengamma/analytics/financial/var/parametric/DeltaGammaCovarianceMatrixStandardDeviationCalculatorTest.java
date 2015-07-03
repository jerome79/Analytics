/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.var.parametric;

import static org.testng.AssertJUnit.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.Matrix;

/**
 * Test.
 */
@Test
public class DeltaGammaCovarianceMatrixStandardDeviationCalculatorTest {

  private static final DeltaGammaCovarianceMatrixStandardDeviationCalculator F = new DeltaGammaCovarianceMatrixStandardDeviationCalculator();
  private static final DoubleMatrix1D DELTA_VECTOR = new DoubleMatrix1D(new double[] {1, 5});
  private static final DoubleMatrix2D GAMMA_MATRIX = new DoubleMatrix2D(
      new double[][] {new double[] {25, -7.5}, new double[] {-7.5, 125}});
  private static final DoubleMatrix2D COVARIANCE_MATRIX = new DoubleMatrix2D(
      new double[][] {new double[] {0.0036, -0.0006}, new double[] {-0.0006, 0.0016}});
  private static final Map<Integer, Matrix<?>> SENSITIVITIES = new HashMap<>();
  private static final Map<Integer, DoubleMatrix2D> COVARIANCES = new HashMap<>();

  static {
    SENSITIVITIES.put(1, DELTA_VECTOR);
    SENSITIVITIES.put(2, GAMMA_MATRIX);
    COVARIANCES.put(1, COVARIANCE_MATRIX);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    F.evaluate((Map<Integer, ParametricVaRDataBundle>) null);
  }

  public void test() {
    ParametricVaRDataBundle deltaData = new ParametricVaRDataBundle(DELTA_VECTOR, COVARIANCE_MATRIX, 1);
    ParametricVaRDataBundle gammaData = new ParametricVaRDataBundle(GAMMA_MATRIX, COVARIANCE_MATRIX, 2);
    Map<Integer, ParametricVaRDataBundle> m = new HashMap<>();
    m.put(1, deltaData);
    assertEquals(F.evaluate(m), Math.sqrt(0.0376), 1e-4);
    m.put(2, gammaData);
    assertEquals(F.evaluate(m), 0.256, 1e-3);
    m = new HashMap<>();
    m.put(2, gammaData);
    assertEquals(F.evaluate(m), Math.sqrt(0.065536 - 0.0376), 1e-3);
  }

}

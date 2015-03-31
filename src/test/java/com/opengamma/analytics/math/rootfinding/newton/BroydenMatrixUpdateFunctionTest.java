/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.rootfinding.newton;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;

/**
 * Test.
 */
@Test
public class BroydenMatrixUpdateFunctionTest {
  private static final BroydenMatrixUpdateFunction UPDATE = new BroydenMatrixUpdateFunction();
  private static final DoubleMatrix1D V = new DoubleMatrix1D(new double[] {1, 2});
  private static final DoubleMatrix2D M = new DoubleMatrix2D(new double[][] {new double[] {3, 4}, new double[] {5, 6}});
  private static final Function1D<DoubleMatrix1D, DoubleMatrix2D> J = new Function1D<DoubleMatrix1D, DoubleMatrix2D>() {
    @Override
    public DoubleMatrix2D evaluate(final DoubleMatrix1D x) {
      return M;
    }
  };

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDeltaX() {
    UPDATE.getUpdatedMatrix(J, V, null, V, M);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDeltaY() {
    UPDATE.getUpdatedMatrix(J, V, V, null, M);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMatrix() {
    UPDATE.getUpdatedMatrix(J, V, V, V, null);
  }
}

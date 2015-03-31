/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.descriptive;

import static org.testng.Assert.assertEquals;

import java.util.function.Function;

import org.testng.annotations.Test;

/**
 * Test.
 */
@Test
public class PartialMomentCalculatorTest {
  private static final Function<double[], Double> UPSIDE1 = new PartialMomentCalculator(0, false);
  private static final Function<double[], Double> DOWNSIDE1 = new PartialMomentCalculator();
  private static final Function<double[], Double> UPSIDE2 = new PartialMomentCalculator(0.5, false);
  private static final Function<double[], Double> DOWNSIDE2 = new PartialMomentCalculator(-0.5, true);
  private static final int N = 100;
  private static final double[] POSITIVE_X = new double[N];
  private static final double[] NEGATIVE_X = new double[N];
  private static final double[] X1 = new double[N];
  private static final double[] X2 = new double[N];

  static {
    for (int i = 0; i < N; i++) {
      POSITIVE_X[i] = 0.5;
      NEGATIVE_X[i] = -0.5;
      if (i % 10 == 0) {
        X1[i] = 0.64;
        X2[i] = -0.6;
      }
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullArray() {
    UPSIDE1.apply(null);
  }

  @Test
  public void test() {
    final double eps = 1e-15;
    assertEquals(UPSIDE1.apply(POSITIVE_X), 0.5, eps);
    assertEquals(DOWNSIDE1.apply(POSITIVE_X), 0, eps);
    assertEquals(UPSIDE1.apply(NEGATIVE_X), 0, eps);
    assertEquals(DOWNSIDE1.apply(NEGATIVE_X), 0.5, eps);
    assertEquals(UPSIDE2.apply(X1), 0.14, eps);
    assertEquals(DOWNSIDE2.apply(X2), 0.1, eps);
  }
}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.descriptive.robust;

import static org.testng.Assert.assertEquals;

import java.util.function.Function;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;
import org.testng.annotations.Test;

import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;

/**
 * Test.
 */
@Test
public class InterquartileRangeCalculatorTest {
  private static final Function<double[], Double> IQR = new InterquartileRangeCalculator();
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);
  private static final double[] UNIFORM_DATA;
  private static final double[] NORMAL_DATA;
  private static final double EPS = 1e-2;
  static {
    final int n = 500000;
    UNIFORM_DATA = new double[n];
    NORMAL_DATA = new double[n];
    for (int i = 0; i < n; i++) {
      UNIFORM_DATA[i] = RANDOM.nextDouble();
      NORMAL_DATA[i] = NORMAL.nextRandom();
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull() {
    IQR.apply(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInsufficientData() {
    IQR.apply(new double[]{1., 2.});
  }

  @Test
  public void test() {
    final double[] x1 = new double[] {1., 2., 3., 4., 5., 6., 7., 8., 9., 10.};
    final double[] x2 = new double[] {1., 2., 3., 4., 5., 6., 7., 8., 9., 10., 11., 12., 13.};
    assertEquals(IQR.apply(x1), 5, 1e-15);
    assertEquals(IQR.apply(x2), 6, 1e-15);
    assertEquals(IQR.apply(UNIFORM_DATA), 0.5, EPS);
    assertEquals(IQR.apply(NORMAL_DATA), 2 * NORMAL.getInverseCDF(0.75), EPS);
  }
}

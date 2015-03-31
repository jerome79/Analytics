/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.descriptive;

import static org.testng.Assert.assertEquals;

import java.util.function.Function;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;
import org.testng.annotations.Test;

/**
 * Test.
 */
@Test
public class SemiStandardDeviationCalculatorTest {
  private static final Function<double[], Double> UPSIDE = new SemiStandardDeviationCalculator(false);
  private static final Function<double[], Double> DOWNSIDE = new SemiStandardDeviationCalculator();
  private static final int N = 1000000;
  private static final double[] X = new double[N];

  static {
    final MersenneTwister64 engine = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);
    for (int i = 0; i < N; i++) {
      X[i] = engine.nextDouble() - 0.5;
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullArray() {
    UPSIDE.apply(null);
  }

  @Test
  public void test() {
    final double eps = 1e-3;
    PartialMomentCalculator pm = new PartialMomentCalculator(0, true);
    assertEquals(UPSIDE.apply(X), pm.apply(X), eps);
    pm = new PartialMomentCalculator(0, false);
    assertEquals(DOWNSIDE.apply(X), pm.apply(X), eps);
  }
}

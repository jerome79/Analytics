/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.descriptive;

import static org.testng.AssertJUnit.assertEquals;

import java.util.function.Function;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;
import org.testng.annotations.Test;

import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;

/**
 * Test.
 */
@Test
public class PearsonSkewnessCoefficientCalculatorTest {
  private static final double STD = 2.;
  private static final Function<double[], Double> FIRST = new PearsonFirstSkewnessCoefficientCalculator();
  private static final Function<double[], Double> SECOND = new PearsonSecondSkewnessCoefficientCalculator();
  private static final ProbabilityDistribution<Double> NORMAL =
      new NormalDistribution(0, STD, new MersenneTwister64(MersenneTwister.DEFAULT_SEED));
  private static final double[] NORMAL_DATA = new double[50000];
  static {
    for (int i = 0; i < 50000; i++) {
      NORMAL_DATA[i] = ((int) (100 * NORMAL.nextRandom())) / 100.;
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFirst() {
    FIRST.apply(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecond() {
    SECOND.apply(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyFirst() {
    FIRST.apply(new double[0]);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptySecond() {
    SECOND.apply(new double[0]);
  }

  @Test
  public void test() {
    final Double t1 = FIRST.apply(NORMAL_DATA);
    final Double t2 = SECOND.apply(NORMAL_DATA);
    assertEquals(t1, 0, 0.1);
    assertEquals(t2, 0, 0.1);
  }
}

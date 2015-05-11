/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.descriptive;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import java.util.function.Function;

import org.apache.commons.math3.random.Well44497b;
import org.testng.annotations.Test;

import com.opengamma.analytics.math.statistics.distribution.ChiSquareDistribution;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;

/**
 * Test.
 */
@Test
public class QuartileSkewnessCalculatorTest {
  private static final double STD = 2.;
  private static final Function<double[], Double> SKEW = new QuartileSkewnessCalculator();
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, STD, new Well44497b(0L));
  private static final ProbabilityDistribution<Double> CHI_SQ = new ChiSquareDistribution(4);
  private static final double[] NORMAL_DATA = new double[50000];
  private static final double[] CHI_SQ_DATA = new double[50000];
  static {
    for (int i = 0; i < 50000; i++) {
      NORMAL_DATA[i] = NORMAL.nextRandom();
      CHI_SQ_DATA[i] = CHI_SQ.nextRandom();
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull() {
    SKEW.apply(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmpty() {
    SKEW.apply(new double[0]);
  }

  @Test
  public void test() {
    Double t = SKEW.apply(NORMAL_DATA);
    assertEquals(t, 0, 0.1);
    t = SKEW.apply(CHI_SQ_DATA);
    assertFalse(Math.abs(t) < 0.1);
  }
}

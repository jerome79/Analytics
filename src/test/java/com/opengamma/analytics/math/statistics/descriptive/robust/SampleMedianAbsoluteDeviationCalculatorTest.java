/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.descriptive.robust;

import static org.testng.AssertJUnit.assertEquals;

import java.util.function.Function;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.descriptive.SampleStandardDeviationCalculator;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;

/**
 */
@Test
public class SampleMedianAbsoluteDeviationCalculatorTest {
  private static final double MEAN = 3.5;
  private static final double STD = 0.2;
  private static final Function<double[], Double> MAD_CALC = new SampleMedianAbsoluteDeviationCalculator();
  private static final Function1D<double[], Double> STD_CALC = new SampleStandardDeviationCalculator();
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(MEAN, STD);
  private static final double[] NORMAL_DATA = new double[50000];
  private static final double EPS = 1e-2;
  static {
    for (int i = 0; i < 50000; i++) {
      NORMAL_DATA[i] = NORMAL.nextRandom();
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull() {
    MAD_CALC.apply(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInsufficientData() {
    MAD_CALC.apply(new double[] {3. });
  }

  @Test
  public void test() {
    assertEquals(STD_CALC.evaluate(NORMAL_DATA), MAD_CALC.apply(NORMAL_DATA) / .6745, EPS);
  }
}

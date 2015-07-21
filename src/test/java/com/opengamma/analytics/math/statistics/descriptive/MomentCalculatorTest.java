/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.descriptive;

import static org.testng.Assert.assertEquals;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.distribution.ChiSquareDistribution;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.analytics.math.statistics.distribution.StudentTDistribution;

/**
 * Test.
 */
@Test
public class MomentCalculatorTest {
  private static final double STD = 2.;
  private static final double DOF = 10;
  private static final Function1D<double[], Double> SAMPLE_VARIANCE = new SampleVarianceCalculator();
  private static final Function1D<double[], Double> SAMPLE_STD = new SampleStandardDeviationCalculator();
  private static final RandomEngine ENGINE = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, STD, ENGINE);
  private static final ProbabilityDistribution<Double> STUDENT_T = new StudentTDistribution(DOF, ENGINE);
  private static final ProbabilityDistribution<Double> CHI_SQ = new ChiSquareDistribution(DOF, ENGINE);
  private static final double[] NORMAL_DATA = new double[500000];
  private static final double[] STUDENT_T_DATA = new double[500000];
  private static final double[] CHI_SQ_DATA = new double[500000];
  private static final double EPS = 0.1;
  static {
    for (int i = 0; i < 500000; i++) {
      NORMAL_DATA[i] = NORMAL.nextRandom();
      STUDENT_T_DATA[i] = STUDENT_T.nextRandom();
      CHI_SQ_DATA[i] = CHI_SQ.nextRandom();
    }
  }

  @Test
  public void testNull() {
    assertNullArg(SAMPLE_VARIANCE);
    assertNullArg(SAMPLE_STD);
  }

  @Test
  public void testInsufficientData() {
    assertInsufficientData(SAMPLE_VARIANCE);
    assertInsufficientData(SAMPLE_STD);
  }

  private void assertNullArg(final Function1D<double[], Double> f) {
    try {
      f.evaluate((double[]) null);
      Assert.fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
  }

  private void assertInsufficientData(final Function1D<double[], Double> f) {
    try {
      f.evaluate(new double[] {1. });
      Assert.fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
  }

  @Test
  public void testNormal() {
    assertEquals(SAMPLE_VARIANCE.evaluate(NORMAL_DATA), STD * STD, EPS);
    assertEquals(SAMPLE_STD.evaluate(NORMAL_DATA), STD, EPS);
  }

  @Test
  public void testStudentT() {
    final double variance = DOF / (DOF - 2);
    assertEquals(SAMPLE_VARIANCE.evaluate(STUDENT_T_DATA), variance, EPS);
    assertEquals(SAMPLE_STD.evaluate(STUDENT_T_DATA), Math.sqrt(variance), EPS);
  }

  @Test
  public void testChiSq() {
    final double variance = 2 * DOF;
    assertEquals(SAMPLE_VARIANCE.evaluate(CHI_SQ_DATA), variance, EPS);
    assertEquals(SAMPLE_STD.evaluate(CHI_SQ_DATA), Math.sqrt(variance), EPS);
  }

}

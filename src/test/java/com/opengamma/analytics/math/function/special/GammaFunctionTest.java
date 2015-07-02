/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.function.special;

import static org.testng.AssertJUnit.assertEquals;

import java.util.function.DoubleUnaryOperator;

import org.apache.commons.math3.random.Well44497b;
import org.testng.annotations.Test;

import com.opengamma.analytics.math.function.Function1D;

/**
 * Test.
 */
@Test
public class GammaFunctionTest {
  private static final Well44497b RANDOM = new Well44497b(0L);
  private static final DoubleUnaryOperator GAMMA = new GammaFunction();
  private static final Function1D<Double, Double> LN_GAMMA = new NaturalLogGammaFunction();
  private static final double EPS = 1e-9;

  @Test
  public void test() {
    final double x = RANDOM.nextDouble();
    assertEquals(Math.log(GAMMA.applyAsDouble(x)), LN_GAMMA.evaluate(x), EPS);
  }
}

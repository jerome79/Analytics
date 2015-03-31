/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.descriptive;

import static org.testng.AssertJUnit.assertEquals;

import java.util.function.DoubleBinaryOperator;

import org.testng.annotations.Test;

/**
 * Test.
 */
@Test
public class LognormalPearsonKurtosisFromVolatilityCalculatorTest {
  private static final DoubleBinaryOperator F = new LognormalPearsonKurtosisFromVolatilityCalculator();
  private static final double SIGMA = 0.3;
  private static final double T = 0.25;

  @Test
  public void test() {
    assertEquals(F.applyAsDouble(SIGMA, T), 3.3719, 1e-4);
  }
}

/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation.weight;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

/**
 * Test.
 */
@Test
public class LinearWeightingFunctionTest extends WeightingFunctionTestCase {

  @Override
  protected LinearWeightingFunction getInstance() {
    return WeightingFunctionFactory.LINEAR_WEIGHTING_FUNCTION;
  }

  @Test
  public void testWeighting() {
    assertEquals(0.55, getInstance().getWeight(STRIKES, INDEX, STRIKE), EPS);
    assertEquals(1, getInstance().getWeight(STRIKES, INDEX, STRIKES[3]), EPS);
  }
}

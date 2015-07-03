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
public class CosineWeightingFunctionTest extends WeightingFunctionTestCase {

  @Override
  protected CosineWeightingFunction getInstance() {
    return WeightingFunctionFactory.COSINE_WEIGHTING_FUNCTION;
  }

  @Test
  public void testWeighting() {
    assertEquals(1.0, getInstance().getWeight(STRIKES, INDEX, STRIKES[3]), EPS);
    assertEquals(0.0, getInstance().getWeight(STRIKES, INDEX, STRIKES[4] - EPS), 100 * EPS);
    assertEquals(0.5, getInstance().getWeight(STRIKES, INDEX, 0.5 * (STRIKES[3] + STRIKES[4])), 10 * EPS);
  }
}

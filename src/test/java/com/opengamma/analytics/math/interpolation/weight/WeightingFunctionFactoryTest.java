/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation.weight;

import static com.opengamma.analytics.math.interpolation.weight.WeightingFunctionFactory.getWeightingFunction;
import static com.opengamma.analytics.math.interpolation.weight.WeightingFunctionFactory.getWeightingFunctionName;
import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

/**
 * Test.
 */
@Test
public class WeightingFunctionFactoryTest {

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadName() {
    getWeightingFunction("Random");
  }

  public void test() {
    assertEquals(
        WeightingFunctionFactory.COSINE_WEIGHTING_FUNCTION_NAME,
        getWeightingFunctionName(getWeightingFunction(WeightingFunctionFactory.COSINE_WEIGHTING_FUNCTION_NAME)));

    assertEquals(
        WeightingFunctionFactory.LINEAR_WEIGHTING_FUNCTION_NAME,
        getWeightingFunctionName(getWeightingFunction(WeightingFunctionFactory.LINEAR_WEIGHTING_FUNCTION_NAME)));

    assertEquals(
        WeightingFunctionFactory.SINE_WEIGHTING_FUNCTION_NAME,
        getWeightingFunctionName(getWeightingFunction(WeightingFunctionFactory.SINE_WEIGHTING_FUNCTION_NAME)));
  }

}

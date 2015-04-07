/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.function;

import org.testng.annotations.Test;



/**
 * Test.
 */
@Test
public class SABRPaulotVolatilityFunctionTest extends SABRVolatilityFunctionTestCase {
  private static final SABRPaulotVolatilityFunction FUNCTION = new SABRPaulotVolatilityFunction();

  @Override
  protected VolatilityFunctionProvider<SABRFormulaData> getFunction() {
    return FUNCTION;
  }

  @Override
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testApproachingLogNormalEquivalent2() {
    getFunction().getVolatilityFunction(OPTION, FORWARD).evaluate(APPROACHING_LOG_NORMAL_EQUIVALENT2);
  }

  @Override
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testApproachingLogNormalEquivalent3() {
    getFunction().getVolatilityFunction(OPTION, FORWARD).evaluate(APPROACHING_LOG_NORMAL_EQUIVALENT3);
  }

}

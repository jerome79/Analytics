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
public class SABRHaganAlternativeVolatilityFunctionTest extends SABRVolatilityFunctionTestCase {
  private static final SABRHaganAlternativeVolatilityFunction FUNCTION = new SABRHaganAlternativeVolatilityFunction();

  @Override
  protected VolatilityFunctionProvider<SABRFormulaData> getFunction() {
    return FUNCTION;
  }

}

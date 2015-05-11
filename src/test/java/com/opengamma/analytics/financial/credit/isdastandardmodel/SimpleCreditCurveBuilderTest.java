/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import org.testng.annotations.Test;

/**
 * Test.
 */
@Test
public class SimpleCreditCurveBuilderTest extends CreditCurveCalibrationTest {

  @SuppressWarnings("deprecation")
  private static ISDACompliantCreditCurveBuilder BUILDER_ISDA = new SimpleCreditCurveBuilder();
  @SuppressWarnings("deprecation")
  private static ISDACompliantCreditCurveBuilder BUILDER_MARKIT = new SimpleCreditCurveBuilder(MARKIT_FIX);

  @Test
  public void test() {
    testCalibrationAgainstISDA(BUILDER_ISDA, 1e-14);
    testCalibrationAgainstISDA(BUILDER_MARKIT, 1e-14);
  }

}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.convention.yield;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;



/**
 * Test.
 */
@Test
public class SimpleYieldConventionTest {

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName() {
    new SimpleYieldConvention(null);
  }

  @Test
  public void test() {
    final String name = "CONV";
    final SimpleYieldConvention convention = new SimpleYieldConvention(name);
    assertEquals(convention.getName(), name);
  }
}

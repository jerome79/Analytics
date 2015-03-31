/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import static org.testng.AssertJUnit.assertEquals;

import java.util.List;

import com.opengamma.strata.collect.tuple.DoublesPair;

/**
 * Collection of useful stuff to stop repeating code it tests
 */
public abstract class TestUtils {

  public static void assertSensitivityEquals(List<DoublesPair> expected, List<DoublesPair> actual, double tol) {
    assertEquals(expected.size(), actual.size(), 0);
    for (int i = 0; i < expected.size(); i++) {
      assertEquals(expected.get(i).getFirst(), actual.get(i).getFirst(), 0.0);
      assertEquals(expected.get(i).getSecond(), actual.get(i).getSecond(), tol);
    }
  }

}

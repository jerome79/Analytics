/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation.data;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;
import org.testng.annotations.Test;

import com.opengamma.analytics.math.interpolation.InterpolatorNDTestCase;

/**
 * Test.
 */
@Test
public class RadialBasisFunctionInterpolatorDataBundleTest extends InterpolatorNDTestCase {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFunction() {
    new RadialBasisFunctionInterpolatorDataBundle(FLAT_DATA, null, false);
  }

  @Override
  protected RandomEngine getRandom() {
    return RANDOM;
  }

}

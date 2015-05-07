/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.PiecewiseMixedLogNormalFitter;
import com.opengamma.analytics.financial.model.volatility.smile.function.MixedLogNormalVolatilityFunction;
import com.opengamma.analytics.math.function.Function1D;


/**
 * Test.
 */
@Test
public class PiecewiseMixLogNormalFitterTest {

  private static final double FORWARD = 1172.011012;
  private static final double EXPIRY = 1.5;
  private static final double[] STRIKES = new double[] {782.9777301, 982.3904005, 1242.99164, 1547.184937, 1854.305534 };
  private static final PiecewiseMixedLogNormalFitter FITTER = new PiecewiseMixedLogNormalFitter();
  
  public void flatTest() {
    final int n = STRIKES.length;
    final double[] vols = new double[n];
    Arrays.fill(vols, 0.2);
    final Function1D<Double, Double> smile = FITTER.getVolatilityFunction(FORWARD, STRIKES, EXPIRY, vols);
    for (int i = 0; i < 200; i++) {
      final double k = 700 + 1300 * i / 199.;
      final double vol = smile.evaluate(k);
      assertEquals(0.2, vol, 1e-9);
    }
  }

}

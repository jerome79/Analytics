/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.surface;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.analytics.math.FunctionUtils;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;

/**
 * Test.
 */
@Test
public class BlackVolatilitySurfaceConverterTest {

  private static final BlackVolatilitySurfaceDelta DELTA_SURFACE;
  private static final BlackVolatilitySurfaceStrike STRIKE_SURFACE;
  private static final ForwardCurve FORWARD_CURVE;
  private static final double SPOT = 167;
  private static final double DRIFT = 0.03;

  static {
    FORWARD_CURVE = new ForwardCurve(SPOT, DRIFT);
    Function<Double, Double> func = new Function<Double, Double>() {
      @Override
      public Double evaluate(Double... td) {
        double delta = td[1];
        return 0.2 + 2.0 * FunctionUtils.square(delta - 0.4);
      }
    };
    DELTA_SURFACE = new BlackVolatilitySurfaceDelta(FunctionalDoublesSurface.from(func), FORWARD_CURVE);

    Function<Double, Double> func2 = new Function<Double, Double>() {
      final SABRHaganVolatilityFunction sabr = new SABRHaganVolatilityFunction();

      @Override
      public Double evaluate(Double... tk) {
        double t = tk[0];
        double k = tk[1];

        double alpha = 0.3 * Math.exp(-0.3 * t) + 0.2;
        double beta = 1.0;
        double rho = -0.4;
        double nu = 0.6;
        return sabr.getVolatility(FORWARD_CURVE.getForward(t), k, t, alpha, beta, rho, nu);
      }
    };

    STRIKE_SURFACE = new BlackVolatilitySurfaceStrike(FunctionalDoublesSurface.from(func2));

  }

  public void testValues() {
    for (int t = 0; t < 10; t++) {
      assertEquals(0.2, DELTA_SURFACE.getVolatilityForDelta(t, 0.4), 1e-6);
    }
  }

  @Test
  public void roundTripTest() {
    deltaToStrikeToDelta(DELTA_SURFACE);
  }

  private void deltaToStrikeToDelta(final BlackVolatilitySurfaceDelta originalDeltaSurface) {
    final BlackVolatilitySurfaceStrike strikeSurface = BlackVolatilitySurfaceConverter.toStrikeSurface(originalDeltaSurface);
    BlackVolatilitySurfaceDelta newDeltaSurface = BlackVolatilitySurfaceConverter.toDeltaSurface(strikeSurface, originalDeltaSurface.getForwardCurve());
    for (int i = 0; i < 10; i++) {
      double t = Math.exp(i / 4.0) - 0.95;
      for (int j = 0; j < 21; j++) {
        double delta = 0.01 + 0.98 * j / 20.;
        double vol1 = originalDeltaSurface.getVolatilityForDelta(t, delta);
        double vol2 = newDeltaSurface.getVolatilityForDelta(t, delta);
        assertEquals(vol1, vol2, 1e-9);
      }
    }
  }

  /**
   * The strike surface is from the Hagan SABR formula which is well known to exhibit arbitrage (that is negative prices of butterflies or equivalently a negative implied density
   * at some strike)  for extreme strikes (the problem gets worse at time-to-expiry is increased).<p>
   * The round trip of strike to delta to strike surface CANNOT work at a strike where there is an arbitrage in the original surface.
   */
  public void roundTripTest2() {
    BlackVolatilitySurfaceDelta deltaSurface = BlackVolatilitySurfaceConverter.toDeltaSurface(STRIKE_SURFACE, FORWARD_CURVE);
    BlackVolatilitySurfaceStrike strikeSurface = BlackVolatilitySurfaceConverter.toStrikeSurface(deltaSurface);
    for (int i = 0; i < 10; i++) {
      double t = Math.exp(i / 4.0) - 0.95;
      double rootT = Math.sqrt(t);
      for (int j = 0; j < 10; j++) {
        double k = SPOT * Math.exp(0.3 * rootT * (-4.5 + 6.0 * j / 9.));
        double vol1 = STRIKE_SURFACE.getVolatility(t, k);
        double vol2 = strikeSurface.getVolatility(t, k);
        assertEquals(vol1, vol2, 1e-12);
      }
    }
  }
}

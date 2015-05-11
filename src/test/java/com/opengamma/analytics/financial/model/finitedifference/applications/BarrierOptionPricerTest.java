/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference.applications;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.option.definition.Barrier;
import com.opengamma.analytics.financial.model.option.definition.Barrier.BarrierType;
import com.opengamma.analytics.financial.model.option.definition.Barrier.KnockType;
import com.opengamma.analytics.financial.model.option.definition.Barrier.ObservationType;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackBarrierPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;

/**
 * Test.
 */
@Test
public class BarrierOptionPricerTest {
  private static final double SPOT = 100;
  private static final double REBATE = 3.0;

  private static final double EXPIRY = 0.5;
  private static final double R = 0.08;
  private static final double B = 0.04;
  private static final double SIGMA = 0.3;
  private static final BarrierOptionPricer PRICER = new BarrierOptionPricer(100, 50, 0.5, 0.1);
  private static final BlackBarrierPriceFunction ANAL_PRICER = BlackBarrierPriceFunction.getInstance();

  public void outBarrierTest() {
    for (int i = 0; i < 5; i++) {
      final double strike = SPOT * (0.8 + i * 0.1);
      for (int j = 0; j < 5; j++) {
        final double h = SPOT * (0.9 + j * 0.05);
        for (int k = 0; k < 2; k++) {
          final boolean isCall = k == 0;
          final BarrierType bt = SPOT > h ? BarrierType.DOWN : BarrierType.UP;
          final double anPrice = ANAL_PRICER.getPrice(new EuropeanVanillaOption(strike, EXPIRY, isCall),
              new Barrier(KnockType.OUT, bt, ObservationType.CONTINUOUS, h), REBATE, SPOT, B, R, SIGMA);
          final double fdPrice = PRICER.outBarrier(SPOT, h, strike, EXPIRY, R, B, SIGMA, isCall, REBATE);

          //TODO not splendid accuracy here - need to use discrete versions of r,b and sigma on the fd-grid to improve things
          assertEquals(anPrice, fdPrice, 5e-2);
        }
      }
    }
  }

  public void inBarrierTest() {
    for (int i = 0; i < 5; i++) {
      final double strike = SPOT * (0.9 + i * 0.1);
      for (int j = 0; j < 5; j++) {
        final double h = SPOT * (0.9 + j * 0.05);
        for (int k = 0; k < 2; k++) {
          final boolean isCall = k == 0;
          final BarrierType bt = SPOT > h ? BarrierType.DOWN : BarrierType.UP;
          final double anPrice = ANAL_PRICER.getPrice(new EuropeanVanillaOption(strike, EXPIRY, isCall), new Barrier(KnockType.IN, bt, ObservationType.CONTINUOUS, h), REBATE, SPOT, B, R, SIGMA);
          final double fdPrice = PRICER.inBarrier(SPOT, h, strike, EXPIRY, R, B, SIGMA, isCall, REBATE);

          //TODO not splendid accuracy here - need to use discrete versions of r,b and sigma on the fd-grid to improve things
          assertEquals(anPrice, fdPrice, 5e-2);
        }
      }
    }
  }

  public void barrierTest() {
    final double spot = 100.0;
    final double expiry = 2.0;
    final boolean isCall = true;
    final double strike = 100.0;
    final double barrerLevel = 95.0;
    final double rebate = 3.0;
    final double r = 0.08;
    final double b = 0.04;
    final double sigma = 0.3;

    final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, expiry, isCall);
    final Barrier barrierIn = new Barrier(KnockType.IN, BarrierType.DOWN, ObservationType.CONTINUOUS, barrerLevel);
    final double p1In = PRICER.getPrice(option, barrierIn, rebate, spot, b, r, sigma);
    final double p2In = PRICER.inBarrier(spot, barrerLevel, strike, expiry, r, b, sigma, isCall, rebate);
    assertEquals(p1In, p2In, 1e-20);
    final Barrier barrierOut = new Barrier(KnockType.OUT, BarrierType.DOWN, ObservationType.CONTINUOUS, barrerLevel);
    final double p1Out = PRICER.getPrice(option, barrierOut, rebate, spot, b, r, sigma);
    final double p2Out = PRICER.outBarrier(spot, barrerLevel, strike, expiry, r, b, sigma, isCall, rebate);
    assertEquals(p1Out, p2Out, 1e-20);
  }

}

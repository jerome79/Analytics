/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference.applications;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.option.pricing.analytic.BjerksundStenslandModel;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;

/**
 * Test.
 */
@Test
public class BlackScholesMertonPDEPricerTest {
  private static final BjerksundStenslandModel AMERICAN_APPOX_PRCIER = new BjerksundStenslandModel();
  private static final BlackScholesMertonPDEPricer PRICER = new BlackScholesMertonPDEPricer();

  /**
   * Test that a wide range of European options price to reasonable accuracy on a moderately sized grid
   */
  public void europeanTest() {
    final double s0 = 10.0;
    final double[] kSet = {7.0, 9.0, 10.0, 13.0, 17.0 };
    final double[] rSet = {0.0, 0.04, 0.2 };
    final double[] qSet = {-0.05, 0.0, 0.1 };
    final double[] tSet = {0.1, 2.0 };
    final double sigma = 0.3;
    final boolean[] isCallSet = {true, false };

    final int tSteps = 100;
    final int nu = 80;
    final int sSteps = nu * tSteps;

    for (final double k : kSet) {
      for (final double r : rSet) {
        for (final double q : qSet) {
          final double b = r - q;
          for (final double t : tSet) {
            for (final boolean isCall : isCallSet) {
              final double bsPrice = Math.exp(-r * t) * BlackFormulaRepository.price(s0 * Math.exp(b * t), k, t, sigma, isCall);
              final double pdePDE = PRICER.price(s0, k, r, b, t, sigma, isCall, false, sSteps, tSteps);
              final double absErr = Math.abs(pdePDE - bsPrice);
              final double relErr = Math.abs(1 - pdePDE / bsPrice);

              if (k < 17.0) {
                assertTrue(k + "\t" + r + "\t" + q + "\t" + t + "\t" + isCall + "\t" + bsPrice + "\t" + pdePDE + "\t" + absErr + "\t" + relErr, absErr < 3e-5);
              } else {
                assertTrue(k + "\t" + r + "\t" + q + "\t" + t + "\t" + isCall + "\t" + bsPrice + "\t" + pdePDE + "\t" + absErr + "\t" + relErr, absErr < 1e-4);
              }
            }
          }
        }
      }
    }
  }

  /**
   * Test that a wide range of American options price to within the accuracy of the Bjerksund-Stensland approximation on a moderately sized grid
   */
  public void americanTest() {
    final double s0 = 10.0;
    final double[] kSet = {7.0, 9.0, 10.0, 13.0, 17.0 };
    final double[] rSet = {0.0, 0.04, 0.2 };
    final double[] qSet = {-0.05, 0.0, 0.1 };
    final double[] tSet = {0.05, 0.25 };
    final double sigma = 0.3;
    final boolean[] isCallSet = {true, false };

    // The Bjerksund-Stensland approximation is not that accurate, so there is no point using a fine grid for this test
    final int tSteps = 80;
    final int nu = 20;
    final int sSteps = nu * tSteps;

    for (final double k : kSet) {
      for (final double r : rSet) {
        for (final double q : qSet) {
          final double b = r - q;
          for (final double t : tSet) {
            for (final boolean isCall : isCallSet) {
              final double bsPrice = Math.exp(-r * t) * BlackFormulaRepository.price(s0 * Math.exp(b * t), k, t, sigma, isCall);
              final double amAprox = AMERICAN_APPOX_PRCIER.price(s0, k, r, b, t, sigma, isCall);
              final double pdePrice = PRICER.price(s0, k, r, b, t, sigma, isCall, true, sSteps, tSteps);

              // Bjerksund-Stensland approximation set a lower limit for the price of an American option, thus the PDE price should always exceed it
              assertTrue(k + "\t" + r + "\t" + q + "\t" + t + "\t" + isCall + "\t" + bsPrice + "\t" + amAprox + "\t" + pdePrice, (pdePrice - amAprox) > -5e-6);

              final double absErr = Math.abs(pdePrice - amAprox);
              final double relErr = Math.abs(1 - pdePrice / amAprox);

              assertTrue(k + "\t" + r + "\t" + q + "\t" + t + "\t" + isCall + "\t" + amAprox + "\t" + pdePrice + "\t" + absErr + "\t" + relErr, absErr < 1e-2);
            }
          }
        }
      }
    }
  }

  public void nonuniformGridTest() {
    final double s0 = 10.0;
    final double k = 13.0;
    final double r = 0.06;
    final double b = 0.04;
    final double t = 1.75;
    final double sigma = 0.5;
    final boolean isCall = false;

    final double beta = 0.01;
    final double lambda = 0.0;
    final double sd = 6.0;

    final int tSteps = 100;
    final double nu = 10;
    final int sSteps = (int) (nu * tSteps);

    final double bsPrice = Math.exp(-r * t) * BlackFormulaRepository.price(s0 * Math.exp(b * t), k, t, sigma, isCall);
    final double pdePrice1 = PRICER.price(s0, k, r, b, t, sigma, isCall, false, sSteps, tSteps);
    final double pdePrice2 = PRICER.price(s0, k, r, b, t, sigma, isCall, false, sSteps, tSteps, beta, lambda, sd);
    final double relErr1 = Math.abs(pdePrice1 / bsPrice - 1.0);
    final double relErr2 = Math.abs(pdePrice2 / bsPrice - 1.0);

    assertEquals(0, relErr1, 5e-4);
    assertEquals(0, relErr2, 2e-6); // much better accuracy with non-uniform
  }

}

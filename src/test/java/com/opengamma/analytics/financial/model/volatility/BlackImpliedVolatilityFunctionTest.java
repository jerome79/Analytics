/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;


/**
 * Test.
 */
@Test
public class BlackImpliedVolatilityFunctionTest {
  private static final double FORWARD = 134.5;
  private static final double DF = 0.87;
  private static final double T = 4.5;
  private static final double SIGMA = 0.2;
  private static final BlackFunctionData[] DATA;
  private static final EuropeanVanillaOption[] OPTIONS;
  private static final double[] PRICES;
  private static final double[] STRIKES;
  private static final BlackPriceFunction FORMULA = new BlackPriceFunction();
  private static final SABRHaganVolatilityFunction SABR = new SABRHaganVolatilityFunction();
  private static final SABRFormulaData SABR_DATA;
  private static final int N = 10;

  static {
    PRICES = new double[N];
    STRIKES = new double[N];
    DATA = new BlackFunctionData[N];
    OPTIONS = new EuropeanVanillaOption[N];
    for (int i = 0; i < 10; i++) {
      STRIKES[i] = FORWARD - 20 + 40 / N * i;
      DATA[i] = new BlackFunctionData(FORWARD, DF, SIGMA);
      OPTIONS[i] = new EuropeanVanillaOption(STRIKES[i], T, true);
      PRICES[i] = FORMULA.getPriceFunction(OPTIONS[i]).evaluate(DATA[i]);
    }

    double beta = 0.6;
    double alpha = Math.pow(SIGMA, 1 - beta);
    double rho = -0.3;
    double nu = 0.4;
    SABR_DATA = new SABRFormulaData(alpha, beta, rho, nu);
  }

  public void test() {
    final BlackImpliedVolatilityFormula formula = new BlackImpliedVolatilityFormula();
    for (int i = 0; i < N; i++) {
      final double vol = formula.getImpliedVolatility(DATA[i], OPTIONS[i], PRICES[i]);
      assertEquals(SIGMA, vol, 1e-6);
    }
  }

  public void flatTest() {
    final double rootT = Math.sqrt(T);
    for (int i = 0; i < 51; i++) {
      double d = -5 + 12.0 * i / 50.;
      double k = FORWARD * Math.exp(d * rootT);
      boolean isCall = k >= FORWARD;

      double price = BlackFormulaRepository.price(FORWARD, k, T, SIGMA, isCall);
      double impVol = BlackFormulaRepository.impliedVolatility(price, FORWARD, k, T, isCall);
      assertEquals(SIGMA, impVol, 1e-6);
    }
  }

  public void sabrTest() {
    final double rootT = Math.sqrt(T);
    //this has a lowest price of 4e-18
    for (int i = 0; i < 51; i++) {
      double d = -9.0 + 12.0 * i / 50.;
      double k = FORWARD * Math.exp(d * rootT);
      boolean isCall = k >= FORWARD;
      double vol = SABR.getVolatility(new EuropeanVanillaOption(k, T, true), FORWARD, SABR_DATA);
      double price = BlackFormulaRepository.price(FORWARD, k, T, vol, isCall);
      double impVol = BlackFormulaRepository.impliedVolatility(price, FORWARD, k, T, isCall);
      assertEquals(vol, impVol, 1e-8);
    }
    //this has a lowest price of 1e-186
    for (int i = 0; i < 21; i++) {
      double d = 3.0 + 4.0 * i / 20.;
      double k = FORWARD * Math.exp(d * rootT);
      boolean isCall = k >= FORWARD;
      double vol = SABR.getVolatility(new EuropeanVanillaOption(k, T, true), FORWARD, SABR_DATA);
      double price = BlackFormulaRepository.price(FORWARD, k, T, vol, isCall);
      double impVol = BlackFormulaRepository.impliedVolatility(price, FORWARD, k, T, isCall);
      assertEquals(vol, impVol, 1e-3);
    }

  }

}

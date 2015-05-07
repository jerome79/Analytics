/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.fourier;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.BlackImpliedVolatilityFormula;

/**
 * Test.
 */
@Test
public class HestonFourierPricerTest {
  private static final double FORWARD = 0.04;
  private static final double T = 2.0;
  private static final double DF = 0.93;
  private static final BlackImpliedVolatilityFormula BLACK_IMPLIED_VOL = new BlackImpliedVolatilityFormula();

  public void testLowVolOfVol() {
    final double sigma = 0.36;

    final double kappa = 1.0; // mean reversion speed
    final double theta = sigma * sigma; // reversion level
    final double vol0 = theta; // start level
    final double omega = 0.001; // vol-of-vol
    final double rho = -0.3; // correlation

    final MartingaleCharacteristicExponent heston = new HestonCharacteristicExponent(kappa, theta, vol0, omega, rho);
    final FourierPricer pricer = new FourierPricer();

    for (int i = 0; i < 21; i++) {
      final double k = 0.2 + 3.0 * i / 20.0;
      final EuropeanVanillaOption option = new EuropeanVanillaOption(k * FORWARD, T, true);
      final BlackFunctionData data = new BlackFunctionData(FORWARD, DF, 0);
      final double price = pricer.price(data, option, heston, -0.5, 1e-6);
      final double impVol = BLACK_IMPLIED_VOL.getImpliedVolatility(data, option, price);
      assertEquals(sigma, impVol, 1e-3);
    }
  }

  public void testHeston() {
    final double alpha = 0.75;

    // parameters from the paper Not-so-complex logarithms in the Heston model
    final double kappa = 1.0; // mean reversion speed
    final double theta = 0.16; // reversion level
    final double vol0 = theta; // start level
    final double omega = 2; // vol-of-vol
    final double rho = -0.8; // correlation

    final double t = 1 / 12.0;

    final MartingaleCharacteristicExponent heston = new HestonCharacteristicExponent(kappa, theta, vol0, omega, rho);
    final FourierPricer pricer = new FourierPricer();
    final BlackFunctionData data = new BlackFunctionData(1, 1, 0);
    for (int i = 0; i < 11; i++) {
      final double k = 0.5 + 1.0 * i / 10.0;

      final EuropeanVanillaOption option = new EuropeanVanillaOption(k, t, true);
      final double price = pricer.price(data, option, heston, alpha, 1e-8);
      BLACK_IMPLIED_VOL.getImpliedVolatility(data, option, price);

    }
  }

  public void testHestonModelGreeks() {

    final FourierModelGreeks modelGreek = new FourierModelGreeks();

    final double alpha = -0.5;

    final double kappa = 1.0; // mean reversion speed
    final double theta = 0.16; // reversion level
    final double vol0 = theta; // start level
    final double omega = 2; // vol-of-vol
    final double rho = -0.8; // correlation

    final double forward = 1.0;
    final double t = 1 / 12.0;

    final MartingaleCharacteristicExponent heston = new HestonCharacteristicExponent(kappa, theta, vol0, omega, rho);
    final FourierPricer pricer = new FourierPricer();
    final BlackFunctionData data = new BlackFunctionData(forward, 1, 0);

    boolean isCall;
    for (int i = 0; i < 11; i++) {
      final double k = 0.7 + 0.6 * i / 10.0;
      isCall = k >= forward;
      final EuropeanVanillaOption option = new EuropeanVanillaOption(k, t, isCall);
      final double[] sense = modelGreek.getGreeks(data, option, heston, alpha, 1e-12);

      final double price = pricer.price(data, option, heston, alpha, 1e-8);
      final double[] fdSense = finiteDifferenceModelGreeks((HestonCharacteristicExponent) heston, pricer, data, option);
      for (int index = 0; index < 5; index++) {
        assertEquals(fdSense[index], sense[index], 1e-3 * price);
      }
    }
  }

  public static double[] finiteDifferenceModelGreeks(final HestonCharacteristicExponent ce, final FourierPricer pricer, final BlackFunctionData data, final EuropeanVanillaOption option) {
    final double eps = 1e-5;
    final double tol = 1e-13;
    final double alpha = -0.5;
    final double[] res = new double[5];
    //kappa
    HestonCharacteristicExponent ceTemp = new HestonCharacteristicExponent(ce.getKappa() + eps, ce.getTheta(), ce.getVol0(), ce.getOmega(), ce.getRho());
    double up = pricer.price(data, option, ceTemp, alpha, tol);
    ceTemp = new HestonCharacteristicExponent(ce.getKappa() - eps, ce.getTheta(), ce.getVol0(), ce.getOmega(), ce.getRho());
    double down = pricer.price(data, option, ceTemp, alpha, tol);
    res[0] = (up - down) / 2 / eps;
    //theta
    ceTemp = new HestonCharacteristicExponent(ce.getKappa(), ce.getTheta() + eps, ce.getVol0(), ce.getOmega(), ce.getRho());
    up = pricer.price(data, option, ceTemp, alpha, tol);
    ceTemp = new HestonCharacteristicExponent(ce.getKappa(), ce.getTheta() - eps, ce.getVol0(), ce.getOmega(), ce.getRho());
    down = pricer.price(data, option, ceTemp, alpha, tol);
    res[1] = (up - down) / 2 / eps;
    //vol0
    ceTemp = new HestonCharacteristicExponent(ce.getKappa(), ce.getTheta(), ce.getVol0() + eps, ce.getOmega(), ce.getRho());
    up = pricer.price(data, option, ceTemp, alpha, tol);
    ceTemp = new HestonCharacteristicExponent(ce.getKappa(), ce.getTheta(), ce.getVol0() - eps, ce.getOmega(), ce.getRho());
    down = pricer.price(data, option, ceTemp, alpha, tol);
    res[2] = (up - down) / 2 / eps;
    //omega
    ceTemp = new HestonCharacteristicExponent(ce.getKappa(), ce.getTheta(), ce.getVol0(), ce.getOmega() + eps, ce.getRho());
    up = pricer.price(data, option, ceTemp, alpha, tol);
    ceTemp = new HestonCharacteristicExponent(ce.getKappa(), ce.getTheta(), ce.getVol0(), ce.getOmega() - eps, ce.getRho());
    down = pricer.price(data, option, ceTemp, alpha, tol);
    res[3] = (up - down) / 2 / eps;
    //rho
    ceTemp = new HestonCharacteristicExponent(ce.getKappa(), ce.getTheta(), ce.getVol0(), ce.getOmega(), ce.getRho() + eps);
    up = pricer.price(data, option, ceTemp, alpha, tol);
    ceTemp = new HestonCharacteristicExponent(ce.getKappa(), ce.getTheta(), ce.getVol0(), ce.getOmega(), ce.getRho() - eps);
    down = pricer.price(data, option, ceTemp, alpha, tol);
    res[4] = (up - down) / 2 / eps;

    return res;
  }

}

/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Random;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.smile.function.MixedBivariateLogNormalModelVolatility;
import com.opengamma.analytics.financial.model.volatility.smile.function.MixedLogNormalModelData;
import com.opengamma.analytics.financial.model.volatility.smile.function.MixedLogNormalVolatilityFunction;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Test.
 */
@Test
public class MixedBivariateLogNormalFitterTest {
  private final static double INF = 1. / 0.;

  /**
   * EPS_1 =EPS_2 = 1.E-14 should be chosen for this test
   */
  public void recoveryTest() {
    final int nNorms = 2;
    final int nParams = 5 * nNorms - 3;
    final int nDataPts = 10;
    final int nDataPtsX = 5;
    final int nParamsX = 3 * nNorms - 2;
    final int nParamsY = 3 * nNorms - 2;

    final double fwdX = 1.;
    final double fwdY = 1.;
    final double time = 1.0;

    final double[] yy = new double[nDataPts];
    double[] aaGuess1 = new double[nParams];
    final double[] aaGuess1X = new double[nParamsX];
    final double[] aaGuess1Y = new double[nParamsY];

    final double[] inRelativePartialForwardsX = {1., 1. };
    final double[] inRelativePartialForwardsY = {Math.exp(-0.2), (1. - Math.exp(-0.2) * 0.7) / 0.3 };

    final double[] inSigmasX = {0.25, 0.7 };
    final double[] inSigmasY = {0.3, 0.5 };

    final double[] inWeights = {0.7, 0.3 };

    final MixedLogNormalModelData inObjX = new MixedLogNormalModelData(inWeights, inSigmasX, inRelativePartialForwardsX);
    final MixedLogNormalModelData inObjY = new MixedLogNormalModelData(inWeights, inSigmasY, inRelativePartialForwardsY);

    final double[] xx = new double[] {0.9, 0.95, 1.0, 1.05, 1.1, 0.9, 0.95, 1.0, 1.05, 1.15 };

    final MixedLogNormalVolatilityFunction volfunc = MixedLogNormalVolatilityFunction.getInstance();
    Arrays.fill(yy, 0.);

    for (int j = 0; j < nDataPtsX; ++j) {
      final EuropeanVanillaOption option = new EuropeanVanillaOption(xx[j], time, true);
      yy[j] = volfunc.getVolatility(option, fwdX, inObjX);
    }

    for (int j = nDataPtsX; j < nDataPts; ++j) {
      final EuropeanVanillaOption option = new EuropeanVanillaOption(xx[j], time, true);
      yy[j] = volfunc.getVolatility(option, fwdY, inObjY);
    }

    for (int i = 0; i < nParams; ++i) {
      aaGuess1[i] = 0.5;
    }
    final MixedBivariateLogNormalFitter fitter1 = new MixedBivariateLogNormalFitter();

    for (int i = 0; i < nNorms; ++i) {
      aaGuess1X[i] = aaGuess1[i];
      aaGuess1Y[i] = aaGuess1[i + nNorms];
    }
    for (int i = 0; i < nNorms - 1; ++i) {
      aaGuess1X[i + nNorms] = aaGuess1[i + 2 * nNorms];
      aaGuess1X[i + 2 * nNorms - 1] = aaGuess1[i + 3 * nNorms - 1];
      aaGuess1Y[i + nNorms] = aaGuess1[i + 2 * nNorms];
      aaGuess1Y[i + 2 * nNorms - 1] = aaGuess1[i + 4 * nNorms - 2];
    }

    fitter1.doFit(aaGuess1, xx, yy, time, fwdX, fwdY, nNorms, nDataPtsX, 1);
    aaGuess1 = fitter1.getParams();

    for (int i = 0; i < nNorms; ++i) {
      aaGuess1X[i] = aaGuess1[i];
      aaGuess1Y[i] = aaGuess1[i + nNorms];
    }
    for (int i = 0; i < nNorms - 1; ++i) {
      aaGuess1X[i + nNorms] = aaGuess1[i + 2 * nNorms];
      aaGuess1X[i + 2 * nNorms - 1] = aaGuess1[i + 3 * nNorms - 1];
      aaGuess1Y[i + nNorms] = aaGuess1[i + 2 * nNorms];
      aaGuess1Y[i + 2 * nNorms - 1] = aaGuess1[i + 4 * nNorms - 2];
    }

    for (int i = 0; i < nNorms; ++i) {
      aaGuess1X[i] = aaGuess1[i];
      aaGuess1Y[i] = aaGuess1[i + nNorms];
    }
    for (int i = 0; i < nNorms - 1; ++i) {
      aaGuess1X[i + nNorms] = aaGuess1[i + 2 * nNorms];
      aaGuess1X[i + 2 * nNorms - 1] = aaGuess1[i + 3 * nNorms - 1];
      aaGuess1Y[i + nNorms] = aaGuess1[i + 2 * nNorms];
      aaGuess1Y[i + 2 * nNorms - 1] = aaGuess1[i + 4 * nNorms - 2];
    }

    final MixedLogNormalModelData objAns1X = new MixedLogNormalModelData(aaGuess1X, true);
    final double[] weights = objAns1X.getWeights();
    final double[] sigmasX = objAns1X.getVolatilities();
    final double[] relativePartialForwardsX = objAns1X.getRelativeForwards();

    final MixedLogNormalModelData objAns1Y = new MixedLogNormalModelData(aaGuess1Y, true);
    final double[] sigmasY = objAns1Y.getVolatilities();
    final double[] relativePartialForwardsY = objAns1Y.getRelativeForwards();

    for (int i = 0; i < nNorms; ++i) {

      assertEquals(weights[i], inWeights[i], Math.abs((inWeights[0] + inWeights[1]) / 2.) * 1e-9);
    }
    for (int i = 0; i < nNorms; ++i) {

      assertEquals(sigmasX[i], inSigmasX[i], Math.abs((inSigmasX[0] + inSigmasX[1]) / 2.) * 1e-9);
    }
    for (int i = 0; i < nNorms; ++i) {

      assertEquals(sigmasY[i], inSigmasY[i], Math.abs((inSigmasY[0] + inSigmasY[1]) / 2.) * 1e-9);
    }
    for (int i = 0; i < nNorms; ++i) {
      assertEquals(relativePartialForwardsX[i], inRelativePartialForwardsX[i], Math.abs((inRelativePartialForwardsX[0] + inRelativePartialForwardsX[1]) / 2.) * 1e-11);
    }
    for (int i = 0; i < nNorms; ++i) {
      assertEquals(relativePartialForwardsY[i], inRelativePartialForwardsY[i], Math.abs((inRelativePartialForwardsY[0] + inRelativePartialForwardsY[1]) / 2.) * 1e-9);
    }

    final double[] ansVolsX = new double[100];
    final double[] ansVolsY = new double[100];
    for (int i = 0; i < 100; i++) {
      final double k = fwdX * (0.1 + 2. * i / 100.);
      final EuropeanVanillaOption option = new EuropeanVanillaOption(k, time, true);
      ansVolsX[i] = volfunc.getVolatility(option, fwdX, objAns1X);
      ansVolsY[i] = volfunc.getVolatility(option, fwdY, objAns1Y);
    }

    final double[] trueVolsX = new double[100];
    final double[] trueVolsY = new double[100];
    for (int i = 0; i < 100; i++) {
      final double k = fwdY * (0.1 + 2. * i / 100.);
      final EuropeanVanillaOption option = new EuropeanVanillaOption(k, time, true);
      trueVolsX[i] = volfunc.getVolatility(option, fwdX, inObjX);
      trueVolsY[i] = volfunc.getVolatility(option, fwdY, inObjY);
    }

    for (int i = 0; i < 100; i++) {
      assertEquals(ansVolsX[i], trueVolsX[i], Math.abs((inSigmasX[0] + inSigmasX[1]) / 2.) * 1e-8);
      assertEquals(ansVolsY[i], trueVolsY[i], Math.abs((inSigmasY[0] + inSigmasY[1]) / 2.) * 1e-8);
    }

    final double[] ansDensityX = new double[100];
    final double[] ansDensityY = new double[100];
    final double[] trueDensityX = new double[100];
    final double[] trueDensityY = new double[100];
    for (int i = 0; i < 100; i++) {
      final double k = fwdX * (0.01 + 2. * i / 100.);
      final EuropeanVanillaOption option = new EuropeanVanillaOption(k, time, true);
      ansDensityX[i] = getDualGamma(option, fwdX, objAns1X);
      trueDensityX[i] = getDualGamma(option, fwdX, inObjX);
      assertEquals(ansDensityX[i], trueDensityX[i], 1e-9);
    }

    for (int i = 0; i < 100; i++) {
      final double k = fwdY * (0.01 + 2. * i / 100.);
      final EuropeanVanillaOption option = new EuropeanVanillaOption(k, time, true);
      ansDensityY[i] = getDualGamma(option, fwdY, objAns1Y);
      trueDensityY[i] = getDualGamma(option, fwdY, inObjY);
      assertEquals(ansDensityY[i], trueDensityY[i], 1e-9);
    }

    for (int i = 0; i < 100; i++) {
      final double k = fwdY * (0.01 + 2. * i / 100.);
      final EuropeanVanillaOption optionCall = new EuropeanVanillaOption(k, time, true);
      final EuropeanVanillaOption optionPut = new EuropeanVanillaOption(k, time, false);
      final double callPrice = getPrice(optionCall, fwdX, objAns1X);
      final double putPrice = getPrice(optionPut, fwdX, objAns1X);
      assertEquals((callPrice - putPrice), (fwdX - k), fwdX * 1e-12);
    }

    for (int i = 0; i < 100; i++) {
      final double k = fwdY * (0.01 + 2. * i / 100.);
      final EuropeanVanillaOption optionCall = new EuropeanVanillaOption(k, time, true);
      final EuropeanVanillaOption optionPut = new EuropeanVanillaOption(k, time, false);
      final double callPrice = getPrice(optionCall, fwdY, objAns1Y);
      final double putPrice = getPrice(optionPut, fwdY, objAns1Y);
      assertEquals((callPrice - putPrice), (fwdY - k), fwdY * 1e-12);
    }

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullaaGuessTest() {
    final int nNorms = 2;
    final int nDataPtsX = 5;

    final double fwdX = 1.;
    final double fwdY = 1.;
    final double time = 1.0;

    final double[] aaGuess1 = null;
    final double[] xx = new double[] {0.9, 0.95, 1.0, 1.05, 1.1, 0.9, 0.95, 1.0, 1.05, 1.15 };
    final double[] yy = new double[] {0.09, 0.085, 0.08, 0.03105, 0.091, 0.09, 0.075, 0.066, 0.705, 0.115 };

    final MixedBivariateLogNormalFitter fitter1 = new MixedBivariateLogNormalFitter();
    fitter1.doFit(aaGuess1, xx, yy, time, fwdX, fwdY, nNorms, nDataPtsX, 1);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullxxTest() {
    final int nNorms = 2;
    final int nDataPtsX = 5;

    final double fwdX = 1.;
    final double fwdY = 1.;
    final double time = 1.0;

    final double[] aaGuess1 = new double[] {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7 };
    final double[] xx = null;
    final double[] yy = new double[] {0.09, 0.085, 0.08, 0.03105, 0.091, 0.09, 0.075, 0.066, 0.705, 0.115 };

    final MixedBivariateLogNormalFitter fitter1 = new MixedBivariateLogNormalFitter();
    fitter1.doFit(aaGuess1, xx, yy, time, fwdX, fwdY, nNorms, nDataPtsX, 1);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullyyTest() {
    final int nNorms = 2;
    final int nDataPtsX = 5;

    final double fwdX = 1.;
    final double fwdY = 1.;
    final double time = 1.0;

    final double[] aaGuess1 = new double[] {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7 };
    final double[] xx = new double[] {0.9, 0.95, 1.0, 1.05, 1.1, 0.9, 0.95, 1.0, 1.05, 1.15 };
    final double[] yy = null;

    final MixedBivariateLogNormalFitter fitter1 = new MixedBivariateLogNormalFitter();
    fitter1.doFit(aaGuess1, xx, yy, time, fwdX, fwdY, nNorms, nDataPtsX, 1);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void naNaaGuessTest() {
    final int nNorms = 2;
    final int nDataPtsX = 5;

    final double fwdX = 1.;
    final double fwdY = 1.;
    final double time = 1.0;

    final double[] aaGuess1 = new double[] {Double.NaN, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7 };
    final double[] xx = new double[] {0.9, 0.95, 1.0, 1.05, 1.1, 0.9, 0.95, 1.0, 1.05, 1.15 };
    final double[] yy = new double[] {0.09, 0.085, 0.08, 0.03105, 0.091, 0.09, 0.075, 0.066, 0.705, 0.115 };

    final MixedBivariateLogNormalFitter fitter1 = new MixedBivariateLogNormalFitter();
    fitter1.doFit(aaGuess1, xx, yy, time, fwdX, fwdY, nNorms, nDataPtsX, 1);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void naNxxTest() {
    final int nNorms = 2;
    final int nDataPtsX = 5;

    final double fwdX = 1.;
    final double fwdY = 1.;
    final double time = 1.0;

    final double[] aaGuess1 = new double[] {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7 };
    final double[] xx = new double[] {Double.NaN, 0.95, 1.0, 1.05, 1.1, 0.9, 0.95, 1.0, 1.05, 1.15 };
    final double[] yy = new double[] {0.09, 0.085, 0.08, 0.03105, 0.091, 0.09, 0.075, 0.066, 0.705, 0.115 };

    final MixedBivariateLogNormalFitter fitter1 = new MixedBivariateLogNormalFitter();
    fitter1.doFit(aaGuess1, xx, yy, time, fwdX, fwdY, nNorms, nDataPtsX, 1);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void naNyyTest() {
    final int nNorms = 2;
    final int nDataPtsX = 5;

    final double fwdX = 1.;
    final double fwdY = 1.;
    final double time = 1.0;

    final double[] aaGuess1 = new double[] {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7 };
    final double[] xx = new double[] {0.9, 0.95, 1.0, 1.05, 1.1, 0.9, 0.95, 1.0, 1.05, 1.15 };
    final double[] yy = new double[] {Double.NaN, 0.085, 0.08, 0.03105, 0.091, 0.09, 0.075, 0.066, 0.705, 0.115 };

    final MixedBivariateLogNormalFitter fitter1 = new MixedBivariateLogNormalFitter();
    fitter1.doFit(aaGuess1, xx, yy, time, fwdX, fwdY, nNorms, nDataPtsX, 1);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void naNtimeTest() {
    final int nNorms = 2;
    final int nDataPtsX = 5;

    final double fwdX = 1.;
    final double fwdY = 1.;
    final double time = Double.NaN;

    final double[] aaGuess1 = new double[] {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7 };
    final double[] xx = new double[] {0.9, 0.95, 1.0, 1.05, 1.1, 0.9, 0.95, 1.0, 1.05, 1.15 };
    final double[] yy = new double[] {0.09, 0.085, 0.08, 0.03105, 0.091, 0.09, 0.075, 0.066, 0.705, 0.115 };

    final MixedBivariateLogNormalFitter fitter1 = new MixedBivariateLogNormalFitter();
    fitter1.doFit(aaGuess1, xx, yy, time, fwdX, fwdY, nNorms, nDataPtsX, 1);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void naNfwdXTest() {
    final int nNorms = 2;
    final int nDataPtsX = 5;

    final double fwdX = Double.NaN;
    final double fwdY = 1.;
    final double time = 1.0;

    final double[] aaGuess1 = new double[] {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7 };
    final double[] xx = new double[] {0.9, 0.95, 1.0, 1.05, 1.1, 0.9, 0.95, 1.0, 1.05, 1.15 };
    final double[] yy = new double[] {0.09, 0.085, 0.08, 0.03105, 0.091, 0.09, 0.075, 0.066, 0.705, 0.115 };

    final MixedBivariateLogNormalFitter fitter1 = new MixedBivariateLogNormalFitter();
    fitter1.doFit(aaGuess1, xx, yy, time, fwdX, fwdY, nNorms, nDataPtsX, 1);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void naNfwdYTest() {
    final int nNorms = 2;
    final int nDataPtsX = 5;

    final double fwdX = 1.;
    final double fwdY = Double.NaN;
    final double time = 1.0;

    final double[] aaGuess1 = new double[] {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7 };
    final double[] xx = new double[] {0.9, 0.95, 1.0, 1.05, 1.1, 0.9, 0.95, 1.0, 1.05, 1.15 };
    final double[] yy = new double[] {0.09, 0.085, 0.08, 0.03105, 0.091, 0.09, 0.075, 0.066, 0.705, 0.115 };

    final MixedBivariateLogNormalFitter fitter1 = new MixedBivariateLogNormalFitter();
    fitter1.doFit(aaGuess1, xx, yy, time, fwdX, fwdY, nNorms, nDataPtsX, 1);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void naNparamsGuessCorrectionTest() {
    final int nNorms = 2;
    final int nDataPtsX = 5;
    final double paramsGuessCorrection = Double.NaN;

    final double fwdX = 1.;
    final double fwdY = 1.;
    final double time = 1.0;

    final double[] aaGuess1 = new double[] {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7 };
    final double[] xx = new double[] {0.9, 0.95, 1.0, 1.05, 1.1, 0.9, 0.95, 1.0, 1.05, 1.15 };
    final double[] yy = new double[] {0.09, 0.085, 0.08, 0.03105, 0.091, 0.09, 0.075, 0.066, 0.705, 0.115 };

    final MixedBivariateLogNormalFitter fitter1 = new MixedBivariateLogNormalFitter();
    fitter1.doFit(aaGuess1, xx, yy, time, fwdX, fwdY, nNorms, nDataPtsX, paramsGuessCorrection);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void iNFaaGuessTest() {
    final int nNorms = 2;
    final int nDataPtsX = 5;

    final double fwdX = 1.;
    final double fwdY = 1.;
    final double time = 1.0;

    final double[] aaGuess1 = new double[] {INF, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7 };
    final double[] xx = new double[] {0.9, 0.95, 1.0, 1.05, 1.1, 0.9, 0.95, 1.0, 1.05, 1.15 };
    final double[] yy = new double[] {0.09, 0.085, 0.08, 0.03105, 0.091, 0.09, 0.075, 0.066, 0.705, 0.115 };

    final MixedBivariateLogNormalFitter fitter1 = new MixedBivariateLogNormalFitter();
    fitter1.doFit(aaGuess1, xx, yy, time, fwdX, fwdY, nNorms, nDataPtsX, 1);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void iNFxxTest() {
    final int nNorms = 2;
    final int nDataPtsX = 5;

    final double fwdX = 1.;
    final double fwdY = 1.;
    final double time = 1.0;

    final double[] aaGuess1 = new double[] {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7 };
    final double[] xx = new double[] {INF, 0.95, 1.0, 1.05, 1.1, 0.9, 0.95, 1.0, 1.05, 1.15 };
    final double[] yy = new double[] {0.09, 0.085, 0.08, 0.03105, 0.091, 0.09, 0.075, 0.066, 0.705, 0.115 };

    final MixedBivariateLogNormalFitter fitter1 = new MixedBivariateLogNormalFitter();
    fitter1.doFit(aaGuess1, xx, yy, time, fwdX, fwdY, nNorms, nDataPtsX, 1);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void iNFyyTest() {
    final int nNorms = 2;
    final int nDataPtsX = 5;

    final double fwdX = 1.;
    final double fwdY = 1.;
    final double time = 1.0;

    final double[] aaGuess1 = new double[] {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7 };
    final double[] xx = new double[] {0.9, 0.95, 1.0, 1.05, 1.1, 0.9, 0.95, 1.0, 1.05, 1.15 };
    final double[] yy = new double[] {INF, 0.085, 0.08, 0.03105, 0.091, 0.09, 0.075, 0.066, 0.705, 0.115 };

    final MixedBivariateLogNormalFitter fitter1 = new MixedBivariateLogNormalFitter();
    fitter1.doFit(aaGuess1, xx, yy, time, fwdX, fwdY, nNorms, nDataPtsX, 1);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void iNFtimeTest() {
    final int nNorms = 2;
    final int nDataPtsX = 5;

    final double fwdX = 1.;
    final double fwdY = 1.;
    final double time = INF;

    final double[] aaGuess1 = new double[] {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7 };
    final double[] xx = new double[] {0.9, 0.95, 1.0, 1.05, 1.1, 0.9, 0.95, 1.0, 1.05, 1.15 };
    final double[] yy = new double[] {0.09, 0.085, 0.08, 0.03105, 0.091, 0.09, 0.075, 0.066, 0.705, 0.115 };

    final MixedBivariateLogNormalFitter fitter1 = new MixedBivariateLogNormalFitter();
    fitter1.doFit(aaGuess1, xx, yy, time, fwdX, fwdY, nNorms, nDataPtsX, 1);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void iNFfwdXTest() {
    final int nNorms = 2;
    final int nDataPtsX = 5;

    final double fwdX = INF;
    final double fwdY = 1.;
    final double time = 1.0;

    final double[] aaGuess1 = new double[] {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7 };
    final double[] xx = new double[] {0.9, 0.95, 1.0, 1.05, 1.1, 0.9, 0.95, 1.0, 1.05, 1.15 };
    final double[] yy = new double[] {0.09, 0.085, 0.08, 0.03105, 0.091, 0.09, 0.075, 0.066, 0.705, 0.115 };

    final MixedBivariateLogNormalFitter fitter1 = new MixedBivariateLogNormalFitter();
    fitter1.doFit(aaGuess1, xx, yy, time, fwdX, fwdY, nNorms, nDataPtsX, 1);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void iNFfwdYTest() {
    final int nNorms = 2;
    final int nDataPtsX = 5;

    final double fwdX = 1.;
    final double fwdY = INF;
    final double time = 1.0;

    final double[] aaGuess1 = new double[] {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7 };
    final double[] xx = new double[] {0.9, 0.95, 1.0, 1.05, 1.1, 0.9, 0.95, 1.0, 1.05, 1.15 };
    final double[] yy = new double[] {0.09, 0.085, 0.08, 0.03105, 0.091, 0.09, 0.075, 0.066, 0.705, 0.115 };

    final MixedBivariateLogNormalFitter fitter1 = new MixedBivariateLogNormalFitter();
    fitter1.doFit(aaGuess1, xx, yy, time, fwdX, fwdY, nNorms, nDataPtsX, 1);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void iNFparamsGuessCorrectionTest() {
    final int nNorms = 2;
    final int nDataPtsX = 5;
    final double paramsGuessCorrection = INF;

    final double fwdX = 1.;
    final double fwdY = 1.;
    final double time = 1.0;

    final double[] aaGuess1 = new double[] {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7 };
    final double[] xx = new double[] {0.9, 0.95, 1.0, 1.05, 1.1, 0.9, 0.95, 1.0, 1.05, 1.15 };
    final double[] yy = new double[] {0.09, 0.085, 0.08, 0.03105, 0.091, 0.09, 0.075, 0.066, 0.705, 0.115 };

    final MixedBivariateLogNormalFitter fitter1 = new MixedBivariateLogNormalFitter();
    fitter1.doFit(aaGuess1, xx, yy, time, fwdX, fwdY, nNorms, nDataPtsX, paramsGuessCorrection);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongnDataPtsXTest() {
    final int nNorms = 2;
    final int nDataPtsX = 11;

    final double fwdX = 1.;
    final double fwdY = 1.;
    final double time = 1.0;

    final double[] aaGuess1 = new double[] {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7 };
    final double[] xx = new double[] {0.9, 0.95, 1.0, 1.05, 1.1, 0.9, 0.95, 1.0, 1.05, 1.15 };
    final double[] yy = new double[] {0.09, 0.085, 0.08, 0.03105, 0.091, 0.09, 0.075, 0.066, 0.705, 0.115 };

    final MixedBivariateLogNormalFitter fitter1 = new MixedBivariateLogNormalFitter();
    fitter1.doFit(aaGuess1, xx, yy, time, fwdX, fwdY, nNorms, nDataPtsX, 1);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongYYlengthTest() {
    final int nNorms = 2;
    final int nDataPtsX = 5;

    final double fwdX = 1.;
    final double fwdY = 1.;
    final double time = 1.0;

    final double[] aaGuess1 = new double[] {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7 };
    final double[] xx = new double[] {0.9, 0.95, 1.0, 1.05, 1.1, 0.9, 0.95, 1.0, 1.05, 1.15 };
    final double[] yy = new double[] {0.09, 0.085, 0.08, 0.03105, 0.091, 0.09, 0.075 };

    final MixedBivariateLogNormalFitter fitter1 = new MixedBivariateLogNormalFitter();
    fitter1.doFit(aaGuess1, xx, yy, time, fwdX, fwdY, nNorms, nDataPtsX, 1);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongParamLengthTest() {
    final int nNorms = 6;
    final int nDataPtsX = 5;

    final double fwdX = 1.;
    final double fwdY = 1.;
    final double time = 1.0;

    final double[] aaGuess1 = new double[] {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7 };
    final double[] xx = new double[] {0.9, 0.95, 1.0, 1.05, 1.1, 0.9, 0.95, 1.0, 1.05, 1.15 };
    final double[] yy = new double[] {0.09, 0.085, 0.08, 0.03105, 0.091, 0.09, 0.075, 0.066, 0.705, 0.115 };

    final MixedBivariateLogNormalFitter fitter1 = new MixedBivariateLogNormalFitter();
    fitter1.doFit(aaGuess1, xx, yy, time, fwdX, fwdY, nNorms, nDataPtsX, 1);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeTimeTest() {
    final int nNorms = 2;
    final int nDataPtsX = 5;

    final double fwdX = 1.;
    final double fwdY = 1.;
    final double time = -1.0;

    final double[] aaGuess1 = new double[] {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7 };
    final double[] xx = new double[] {0.9, 0.95, 1.0, 1.05, 1.1, 0.9, 0.95, 1.0, 1.05, 1.15 };
    final double[] yy = new double[] {0.09, 0.085, 0.08, 0.03105, 0.091, 0.09, 0.075, 0.066, 0.705, 0.115 };

    final MixedBivariateLogNormalFitter fitter1 = new MixedBivariateLogNormalFitter();
    fitter1.doFit(aaGuess1, xx, yy, time, fwdX, fwdY, nNorms, nDataPtsX, 1);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeFwdXTest() {
    final int nNorms = 2;
    final int nDataPtsX = 5;

    final double fwdX = 1.;
    final double fwdY = -1.;
    final double time = 1.0;

    final double[] aaGuess1 = new double[] {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7 };
    final double[] xx = new double[] {0.9, 0.95, 1.0, 1.05, 1.1, 0.9, 0.95, 1.0, 1.05, 1.15 };
    final double[] yy = new double[] {0.09, 0.085, 0.08, 0.03105, 0.091, 0.09, 0.075, 0.066, 0.705, 0.115 };

    final MixedBivariateLogNormalFitter fitter1 = new MixedBivariateLogNormalFitter();
    fitter1.doFit(aaGuess1, xx, yy, time, fwdX, fwdY, nNorms, nDataPtsX, 1);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeFwdYTest() {
    final int nNorms = 2;
    final int nDataPtsX = 5;

    final double fwdX = 1.;
    final double fwdY = -1.;
    final double time = 1.0;

    final double[] aaGuess1 = new double[] {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7 };
    final double[] xx = new double[] {0.9, 0.95, 1.0, 1.05, 1.1, 0.9, 0.95, 1.0, 1.05, 1.15 };
    final double[] yy = new double[] {0.09, 0.085, 0.08, 0.03105, 0.091, 0.09, 0.075, 0.066, 0.705, 0.115 };

    final MixedBivariateLogNormalFitter fitter1 = new MixedBivariateLogNormalFitter();
    fitter1.doFit(aaGuess1, xx, yy, time, fwdX, fwdY, nNorms, nDataPtsX, 1);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeParamsGuessCorrectionTest() {
    final int nNorms = 2;
    final int nDataPtsX = 5;

    final double fwdX = 1.;
    final double fwdY = 1.;
    final double time = 1.0;

    final double[] aaGuess1 = new double[] {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7 };
    final double[] xx = new double[] {0.9, 0.95, 1.0, 1.05, 1.1, 0.9, 0.95, 1.0, 1.05, 1.15 };
    final double[] yy = new double[] {0.09, 0.085, 0.08, 0.03105, 0.091, 0.09, 0.075, 0.066, 0.705, 0.115 };

    final MixedBivariateLogNormalFitter fitter1 = new MixedBivariateLogNormalFitter();
    fitter1.doFit(aaGuess1, xx, yy, time, fwdX, fwdY, nNorms, nDataPtsX, -1);
  }

  public void testDerivingZ1() {

    final int nNorms = 2;
    final int nParams = 5 * nNorms - 3;
    final int nDataPts = 14;
    final int nDataPtsX = 7;
    final int nParamsX = 3 * nNorms - 2;
    final int nParamsY = 3 * nNorms - 2;

    final double fwdX = 1.;
    final double fwdY = 1.;
    final double fwdZ = fwdX / fwdY;
    final double time = 1.0;

    double[] xx = new double[nDataPts];
    final double[] yy = new double[nDataPts];
    double[] aaGuess1 = new double[nParams];
    final double[] aaGuess1X = new double[nParamsX];
    final double[] aaGuess1Y = new double[nParamsY];
    final double[] rhos = new double[nNorms];

    final Random obj = new Random();

    final double[] inRelativePartialForwardsX = {1., 1. };
    final double[] inRelativePartialForwardsY = {Math.exp(-0.2), (1. - Math.exp(-0.2) * 0.7) / 0.3 };

    final double[] inSigmasX = {0.25, 0.7 };
    final double[] inSigmasY = {0.3, 0.5 };

    final double[] inWeights = {0.7, 0.3 };

    final MixedLogNormalModelData inObjX = new MixedLogNormalModelData(inWeights, inSigmasX, inRelativePartialForwardsX);
    final MixedLogNormalModelData inObjY = new MixedLogNormalModelData(inWeights, inSigmasY, inRelativePartialForwardsY);

    xx = new double[] {0.5, 0.7, 0.9, 1.0, 1.2, 1.5, 1.8, 0.5, 0.7, 0.9, 1.0, 1.2, 1.5, 1.8 };

    final MixedLogNormalVolatilityFunction volfunc = MixedLogNormalVolatilityFunction.getInstance();
    Arrays.fill(yy, 0.);

    for (int j = 0; j < nDataPtsX; ++j) {
      final EuropeanVanillaOption option = new EuropeanVanillaOption(xx[j], time, true);
      yy[j] = volfunc.getVolatility(option, fwdX, inObjX);
    }

    for (int j = nDataPtsX; j < nDataPts; ++j) {
      final EuropeanVanillaOption option = new EuropeanVanillaOption(xx[j], time, true);
      yy[j] = volfunc.getVolatility(option, fwdY, inObjY);
    }

    rhos[0] = 0.2;
    rhos[1] = 0.9;

    final MixedBivariateLogNormalModelVolatility objTrueZ = new MixedBivariateLogNormalModelVolatility(inWeights, inSigmasX,
        inSigmasY, inRelativePartialForwardsX, inRelativePartialForwardsY, rhos);

    final double[] inSigmasZ = objTrueZ.getSigmasZ();
    final double[] inRelativePartialForwardsZ = objTrueZ.getRelativeForwardsZ();

    for (int i = 0; i < nParams; ++i) {
      aaGuess1[i] = 1e-2 + obj.nextDouble();
    }
    MixedBivariateLogNormalFitter fitter1 = new MixedBivariateLogNormalFitter();

    boolean fitDone = false;

    while (fitDone == false) {

      for (int i = 0; i < nNorms; ++i) {
        aaGuess1X[i] = aaGuess1[i];
        aaGuess1Y[i] = aaGuess1[i + nNorms];
      }
      for (int i = 0; i < nNorms - 1; ++i) {
        aaGuess1X[i + nNorms] = aaGuess1[i + 2 * nNorms];
        aaGuess1X[i + 2 * nNorms - 1] = aaGuess1[i + 3 * nNorms - 1];
        aaGuess1Y[i + nNorms] = aaGuess1[i + 2 * nNorms];
        aaGuess1Y[i + 2 * nNorms - 1] = aaGuess1[i + 4 * nNorms - 2];
      }

      fitter1.doFit(aaGuess1, xx, yy, time, fwdX, fwdY, nNorms, nDataPtsX, 1);
      aaGuess1 = fitter1.getParams();

      for (int i = 0; i < nNorms; ++i) {
        aaGuess1X[i] = aaGuess1[i];
        aaGuess1Y[i] = aaGuess1[i + nNorms];
      }
      for (int i = 0; i < nNorms - 1; ++i) {
        aaGuess1X[i + nNorms] = aaGuess1[i + 2 * nNorms];
        aaGuess1X[i + 2 * nNorms - 1] = aaGuess1[i + 3 * nNorms - 1];
        aaGuess1Y[i + nNorms] = aaGuess1[i + 2 * nNorms];
        aaGuess1Y[i + 2 * nNorms - 1] = aaGuess1[i + 4 * nNorms - 2];
      }

      if (fitter1.getFinalSq() <= fitter1.getInitialSq() * 1e-10) {
        fitDone = true;
      } else {
        for (int i = 0; i < nParams; ++i) {
          aaGuess1[i] = 1e-2 + obj.nextDouble();
        }
        fitter1 = new MixedBivariateLogNormalFitter();
      }

    }

    for (int i = 0; i < nNorms; ++i) {
      aaGuess1X[i] = aaGuess1[i];
      aaGuess1Y[i] = aaGuess1[i + nNorms];
    }
    for (int i = 0; i < nNorms - 1; ++i) {
      aaGuess1X[i + nNorms] = aaGuess1[i + 2 * nNorms];
      aaGuess1X[i + 2 * nNorms - 1] = aaGuess1[i + 3 * nNorms - 1];
      aaGuess1Y[i + nNorms] = aaGuess1[i + 2 * nNorms];
      aaGuess1Y[i + 2 * nNorms - 1] = aaGuess1[i + 4 * nNorms - 2];
    }

    final MixedLogNormalModelData objAns1X = new MixedLogNormalModelData(aaGuess1X, true);
    final double[] weights = objAns1X.getWeights();
    final double[] sigmasX = objAns1X.getVolatilities();
    final double[] relativePartialForwardsX = objAns1X.getRelativeForwards();

    final MixedLogNormalModelData objAns1Y = new MixedLogNormalModelData(aaGuess1Y, true);
    final double[] sigmasY = objAns1Y.getVolatilities();
    final double[] relativePartialForwardsY = objAns1Y.getRelativeForwards();

    final MixedBivariateLogNormalModelVolatility objZ = new MixedBivariateLogNormalModelVolatility(weights, sigmasX,
        sigmasY, relativePartialForwardsX, relativePartialForwardsY, rhos);

    final double[] sigmasZ = objZ.getSigmasZ();
    final double[] relativePartialForwardsZ = objZ.getRelativeForwardsZ();

    for (int i = 0; i < nNorms; ++i) {
      assertEquals(weights[i], inWeights[i], Math.abs((inWeights[0] + inWeights[1]) / 2.) * 1e-6);
    }
    for (int i = 0; i < nNorms; ++i) {
      assertEquals(sigmasX[i], inSigmasX[i], Math.abs((inSigmasX[0] + inSigmasX[1]) / 2.) * 1e-6);
    }
    for (int i = 0; i < nNorms; ++i) {
      assertEquals(sigmasY[i], inSigmasY[i], Math.abs((inSigmasY[0] + inSigmasY[1]) / 2.) * 1e-6);
    }
    for (int i = 0; i < nNorms; ++i) {
      assertEquals(relativePartialForwardsX[i], inRelativePartialForwardsX[i], Math.abs((inRelativePartialForwardsX[0] + inRelativePartialForwardsX[1]) / 2.) * 1e-6);
    }
    for (int i = 0; i < nNorms; ++i) {
      assertEquals(relativePartialForwardsY[i], inRelativePartialForwardsY[i], Math.abs((inRelativePartialForwardsY[0] + inRelativePartialForwardsY[1]) / 2.) * 1e-6);
    }

    for (int i = 0; i < nNorms; ++i) {
      assertEquals(sigmasZ[i], inSigmasZ[i], Math.abs((inSigmasZ[0] + inSigmasZ[1]) / 2.) * 1e-6);
    }
    for (int i = 0; i < nNorms; ++i) {
      assertEquals(relativePartialForwardsZ[i], inRelativePartialForwardsZ[i], Math.abs((inRelativePartialForwardsZ[0] + inRelativePartialForwardsZ[1]) / 2.) * 1e-6);
    }

    final double[] ansVolsX = new double[100];
    final double[] ansVolsY = new double[100];
    final double[] ansVolsZ = new double[100];
    for (int i = 0; i < 100; i++) {
      final double k = fwdX * (0.1 + 2. * i / 100.);
      final EuropeanVanillaOption option = new EuropeanVanillaOption(k, time, true);
      ansVolsX[i] = volfunc.getVolatility(option, fwdX, objAns1X);
      ansVolsY[i] = volfunc.getVolatility(option, fwdY, objAns1Y);
      ansVolsZ[i] = objZ.getImpliedVolatilityZ(option, fwdZ);
    }

    final double[] trueVolsX = new double[100];
    final double[] trueVolsY = new double[100];
    final double[] trueVolsZ = new double[100];
    for (int i = 0; i < 100; i++) {
      final double k = fwdY * (0.1 + 2. * i / 100.);
      final EuropeanVanillaOption option = new EuropeanVanillaOption(k, time, true);
      trueVolsX[i] = volfunc.getVolatility(option, fwdX, inObjX);
      trueVolsY[i] = volfunc.getVolatility(option, fwdY, inObjY);
      trueVolsZ[i] = objTrueZ.getImpliedVolatilityZ(option, fwdZ);
    }

    for (int i = 0; i < 100; i++) {
      assertEquals(ansVolsX[i], trueVolsX[i], Math.abs((inSigmasX[0] + inSigmasX[1]) / 2.) * 1e-6);
      assertEquals(ansVolsY[i], trueVolsY[i], Math.abs((inSigmasY[0] + inSigmasY[1]) / 2.) * 1e-6);
      assertEquals(ansVolsZ[i], trueVolsZ[i], Math.abs((inSigmasZ[0] + inSigmasZ[1]) / 2.) * 1e-6);
    }

    final double[] ansDensityX = new double[100];
    final double[] ansDensityY = new double[100];
    final double[] ansDensityZ = new double[100];
    final double[] trueDensityX = new double[100];
    final double[] trueDensityY = new double[100];
    final double[] trueDensityZ = new double[100];
    for (int i = 0; i < 100; i++) {
      final double k = fwdX * (0.01 + 2. * i / 100.);
      final EuropeanVanillaOption option = new EuropeanVanillaOption(k, time, true);
      ansDensityX[i] = getDualGamma(option, fwdX, objAns1X);
      trueDensityX[i] = getDualGamma(option, fwdX, inObjX);
      assertEquals(ansDensityX[i], trueDensityX[i], 1e-6);
    }

    for (int i = 0; i < 100; i++) {
      final double k = fwdY * (0.01 + 2. * i / 100.);
      final EuropeanVanillaOption option = new EuropeanVanillaOption(k, time, true);
      ansDensityY[i] = getDualGamma(option, fwdY, objAns1Y);
      trueDensityY[i] = getDualGamma(option, fwdY, inObjY);
      assertEquals(ansDensityY[i], trueDensityY[i], 1e-6);
    }

    for (int i = 0; i < 100; i++) {
      final double k = fwdY * (0.01 + 2. * i / 100.);
      final EuropeanVanillaOption option = new EuropeanVanillaOption(k, time, true);
      ansDensityZ[i] = getDualGammaZ(option, fwdZ, objZ);
      trueDensityZ[i] = getDualGammaZ(option, fwdZ, objTrueZ);
      assertEquals(ansDensityZ[i], trueDensityZ[i], 1e-6);
    }
    for (int i = 0; i < 100; i++) {
      final double k = fwdY * (0.01 + 2. * i / 100.);
      final EuropeanVanillaOption optionCall = new EuropeanVanillaOption(k, time, true);
      final EuropeanVanillaOption optionPut = new EuropeanVanillaOption(k, time, false);
      final double callPrice = getPrice(optionCall, fwdX, objAns1X);
      final double putPrice = getPrice(optionPut, fwdX, objAns1X);
      assertEquals((callPrice - putPrice), (fwdX - k), fwdX * 1e-10);
    }

    for (int i = 0; i < 100; i++) {
      final double k = fwdY * (0.01 + 2. * i / 100.);
      final EuropeanVanillaOption optionCall = new EuropeanVanillaOption(k, time, true);
      final EuropeanVanillaOption optionPut = new EuropeanVanillaOption(k, time, false);
      final double callPrice = getPrice(optionCall, fwdY, objAns1Y);
      final double putPrice = getPrice(optionPut, fwdY, objAns1Y);
      assertEquals((callPrice - putPrice), (fwdY - k), fwdY * 1e-10);
    }

    for (int i = 0; i < 100; i++) {
      final double k = fwdZ * (0.01 + 2. * i / 100.);
      final EuropeanVanillaOption optionCall = new EuropeanVanillaOption(k, time, true);
      final EuropeanVanillaOption optionPut = new EuropeanVanillaOption(k, time, false);
      final double callPrice = objZ.getPriceZ(optionCall, fwdZ);
      final double putPrice = objZ.getPriceZ(optionPut, fwdZ);
      assertEquals((callPrice - putPrice), (fwdY - k), fwdY * 1e-10);
    }

  }

  /**
   * 
   */
  public void testAccuracy() {

    final int nNorms = 2;
    final int nParams = 5 * nNorms - 3;
    final int nDataPts = 14;
    final int nDataPtsX = 7;
    final int nParamsX = 3 * nNorms - 2;
    final int nParamsY = 3 * nNorms - 2;

    final double fwdX = 1.;
    final double fwdY = 1.;
    final double fwdZ = fwdX / fwdY;
    final double time = 1.0;

    final double[] yy = new double[nDataPts];
    double[] aaGuess1 = new double[nParams];
    final double[] aaGuess1X = new double[nParamsX];
    final double[] aaGuess1Y = new double[nParamsY];
    final double[] rhos = new double[nNorms];

    final Random obj = new Random();

    final double[] inRelativePartialForwardsX = {1., 1. };
    final double[] inRelativePartialForwardsY = {Math.exp(-0.2), (1. - Math.exp(-0.2) * 0.7) / 0.3 };

    final double[] inSigmasX = {0.25, 0.7 };
    final double[] inSigmasY = {0.3, 0.5 };

    final double[] inWeights = {0.7, 0.3 };

    final MixedLogNormalModelData inObjX = new MixedLogNormalModelData(inWeights, inSigmasX, inRelativePartialForwardsX);
    final MixedLogNormalModelData inObjY = new MixedLogNormalModelData(inWeights, inSigmasY, inRelativePartialForwardsY);

    final double[] xx = new double[] {0.5, 0.7, 0.9, 1.0, 1.2, 1.5, 1.8, 0.5, 0.7, 0.9, 1.0, 1.2, 1.5, 1.8 };

    final MixedLogNormalVolatilityFunction volfunc = MixedLogNormalVolatilityFunction.getInstance();
    Arrays.fill(yy, 0.);

    for (int j = 0; j < nDataPtsX; ++j) {
      final EuropeanVanillaOption option = new EuropeanVanillaOption(xx[j], time, true);
      yy[j] = volfunc.getVolatility(option, fwdX, inObjX);
    }

    for (int j = nDataPtsX; j < nDataPts; ++j) {
      final EuropeanVanillaOption option = new EuropeanVanillaOption(xx[j], time, true);
      yy[j] = volfunc.getVolatility(option, fwdY, inObjY);
    }

    for (int i = 0; i < nNorms; ++i) {
      rhos[i] = 0.2 * (0.5 * i + 1.);
    }

    for (int i = 0; i < nParams; ++i) {
      aaGuess1[i] = 1e-2 + obj.nextDouble();
    }
    MixedBivariateLogNormalFitter fitter1 = new MixedBivariateLogNormalFitter();

    boolean fitDone = false;

    while (fitDone == false) {

      for (int i = 0; i < nNorms; ++i) {
        aaGuess1X[i] = aaGuess1[i];
        aaGuess1Y[i] = aaGuess1[i + nNorms];
      }
      for (int i = 0; i < nNorms - 1; ++i) {
        aaGuess1X[i + nNorms] = aaGuess1[i + 2 * nNorms];
        aaGuess1X[i + 2 * nNorms - 1] = aaGuess1[i + 3 * nNorms - 1];
        aaGuess1Y[i + nNorms] = aaGuess1[i + 2 * nNorms];
        aaGuess1Y[i + 2 * nNorms - 1] = aaGuess1[i + 4 * nNorms - 2];
      }

      fitter1.doFit(aaGuess1, xx, yy, time, fwdX, fwdY, nNorms, nDataPtsX, 1);
      aaGuess1 = fitter1.getParams();

      for (int i = 0; i < nNorms; ++i) {
        aaGuess1X[i] = aaGuess1[i];
        aaGuess1Y[i] = aaGuess1[i + nNorms];
      }
      for (int i = 0; i < nNorms - 1; ++i) {
        aaGuess1X[i + nNorms] = aaGuess1[i + 2 * nNorms];
        aaGuess1X[i + 2 * nNorms - 1] = aaGuess1[i + 3 * nNorms - 1];
        aaGuess1Y[i + nNorms] = aaGuess1[i + 2 * nNorms];
        aaGuess1Y[i + 2 * nNorms - 1] = aaGuess1[i + 4 * nNorms - 2];
      }

      if (fitter1.getFinalSq() <= fitter1.getInitialSq() * 1e-14) {
        fitDone = true;
      } else {
        for (int i = 0; i < nParams; ++i) {
          aaGuess1[i] = 1e-2 + obj.nextDouble();
        }
        fitter1 = new MixedBivariateLogNormalFitter();
      }

    }

    for (int i = 0; i < nNorms; ++i) {
      aaGuess1X[i] = aaGuess1[i];
      aaGuess1Y[i] = aaGuess1[i + nNorms];
    }
    for (int i = 0; i < nNorms - 1; ++i) {
      aaGuess1X[i + nNorms] = aaGuess1[i + 2 * nNorms];
      aaGuess1X[i + 2 * nNorms - 1] = aaGuess1[i + 3 * nNorms - 1];
      aaGuess1Y[i + nNorms] = aaGuess1[i + 2 * nNorms];
      aaGuess1Y[i + 2 * nNorms - 1] = aaGuess1[i + 4 * nNorms - 2];
    }

    final MixedLogNormalModelData objAns1X = new MixedLogNormalModelData(aaGuess1X, true);
    final double[] weights = objAns1X.getWeights();
    final double[] sigmasX = objAns1X.getVolatilities();
    final double[] relativePartialForwardsX = objAns1X.getRelativeForwards();

    final MixedLogNormalModelData objAns1Y = new MixedLogNormalModelData(aaGuess1Y, true);
    final double[] sigmasY = objAns1Y.getVolatilities();
    final double[] relativePartialForwardsY = objAns1Y.getRelativeForwards();

    final MixedBivariateLogNormalModelVolatility objZ = new MixedBivariateLogNormalModelVolatility(weights, sigmasX,
        sigmasY, relativePartialForwardsX, relativePartialForwardsY, rhos);

    final double[] ansVolsX = new double[100];
    final double[] ansVolsY = new double[100];
    final double[] ansVolsZ = new double[100];
    for (int i = 0; i < 100; i++) {
      final double k = fwdX * (0.1 + 2. * i / 100.);
      final EuropeanVanillaOption option = new EuropeanVanillaOption(k, time, true);
      ansVolsX[i] = volfunc.getVolatility(option, fwdX, objAns1X);
      ansVolsY[i] = volfunc.getVolatility(option, fwdY, objAns1Y);
      ansVolsZ[i] = objZ.getImpliedVolatilityZ(option, fwdZ);
    }

    final MixedBivariateLogNormalModelVolatility objTrueZ = new MixedBivariateLogNormalModelVolatility(inWeights, inSigmasX,
        inSigmasY, inRelativePartialForwardsX, inRelativePartialForwardsY, rhos);

    final double[] trueVolsX = new double[100];
    final double[] trueVolsY = new double[100];
    final double[] trueVolsZ = new double[100];
    for (int i = 0; i < 100; i++) {
      final double k = fwdY * (0.1 + 2. * i / 100.);
      final EuropeanVanillaOption option = new EuropeanVanillaOption(k, time, true);
      trueVolsX[i] = volfunc.getVolatility(option, fwdX, inObjX);
      trueVolsY[i] = volfunc.getVolatility(option, fwdY, inObjY);
      trueVolsZ[i] = objTrueZ.getImpliedVolatilityZ(option, fwdZ);
    }

    final double[] inSigmasZ = objTrueZ.getSigmasZ();

    for (int i = 0; i < 100; i++) {
      assertEquals(ansVolsX[i], trueVolsX[i], Math.abs((inSigmasX[0] + inSigmasX[1]) / 2.) * 1e-6);
      assertEquals(ansVolsY[i], trueVolsY[i], Math.abs((inSigmasY[0] + inSigmasY[1]) / 2.) * 1e-6);
      assertEquals(ansVolsZ[i], trueVolsZ[i], Math.abs((inSigmasZ[0] + inSigmasZ[1]) / 2.) * 1e-6);
    }

  }

  /**
   * 
   */
  public void testAccuracy2() {

    final int nNorms = 2;
    final int nParams = 5 * nNorms - 3;
    final int nDataPts = 14;
    final int nDataPtsX = 7;
    final int nParamsX = 3 * nNorms - 2;
    final int nParamsY = 3 * nNorms - 2;

    final double fwdX = 1.;
    final double fwdY = 1.;
    final double fwdZ = fwdX / fwdY;
    final double time = 1.0;

    double[] xx = new double[nDataPts];
    final double[] yy = new double[nDataPts];
    double[] aaGuess1 = new double[nParams];
    final double[] aaGuess1X = new double[nParamsX];
    final double[] aaGuess1Y = new double[nParamsY];
    final double[] rhos = new double[nNorms];

    final Random obj = new Random();

    final double[] inRelativePartialForwardsX = {1., 1. };
    final double[] inRelativePartialForwardsY = {Math.exp(-0.2), (1. - Math.exp(-0.2) * 0.7) / 0.3 };

    final double[] inSigmasX = {0.25, 0.7 };
    final double[] inSigmasY = {0.3, 0.5 };

    final double[] inWeights = {0.7, 0.3 };

    final MixedLogNormalModelData inObjX = new MixedLogNormalModelData(inWeights, inSigmasX, inRelativePartialForwardsX);
    final MixedLogNormalModelData inObjY = new MixedLogNormalModelData(inWeights, inSigmasY, inRelativePartialForwardsY);

    xx = new double[] {0.5, 0.7, 0.9, 1.0, 1.2, 1.5, 1.8, 0.5, 0.7, 0.9, 1.0, 1.2, 1.5, 1.8 };

    final MixedLogNormalVolatilityFunction volfunc = MixedLogNormalVolatilityFunction.getInstance();
    Arrays.fill(yy, 0.);

    for (int j = 0; j < nDataPtsX; ++j) {
      final EuropeanVanillaOption option = new EuropeanVanillaOption(xx[j], time, true);
      yy[j] = volfunc.getVolatility(option, fwdX, inObjX);
    }

    for (int j = nDataPtsX; j < nDataPts; ++j) {
      final EuropeanVanillaOption option = new EuropeanVanillaOption(xx[j], time, true);
      yy[j] = volfunc.getVolatility(option, fwdY, inObjY);
    }

    for (int i = 0; i < nNorms; ++i) {
      rhos[i] = 0.2 * (0.5 * i + 1.);
    }

    for (int i = 0; i < nParams; ++i) {
      aaGuess1[i] = 1e-2 + obj.nextDouble();
    }
    MixedBivariateLogNormalFitter fitter1 = new MixedBivariateLogNormalFitter();

    boolean fitDone = false;

    while (fitDone == false) {

      for (int i = 0; i < nNorms; ++i) {
        aaGuess1X[i] = aaGuess1[i];
        aaGuess1Y[i] = aaGuess1[i + nNorms];
      }
      for (int i = 0; i < nNorms - 1; ++i) {
        aaGuess1X[i + nNorms] = aaGuess1[i + 2 * nNorms];
        aaGuess1X[i + 2 * nNorms - 1] = aaGuess1[i + 3 * nNorms - 1];
        aaGuess1Y[i + nNorms] = aaGuess1[i + 2 * nNorms];
        aaGuess1Y[i + 2 * nNorms - 1] = aaGuess1[i + 4 * nNorms - 2];
      }

      fitter1.doFit(aaGuess1, xx, yy, time, fwdX, fwdY, nNorms, nDataPtsX, 1.);
      aaGuess1 = fitter1.getParams();

      for (int i = 0; i < nNorms; ++i) {
        aaGuess1X[i] = aaGuess1[i];
        aaGuess1Y[i] = aaGuess1[i + nNorms];
      }
      for (int i = 0; i < nNorms - 1; ++i) {
        aaGuess1X[i + nNorms] = aaGuess1[i + 2 * nNorms];
        aaGuess1X[i + 2 * nNorms - 1] = aaGuess1[i + 3 * nNorms - 1];
        aaGuess1Y[i + nNorms] = aaGuess1[i + 2 * nNorms];
        aaGuess1Y[i + 2 * nNorms - 1] = aaGuess1[i + 4 * nNorms - 2];
      }

      if (fitter1.getFinalSq() <= fitter1.getInitialSq() * 1e-14) {
        fitDone = true;
      } else {
        for (int i = 0; i < nParams; ++i) {
          aaGuess1[i] = 1e-2 + obj.nextDouble();
        }
        fitter1 = new MixedBivariateLogNormalFitter();
      }

    }

    for (int i = 0; i < nNorms; ++i) {
      aaGuess1X[i] = aaGuess1[i];
      aaGuess1Y[i] = aaGuess1[i + nNorms];
    }
    for (int i = 0; i < nNorms - 1; ++i) {
      aaGuess1X[i + nNorms] = aaGuess1[i + 2 * nNorms];
      aaGuess1X[i + 2 * nNorms - 1] = aaGuess1[i + 3 * nNorms - 1];
      aaGuess1Y[i + nNorms] = aaGuess1[i + 2 * nNorms];
      aaGuess1Y[i + 2 * nNorms - 1] = aaGuess1[i + 4 * nNorms - 2];
    }

    final MixedLogNormalModelData objAns1X = new MixedLogNormalModelData(aaGuess1X, true);
    final double[] weights = objAns1X.getWeights();
    final double[] sigmasX = objAns1X.getVolatilities();
    final double[] relativePartialForwardsX = objAns1X.getRelativeForwards();

    final MixedLogNormalModelData objAns1Y = new MixedLogNormalModelData(aaGuess1Y, true);
    final double[] sigmasY = objAns1Y.getVolatilities();
    final double[] relativePartialForwardsY = objAns1Y.getRelativeForwards();

    final MixedBivariateLogNormalModelVolatility objTrueZ = new MixedBivariateLogNormalModelVolatility(inWeights, inSigmasX,
        inSigmasY, inRelativePartialForwardsX, inRelativePartialForwardsY, rhos);

    final double[] xxZ = new double[nDataPtsX];
    final double[] yyZ = new double[nDataPtsX];
    for (int j = 0; j < nDataPtsX; ++j) {
      final EuropeanVanillaOption option = new EuropeanVanillaOption(xx[j], time, true);
      xxZ[j] = xx[j];
      yyZ[j] = objTrueZ.getImpliedVolatilityZ(option, fwdZ);
    }

    double[] rhosGuess = new double[nNorms];
    for (int i = 0; i < nNorms; ++i) {
      rhosGuess[i] = 1. - obj.nextDouble();
    }

    MixedBivariateLogNormalCorrelationFinder fitter = new MixedBivariateLogNormalCorrelationFinder();

    boolean fitRhoDone = false;
    int counterRho = 0;

    while (fitRhoDone == false) {
      ++counterRho;

      fitter.doFit(rhosGuess, xxZ, yyZ, time, weights, sigmasX, sigmasY,
          relativePartialForwardsX,
          relativePartialForwardsY, fwdX, fwdY);
      rhosGuess = fitter.getParams();

      if (fitter.getFinalSq() <= 1e-14) {
        fitRhoDone = true;
      } else {
        for (int i = 0; i < nNorms; ++i) {
          rhosGuess[i] = 1. - obj.nextDouble();
        }
        fitter = new MixedBivariateLogNormalCorrelationFinder();
      }

      ArgChecker.isTrue(counterRho < 500, "Too many inerations for rho. Start with new guess parameters.");
    }

    rhosGuess = fitter.getParams();

    final MixedBivariateLogNormalModelVolatility objZ = new MixedBivariateLogNormalModelVolatility(weights, sigmasX,
        sigmasY, relativePartialForwardsX, relativePartialForwardsY, rhosGuess);

    final double[] ansVolsX = new double[100];
    final double[] ansVolsY = new double[100];
    final double[] ansVolsZ = new double[100];
    for (int i = 0; i < 100; i++) {
      final double k = fwdX * (0.1 + 2. * i / 100.);
      final EuropeanVanillaOption option = new EuropeanVanillaOption(k, time, true);
      ansVolsX[i] = volfunc.getVolatility(option, fwdX, objAns1X);
      ansVolsY[i] = volfunc.getVolatility(option, fwdY, objAns1Y);
      ansVolsZ[i] = objZ.getImpliedVolatilityZ(option, fwdZ);
    }

    final double[] trueVolsX = new double[100];
    final double[] trueVolsY = new double[100];
    final double[] trueVolsZ = new double[100];
    for (int i = 0; i < 100; i++) {
      final double k = fwdY * (0.1 + 2. * i / 100.);
      final EuropeanVanillaOption option = new EuropeanVanillaOption(k, time, true);
      trueVolsX[i] = volfunc.getVolatility(option, fwdX, inObjX);
      trueVolsY[i] = volfunc.getVolatility(option, fwdY, inObjY);
      trueVolsZ[i] = objTrueZ.getImpliedVolatilityZ(option, fwdZ);
    }

    final double[] inSigmasZ = objTrueZ.getSigmasZ();

    for (int i = 0; i < 100; i++) {
      assertEquals(ansVolsX[i], trueVolsX[i], Math.abs((inSigmasX[0] + inSigmasX[1]) / 2.) * 1e-7);
      assertEquals(ansVolsY[i], trueVolsY[i], Math.abs((inSigmasY[0] + inSigmasY[1]) / 2.) * 1e-7);
      assertEquals(ansVolsZ[i], trueVolsZ[i], Math.abs((inSigmasZ[0] + inSigmasZ[1]) / 2.) * 1e-7);
    }

  }

  private double getPrice(final EuropeanVanillaOption option, final double forward, final MixedLogNormalModelData data) {
    final double[] w = data.getWeights();
    final double[] sigma = data.getVolatilities();
    final double[] rf = data.getRelativeForwards();
    final int n = w.length;
    final double t = option.getTimeToExpiry();
    final double k = option.getStrike();
    final double kStar = k / forward;
    final boolean isCall = option.isCall();

    double sum = 0;
    for (int i = 0; i < n; i++) {
      sum += w[i] * BlackFormulaRepository.price(rf[i], kStar, t, sigma[i], isCall);
    }
    return forward * sum;
  }

  private double getDualGamma(final EuropeanVanillaOption option, final double forward, final MixedLogNormalModelData data) {
    final double[] w = data.getWeights();
    final double[] sigma = data.getVolatilities();
    final double[] rf = data.getRelativeForwards();
    final int n = w.length;
    final double t = option.getTimeToExpiry();
    final double k = option.getStrike();
    final double kStar = k / forward;
    double sum = 0;
    for (int i = 0; i < n; i++) {
      sum += w[i] * BlackFormulaRepository.dualGamma(rf[i], kStar, t, sigma[i]);
    }
    return forward * sum;
  }

  private double getDualGammaZ(final EuropeanVanillaOption option, final double forward, final MixedBivariateLogNormalModelVolatility dataZ) {
    final double[] w = dataZ.getOrderedWeights();
    final double[] sigma = dataZ.getSigmasZ();
    final double[] rf = dataZ.getRelativeForwardsZ();
    final int n = w.length;
    final double t = option.getTimeToExpiry();
    final double k = option.getStrike();
    final double kStar = k / forward;
    double sum = 0;
    for (int i = 0; i < n; i++) {
      sum += w[i] * BlackFormulaRepository.dualGamma(rf[i], kStar, t, sigma[i]);
    }
    return forward * sum;
  }

}

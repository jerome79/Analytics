/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility;

import com.google.common.primitives.Doubles;
import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.rootfinding.BisectionSingleRootFinder;
import com.opengamma.analytics.math.rootfinding.BracketRoot;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Finds an implied volatility (a parameter that put into a model gives the market pirce of an option) for any option pricing model that has a 'volatility' parameter.
 * This included the Black-Scholes-Merton model (and derivatives) for European options and Barone-Adesi & Whaley and Bjeksund and Stensland for American options
 */
public class GenericImpliedVolatiltySolver {

  private static final int MAX_ITERATIONS = 20; // something's wrong if Newton-Raphson taking longer than this
  private static final double VOL_TOL = 1e-9; // 1 part in 100,000 basis points will do for implied vol
  private static final double VOL_GUESS = 0.3;
  private static final double BRACKET_STEP = 0.1;
  private static final double MAX_CHANGE = 0.5;

  private final Function1D<Double, Double> _priceFunc;
  private final Function1D<Double, double[]> _priceAndVegaFunc;

  public GenericImpliedVolatiltySolver(Function1D<Double, double[]> priceAndVegaFunc) {
    ArgChecker.notNull(priceAndVegaFunc, "priceAndVegaFunc");
    _priceAndVegaFunc = priceAndVegaFunc;
    _priceFunc = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(Double sigma) {
        return _priceAndVegaFunc.evaluate(sigma)[0];
      }
    };
  }

  public GenericImpliedVolatiltySolver(final Function1D<Double, Double> priceFunc, final Function1D<Double, Double> vegaFunc) {
    ArgChecker.notNull(priceFunc, "priceFunc");
    ArgChecker.notNull(vegaFunc, "vegaFunc");
    _priceFunc = priceFunc;
    _priceAndVegaFunc = new Function1D<Double, double[]>() {

      @Override
      public double[] evaluate(Double sigma) {
        return new double[] {priceFunc.evaluate(sigma), vegaFunc.evaluate(sigma) };
      }
    };
  }

  public double impliedVolatility(double optionPrice) {
    return impliedVolatility(optionPrice, VOL_GUESS);
  }

  public double impliedVolatility(double optionPrice, double volGuess) {
    ArgChecker.isTrue(volGuess >= 0.0, "volGuess must be positive; have {}", volGuess);
    ArgChecker.isTrue(Doubles.isFinite(volGuess), "volGuess must be finite; have {} ", volGuess);

    double lowerSigma;
    double upperSigma;

    try {
      final double[] temp = bracketRoot(optionPrice, volGuess);
      lowerSigma = temp[0];
      upperSigma = temp[1];
    } catch (final MathException e) {
      throw new IllegalArgumentException(e.toString() + " No implied Volatility for this price. [price: " + optionPrice + "]");
    }
    double sigma = (lowerSigma + upperSigma) / 2.0;

    double[] pnv = _priceAndVegaFunc.evaluate(sigma);

    // This can happen for American options, where low volatilities puts you in the early excise region which obviously has zero vega
    if (pnv[1] == 0 || Double.isNaN(pnv[1])) {
      return solveByBisection(optionPrice, lowerSigma, upperSigma);
    }
    double diff = pnv[0] - optionPrice;
    boolean above = diff > 0;
    if (above) {
      upperSigma = sigma;
    } else {
      lowerSigma = sigma;
    }

    double trialChange = -diff / pnv[1];
    double actChange;
    if (trialChange > 0.0) {
      actChange = Math.min(MAX_CHANGE, Math.min(trialChange, upperSigma - sigma));
    } else {
      actChange = Math.max(-MAX_CHANGE, Math.max(trialChange, lowerSigma - sigma));
    }

    int count = 0;
    while (Math.abs(actChange) > VOL_TOL) {
      sigma += actChange;
      pnv = _priceAndVegaFunc.evaluate(sigma);

      if (pnv[1] == 0 || Double.isNaN(pnv[1])) {
        return solveByBisection(optionPrice, lowerSigma, upperSigma);
      }

      diff = pnv[0] - optionPrice;
      above = diff > 0;
      if (above) {
        upperSigma = sigma;
      } else {
        lowerSigma = sigma;
      }

      trialChange = -diff / pnv[1];
      if (trialChange > 0.0) {
        actChange = Math.min(MAX_CHANGE, Math.min(trialChange, upperSigma - sigma));
      } else {
        actChange = Math.max(-MAX_CHANGE, Math.max(trialChange, lowerSigma - sigma));
      }

      if (count++ > MAX_ITERATIONS) {
        return solveByBisection(optionPrice, lowerSigma, upperSigma);
      }
    }
    return sigma + actChange; // apply the final change

  }

  private double[] bracketRoot(final double optionPrice, final double sigma) {
    final BracketRoot bracketer = new BracketRoot();
    final Function1D<Double, Double> func = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double volatility) {
        return _priceFunc.evaluate(volatility) / optionPrice - 1.0;
      }
    };
    return bracketer.getBracketedPoints(func, Math.max(0.0, sigma - BRACKET_STEP), sigma + BRACKET_STEP, 0.0, Double.POSITIVE_INFINITY);
  }

  private double solveByBisection(final double optionPrice, final double lowerSigma, final double upperSigma) {
    final BisectionSingleRootFinder rootFinder = new BisectionSingleRootFinder(VOL_TOL);
    final Function1D<Double, Double> func = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double volatility) {
        double trialPrice = _priceFunc.evaluate(volatility);
        return trialPrice / optionPrice - 1.0;
      }
    };
    return rootFinder.getRoot(func, lowerSigma, upperSigma);
  }

}

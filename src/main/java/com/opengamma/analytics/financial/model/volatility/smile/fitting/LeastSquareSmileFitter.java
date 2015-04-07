/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting;

import java.util.BitSet;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.math.statistics.leastsquare.LeastSquareResultsWithTransform;
import com.opengamma.strata.collect.ArgChecker;

/**
 * An interface for least-square fitting of option data to smile models
 * 
 */
public abstract class LeastSquareSmileFitter {

  public abstract LeastSquareResultsWithTransform getFitResult(final EuropeanVanillaOption[] options, BlackFunctionData[] data,
      double[] errors, final double[] initialFitParameters, final BitSet fixed);

  public abstract LeastSquareResultsWithTransform getFitResult(final EuropeanVanillaOption[] options, BlackFunctionData[] data, final double[] initialFitParameters, final BitSet fixed);

  protected void testData(final EuropeanVanillaOption[] options, final BlackFunctionData[] data, final double[] errors, final double[] initialFitParameters, final BitSet fixed,
      final int nParameters) {
    ArgChecker.notEmpty(options, "options");
    final int n = options.length;
    ArgChecker.notNull(data, "data");
    ArgChecker.isTrue(data.length == n, "Black function data array must be the same length as option array");
    if (errors != null) {
      ArgChecker.isTrue(errors.length == n, "Error array length must be the same as the option array length");
    }
    ArgChecker.notNull(initialFitParameters, "initial values");
    ArgChecker.isTrue(initialFitParameters.length == nParameters, "must have length of initial values array equal to number of parameters");
    ArgChecker.notNull(fixed, "fixed");

    final double t = options[0].getTimeToExpiry();
    final double fwd = data[0].getForward();
    final double df = data[0].getDiscountFactor();

    for (int i = 1; i < n; i++) {
      ArgChecker.isTrue(Double.doubleToLongBits(options[i].getTimeToExpiry()) == Double.doubleToLongBits(t), "options not all at same time horizon");
      ArgChecker.isTrue(Double.doubleToLongBits(data[i].getForward()) == Double.doubleToLongBits(fwd), "options don't all have same forward");
      ArgChecker.isTrue(Double.doubleToLongBits(data[i].getDiscountFactor()) == Double.doubleToLongBits(df), "options don't all have same discount factors");
    }
  }
}

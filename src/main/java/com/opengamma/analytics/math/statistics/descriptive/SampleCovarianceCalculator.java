/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.descriptive;

import java.util.function.BiFunction;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Calculates the sample covariance of two series of data, $x_1, x_2, \dots, x_n$ and $y_1, y_2, \dots, y_n$.
 *
 * <p>
 * The sample covariance is given by:
 * $$
 * \begin{align*}
 * \text{cov} = \frac{1}{n-1}\sum_{i=1}^n (x_i - \overline{x})(y_i - \overline{y})
 * \end{align*}
 * $$
 * where $\overline{x}$ and $\overline{y}$ are the means of the two series.
 */
public class SampleCovarianceCalculator implements BiFunction<double[], double[], Double> {

  private static final Function1D<double[], Double> MEAN_CALCULATOR = new MeanCalculator();

  /**
   * @param x1  the first array of data, containing at least 2 elements
   * @param x2  the second array of data, must be the same size as x1
   * @return The sample covariance
   */
  @Override
  public Double apply(double[] x1, double[] x2) {

    ArgChecker.notNull(x1, "x1");
    ArgChecker.notNull(x2, "x2");
    ArgChecker.isTrue(x1.length > 1, "Array must have at least 2 elements");
    final int n = x1.length;
    ArgChecker.isTrue(x2.length == n, "Array lengths must match");
    final double mean1 = MEAN_CALCULATOR.evaluate(x1);
    final double mean2 = MEAN_CALCULATOR.evaluate(x2);
    double sum = 0;
    for (int i = 0; i < n; i++) {
      sum += (x1[i] - mean1) * (x2[i] - mean2);
    }
    return sum / (n - 1);
  }
}


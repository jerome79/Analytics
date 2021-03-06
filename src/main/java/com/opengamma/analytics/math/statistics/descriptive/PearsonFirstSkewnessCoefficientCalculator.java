/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.descriptive;

import java.util.function.Function;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Given a series of data $x_1, x_2, \dots, x_n$ with mean $\overline{x}$, mode $m$
 * and standard deviation $\sigma$, the Pearson first skewness coefficient is given by
 * $$
 * \begin{align*}
 * \text{skewness} = \frac{3(\overline{x} - m)}{\sigma}
 * \end{align*}
 * $$
 * @see MeanCalculator
 * @see MedianCalculator
 * @see SampleStandardDeviationCalculator
 */
public class PearsonFirstSkewnessCoefficientCalculator implements Function<double[], Double> {
  private static final Function1D<double[], Double> MEAN = new MeanCalculator();
  private static final Function<double[], Double> MODE = new ModeCalculator();
  private static final Function1D<double[], Double> STD_DEV = new SampleStandardDeviationCalculator();

  /**
   * @param x The array of data, not null. Must contain at least two data points
   * @return The Pearson first skewness coefficient 
   */
  @Override
  public Double apply(final double[] x) {
    ArgChecker.notNull(x, "x");
    ArgChecker.isTrue(x.length > 1, "Need at least two data points to calculate Pearson first skewness coefficient");
    return 3 * (MEAN.evaluate(x) - MODE.apply(x)) / STD_DEV.evaluate(x);
  }

}

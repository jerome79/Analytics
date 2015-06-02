/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.distribution;

import java.util.Date;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well44497b;

import com.opengamma.strata.collect.ArgChecker;

/**
 * The normal distribution is a continuous probability distribution with probability density function
 * $$
 * \begin{align*}
 * f(x) = \frac{1}{\sqrt{2\pi}\sigma} e^{-\frac{(x - \mu)^2}{2\sigma^2}}
 * \end{align*}
 * $$
 * where $\mu$ is the mean and $\sigma$ the standard deviation of
 * the distribution.
 * <p>
 */
public class NormalDistribution implements ProbabilityDistribution<Double> {
  private final double _mean;
  private final double _standardDeviation;
  private final org.apache.commons.math3.distribution.NormalDistribution _normal;

  /**
   * @param mean The mean of the distribution
   * @param standardDeviation The standard deviation of the distribution, not negative or zero
   */
  public NormalDistribution(final double mean, final double standardDeviation) {
    this(mean, standardDeviation, new Well44497b(new Date().getTime()));
  }

  /**
   * @param mean The mean of the distribution
   * @param standardDeviation The standard deviation of the distribution, not negative or zero
   * @param randomEngine A generator of uniform random numbers, not null
   */
  public NormalDistribution(final double mean, final double standardDeviation, final RandomGenerator randomGenerator) {
    ArgChecker.isTrue(standardDeviation > 0, "standard deviation");
    ArgChecker.notNull(randomGenerator, "randomGenerator");
    _mean = mean;
    _standardDeviation = standardDeviation;
    _normal = new org.apache.commons.math3.distribution.NormalDistribution(randomGenerator, mean, standardDeviation);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getCDF(final Double x) {
    ArgChecker.notNull(x, "x");
    return _normal.cumulativeProbability(x);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getPDF(final Double x) {
    ArgChecker.notNull(x, "x");
    return _normal.density(x);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double nextRandom() {
    return _normal.sample();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getInverseCDF(final Double p) {
    ArgChecker.notNull(p, "p");
    return _normal.inverseCumulativeProbability(p);
  }

  /**
   * @return The mean
   */
  public double getMean() {
    return _mean;
  }

  /**
   * @return The standard deviation
   */
  public double getStandardDeviation() {
    return _standardDeviation;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_mean);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_standardDeviation);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final NormalDistribution other = (NormalDistribution) obj;
    if (Double.doubleToLongBits(_mean) != Double.doubleToLongBits(other._mean)) {
      return false;
    }
    return Double.doubleToLongBits(_standardDeviation) == Double.doubleToLongBits(other._standardDeviation);
  }
}

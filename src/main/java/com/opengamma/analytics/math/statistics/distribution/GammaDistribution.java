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
 * The Gamma distribution is a continuous probability distribution with cdf
 * $$
 * \begin{align*}
 * F(x)=\frac{\gamma\left(k, \frac{x}{\theta}\right)}{\Gamma(k)}
 * \end{align*}
 * $$
 * and pdf
 * $$
 * \begin{align*}
 * f(x)=\frac{x^{k-1}e^{-\frac{x}{\theta}}}{\Gamma{k}\theta^k}
 * \end{align*}
 * $$
 * where $k$ is the shape parameter and $\theta$ is the scale parameter.
 * <p>
 * This implementation uses the CERN <a href="http://acs.lbl.gov/~hoschek/colt/api/index.html">colt</a> package for the cdf, pdf
 * and $\Gamma$-distributed random numbers.
 * 
 */
public class GammaDistribution implements ProbabilityDistribution<Double> {
  private final org.apache.commons.math3.distribution.GammaDistribution _gamma;
  private final double _k;
  private final double _theta;

  /**
   * @param k The shape parameter of the distribution, not negative or zero
   * @param theta The scale parameter of the distribution, not negative or zero
   */
  public GammaDistribution(final double k, final double theta) {
    this(k, theta, new Well44497b(new Date().getTime()));
  }

  /**
   * @param k The shape parameter of the distribution, not negative or zero
   * @param theta The scale parameter of the distribution, not negative or zero
   * @param engine A uniform random number generator, not null
   */
  public GammaDistribution(final double k, final double theta, final RandomGenerator engine) {
    ArgChecker.isTrue(k > 0, "k must be > 0");
    ArgChecker.isTrue(theta > 0, "theta must be > 0");
    ArgChecker.notNull(engine, "engine");
    _gamma = new org.apache.commons.math3.distribution.GammaDistribution(engine, k, 1. / theta);
    _k = k;
    _theta = theta;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getCDF(final Double x) {
    ArgChecker.notNull(x, "x");
    return _gamma.cumulativeProbability(x);
  }

  /**
   * {@inheritDoc}
   * @return Not supported
   * @throws UnsupportedOperationException
   */
  @Override
  public double getInverseCDF(final Double p) {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getPDF(final Double x) {
    ArgChecker.notNull(x, "x");
    return _gamma.density(x);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double nextRandom() {
    return _gamma.sample();
  }

  /**
   * @return The shape parameter
   */
  public double getK() {
    return _k;
  }

  /**
   * @return The location parameter
   */
  public double getTheta() {
    return _theta;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_k);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_theta);
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
    final GammaDistribution other = (GammaDistribution) obj;
    if (Double.doubleToLongBits(_k) != Double.doubleToLongBits(other._k)) {
      return false;
    }
    return Double.doubleToLongBits(_theta) == Double.doubleToLongBits(other._theta);
  }

}

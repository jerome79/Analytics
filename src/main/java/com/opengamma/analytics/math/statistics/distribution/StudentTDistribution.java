/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.distribution;

import java.util.Date;

import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well44497b;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.function.special.GammaFunction;
import com.opengamma.analytics.math.function.special.InverseIncompleteBetaFunction;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Student's T-distribution is a continuous probability distribution with probability density function
 * $$
 * \begin{align*}
 * f(x) = \frac{\Gamma\left(\frac{\nu + 1}{2}\right)}{\sqrt{\nu\pi}\Gamma(\left(\frac{\nu}{2}\right)}\left(1 + \frac{x^2}{\nu}\right)^{-\frac{1}{2}(\nu + 1)}
 * \end{align*}
 * $$
 * where $\nu$ is the number of degrees of freedom and $\Gamma$ is the Gamma function ({@link GammaFunction}).
 * <p>
 * This class is a wrapper for the <a href="http://acs.lbl.gov/software/colt/api/cern/jet/random/StudentT.html">Colt</a> implementation of Student's T-distribution for cdf and pdf calculations
 * and Student's T-distributed random number generation.
 */
public class StudentTDistribution implements ProbabilityDistribution<Double> {
  private final double _degFreedom;
  private final TDistribution _dist;
  private final Function1D<Double, Double> _beta;

  /**
   * @param degFreedom The number of degrees of freedom, not negative or zero
   */
  public StudentTDistribution(final double degFreedom) {
    this(degFreedom, new Well44497b(new Date().getTime()));
  }

  /**
   * @param degFreedom The number of degrees of freedom, not negative or zero
   * @param engine A generator of uniform random numbers, not null
   */
  public StudentTDistribution(final double degFreedom, final RandomGenerator engine) {
    ArgChecker.isTrue(degFreedom > 0, "degrees of freedom");
    ArgChecker.notNull(engine, "engine");
    _degFreedom = degFreedom;
    _dist = new TDistribution(engine, degFreedom);
    _beta = new InverseIncompleteBetaFunction(degFreedom / 2., 0.5);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getCDF(final Double x) {
    ArgChecker.notNull(x, "x");
    return _dist.cumulativeProbability(x);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getPDF(final Double x) {
    ArgChecker.notNull(x, "x");
    return _dist.density(x);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double nextRandom() {
    return _dist.sample();
  }

  /**
   * {@inheritDoc}
   * The inverse cdf is given by:
   * $$
   * \begin{align*}
   * F(P) &= \mathrm{sign}(p - \frac{1}{2})\sqrt{\frac{\nu}{x - 1}}\\
   * x &= B(2 \min(p, 1-p)) 
   * \end{align*}
   * $$
   * where $B$ is the inverse incomplete Beta function ({@link InverseIncompleteBetaFunction}).
   */
  @Override
  public double getInverseCDF(final Double p) {
    ArgChecker.notNull(p, "p");
    ArgChecker.isTrue(p >= 0 && p <= 1, "Probability must be >= 0 and <= 1");
    final double x = _beta.evaluate(2 * Math.min(p, 1 - p));
    return Math.signum(p - 0.5) * Math.sqrt(_degFreedom * (1. / x - 1));
  }

  /**
   * @return The number of degrees of freedom
   */
  public double getDegreesOfFreedom() {
    return _degFreedom;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_degFreedom);
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
    final StudentTDistribution other = (StudentTDistribution) obj;
    return Double.doubleToLongBits(_degFreedom) == Double.doubleToLongBits(other._degFreedom);
  }
}

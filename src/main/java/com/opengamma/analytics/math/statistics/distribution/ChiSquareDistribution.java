/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.distribution;

import java.util.Date;

import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well44497b;

import com.opengamma.analytics.math.function.Function2D;
import com.opengamma.analytics.math.function.special.InverseIncompleteGammaFunction;
import com.opengamma.strata.collect.ArgChecker;

/**
 * A $\chi^2$ distribution with $k$ degrees of freedom is the distribution of
 * the sum of squares of $k$ independent standard normal random variables with
 * cdf and inverse cdf
 * $$
 * \begin{align*}
 * F(x) &=\frac{\gamma\left(\frac{k}{2}, \frac{x}{2}\right)}{\Gamma\left(\frac{k}{2}\right)}\\
 * F^{-1}(p) &= 2\gamma^{-1}\left(\frac{k}{2}, p\right)
 * \end{align*}
 * $$
 * where $\gamma(y, z)$ is the lower incomplete Gamma function and $\Gamma(y)$
 * is the Gamma function.  The pdf is given by:
 * $$
 * \begin{align*}
 * f(x)=\frac{x^{\frac{k}{2}-1}e^{-\frac{x}{2}}}{2^{\frac{k}{2}}\Gamma\left(\frac{k}{2}\right)}
 * \end{align*}
 * $$
 * 
 */
public class ChiSquareDistribution implements ProbabilityDistribution<Double> {
  private final Function2D<Double, Double> _inverseFunction = new InverseIncompleteGammaFunction();
  private final ChiSquaredDistribution _chiSquare;
  private final double _degrees;

  /**
   * @param degrees The degrees of freedom of the distribution, not less than one
   */
  public ChiSquareDistribution(final double degrees) {
    this(degrees, new Well44497b(new Date().getTime()));
  }

  /**
   * @param degrees The degrees of freedom of the distribution, not less than one
   * @param engine A uniform random number generator, not null
   */
  public ChiSquareDistribution(final double degrees, final RandomGenerator engine) {
    ArgChecker.isTrue(degrees >= 1, "Degrees of freedom must be greater than or equal to one");
    ArgChecker.notNull(engine, "engine");
    _chiSquare = new ChiSquaredDistribution(engine, degrees);
    _degrees = degrees;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getCDF(final Double x) {
    ArgChecker.notNull(x, "x");
    return _chiSquare.cumulativeProbability(x);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getPDF(final Double x) {
    ArgChecker.notNull(x, "x");
    return _chiSquare.density(x);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getInverseCDF(final Double p) {
    ArgChecker.notNull(p, "p");
    ArgChecker.isTrue(p >= 0 && p <= 1, "Probability must lie between 0 and 1");
    return 2 * _inverseFunction.evaluate(0.5 * _degrees, p);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double nextRandom() {
    return _chiSquare.sample();
  }

  /**
   * @return The number of degrees of freedom
   */
  public double getDegreesOfFreedom() {
    return _degrees;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_degrees);
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
    final ChiSquareDistribution other = (ChiSquareDistribution) obj;
    return Double.doubleToLongBits(_degrees) == Double.doubleToLongBits(other._degrees);
  }

}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import com.opengamma.strata.collect.ArgChecker;

/**
 * 
 */
public class ContinuousInterestRate extends InterestRate {
  //  private final double _annualEquivalent;

  public ContinuousInterestRate(final double rate) {
    super(rate);
    //    _annualEquivalent = Math.exp(getRate()) - 1;
  }

  @Override
  public InterestRate fromContinuous(final ContinuousInterestRate continuous) {
    ArgChecker.notNull(continuous, "continuous");
    return new ContinuousInterestRate(continuous.getRate());
  }

  @Override
  public double fromContinuousDerivative(final ContinuousInterestRate continuous) {
    ArgChecker.notNull(continuous, "continuous");
    return 1.0;
  }

  @Override
  public InterestRate fromPeriodic(final PeriodicInterestRate periodic) {
    ArgChecker.notNull(periodic, "periodic");
    final int m = periodic.getCompoundingPeriodsPerYear();
    return new ContinuousInterestRate(m * Math.log(1 + periodic.getRate() / m));
  }

  @Override
  public double getDiscountFactor(final double t) {
    return Math.exp(-getRate() * t);
  }

  @Override
  public ContinuousInterestRate toContinuous() {
    return new ContinuousInterestRate(getRate());
  }

  @Override
  public PeriodicInterestRate toPeriodic(final int periodsPerYear) {
    ArgChecker.notNegativeOrZero(periodsPerYear, "compounding periods per year");
    return new PeriodicInterestRate(periodsPerYear * (Math.exp(getRate() / periodsPerYear) - 1), periodsPerYear);
  }

  @Override
  public String toString() {
    return "Continuous[r = " + getRate() + "]";
  }
}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import com.opengamma.analytics.util.time.Expiry;
import com.opengamma.strata.collect.ArgChecker;

/**
 * 
 * Definition for an asymmetric power options (a.k.a. standard power options).
 * <p>
 * The exercise style is European. The payoff of these options is:
 * $$
 * \begin{align*}
 * c &= \max(S^i - K, 0)\\\\
 * p &= \max(K - S^i, 0)
 * \end{align*}
 * $$
 * where $K$ is the strike, $i$ is the power, with $i > 0$, and $S$ is the spot.
 */
public class AsymmetricPowerOptionDefinition extends OptionDefinition {
  private final OptionPayoffFunction<StandardOptionDataBundle> _payoffFunction = new OptionPayoffFunction<StandardOptionDataBundle>() {

    @Override
    public double getPayoff(final StandardOptionDataBundle data, final Double optionPrice) {
      ArgChecker.notNull(data, "data");
      final double spot = data.getSpot();
      return isCall() ? Math.max(0, Math.pow(spot, getPower()) - getStrike()) : Math.max(0, getStrike() - Math.pow(spot, getPower()));
    }
  };
  private final OptionExerciseFunction<StandardOptionDataBundle> _exerciseFunction = new EuropeanExerciseFunction<>();
  private final double _power;

  /**
   * 
   * @param strike The strike
   * @param expiry The expiry
   * @param power The power, greater than zero
   * @param isCall Is the option a call or put
   */
  public AsymmetricPowerOptionDefinition(final double strike, final Expiry expiry, final double power, final boolean isCall) {
    super(strike, expiry, isCall);
    ArgChecker.isTrue(power > 0, "power must be > 0");
    _power = power;
  }

  /**
   * @return The power.
   */
  public double getPower() {
    return _power;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public OptionExerciseFunction<StandardOptionDataBundle> getExerciseFunction() {
    return _exerciseFunction;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public OptionPayoffFunction<StandardOptionDataBundle> getPayoffFunction() {
    return _payoffFunction;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_power);
    result = prime * result + (int) (temp ^ temp >>> 32);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final AsymmetricPowerOptionDefinition other = (AsymmetricPowerOptionDefinition) obj;
    if (Double.doubleToLongBits(_power) != Double.doubleToLongBits(other._power)) {
      return false;
    }
    return true;
  }
}

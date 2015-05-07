/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.var;

import java.util.Objects;

import com.opengamma.analytics.math.function.Function;
import com.opengamma.strata.collect.ArgChecker;

/**
 *
 * @param <T> The type of the data
 */
public class StudentTLinearVaRCalculator<T> implements VaRCalculator<StudentTVaRParameters, T> {
  private final Function<T, Double> _meanCalculator;
  private final Function<T, Double> _stdCalculator;

  public StudentTLinearVaRCalculator(final Function<T, Double> meanCalculator, final Function<T, Double> stdCalculator) {
    ArgChecker.notNull(meanCalculator, "mean calculator");
    ArgChecker.notNull(stdCalculator, "standard deviation calculator");
    _meanCalculator = meanCalculator;
    _stdCalculator = stdCalculator;
  }

  @SuppressWarnings("unchecked")
  @Override
  public VaRCalculationResult evaluate(final StudentTVaRParameters parameters, final T... data) {
    ArgChecker.notNull(parameters, "parameters");
    ArgChecker.notNull(data, "data");
    final Double value = parameters.getMult() * _stdCalculator.evaluate(data) - parameters.getScale() * _meanCalculator.evaluate(data);
    // Is the "stdCalculator" a standard deviation calculator that we can use for the result?
    return new VaRCalculationResult(value, null);
  }

  public Function<T, Double> getMeanCalculator() {
    return _meanCalculator;
  }

  public Function<T, Double> getStandardDeviationCalculator() {
    return _stdCalculator;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _meanCalculator.hashCode();
    result = prime * result + _stdCalculator.hashCode();
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
    final StudentTLinearVaRCalculator<?> other = (StudentTLinearVaRCalculator<?>) obj;
    return Objects.equals(_meanCalculator, other._meanCalculator) && Objects.equals(_stdCalculator, other._stdCalculator);
  }

}

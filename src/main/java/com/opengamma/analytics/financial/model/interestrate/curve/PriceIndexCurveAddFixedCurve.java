/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.curve;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.opengamma.strata.collect.ArgChecker;

/**
 * PriceIndexCurve created by adding a fixed curve to a price index curve. 
 * No parameter is associated to the fixed curve.
 * The fixed curve can be used for additive seasonal adjustment.
 */
public class PriceIndexCurveAddFixedCurve implements PriceIndexCurve {

  /** The curve name. */
  private final String _name; 
  /**
   * The main underlying curve.
   */
  private final PriceIndexCurve _curve;
  /**
   * The fixed seasonal curve.
   */
  private final SeasonalCurve _seasonalCurve;

  /**
   * Constructor from an array of curves.
   * The new price index curve  will be the sum of the underlying curve and the seasonal curve.
   * @param name The curve name.
   * @param curve The main curve.
   * @param seasonalCurve The fixed curve (as a spread).
   */
  public PriceIndexCurveAddFixedCurve(final String name, final PriceIndexCurve curve, final SeasonalCurve seasonalCurve) {
    ArgChecker.notNull(curve, "Curve");
    ArgChecker.notNull(seasonalCurve, "Curve fixed");
    _name = name;
    _curve = curve;
    _seasonalCurve = seasonalCurve;
  }

  @Override
  public double getPriceIndex(final Double timeToIndex) {
    return _curve.getPriceIndex(timeToIndex) + _seasonalCurve.getFunction().evaluate(timeToIndex);
  }

  @Override
  public double getInflationRate(final Double firstTime, final Double secondTime) {
    ArgChecker.isTrue(firstTime < secondTime, "firstTime should be before secondTime");
    return this.getPriceIndex(secondTime) / this.getPriceIndex(firstTime) - 1.0;
  }

  @Override
  public double[] getPriceIndexParameterSensitivity(final double time) {
    return _curve.getPriceIndexParameterSensitivity(time);
  }

  @Override
  public int getNumberOfParameters() {
    return _curve.getNumberOfParameters();
  }

  @Override
  public List<String> getUnderlyingCurvesNames() {
    return new ArrayList<>();
  }

  @Override
  public String getName() {
    return _name;
  }

  @Override
  public int getNumberOfIntrinsicParameters(Set<String> curvesNames) {
    return _curve.getNumberOfIntrinsicParameters(curvesNames);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _curve.hashCode();
    result = prime * result + _seasonalCurve.hashCode();
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
    final PriceIndexCurveAddFixedCurve other = (PriceIndexCurveAddFixedCurve) obj;
    if (!Objects.equals(_curve, other._curve)) {
      return false;
    }
    if (!Objects.equals(_seasonalCurve, other._seasonalCurve)) {
      return false;
    }
    return true;
  }

}

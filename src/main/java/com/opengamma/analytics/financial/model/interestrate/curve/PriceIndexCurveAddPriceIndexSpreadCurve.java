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
 *  PriceIndexCurve created by adding the price index of other curves.
 */
public class PriceIndexCurveAddPriceIndexSpreadCurve implements PriceIndexCurve {

  /** The curve name. */
  private final String _name;
  /**
   * The array of underlying curves.
   */
  private final PriceIndexCurve[] _curves;

  /**
   * If -1 the rate of all curves, except the first one, will be subtracted from the first one. If +1, all the rates are added.
   */
  private final double _sign;

  /**
   * Constructor from an array of curves.
   * The new price index curve  will be the sum (or the difference) of the different underlying curves.
   * @param name The curve name.
   * @param substract If true, the rate of all curves, except the first one, will be subtracted from the first one. If false, all the rates are added.
   * @param curves  The array of underlying curves.

   */
  public PriceIndexCurveAddPriceIndexSpreadCurve(String name, boolean substract, PriceIndexCurve... curves) {
    ArgChecker.notNull(curves, "Curves");
    _name = name;
    _sign = substract ? -1.0 : 1.0;
    _curves = curves;
  }

  @Override
  public double getPriceIndex(Double timeToIndex) {
    double priceIndex = _curves[0].getPriceIndex(timeToIndex);
    for (int loopcurve = 1; loopcurve < _curves.length; loopcurve++) {
      priceIndex += _sign * _curves[loopcurve].getPriceIndex(timeToIndex);
    }
    return priceIndex;
  }

  @Override
  public double getInflationRate(Double firstTime, Double secondTime) {
    ArgChecker.isTrue(firstTime < secondTime, "firstTime should be before secondTime");
    return this.getPriceIndex(secondTime) / this.getPriceIndex(firstTime) - 1.0;
  }

  @Override
  public double[] getPriceIndexParameterSensitivity(double time) {
    // calculate size of the result
    int size = 0;
    for (int loopcurve = 0; loopcurve < _curves.length; loopcurve++) {
      size += _curves[loopcurve].getPriceIndexParameterSensitivity(time).length;
    }
    // create result
    double[] result = new double[size];
    int i = 0;
    for (int loopcurve = 0; loopcurve < _curves.length; loopcurve++) {
      double[] temp = _curves[loopcurve].getPriceIndexParameterSensitivity(time);
      for (double element : temp) {
        result[i++] = element;
      }
    }
    return result;
  }

  @Override
  public int getNumberOfParameters() {
    int result = 0;
    for (PriceIndexCurve curve : _curves) {
      result += curve.getNumberOfParameters();
    }
    return result;
  }

  @Override
  public List<String> getUnderlyingCurvesNames() {
    List<String> names = new ArrayList<>();
    for (PriceIndexCurve curve : _curves) {
      names.add(curve.getName());
    }
    return names;
  }

  public PriceIndexCurve[] getCurves() {
    return _curves;
  }

  @Override
  public String getName() {
    return _name;
  }

  @Override
  public int getNumberOfIntrinsicParameters(Set<String> curvesNames) {
    int nb = 0;
    for (int loopcurve = 0; loopcurve < _curves.length; loopcurve++) {
      nb += _curves[loopcurve].getNumberOfIntrinsicParameters(curvesNames);
    }
    return nb;
  }

  @Override
  public int hashCode() {
    int prime = 31;
    int result = 1;
    result = prime * result + _curves.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_sign);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    PriceIndexCurveAddPriceIndexSpreadCurve other = (PriceIndexCurveAddPriceIndexSpreadCurve) obj;
    if (!Objects.equals(_curves, other._curves)) {
      return false;
    }
    if (Double.doubleToLongBits(_sign) != Double.doubleToLongBits(other._sign)) {
      return false;
    }
    return true;
  }

}

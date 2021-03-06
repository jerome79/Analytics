/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.curve;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.opengamma.strata.collect.ArgChecker;

/**
 * YieldAndDiscountCurve created by adding the zero-coupon continuously compounded rate of other curves.
 */
public class YieldAndDiscountAddZeroSpreadCurve extends YieldAndDiscountCurve {

  /**
   * The array of underlying curves.
   */
  private final YieldAndDiscountCurve[] _curves;
  /**
   * If -1 the rate of all curves, except the first one, will be subtracted from the first one. If +1, all the rates are added.
   */
  private final double _sign;

  /**
   * Constructor from an array of curves.
   * The new curve interest rate (zero-coupon continuously compounded) will be the sum (or the difference) of the different underlying curves.
   * @param name The curve name.
   * @param subtract If true, the rate of all curves, except the first one, will be subtracted from the first one. If false, all the rates are added.
   * @param curves The array of underlying curves.
   */
  public YieldAndDiscountAddZeroSpreadCurve(String name, boolean subtract, YieldAndDiscountCurve... curves) {
    super(name);
    ArgChecker.notNull(curves, "Curves");
    _sign = subtract ? -1.0 : 1.0;
    _curves = curves;
  }

  @Override
  public double getInterestRate(Double t) {
    double rate = _curves[0].getInterestRate(t);
    for (int loopcurve = 1; loopcurve < _curves.length; loopcurve++) {
      rate += _sign * _curves[loopcurve].getInterestRate(t);
    }
    return rate;
  }

  @Override
  public double getForwardRate(double t) {
    double f = _curves[0].getForwardRate(t);
    for (int loopcurve = 1; loopcurve < _curves.length; loopcurve++) {
      f += _sign * _curves[loopcurve].getForwardRate(t);
    }
    return f;
  }

  @Override
  public double[] getInterestRateParameterSensitivity(double time) {
    // calculate size of the result
    int size = 0;
    for (int loopcurve = 0; loopcurve < _curves.length; loopcurve++) {
      size += _curves[loopcurve].getInterestRateParameterSensitivity(time).length;
    }
    // create result
    double[] result = new double[size];
    int i = 0;
    for (int loopcurve = 0; loopcurve < _curves.length; loopcurve++) {
      double[] temp = _curves[loopcurve].getInterestRateParameterSensitivity(time);
      for (double element : temp) {
        result[i++] = element;
      }
    }
    return result;
  }

  @Override
  public int getNumberOfParameters() {
    int result = 0;
    for (YieldAndDiscountCurve curve : _curves) {
      result += curve.getNumberOfParameters();
    }
    return result;
  }

  @Override
  public int getNumberOfIntrinsicParameters(Set<String> curvesNames) {
    int result = 0;
    for (YieldAndDiscountCurve curve : _curves) {
      if (!curvesNames.contains(curve.getName())) {
        result += curve.getNumberOfParameters();
      }
    }
    return result;
  }

  @Override
  public List<String> getUnderlyingCurvesNames() {
    List<String> names = new ArrayList<>();
    for (YieldAndDiscountCurve curve : _curves) {
      names.add(curve.getName());
    }
    return names;
  }

  /**
   * Gets all of the curves.
   * @return The curves
   */
  public YieldAndDiscountCurve[] getCurves() {
    return _curves;
  }

  /**
   * Returns +1 if the curves are to be added to the base curve or -1 if the curves are to be subtracted
   * from the base curve
   * @return +/-1 depending on the operation
   */
  public double getSign() {
    return _sign;
  }

  @Override
  public int hashCode() {
    int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_curves);
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
    YieldAndDiscountAddZeroSpreadCurve other = (YieldAndDiscountAddZeroSpreadCurve) obj;
    if (!Arrays.equals(_curves, other._curves)) {
      return false;
    }
    if (Double.doubleToLongBits(_sign) != Double.doubleToLongBits(other._sign)) {
      return false;
    }
    return true;
  }

}

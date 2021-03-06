/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr;

import java.util.Arrays;

import com.opengamma.strata.collect.ArgChecker;

/**
 * 
 */
public class EuropeanOptionPrices {

  private final double[] _expiries;
  private final double[][] _strikes;
  private final double[][] _otmPrices;
  private final int _nExpiries;

  public EuropeanOptionPrices(double[] expiries, double[][] strikes, double[][] otmPrices) {
    ArgChecker.notNull(expiries, "expiries");
    ArgChecker.notNull(strikes, "strikes");
    ArgChecker.notNull(otmPrices, "otmPrices");
    _nExpiries = expiries.length;
    ArgChecker.isTrue(_nExpiries > 0, "Need at least one expiry, given {} ", _nExpiries);
    ArgChecker.isTrue(strikes.length == _nExpiries, "number of strike strips ({}) not equal to number of expiries({})", strikes.length, _nExpiries);
    ArgChecker.isTrue(otmPrices.length == _nExpiries, "number of price strips ({}) not equal to number of expiries({})", strikes.length, _nExpiries);
    for (int i = 0; i < _nExpiries; i++) {
      ArgChecker.isTrue(strikes[i].length == otmPrices[i].length, "number of prices and strikes in strip #{} (expiry = {}) do not match. {} prices and {} strikes", i, expiries[i],
          otmPrices[i].length,
          strikes[i].length);
    }

    //do deep copy of inputs
    _expiries = Arrays.copyOf(expiries, _nExpiries);
    _strikes = new double[_nExpiries][];
    _otmPrices = new double[_nExpiries][];
    for (int i = 0; i < _nExpiries; i++) {
      _strikes[i] = Arrays.copyOf(strikes[i], strikes[i].length);
      _otmPrices[i] = Arrays.copyOf(otmPrices[i], otmPrices[i].length);
    }
  }

  public int getNumExpiries() {
    return _nExpiries;
  }

  public double[] getExpiries() {
    return _expiries;
  }

  public double[][] getStrikes() {
    return _strikes;
  }

  public double[][] getOTMPrices() {
    return _otmPrices;
  }

  public EuropeanOptionPrices withBumpedPoint(final int expiryIndex, final int strikeIndex, final double amount) {
    ArgChecker.inRange(expiryIndex, 0d, _nExpiries, "expiryIndex");
    ArgChecker.inRange(strikeIndex, 0d, _strikes[expiryIndex].length, "strikeIndex");

    double[] p = Arrays.copyOf(_otmPrices[expiryIndex], _otmPrices[expiryIndex].length);
    p[strikeIndex] += amount;

    double[][] temp = _otmPrices;
    temp[expiryIndex] = p;

    return new EuropeanOptionPrices(_expiries, _strikes, temp);
  }
}

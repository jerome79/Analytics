/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.method;

import java.util.Arrays;
import java.util.Objects;

import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.collect.ArgChecker;
import com.opengamma.strata.collect.tuple.Pair;

/**
 * Class describing the present value sensitivity to a Forex currency pair quoted volatility parameters (ATM, RR, Strangle).
 */
public class PresentValueForexBlackVolatilityQuoteSensitivityDataBundle {

  /**
   * The currency pair.
   */
  private final Pair<Currency, Currency> _currencyPair;
  /**
   * The volatility sensitivity as a matrix with same dimension as the input. The sensitivity value is in second/domestic currency.
   */
  private final double[][] _vega;
  private final double[] _expiries;
  private final double[] _delta;

  /**
   * Constructor with initial sensitivities for a given currency pair.
   * @param ccy1 First currency, not null
   * @param ccy2 Second currency, not null
   * @param expiries The expiries for the vega matrix, not null
   * @param delta The deltas for the vega matrix, not null
   * @param vega The initial sensitivity, not null
   */
  public PresentValueForexBlackVolatilityQuoteSensitivityDataBundle(final Currency ccy1, final Currency ccy2, final double[] expiries, final double[] delta, final double[][] vega) {
    ArgChecker.notNull(ccy1, "currency 1");
    ArgChecker.notNull(ccy2, "currency 2");
    ArgChecker.notNull(expiries, "expiries");
    ArgChecker.notNull(delta, "delta");
    ArgChecker.notNull(vega, "Matrix");
    ArgChecker.isTrue(vega.length == expiries.length, "Number of rows did not match number of expiries");
    ArgChecker.isTrue(vega[0].length == delta.length, "Number of columns did not match number of delta");
    _currencyPair = Pair.of(ccy1, ccy2);
    _expiries = expiries;
    _delta = delta;
    _vega = vega;
  }

  /**
   * Gets the currency pair.
   * @return The currency pair.
   */
  public Pair<Currency, Currency> getCurrencyPair() {
    return _currencyPair;
  }

  /**
   * Gets the volatility sensitivity (vega) map.
   * @return The sensitivity.
   */
  public double[][] getVega() {
    return _vega;
  }

  public double[] getExpiries() {
    return _expiries;
  }

  public double[] getDelta() {
    return _delta;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _currencyPair.hashCode();
    result = prime * result + Arrays.hashCode(_expiries);
    result = prime * result + Arrays.hashCode(_delta);
    result = prime * result + Arrays.hashCode(_vega);
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
    PresentValueForexBlackVolatilityQuoteSensitivityDataBundle other = (PresentValueForexBlackVolatilityQuoteSensitivityDataBundle) obj;
    if (!Objects.equals(_currencyPair, other._currencyPair)) {
      return false;
    }
    if (!Arrays.equals(_delta, other._delta)) {
      return false;
    }
    if (!Arrays.equals(_expiries, other._expiries)) {
      return false;
    }
    if (!Arrays.equals(_vega, other._vega)) {
      return false;
    }
    return true;
  }

}

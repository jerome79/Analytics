/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.parameters;

import java.util.Arrays;
import java.util.Objects;

import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.strata.collect.ArgChecker;

/**
 * _atmVolatility
 */
public class InflationConvexityAdjustmentParameters {

  /**
   * The times separating the inflation periods.
   */
  private final double[] _inflationTime;
  /**
   * The ATM volatility curve of the price index. The dimensions are the expiration. Not null.
   */
  private final double[] _atmVolatility;

  /**
   * The price index correlation surface. The dimensions are the expiration. Not null.
   */
  private final Surface<Double, Double, Double> _priceIndexCorrelation;

  /**
   * The libor correlation surface. The dimensions are the expiration. Not null.
   */
  private final Surface<Double, Double, Double> _liborCorrelation;

  /**
   * The price index\rate volatility surface. The dimensions are the expiration. Not null.
   */
  private final DoublesCurve _priceIndexRateCorrelation;
  /**
   * The index  price for which the volatility is valid. Not null.
   */
  private final IndexPrice _index;

  /**
   * Constructor from the parameter surfaces.
   * @param inflationTime the inflation times.
   * @param atmVolatility The atm Black volatility curve.
   * @param liborCorrelation the inter libor correlation
   * @param priceIndexCorrelation The price index correlation surface.
   * @param priceIndexRateCorrelation  The price index\rate volatility surface.
   * @param index The index price for which the volatility is valid.
   */
  public InflationConvexityAdjustmentParameters(final double[] inflationTime, final double[] atmVolatility, final Surface<Double, Double, Double> priceIndexCorrelation,
      final Surface<Double, Double, Double> liborCorrelation, final DoublesCurve priceIndexRateCorrelation, final IndexPrice index) {
    ArgChecker.notNull(inflationTime, "inflation time");
    ArgChecker.notNull(atmVolatility, "price index correlation surface");
    ArgChecker.notNull(priceIndexCorrelation, "price index correlation");
    ArgChecker.notNull(liborCorrelation, "Libor correlation");
    ArgChecker.notNull(priceIndexRateCorrelation, "price index\rate volatility");
    ArgChecker.notNull(index, "index price");
    _inflationTime = inflationTime;
    _atmVolatility = atmVolatility;
    _priceIndexCorrelation = priceIndexCorrelation;
    _liborCorrelation = liborCorrelation;
    _priceIndexRateCorrelation = priceIndexRateCorrelation;
    _index = index;
  }

  /**
   * Return the volatility for a time to expiration and strike.
   * @return The atm volatility.
   */
  public double[] getInflationTime() {
    return _inflationTime;
  }

  /**
   * Return the volatility for a time to expiration and strike.
   * @return The atm volatility.
   */
  public double[] getPriceIndexAtmVolatility() {
    return _atmVolatility;
  }

  /**
   * Return the price index correlation surface.
   * @return The price index correlation surface.
   */
  public Surface<Double, Double, Double> getPriceIndexCorrelation() {
    return _priceIndexCorrelation;
  }

  /**
   * Return the libor correlation surface.
   * @return The libor correlation surface.
   */
  public Surface<Double, Double, Double> getLiborCorrelation() {
    return _liborCorrelation;
  }

  /**
   * Return the libor/price index correlation vector.
   * @return  The libor/price index correlation vector.
   */
  public DoublesCurve getPriceIndexRateCorrelation() {
    return _priceIndexRateCorrelation;
  }

  /**
   * Gets the Ibor index for which the volatility is valid.
   * @return The index.
   */
  public IndexPrice getPriceIndex() {
    return _index;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_atmVolatility);
    result = prime * result + _index.hashCode();
    result = prime * result + Arrays.hashCode(_inflationTime);
    result = prime * result + _liborCorrelation.hashCode();
    result = prime * result + _priceIndexCorrelation.hashCode();
    result = prime * result + _priceIndexRateCorrelation.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof InflationConvexityAdjustmentParameters)) {
      return false;
    }
    final InflationConvexityAdjustmentParameters other = (InflationConvexityAdjustmentParameters) obj;
    if (!Arrays.equals(_inflationTime, other._inflationTime)) {
      return false;
    }
    if (!Arrays.equals(_atmVolatility, other._atmVolatility)) {
      return false;
    }
    if (!Objects.equals(_index, other._index)) {
      return false;
    }
    if (!Objects.equals(_liborCorrelation, other._liborCorrelation)) {
      return false;
    }
    if (!Objects.equals(_priceIndexCorrelation, other._priceIndexCorrelation)) {
      return false;
    }
    if (!Objects.equals(_priceIndexRateCorrelation, other._priceIndexRateCorrelation)) {
      return false;
    }
    return true;
  }

}

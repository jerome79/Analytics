/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.parameters;

import java.util.Objects;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.model.volatility.VolatilityModel;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Class describing the Black volatility surface used in swaption modeling.
 */
public class BlackFlatCapFloorParameters implements VolatilityModel<double[]> {

  /**
   * The volatility curve. The dimension is the expiration. Not null.
   */
  private final DoublesCurve _volatility;
  /**
   * The Ibor index for which the volatility is valid. Not null.
   */
  private final IborIndex _index;

  /**
   * Constructor from the parameter surfaces. The default SABR volatility formula is HaganVolatilityFunction.
   * @param volatility The Black volatility curve.
   * @param index The Ibor index for which the volatility is valid.
   */
  public BlackFlatCapFloorParameters(final DoublesCurve volatility, final IborIndex index) {
    ArgChecker.notNull(volatility, "volatility");
    ArgChecker.notNull(index, "index");
    _volatility = volatility;
    _index = index;
  }

  /**
   * Return the volatility for a time to expiration.
   * @param expiration The time to expiration.
   * @return The volatility.
   */
  public double getVolatility(final double expiration) {
    return _volatility.getYValue(expiration);
  }

  @Override
  /**
   * Return the volatility for a expiration tenor array.
   * @param data An array of one doubles with the expiration.
   * @return The volatility.
   */
  public Double getVolatility(final double[] data) {
    ArgChecker.notNull(data, "data");
    ArgChecker.isTrue(data.length == 1, "data should have one components (expiration)");
    return getVolatility(data[0]);
  }

  /**
   * Gets the Ibor index for which the volatility is valid.
   * @return The index.
   */
  public IborIndex getIndex() {
    return _index;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _index.hashCode();
    result = prime * result + _volatility.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof BlackFlatCapFloorParameters)) {
      return false;
    }
    final BlackFlatCapFloorParameters other = (BlackFlatCapFloorParameters) obj;
    if (!Objects.equals(_index, other._index)) {
      return false;
    }
    if (!Objects.equals(_volatility, other._volatility)) {
      return false;
    }
    return true;
  }

}

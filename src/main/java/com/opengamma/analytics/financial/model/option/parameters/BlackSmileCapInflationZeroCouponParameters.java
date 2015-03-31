/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.parameters;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.model.interestrate.definition.InflationZeroCouponCapFloorParameters;
import com.opengamma.analytics.financial.model.volatility.VolatilityModel;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.interpolation.Interpolator2D;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.strata.collect.ArgChecker;
import com.opengamma.strata.collect.tuple.DoublesPair;

/**
 *  Class describing the Black volatility surface used in inflation zero-coupon cap/floor modeling. The CPI forward is assumed to log normal.
 */
public class BlackSmileCapInflationZeroCouponParameters implements VolatilityModel<double[]> {

  /**
   * The volatility surface. The dimensions are the expiration and the strike. Not null.
   */
  private final InterpolatedDoublesSurface _volatility;
  /**
   * The Ibor index for which the volatility is valid. Not null.
   */
  private final IndexPrice _index;

  /**
   * Constructor from the parameter surfaces.
   * @param volatility The Black volatility curve.
   * @param index The Ibor index for which the volatility is valid.
   */
  public BlackSmileCapInflationZeroCouponParameters(final InterpolatedDoublesSurface volatility, final IndexPrice index) {
    ArgChecker.notNull(volatility, "volatility");
    ArgChecker.notNull(index, "index");
    _volatility = volatility;
    _index = index;
  }

  /**
   * Constructor from the parameter surfaces.
   * @param expiryTimes The Black volatility curve.
   * @param strikes The Black volatility curve.
   * @param volatility The Black volatility cube.
   * @param interpolator The interpolator necessary to Black volatility surface from the black volatility cube.
   * @param index The index price for which the volatility is valid.
   */
  public BlackSmileCapInflationZeroCouponParameters(final double[] expiryTimes, final double[] strikes, final double[][] volatility, final Interpolator2D interpolator, final IndexPrice index) {
    ArgChecker.notNull(volatility, "volatility");
    ArgChecker.notNull(expiryTimes, "expiryTimes");
    ArgChecker.notNull(strikes, "strikes");
    ArgChecker.notNull(index, "index");
    ArgChecker.isTrue(expiryTimes.length == volatility.length, null);
    ArgChecker.isTrue(strikes.length == volatility[0].length, null);

    final DoublesPair[] xyData = new DoublesPair[expiryTimes.length * strikes.length];
    final double[] volatilityVector = new double[expiryTimes.length * strikes.length];
    for (int i = 0; i < expiryTimes.length; i++) {
      for (int j = 0; j < strikes.length; j++) {
        xyData[i + j * expiryTimes.length] = DoublesPair.of(expiryTimes[i], strikes[j]);
        volatilityVector[i + j * expiryTimes.length] = volatility[i][j];
      }
    }
    _volatility = InterpolatedDoublesSurface.from(xyData, volatilityVector, interpolator);
    _index = index;
  }

  /**
   * Constructor from the parameter surfaces.
   * @param parameters The Black volatility curve.
   * @param interpolator The Black volatility curve.
   */
  public BlackSmileCapInflationZeroCouponParameters(final InflationZeroCouponCapFloorParameters parameters, final Interpolator2D interpolator) {
    ArgChecker.notNull(interpolator, "interpolator");

    final double[] expiryTimes = parameters.getExpiryTimes();
    final double[] strikes = parameters.getStrikes();
    final double[][] volatility = parameters.getVolatility();

    final DoublesPair[] xyData = new DoublesPair[expiryTimes.length * strikes.length];
    final double[] volatilityVector = new double[expiryTimes.length * strikes.length];
    for (int i = 0; i < expiryTimes.length; i++) {
      for (int j = 0; j < strikes.length; j++) {
        xyData[i + j * expiryTimes.length] = DoublesPair.of(expiryTimes[i], strikes[j]);
        volatilityVector[i + j * expiryTimes.length] = volatility[i][j];
      }
    }
    _volatility = InterpolatedDoublesSurface.from(xyData, volatilityVector, interpolator);
    _index = parameters.getIndex();

  }

  /**
   * Constructor from the parameter surfaces and default interpolator (flat extrapolation, linear interpolation).
   * @param parameters The Black volatility curve.
   */
  public BlackSmileCapInflationZeroCouponParameters(final InflationZeroCouponCapFloorParameters parameters) {

    final double[] expiryTimes = parameters.getExpiryTimes();
    final double[] strikes = parameters.getStrikes();
    final double[][] volatility = parameters.getVolatility();

    final DoublesPair[] xyData = new DoublesPair[expiryTimes.length * strikes.length];
    final double[] volatilityVector = new double[expiryTimes.length * strikes.length];
    for (int i = 0; i < expiryTimes.length; i++) {
      for (int j = 0; j < strikes.length; j++) {
        xyData[i + j * expiryTimes.length] = DoublesPair.of(expiryTimes[i], strikes[j]);
        volatilityVector[i + j * expiryTimes.length] = volatility[i][j];
      }
    }

    final Interpolator1D linearFlat = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
        Interpolator1DFactory.FLAT_EXTRAPOLATOR);
    final GridInterpolator2D interpolator = new GridInterpolator2D(linearFlat, linearFlat);
    _volatility = InterpolatedDoublesSurface.from(xyData, volatilityVector, interpolator);
    _index = parameters.getIndex();
  }

  /**
   * Return the volatility surface.
   * @return The volatility surface.
   */
  public InterpolatedDoublesSurface getVolatilitySurface() {
    return _volatility;
  }

  /**
   * Return the volatility for a time to expiration and strike.
   * @param expiration The time to expiration.
   * @param strike The strike.
   * @return The volatility.
   */
  public double getVolatility(final double expiration, final double strike) {
    return _volatility.getZValue(expiration, strike);
  }

  @Override
  /**
   * Return the volatility for a expiration tenor array.
   * @param data An array of one doubles with the expiration.
   * @return The volatility.
   */
  public Double getVolatility(final double[] data) {
    ArgChecker.notNull(data, "data");
    ArgChecker.isTrue(data.length == 2, "data should have two components (expiration and strike)");
    return getVolatility(data[0], data[1]);
  }

  /**
   * Gets the Ibor index for which the volatility is valid.
   * @return The index.
   */
  public IndexPrice getIndex() {
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
    if (!(obj instanceof BlackSmileCapInflationZeroCouponParameters)) {
      return false;
    }
    final BlackSmileCapInflationZeroCouponParameters other = (BlackSmileCapInflationZeroCouponParameters) obj;
    if (!ObjectUtils.equals(_index, other._index)) {
      return false;
    }
    if (!ObjectUtils.equals(_volatility, other._volatility)) {
      return false;
    }
    return true;
  }

}

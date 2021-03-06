/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr;

import java.util.Arrays;
import java.util.Objects;

import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.util.ArrayUtils;
import com.opengamma.strata.collect.ArgChecker;

/**
 * 
 */
public class StandardSmileSurfaceDataBundle extends SmileSurfaceDataBundle {
  private final double[] _forwards;
  private final double[] _expiries;
  private final double[][] _strikes;
  private final double[][] _impliedVols;
  private final ForwardCurve _forwardCurve;
  private final int _nExpiries;

  public StandardSmileSurfaceDataBundle(final double spot, final double[] forwards, final double[] expiries, final double[][] strikes, final double[][] impliedVols,
      final Interpolator1D forwardCurveInterpolator) {
    ArgChecker.notNull(forwards, "forwards");
    ArgChecker.notNull(expiries, "expiries");
    ArgChecker.notNull(strikes, "strikes");
    ArgChecker.notNull(impliedVols, "implied volatilities");
    ArgChecker.notNull(forwardCurveInterpolator, "forward curve interpolator");
    _nExpiries = expiries.length;
    ArgChecker.isTrue(_nExpiries == forwards.length, "forwards wrong length; have {}, need {}", forwards.length, _nExpiries);
    ArgChecker.isTrue(_nExpiries == strikes.length, "strikes wrong length; have {}, need {}", strikes.length, _nExpiries);
    ArgChecker.isTrue(_nExpiries == impliedVols.length, "implied volatilities wrong length; have {}, need {}", impliedVols.length, _nExpiries);
    for (int i = 0; i < strikes.length; i++) {
      ArgChecker.isTrue(strikes[i].length == impliedVols[i].length, "implied volatilities for expiry {} not the same length as strikes; have {}, need {}",
          strikes[i].length, impliedVols[i].length);
    }
    // checkVolatilities(expiries, strikes, impliedVols); // Put this check in place, if desired, after construction.
    _expiries = expiries;
    _forwards = forwards;

    final double[] t = ArrayUtils.add(expiries, 0, 0.0);
    final double[] f = ArrayUtils.add(forwards, 0, spot);
    _forwardCurve = new ForwardCurve(InterpolatedDoublesCurve.from(t, f, forwardCurveInterpolator));
    _strikes = strikes;
    _impliedVols = impliedVols;
  }

  public StandardSmileSurfaceDataBundle(final ForwardCurve forwardCurve, final double[] expiries, final double[][] strikes, final double[][] impliedVols) {
    ArgChecker.notNull(forwardCurve, "forward curve");
    ArgChecker.notNull(expiries, "expiries");
    ArgChecker.notNull(strikes, "strikes");
    ArgChecker.notNull(impliedVols, "implied volatilities");
    _nExpiries = expiries.length;
    ArgChecker.isTrue(_nExpiries == strikes.length, "strikes wrong length; have {}, need {}", _nExpiries, strikes.length);
    ArgChecker.isTrue(_nExpiries == impliedVols.length, "implied volatilities wrong length; have {}, need {}", _nExpiries, impliedVols.length);
    _forwards = new double[_nExpiries];
    for (int i = 0; i < _nExpiries; i++) {
      ArgChecker.isTrue(strikes[i].length == impliedVols[i].length, "implied volatilities for expiry {} not the same length as strikes; have {}, need {}",
          strikes[i].length, impliedVols[i].length);
      _forwards[i] = forwardCurve.getForward(expiries[i]);
    }
    // checkVolatilities(expiries, strikes, impliedVols); // Put this check in place, if desired, after construction.
    _expiries = expiries;
    _strikes = strikes;
    _impliedVols = impliedVols;
    _forwardCurve = forwardCurve;
  }

  @Override
  public int getNumExpiries() {
    return _nExpiries;
  }

  @Override
  public double[] getExpiries() {
    return _expiries;
  }

  @Override
  public double[][] getStrikes() {
    return _strikes;
  }

  @Override
  public double[][] getVolatilities() {
    return _impliedVols;
  }

  @Override
  public double[] getForwards() {
    return _forwards;
  }

  @Override
  public ForwardCurve getForwardCurve() {
    return _forwardCurve;
  }

  @Override
  public SmileSurfaceDataBundle withBumpedPoint(final int expiryIndex, final int strikeIndex, final double amount) {
    ArgChecker.inRange(expiryIndex, 0d, _nExpiries, "expiryIndex");
    final double[][] strikes = getStrikes();
    ArgChecker.inRange(strikeIndex, 0d, strikes[expiryIndex].length, "strikeIndex");
    final double[][] vols = new double[_nExpiries][];
    for (int i = 0; i < _nExpiries; i++) {
      final int nStrikes = strikes[i].length;
      vols[i] = new double[nStrikes];
      System.arraycopy(_impliedVols[i], 0, vols[i], 0, nStrikes);
    }
    vols[expiryIndex][strikeIndex] += amount;
    return new StandardSmileSurfaceDataBundle(getForwardCurve(), getExpiries(), getStrikes(), vols);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _forwardCurve.hashCode();
    result = prime * result + Arrays.deepHashCode(_impliedVols);

    result = prime * result + Arrays.deepHashCode(_strikes);
    result = prime * result + Arrays.hashCode(_expiries);
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
    final StandardSmileSurfaceDataBundle other = (StandardSmileSurfaceDataBundle) obj;
    if (!Objects.equals(_forwardCurve, other._forwardCurve)) {
      return false;
    }
    if (!Arrays.equals(_expiries, other._expiries)) {
      return false;
    }
    for (int i = 0; i < _nExpiries; i++) {
      if (!Arrays.equals(_strikes[i], other._strikes[i])) {
        return false;
      }
      if (!Arrays.equals(_impliedVols[i], other._impliedVols[i])) {
        return false;
      }
    }

    return true;
  }
}

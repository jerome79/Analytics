/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.pnl;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.opengamma.analytics.financial.greeks.Underlying;
import com.opengamma.analytics.financial.sensitivity.Sensitivity;
import com.opengamma.analytics.util.timeseries.DoubleTimeSeries;
import com.opengamma.strata.collect.ArgChecker;

/**
 * 
 */
public class SensitivityAndReturnDataBundle {
  private final Sensitivity<?> _sensitivity;
  private final double _value;
  private final Map<UnderlyingType, DoubleTimeSeries<?>> _underlyingReturnTS;
  private final List<UnderlyingType> _underlyings;

  public SensitivityAndReturnDataBundle(
      Sensitivity<?> sensitivity,
      double value,
      Map<UnderlyingType, DoubleTimeSeries<?>> underlyingReturnTS) {

    ArgChecker.notNull(sensitivity, "sensitivity");
    ArgChecker.notNull(underlyingReturnTS, "underlying returns");
    ArgChecker.notEmpty(underlyingReturnTS, "underlying returns");
    ArgChecker.noNulls(underlyingReturnTS.keySet(), "underlying return key set");
    ArgChecker.noNulls(underlyingReturnTS.values(), "underlying return values");
    _underlyings = sensitivity.getUnderlyingTypes();
    ArgChecker.isTrue(_underlyings.size() == underlyingReturnTS.size(), "underlying sizes must match");
    ArgChecker.isTrue(_underlyings.containsAll(underlyingReturnTS.keySet()), "underlyings key sets must match");
    _sensitivity = sensitivity;
    _value = value;
    _underlyingReturnTS = underlyingReturnTS;
  }

  public Sensitivity<?> getSensitivity() {
    return _sensitivity;
  }

  public double getValue() {
    return _value;
  }

  public Map<UnderlyingType, DoubleTimeSeries<?>> getUnderlyingReturnTS() {
    return _underlyingReturnTS;
  }

  public List<UnderlyingType> getUnderlyingTypes() {
    return _underlyings;
  }

  public Underlying getUnderlying() {
    return _sensitivity.getUnderlying();
  }

  public DoubleTimeSeries<?> getReturnTimeSeriesForUnderlying(UnderlyingType type) {
    ArgChecker.notNull(type, "underlying");
    DoubleTimeSeries<?> result = _underlyingReturnTS.get(type);
    ArgChecker.notNull(result, "underlying return time series for " + type);
    return result;
  }

  @Override
  public int hashCode() {
    int prime = 31;
    int result = 1;
    result = prime * result + ((_sensitivity == null) ? 0 : _sensitivity.hashCode());
    result = prime * result + ((_underlyingReturnTS == null) ? 0 : _underlyingReturnTS.hashCode());
    long temp;
    temp = Double.doubleToLongBits(_value);
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
    SensitivityAndReturnDataBundle other = (SensitivityAndReturnDataBundle) obj;
    return Objects.equals(_sensitivity, other._sensitivity) &&
        Objects.equals(_underlyingReturnTS, other._underlyingReturnTS) &&
        Objects.equals(_value, other._value);
  }

}

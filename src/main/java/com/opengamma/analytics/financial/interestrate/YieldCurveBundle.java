/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.currency.FxMatrix;
import com.opengamma.strata.collect.ArgChecker;
/**
 * A bundle of curves and forex exchange rates used for pricing. The curves are stored as a map <String, YieldAndDiscountCurve>.
 * @deprecated Use {@link MulticurveProviderDiscount}
 */
@Deprecated
public class YieldCurveBundle {

  /**
   * The map used to store the curves.
   */
  private final Map<String, YieldAndDiscountCurve> _curves;
  /**
   * A map linking each curve in the bundle to its currency.
   */
  private final Map<String, Currency> _curveCurrency;
  /**
   * The matrix containing the exchange rates.
   */
  private final FxMatrix _fxMatrix;

  /**
   * Constructor. An empty map is created for the curves and an empty FXMatrix.
   */
  public YieldCurveBundle() {
    _curves = new LinkedHashMap<>();
    _curveCurrency = new HashMap<>();
    _fxMatrix = FxMatrix.empty();
  }

  /**
   * Constructor from existing currency map and existing fxMatrix.
   * @param fxMatrix The FXMatrix.
   * @param curveCurrency The map of currency names to currency
   * @param curvesMap The map of curve names to curve
   */
  public YieldCurveBundle(final FxMatrix fxMatrix, final Map<String, Currency> curveCurrency, final Map<String, ? extends YieldAndDiscountCurve> curvesMap) {

    ArgChecker.notNull(curveCurrency, "curve currency");
    _curves = new LinkedHashMap<>();
    if (curvesMap != null) {
      ArgChecker.noNulls(curvesMap.keySet(), "curve map key set");
      ArgChecker.noNulls(curvesMap.values(), "curve map entry set");
      _curves.putAll(curvesMap);
    }
    _curveCurrency = new HashMap<>(curveCurrency);
    _fxMatrix = ArgChecker.notNull(fxMatrix, "FX matrix");
  }

  /**
   * Constructor from existing currency map and existing fxMatrix.
   * @param fxMatrix The FXMatrix.
   * @param curveCurrency The map of currency names to currency
   */
  public YieldCurveBundle(final FxMatrix fxMatrix, final Map<String, Currency> curveCurrency) {
    _curves = new LinkedHashMap<>();
    _curveCurrency = new HashMap<>(curveCurrency);
    _fxMatrix = fxMatrix;
  }

  /**
   * Constructor from an existing map. A new map is created.
   * @param curvesMap The map.
   */
  public YieldCurveBundle(final Map<String, ? extends YieldAndDiscountCurve> curvesMap) {
    _curves = new LinkedHashMap<>();
    _curveCurrency = new HashMap<>();
    _fxMatrix = FxMatrix.empty();
    if (curvesMap != null) {
      ArgChecker.noNulls(curvesMap.keySet(), "curves map key set");
      ArgChecker.noNulls(curvesMap.values(), "curves map values");
      _curves.putAll(curvesMap);
    }
  }

  /**
   * Constructor from an array of names and curves. The two arrays should have the same length.
   * The names and curves are linked in the order of the arrays.
   * @param names The names.
   * @param curves The curves.
   */
  public YieldCurveBundle(final String[] names, final YieldAndDiscountCurve[] curves) {
    ArgChecker.notNull(names, "names");
    ArgChecker.notNull(curves, "curves");
    ArgChecker.isTrue(names.length == curves.length, "Different number of names ({}) and curves ({})", names.length, curves.length);
    ArgChecker.noNulls(names, "names");
    ArgChecker.noNulls(curves, "curves");
    _curves = new LinkedHashMap<>();
    _curveCurrency = new HashMap<>();
    _fxMatrix = FxMatrix.empty();
    ;
    final int n = names.length;
    for (int i = 0; i < n; i++) {
      _curves.put(names[i], curves[i]);
    }
  }

  public YieldCurveBundle(final FxMatrix fxMatrix, final String[] names, final YieldAndDiscountCurve[] curves) {
    ArgChecker.notNull(names, "names");
    ArgChecker.notNull(curves, "curves");
    ArgChecker.isTrue(names.length == curves.length, "Different number of names ({}) and curves ({})", names.length, curves.length);
    ArgChecker.noNulls(names, "names");
    ArgChecker.noNulls(curves, "curves");
    _curves = new LinkedHashMap<>();
    _curveCurrency = new HashMap<>();
    final int n = names.length;
    for (int i = 0; i < n; i++) {
      _curves.put(names[i], curves[i]);
    }
    _fxMatrix = fxMatrix;
  }

  /**
   * Constructor from existing curve and currency maps and existing fxMatrix. A new map is created.
   * @param curvesMap The map of curves.
   * @param fxMatrix The FXMatrix.
   * @param curveCurrency The map of currencies.
   */
  public YieldCurveBundle(final Map<String, ? extends YieldAndDiscountCurve> curvesMap, final FxMatrix fxMatrix, final Map<String, Currency> curveCurrency) {
    ArgChecker.notNull(curvesMap, "curves map");
    ArgChecker.notNull(fxMatrix, "FX matrix");
    ArgChecker.notNull(curveCurrency, "curve currency");
    _curves = new LinkedHashMap<>(curvesMap);
    _curveCurrency = new HashMap<>(curveCurrency);
    _fxMatrix = fxMatrix;
  }

  /**
   * Constructor from an array of names and curves. The two arrays should have the same length.
   * The names and curves are linked in the order of the arrays.
   * @param names The names.
   * @param curves The curves.
   * @param currencies The currency associated to each curve.
   */
  public YieldCurveBundle(final String[] names, final YieldAndDiscountCurve[] curves, final Currency[] currencies) {
    ArgChecker.notNull(names, "names");
    ArgChecker.notNull(curves, "curves");
    ArgChecker.isTrue(names.length == curves.length, "Different number of names ({}) and curves ({})", names.length, curves.length);
    ArgChecker.isTrue(names.length == currencies.length, "Different number of names ({}) and currencies ({})", names.length, currencies.length);
    ArgChecker.noNulls(names, "names");
    ArgChecker.noNulls(curves, "curves");
    _curves = new LinkedHashMap<>();
    _curveCurrency = new HashMap<>();
    _fxMatrix = FxMatrix.empty();
    ;
    final int n = names.length;
    for (int i = 0; i < n; i++) {
      _curves.put(names[i], curves[i]);
      _curveCurrency.put(names[i], currencies[i]);
    }
  }

  /**
   * Constructor from a bundle. A new map is created.
   * @param bundle A bundle.
   */
  public YieldCurveBundle(final YieldCurveBundle bundle) {
    ArgChecker.notNull(bundle, "bundle");
    _curves = new LinkedHashMap<>(bundle._curves);
    _curveCurrency = new HashMap<>(bundle._curveCurrency);
    _fxMatrix = bundle._fxMatrix;
  }

  /**
   * Create a new copy of the bundle.
   * @return The bundle.
   */
  public YieldCurveBundle copy() {
    final LinkedHashMap<String, YieldAndDiscountCurve> curves = new LinkedHashMap<>(_curves);
    final Map<String, Currency> curveCurrency = new HashMap<>(_curveCurrency);
    return new YieldCurveBundle(_fxMatrix, curveCurrency, curves);
  }

  /**
   * Add a new curve to the bundle.
   * @param name The curve name.
   * @param curve The curve.
   * @throws IllegalArgumentException if curve name already present
   */
  public void setCurve(final String name, final YieldAndDiscountCurve curve) {
    ArgChecker.notNull(name, "name");
    ArgChecker.notNull(curve, "curve");
    if (_curves.containsKey(name)) {
      throw new IllegalArgumentException("Named yield curve already set: " + name);
    }
    _curves.put(name, curve);
  }

  /**
   * Replace an existing curve with a new one.
   * @param name The curve name.
   * @param curve The curve.
   *  @throws IllegalArgumentException if curve name NOT already present
   */
  public void replaceCurve(final String name, final YieldAndDiscountCurve curve) {
    ArgChecker.notNull(name, "name");
    ArgChecker.notNull(curve, "curve");
    if (!_curves.containsKey(name)) {
      throw new IllegalArgumentException("Named yield curve not in set" + name);
    }
    _curves.put(name, curve);
  }

  public void addAll(final YieldCurveBundle other) {
    ArgChecker.notNull(other, "yield curve bundle");
    _curves.putAll(other._curves);
  }

  public YieldAndDiscountCurve getCurve(final String name) {
    if (_curves.containsKey(name)) {
      return _curves.get(name);
    }
    throw new IllegalArgumentException("Named yield curve not found: " + name);
  }

  /**
   * Returns the map with the curves.
   * @return The map.
   */
  public Map<String, YieldAndDiscountCurve> getCurvesMap() {
    return _curves;
  }

  /**
   * Returns the number of curves in the bundle.
   * @return The number of curves.
   */
  public int size() {
    return _curves.size();
  }

  public Set<String> getAllNames() {
    return _curves.keySet();
  }

  public Boolean containsName(final String curveName) {
    return _curves.containsKey(curveName);
  }

  /**
   * Gets map linking each curve in the bundle to its currency.
   * @return The map.
   */
  public Map<String, Currency> getCurrencyMap() {
    return _curveCurrency;
  }

  /**
   * Return the currency associated to a given curve.
   * @param curveName The curve name.
   * @return The currency.
   */
  public Currency getCurveCurrency(final String curveName) {
    final Currency ccy = _curveCurrency.get(curveName);
    if (ccy == null) {
      throw new IllegalArgumentException("Named yield curve not in map: " + curveName);
    }
    return ccy;
  }

  /**
   * Gets the underlying FXMatrix containing the exchange rates.
   * @return The matrix.
   */
  public FxMatrix getFxRates() {
    return _fxMatrix;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _curveCurrency.hashCode();
    result = prime * result + _curves.hashCode();
    result = prime * result + _fxMatrix.hashCode();
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
    final YieldCurveBundle other = (YieldCurveBundle) obj;
    if (!Objects.equals(_curveCurrency, other._curveCurrency)) {
      return false;
    }
    if (!Objects.equals(_curves, other._curves)) {
      return false;
    }
    if (!Objects.equals(_fxMatrix, other._fxMatrix)) {
      return false;
    }
    return true;
  }


}

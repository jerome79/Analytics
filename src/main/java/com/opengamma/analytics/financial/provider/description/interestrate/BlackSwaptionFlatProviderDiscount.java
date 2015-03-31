/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.option.parameters.BlackFlatSwaptionParameters;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Class describing a "market" with discounting, forward, price index and credit curves.
 * The forward rate are computed as the ratio of discount factors stored in YieldAndDiscountCurve.
 */
public class BlackSwaptionFlatProviderDiscount extends BlackSwaptionFlatProvider {

  /**
   * Constructor from exiting multicurveProvider and Black swaption parameters. The given provider and parameters are used for the new provider (the same maps are used, not copied).
   * @param multicurves The multi-curves provider.
   * @param parameters The Black swaption parameters.
   */
  public BlackSwaptionFlatProviderDiscount(final MulticurveProviderDiscount multicurves, final BlackFlatSwaptionParameters parameters) {
    super(multicurves, parameters);
  }

  /**
   * Create a new copy of the provider.
   * @return The bundle.
   */
  @Override
  public BlackSwaptionFlatProviderDiscount copy() {
    final MulticurveProviderDiscount multicurveProvider = getMulticurveProvider().copy();
    return new BlackSwaptionFlatProviderDiscount(multicurveProvider, getBlackParameters());
  }

  /**
   * Returns the MulticurveProvider from which the BlackSwaptionProviderDiscount is composed.
   * @return The multi-curves provider.
   */
  @Override
  public MulticurveProviderDiscount getMulticurveProvider() {
    return (MulticurveProviderDiscount) super.getMulticurveProvider();
  }

  /**
   * Gets the discounting curve associated in a given currency in the market.
   * @param ccy The currency.
   * @return The curve.
   */
  public YieldAndDiscountCurve getCurve(final Currency ccy) {
    return getMulticurveProvider().getCurve(ccy);
  }

  /**
   * Gets the forward curve associated to a given Ibor index in the market.
   * @param index The Ibor index.
   * @return The curve.
   */
  public YieldAndDiscountCurve getCurve(final IborIndex index) {
    return getMulticurveProvider().getCurve(index);
  }

  /**
   * Gets the forward curve associated to a given ON index in the market.
   * @param index The ON index.
   * @return The curve.
   */
  public YieldAndDiscountCurve getCurve(final IndexON index) {
    return getMulticurveProvider().getCurve(index);
  }

  /**
   * Sets the discounting curve for a given currency.
   * @param ccy The currency.
   * @param curve The yield curve used for discounting.
   */
  public void setCurve(final Currency ccy, final YieldAndDiscountCurve curve) {
    getMulticurveProvider().setCurve(ccy, curve);
  }

  /**
   * Sets the curve associated to an Ibor index.
   * @param index The index.
   * @param curve The curve.
   */
  public void setCurve(final IborIndex index, final YieldAndDiscountCurve curve) {
    getMulticurveProvider().setCurve(index, curve);
  }

  /**
   * Sets the curve associated to an ON index.
   * @param index The index.
   * @param curve The curve.
   */
  public void setCurve(final IndexON index, final YieldAndDiscountCurve curve) {
    getMulticurveProvider().setCurve(index, curve);
  }

  /**
   * Set all the curves contains in another provider. If a currency or index is already present in the map, the associated curve is changed.
   * @param other The other provider.
   */
  public void setAll(final BlackSwaptionFlatProviderDiscount other) {
    ArgChecker.notNull(other, "other");
    getMulticurveProvider().setAll(other.getMulticurveProvider());
  }

  /**
   * Replaces the discounting curve for a given currency.
   * @param ccy The currency.
   * @param curve The yield curve used for discounting.
   * @throws IllegalArgumentException if curve name NOT already present
   */
  public void replaceCurve(final Currency ccy, final YieldAndDiscountCurve curve) {
    getMulticurveProvider().replaceCurve(ccy, curve);
  }

  /**
   * Replaces the forward curve for a given index.
   * @param index The index.
   * @param curve The yield curve used for forward.
   * @throws IllegalArgumentException if curve name NOT already present
   */
  public void replaceCurve(final IborIndex index, final YieldAndDiscountCurve curve) {
    getMulticurveProvider().replaceCurve(index, curve);
  }

  /**
   * Replaces a discounting curve for a currency.
   * @param ccy The currency
   * @param replacement The replacement curve
   * @return A new provider with the supplied discounting curve
   */
  public BlackSwaptionFlatProviderDiscount withDiscountFactor(final Currency ccy, final YieldAndDiscountCurve replacement) {
    final MulticurveProviderDiscount decoratedMulticurve = getMulticurveProvider().withDiscountFactor(ccy, replacement);
    return new BlackSwaptionFlatProviderDiscount(decoratedMulticurve, getBlackParameters());
  }

  /**
   * Replaces an ibor curve for an index.
   * @param index The index
   * @param replacement The replacement curve
   * @return A new provider with the supplied ibor curve
   */
  public BlackSwaptionFlatProviderDiscount withForward(final IborIndex index, final YieldAndDiscountCurve replacement) {
    final MulticurveProviderDiscount decoratedMulticurve = getMulticurveProvider().withForward(index, replacement);
    return new BlackSwaptionFlatProviderDiscount(decoratedMulticurve, getBlackParameters());
  }

  /**
   * Replaces an overnight curve for an index.
   * @param index The index
   * @param replacement The replacement curve
   * @return A new provider with the supplied overnight curve
   */
  public BlackSwaptionFlatProviderDiscount withForward(final IndexON index, final YieldAndDiscountCurve replacement) {
    final MulticurveProviderDiscount decoratedMulticurve = getMulticurveProvider().withForward(index, replacement);
    return new BlackSwaptionFlatProviderDiscount(decoratedMulticurve, getBlackParameters());
  }

}

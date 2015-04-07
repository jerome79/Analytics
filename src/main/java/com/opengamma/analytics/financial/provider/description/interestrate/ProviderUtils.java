/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import java.util.Collection;
import java.util.Map;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.currency.FxMatrix;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Utility class for providers. 
 * This is a temporary class and will be removed when the providers have been refactored.
 */
public class ProviderUtils {

  /**
   * Merges discounting curve providers. 
   * If a currency or index appears twice in the providers, an error is returned.
   * The FXMatrix are also merged.
   * @param providers The providers to merge, not null or empty
   * @return The merged providers
   */
  public static MulticurveProviderDiscount mergeDiscountingProviders(final Collection<MulticurveProviderDiscount> providers) {
    ArgChecker.notNull(providers, "providers");
    ArgChecker.notEmpty(providers, "providers");
    final MulticurveProviderDiscount result = new MulticurveProviderDiscount();
    FxMatrix matrix = FxMatrix.empty();
    int loop = 0;
    for (final MulticurveProviderDiscount provider : providers) {
      for (final Map.Entry<Currency, YieldAndDiscountCurve> entry : provider.getDiscountingCurves().entrySet()) {
        result.setCurve(entry.getKey(), entry.getValue());
      }
      for (final Map.Entry<IborIndex, YieldAndDiscountCurve> entry : provider.getForwardIborCurves().entrySet()) {
        result.setCurve(entry.getKey(), entry.getValue());
      }
      for (final Map.Entry<IndexON, YieldAndDiscountCurve> entry : provider.getForwardONCurves().entrySet()) {
        result.setCurve(entry.getKey(), entry.getValue());
      }
      if (loop == 0) {
        matrix = provider.getFxRates();
        loop++;
      } else {
        matrix = matrix.merge(provider.getFxRates());
      }
    }
    result.setForexMatrix(matrix);
    return result;
  }

  /**
   * Merges discounting curve providers. 
   * If a currency or index appears twice in the providers, the curve in the last instance is used.
   * The FXMatrix are also merged.
   * @param providers The providers to merge, not null or empty
   * @return The merged providers
   */
  public static MulticurveProviderDiscount mergeWithDuplicateDiscountingProviders(final Collection<MulticurveProviderDiscount> providers) {
    ArgChecker.notNull(providers, "providers");
    ArgChecker.notEmpty(providers, "providers");
    final MulticurveProviderDiscount result = new MulticurveProviderDiscount();
    FxMatrix matrix = FxMatrix.empty();
    int loop = 0;
    for (final MulticurveProviderDiscount provider : providers) {
      for (final Map.Entry<Currency, YieldAndDiscountCurve> entry : provider.getDiscountingCurves().entrySet()) {
        result.setOrReplaceCurve(entry.getKey(), entry.getValue());
      }
      for (final Map.Entry<IborIndex, YieldAndDiscountCurve> entry : provider.getForwardIborCurves().entrySet()) {
        result.setOrReplaceCurve(entry.getKey(), entry.getValue());
      }
      for (final Map.Entry<IndexON, YieldAndDiscountCurve> entry : provider.getForwardONCurves().entrySet()) {
        result.setOrReplaceCurve(entry.getKey(), entry.getValue());
      }
      if (loop == 0) {
        matrix = provider.getFxRates();
        loop++;
      } else {
        matrix = matrix.merge(provider.getFxRates());
      }
    }
    result.setForexMatrix(matrix);
    return result;
  }

  /**
   * Merges a discounting curve provider and an FX matrix.
   * @param provider The provider, not null
   * @param matrix The FX matrix, not null
   * @return The merged provider
   */
  public static MulticurveProviderDiscount mergeDiscountingProviders(final MulticurveProviderDiscount provider, final FxMatrix matrix) {
    ArgChecker.notNull(provider, "provider");
    ArgChecker.notNull(matrix, "matrix");
    final MulticurveProviderDiscount result = provider.copy();
    final FxMatrix fxMatrix = provider.getFxRates().merge(matrix);
    result.setForexMatrix(fxMatrix);
    return result;
  }

  /**
   * Merges a Hull-White curve provider and an FX matrix.
   * @param provider The provider, not null
   * @param matrix The FX matrix, not null
   * @return The merged provider
   */
  public static HullWhiteOneFactorProviderDiscount mergeHullWhiteProviders(final HullWhiteOneFactorProviderDiscount provider, final FxMatrix matrix) {
    ArgChecker.notNull(provider, "provider");
    ArgChecker.notNull(matrix, "matrix");
    final HullWhiteOneFactorProviderDiscount result = provider.copy();
    final FxMatrix fxMatrix = provider.getMulticurveProvider().getFxRates().merge(matrix);
    result.getMulticurveProvider().setForexMatrix(fxMatrix);
    return result;
  }

}

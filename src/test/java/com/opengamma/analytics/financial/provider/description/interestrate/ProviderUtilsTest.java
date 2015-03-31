/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import static com.opengamma.strata.basics.currency.Currency.EUR;
import static com.opengamma.strata.basics.currency.Currency.USD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;
import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.forex.datasets.StandardDataSetsEURUSDForex;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.datasets.StandardDataSetsMulticurveUSD;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.currency.FxMatrix;
import com.opengamma.strata.collect.tuple.Pair;

/**
 * Tests the merge tools between different providers.
 */
public class ProviderUtilsTest {

  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_PAIR_USD136 = StandardDataSetsMulticurveUSD.getCurvesUSDOisL1L3L6();
  private static final MulticurveProviderDiscount MULTICURVE_USD136 = MULTICURVE_PAIR_USD136.getFirst();
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_PAIR_USDEUR_FX = StandardDataSetsEURUSDForex.getCurvesUSDOisEURFx();
  private static final MulticurveProviderDiscount MULTICURVE_USDEUR_FX = MULTICURVE_PAIR_USDEUR_FX.getFirst();
  private static final IborIndex[] INDEX_USD136 = StandardDataSetsMulticurveUSD.indexIborArrayUSDOisL3();

  private static final double TOLERANCE_FX_RATE = 1.0E-10;

  @Test
  /**
   * Test merge of a single provider.
   */
  public void mergeUSD136() {
    final ArrayList<MulticurveProviderDiscount> multicurveList = new ArrayList<>();
    multicurveList.add(MULTICURVE_USD136);
    final MulticurveProviderDiscount merged1 = ProviderUtils.mergeDiscountingProviders(multicurveList);
    assertEquals("ProviderUtils - Merge multi-curve provider", MULTICURVE_USD136.getCurve(USD), merged1.getCurve(USD));
    for (IborIndex index : INDEX_USD136) {
      assertEquals("ProviderUtils - Merge multi-curve provider", MULTICURVE_USD136.getCurve(index), merged1.getCurve(index));
    }
    compareFxMatrices(MULTICURVE_USD136.getFxRates(), merged1.getFxRates(), TOLERANCE_FX_RATE);
  }

  @Test
  /**
   * Test merge of a single provider.
   */
  public void mergeUSDEURForex() {
    final ArrayList<MulticurveProviderDiscount> multicurveList = new ArrayList<>();
    multicurveList.add(MULTICURVE_USDEUR_FX);
    final MulticurveProviderDiscount merged1 = ProviderUtils.mergeDiscountingProviders(multicurveList);
    assertEquals("ProviderUtils - Merge multi-curve provider", MULTICURVE_USDEUR_FX.getCurve(USD), merged1.getCurve(USD));
    assertEquals("ProviderUtils - Merge multi-curve provider", MULTICURVE_USDEUR_FX.getCurve(EUR), merged1.getCurve(EUR));
    compareFxMatrices(MULTICURVE_USDEUR_FX.getFxRates(), merged1.getFxRates(), TOLERANCE_FX_RATE);
  }

  private void compareFxMatrices(FxMatrix matrix1, FxMatrix matrix2, double tolerance) {

    final Set<Currency> set1 = matrix1.getCurrencies();
    final Set<Currency> set2 = matrix2.getCurrencies();
    assertThat(set1).isEqualTo(set2);

    // Compare one diagonal (other will be correct also if matrices are coherent).
    if (set1.size() > 0) {
      final Iterator<Currency> iterator = set1.iterator();
      final Currency initialCurrency = iterator.next();
      while (iterator.hasNext()) {
        final Currency otherCurrency = iterator.next();

        assertThat(matrix1.getRate(initialCurrency, otherCurrency))
            .isEqualTo(matrix2.getRate(initialCurrency, otherCurrency), offset(tolerance));
      }
    }
  }

}

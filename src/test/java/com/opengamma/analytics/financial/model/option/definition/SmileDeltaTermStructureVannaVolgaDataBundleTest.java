/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import java.time.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.forex.method.TestsDataSetsForex;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParametersStrikeInterpolation;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.util.time.DateUtils;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.collect.tuple.Pair;


/**
 * @deprecated This class tests deprecated functionality
 */
@Deprecated
@Test
public class SmileDeltaTermStructureVannaVolgaDataBundleTest {

  private static final YieldCurveBundle CURVES = TestsDataSetsForex.createCurvesForex();
  private static final Pair<Currency, Currency> CCYS = Pair.of(Currency.EUR, Currency.EUR);
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 6, 13);
  private static final SmileDeltaTermStructureParametersStrikeInterpolation SMILES = TestsDataSetsForex.smile3points(REFERENCE_DATE, Interpolator1DFactory.LINEAR_INSTANCE);
  private static final SmileDeltaTermStructureVannaVolgaDataBundle FX_DATA = new SmileDeltaTermStructureVannaVolgaDataBundle(CURVES, SMILES, CCYS);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurves() {
    new SmileDeltaTermStructureVannaVolgaDataBundle(null, SMILES, CCYS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSmiles() {
    new SmileDeltaTermStructureVannaVolgaDataBundle(CURVES, null, CCYS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCcys() {
    new SmileDeltaTermStructureVannaVolgaDataBundle(CURVES, SMILES, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadCurrencyPair() {
    new SmileDeltaTermStructureVannaVolgaDataBundle(CURVES, SMILES, Pair.of(Currency.AUD, Currency.SEK));
  }

  @Test
  public void testObject() {
    assertEquals(FX_DATA.getVolatilityModel(), SMILES);
    SmileDeltaTermStructureVannaVolgaDataBundle other = new SmileDeltaTermStructureVannaVolgaDataBundle(CURVES, SMILES, CCYS);
    assertEquals(FX_DATA, other);
    assertEquals(FX_DATA.hashCode(), other.hashCode());
    other = SmileDeltaTermStructureVannaVolgaDataBundle.from(CURVES, SMILES, CCYS);
    assertEquals(FX_DATA, other);
    assertEquals(FX_DATA.hashCode(), other.hashCode());
    other = new SmileDeltaTermStructureVannaVolgaDataBundle(TestsDataSetsForex.createCurvesForex2(), SMILES, CCYS);
    assertFalse(FX_DATA.equals(other));
    other = new SmileDeltaTermStructureVannaVolgaDataBundle(CURVES, TestsDataSetsForex.smile3points(REFERENCE_DATE, Interpolator1DFactory.LOG_LINEAR_INSTANCE), CCYS);
    assertFalse(FX_DATA.equals(other));
    other = new SmileDeltaTermStructureVannaVolgaDataBundle(CURVES, SMILES, Pair.of(Currency.EUR, Currency.GBP));
    assertFalse(FX_DATA.equals(other));
  }

  @Test
  public void testCopy() {
    assertFalse(FX_DATA == FX_DATA.copy());
    assertEquals(FX_DATA, FX_DATA.copy());
  }

  @Test
  public void testBuilders() {
    final SmileDeltaTermStructureVannaVolgaDataBundle fxData = new SmileDeltaTermStructureVannaVolgaDataBundle(CURVES, SMILES, CCYS);
    assertEquals(FX_DATA, fxData);
    SmileDeltaTermStructureVannaVolgaDataBundle other = fxData.with(TestsDataSetsForex.createCurvesForex2());
    assertEquals(FX_DATA, fxData);
    assertFalse(other.equals(fxData));
    other = FX_DATA.with(TestsDataSetsForex.smile3points(REFERENCE_DATE, Interpolator1DFactory.LOG_LINEAR_INSTANCE));
    assertEquals(FX_DATA, fxData);
    assertFalse(other.equals(fxData));
  }
}

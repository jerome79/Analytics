/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.legalentity;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import java.util.Collections;
import java.util.Set;

import org.testng.annotations.Test;

import com.google.common.collect.Sets;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.location.Country;

/**
 * Tests the region object.
 */
@Test
public class RegionTest {
  /** The name */
  private static final String NAME = "NORTH_AMERICA";
  /** The countries */
  private static final Set<Country> COUNTRIES = Sets.newHashSet(Country.CA, Country.MX, Country.US);
  /** The currencies */
  private static final Set<Currency> CURRENCIES = Sets.newHashSet(Currency.CAD, Currency.of("MXN"), Currency.USD);

  /**
   * Tests failure on null name
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName1() {
    Region.of(null, COUNTRIES, CURRENCIES);
  }

  /**
   * Test failure on null name
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName2() {
    Region.of(null, Country.CA, Currency.CAD);
  }

  /**
   * Tests failure on null currencies
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrencies1() {
    Region.of(NAME, COUNTRIES, null);
  }

  /**
   * Tests null currency gives an empty set.
   */
  @Test
  public void testNullCurrency1() {
    final Region region = Region.of(NAME, Country.CA, null);
    assertEquals(Collections.emptySet(), region.getCurrencies());
  }

  /**
   * Tests null country gives an empty set.
   */
  @Test
  public void testNullCountry1() {
    final Region region = Region.of(NAME, null, Currency.CAD);
    assertEquals(Collections.emptySet(), region.getCountries());
  }

  /**
   * Tests the object
   */
  @Test
  public void testObject() {
    final Region region = Region.of(NAME, COUNTRIES, CURRENCIES);
    assertEquals(NAME, region.getName());
    assertEquals(COUNTRIES, region.getCountries());
    assertEquals(CURRENCIES, region.getCurrencies());
    Region other = Region.of(NAME, COUNTRIES, CURRENCIES);
    assertEquals(region, other);
    assertEquals(region.hashCode(), other.hashCode());
    other = Region.of("OTHER", COUNTRIES, CURRENCIES);
    assertFalse(region.equals(other));
    other = Region.of(NAME, Collections.singleton(Country.CA), CURRENCIES);
    assertFalse(region.equals(other));
    other = Region.of(NAME, COUNTRIES, Collections.singleton(Currency.CAD));
    assertFalse(region.equals(other));
  }
}

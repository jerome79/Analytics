/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.legalentity;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.joda.beans.impl.flexi.FlexiBean;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.opengamma.analytics.financial.util.types.ParameterizedTypeImpl;
import com.opengamma.analytics.financial.util.types.VariantType;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.location.Country;
import com.opengamma.strata.collect.tuple.Pair;

/**
 * Tests for the classes that extract data from an {@link LegalEntity}.
 */
@Test
public class LegalEntityFilterTest {

  /**
   * Tests failure for a credit rating-specific request when the credit rating is null
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testNullCreditRatings() {
    final LegalEntityCreditRatings filter = new LegalEntityCreditRatings();
    filter.getFilteredData(new LegalEntity(null, LegalEntityTest.SHORT_NAME, null, null, null));
  }

  /**
   * Tests failure for a request based on the ratings description where the description is null.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testRatingDescription() {
    final Set<CreditRating> creditRatings = new HashSet<>();
    creditRatings.add(CreditRating.of("A", "S&P", true));
    final LegalEntityCreditRatings filter = new LegalEntityCreditRatings();
    filter.setPerAgencyRatingDescriptions(Collections.singleton("S&P"));
    filter.getFilteredData(new LegalEntity(null, LegalEntityTest.SHORT_NAME, creditRatings, null, null));
  }

  /**
   * Tests failure for an agency rating that is not present in the legal entity.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testUseUnavailableRating() {
    final Set<CreditRating> creditRatings = new HashSet<>();
    creditRatings.add(CreditRating.of("A", "S&P", true));
    creditRatings.add(CreditRating.of("A", "Moody's", true));
    final LegalEntityCreditRatings filter = new LegalEntityCreditRatings();
    filter.setPerAgencyRatings(Collections.singleton("Fitch"));
    filter.getFilteredData(new LegalEntity(null, LegalEntityTest.SHORT_NAME, creditRatings, null, null));
  }

  /**
   * Tests failure for an agency rating description that is not present in the legal entity.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testUseUnavailableRatingDescription() {
    final Set<CreditRating> creditRatings = new HashSet<>();
    creditRatings.add(CreditRating.of("A", "S&P", "Prime", true));
    creditRatings.add(CreditRating.of("A", "Moody's", "Prime", true));
    final LegalEntityCreditRatings filter = new LegalEntityCreditRatings();
    filter.setPerAgencyRatingDescriptions(Collections.singleton("Fitch"));
    filter.getFilteredData(new LegalEntity(null, LegalEntityTest.SHORT_NAME, creditRatings, null, null));
  }

  /**
   * Tests ratings requests.
   */
  @Test
  public void testCreditRatings() {
    LegalEntityCreditRatings filter = new LegalEntityCreditRatings();
    assertEquals(LegalEntityTest.CREDIT_RATINGS, filter.getFilteredData(LegalEntityTest.LEGAL_ENTITY));
    assertEquals(LegalEntityTest.CREDIT_RATINGS, filter.getFilteredData(LegalEntityTest.LEGAL_ENTITY_RED_CODE));
    final Set<CreditRating> creditRatings = new HashSet<>(LegalEntityTest.CREDIT_RATINGS);
    creditRatings.add(CreditRating.of("C", "Poor", "Test", false));
    assertEquals(ParameterizedTypeImpl.of(Set.class, CreditRating.class), filter.getFilteredDataType());
    filter = new LegalEntityCreditRatings();
    filter.setUseRating(true);
    Set<Pair<String, String>> expected = new HashSet<>();
    expected.add(Pair.of("Moody's", "B"));
    expected.add(Pair.of("S&P", "A"));
    expected.add(Pair.of("Test", "C"));
    assertEquals(expected, filter.getFilteredData(new LegalEntity(null, LegalEntityTest.SHORT_NAME, creditRatings, null, null)));
    assertEquals(expected, filter.getFilteredData(new LegalEntityWithREDCode(null, LegalEntityTest.SHORT_NAME, creditRatings, null, null, "")));
    expected = new HashSet<>();
    expected.add(Pair.of("Moody's", "B"));
    expected.add(Pair.of("S&P", "A"));
    assertEquals(expected, filter.getFilteredData(LegalEntityTest.LEGAL_ENTITY));
    assertEquals(expected, filter.getFilteredData(LegalEntityTest.LEGAL_ENTITY_RED_CODE));
    assertEquals(ParameterizedTypeImpl.of(Set.class, ParameterizedTypeImpl.of(Pair.class, String.class, String.class)), filter.getFilteredDataType());
    filter = new LegalEntityCreditRatings();
    filter.setPerAgencyRatings(Collections.singleton("Moody's"));
    expected = new HashSet<>();
    expected.add(Pair.of("Moody's", "B"));
    assertEquals(expected, filter.getFilteredData(LegalEntityTest.LEGAL_ENTITY));
    assertEquals(expected, filter.getFilteredData(LegalEntityTest.LEGAL_ENTITY_RED_CODE));
    assertEquals(ParameterizedTypeImpl.of(Set.class, ParameterizedTypeImpl.of(Pair.class, String.class, String.class)), filter.getFilteredDataType());
    filter = new LegalEntityCreditRatings();
    filter.setPerAgencyRatings(Collections.singleton("S&P"));
    expected = new HashSet<>();
    expected.add(Pair.of("S&P", "A"));
    assertEquals(expected, filter.getFilteredData(LegalEntityTest.LEGAL_ENTITY));
    assertEquals(expected, filter.getFilteredData(LegalEntityTest.LEGAL_ENTITY_RED_CODE));
    assertEquals(ParameterizedTypeImpl.of(Set.class, ParameterizedTypeImpl.of(Pair.class, String.class, String.class)), filter.getFilteredDataType());
    filter = new LegalEntityCreditRatings();
    filter.setUseRatingDescription(true);
    expected = new HashSet<>();
    expected.add(Pair.of("Moody's", "Investment Grade"));
    expected.add(Pair.of("S&P", "Prime"));
    expected.add(Pair.of("Test", "Poor"));
    assertEquals(expected, filter.getFilteredData(new LegalEntity(null, LegalEntityTest.SHORT_NAME, creditRatings, null, null)));
    assertEquals(expected, filter.getFilteredData(new LegalEntityWithREDCode(null, LegalEntityTest.SHORT_NAME, creditRatings, null, null, "")));
    expected = new HashSet<>();
    expected.add(Pair.of("Moody's", "Investment Grade"));
    expected.add(Pair.of("S&P", "Prime"));
    assertEquals(expected, filter.getFilteredData(LegalEntityTest.LEGAL_ENTITY));
    assertEquals(expected, filter.getFilteredData(LegalEntityTest.LEGAL_ENTITY_RED_CODE));
    assertEquals(ParameterizedTypeImpl.of(Set.class, ParameterizedTypeImpl.of(Pair.class, String.class, String.class)), filter.getFilteredDataType());
    filter = new LegalEntityCreditRatings();
    filter.setPerAgencyRatingDescriptions(Collections.singleton("Moody's"));
    expected = new HashSet<>();
    expected.add(Pair.of("Moody's", "Investment Grade"));
    assertEquals(expected, filter.getFilteredData(LegalEntityTest.LEGAL_ENTITY));
    assertEquals(expected, filter.getFilteredData(LegalEntityTest.LEGAL_ENTITY_RED_CODE));
    filter = new LegalEntityCreditRatings();
    filter.setPerAgencyRatingDescriptions(Collections.singleton("S&P"));
    expected = new HashSet<>();
    expected.add(Pair.of("S&P", "Prime"));
    assertEquals(expected, filter.getFilteredData(LegalEntityTest.LEGAL_ENTITY));
    assertEquals(expected, filter.getFilteredData(LegalEntityTest.LEGAL_ENTITY_RED_CODE));
    assertEquals(ParameterizedTypeImpl.of(Set.class, ParameterizedTypeImpl.of(Pair.class, String.class, String.class)), filter.getFilteredDataType());
  }

  /**
   * Tests requests for RED codes.
   */
  @Test
  public void testREDCode() {
    LegalEntityREDCode filter = new LegalEntityREDCode();
    assertEquals(LegalEntityTest.RED_CODE, filter.getFilteredData(LegalEntityTest.LEGAL_ENTITY_RED_CODE));
    assertEquals(String.class, filter.getFilteredDataType());
  }

  /**
   * Tests failure for a region-specific request when the region is null in the legal entity.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testNullRegionInEntity() {
    final LegalEntityRegion filter = new LegalEntityRegion();
    filter.getFilteredData(new LegalEntity(null, LegalEntityTest.SHORT_NAME, null, null, null));
  }

  /**
   * Tests region requests.
   */
  @Test
  public void testRegion() {
    LegalEntityRegion filter = new LegalEntityRegion();
    assertEquals(LegalEntityTest.REGION, filter.getFilteredData(LegalEntityTest.LEGAL_ENTITY));
    assertEquals(LegalEntityTest.REGION, filter.getFilteredData(LegalEntityTest.LEGAL_ENTITY_RED_CODE));
    assertEquals(Region.class, filter.getFilteredDataType());
    filter = new LegalEntityRegion();
    filter.setUseName(true);
    assertEquals(Collections.singleton(LegalEntityTest.REGION.getName()), filter.getFilteredData(LegalEntityTest.LEGAL_ENTITY));
    assertEquals(Collections.singleton(LegalEntityTest.REGION.getName()), filter.getFilteredData(LegalEntityTest.LEGAL_ENTITY_RED_CODE));
    assertEquals(ParameterizedTypeImpl.of(Set.class, String.class), filter.getFilteredDataType());
    filter = new LegalEntityRegion();
    filter.setUseCountry(true);
    assertEquals(Sets.newHashSet(Country.US, Country.CA), filter.getFilteredData(LegalEntityTest.LEGAL_ENTITY));
    assertEquals(Sets.newHashSet(Country.US, Country.CA), filter.getFilteredData(LegalEntityTest.LEGAL_ENTITY_RED_CODE));
    assertEquals(ParameterizedTypeImpl.of(Set.class, Country.class), filter.getFilteredDataType());
    filter = new LegalEntityRegion();
    filter.setCountries(Collections.singleton(Country.US));
    assertEquals(Sets.newHashSet(Country.US), filter.getFilteredData(LegalEntityTest.LEGAL_ENTITY));
    assertEquals(Sets.newHashSet(Country.US), filter.getFilteredData(LegalEntityTest.LEGAL_ENTITY_RED_CODE));
    assertEquals(ParameterizedTypeImpl.of(Set.class, Country.class), filter.getFilteredDataType());
    filter = new LegalEntityRegion();
    filter.setUseCurrency(true);
    assertEquals(Sets.newHashSet(Currency.CAD, Currency.USD), filter.getFilteredData(LegalEntityTest.LEGAL_ENTITY));
    assertEquals(Sets.newHashSet(Currency.CAD, Currency.USD), filter.getFilteredData(LegalEntityTest.LEGAL_ENTITY_RED_CODE));
    assertEquals(ParameterizedTypeImpl.of(Set.class, Currency.class), filter.getFilteredDataType());
    filter = new LegalEntityRegion();
    filter.setCurrencies(Collections.singleton(Currency.USD));
    assertEquals(Sets.newHashSet(Currency.USD), filter.getFilteredData(LegalEntityTest.LEGAL_ENTITY));
    assertEquals(Sets.newHashSet(Currency.USD), filter.getFilteredData(LegalEntityTest.LEGAL_ENTITY_RED_CODE));
    assertEquals(ParameterizedTypeImpl.of(Set.class, Currency.class), filter.getFilteredDataType());
    //TODO test builder chaining and currency / country pairs
  }

  /**
   * Tests failure for empty sector classifications.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testEmptySectorClassifications() {
    final FlexiBean classifications = new FlexiBean();
    classifications.put(GICSCode.NAME, GICSCode.of("1020"));
    final Sector sector = Sector.of("INDUSTRIALS", classifications);
    final LegalEntitySector filter = new LegalEntitySector();
    filter.setClassifications(Collections.singleton(ICBCode.NAME));
    filter.getFilteredData(new LegalEntity(LegalEntityTest.TICKER, LegalEntityTest.SHORT_NAME, LegalEntityTest.CREDIT_RATINGS, sector, LegalEntityTest.REGION));
  }

  /**
   * Tests failure for a classification type that does not exist in legal entity.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testSectorNoMatchingClassification() {
    final Sector sector = Sector.of("INDUSTRIALS");
    final LegalEntitySector filter = new LegalEntitySector();
    filter.setClassifications(Collections.singleton(GICSCode.NAME));
    filter.getFilteredData(new LegalEntity(LegalEntityTest.TICKER, LegalEntityTest.SHORT_NAME, LegalEntityTest.CREDIT_RATINGS, sector, LegalEntityTest.REGION));
  }

  /**
   * Tests sector requests.
   */
  @Test
  public void testSector() {
    LegalEntitySector filter = new LegalEntitySector();
    assertEquals(LegalEntityTest.SECTOR, filter.getFilteredData(LegalEntityTest.LEGAL_ENTITY));
    assertEquals(LegalEntityTest.SECTOR, filter.getFilteredData(LegalEntityTest.LEGAL_ENTITY_RED_CODE));
    assertEquals(Sector.class, filter.getFilteredDataType());
    filter = new LegalEntitySector();
    filter.setUseSectorName(true);
    assertEquals(Collections.singleton(LegalEntityTest.SECTOR.getName()), filter.getFilteredData(LegalEntityTest.LEGAL_ENTITY));
    assertEquals(ParameterizedTypeImpl.of(Set.class, String.class), filter.getFilteredDataType());
    filter = new LegalEntitySector();
    filter.setClassifications(Collections.singleton(GICSCode.NAME));
    filter.setClassificationValueTypes(Collections.singleton(GICSCode.class));
    assertEquals(Collections.singleton(GICSCode.of(10203040)), filter.getFilteredData(LegalEntityTest.LEGAL_ENTITY));
    assertEquals(ParameterizedTypeImpl.of(Set.class, GICSCode.class), filter.getFilteredDataType());
    filter = new LegalEntitySector();
    filter.setClassifications(Collections.singleton(ICBCode.NAME));
    filter.setClassificationValueTypes(Collections.singleton(ICBCode.class));
    assertEquals(Collections.singleton(ICBCode.of("1020")), filter.getFilteredData(LegalEntityTest.LEGAL_ENTITY));
    assertEquals(ParameterizedTypeImpl.of(Set.class, ICBCode.class), filter.getFilteredDataType());
    filter = new LegalEntitySector();
    filter.setClassifications(ImmutableSet.of(GICSCode.NAME, ICBCode.NAME));
    filter.setClassificationValueTypes(ImmutableSet.of(GICSCode.class, ICBCode.class));
    assertEquals(ImmutableSet.of(GICSCode.of(10203040), ICBCode.of("1020")), filter.getFilteredData(LegalEntityTest.LEGAL_ENTITY));
    assertEquals(ParameterizedTypeImpl.of(Set.class, VariantType.either(GICSCode.class, ICBCode.class)), filter.getFilteredDataType());
    filter = new LegalEntitySector();
    filter.setUseClassificationName(true);
    assertTrue(filter.getFilteredData(LegalEntityTest.LEGAL_ENTITY) instanceof Set);
    assertTrue(((Set<?>) filter.getFilteredData(LegalEntityTest.LEGAL_ENTITY)).isEmpty());
    assertTrue(filter.getFilteredData(LegalEntityTest.LEGAL_ENTITY_RED_CODE) instanceof Set);
    assertTrue(((Set<?>) filter.getFilteredData(LegalEntityTest.LEGAL_ENTITY_RED_CODE)).isEmpty());
    assertEquals(ParameterizedTypeImpl.of(Set.class, Object.class), filter.getFilteredDataType());
  }

  /**
   * Tests short name requests.
   */
  @Test
  public void testShortName() {
    assertEquals(LegalEntityTest.SHORT_NAME, new LegalEntityShortName().getFilteredData(LegalEntityTest.LEGAL_ENTITY));
    assertEquals(LegalEntityTest.SHORT_NAME, new LegalEntityShortName().getFilteredData(LegalEntityTest.LEGAL_ENTITY_RED_CODE));
  }

  /**
   * Tests ticker requests.
   */
  @Test
  public void testTicker() {
    assertEquals(LegalEntityTest.TICKER, new LegalEntityTicker().getFilteredData(LegalEntityTest.LEGAL_ENTITY));
    assertEquals(LegalEntityTest.TICKER, new LegalEntityTicker().getFilteredData(LegalEntityTest.LEGAL_ENTITY_RED_CODE));
  }

  /**
   * Tests combining rating and sector.
   */
  @Test
  public void testRatingAndSector() {
    final LegalEntityCombiningFilter filter = new LegalEntityCombiningFilter();
    final Set<LegalEntityFilter<LegalEntity>> underlyingFilters = new HashSet<>();
    final LegalEntityCreditRatings ratingsFilter = new LegalEntityCreditRatings();
    ratingsFilter.setPerAgencyRatings(Collections.singleton("S&P"));
    underlyingFilters.add(ratingsFilter);
    final LegalEntitySector sectorFilter = new LegalEntitySector();
    sectorFilter.setUseSectorName(true);
    underlyingFilters.add(sectorFilter);
    underlyingFilters.add(new LegalEntityCombiningFilter());
    filter.setFiltersToUse(underlyingFilters);
    final Set<Object> expected = new HashSet<>();
    expected.add(LegalEntityTest.SECTOR.getName());
    expected.add(Pair.of("S&P", "A"));
    assertEquals(expected, filter.getFilteredData(LegalEntityTest.LEGAL_ENTITY));
    assertEquals(expected, filter.getFilteredData(LegalEntityTest.LEGAL_ENTITY_RED_CODE));
    assertEquals(ParameterizedTypeImpl.of(Set.class, VariantType.either(String.class, ParameterizedTypeImpl.of(Pair.class, String.class, String.class))), filter.getFilteredDataType());
  }
}

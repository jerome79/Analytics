/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.definition;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.time.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.ExerciseDecisionType;
import com.opengamma.analytics.financial.commodity.derivative.EnergyFutureOption;
import com.opengamma.analytics.util.time.DateUtils;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.collect.id.StandardId;


/**
 * Test.
 */
@Test
public class EnergyFutureOptionDefinitionTest {
  private final static StandardId AN_ID = StandardId.of("Scheme", "value");
  private final static ZonedDateTime FIRST_DELIVERY_DATE = DateUtils.getUTCDate(2011, 9, 21);
  private final static ZonedDateTime LAST_DELIVERY_DATE = DateUtils.getUTCDate(2012, 9, 21);
  private final static ZonedDateTime EXPIRY_DATE = DateUtils.getUTCDate(2011, 9, 21);
  private final static ZonedDateTime SETTLEMENT_DATE = LAST_DELIVERY_DATE;
  private final static ZonedDateTime A_DATE = DateUtils.getUTCDate(2011, 9, 20);
  private final static EnergyFutureDefinition AN_UNDERLYING = new EnergyFutureDefinition(EXPIRY_DATE, AN_ID, 100, FIRST_DELIVERY_DATE, LAST_DELIVERY_DATE, 1000, "tonnes", SettlementType.PHYSICAL, 0,
      Currency.GBP, SETTLEMENT_DATE);

  /**
   * Test hashCode and equals methods.
   */
  @Test()
  public void testHashEquals() {
    EnergyFutureOptionDefinition first = new EnergyFutureOptionDefinition(EXPIRY_DATE, AN_UNDERLYING, 100, ExerciseDecisionType.EUROPEAN, true);
    EnergyFutureOptionDefinition second = new EnergyFutureOptionDefinition(EXPIRY_DATE, AN_UNDERLYING, 100, ExerciseDecisionType.EUROPEAN, true);
    assertEquals(first, second);
    assertEquals(first.hashCode(), second.hashCode());
    EnergyFutureOptionDefinition third = new EnergyFutureOptionDefinition(EXPIRY_DATE, AN_UNDERLYING, 100, ExerciseDecisionType.AMERICAN, false);
    assertFalse(first.equals(third));
    assertFalse(second.hashCode() == third.hashCode());
  }

  /**
   * Test getters
   */
  @Test
  public void testGetters() {
    EnergyFutureOptionDefinition first = new EnergyFutureOptionDefinition(EXPIRY_DATE, AN_UNDERLYING, 100, ExerciseDecisionType.EUROPEAN, true);
    EnergyFutureOptionDefinition second = new EnergyFutureOptionDefinition(EXPIRY_DATE, AN_UNDERLYING, 100, ExerciseDecisionType.AMERICAN, false);
    assertEquals(first.getStrike(), 100.);
    assertEquals(first.getExerciseType(), ExerciseDecisionType.EUROPEAN);
    assertEquals(first.getUnderlying(), AN_UNDERLYING);
    assertTrue(first.isCall());
    assertEquals(second.getExerciseType(), ExerciseDecisionType.AMERICAN);
    assertFalse(second.isCall());
  }

  /**
   * Test method for {@link EnergyFutureOptionDefinition#toDerivative(javax.time.calendar.ZonedDateTime)}.
   */
  @Test
  public void testToDerivative() {
    EnergyFutureOptionDefinition first = new EnergyFutureOptionDefinition(EXPIRY_DATE, AN_UNDERLYING, 100, ExerciseDecisionType.EUROPEAN, true);
    EnergyFutureOptionDefinition second = new EnergyFutureOptionDefinition(EXPIRY_DATE, AN_UNDERLYING, 100, ExerciseDecisionType.AMERICAN, false);

    EnergyFutureOption firstDerivative = first.toDerivative(A_DATE);
    EnergyFutureOption secondDerivative = second.toDerivative(A_DATE);
    assertEquals(firstDerivative.getStrike(), 100.);
    assertEquals(firstDerivative.getExerciseType(), ExerciseDecisionType.EUROPEAN);
    assertEquals(firstDerivative.getUnderlying(), AN_UNDERLYING.toDerivative(A_DATE));
    assertTrue(firstDerivative.isCall());
    assertEquals(secondDerivative.getExerciseType(), ExerciseDecisionType.AMERICAN);
    assertFalse(secondDerivative.isCall());

    EnergyFutureOption firstDerivative2 = new EnergyFutureOption(0.0027397260273972603, AN_UNDERLYING.toDerivative(A_DATE), 100, ExerciseDecisionType.EUROPEAN, true);
    assertEquals(firstDerivative.hashCode(), firstDerivative2.hashCode());
    assertEquals(firstDerivative, firstDerivative2);
  }

}

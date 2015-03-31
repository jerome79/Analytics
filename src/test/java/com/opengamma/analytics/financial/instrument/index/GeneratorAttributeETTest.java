/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;


/**
 * Tests related to the generator attribute for Exchange Traded instrument.
 */
@Test
public class GeneratorAttributeETTest {
  
  private final static boolean IS_PRICE = true;
  private final static GeneratorAttributeET ATTRIBUTE_ET = new GeneratorAttributeET(IS_PRICE);
  
  @Test
  public void getter() {
    assertEquals("GeneratorAttributeET: getter", IS_PRICE, ATTRIBUTE_ET.isPrice());
  }
  
}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.greeks;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test.
 */
@Test
@SuppressWarnings("synthetic-access")
public class GreekVisitorTest {
  private static final String STRING = "X";
  private static final GreekVisitor<?> NO_ACTION = new NoActionGreekVisitor();
  private static final GreekVisitor<?> DELTA_ONLY = new DeltaOnlyGreekVisitor();

  @Test
  public void testExceptions() {
    final Set<Greek> greeks = Greek.getAllGreeks();
    for (final Greek g : greeks) {
      try {
        g.accept(NO_ACTION);
        Assert.fail();
      } catch (final UnsupportedOperationException e) {
      }
      if (g.equals(Greek.DELTA)) {
        assertEquals(STRING, g.accept(DELTA_ONLY));
      } else {
        try {
          g.accept(DELTA_ONLY);
          Assert.fail();
        } catch (final UnsupportedOperationException e) {
        }
      }
    }
  }

  private static final class NoActionGreekVisitor extends AbstractGreekVisitor<Object> {

  }

  private static final class DeltaOnlyGreekVisitor extends AbstractGreekVisitor<String> {

    @Override
    public String visitDelta() {
      return STRING;
    }
  }
}

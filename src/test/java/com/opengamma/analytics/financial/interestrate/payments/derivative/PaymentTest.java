/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.derivative;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.strata.basics.currency.Currency;

/**
 * Test.
 */
@Test
public class PaymentTest {
  private static final Currency CCY = Currency.CAD;
  private static final double PAYMENT_TIME = 0.5;
  private static final MyPayment PAYMENT = new MyPayment(CCY, PAYMENT_TIME);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency() {
    new MyPayment(null, PAYMENT_TIME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativePaymentTime() {
    new MyPayment(CCY, -PAYMENT_TIME);
  }

  @Test
  public void testObject() {
    assertEquals(CCY, PAYMENT.getCurrency());
    assertEquals(PAYMENT_TIME, PAYMENT.getPaymentTime(), 0);
    MyPayment other = new MyPayment(CCY, PAYMENT_TIME);
    assertEquals(PAYMENT, other);
    assertEquals(PAYMENT.hashCode(), other.hashCode());
    assertEquals("Currency=CAD, payment time=0.5", PAYMENT.toString());
    other = new MyPayment(Currency.AUD, PAYMENT_TIME);
    assertFalse(other.equals(PAYMENT));
    other = new MyPayment(CCY, PAYMENT_TIME + 1);
    assertFalse(other.equals(PAYMENT));
  }

  private static class MyPayment extends Payment {

    public MyPayment(final Currency currency, final double paymentTime) {
      super(currency, paymentTime);
    }

    @Override
    public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
      throw new UnsupportedOperationException();
    }

    @Override
    public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
      throw new UnsupportedOperationException();
    }

    @Override
    public double getReferenceAmount() {
      throw new UnsupportedOperationException();
    }

  }
}

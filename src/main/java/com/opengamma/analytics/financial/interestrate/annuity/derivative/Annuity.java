/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.annuity.derivative;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.collect.ArgChecker;

/**
 * A generic annuity is a set of payments (cash flows) at known future times.
 * All payments have the same currency.
 * There payments can be known in advance, or depend on the future value of some
 * (possibly several) indices, e.g. the Libor.
 * 
 * @param <P> The payment type
 */
public class Annuity<P extends Payment> implements InstrumentDerivative {

  /**
   * The list of the annuity payments.
   */
  private final P[] _payments;
  /**
   * Flag indicating if the annuity is payer (true) or receiver (false). Deduced from the first non-zero amount;
   * if all amounts don't have the same sign, the flag may be incorrect.
   */
  private final boolean _isPayer;

  /**
   * @param payments The payments, not null or empty
   */
  public Annuity(P[] payments) {
    ArgChecker.noNulls(payments, "payments");
    ArgChecker.isTrue(payments.length > 0, "Have no payments in annuity");
    Currency currency0 = payments[0].getCurrency();
    double amount = payments[0].getReferenceAmount();
    for (int loopcpn = 1; loopcpn < payments.length; loopcpn++) {
      ArgChecker.isTrue(currency0.equals(payments[loopcpn].getCurrency()), "currency not the same for all payments");
      amount = (amount == 0) ? payments[loopcpn].getReferenceAmount() : amount;
    }
    _payments = payments;
    _isPayer = (amount < 0);
  }

  /**
   * @param payments The payments, not null or empty
   * @param pType The type of the payments, not null
   * @param isPayer True if the annuity is to be paid
   */
  @SuppressWarnings("unchecked")
  public Annuity(List<? extends P> payments, Class<P> pType, boolean isPayer) {
    ArgChecker.noNulls(payments, "payments");
    ArgChecker.notNull(pType, "type");
    ArgChecker.isTrue(payments.size() > 0, "Payments size must be greater than zero");
    _payments = payments.toArray((P[]) Array.newInstance(pType, 0));
    _isPayer = isPayer;
  }

  /**
   * Gets the number of payments in the annuity.
   * @return The number of payments
   */
  public int getNumberOfPayments() {
    return _payments.length;
  }

  /**
   * Gets the nth payment in an annuity. <b>Note that n = 0 will give the first payment</b>.
   * @param n The number of the payment
   * @return The nth payment
   */
  public P getNthPayment(int n) {
    return _payments[n];
  }

  /**
   * Return the currency of the annuity.
   * @return The currency.
   */
  public Currency getCurrency() {
    return _payments[0].getCurrency();
  }

  /**
   * Check if the payments of an annuity is of the type CouponFixed or CouponIbor. Used to check that payment are of vanilla type.
   * @return  True if IborCoupon or FixedCoupon
   */
  public boolean isIborOrFixed() { //TODO: is this method necessary?
    boolean result = true;
    for (P payment : _payments) {
      result = result && payment.isIborOrFixed();
    }
    return result;
  }

  /**
   * Gets the payments array.
   * @return the payments
   */
  public P[] getPayments() {
    return _payments;
  }

  /**
   * Gets the payer flag: payer (true) or receiver (false)
   * @return The payer flag.
   * @deprecated The payer flag is no longer used; the sign of the notional
   * determines whether a leg is paid or received
   */
  @Deprecated
  public boolean isPayer() {
    return _isPayer;
  }

  /**
   * Create a new annuity with the payments of the original one paying strictly after the given time.
   * @param trimTime The time.
   * @return The trimmed annuity.
   */
  @SuppressWarnings("unchecked")
  public Annuity<P> trimBefore(double trimTime) {
    List<P> list = new ArrayList<>();
    list.clear();
    for (P payment : _payments) {
      if (payment.getPaymentTime() > trimTime) {
        list.add(payment);
      }
    }
    return new Annuity<>(list.toArray((P[]) new Payment[list.size()]));
  }

  /**
   * Create a new annuity with the payments of the original one paying before or on the given time.
   * @param trimTime The time.
   * @return The trimmed annuity.
   */
  @SuppressWarnings("unchecked")
  public Annuity<P> trimAfter(double trimTime) {
    List<P> list = new ArrayList<>();
    for (P payment : _payments) {
      if (payment.getPaymentTime() <= trimTime) {
        list.add(payment);
      }
    }
    return new Annuity<>(list.toArray((P[]) new Payment[list.size()]));
  }

  @Override
  public String toString() {
    StringBuffer result = new StringBuffer("Annuity:");
    for (P payment : _payments) {
      result.append(payment.toString());
      result.append("\n");
    }
    return result.toString();
  }

  @Override
  public int hashCode() {
    int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_payments);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Annuity<?> other = (Annuity<?>) obj;
    if (_payments.length != other._payments.length) {
      return false;
    }
    for (int i = 0; i < _payments.length; i++) {
      if (!Objects.equals(_payments[i], other._payments[i])) {
        return false;
      }
    }
    return true;
  }

  @Override
  public <S, T> T accept(InstrumentDerivativeVisitor<S, T> visitor, S data) {
    ArgChecker.notNull(visitor, "visitor");
    return visitor.visitGenericAnnuity(this, data);
  }

  @Override
  public <T> T accept(InstrumentDerivativeVisitor<?, T> visitor) {
    ArgChecker.notNull(visitor, "visitor");
    return visitor.visitGenericAnnuity(this);
  }

}

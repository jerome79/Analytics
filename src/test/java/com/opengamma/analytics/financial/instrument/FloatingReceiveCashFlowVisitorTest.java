/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument;

import static org.testng.AssertJUnit.assertEquals;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.testng.annotations.Test;

import com.google.common.collect.Iterables;
import com.opengamma.strata.basics.currency.CurrencyAmount;
import com.opengamma.strata.basics.currency.MultiCurrencyAmount;


/**
 * Test.
 */
@Test
public class FloatingReceiveCashFlowVisitorTest {
  private static final FloatingReceiveCashFlowVisitor VISITOR = FloatingReceiveCashFlowVisitor.getInstance();

  @Test
  public void testIborDeposit() {
    final Map<LocalDate, MultiCurrencyAmount> payment = InstrumentTestHelper.RECEIVE_IBOR_DEPOSIT.accept(VISITOR);
    assertEquals(1, payment.size());
    assertEquals(InstrumentTestHelper.RECEIVE_IBOR_DEPOSIT.getEndDate().toLocalDate(), Iterables.getOnlyElement(payment.keySet()));
    final MultiCurrencyAmount mca = Iterables.getOnlyElement(payment.values());
    assertEquals(1, mca.size());
    final CurrencyAmount ca = Iterables.getOnlyElement(mca.getAmounts());
    assertEquals(InstrumentTestHelper.FIXED_INCOME_CURRENCY, ca.getCurrency());
    assertEquals(-InstrumentTestHelper.DEPOSIT_NOTIONAL * InstrumentTestHelper.DEPOSIT_RATE / 2., ca.getAmount(), 1e-15);
    assertEquals(payment, InstrumentTestHelper.RECEIVE_IBOR_DEPOSIT.accept(VISITOR, null));
    assertEquals(Collections.emptyMap(), InstrumentTestHelper.PAY_IBOR_DEPOSIT.accept(VISITOR));
  }

  @Test
  public void testIborCoupon() {
    Map<LocalDate, MultiCurrencyAmount> payment = InstrumentTestHelper.RECEIVE_IBOR_COUPON.accept(VISITOR);
    assertEquals(1, payment.size());
    assertEquals(InstrumentTestHelper.RECEIVE_IBOR_COUPON.getPaymentDate().toLocalDate(), Iterables.getOnlyElement(payment.keySet()));
    MultiCurrencyAmount mca = Iterables.getOnlyElement(payment.values());
    assertEquals(1, mca.size());
    CurrencyAmount ca = Iterables.getOnlyElement(mca.getAmounts());
    assertEquals(InstrumentTestHelper.FIXED_INCOME_CURRENCY, ca.getCurrency());
    assertEquals(-InstrumentTestHelper.IBOR_COUPON_NOTIONAL / 2., ca.getAmount(), 1e-15);
    assertEquals(payment, InstrumentTestHelper.RECEIVE_IBOR_COUPON.accept(VISITOR, null));
    assertEquals(Collections.emptyMap(), InstrumentTestHelper.PAY_IBOR_COUPON.accept(VISITOR));
    payment = InstrumentTestHelper.RECEIVE_IBOR_SPREAD_COUPON.accept(VISITOR);
    assertEquals(1, payment.size());
    assertEquals(InstrumentTestHelper.RECEIVE_IBOR_SPREAD_COUPON.getPaymentDate().toLocalDate(), Iterables.getOnlyElement(payment.keySet()));
    mca = Iterables.getOnlyElement(payment.values());
    assertEquals(1, mca.size());
    ca = Iterables.getOnlyElement(mca.getAmounts());
    assertEquals(InstrumentTestHelper.FIXED_INCOME_CURRENCY, ca.getCurrency());
    assertEquals(-InstrumentTestHelper.IBOR_COUPON_NOTIONAL / 2. - InstrumentTestHelper.IBOR_COUPON_NOTIONAL * InstrumentTestHelper.IBOR_COUPON_SPREAD / 2., ca.getAmount(), 1e-15);
    assertEquals(payment, InstrumentTestHelper.RECEIVE_IBOR_SPREAD_COUPON.accept(VISITOR, null));
    assertEquals(Collections.emptyMap(), InstrumentTestHelper.PAY_IBOR_SPREAD_COUPON.accept(VISITOR));
    payment = InstrumentTestHelper.RECEIVE_IBOR_GEARING_COUPON.accept(VISITOR);
    assertEquals(1, payment.size());
    assertEquals(InstrumentTestHelper.RECEIVE_IBOR_GEARING_COUPON.getPaymentDate().toLocalDate(), Iterables.getOnlyElement(payment.keySet()));
    mca = Iterables.getOnlyElement(payment.values());
    assertEquals(1, mca.size());
    ca = Iterables.getOnlyElement(mca.getAmounts());
    assertEquals(InstrumentTestHelper.FIXED_INCOME_CURRENCY, ca.getCurrency());
    assertEquals(-InstrumentTestHelper.IBOR_COUPON_NOTIONAL * InstrumentTestHelper.GEARING / 2. - InstrumentTestHelper.IBOR_COUPON_NOTIONAL * InstrumentTestHelper.IBOR_COUPON_SPREAD / 2., ca.getAmount(), 1e-15);
    assertEquals(payment, InstrumentTestHelper.RECEIVE_IBOR_GEARING_COUPON.accept(VISITOR, null));
    assertEquals(Collections.emptyMap(), InstrumentTestHelper.PAY_IBOR_GEARING_COUPON.accept(VISITOR));
  }

  @Test
  public void testFRA() {
    final Map<LocalDate, MultiCurrencyAmount> payment = InstrumentTestHelper.PAYER_FRA.accept(VISITOR);
    assertEquals(1, payment.size());
    assertEquals(InstrumentTestHelper.PAYER_FRA.getPaymentDate().toLocalDate(), Iterables.getOnlyElement(payment.keySet()));
    final MultiCurrencyAmount mca = Iterables.getOnlyElement(payment.values());
    assertEquals(1, mca.size());
    final CurrencyAmount ca = Iterables.getOnlyElement(mca.getAmounts());
    assertEquals(InstrumentTestHelper.FIXED_INCOME_CURRENCY, ca.getCurrency());
    assertEquals(InstrumentTestHelper.FRA_NOTIONAL * 0.5, ca.getAmount());
    assertEquals(payment, InstrumentTestHelper.PAYER_FRA.accept(VISITOR, null));
    assertEquals(Collections.emptyMap(), InstrumentTestHelper.RECEIVER_FRA.accept(VISITOR));
  }

  @Test
  public void testSwap() {
    Map<LocalDate, MultiCurrencyAmount> payments = new TreeMap<>(InstrumentTestHelper.PAYER_SWAP.accept(VISITOR));
    assertEquals(60, payments.size());
    LocalDate paymentDate = InstrumentTestHelper.SWAP_START.plusMonths(6).toLocalDate();
    for (final Map.Entry<LocalDate, MultiCurrencyAmount> entry : payments.entrySet()) {
      assertEquals(paymentDate, entry.getKey());
      paymentDate = paymentDate.plusMonths(6);
      final MultiCurrencyAmount mca = entry.getValue();
      assertEquals(1, mca.size());
      final CurrencyAmount ca = Iterables.getOnlyElement(mca.getAmounts());
      assertEquals(InstrumentTestHelper.FIXED_INCOME_CURRENCY, ca.getCurrency());
      assertEquals(InstrumentTestHelper.SWAP_NOTIONAL * 0.5, ca.getAmount());
    }
    assertEquals(payments, InstrumentTestHelper.PAYER_SWAP.accept(VISITOR, null));
    payments = new TreeMap<>(InstrumentTestHelper.PAYER_SWAP_WITH_SPREAD.accept(VISITOR));
    assertEquals(60, payments.size());
    paymentDate = InstrumentTestHelper.SWAP_START.plusMonths(6).toLocalDate();
    for (final Map.Entry<LocalDate, MultiCurrencyAmount> entry : payments.entrySet()) {
      assertEquals(paymentDate, entry.getKey());
      paymentDate = paymentDate.plusMonths(6);
      final MultiCurrencyAmount mca = entry.getValue();
      assertEquals(1, mca.size());
      final CurrencyAmount ca = Iterables.getOnlyElement(mca.getAmounts());
      assertEquals(InstrumentTestHelper.FIXED_INCOME_CURRENCY, ca.getCurrency());
      assertEquals(InstrumentTestHelper.SWAP_NOTIONAL / 2. + InstrumentTestHelper.SWAP_NOTIONAL * InstrumentTestHelper.IBOR_SPREAD / 2., ca.getAmount());
    }
    assertEquals(payments, InstrumentTestHelper.PAYER_SWAP_WITH_SPREAD.accept(VISITOR, null));
    assertEquals(Collections.emptyMap(), InstrumentTestHelper.RECEIVER_SWAP.accept(VISITOR));
    assertEquals(Collections.emptyMap(), InstrumentTestHelper.RECEIVER_SWAP_WITH_SPREAD.accept(VISITOR));
  }

  @Test
  public void testIborIborSwap() {
    Map<LocalDate, MultiCurrencyAmount> payments = new TreeMap<>(InstrumentTestHelper.RECEIVE_SPREAD_IBOR_IBOR_SWAP.accept(VISITOR));
    assertEquals(100, payments.size());
    LocalDate paymentDate = InstrumentTestHelper.SWAP_START.plusMonths(6).toLocalDate();
    for (final Map.Entry<LocalDate, MultiCurrencyAmount> entry : payments.entrySet()) {
      assertEquals(paymentDate, entry.getKey());
      paymentDate = paymentDate.plusMonths(6);
      final MultiCurrencyAmount mca = entry.getValue();
      assertEquals(1, mca.size());
      final CurrencyAmount ca = Iterables.getOnlyElement(mca.getAmounts());
      assertEquals(InstrumentTestHelper.FIXED_INCOME_CURRENCY, ca.getCurrency());
      assertEquals(InstrumentTestHelper.SWAP_NOTIONAL / 2. + InstrumentTestHelper.SWAP_NOTIONAL * InstrumentTestHelper.IBOR_SPREAD / 2., ca.getAmount());
    }
    assertEquals(payments, InstrumentTestHelper.RECEIVE_SPREAD_IBOR_IBOR_SWAP.accept(VISITOR, null));
    payments = new TreeMap<>(InstrumentTestHelper.PAY_SPREAD_IBOR_IBOR_SWAP.accept(VISITOR));
    assertEquals(200, payments.size());
    paymentDate = InstrumentTestHelper.SWAP_START.plusMonths(3).toLocalDate();
    for (final Map.Entry<LocalDate, MultiCurrencyAmount> entry : payments.entrySet()) {
      assertEquals(paymentDate, entry.getKey());
      paymentDate = paymentDate.plusMonths(3);
      final MultiCurrencyAmount mca = entry.getValue();
      assertEquals(1, mca.size());
      final CurrencyAmount ca = Iterables.getOnlyElement(mca.getAmounts());
      assertEquals(InstrumentTestHelper.FIXED_INCOME_CURRENCY, ca.getCurrency());
      assertEquals(InstrumentTestHelper.SWAP_NOTIONAL / 4., ca.getAmount());
    }
    assertEquals(payments, InstrumentTestHelper.PAY_SPREAD_IBOR_IBOR_SWAP.accept(VISITOR, null));
  }
}

/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument;

import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.CASH_MATURITY;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.CASH_NOTIONAL;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.CASH_RATE;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.FIXED_COUPON_MATURITY;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.FIXED_COUPON_NOTIONAL;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.FIXED_COUPON_RATE;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.FIXED_INCOME_CURRENCY;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.FIXING_RATE;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.FRA_END;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.FRA_NOTIONAL;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.FRA_RATE;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.FRA_START;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.FX_MATURITY;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.FX_PAY_AMOUNT;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.FX_PAY_CURRENCY;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.FX_PAY_EUR;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.FX_PAY_GBP;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.FX_RECEIVE_AMOUNT;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.FX_RECEIVE_CURRENCY;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.IBOR_COUPON_SPREAD;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.IBOR_FIXING_SERIES;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.IBOR_SPREAD;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.LONG_NDF;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.NO_HOLIDAY;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.PAYER_FRA;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.PAYER_SWAP;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.PAYER_SWAP_WITH_SPREAD;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.PAYMENT_AMOUNT;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.PAYMENT_MATURITY;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.PAY_CASH;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.PAY_FIXED_COUPON;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.PAY_FIXED_PAYMENT;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.PAY_IBOR_COUPON;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.PAY_IBOR_SPREAD_COUPON;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.RECEIVER_FRA;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.RECEIVER_SWAP;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.RECEIVER_SWAP_WITH_SPREAD;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.RECEIVE_CASH;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.RECEIVE_FIXED_COUPON;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.RECEIVE_FIXED_PAYMENT;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.RECEIVE_IBOR_COUPON;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.RECEIVE_IBOR_SPREAD_COUPON;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.SHORT_NDF;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.SWAP_FIXED_RATE;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.SWAP_NOTIONAL;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.SWAP_START;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.USD_IBOR_INDEX1;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.testng.annotations.Test;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.strata.basics.currency.CurrencyAmount;
import com.opengamma.strata.basics.currency.MultiCurrencyAmount;
import com.opengamma.strata.collect.timeseries.LocalDateDoubleTimeSeries;

/**
 * Test.
 */
@Test
public class FixedPayCashFlowVisitorTest {
  private static final ForwardRateAgreementDefinition RECEIVER_FRA_UNFIXED = ForwardRateAgreementDefinition.from(FRA_START.plusYears(3), FRA_END.plusYears(3),
      -FRA_NOTIONAL, USD_IBOR_INDEX1, FRA_RATE, NO_HOLIDAY);
  private static final Set<InstrumentDefinition<?>> INSTRUMENTS_WITHOUT_FIXINGS;
  private static final Set<InstrumentDefinition<?>> INSTRUMENTS_WITH_OPTIONAL_FIXING_SERIES;
  private static final Set<InstrumentDefinition<?>> INSTRUMENTS_WITH_MANDATORY_FIXING_SERIES;
  private static final FixedPayCashFlowVisitor VISITOR = FixedPayCashFlowVisitor.getInstance();

  static {
    INSTRUMENTS_WITHOUT_FIXINGS = new HashSet<>();
    INSTRUMENTS_WITHOUT_FIXINGS.add(PAY_CASH);
    INSTRUMENTS_WITHOUT_FIXINGS.add(PAY_FIXED_PAYMENT);
    INSTRUMENTS_WITHOUT_FIXINGS.add(PAY_FIXED_COUPON);
    INSTRUMENTS_WITHOUT_FIXINGS.add(PAYER_FRA);
    INSTRUMENTS_WITHOUT_FIXINGS.add(PAYER_SWAP);
    INSTRUMENTS_WITHOUT_FIXINGS.add(PAYER_SWAP_WITH_SPREAD);
    INSTRUMENTS_WITHOUT_FIXINGS.add(FX_PAY_GBP);
    INSTRUMENTS_WITHOUT_FIXINGS.add(FX_PAY_EUR);
    INSTRUMENTS_WITHOUT_FIXINGS.add(LONG_NDF);
    INSTRUMENTS_WITHOUT_FIXINGS.add(SHORT_NDF);
    INSTRUMENTS_WITH_OPTIONAL_FIXING_SERIES = new HashSet<>();
    INSTRUMENTS_WITH_OPTIONAL_FIXING_SERIES.add(RECEIVER_FRA);
    INSTRUMENTS_WITH_OPTIONAL_FIXING_SERIES.add(RECEIVER_SWAP);
    INSTRUMENTS_WITH_OPTIONAL_FIXING_SERIES.add(RECEIVER_SWAP_WITH_SPREAD);
    INSTRUMENTS_WITH_MANDATORY_FIXING_SERIES = new HashSet<>();
    INSTRUMENTS_WITH_MANDATORY_FIXING_SERIES.add(PAY_IBOR_COUPON);
    INSTRUMENTS_WITH_MANDATORY_FIXING_SERIES.add(PAY_IBOR_SPREAD_COUPON);

  }

  @Test
  public void testNoFixingData() {
    for (final InstrumentDefinition<?> definition : INSTRUMENTS_WITH_OPTIONAL_FIXING_SERIES) {
      try {
        definition.accept(VISITOR);
        fail();
      } catch (final IllegalArgumentException e) {
      }
    }
    for (final InstrumentDefinition<?> definition : INSTRUMENTS_WITH_MANDATORY_FIXING_SERIES) {
      try {
        definition.accept(VISITOR);
        fail();
      } catch (final IllegalArgumentException e) {
      }
    }
  }

  @Test
  public void testNoFixingNeeded() {
    for (final InstrumentDefinition<?> definition : INSTRUMENTS_WITHOUT_FIXINGS) {
      assertEquals(definition.accept(VISITOR), definition.accept(VISITOR, null));
    }
  }

  @Test
  public void testMissingFixingData() {
    final List<LocalDate> dates = new ArrayList<>();
    final List<Double> fixings = new ArrayList<>();
    LocalDate date = LocalDate.of(2013, 1, 1);
    final LocalDate lastDate = LocalDate.of(2014, 1, 1);
    while (date.isBefore(lastDate)) {
      dates.add(date);
      fixings.add(FIXING_RATE);
      date = date.plusDays(1);
    }
    final LocalDateDoubleTimeSeries fixingSeries =
        LocalDateDoubleTimeSeries.builder()
            .putAll(dates, fixings)
            .build();
    final Set<InstrumentDefinition<?>> floatingInstruments = new HashSet<>(INSTRUMENTS_WITH_MANDATORY_FIXING_SERIES);
    floatingInstruments.addAll(INSTRUMENTS_WITH_OPTIONAL_FIXING_SERIES);
    for (final InstrumentDefinition<?> definition : floatingInstruments) {
      try {
        definition.accept(VISITOR, fixingSeries);
        fail();
      } catch (final IllegalArgumentException e) {
      }
    }
  }

  @Test
  public void testCash() {
    final Map<LocalDate, MultiCurrencyAmount> payment = PAY_CASH.accept(VISITOR);
    assertEquals(1, payment.size());
    assertEquals(CASH_MATURITY.toLocalDate(), Iterables.getOnlyElement(payment.keySet()));
    final MultiCurrencyAmount mca = Iterables.getOnlyElement(payment.values());
    assertEquals(1, mca.size());
    final CurrencyAmount ca = Iterables.getOnlyElement(mca.getAmounts());
    assertEquals(FIXED_INCOME_CURRENCY, ca.getCurrency());
    assertEquals(CASH_NOTIONAL * CASH_RATE * 0.5, ca.getAmount());
    assertEquals(Collections.emptyMap(), RECEIVE_CASH.accept(VISITOR));
  }

  @Test
  public void testFixedPayment() {
    final Map<LocalDate, MultiCurrencyAmount> payment = PAY_FIXED_PAYMENT.accept(VISITOR);
    assertEquals(1, payment.size());
    assertEquals(PAYMENT_MATURITY.toLocalDate(), Iterables.getOnlyElement(payment.keySet()));
    final MultiCurrencyAmount mca = Iterables.getOnlyElement(payment.values());
    assertEquals(1, mca.size());
    final CurrencyAmount ca = Iterables.getOnlyElement(mca.getAmounts());
    assertEquals(FIXED_INCOME_CURRENCY, ca.getCurrency());
    assertEquals(PAYMENT_AMOUNT, ca.getAmount());
    assertEquals(Collections.emptyMap(), RECEIVE_FIXED_PAYMENT.accept(VISITOR));
  }

  @Test
  public void testFixedCoupon() {
    final Map<LocalDate, MultiCurrencyAmount> payment = PAY_FIXED_COUPON.accept(VISITOR);
    assertEquals(1, payment.size());
    assertEquals(FIXED_COUPON_MATURITY.toLocalDate(), Iterables.getOnlyElement(payment.keySet()));
    final MultiCurrencyAmount mca = Iterables.getOnlyElement(payment.values());
    assertEquals(1, mca.size());
    final CurrencyAmount ca = Iterables.getOnlyElement(mca.getAmounts());
    assertEquals(FIXED_INCOME_CURRENCY, ca.getCurrency());
    assertEquals(FIXED_COUPON_NOTIONAL * FIXED_COUPON_RATE / 12, ca.getAmount(), 1e-15);
    assertEquals(Collections.emptyMap(), RECEIVE_FIXED_COUPON.accept(VISITOR));
  }

  @Test
  public void testIborCoupon() {
    Map<LocalDate, MultiCurrencyAmount> payment = PAY_IBOR_COUPON.accept(VISITOR, IBOR_FIXING_SERIES);
    assertEquals(1, payment.size());
    assertEquals(FIXED_COUPON_MATURITY.toLocalDate().plusMonths(6), Iterables.getOnlyElement(payment.keySet()));
    MultiCurrencyAmount mca = Iterables.getOnlyElement(payment.values());
    assertEquals(1, mca.size());
    CurrencyAmount ca = Iterables.getOnlyElement(mca.getAmounts());
    assertEquals(FIXED_INCOME_CURRENCY, ca.getCurrency());
    assertEquals(FIXED_COUPON_NOTIONAL * FIXING_RATE / 2, ca.getAmount(), 1e-15);
    assertEquals(Collections.emptyMap(), RECEIVE_IBOR_COUPON.accept(VISITOR, IBOR_FIXING_SERIES));
    payment = PAY_IBOR_SPREAD_COUPON.accept(VISITOR, IBOR_FIXING_SERIES);
    assertEquals(1, payment.size());
    assertEquals(FIXED_COUPON_MATURITY.toLocalDate().plusMonths(6), Iterables.getOnlyElement(payment.keySet()));
    mca = Iterables.getOnlyElement(payment.values());
    assertEquals(1, mca.size());
    ca = Iterables.getOnlyElement(mca.getAmounts());
    assertEquals(FIXED_INCOME_CURRENCY, ca.getCurrency());
    assertEquals(FIXED_COUPON_NOTIONAL * (FIXING_RATE + IBOR_COUPON_SPREAD) / 2, ca.getAmount(), 1e-15);
    assertEquals(Collections.emptyMap(), RECEIVE_IBOR_SPREAD_COUPON.accept(VISITOR, IBOR_FIXING_SERIES));
  }

  @Test
  public void testPayerFRA() {
    final Map<LocalDate, MultiCurrencyAmount> payment = PAYER_FRA.accept(VISITOR);
    assertEquals(1, payment.size());
    assertEquals(FRA_START.toLocalDate(), Iterables.getOnlyElement(payment.keySet()));
    final MultiCurrencyAmount mca = Iterables.getOnlyElement(payment.values());
    assertEquals(1, mca.size());
    final CurrencyAmount ca = Iterables.getOnlyElement(mca.getAmounts());
    assertEquals(FIXED_INCOME_CURRENCY, ca.getCurrency());
    assertEquals(FRA_NOTIONAL * FRA_RATE * 0.5, ca.getAmount(), 1e-15);
  }

  @Test
  public void testReceiverFRA() {
    final Map<LocalDate, MultiCurrencyAmount> payment = RECEIVER_FRA.accept(VISITOR, IBOR_FIXING_SERIES);
    assertEquals(1, payment.size());
    assertEquals(FRA_START.toLocalDate(), Iterables.getOnlyElement(payment.keySet()));
    final MultiCurrencyAmount mca = Iterables.getOnlyElement(payment.values());
    assertEquals(1, mca.size());
    final CurrencyAmount ca = Iterables.getOnlyElement(mca.getAmounts());
    assertEquals(FIXED_INCOME_CURRENCY, ca.getCurrency());
    assertEquals(FRA_NOTIONAL * FIXING_RATE * 0.5, ca.getAmount(), 1e-15);
    assertEquals(Collections.emptyMap(), RECEIVER_FRA_UNFIXED.accept(VISITOR, IBOR_FIXING_SERIES));
  }

  @Test
  public void testPayerSwap() {
    Map<LocalDate, MultiCurrencyAmount> payments = new TreeMap<>(PAYER_SWAP.accept(VISITOR));
    assertEquals(60, payments.size());
    LocalDate paymentDate = SWAP_START.toLocalDate().plusMonths(6);
    for (final Map.Entry<LocalDate, MultiCurrencyAmount> entry : payments.entrySet()) {
      assertEquals(paymentDate, entry.getKey());
      assertEquals(1, entry.getValue().size());
      assertEquals(FIXED_INCOME_CURRENCY, entry.getValue().stream().findFirst().get().getCurrency());
      assertEquals(SWAP_NOTIONAL * SWAP_FIXED_RATE / 2, entry.getValue().stream().findFirst().get().getAmount(), 1e-15);
      paymentDate = paymentDate.plusMonths(6);
    }
    payments = new TreeMap<>(PAYER_SWAP_WITH_SPREAD.accept(VISITOR));
    assertEquals(60, payments.size());
    paymentDate = SWAP_START.toLocalDate().plusMonths(6);
    for (final Map.Entry<LocalDate, MultiCurrencyAmount> entry : payments.entrySet()) {
      assertEquals(paymentDate, entry.getKey());
      assertEquals(1, entry.getValue().size());
      assertEquals(FIXED_INCOME_CURRENCY, entry.getValue().stream().findFirst().get().getCurrency());
      assertEquals(SWAP_NOTIONAL * SWAP_FIXED_RATE / 2, entry.getValue().stream().findFirst().get().getAmount(), 1e-15);
      paymentDate = paymentDate.plusMonths(6);
    }
  }

  @Test
  public void testReceiverSwap() {
    Map<LocalDate, MultiCurrencyAmount> payments = new TreeMap<>(RECEIVER_SWAP.accept(VISITOR, IBOR_FIXING_SERIES));
    assertEquals(24, payments.size());
    LocalDate paymentDate = SWAP_START.toLocalDate().plusMonths(6);
    for (final Map.Entry<LocalDate, MultiCurrencyAmount> entry : payments.entrySet()) {
      assertEquals(paymentDate, entry.getKey());
      assertEquals(1, entry.getValue().size());
      assertEquals(FIXED_INCOME_CURRENCY, entry.getValue().stream().findFirst().get().getCurrency());
      assertEquals(SWAP_NOTIONAL * FIXING_RATE / 2, entry.getValue().stream().findFirst().get().getAmount(), 1e-15);
      paymentDate = paymentDate.plusMonths(6);
    }
    payments = new TreeMap<>(RECEIVER_SWAP_WITH_SPREAD.accept(VISITOR, IBOR_FIXING_SERIES));
    assertEquals(24, payments.size());
    paymentDate = SWAP_START.toLocalDate().plusMonths(6);
    for (final Map.Entry<LocalDate, MultiCurrencyAmount> entry : payments.entrySet()) {
      assertEquals(paymentDate, entry.getKey());
      assertEquals(1, entry.getValue().size());
      assertEquals(FIXED_INCOME_CURRENCY, entry.getValue().stream().findFirst().get().getCurrency());
      assertEquals(SWAP_NOTIONAL * (FIXING_RATE + IBOR_SPREAD) / 2, entry.getValue().stream().findFirst().get().getAmount(), 1e-15);
      paymentDate = paymentDate.plusMonths(6);
    }
  }

  @Test
  public void testFX() {
    Map<LocalDate, MultiCurrencyAmount> payment = FX_PAY_GBP.accept(VISITOR);
    assertEquals(1, payment.size());
    assertEquals(FX_MATURITY.toLocalDate(), Iterables.getOnlyElement(payment.keySet()));
    CurrencyAmount amount = Iterables.getOnlyElement(payment.values()).stream().findFirst().get();
    assertEquals(CurrencyAmount.of(FX_PAY_CURRENCY, -FX_PAY_AMOUNT), amount);
    payment = FX_PAY_EUR.accept(VISITOR);
    assertEquals(1, payment.size());
    assertEquals(FX_MATURITY.toLocalDate(), Iterables.getOnlyElement(payment.keySet()));
    amount = Iterables.getOnlyElement(payment.values()).stream().findFirst().get();
    assertEquals(CurrencyAmount.of(FX_RECEIVE_CURRENCY, FX_RECEIVE_AMOUNT), amount);
  }

  @Test
  public void testNDF() {
    Map<LocalDate, MultiCurrencyAmount> payment = LONG_NDF.accept(VISITOR);
    assertEquals(0, payment.size());
    payment = SHORT_NDF.accept(VISITOR);
    assertEquals(1, payment.size());
    assertEquals(FX_MATURITY.toLocalDate(), Iterables.getOnlyElement(payment.keySet()));
    final CurrencyAmount amount = Iterables.getOnlyElement(payment.values()).stream().findFirst().get();
    assertEquals(CurrencyAmount.of(FX_RECEIVE_CURRENCY, -FX_PAY_AMOUNT), amount);
  }

}

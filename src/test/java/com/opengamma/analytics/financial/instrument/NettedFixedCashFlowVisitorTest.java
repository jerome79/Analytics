/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument;

import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.FX_PAY_EUR;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.FX_PAY_GBP;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.IBOR_FIXING_SERIES;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.LONG_NDF;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.PAYER_FRA;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.PAYER_SWAP;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.PAYER_SWAP_WITH_SPREAD;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.PAY_CASH;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.PAY_FIXED_COUPON;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.PAY_FIXED_PAYMENT;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.RECEIVER_SWAP;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.RECEIVER_SWAP_WITH_SPREAD;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.RECEIVE_CASH;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.RECEIVE_FIXED_COUPON;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.RECEIVE_FIXED_PAYMENT;
import static com.opengamma.analytics.financial.instrument.InstrumentTestHelper.SHORT_NDF;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.testng.annotations.Test;

import com.opengamma.strata.basics.currency.CurrencyAmount;
import com.opengamma.strata.basics.currency.MultiCurrencyAmount;
import com.opengamma.strata.collect.timeseries.LocalDateDoubleTimeSeries;

/**
 * Test.
 */
@Test
public class NettedFixedCashFlowVisitorTest {
  private static final InstrumentDefinitionVisitor<LocalDateDoubleTimeSeries, Map<LocalDate, MultiCurrencyAmount>> VISITOR = NettedFixedCashFlowVisitor.getVisitor();
  private static final Set<InstrumentDefinition<?>> NO_NETTING_PAY_INSTRUMENTS = new HashSet<>();
  private static final Set<InstrumentDefinition<?>> NO_NETTING_RECEIVE_INSTRUMENTS = new HashSet<>();
  private static final Set<InstrumentDefinition<?>> NO_NETTING_MULTIPLE_CASHFLOWS = new HashSet<>();
  private static final FixedPayCashFlowVisitor PAY_CASH_FLOWS = FixedPayCashFlowVisitor.getInstance();
  private static final FixedReceiveCashFlowVisitor RECEIVE_CASH_FLOWS = FixedReceiveCashFlowVisitor.getInstance();

  static {
    NO_NETTING_PAY_INSTRUMENTS.add(PAY_CASH);
    NO_NETTING_PAY_INSTRUMENTS.add(PAY_FIXED_COUPON);
    NO_NETTING_PAY_INSTRUMENTS.add(SHORT_NDF);
    NO_NETTING_PAY_INSTRUMENTS.add(PAY_FIXED_PAYMENT);
    NO_NETTING_RECEIVE_INSTRUMENTS.add(RECEIVE_CASH);
    NO_NETTING_RECEIVE_INSTRUMENTS.add(RECEIVE_FIXED_COUPON);
    NO_NETTING_RECEIVE_INSTRUMENTS.add(LONG_NDF);
    NO_NETTING_RECEIVE_INSTRUMENTS.add(RECEIVE_FIXED_PAYMENT);
    NO_NETTING_MULTIPLE_CASHFLOWS.add(FX_PAY_EUR);
    NO_NETTING_MULTIPLE_CASHFLOWS.add(FX_PAY_GBP);
    NO_NETTING_MULTIPLE_CASHFLOWS.add(PAYER_FRA);
    NO_NETTING_MULTIPLE_CASHFLOWS.add(RECEIVER_SWAP);
    NO_NETTING_MULTIPLE_CASHFLOWS.add(PAYER_SWAP);
    NO_NETTING_MULTIPLE_CASHFLOWS.add(RECEIVER_SWAP);
    NO_NETTING_MULTIPLE_CASHFLOWS.add(PAYER_SWAP_WITH_SPREAD);
    NO_NETTING_MULTIPLE_CASHFLOWS.add(RECEIVER_SWAP_WITH_SPREAD);
  }

  @Test
  public void testPayCashFlowsNoNetting() {
    for (final InstrumentDefinition<?> definition : NO_NETTING_PAY_INSTRUMENTS) {
      final TreeMap<LocalDate, MultiCurrencyAmount> pay = new TreeMap<>(definition.accept(PAY_CASH_FLOWS));
      final TreeMap<LocalDate, MultiCurrencyAmount> netted = new TreeMap<>(definition.accept(VISITOR));
      assertEquals(pay.size(), netted.size());
      assertEquals(pay.keySet(), netted.keySet());
      final Iterator<Map.Entry<LocalDate, MultiCurrencyAmount>> nettedIterator = netted.entrySet().iterator();
      for (final Map.Entry<LocalDate, MultiCurrencyAmount> payEntry : pay.entrySet()) {
        final Map.Entry<LocalDate, MultiCurrencyAmount> nettedEntry = nettedIterator.next();
        assertEquals(payEntry.getKey(), nettedEntry.getKey());
        assertEquals(payEntry.getValue().size(), nettedEntry.getValue().size());
        final Iterator<CurrencyAmount> nettedMCAIterator = nettedEntry.getValue().getAmounts().iterator();
        for (final CurrencyAmount payCA : payEntry.getValue().getAmounts()) {
          final CurrencyAmount nettedCA = nettedMCAIterator.next();
          assertEquals(payCA.getCurrency(), nettedCA.getCurrency());
          assertEquals(payCA.getAmount(), -nettedCA.getAmount());
        }
      }
    }
  }

  @Test
  public void testReceiveCashFlowsNoNetting() {
    for (final InstrumentDefinition<?> definition : NO_NETTING_RECEIVE_INSTRUMENTS) {
      final TreeMap<LocalDate, MultiCurrencyAmount> pay = new TreeMap<>(definition.accept(RECEIVE_CASH_FLOWS));
      final TreeMap<LocalDate, MultiCurrencyAmount> netted = new TreeMap<>(definition.accept(VISITOR));
      assertEquals(pay.size(), netted.size());
      assertEquals(pay.keySet(), netted.keySet());
      final Iterator<Map.Entry<LocalDate, MultiCurrencyAmount>> nettedIterator = netted.entrySet().iterator();
      for (final Map.Entry<LocalDate, MultiCurrencyAmount> payEntry : pay.entrySet()) {
        final Map.Entry<LocalDate, MultiCurrencyAmount> nettedEntry = nettedIterator.next();
        assertEquals(payEntry.getKey(), nettedEntry.getKey());
        assertEquals(payEntry.getValue().size(), nettedEntry.getValue().size());
        final Iterator<CurrencyAmount> nettedMCAIterator = nettedEntry.getValue().getAmounts().iterator();
        for (final CurrencyAmount payCA : payEntry.getValue().getAmounts()) {
          final CurrencyAmount nettedCA = nettedMCAIterator.next();
          assertEquals(payCA.getCurrency(), nettedCA.getCurrency());
          assertEquals(payCA.getAmount(), nettedCA.getAmount());
        }
      }
    }
  }

  @Test
  public void testMultipleCashFlowsNoNetting() {
    for (final InstrumentDefinition<?> definition : NO_NETTING_MULTIPLE_CASHFLOWS) {
      final TreeMap<LocalDate, MultiCurrencyAmount> pay = new TreeMap<>(definition.accept(PAY_CASH_FLOWS, IBOR_FIXING_SERIES));
      final TreeMap<LocalDate, MultiCurrencyAmount> receive = new TreeMap<>(definition.accept(RECEIVE_CASH_FLOWS, IBOR_FIXING_SERIES));
      final TreeMap<LocalDate, MultiCurrencyAmount> netted = new TreeMap<>(definition.accept(VISITOR, IBOR_FIXING_SERIES));
      final Set<LocalDate> combinedDates = new HashSet<>();
      combinedDates.addAll(pay.keySet());
      combinedDates.addAll(receive.keySet());
      assertEquals(combinedDates.size(), netted.size());
      for (final Map.Entry<LocalDate, MultiCurrencyAmount> entry : netted.entrySet()) {
        final LocalDate date = entry.getKey();
        final MultiCurrencyAmount payAmount = pay.get(date);
        final MultiCurrencyAmount receiveAmount = receive.get(date);
        MultiCurrencyAmount combinedAmountForDate = null;
        if (payAmount != null) {
          combinedAmountForDate = payAmount.multipliedBy(-1);
          if (receiveAmount != null) {
            combinedAmountForDate = combinedAmountForDate.plus(receiveAmount);
          }
        } else {
          if (receiveAmount != null) {
            combinedAmountForDate = receiveAmount;
          }
        }
        assertNotNull(combinedAmountForDate);
        assertEquals(combinedAmountForDate, entry.getValue());
      }
    }
  }

}

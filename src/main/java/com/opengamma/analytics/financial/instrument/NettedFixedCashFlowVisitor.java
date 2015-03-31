/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import com.opengamma.strata.basics.currency.MultiCurrencyAmount;
import com.opengamma.strata.collect.ArgChecker;
import com.opengamma.strata.collect.timeseries.LocalDateDoubleTimeSeries;

/**
 * Returns the netted results of pay and receive cash-flows, where a negative value implies a net liability.
 */
public final class NettedFixedCashFlowVisitor extends InstrumentDefinitionVisitorSameMethodAdapter<LocalDateDoubleTimeSeries, Map<LocalDate, MultiCurrencyAmount>> {
  private static final FixedPayCashFlowVisitor PAY_VISITOR = FixedPayCashFlowVisitor.getInstance();
  private static final FixedReceiveCashFlowVisitor RECEIVE_VISITOR = FixedReceiveCashFlowVisitor.getInstance();
  private static final NettedFixedCashFlowVisitor INSTANCE = new NettedFixedCashFlowVisitor();

  public static InstrumentDefinitionVisitorSameMethodAdapter<LocalDateDoubleTimeSeries, Map<LocalDate, MultiCurrencyAmount>> getVisitor() {
    return INSTANCE;
  }

  /**
   * Returns netted known cash-flows, including any floating cash-flows that have fixed.
   * @param instrument The instrument, not null
   * @return A map containing netted cash-flows.
   */
  @Override
  public Map<LocalDate, MultiCurrencyAmount> visit(final InstrumentDefinition<?> instrument) {
    return visit(instrument, null);
  }

  /**
   * Returns netted known cash-flows, including any floating cash-flows that have fixed.
   * @param instrument The instrument, not null
   * @param indexFixingTimeSeries The fixing time series
   * @return A map containing netted cash-flows.
   */
  @Override
  public Map<LocalDate, MultiCurrencyAmount> visit(final InstrumentDefinition<?> instrument, final LocalDateDoubleTimeSeries indexFixingTimeSeries) {
    ArgChecker.notNull(instrument, "instrument");
    final Map<LocalDate, MultiCurrencyAmount> payCashFlows = instrument.accept(PAY_VISITOR, indexFixingTimeSeries);
    final Map<LocalDate, MultiCurrencyAmount> receiveCashFlows = instrument.accept(RECEIVE_VISITOR, indexFixingTimeSeries);
    return add(payCashFlows, receiveCashFlows);
  }

  private static Map<LocalDate, MultiCurrencyAmount> add(final Map<LocalDate, MultiCurrencyAmount> payCashFlows,
      final Map<LocalDate, MultiCurrencyAmount> receiveCashFlows) {
    final Map<LocalDate, MultiCurrencyAmount> result = new HashMap<>(receiveCashFlows);
    for (final Map.Entry<LocalDate, MultiCurrencyAmount> entry : payCashFlows.entrySet()) {
      final MultiCurrencyAmount mca = entry.getValue().multipliedBy(-1);
      final LocalDate date = entry.getKey();
      if (result.containsKey(date)) {
        result.put(date, result.get(date).plus(mca));
      } else {
        result.put(date, mca);
      }
    }
    return result;
  }
}

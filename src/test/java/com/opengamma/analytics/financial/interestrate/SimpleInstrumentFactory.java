/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import java.time.Period;

import com.opengamma.analytics.convention.daycount.DayCounts;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.fra.derivative.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureTransaction;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.date.BusinessDayConventions;
import com.opengamma.strata.basics.schedule.Frequency;

/**
 * A set of methods to generate simple interest rate derivatives for testing purposes
 */
public abstract class SimpleInstrumentFactory {


  /** Replaces rates */
  protected static final RateReplacingVisitor REPLACE_RATE = RateReplacingVisitor.getInstance();
  private static final Currency DUMMY_CUR = Currency.EUR;
  private static final IborIndex DUMMY_INDEX = new IborIndex(DUMMY_CUR, Period.ofMonths(1), 2, DayCounts.ACT_365F,
      BusinessDayConventions.FOLLOWING, true, "Ibor");

  public static InstrumentDerivative makeCash(final double time, final double rate, final double notional) {
    return new Cash(DUMMY_CUR, 0, time, notional, rate, time);
  }

  public static InstrumentDerivative makeLibor(final double time, final double rate, final double notional) {
    return new Cash(DUMMY_CUR, 0, time, notional, rate, time);
  }

  /**
   * makes a very simple FRA with  payment time, fixing time and fixing period start being identical and an amount tau before fixing period end. The payment and fixing year fractions are
   * Identically equal to tau.
   * @param time The fixing period end (the last relevant date for the FRA)
   * @param paymentFreq for a 3M FRA the payment freq is quarterly
   * @param fundCurveName Name of funding curve
   * @param indexCurveName Name of index curve
   * @param rate The FRA rate
   * @param notional the notional amount
   * @return A FRA
   */
  public static InstrumentDerivative makeFRA(final double time, final Frequency paymentFreq, final double rate, final double notional) {
    final double tau = 1. / paymentFreq.eventsPerYear();
    return new ForwardRateAgreement(DUMMY_CUR, time - tau, tau, notional, DUMMY_INDEX, time - tau, time - tau, time, tau, rate);
  }

  public static InstrumentDerivative makeFuture(final double time, final Frequency paymentFreq) {
    final double tau = 1. / paymentFreq.eventsPerYear();
    InterestRateFutureSecurity sec = new InterestRateFutureSecurity(time, DUMMY_INDEX, time, time + tau, tau, 1.0, tau, "N");
    return new InterestRateFutureTransaction(sec, 0, 1);
  }

}

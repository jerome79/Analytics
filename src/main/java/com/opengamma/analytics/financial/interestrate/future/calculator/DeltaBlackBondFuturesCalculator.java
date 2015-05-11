/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.calculator;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.provider.BondFutureOptionMarginSecurityBlackSmileMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackBondFuturesProviderInterface;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Computes the delta for bond future options.
 */
public final class DeltaBlackBondFuturesCalculator extends InstrumentDerivativeVisitorAdapter<BlackBondFuturesProviderInterface, Double> {

  /**
   * The singleton.
   */
  private static final DeltaBlackBondFuturesCalculator INSTANCE = new DeltaBlackBondFuturesCalculator();

  /**
   * Returns the calculator instance.
   * @return the calculator.
   */
  public static DeltaBlackBondFuturesCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Singleton constructor.
   */
  private DeltaBlackBondFuturesCalculator() {
  }

  /** The method used to compute the future option price */
  private static final BondFutureOptionMarginSecurityBlackSmileMethod METHOD_FUTURE_OPTION = BondFutureOptionMarginSecurityBlackSmileMethod
      .getInstance();

  @Override
  public Double visitBondFuturesOptionMarginSecurity(BondFuturesOptionMarginSecurity option, BlackBondFuturesProviderInterface data) {
    ArgChecker.notNull(option, "security");
    ArgChecker.notNull(data, "data");
    return METHOD_FUTURE_OPTION.delta(option, data);
  }

  @Override
  public Double visitBondFuturesOptionMarginTransaction(BondFuturesOptionMarginTransaction option, BlackBondFuturesProviderInterface data) {
    ArgChecker.notNull(option, "security");
    ArgChecker.notNull(data, "data");
    return METHOD_FUTURE_OPTION.delta(option.getUnderlyingSecurity(), data);
  }
}

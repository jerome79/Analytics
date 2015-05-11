/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.calculator;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.provider.BondFutureOptionMarginSecurityBlackSmileMethod;
import com.opengamma.analytics.financial.interestrate.future.provider.FuturesSecurityIssuerMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackBondFuturesProviderInterface;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Computes the price for different types of futures. Calculator using a multi-curve and issuer provider.
 */
public final class FuturesPriceBlackBondFuturesCalculator
    extends InstrumentDerivativeVisitorAdapter<BlackBondFuturesProviderInterface, Double> {

  /** The default instance of the calculator. */
  private static final FuturesPriceBlackBondFuturesCalculator DEFAULT = new FuturesPriceBlackBondFuturesCalculator();

  /** The method used to compute futures option */
  private final BondFutureOptionMarginSecurityBlackSmileMethod _methodFuturesOption;

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static FuturesPriceBlackBondFuturesCalculator getInstance() {
    return DEFAULT;
  }

  /**
   * Default constructor.
   */
  private FuturesPriceBlackBondFuturesCalculator() {
    _methodFuturesOption = BondFutureOptionMarginSecurityBlackSmileMethod.getInstance();
  }

  /**
   * Constructor from a particular bond futures method. The method is used to compute the price and price curve
   * sensitivity of the underlying futures.
   * @param methodFutures The method used to compute futures option.
   */
  public FuturesPriceBlackBondFuturesCalculator(FuturesSecurityIssuerMethod methodFutures) {
    _methodFuturesOption = new BondFutureOptionMarginSecurityBlackSmileMethod(methodFutures);
  }

  //     -----     Futures options    -----

  @Override
  public Double visitBondFuturesOptionMarginSecurity(final BondFuturesOptionMarginSecurity security,
      final BlackBondFuturesProviderInterface black) {
    ArgChecker.notNull(security, "security");
    ArgChecker.notNull(black, "black");
    return _methodFuturesOption.price(security, black);
  }

  @Override
  public Double visitBondFuturesOptionMarginTransaction(BondFuturesOptionMarginTransaction option,
      BlackBondFuturesProviderInterface data) {
    return visitBondFuturesOptionMarginSecurity(option.getUnderlyingSecurity(), data);
  }
}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.calculator;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.provider.BondFuturesSecurityDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.future.provider.FuturesSecurityIssuerMethod;
import com.opengamma.analytics.financial.interestrate.sensitivity.PresentValueBlackBondFuturesCubeSensitivity;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackBondFuturesProviderInterface;
import com.opengamma.analytics.util.amount.CubeValue;
import com.opengamma.strata.collect.ArgChecker;
import com.opengamma.strata.collect.tuple.Triple;

/**
 * Computes the price for different types of futures. Calculator using a multi-curve and issuer provider.
 */
public final class FuturesPriceBlackSensitivityBlackBondFuturesCalculator
    extends InstrumentDerivativeVisitorAdapter<BlackBondFuturesProviderInterface, PresentValueBlackBondFuturesCubeSensitivity> {

  /** The default instance of the calculator. */
  private static final FuturesPriceBlackSensitivityBlackBondFuturesCalculator DEFAULT =
      new FuturesPriceBlackSensitivityBlackBondFuturesCalculator();
  /** The method used to compute the future price. */
  private final FuturesSecurityIssuerMethod _methodFutures;

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static FuturesPriceBlackSensitivityBlackBondFuturesCalculator getInstance() {
    return DEFAULT;
  }

  /**
   * Default constructor.
   */
  private FuturesPriceBlackSensitivityBlackBondFuturesCalculator() {
    _methodFutures = BondFuturesSecurityDiscountingMethod.getInstance();
  }

  /**
   * Constructor from a particular bond futures method. The method is used to compute the price and price curve
   * sensitivity of the underlying futures.
   * @param methodFutures The method used to compute futures option.
   */
  public FuturesPriceBlackSensitivityBlackBondFuturesCalculator(FuturesSecurityIssuerMethod methodFutures) {
    _methodFutures = methodFutures;
  }

  /** The Black function used in the pricing. */
  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();

  //     -----     Futures options    -----

  @Override
  public PresentValueBlackBondFuturesCubeSensitivity visitBondFuturesOptionMarginSecurity(final BondFuturesOptionMarginSecurity security,
      final BlackBondFuturesProviderInterface black) {
    ArgChecker.notNull(security, "security");
    ArgChecker.notNull(black, "Black  data");
    final double priceFutures = _methodFutures.price(security.getUnderlyingFuture(), black.getIssuerProvider());
    // Forward sweep
    final double strike = security.getStrike();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, security.getExpirationTime(), security.isCall());
    final double delay = security.getUnderlyingFuture().getNoticeLastTime() - security.getExpirationTime();
    final double volatility = black.getVolatility(security.getExpirationTime(), delay, strike, priceFutures);
    final BlackFunctionData dataBlack = new BlackFunctionData(priceFutures, 1.0, volatility);
    final double[] priceAdjoint = BLACK_FUNCTION.getPriceAdjoint(option, dataBlack);
    // Backward sweep
    final double priceBar = 1.0;
    final double volBar = priceAdjoint[2] * priceBar;
    final CubeValue blackSensi = new CubeValue();
    blackSensi.add(Triple.of(security.getExpirationTime(), delay, strike), volBar);
    return new PresentValueBlackBondFuturesCubeSensitivity(blackSensi, security.getCurrency(), black.getLegalEntity());
  }

}

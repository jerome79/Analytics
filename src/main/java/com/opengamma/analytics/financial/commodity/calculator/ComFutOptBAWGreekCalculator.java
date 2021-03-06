/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.calculator;

import com.opengamma.analytics.financial.commodity.CommodityFutureOptionSameMethodVisitorAdapter;
import com.opengamma.analytics.financial.commodity.derivative.CommodityFutureOption;
import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.greeks.Greek;
import com.opengamma.analytics.financial.greeks.GreekResultCollection;
import com.opengamma.analytics.financial.model.option.pricing.analytic.BaroneAdesiWhaleyModel;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Calculates the greeks of a commodity future option using the Barone-Adesi Whaley model {@link BaroneAdesiWhaleyModel}.
 * <p>
 * The greeks returned are delta, dual-delta, rho, carry rho, theta and vega.
 */
public final class ComFutOptBAWGreekCalculator extends CommodityFutureOptionSameMethodVisitorAdapter<StaticReplicationDataBundle, GreekResultCollection> {
  /** A static instance of this calculator */
  private static final ComFutOptBAWGreekCalculator INSTANCE = new ComFutOptBAWGreekCalculator();
  /** The pricing model */
  private static final BaroneAdesiWhaleyModel MODEL = new BaroneAdesiWhaleyModel();

  /**
   * @return A static instance of this class
   */
  public static ComFutOptBAWGreekCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private ComFutOptBAWGreekCalculator() {
  }

  @Override
  public GreekResultCollection visit(final CommodityFutureOption<?> option, final StaticReplicationDataBundle data) {
    ArgChecker.notNull(option, "option");
    ArgChecker.notNull(data, "data");
    final double s = data.getForwardCurve().getSpot();
    final double k = option.getStrike();
    final double t = option.getExpiry();
    final double r = data.getDiscountCurve().getInterestRate(t);
    final double b = r; //TODO
    final double volatility = data.getVolatilitySurface().getVolatility(t, k);
    final boolean isCall = option.isCall();
    final double[] greeks = MODEL.getPriceAdjoint(s, k, r, b, t, volatility, isCall);
    final GreekResultCollection result = new GreekResultCollection();
    result.put(Greek.DELTA, greeks[1]);
    result.put(Greek.DUAL_DELTA, greeks[2]);
    result.put(Greek.RHO, greeks[3]);
    result.put(Greek.CARRY_RHO, greeks[4]);
    result.put(Greek.THETA, greeks[5]);
    result.put(Greek.VEGA, greeks[6]);

    final double[] pdg = MODEL.getPriceDeltaGamma(s, k, r, b, volatility, s, isCall);
    result.put(Greek.GAMMA, pdg[2]);
    return result;
  }

}

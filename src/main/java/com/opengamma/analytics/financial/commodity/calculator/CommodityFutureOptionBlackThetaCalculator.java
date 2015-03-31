/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.calculator;

import com.opengamma.analytics.financial.commodity.derivative.AgricultureFutureOption;
import com.opengamma.analytics.financial.commodity.derivative.EnergyFutureOption;
import com.opengamma.analytics.financial.commodity.derivative.MetalFutureOption;
import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Calculates the theta of commodity future options using the Black method.
 */
public final class CommodityFutureOptionBlackThetaCalculator extends InstrumentDerivativeVisitorAdapter<StaticReplicationDataBundle, Double> {
  /** A static instance of this calculator */
  private static final CommodityFutureOptionBlackThetaCalculator s_instance = new CommodityFutureOptionBlackThetaCalculator();
  /** The Black pricer */
  private static final CommodityFutureOptionBlackMethod PRICER = CommodityFutureOptionBlackMethod.getInstance();

  /**
   * @return The static instance of this calculator
   */
  public static CommodityFutureOptionBlackThetaCalculator getInstance() {
    return s_instance;
  }

  /**
   * Private constructor.
   */
  private CommodityFutureOptionBlackThetaCalculator() {
  }

  @Override
  public Double visitAgricultureFutureOption(final AgricultureFutureOption derivative, final StaticReplicationDataBundle data) {
    ArgChecker.notNull(derivative, "derivative");
    ArgChecker.notNull(data, "data");
    return PRICER.theta(derivative, data);
  }

  @Override
  public Double visitEnergyFutureOption(final EnergyFutureOption derivative, final StaticReplicationDataBundle data) {
    ArgChecker.notNull(derivative, "derivative");
    ArgChecker.notNull(data, "data");
    return PRICER.theta(derivative, data);
  }

  @Override
  public Double visitMetalFutureOption(final MetalFutureOption derivative, final StaticReplicationDataBundle data) {
    ArgChecker.notNull(derivative, "derivative");
    ArgChecker.notNull(data, "data");
    return PRICER.theta(derivative, data);
  }

}

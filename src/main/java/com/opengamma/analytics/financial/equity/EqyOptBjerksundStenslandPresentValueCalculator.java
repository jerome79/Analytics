/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity;

import com.opengamma.analytics.financial.ExerciseDecisionType;
import com.opengamma.analytics.financial.equity.option.EquityIndexFutureOption;
import com.opengamma.analytics.financial.equity.option.EquityIndexOption;
import com.opengamma.analytics.financial.equity.option.EquityOption;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.option.pricing.analytic.BjerksundStenslandModel;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Calculates the present value of equity options using the Black method.
 */
public final class EqyOptBjerksundStenslandPresentValueCalculator extends InstrumentDerivativeVisitorAdapter<StaticReplicationDataBundle, Double> {
  /** A static instance */
  private static final EqyOptBjerksundStenslandPresentValueCalculator INSTANCE = new EqyOptBjerksundStenslandPresentValueCalculator();
  /** The present value calculator */
  private static final BjerksundStenslandModel MODEL = new BjerksundStenslandModel();

  /**
   * Gets the static instance
   * @return The static instance
   */
  public static EqyOptBjerksundStenslandPresentValueCalculator getInstance() {
    return INSTANCE;
  }

  private EqyOptBjerksundStenslandPresentValueCalculator() {
  }

  @Override
  public Double visitEquityIndexOption(EquityIndexOption option, StaticReplicationDataBundle data) {
    ArgChecker.notNull(option, "option");
    ArgChecker.notNull(data, "data");
    ArgChecker.isTrue(option.getExerciseType() == ExerciseDecisionType.AMERICAN, "option must be American");
    double strike = option.getStrike();
    double time = option.getTimeToExpiry();
    boolean isCall = option.isCall();
    return option.getUnitAmount() * computePrice(strike, time, isCall, data);
  }

  @Override
  public Double visitEquityOption(EquityOption option, StaticReplicationDataBundle data) {
    ArgChecker.notNull(option, "option");
    ArgChecker.notNull(data, "data");
    ArgChecker.isTrue(option.getExerciseType() == ExerciseDecisionType.AMERICAN, "option must be American");
    double strike = option.getStrike();
    double time = option.getTimeToExpiry();
    boolean isCall = option.isCall();
    return option.getUnitAmount() * computePrice(strike, time, isCall, data);
  }

  @Override
  public Double visitEquityIndexFutureOption(final EquityIndexFutureOption option, final StaticReplicationDataBundle data) {
    ArgChecker.notNull(option, "option");
    ArgChecker.notNull(data, "data");
    ForwardCurve forwardCurve = data.getForwardCurve();
    final double spot = forwardCurve.getSpot();
    final double strike = option.getStrike();
    final double time = option.getExpiry();
    final double sigma = data.getVolatilitySurface().getVolatility(time, strike);
    final boolean isCall = option.isCall();
    final double interestRate = data.getDiscountCurve().getInterestRate(time);
    final double costOfCarry = time > 0 ? Math.log(forwardCurve.getForward(time) / spot) / time : interestRate;
    return option.getPointValue() * MODEL.price(spot, strike, interestRate, costOfCarry, time, sigma, isCall);
  }

  private double computePrice(double strike, double time, boolean isCall, StaticReplicationDataBundle data) {
    ForwardCurve forwardCurve = data.getForwardCurve();
    double spot = forwardCurve.getSpot();
    double sigma = data.getVolatilitySurface().getVolatility(time, strike);
    double interestRate = data.getDiscountCurve().getInterestRate(time);
    double costOfCarry = time > 0 ? Math.log(forwardCurve.getForward(time) / spot) / time : interestRate;
    return MODEL.price(spot, strike, interestRate, costOfCarry, time, sigma, isCall);
  }
}

/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.future;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.simpleinstruments.pricing.SimpleFutureDataBundle;

/**
 * 
 */
public abstract class CostOfCarryFuturesCalculator extends InstrumentDerivativeVisitorAdapter<SimpleFutureDataBundle, Double> {

  /* package */CostOfCarryFuturesCalculator() {
  }

  abstract double getResult(SimpleFutureDataBundle dataBundle, double strike, double unitAmount, double t);

  /**
   * Calculates the present value
   */
  public static final class PresentValueCalculator extends CostOfCarryFuturesCalculator {
    private static final PresentValueCalculator INSTANCE = new PresentValueCalculator();

    public static PresentValueCalculator getInstance() {
      return INSTANCE;
    }

    private PresentValueCalculator() {
    }

    @Override
    double getResult(final SimpleFutureDataBundle dataBundle, final double strike, final double unitAmount, final double t) {
      final double fwdPrice = ForwardPriceCalculator.getInstance().getResult(dataBundle, strike, unitAmount, t);
      return (fwdPrice - strike) * unitAmount;
    }

  }

  /**
   * Calculates the spot delta
   */
  public static final class SpotDeltaCalculator extends CostOfCarryFuturesCalculator {
    private static final SpotDeltaCalculator INSTANCE = new SpotDeltaCalculator();

    public static SpotDeltaCalculator getInstance() {
      return INSTANCE;
    }

    private SpotDeltaCalculator() {
    }

    @Override
    double getResult(final SimpleFutureDataBundle dataBundle, final double strike, final double unitAmount, final double t) {
      final double discountRate = dataBundle.getFundingCurve().getInterestRate(t);
      final double costOfCarry = Math.exp(t * (discountRate - dataBundle.getDividendYield()));
      return costOfCarry * unitAmount;
    }
  }

  /**
   * Calculates the rates delta
   */
  public static final class RatesDeltaCalculator extends CostOfCarryFuturesCalculator {
    private static final RatesDeltaCalculator INSTANCE = new RatesDeltaCalculator();

    public static RatesDeltaCalculator getInstance() {
      return INSTANCE;
    }

    private RatesDeltaCalculator() {
    }

    @Override
    double getResult(final SimpleFutureDataBundle dataBundle, final double strike, final double unitAmount, final double t) {
      final double discountRate = dataBundle.getFundingCurve().getInterestRate(t);
      final double costOfCarry = Math.exp(t * (discountRate - dataBundle.getDividendYield()));
      final double fwdPrice = dataBundle.getSpotValue() * costOfCarry;
      return t * fwdPrice * unitAmount;
    }
  }

  /**
   * Calculates the pv01
   */
  public static final class PV01Calculator extends CostOfCarryFuturesCalculator {
    private static final PV01Calculator INSTANCE = new PV01Calculator();

    public static PV01Calculator getInstance() {
      return INSTANCE;
    }

    private PV01Calculator() {
    }

    @Override
    double getResult(final SimpleFutureDataBundle dataBundle, final double strike, final double unitAmount, final double t) {
      return RatesDeltaCalculator.getInstance().getResult(dataBundle, strike, unitAmount, t) / 10000;
    }
  }

  /**
   * Gets the spot price
   */
  public static final class SpotPriceCalculator extends CostOfCarryFuturesCalculator {
    private static final SpotPriceCalculator INSTANCE = new SpotPriceCalculator();

    public static SpotPriceCalculator getInstance() {
      return INSTANCE;
    }

    private SpotPriceCalculator() {
    }

    @Override
    double getResult(final SimpleFutureDataBundle dataBundle, final double strike, final double unitAmount, final double t) {
      return dataBundle.getSpotValue();
    }

  }

  /**
   * Gets the forward price
   */
  public static final class ForwardPriceCalculator extends CostOfCarryFuturesCalculator {
    private static final ForwardPriceCalculator INSTANCE = new ForwardPriceCalculator();

    public static ForwardPriceCalculator getInstance() {
      return INSTANCE;
    }

    private ForwardPriceCalculator() {
    }

    @Override
    double getResult(final SimpleFutureDataBundle dataBundle, final double strike, final double unitAmount, final double t) {
      final double discountRate = dataBundle.getFundingCurve().getInterestRate(t);
      final double costOfCarry = Math.exp(t * (discountRate - dataBundle.getDividendYield()));
      return dataBundle.getSpotValue() * costOfCarry;
    }

  }
}

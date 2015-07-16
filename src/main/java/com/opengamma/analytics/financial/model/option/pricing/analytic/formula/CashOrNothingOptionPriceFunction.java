/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic.formula;

import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Computes (undiscounted) price and Greeks of a cash-or-nothing option in the lognormal model.
 */
public class CashOrNothingOptionPriceFunction {

  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);
  private static final double SMALL_DEFAULT = 1.0e-15;

  private final double _small;

  /**
   * Constructor with the default small parameter.
   */
  public CashOrNothingOptionPriceFunction() {
    this(SMALL_DEFAULT);
  }

  /**
   * Constructor with a specified small parameter. 
   * 
   * @param small  the small parameter
   */
  public CashOrNothingOptionPriceFunction(double small) {
    _small = small;
  }

  //-------------------------------------------------------------------------
  /**
   * Compute price of cash-or-nothing option.
   * 
   * @param forward  the forward
   * @param strike  the strike
   * @param timeToExpiry  the time to expiry
   * @param lognormalVol  the log-normal volatility
   * @param isCall  true for calls, false for puts
   * @return the option price
   */
  public double price(double forward, double strike, double timeToExpiry, double lognormalVol, boolean isCall) {
    ArgChecker.isTrue(forward > 0.0, "negative spot; have {}", forward);
    ArgChecker.isTrue(strike > 0.0, "negative strike; have {}", strike);
    ArgChecker.isTrue(lognormalVol >= 0.0, "negative lognormalVol; have {}", lognormalVol);
    ArgChecker.isTrue(timeToExpiry >= 0.0, "negative timeToExpiry; have {}", timeToExpiry);
    double sigRootT = lognormalVol * Math.sqrt(timeToExpiry);
    double logMoneyness = Math.log(forward / strike);
    if (sigRootT < _small && Math.abs(logMoneyness) < _small) {
      return 0d;
    }
    double d2 = logMoneyness / sigRootT - 0.5 * sigRootT;
    double sign = isCall ? 1d : -1d;
    return NORMAL.getCDF(sign * d2);
  }

  /**
   * Compute forward delta of cash-or-nothing option.
   * 
   * @param forward  the forward
   * @param strike  the strike
   * @param timeToExpiry  the time to expiry
   * @param lognormalVol  the log-normal volatility
   * @param isCall  true for calls, false for puts
   * @return the option delta
   */
  public double delta(double forward, double strike, double timeToExpiry, double lognormalVol, boolean isCall) {
    ArgChecker.isTrue(forward > 0.0, "negative spot; have {}", forward);
    ArgChecker.isTrue(strike > 0.0, "negative strike; have {}", strike);
    ArgChecker.isTrue(lognormalVol >= 0.0, "negative lognormalVol; have {}", lognormalVol);
    ArgChecker.isTrue(timeToExpiry >= 0.0, "negative timeToExpiry; have {}", timeToExpiry);
    double sigRootT = lognormalVol * Math.sqrt(timeToExpiry);
    if (sigRootT < _small) {
      return 0d;
    }
    double logMoneyness = Math.log(forward / strike);
    double d2 = logMoneyness / sigRootT - 0.5 * sigRootT;
    double sign = isCall ? 1d : -1d;
    return sign * NORMAL.getPDF(d2) / forward / sigRootT;
  }

  /**
   * Compute forward gamma of cash-or-nothing option.
   * 
   * @param forward  the forward
   * @param strike  the strike
   * @param timeToExpiry  the time to expiry
   * @param lognormalVol  the log-normal volatility
   * @param isCall  true for calls, false for puts
   * @return the option gamma
   */
  public double gamma(double forward, double strike, double timeToExpiry, double lognormalVol, boolean isCall) {
    ArgChecker.isTrue(forward > 0.0, "negative spot; have {}", forward);
    ArgChecker.isTrue(strike > 0.0, "negative strike; have {}", strike);
    ArgChecker.isTrue(lognormalVol >= 0.0, "negative lognormalVol; have {}", lognormalVol);
    ArgChecker.isTrue(timeToExpiry >= 0.0, "negative timeToExpiry; have {}", timeToExpiry);
    double sigRootT = lognormalVol * Math.sqrt(timeToExpiry);
    if (sigRootT < _small) {
      return 0d;
    }
    double logMoneyness = Math.log(forward / strike);
    double d2 = logMoneyness / sigRootT - 0.5 * sigRootT;
    double d1 = logMoneyness / sigRootT + 0.5 * sigRootT;
    double sign = isCall ? -1d : 1d;
    return sign * d1 * NORMAL.getPDF(d2) / Math.pow(forward * lognormalVol, 2) / timeToExpiry;
  }

  /**
   * Compute (forward) theta of cash-or-nothing option.
   * 
   * @param forward  the forward
   * @param strike  the strike
   * @param timeToExpiry  the time to expiry
   * @param lognormalVol  the log-normal volatility
   * @param isCall  true for calls, false for puts
   * @return the option theta
   */
  public double theta(double forward, double strike, double timeToExpiry, double lognormalVol, boolean isCall) {
    ArgChecker.isTrue(forward > 0.0, "negative spot; have {}", forward);
    ArgChecker.isTrue(strike > 0.0, "negative strike; have {}", strike);
    ArgChecker.isTrue(lognormalVol >= 0.0, "negative lognormalVol; have {}", lognormalVol);
    ArgChecker.isTrue(timeToExpiry >= 0.0, "negative timeToExpiry; have {}", timeToExpiry);
    double sigRootT = lognormalVol * Math.sqrt(timeToExpiry);
    if (sigRootT < _small) {
      return 0d;
    }
    double logMoneyness = Math.log(forward / strike);
    double d2 = logMoneyness / sigRootT - 0.5 * sigRootT;
    double sign = isCall ? 1d : -1d;
    double div = 0.5 *
        (logMoneyness / sigRootT / timeToExpiry + 0.5 * lognormalVol / Math.sqrt(timeToExpiry));
    return sign * NORMAL.getPDF(d2) * div;
  }

  /**
   * Compute vega of cash-or-nothing option.
   * 
   * @param forward  the forward
   * @param strike  the strike
   * @param timeToExpiry  the time to expiry
   * @param lognormalVol  the log-normal volatility
   * @param isCall  true for calls, false for puts
   * @return the option vega
   */
  public double vega(double forward, double strike, double timeToExpiry, double lognormalVol, boolean isCall) {
    ArgChecker.isTrue(forward > 0.0, "negative spot; have {}", forward);
    ArgChecker.isTrue(strike > 0.0, "negative strike; have {}", strike);
    ArgChecker.isTrue(lognormalVol >= 0.0, "negative lognormalVol; have {}", lognormalVol);
    ArgChecker.isTrue(timeToExpiry >= 0.0, "negative timeToExpiry; have {}", timeToExpiry);
    double sigRootT = lognormalVol * Math.sqrt(timeToExpiry);
    if (sigRootT < _small) {
      return 0d;
    }
    double logMoneyness = Math.log(forward / strike);
    double d2 = logMoneyness / sigRootT - 0.5 * sigRootT;
    double sign = isCall ? 1d : -1d;
    double div = -(logMoneyness / sigRootT / lognormalVol + 0.5 * Math.sqrt(timeToExpiry));
    return sign * NORMAL.getPDF(d2) * div;
  }

}

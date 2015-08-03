/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic.formula;

import com.google.common.math.DoubleMath;
import com.opengamma.analytics.financial.model.option.definition.Barrier;
import com.opengamma.analytics.financial.model.option.definition.Barrier.BarrierType;
import com.opengamma.analytics.financial.model.option.definition.Barrier.KnockType;
import com.opengamma.analytics.financial.model.volatility.BlackScholesFormulaRepository;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.strata.collect.ArgChecker;

/**
 * The price function to compute the price of barrier option in the Black world.
 * Reference: E. G. Haug (2007) The complete guide to Option Pricing Formulas. Mc Graw Hill. Section 4.17.1.
 */
public final class BlackBarrierPriceFunction {

  private static final double SMALL_PARAMETER = 1.0e-16;

  /**
   * The normal distribution implementation used in the pricing.
   */
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);
  /** Static instance */
  private static final BlackBarrierPriceFunction INSTANCE = new BlackBarrierPriceFunction();

  /**
   * Gets the static instance
   * @return The instance
   */
  public static BlackBarrierPriceFunction getInstance() {
    return INSTANCE;
  }

  private BlackBarrierPriceFunction() {
  }

  /**
   * Computes the price of a barrier option in the Black world.
   * @param option The underlying European vanilla option.
   * @param barrier The barrier.
   * @param rebate The rebate. This is paid <b>immediately</b> if the knock-out barrier is hit and at expiry if the knock-in barrier is not hit
   * @param spot The spot price.
   * @param costOfCarry The cost of carry (i.e. the forward = spot*exp(costOfCarry*T) )
   * @param rate The interest rate.
   * @param sigma The Black volatility.
   * @return The price.
   */
  public double getPrice(final EuropeanVanillaOption option, final Barrier barrier, final double rebate, final double spot, final double costOfCarry, final double rate, final double sigma) {
    ArgChecker.notNull(option, "option");
    ArgChecker.notNull(barrier, "barrier");
    final boolean isKnockIn = (barrier.getKnockType() == KnockType.IN);
    final boolean isDown = (barrier.getBarrierType() == BarrierType.DOWN);
    final double h = barrier.getBarrierLevel();
    ArgChecker.isTrue(!(barrier.getBarrierType() == BarrierType.DOWN && spot < barrier.getBarrierLevel()), "The Data is not consistent with an alive barrier (DOWN and spot<barrier).");
    ArgChecker.isTrue(!(barrier.getBarrierType() == BarrierType.UP && spot > barrier.getBarrierLevel()), "The Data is not consistent with an alive barrier (UP and spot>barrier).");
    final boolean isCall = option.isCall();
    final double t = option.getTimeToExpiry();
    final double strike = option.getStrike();
    final int phi = isCall ? 1 : -1;
    final double eta = isDown ? 1 : -1;
    final double df1 = Math.exp(t * (costOfCarry - rate));
    final double df2 = Math.exp(-rate * t);
    if (DoubleMath.fuzzyEquals(sigma, 0d, SMALL_PARAMETER) || DoubleMath.fuzzyEquals(t, 0d, SMALL_PARAMETER)) {
      double forward = spot * Math.exp(costOfCarry * t);
      if (isKnockIn) {
        double price = 0d;
        if ((forward - strike) * phi > 0d && (forward - h) * eta < 0d) {
          price = BlackScholesFormulaRepository.price(spot, strike, t, sigma, rate, costOfCarry, isCall);
        }
        if ((forward - h) * eta < 0d && (h * Math.exp(costOfCarry * t) - spot) * eta < 0d) {
          return price;
        }
        price += df2 * rebate;
        return price;
      }
      if ((forward - h) * eta > 0d) {
        return BlackScholesFormulaRepository.price(spot, strike, t, sigma, rate, costOfCarry, isCall);
      }
      return rebate;
    }
    final double sigmaSq = sigma * sigma;
    final double sigmaT = sigma * Math.sqrt(t);
    final double mu = (costOfCarry - 0.5 * sigmaSq) / sigmaSq;
    final double lambda = Math.sqrt(mu * mu + 2 * rate / sigmaSq);
    final double m1 = sigmaT * (1 + mu);
    final double x1 = Math.log(spot / strike) / sigmaT + m1;
    final double x2 = Math.log(spot / h) / sigmaT + m1;
    final double y1 = Math.log(h * h / spot / strike) / sigmaT + m1;
    final double y2 = Math.log(h / spot) / sigmaT + m1;
    final double z = Math.log(h / spot) / sigmaT + lambda * sigmaT;
    final double xA = getA(spot, strike, df1, df2, x1, sigmaT, phi);
    final double xB = getA(spot, strike, df1, df2, x2, sigmaT, phi);
    final double xC = getC(spot, strike, df1, df2, y1, sigmaT, h, mu, phi, eta);
    final double xD = getC(spot, strike, df1, df2, y2, sigmaT, h, mu, phi, eta);
    final double xE = isKnockIn ? getE(spot, rebate, df2, x2, y2, sigmaT, h, mu, eta) : getF(spot, rebate, z, sigmaT, h, mu, lambda, eta);
    if (isKnockIn) {
      if (isDown) {
        if (isCall) {
          return strike > h ? xC + xE : xA - xB + xD + xE;
        }
        return strike > h ? xB - xC + xD + xE : xA + xE;
      }
      if (isCall) {
        return strike > h ? xA + xE : xB - xC + xD + xE;
      }
      return strike > h ? xA - xB + xD + xE : xC + xE;
    }
    // KnockOut
    if (isDown) {
      if (isCall) {
        return strike > h ? xA - xC + xE : xB - xD + xE;
      }
      return strike > h ? xA - xB + xC - xD + xE : xE;
    }
    if (isCall) {
      return strike > h ? xE : xA - xB + xC - xD + xE;
    }
    return strike > h ? xB - xD + xE : xA - xC + xE;
  }

  /**
   * Computes the price of a barrier option in the Black world.
   * @param option  the underlying European vanilla option.
   * @param barrier  the barrier.
   * @param rebate  the rebate.
   * @param spot  the spot price.
   * @param costOfCarry  the cost of carry.
   * @param rate  the interest rate.
   * @param sigma  the Black volatility.
   * @param derivatives Array used to return the derivatives. Will be changed during the call. 
   * The derivatives are [0] spot, [1] strike, [2] rate, [3] cost-of-carry, [4] volatility, [5] time, [6] spot twice.
   * @return The price.
   */
  public double getPriceAdjoint(EuropeanVanillaOption option, Barrier barrier, double rebate, double spot,
      double costOfCarry, double rate, double sigma, double[] derivatives) {
    ArgChecker.notNull(option, "option");
    ArgChecker.notNull(barrier, "barrier");
    ArgChecker.notNull(derivatives, "derivatives");
    ArgChecker.isTrue(derivatives.length == 7, "the length of derivatives should be 7");
    for (int loopder = 0; loopder < 7; loopder++) { // To clean the array.
      derivatives[loopder] = 0d;
    }
    boolean isKnockIn = (barrier.getKnockType() == KnockType.IN);
    boolean isDown = (barrier.getBarrierType() == BarrierType.DOWN);
    double h = barrier.getBarrierLevel();
    ArgChecker.isTrue(!(barrier.getBarrierType() == BarrierType.DOWN && spot < barrier.getBarrierLevel()),
        "The Data is not consistent with an alive barrier (DOWN and spot<barrier).");
    ArgChecker.isTrue(!(barrier.getBarrierType() == BarrierType.UP && spot > barrier.getBarrierLevel()),
        "The Data is not consistent with an alive barrier (UP and spot>barrier).");
    boolean isCall = option.isCall();
    double t = option.getTimeToExpiry();
    double strike = option.getStrike();
    int phi = isCall ? 1 : -1;
    double eta = isDown ? 1 : -1;
    double df1 = Math.exp(t * (costOfCarry - rate));
    double df2 = Math.exp(-rate * t);
    if (DoubleMath.fuzzyEquals(sigma, 0d, SMALL_PARAMETER) || DoubleMath.fuzzyEquals(t, 0d, SMALL_PARAMETER)) {
      double priceBar = 1.0;
      double forward = spot * Math.exp(costOfCarry * t);
      if (isKnockIn) {
        double price = 0d;
        if ((forward - strike) * phi > 0d && (forward - h) * eta < 0d) {
          price = BlackScholesFormulaRepository.price(spot, strike, t, sigma, rate, costOfCarry, isCall);
          double cRho = BlackScholesFormulaRepository.carryRho(spot, strike, t, sigma, rate, costOfCarry, isCall);
          derivatives[0] = BlackScholesFormulaRepository.delta(spot, strike, t, sigma, rate, costOfCarry, isCall);
          derivatives[1] = BlackScholesFormulaRepository.dualDelta(spot, strike, t, sigma, rate, costOfCarry, isCall);
          derivatives[2] = BlackScholesFormulaRepository.rho(spot, strike, t, sigma, rate, costOfCarry, isCall) - cRho;
          derivatives[3] = cRho;
          derivatives[4] = BlackScholesFormulaRepository.vega(spot, strike, t, sigma, rate, costOfCarry);
          derivatives[5] = -BlackScholesFormulaRepository.theta(spot, strike, t, sigma, rate, costOfCarry, isCall);
          derivatives[6] = BlackScholesFormulaRepository.gamma(spot, strike, t, sigma, rate, costOfCarry);
        }
        if ((forward - h) * eta < 0d && (h * Math.exp(costOfCarry * t) - spot) * eta < 0d) {
          return price;
        }
        double df1Bar = rebate * priceBar;
        price += df2 * rebate;
        derivatives[2] += -t * df2 * df1Bar;
        derivatives[5] += -rate * df2 * df1Bar;
        return price;
      }
      if ((forward - h) * eta > 0d) {
        double cRho = BlackScholesFormulaRepository.carryRho(spot, strike, t, sigma, rate, costOfCarry, isCall);
        derivatives[0] = BlackScholesFormulaRepository.delta(spot, strike, t, sigma, rate, costOfCarry, isCall);
        derivatives[1] = BlackScholesFormulaRepository.dualDelta(spot, strike, t, sigma, rate, costOfCarry, isCall);
        derivatives[2] = BlackScholesFormulaRepository.rho(spot, strike, t, sigma, rate, costOfCarry, isCall) - cRho;
        derivatives[3] = cRho;
        derivatives[4] = BlackScholesFormulaRepository.vega(spot, strike, t, sigma, rate, costOfCarry);
        derivatives[5] = -BlackScholesFormulaRepository.theta(spot, strike, t, sigma, rate, costOfCarry, isCall);
        derivatives[6] = BlackScholesFormulaRepository.gamma(spot, strike, t, sigma, rate, costOfCarry);
        return BlackScholesFormulaRepository.price(spot, strike, t, sigma, rate, costOfCarry, isCall);
      }
      double df1Bar = rebate * priceBar;
      derivatives[2] += -Math.log(h / spot) / costOfCarry * df1Bar;
      derivatives[4] += 4d * Math.log(h / spot) / sigma * df1Bar; // tend to be infinite for large sigma
      return rebate;
    }
    double sigmaSq = sigma * sigma;
    double rootT = Math.sqrt(t);
    double sigmaT = sigma * rootT;
    double mu = (costOfCarry - 0.5 * sigmaSq) / sigmaSq;
    double lambda = Math.sqrt(mu * mu + 2 * rate / sigmaSq);
    double m1 = sigmaT * (1 + mu);
    double lnSpotStr = Math.log(spot / strike);
    double lnSpotBar = Math.log(spot / h);
    double lnBarSpotStr = Math.log(h * h / spot / strike);
    double lnBarSpot = Math.log(h / spot);
    double x1 = lnSpotStr / sigmaT + m1;
    double x2 = lnSpotBar / sigmaT + m1;
    //    System.out.println(lnSpotBar / sigmaT + "\t" + m1);
    double y1 = lnBarSpotStr / sigmaT + m1;
    double y2 = lnBarSpot / sigmaT + m1;
    double z = lnBarSpot / sigmaT + lambda * sigmaT;
    double[] aDerivatives = new double[8];
    double xA = getAAdjoint(spot, strike, df1, df2, x1, sigmaT, phi, aDerivatives);
    double[] bDerivatives = new double[8];
    double xB = getAAdjoint(spot, strike, df1, df2, x2, sigmaT, phi, bDerivatives);
    double[] cDerivatives = new double[10];
    double xC = getCAdjoint(spot, strike, df1, df2, y1, sigmaT, h, mu, phi, eta, cDerivatives);
    double[] dDerivatives = new double[10];
    double xD = getCAdjoint(spot, strike, df1, df2, y2, sigmaT, h, mu, phi, eta, dDerivatives);
    double[] fDerivatives = new double[8];
    double[] eDerivatives = new double[10];
    double xE = isKnockIn ? getEAdjoint(spot, rebate, df2, x2, y2, sigmaT, h, mu, eta, eDerivatives) :
        getFAdjoint(spot, rebate, z, sigmaT, h, mu, lambda, eta, fDerivatives);
    double xEBar = 0.0;
    double xDBar = 0.0;
    double xCBar = 0.0;
    double xBBar = 0.0;
    double xABar = 0.0;
    double price;
    if (isKnockIn) { // IN start
      if (isDown) { // DOWN start
        if (isCall) { // Call start
          if (strike > h) {
            xCBar = 1.0;
            xEBar = 1.0;
            price = xC + xE;
          } else {
            xABar = 1.0;
            xBBar = -1.0;
            xDBar = 1.0;
            xEBar = 1.0;
            price = xA - xB + xD + xE;
          }
        } else { // Put start
          if (strike > h) {
            xBBar = 1.0;
            xCBar = -1.0;
            xDBar = 1.0;
            xEBar = 1.0;
            price = xB - xC + xD + xE;
          } else {
            xABar = 1.0;
            xEBar = 1.0;
            price = xA + xE;
          }
        } // DOWN end
      } else { // UP start
        if (isCall) {
          if (strike > h) {
            xABar = 1.0;
            xEBar = 1.0;
            price = xA + xE;
          } else {
            xBBar = 1.0;
            xCBar = -1.0;
            xDBar = 1.0;
            xEBar = 1.0;
            price = xB - xC + xD + xE;
          }
        } else {
          if (strike > h) {
            xABar = 1.0;
            xBBar = -1.0;
            xDBar = 1.0;
            xEBar = 1.0;
            price = xA - xB + xD + xE;
          } else {
            xCBar = 1.0;
            xEBar = 1.0;
            price = xC + xE;
          }
        } // UP end
      } // IN end
    } else { // OUT start
      if (isDown) { // DOWN start
        if (isCall) { // CALL start
          if (strike > h) {
            xABar = 1.0;
            xCBar = -1.0;
            xEBar = 1.0;
            price = xA - xC + xE;
          } else {
            xBBar = 1.0;
            xDBar = -1.0;
            xEBar = 1.0;
            price = xB - xD + xE;
          }
        } else { // PUT start
          if (strike > h) {
            xABar = 1.0;
            xBBar = -1.0;
            xCBar = 1.0;
            xDBar = -1.0;
            xEBar = 1.0;
            price = xA - xB + xC - xD + xE;
          } else {
            xEBar = 1.0;
            price = xE;
          } // PUT end
        } // DOWN end
      } else { // UP start
        if (isCall) {
          if (strike > h) {
            xEBar = 1.0;
            price = xE;
          } else {
            xABar = 1.0;
            xBBar = -1.0;
            xCBar = 1.0;
            xDBar = -1.0;
            xEBar = 1.0;
            price = xA - xB + xC - xD + xE;
          }
        } else {
          if (strike > h) {
            xBBar = 1.0;
            xDBar = -1.0;
            xEBar = 1.0;
            price = xB - xD + xE;
          } else {
            xABar = 1.0;
            xCBar = -1.0;
            xEBar = 1.0;
            price = xA - xC + xE;
          } // PUT end
        } // UP end
      } // OUT end
    }
    // Backward sweep (first step in the forward sweep)
    double zBar = 0.0;
    double y2Bar = 0.0;
    double x2Bar = 0.0;
    double lambdaBar = 0.0;
    double muBar = 0.0;
    double sigmaTBar = 0.0;
    double df2Bar = 0.0;
    double div = 1d / spot / sigmaT;
    double divSq = 1d / spot / spot / sigmaSq / t;
    if (isKnockIn) {
      y2Bar = eDerivatives[3] * xEBar;
      x2Bar = eDerivatives[2] * xEBar;
      muBar = eDerivatives[5] * xEBar;
      sigmaTBar = eDerivatives[4] * xEBar;
      df2Bar = eDerivatives[1] * xEBar;
      derivatives[0] = eDerivatives[0] * xEBar;
      derivatives[6] =
          xEBar * (eDerivatives[6] + (eDerivatives[7] + eDerivatives[8]) * divSq - 2d * div * eDerivatives[9]);
    } else {
      zBar = fDerivatives[1] * xEBar;
      lambdaBar = fDerivatives[4] * xEBar; // only F has lambda dependence, which in turn is a function of mu, see muBar+= below
      muBar = fDerivatives[3] * xEBar;
      sigmaTBar = fDerivatives[2] * xEBar;
      derivatives[0] = fDerivatives[0] * xEBar;
      derivatives[6] = xEBar * (fDerivatives[5] + fDerivatives[6] * divSq - 2d * fDerivatives[7] * div);
    }
    y2Bar += dDerivatives[4] * xDBar;
    double y1Bar = cDerivatives[4] * xCBar;
    x2Bar += bDerivatives[4] * xBBar;
    double x1Bar = aDerivatives[4] * xABar;
    double m1Bar = x1Bar + x2Bar + y1Bar + y2Bar;
    muBar += cDerivatives[6] * xCBar + dDerivatives[6] * xDBar + sigmaT * m1Bar + mu / lambda * lambdaBar;
    sigmaTBar += aDerivatives[5] * xABar // dA/dsigT - it does not include x1's dependence on sigmaT. This is below in x1Bar 
        + bDerivatives[5] * xBBar // Same as above - A and B share form/function
        + cDerivatives[5] * xCBar // C additionally has mu dependence on sigma. This is captured in muBar
        + dDerivatives[5] * xDBar + (lambda - lnBarSpot / (sigmaT * sigmaT)) * zBar
        - lnBarSpot / (sigmaT * sigmaT) * y2Bar - lnBarSpotStr / (sigmaT * sigmaT) * y1Bar
        - lnSpotBar / (sigmaT * sigmaT) * x2Bar - lnSpotStr / (sigmaT * sigmaT) * x1Bar + (1 + mu) * m1Bar;
    double sigmaSqBar = -costOfCarry / (sigmaSq * sigmaSq) * muBar - rate / (sigmaSq * sigmaSq) / lambda * lambdaBar;
    df2Bar += aDerivatives[3] * xABar + bDerivatives[3] * xBBar + cDerivatives[3] * xCBar + dDerivatives[3] * xDBar;
    double df1Bar = aDerivatives[2] * xABar + bDerivatives[2] * xBBar + cDerivatives[2] * xCBar + dDerivatives[2] * xDBar;
    derivatives[0] += aDerivatives[0] * xABar + bDerivatives[0] * xBBar + cDerivatives[0] * xCBar
        + dDerivatives[0] * xDBar + div * x1Bar + div * x2Bar + -div * y1Bar + -div * y2Bar - div * zBar;
    derivatives[1] = aDerivatives[1] * xABar + bDerivatives[1] * xBBar + cDerivatives[1] * xCBar + dDerivatives[1] * xDBar + -1.0 / strike / sigmaT * x1Bar - 1 / strike / sigmaT * y1Bar;
    derivatives[2] = -t * df1 * df1Bar - t * df2 * df2Bar + 1.0 / lambda / sigmaSq * lambdaBar;
    derivatives[3] = t * df1 * df1Bar + 1.0 / sigmaSq * muBar;
    derivatives[4] = 2d * sigma * sigmaSqBar + rootT * sigmaTBar;
    derivatives[5] = 0.5 * sigmaTBar * sigma / rootT + df1Bar * df1 * (costOfCarry - rate) - df2Bar * df2 * rate;
    derivatives[6] += cDerivatives[7] * xCBar + dDerivatives[7] * xDBar + divSq *
        (aDerivatives[6] * xABar + bDerivatives[6] * xBBar + cDerivatives[8] * xCBar + dDerivatives[8] * xDBar)
        - div / spot * (x1Bar + x2Bar - y1Bar - y2Bar - zBar) + 2d * div *
        (aDerivatives[7] * xABar + bDerivatives[7] * xBBar - cDerivatives[9] * xCBar - dDerivatives[9] * xDBar);
    return price;
  }

  private double getA(final double s, final double k, final double df1, final double df2, final double x, final double sigmaT, final double phi) {
    return phi * (s * df1 * NORMAL.getCDF(phi * x) - k * df2 * NORMAL.getCDF(phi * (x - sigmaT)));
  }

  /**
   * The adjoint version of the quantity A computation.
   * @param s  the spot
   * @param k  the strike.
   * @param df1  the first discount factor.
   * @param df2  the second discount factor.
   * @param x  the x value.
   * @param sigmaT  volatility multiplied by square root of time
   * @param phi  the phi value
   * @param derivatives Array used to return the derivatives. Will be changed during the call. 
   * The derivatives are [0] s, [1] k, [2] df1, [3] df2, [4] x, [5] sigmaT, [6] x twice, [7] s, x 
   * (Note "s twice" vanishes).
   * @return the A value
   */
  private double getAAdjoint(double s, double k, double df1, double df2, double x, double sigmaT, double phi,
      double[] derivatives) {
    //  Forward sweep
    double n1 = NORMAL.getCDF(phi * x);
    double n2 = NORMAL.getCDF(phi * (x - sigmaT));
    double a = phi * (s * df1 * n1 - k * df2 * n2);
    // Backward sweep
    double aBar = 1.0;
    double n2Bar = phi * -k * df2 * aBar;
    double n1Bar = phi * s * df1 * aBar;
    derivatives[0] = phi * df1 * n1 * aBar;
    derivatives[1] = phi * -df2 * n2 * aBar;
    derivatives[2] = phi * s * n1 * aBar;
    derivatives[3] = phi * -k * n2 * aBar;
    double n1df = NORMAL.getPDF(x);
    double n2df = NORMAL.getPDF(x - sigmaT);
    derivatives[4] = n1df * phi * n1Bar + n2df * phi * n2Bar;
    derivatives[5] = n2df * -phi * n2Bar;
    derivatives[6] = n1df * phi * n1Bar * -x + n2df * phi * n2Bar * (sigmaT - x);
    derivatives[7] = df1 * n1df * aBar;
    return a;
  }

  /**
   * The quantity C computation.
   * @param s  the spot
   * @param k  the strike
   * @param df1  the first discount factor
   * @param df2  the second discounter factor
   * @param y  the y value
   * @param sigmaT  volatility multiplied by square root of time
   * @param h  the barrier
   * @param mu  the mu value
   * @param phi  the phi value
   * @param eta  the eta value
   * @return the C value
   */
  private double getC(final double s, final double k, final double df1, final double df2, final double y, final double sigmaT, final double h, final double mu, final double phi, final double eta) {
    return phi * (s * df1 * Math.pow(h / s, 2 * (mu + 1)) * NORMAL.getCDF(eta * y) - k * df2 * Math.pow(h / s, 2 * mu) * NORMAL.getCDF(eta * (y - sigmaT)));
  }

  /**
   * The adjoint version of the quantity C computation.
   * @param s  the spot
   * @param k  the strike
   * @param df1  the first discount factor
   * @param df2  the second discounter factor
   * @param y  the y value
   * @param sigmaT  volatility multiplied by square root of time
   * @param h  the barrier
   * @param mu  the mu value
   * @param phi  the phi value
   * @param eta  the eta value
   * @param derivatives Array used to return the derivatives. Will be changed during the call. 
   * The derivatives are [0] s, [1] k, [2] df1, [3] df2, [4] y, [5] sigmaT, [6] mu, [7] s twice, [8] y twice, [9] s,y.
   * @return C and its adjoints
   */
  private double getCAdjoint(double s, double k, double df1, double df2, double y, double sigmaT, double h, double mu,
      double phi, double eta, double[] derivatives) {
    //  Forward sweep
    double n1 = NORMAL.getCDF(eta * y);
    double n2 = NORMAL.getCDF(eta * (y - sigmaT));
    double hsMu1 = n1 == 0d ? 0d : Math.pow(h / s, 2 * (mu + 1));
    double hsMu = n2 == 0d ? 0d : Math.pow(h / s, 2 * mu);
    double c = phi * (s * df1 * hsMu1 * n1 - k * df2 * hsMu * n2);
    // Backward sweep
    double n1df = NORMAL.getPDF(y);
    double n2df = NORMAL.getPDF(y - sigmaT);
    double cBar = 1.0;
    double hsMuBar = phi * -k * df2 * n2 * cBar;
    double hsMu1Bar = phi * s * df1 * n1 * cBar;
    double n2Bar = phi * -k * df2 * hsMu * cBar;
    double n1Bar = phi * s * df1 * hsMu1 * cBar;
    double mu1 = 2d * (mu + 1d);
    double mu2 = 2d * mu + 1d;
    double ln = Math.log(h / s);
    derivatives[0] = phi * df1 * hsMu1 * n1 * cBar - 2d * mu * hsMu / s * hsMuBar - mu1 * hsMu1 / s * hsMu1Bar; // s
    derivatives[1] = phi * -df2 * hsMu * n2 * cBar; // k
    derivatives[2] = phi * s * hsMu1 * n1 * cBar; // df1
    derivatives[3] = phi * -k * hsMu * n2 * cBar; // df2
    derivatives[4] = n1df * eta * n1Bar + n2df * eta * n2Bar; // y
    derivatives[5] = -n2df * eta * n2Bar; // sigmaT
    derivatives[6] = 2d * ln * hsMu * hsMuBar + 2d * ln * hsMu1 * hsMu1Bar; // mu
    derivatives[7] = mu1 * mu2 * hsMu1 * hsMu1Bar / s / s + 2d * mu * mu2 * hsMu * hsMuBar / s / s; // s s  
    derivatives[8] = n1df * eta * n1Bar * -y + n2df * eta * n2Bar * (sigmaT - y); // y y 
    derivatives[9] = -mu2 * n1df * eta * phi * df1 * hsMu1 * cBar - 2d * mu * n2df * eta * n2Bar / s;
    return c;
  }

  /**
   * The quantity E computation.
   * @param s  the spot
   * @param rebate  the rebate
   * @param df2  the second discount factor
   * @param x  the x value
   * @param y  the y value
   * @param sigmaT  volatility multiplied by square root of time
   * @param h  the barrier
   * @param mu  the mu value
   * @param eta  the eta value
   * @return the E value
   */
  private double getE(final double s, final double rebate, final double df2, final double x, final double y, final double sigmaT, final double h, final double mu, final double eta) {
    return rebate * df2 * (NORMAL.getCDF(eta * (x - sigmaT)) - Math.pow(h / s, 2 * mu) * NORMAL.getCDF(eta * (y - sigmaT)));
  }

  /**
   * The adjoint version of the quantity E computation.
   * @param s  the spot
   * @param rebate  the rebate
   * @param df2  the second discount factor
   * @param x  the x value
   * @param y  the y value
   * @param sigmaT  volatility multiplied by square root of time
   * @param h  the barrier
   * @param mu  the mu value
   * @param eta  the eta value
   * @param derivatives Array used to return the derivatives. Will be changed during the call. 
   * The derivatives are [0] s, [1] df2, [2] x, [3] y, [4] sigmaT, [5] mu, [6] s twice, [7] x twice, [8] y twice, 
   * [9] s,y (note that "s,x" and "x,y" vanish). 
   * @return the E value
   */
  private double getEAdjoint(double s, double rebate, double df2, double x, double y, double sigmaT, double h,
      double mu, final double eta, double[] derivatives) {
    //  Forward sweep
    double n1 = NORMAL.getCDF(eta * (x - sigmaT));
    double n2 = NORMAL.getCDF(eta * (y - sigmaT));
    double hsMu = n2 == 0d ? 0d : Math.pow(h / s, 2 * mu);
    double e = rebate * df2 * (n1 - hsMu * n2);
    // Backward sweep
    double eBar = 1d;
    double n1df = NORMAL.getPDF(x - sigmaT);
    double n2df = NORMAL.getPDF(y - sigmaT);
    double hsMuBar = rebate * df2 * -n2 * eBar;
    double n2Bar = rebate * df2 * -hsMu * eBar;
    double n1Bar = rebate * df2 * eBar;
    derivatives[0] = -2d * mu * hsMu / s * hsMuBar; // s
    derivatives[1] = rebate * (n1 - hsMu * n2) * eBar; // df2;
    derivatives[2] = n1df * eta * n1Bar; // x
    derivatives[3] = n2df * eta * n2Bar; // y
    derivatives[4] = n2df * -eta * n2Bar + n1df * -eta * n1Bar; // sigmaT
    derivatives[5] = 2d * Math.log(h / s) * hsMu * hsMuBar; // mu
    derivatives[6] = 2d * mu * (2d * mu + 1d) * hsMu * hsMuBar / s / s; // s s
    derivatives[7] = derivatives[2] * (sigmaT - x); // x x
    derivatives[8] = derivatives[3] * (sigmaT - y); // y y
    derivatives[9] = -2d * mu * n2df * eta * n2Bar / s; // s y
    return e;
  }

  /**
   * The quantity F computation.
   * @param s  the spot
   * @param rebate  the rebate
   * @param z  the z value
   * @param sigmaT  volatility multiplied by square root of time
   * @param h  the barrier
   * @param mu  the mu value
   * @param lambda  the lambda value
   * @param eta  the eta value
   * @return the F value
   */
  private double getF(final double s, final double rebate, final double z, final double sigmaT, final double h, final double mu, final double lambda, final double eta) {
    return rebate * (Math.pow(h / s, mu + lambda) * NORMAL.getCDF(eta * z) + Math.pow(h / s, mu - lambda) * NORMAL.getCDF(eta * (z - 2 * lambda * sigmaT)));
  }

  /**
   * The adjoint version of the quantity F computation.
   * @param s  the spot
   * @param rebate  the rebate
   * @param z  the z value
   * @param sigmaT  volatility multiplied by square root of time
   * @param h  the barrier
   * @param mu  the mu value
   * @param lambda  the lambda value
   * @param eta  the eta value
   * @param derivatives Array used to return the derivatives. Will be changed during the call. 
   * The derivatives are [0] s, [1] z, [2] sigmaT, [3] mu, [4] lambda, [5] s twice, [6] z twice, [7] s,z. 
   * @return the F value 
   */
  private double getFAdjoint(double s, double rebate, double z, double sigmaT, double h, double mu, double lambda,
      double eta, double[] derivatives) {
    //  Forward sweep
    double n1 = NORMAL.getCDF(eta * z);
    double n2 = NORMAL.getCDF(eta * (z - 2 * lambda * sigmaT));
    double hsMuPLa = n1 == 0d ? 0d : Math.pow(h / s, mu + lambda);
    double hsMuMLa = n2 == 0d ? 0d : Math.pow(h / s, mu - lambda);
    double f = rebate * (hsMuPLa * n1 + hsMuMLa * n2);
    // Backward sweep
    double fBar = 1.0;
    double n1df = NORMAL.getPDF(z);
    double n2df = NORMAL.getPDF(z - 2 * lambda * sigmaT);
    double hsMuPLaBar = rebate * n1 * fBar;
    double hsMuMLaBar = rebate * n2 * fBar;
    double n2Bar = rebate * hsMuMLa * fBar;
    double n1Bar = rebate * hsMuPLa * fBar;
    double pls = mu + lambda;
    double mns = mu - lambda;
    double ln = Math.log(h / s);
    derivatives[0] = -pls * hsMuPLa / s * hsMuPLaBar - mns * hsMuMLa / s * hsMuMLaBar; //s
    derivatives[1] = n1df * eta * n1Bar + n2df * eta * n2Bar; // z
    derivatives[2] = -n2df * eta * 2d * lambda * n2Bar; //sigmaT
    derivatives[3] = hsMuPLa * ln * hsMuPLaBar + hsMuMLa * ln * hsMuMLaBar; // mu
    derivatives[4] = hsMuPLa * ln * hsMuPLaBar - hsMuMLa * ln * hsMuMLaBar; // lambda
    derivatives[5] = pls * (pls + 1d) * hsMuPLa / s / s * hsMuPLaBar + mns * (mns + 1d) * hsMuMLa / s / s * hsMuMLaBar; // s s
    derivatives[6] = n1df * eta * n1Bar * -z + n2df * eta * n2Bar * (2d * lambda * sigmaT - z); // z z
    derivatives[7] = -pls * n1df * eta * n1Bar / s - mns * n2df * eta * n2Bar / s; // s z
    return f;
  }

}

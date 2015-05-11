/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.option;

import static com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory.getInterpolator;
import static org.testng.AssertJUnit.assertEquals;

import java.time.ZonedDateTime;
import java.util.ArrayList;

import com.opengamma.analytics.convention.daycount.DayCounts;
import com.opengamma.analytics.financial.ExerciseDecisionType;
import com.opengamma.analytics.financial.equity.EquityOptionBlackPresentValueCalculator;
import com.opengamma.analytics.financial.equity.EquityOptionBlackScholesRhoCalculator;
import com.opengamma.analytics.financial.equity.EquityOptionBlackScholesThetaCalculator;
import com.opengamma.analytics.financial.equity.EquityOptionBlackSpotDeltaCalculator;
import com.opengamma.analytics.financial.equity.EquityOptionBlackSpotGammaCalculator;
import com.opengamma.analytics.financial.equity.EquityOptionBlackVegaCalculator;
import com.opengamma.analytics.financial.equity.EqyOptBjerksundStenslandGreekCalculator;
import com.opengamma.analytics.financial.equity.EqyOptBjerksundStenslandPresentValueCalculator;
import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.greeks.Greek;
import com.opengamma.analytics.financial.greeks.GreekResultCollection;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.option.pricing.analytic.BjerksundStenslandModel;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceStrike;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.analytics.util.time.TimeCalculator;

/**
 * 
 */
@SuppressWarnings("javadoc")
public class EquityE2ETestMaster {
  // Calculators for European options
  protected static final EquityOptionBlackPresentValueCalculator PV_EUROPEAN = EquityOptionBlackPresentValueCalculator
      .getInstance();
  protected static final EquityOptionBlackScholesRhoCalculator RHO_EUROPEAN = EquityOptionBlackScholesRhoCalculator
      .getInstance();
  protected static final EquityOptionBlackSpotDeltaCalculator DELTA_EUROPEAN = EquityOptionBlackSpotDeltaCalculator
      .getInstance();
  protected static final EquityOptionBlackSpotGammaCalculator GAMMA_EUROPEAN = EquityOptionBlackSpotGammaCalculator
      .getInstance();
  protected static final EquityOptionBlackScholesThetaCalculator THETA_EUROPEAN = EquityOptionBlackScholesThetaCalculator
      .getInstance();
  protected static final EquityOptionBlackVegaCalculator VEGA_EUROPEAN = EquityOptionBlackVegaCalculator.getInstance();

  // Calculators for American options
  protected static final EqyOptBjerksundStenslandPresentValueCalculator PV_AMERICAN = EqyOptBjerksundStenslandPresentValueCalculator
      .getInstance();
  protected static final EqyOptBjerksundStenslandGreekCalculator GREEKS_AMERICAN = EqyOptBjerksundStenslandGreekCalculator
      .getInstance();

  // yield curve
  private static final double[] SINGLE_CURVE_TIME = new double[] {0.002739726, 0.093150685, 0.257534247, 0.515068493,
    1.005479452, 2.009416873, 3.005479452, 4.005479452, 5.005479452, 6.006684632, 7.010958904, 8.008219178,
    9.005479452, 10.00668463, 12.00547945, 15.00547945, 20.00547945 };
  private static final double[] SINGLE_CURVE_RATE = new double[] {0.001774301, 0.000980829, 0.000940143, 0.001061566,
    0.001767578, 0.005373189, 0.009795971, 0.013499667, 0.016397755, 0.018647803, 0.020528999, 0.022002859,
    0.023322553, 0.024538027, 0.026482704, 0.028498622, 0.030369559 };
  private static final String SINGLE_CURVE_NAME = "Single Curve";
  private static final Interpolator1D YIELD_INTERPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(
      Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.LINEAR_EXTRAPOLATOR,
      Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
  private static final InterpolatedDoublesCurve INTERPOLATED_CURVE = InterpolatedDoublesCurve.from(SINGLE_CURVE_TIME,
      SINGLE_CURVE_RATE, YIELD_INTERPOLATOR);
  protected static final YieldAndDiscountCurve SINGLE_CURVE = new YieldCurve(SINGLE_CURVE_NAME, INTERPOLATED_CURVE);

  // tools for vol surface
  private static final BjerksundStenslandModel AMERICAN_MODEL = new BjerksundStenslandModel();
  private final static CombinedInterpolatorExtrapolator EXPIRY_INTERPOLATOR = getInterpolator(
      Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.LINEAR_EXTRAPOLATOR,
      Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
  private static final CombinedInterpolatorExtrapolator STRIKE_INTERPOLATOR = getInterpolator(
      Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.LINEAR_EXTRAPOLATOR,
      Interpolator1DFactory.LINEAR_EXTRAPOLATOR);

  protected static final String[] COMPUTE_VALUES = new String[] {"pv per contract", "pv", "delta", "gamma", "theta",
    "rho", "vega", "position delta", "position gamma", "position theta", "position rho", "position vega" };
  protected static final double TOL = 1.0e-10;

  /**
   * @param spot The underlying spot
   * @param timeToExpiries The time to expiry
   * @param strikes The market strikes 
   * @param marketPrices The market prices
   * @param isCalls True if call
   * @param forwardCurve The forward curve
   * @param isAmerican True if American
   * @return BlackVolatilitySurfaceStrike
   */
  protected BlackVolatilitySurfaceStrike createSurface(double spot, double[] timeToExpiries, double[][] strikes,
      double[][] marketPrices, boolean[][] isCalls, ForwardCurve forwardCurve, ExerciseDecisionType exerciseType) {
    int nExpiry = timeToExpiries.length;
    ArrayList<Double> expiryList = new ArrayList<>();
    ArrayList<Double> strikeList = new ArrayList<>();
    ArrayList<Double> impliedVolList = new ArrayList<>();
    for (int i = 0; i < nExpiry; ++i) {
      double expiry = timeToExpiries[i];
      int nStrikes = strikes[i].length;
      for (int j = 0; j < nStrikes; ++j) {
        if (!Double.isNaN(marketPrices[i][j])) {
          double strike = strikes[i][j];
          expiryList.add(expiry);
          strikeList.add(strike);
          double interestRate = SINGLE_CURVE.getInterestRate(expiry);
          double costOfCarry = Math.log(forwardCurve.getForward(expiry) / spot) / expiry;
          boolean isCall = isCalls[i][j];
          double impliedVol;
          if (exerciseType == ExerciseDecisionType.AMERICAN) {
            impliedVol = AMERICAN_MODEL.impliedVolatility(marketPrices[i][j], spot, strike, interestRate,
                costOfCarry, expiry, isCall);
          } else {
            double df = SINGLE_CURVE.getDiscountFactor(expiry);
            double fwdPrice = marketPrices[i][j] / df;
            double fwd = forwardCurve.getForward(expiry);
            impliedVol = BlackFormulaRepository.impliedVolatility(fwdPrice, fwd, strike, expiry, isCall);
          }
          impliedVolList.add(impliedVol);
        }
      }
    }

    InterpolatedDoublesSurface surface = new InterpolatedDoublesSurface(expiryList, strikeList, impliedVolList,
        new GridInterpolator2D(EXPIRY_INTERPOLATOR, STRIKE_INTERPOLATOR));
    return new BlackVolatilitySurfaceStrike(surface);
  }

  protected double[] toDateToDouble(ZonedDateTime baseDate, ZonedDateTime[] targetDates) {
    int nDates = targetDates.length;
    double[] res = new double[nDates];
    for (int i = 0; i < nDates; ++i) {
      res[i] = TimeCalculator.getTimeBetween(baseDate, targetDates[i], DayCounts.ACT_365F);
    }
    return res;
  }

  protected void assertOptionResult(InstrumentDerivative targetInstrument, double notional,
      StaticReplicationDataBundle data, double[] expected) {
    double unitAmount;
    ExerciseDecisionType exerciseType;
    if (targetInstrument instanceof EquityOption) {
      EquityOption targetOption = (EquityOption) targetInstrument;
      unitAmount = targetOption.getUnitAmount();
      exerciseType = targetOption.getExerciseType();
    } else if (targetInstrument instanceof EquityIndexOption) {
      EquityIndexOption targetOption = (EquityIndexOption) targetInstrument;
      unitAmount = targetOption.getUnitAmount();
      exerciseType = targetOption.getExerciseType();
    } else {
      throw new IllegalArgumentException("instrument should be EquityOption or EquityIndexOption");
    }

    Double pvPerContract;
    Double pv;
    Double delta;
    Double gamma;
    Double theta;
    Double rho;
    Double vega;
    if (exerciseType == ExerciseDecisionType.AMERICAN) {
      pvPerContract = targetInstrument.accept(PV_AMERICAN, data);
      pv = targetInstrument.accept(PV_AMERICAN, data) * notional;
      GreekResultCollection greeks = targetInstrument.accept(GREEKS_AMERICAN, data);
      delta = greeks.get(Greek.DELTA);
      gamma = greeks.get(Greek.GAMMA);
      theta = greeks.get(Greek.THETA);
      rho = greeks.get(Greek.RHO);
      vega = greeks.get(Greek.VEGA);
    } else {
      pvPerContract = targetInstrument.accept(PV_EUROPEAN, data);
      pv = targetInstrument.accept(PV_EUROPEAN, data) * notional;
      delta = targetInstrument.accept(DELTA_EUROPEAN, data);
      gamma = targetInstrument.accept(GAMMA_EUROPEAN, data);
      theta = targetInstrument.accept(THETA_EUROPEAN, data);
      rho = targetInstrument.accept(RHO_EUROPEAN, data);
      vega = targetInstrument.accept(VEGA_EUROPEAN, data);
    }
    double positionDelta = delta * unitAmount * notional;
    double positionGamma = gamma * unitAmount * notional;
    double positionTheta = theta * unitAmount * notional;
    double positionRho = rho * unitAmount * notional;
    double positionVega = vega * unitAmount * notional;
    double[] res = new double[] {pvPerContract, pv, delta, gamma, theta, rho, vega, positionDelta,
      positionGamma, positionTheta, positionRho, positionVega };
    assertEqualsArray(COMPUTE_VALUES, expected, res, TOL);
  }

  private void assertEqualsArray(String[] messages, double[] expected, double[] result, double relativeTol) {
    int nData = messages.length;
    for (int i = 0; i < nData; ++i) {
      assertEqualsRelative(messages[i], expected[i], result[i], relativeTol);
    }
  }

  private void assertEqualsRelative(String message, double expected, double result, double relativeTol) {
    double tol = Math.max(1.0, Math.abs(expected)) * relativeTol;
    assertEquals(message, expected, result, tol);
  }
}

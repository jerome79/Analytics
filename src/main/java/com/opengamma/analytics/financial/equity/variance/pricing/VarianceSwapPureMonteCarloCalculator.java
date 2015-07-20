/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.variance.pricing;

import java.util.Arrays;

import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.volatility.local.PureLocalVolatilitySurface;
import com.opengamma.analytics.math.FunctionUtils;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Monte Carlo calculator to price a variance swap in the presence of discrete dividends. <p>
 * <b>Note</b> this is primarily to test other numerical methods 
 */
public class VarianceSwapPureMonteCarloCalculator {
  /** The number of simulation variables */
  private static final int N_SIM_VARIABLES = 3;
  /** Number of days per year */
  private static final int DAYS_PER_YEAR = 252;

  /** Whether ot use antithetic variables */
  private final boolean _useAntithetics;
  /** Whether to calculate the variance of the result */
  private final boolean _calculateVariance;
  /** Provides normally-distributed random numbers */
  private final NormalDistribution _norm;

  /**
   * Constructor taking a seed for the random number generator. The calculator is set up to use antithetic variables
   * and calculate the variance of the result.
   * @param seed The seed
   */
  public VarianceSwapPureMonteCarloCalculator(final int seed) {
    _useAntithetics = true;
    _calculateVariance = true;
    final RandomEngine random = new MersenneTwister64(seed);
    _norm = new NormalDistribution(0, 1.0, random);
  }

  /**
   * @param seed The seed 
   * @param useAntithetics true if antithetic variables are to be used
   * @param calculateVariance true if the variance of the result is to be calculated
   */
  public VarianceSwapPureMonteCarloCalculator(final int seed, final boolean useAntithetics, final boolean calculateVariance) {
    _useAntithetics = useAntithetics;
    _calculateVariance = calculateVariance;
    final RandomEngine random = new MersenneTwister64(seed);
    _norm = new NormalDistribution(0, 1.0, random);
  }

  /**
   * @param spot The spot value, not negative
   * @param discountCurve The discount curve, not null
   * @param dividends The dividends, not null
   * @param expiry The time to expiry in years, not negative
   * @param localVol A local volatility surface parameterised by strike, not null
   * @param nSims The number of simulations to run, not negative
   * @return An array containing the final value of spot, realized variance with dividends, realized variance without dividends
   * and the variance of these values of requested.
   */
  public double[] solve(final double spot, final YieldAndDiscountCurve discountCurve, final AffineDividends dividends,
      final double expiry, final PureLocalVolatilitySurface localVol, final int nSims) {

    ArgChecker.notNull(dividends, "null dividends");
    ArgChecker.notNegative(expiry, "negative expiry");
    ArgChecker.notNegative(spot, "negative spot");
    ArgChecker.notNull(discountCurve, "null discountCurve");
    ArgChecker.notNull(localVol, "null localVol");
    ArgChecker.notNegative(nSims, "negative nSims");

    final MonteCarloPath mc = new MonteCarloPath(dividends, expiry, spot, discountCurve, localVol);
    return mc.runMC(nSims, _useAntithetics, _calculateVariance);

  }

  /**
   * Monte-Carlo generator
   */
  private class MonteCarloPath {
    /** The affine dividends */
    private final AffineDividends _dividends;
    /** The spot */
    private final double _spot;
    /** The steps */
    private final int[] _steps;
    /** The number of days to expiry */
    private final int _nSteps;
    /** The number of dividends before expiry */
    private final int _nDivs;
    /** The local volatility surface */
    private final PureLocalVolatilitySurface _localVol;
    /** The time step (one day) */
    private final double _dt;
    /** The square root of the time step */
    private final double _rootDt;
    /** The forward */
    private final double[] _f;
    /** The dividends */
    private final double[] _d;

    public MonteCarloPath(final AffineDividends dividends, final double expiry, final double spot,
        final YieldAndDiscountCurve discountCurve, final PureLocalVolatilitySurface localVol) {

      _dividends = dividends;
      _spot = spot;
      _localVol = localVol;

      final int nDivs = dividends.getNumberOfDividends();
      final EquityDividendsCurvesBundle divCurves = new EquityDividendsCurvesBundle(spot, discountCurve, dividends);

      _dt = 1.0 / DAYS_PER_YEAR;
      _rootDt = Math.sqrt(_dt);
      _nSteps = (int) Math.ceil((expiry * DAYS_PER_YEAR)); //Effectively move expiry to end of day
      _f = new double[_nSteps];
      _d = new double[_nSteps];
      final double[] logP = new double[_nSteps + 1];
      logP[0] = 0.0;
      for (int i = 0; i < _nSteps; i++) {
        final double t = (i + 1) * _dt;
        logP[i + 1] = -discountCurve.getInterestRate(t) * t;
        _f[i] = divCurves.getF(t);
        _d[i] = divCurves.getD(t);
      }

      int nDivsBeforeExpiry = 0;
      int totalSteps = 0;
      final int[] steps = new int[nDivs + 1];

      if (nDivs == 0 || dividends.getTau(0) > expiry) { //no dividends or first dividend after option expiry 
        steps[0] = (int) (Math.ceil(expiry * DAYS_PER_YEAR));
        totalSteps = steps[0];
      } else {
        steps[0] = (int) (Math.ceil(dividends.getTau(0) * DAYS_PER_YEAR)) - 1; //Effectively move dividend payment to end of day, and take steps up to the end of the day before
        totalSteps += steps[0] + 1;
        nDivsBeforeExpiry++;
        for (int i = 1; i < nDivs; i++) {
          if (dividends.getTau(i) > expiry) { //if dividend after expiry, step steps up to expiry and not not consider anymore dividends 
            steps[i] = ((int) Math.ceil(expiry * DAYS_PER_YEAR)) - totalSteps;
            totalSteps += steps[i];
            break;
          }
          steps[i] = ((int) Math.ceil(dividends.getTau(i) * DAYS_PER_YEAR)) - totalSteps - 1;
          totalSteps += steps[i] + 1;
          nDivsBeforeExpiry++;
        }

        if (dividends.getTau(nDivs - 1) < expiry) {
          steps[nDivs] = ((int) Math.ceil(expiry * DAYS_PER_YEAR)) - totalSteps;
          totalSteps += steps[nDivs];
        }
      }

      //check
      ArgChecker.isTrue(totalSteps == _nSteps, "got the steps wrong");

      //only care about the dividends that occur before expiry 
      _steps = Arrays.copyOfRange(steps, 0, nDivsBeforeExpiry + 1);
      _nDivs = nDivsBeforeExpiry;

    }

    public double[] runMC(final int nSims, final boolean useAntithetics, final boolean calErrors) {

      final int nVar = calErrors ? 2 * N_SIM_VARIABLES : N_SIM_VARIABLES;
      final double[] res = new double[nVar];
      double[] temp;
      for (int i = 0; i < nSims; i++) {
        final double[] z = getNormals();
        temp = runPath(z);
        for (int j = 0; j < N_SIM_VARIABLES; j++) {
          res[j] += temp[j];
        }
        if (calErrors) {
          for (int j = 0; j < N_SIM_VARIABLES; j++) {
            res[j + N_SIM_VARIABLES] += temp[j] * temp[j];
          }
        }
        if (useAntithetics) {
          for (int k = 0; k < _nSteps; k++) {
            z[k] *= -1.0;
          }
          temp = runPath(z);
          for (int j = 0; j < N_SIM_VARIABLES; j++) {
            res[j] += temp[j];
          }
          if (calErrors) {
            for (int j = 0; j < N_SIM_VARIABLES; j++) {
              res[j + N_SIM_VARIABLES] += temp[j] * temp[j];
            }
          }
        }
      }
      final int n = useAntithetics ? 2 * nSims : nSims;
      for (int j = 0; j < N_SIM_VARIABLES; j++) {
        res[j] /= n;
      }
      if (calErrors) {
        for (int j = 0; j < N_SIM_VARIABLES; j++) {
          res[j + N_SIM_VARIABLES] = (res[j + N_SIM_VARIABLES] - n * FunctionUtils.square(res[j])) / (n - 1) / n;
        }
      }

      return res;
    }

    /**
     * 
     * @param z Set of iid standard normal random variables 
     * @return The final value of spot and the realized variance with and without dividends correction
     */
    public double[] runPath(final double[] z) {

      double xOld = 1.0; //previous value of pure stock
      double x = 0; //current value of pure stock
      double sOld = _spot; //previous value of stock
      double s = 0; //current value of stock
      double sTotOld = _spot; //previous value of total returns process
      double sTot = 0; //current value of total returns process
      double rv1 = 0.0; //Accumulator of realised variance (with correction made for dividends)
      double rv2 = 0.0; //Accumulator of realised variance (without correction made for dividends)
      double t = 0.0; //current time
      double vol; //value of (pure) local vol at start of step
      double ret; //The daily return 

      int tSteps = 0;
      for (int k = 0; k < _nDivs; k++) {
        final int steps = _steps[k];
        //simulate the continuous path between dividends
        for (int i = 0; i < steps; i++) {
          vol = _localVol.getVolatility(t, xOld);
          //this Euler step is exact if the volatility is constant 
          x = xOld * Math.exp(-vol * vol / 2 * _dt + vol * _rootDt * z[tSteps]);
          s = (_f[tSteps] - _d[tSteps]) * x + _d[tSteps];
          sTot = sTotOld * s / sOld;
          ret = Math.log(s / sOld);
          final double temp = ret * ret;
          rv1 += temp;
          rv2 += temp;
          xOld = x;
          sOld = s;
          sTotOld = sTot;
          t += _dt;
          tSteps++;
        }

        //simulate the path on dividend day
        //The dividend payment is effectively moved to the end of the day
        vol = _localVol.getVolatility(t, xOld);
        x = xOld * Math.exp(-vol * vol / 2 * _dt + vol * _rootDt * z[tSteps]);
        s = (_f[tSteps] - _d[tSteps]) * x + _d[tSteps];
        final double sm = (s + _dividends.getAlpha(k)) / (1 - _dividends.getBeta(k)); //the stock price immediately before the dividend payment 
        sTot = sTotOld * sm / sOld;
        rv1 += FunctionUtils.square(Math.log(sm / sOld));
        rv2 += FunctionUtils.square(Math.log(s / sOld));
        xOld = x;
        sOld = s;
        sTotOld = sTot;
        t += _dt;
        tSteps++;
      }

      //simulate the remaining continuous path from the last dividend to the expiry 
      final int steps = _steps[_nDivs];
      //simulate the continuous path between dividends
      for (int i = 0; i < steps; i++) {
        vol = _localVol.getVolatility(t, xOld);
        x = xOld * Math.exp(-vol * vol / 2 * _dt + vol * _rootDt * z[tSteps]);
        s = (_f[tSteps] - _d[tSteps]) * x + _d[tSteps];
        sTot = sTotOld * s / sOld;
        ret = Math.log(s / sOld);
        rv1 += ret * ret;
        rv2 += ret * ret;
        xOld = x;
        sOld = s;
        sTotOld = sTot;
        tSteps++;
        t += _dt;
      }

      //correct for mean and bias
      rv1 -= FunctionUtils.square(Math.log(sTot / _spot)) / _nSteps;
      rv2 -= FunctionUtils.square(Math.log(s / _spot)) / _nSteps;
      final double biasCorr = ((double) DAYS_PER_YEAR) / (_nSteps - 1);
      rv1 *= biasCorr;
      rv2 *= biasCorr;

      return new double[] {s, rv1, rv2 };
    }

    @SuppressWarnings("synthetic-access")
    private double[] getNormals() {
      final double[] z = new double[_nSteps];
      for (int i = 0; i < _nSteps; i++) {
        z[i] = _norm.nextRandom();
      }
      return z;
    }

  }
}
/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.surface;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Map;

import org.testng.annotations.Test;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.analytics.financial.model.finitedifference.BoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.ConvectionDiffusionPDE1DCoefficients;
import com.opengamma.analytics.financial.model.finitedifference.ConvectionDiffusionPDE1DStandardCoefficients;
import com.opengamma.analytics.financial.model.finitedifference.ConvectionDiffusionPDESolver;
import com.opengamma.analytics.financial.model.finitedifference.DoubleExponentialMeshing;
import com.opengamma.analytics.financial.model.finitedifference.ExponentialMeshing;
import com.opengamma.analytics.financial.model.finitedifference.HyperbolicMeshing;
import com.opengamma.analytics.financial.model.finitedifference.MeshingFunction;
import com.opengamma.analytics.financial.model.finitedifference.NeumannBoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.PDE1DDataBundle;
import com.opengamma.analytics.financial.model.finitedifference.PDEGrid1D;
import com.opengamma.analytics.financial.model.finitedifference.PDEResults1D;
import com.opengamma.analytics.financial.model.finitedifference.PDETerminalResults1D;
import com.opengamma.analytics.financial.model.finitedifference.ThetaMethodFiniteDifference;
import com.opengamma.analytics.financial.model.finitedifference.applications.InitialConditionsProvider;
import com.opengamma.analytics.financial.model.finitedifference.applications.PDE1DCoefficientsProvider;
import com.opengamma.analytics.financial.model.finitedifference.applications.PDEUtilityTools;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.local.DupireLocalVolatilityCalculator;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilitySurfaceConverter;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilitySurfaceMoneyness;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilitySurfaceStrike;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.SurfaceArrayUtils;
import com.opengamma.analytics.financial.model.volatility.smile.function.MultiHorizonMixedLogNormalModelData;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.DoubleQuadraticInterpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;


/**
 * Test.
 */
@Test
public class MixedLogNormalVolatilitySurfaceTest {

  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);

  public void flatTest() {
    final double spot = 123.0;
    final double r = 0.05;
    final ForwardCurve fc = new ForwardCurve(spot, r);
    final double vol = 0.3;
    double[] w = new double[] {0.6, 0.4 };
    double[] sigma = new double[] {vol, vol };
    MultiHorizonMixedLogNormalModelData data = new MultiHorizonMixedLogNormalModelData(w, sigma);
    BlackVolatilitySurfaceStrike ivs = MixedLogNormalVolatilitySurface.getImpliedVolatilitySurface(fc, data);
    LocalVolatilitySurfaceStrike lvs = MixedLogNormalVolatilitySurface.getLocalVolatilitySurface(fc, data);
    for (int i = 0; i < 100; i++) {
      double t = 5.0 * RANDOM.nextDouble();
      //strikes in range +- 8-sigma 
      double a = 16 * (RANDOM.nextDouble() - 0.5);
      double k = spot * Math.exp((r - vol * vol / 2) * t + Math.sqrt(t) * vol * a);
      double iv = ivs.getVolatility(t, k);
      double lv = lvs.getVolatility(t, k);
      assertEquals("implied volatility t=" + t + ", k=" + k, vol, iv, 1e-14);
      assertEquals("local volatilityt=" + t + ", k=" + k, vol, lv, 1e-14);
    }
  }


  public void nonflatTest1() {
    DupireLocalVolatilityCalculator dupire = new DupireLocalVolatilityCalculator();
    final double spot = 0.03;
    final double r = 0.05;
    final ForwardCurve fc = new ForwardCurve(spot, r);
    final double vol = 0.3;
    double[] w = new double[] {0.8, 0.2 };
    double[] sigma = new double[] {0.2, 0.7 };
    MultiHorizonMixedLogNormalModelData data = new MultiHorizonMixedLogNormalModelData(w, sigma);
    BlackVolatilitySurfaceStrike ivs = MixedLogNormalVolatilitySurface.getImpliedVolatilitySurface(fc, data);
    LocalVolatilitySurfaceStrike lvs = MixedLogNormalVolatilitySurface.getLocalVolatilitySurface(fc, data);
    LocalVolatilitySurfaceStrike lvs2 = dupire.getLocalVolatility(ivs, fc);

    for (int i = 0; i < 100; i++) {
      double t = 5.0 * RANDOM.nextDouble();
      //strikes in range +- 8-sigma 
      double a = 16 * (RANDOM.nextDouble() - 0.5);
      double k = spot * Math.exp((r - vol * vol / 2) * t + Math.sqrt(t) * vol * a);
      double lv = lvs.getVolatility(t, k);
      double lv2 = lvs2.getVolatility(t, k);
      assertEquals("Local volatility t=" + t + ", k=" + k, lv, lv2, 1e-3); //loss a lot of accuracy going via Dupire formula (since this used finite difference on the implied vol surface)
    }
  }


  public void nonflatTest2() {
    DupireLocalVolatilityCalculator dupire = new DupireLocalVolatilityCalculator();
    final double spot = 0.03;
    final double r = 0.10;
    final ForwardCurve fc = new ForwardCurve(spot, r);
    final double vol = 0.3;
    double[] w = new double[] {0.9, 0.1 };
    double[] sigma = new double[] {0.2, 0.7 };
    double[] mu = new double[] {0.1, -0.1 };

    MultiHorizonMixedLogNormalModelData data = new MultiHorizonMixedLogNormalModelData(w, sigma, mu);
    BlackVolatilitySurfaceStrike ivs = MixedLogNormalVolatilitySurface.getImpliedVolatilitySurface(fc, data);
    LocalVolatilitySurfaceStrike lvs = MixedLogNormalVolatilitySurface.getLocalVolatilitySurface(fc, data);
    LocalVolatilitySurfaceStrike lvs2 = dupire.getLocalVolatility(ivs, fc);

    for (int i = 0; i < 100; i++) {
      double t = 5.0 * RANDOM.nextDouble();
      //strikes in range +- 8-sigma 
      double a = 16 * (RANDOM.nextDouble() - 0.5);
      double k = spot * Math.exp((r - vol * vol / 2) * t + Math.sqrt(t) * vol * a);
      double lv = lvs.getVolatility(t, k);
      double lv2 = lvs2.getVolatility(t, k);
      assertEquals("Local volatility t=" + t + ", k=" + k, lv, lv2, 1e-3); //loss a lot of accuracy going via Dupire formula (since this used finite difference on the implied vol surface)
    }
 }

  /**
   * Test the pricing of a 1D option with the local volatility surface produced by the mixed log-normal model solving both the forward and backwards PDE, and compare 
   * with the result from the implied volatility surface. Neither solving the forward nor the backwards PDE give great accuracy
   */

  public void shortDatedOptionTest() {
    final ConvectionDiffusionPDESolver solver = new ThetaMethodFiniteDifference(0.5, false);
    final PDE1DCoefficientsProvider pdeProvider = new PDE1DCoefficientsProvider();
    final InitialConditionsProvider initialConProvider = new InitialConditionsProvider();

    //set up the mixed log-normal model 
    final double[] weights = new double[] {0.1, 0.8, 0.1 };
    final double[] sigmas = new double[] {0.15, 0.5, 0.9 };
    final double[] mus = new double[] {-0.1, 0, 0.1 };

    final MultiHorizonMixedLogNormalModelData mlnData = new MultiHorizonMixedLogNormalModelData(weights, sigmas, mus);
    final double spot = 100.;
    final double r = 0.1;
    final ForwardCurve fwdCurve = new ForwardCurve(spot, r);

    final double t = 1.0 / 365.;
    final double rootT = Math.sqrt(t);
    final double fwd = fwdCurve.getForward(t);

    BlackVolatilitySurfaceStrike ivs = MixedLogNormalVolatilitySurface.getImpliedVolatilitySurface(fwdCurve, mlnData);
    LocalVolatilitySurfaceStrike lvs = MixedLogNormalVolatilitySurface.getLocalVolatilitySurface(fwdCurve, mlnData);
    LocalVolatilitySurfaceMoneyness lvsm = LocalVolatilitySurfaceConverter.toMoneynessSurface(lvs, fwdCurve);

    //set up for solving the forward PDE 
    //TODO shunt this setup into its own class 
    ConvectionDiffusionPDE1DStandardCoefficients pde = pdeProvider.getForwardLocalVol(lvsm);
    Function1D<Double, Double> initialCond = initialConProvider.getForwardCallPut(true);
    double xL = 0.8;
    double xH = 1.2;
    BoundaryCondition lower = new NeumannBoundaryCondition(-1.0, xL, true);
    BoundaryCondition upper = new NeumannBoundaryCondition(0.0, xH, false);
    final MeshingFunction spaceMeshF = new HyperbolicMeshing(xL, xH, 1.0, 200, 0.001);
    final MeshingFunction timeMeshF = new ExponentialMeshing(0, t, 50, 4.0);
    final MeshingFunction timeMeshB = new DoubleExponentialMeshing(0, t, t / 2, 50, 2.0, -4.0);
    final PDEGrid1D grid = new PDEGrid1D(timeMeshF, spaceMeshF);
    PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients> dbF = new PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients>(pde, initialCond, lower, upper, grid);
    PDETerminalResults1D res = (PDETerminalResults1D) solver.solve(dbF);
    final double minK = Math.exp(-6 * rootT);
    final double maxK = Math.exp(6 * rootT);
    Map<Double, Double> vols = PDEUtilityTools.priceToImpliedVol(fwdCurve, t, res, minK, maxK, true);
    DoubleQuadraticInterpolator1D interpolator = Interpolator1DFactory.DOUBLE_QUADRATIC_INSTANCE;
    Interpolator1DDataBundle idb = interpolator.getDataBundle(vols);

    //set up for solving backwards PDE 
    ConvectionDiffusionPDE1DStandardCoefficients pdeB = pdeProvider.getBackwardsLocalVol(t, lvsm);
    double sL = xL * spot;
    double sH = xH * spot;
    final MeshingFunction spaceMeshB = new HyperbolicMeshing(sL, sH, spot, 200, 0.001);
    final PDEGrid1D gridB = new PDEGrid1D(timeMeshB, spaceMeshB);
    int index = SurfaceArrayUtils.getLowerBoundIndex(gridB.getSpaceNodes(), spot);
    double s1 = gridB.getSpaceNode(index);
    double s2 = gridB.getSpaceNode(index + 1);
    final double w = (s2 - spot) / (s2 - s1);

    //solve a separate backwards PDE for each strike
    for (int i = 0; i < 10; i++) {
      double z = -5 + i;
      double k = spot * Math.exp(0.4 * rootT * z);
      double x = k / fwd;
      double vol = ivs.getVolatility(t, k);
      double volFPDE = interpolator.interpolate(idb, x);

      boolean isCall = (k >= fwd);
      BoundaryCondition lowerB = new NeumannBoundaryCondition(isCall ? 0 : -1, sL, true);
      BoundaryCondition upperB = new NeumannBoundaryCondition(isCall ? 1 : 0, sH, false);

      Function1D<Double, Double> bkdIC = initialConProvider.getEuropeanPayoff(k, isCall);
      PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients> dbB = new PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients>(pdeB, bkdIC, lowerB, upperB, gridB);
      PDEResults1D resB = solver.solve(dbB);
      double price1 = resB.getFunctionValue(index);
      double price2 = resB.getFunctionValue(index + 1);
      double vol1 = BlackFormulaRepository.impliedVolatility(price1, s1, k, t, isCall);
      double vol2 = BlackFormulaRepository.impliedVolatility(price2, s2, k, t, isCall);
      double volBPDE = w * vol1 + (1 - w) * vol2;

      assertEquals("forward PDE " + x, vol, volFPDE, 4e-3); //40bps error
      assertEquals("backwards PDE " + x, vol, volBPDE, 5e-3); //50bps error TODO - why is this so bad 
    }

  }
}

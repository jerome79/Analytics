/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.random.Well44497b;
import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.volatility.discrete.DiscreteVolatilityFunctionProvider;
import com.opengamma.analytics.financial.model.volatility.discrete.DiscreteVolatilityFunctionProviderDirect;
import com.opengamma.analytics.math.differentiation.VectorFieldFirstOrderDifferentiator;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.analytics.math.matrix.OGMatrixAlgebra;

/**
 * 
 */
@Test
public class CapletStrippingDirectTest extends CapletStrippingSetup {

  private static final VectorFieldFirstOrderDifferentiator DIFF = new VectorFieldFirstOrderDifferentiator();
  private static final Well44497b RANDOM = new Well44497b(0L);

  MatrixAlgebra MA = new OGMatrixAlgebra();

  /**
   * Fit each absolute strike in turn, so the penalty is on the curvature of the (strike-by-strike) volatility term
   * structure only
   */
  public void singleStrikeTest() {
    double lambda = 0.03;
    double[] expectedChi2 = new double[] {
        0.0497568716950382, 0.000394311340185193, 0.0300620236899078, 3.77865527010357E-05,
        0.0898729134093798, 0.000191249811885008, 0.0128366199450198, 0.0359160057932825,
        0.00017725008058278, 0.0226437526116605, 0.013244786627002, 0.00852026735632804,
        0.000499800863515531, 0.00124709904489161, 0.000268966976046058, 0.000348516506562646,
        0.00157397923356938, 0.00153840887006406};
    DoubleMatrix1D guess = null;
    int nStrikes = getNumberOfStrikes();
    CapletStrippingResult[] singleStrikeResults = new CapletStrippingResult[nStrikes];
    for (int i = 0; i < nStrikes; i++) {
      MultiCapFloorPricerGrid pricer = new MultiCapFloorPricerGrid(getCaps(i), getYieldCurves());
      CapletStripperDirect stripper = new CapletStripperDirect(pricer, lambda);

      double[] capVols = getCapVols(i);
      int n = capVols.length;
      double[] errors = new double[n];
      Arrays.fill(errors, 1e-4); // 1bps
      if (i == 0) {
        guess = new DoubleMatrix1D(pricer.getNumCaplets(), 0.7);
      }

      CapletStrippingResult res = stripper.solve(capVols, MarketDataType.VOL, errors, guess);
      singleStrikeResults[i] = res;
      guess = res.getFitParameters();
      assertEquals(expectedChi2[i], res.getChiSqr(), 1e-8);
    }
  }

  /**
   *
   */
  @Test
  public void priceTest() {
    double lambda = 0.03; // this is chosen to give a chi2/DoF of around 1

    MultiCapFloorPricerGrid pricer = new MultiCapFloorPricerGrid(getAllCapsExATM(), getYieldCurves());
    double[] capVols = getAllCapVolsExATM();
    double[] capPrices = pricer.price(capVols);
    double[] capVega = pricer.vega(capVols);
    int n = capVega.length;
    for (int i = 0; i < n; i++) {
      capVega[i] *= 1e-4; // this is approximately like having a 1bps error on volatility
    }

    CapletStripperDirect stripper = new CapletStripperDirect(pricer, lambda);

    CapletStrippingResult res = stripper.solve(capPrices, MarketDataType.PRICE, capVega);
    double expectedChi2 = 106.900149325811982;
    assertEquals(expectedChi2, res.getChiSqr(), expectedChi2 * 1e-8);
  }

  /**
   */
  @Test
  public void volTest() {
    double lambda = 0.03; // this is chosen to give a chi2/DoF of around 1
    MultiCapFloorPricerGrid pricer = new MultiCapFloorPricerGrid(getAllCapsExATM(), getYieldCurves());
    CapletStripperDirect stripper = new CapletStripperDirect(pricer, lambda);

    double[] capVols = getAllCapVolsExATM();
    int n = capVols.length;
    double[] errors = new double[n];
    Arrays.fill(errors, 1e-4); // 1bps
    DoubleMatrix1D guess = new DoubleMatrix1D(pricer.getGridSize(), 0.7);

    CapletStrippingResult res = stripper.solve(capVols, MarketDataType.VOL, errors, guess);
    double expectedChi2 = 106.90744994488705;
    assertEquals(expectedChi2, res.getChiSqr(), expectedChi2 * 1e-8);
  }

  /**
   * Run the same test as above, with a unit error - we must also scale lambda to obtain the same result (see comment below)
   */
  @Test
  public void unitErrorVolTest() {
    double lambda = 1e-8 * 0.03; //scale lambda
    MultiCapFloorPricerGrid pricer = new MultiCapFloorPricerGrid(getAllCapsExATM(), getYieldCurves());
    CapletStripperDirect stripper = new CapletStripperDirect(pricer, lambda);

    double[] capVols = getAllCapVolsExATM();
    DoubleMatrix1D guess = new DoubleMatrix1D(pricer.getGridSize(), 0.7);

    CapletStrippingResult res = stripper.solve(capVols, MarketDataType.VOL, guess);
    //Comment this is significantly different from a scaled (by 1e-8) value of the chi2 in the previous test
    double expectedChi2 = 1.201797943622287E-6;
    assertEquals(expectedChi2, res.getChiSqr(), expectedChi2 * 1e-8);
  }

  /**
   * as the previous test, but use default starting position 
   */
  @Test
  public void defaultGuessUnitErrorVolTest() {
    double lambda = 1e-8 * 0.03; //scale lambda
    MultiCapFloorPricerGrid pricer = new MultiCapFloorPricerGrid(getAllCapsExATM(), getYieldCurves());
    CapletStripperDirect stripper = new CapletStripperDirect(pricer, lambda);

    double[] capVols = getAllCapVolsExATM();
    CapletStrippingResult res = stripper.solve(capVols, MarketDataType.VOL);
    //Comment this is must closer to the scaled chi2 
    double expectedChi2 = 1.0691292714566707E-6;
    assertEquals(expectedChi2, res.getChiSqr(), expectedChi2 * 1e-8);
  }

  @Test
  public void atmCapsVolTest() {
    double lambda = 0.7; // this is chosen to give a chi2/DoF of around 1
    MultiCapFloorPricerGrid pricer = new MultiCapFloorPricerGrid(getATMCaps(), getYieldCurves());
    CapletStripperDirect stripper = new CapletStripperDirect(pricer, lambda);

    double[] capVols = getATMCapVols();
    int n = capVols.length;
    double[] errors = new double[n];
    Arrays.fill(errors, 1e-4); // 1bps
    DoubleMatrix1D guess = new DoubleMatrix1D(pricer.getGridSize(), 0.7);

    CapletStrippingResult res = stripper.solve(capVols, MarketDataType.VOL, errors, guess);

    assertEquals(5.7604902403614915, res.getChiSqr(), 1e-8);
  }

  /**
   *
   */
  @Test
  public void allCapsVolTest() {
    double lambdaT = 0.01; // this is chosen to give a chi2/DoF of around 1
    double lambdaK = 0.0002;
    List<CapFloor> allCaps = getAllCaps();
    List<CapFloor> atmCaps = getATMCaps();
    MultiCapFloorPricerGrid pricer = new MultiCapFloorPricerGrid(allCaps, getYieldCurves());
    CapletStripperDirect stripper = new CapletStripperDirect(pricer, lambdaK, lambdaT);

    double[] capVols = getAllCapVols();
    int n = capVols.length;
    double[] errors = new double[n];
    Arrays.fill(errors, 1e-3); // 10bps

    int nATM = atmCaps.size();
    for (int i = 0; i < nATM; i++) {
      int index = allCaps.indexOf(atmCaps.get(i));
      errors[index] = 1e-4; // 1bps
    }

    DoubleMatrix1D guess = new DoubleMatrix1D(pricer.getGridSize(), 0.7);

    CapletStrippingResult res = stripper.solve(capVols, MarketDataType.VOL, errors, guess);
    double expChiSqr = 131.50826642145796;
    assertEquals(expChiSqr, res.getChiSqr(), expChiSqr * 1e-8);
  }

  /**
   * We solve strike-by-strike (this takes 3-4 iterations), then concatenate the results as a starting
   * guess of a global fit; this doesn't make much different - converge in 9 rather than 11 iterations,
   * to a slightly different point from above.
   */
  @Test
  public void singleStrikeSeedTest() {
    double lambda = 0.03;
    DoubleMatrix1D guess = null;
    int nStrikes = getNumberOfStrikes();
    CapletStrippingResult[] singleStrikeResults = new CapletStrippingResult[nStrikes];
    for (int i = 0; i < nStrikes; i++) {
      MultiCapFloorPricerGrid pricer = new MultiCapFloorPricerGrid(getCaps(i), getYieldCurves());
      CapletStripperDirect stripper = new CapletStripperDirect(pricer, lambda);

      double[] capVols = getCapVols(i);
      int n = capVols.length;
      double[] errors = new double[n];
      Arrays.fill(errors, 1e-4); // 1bps
      if (i == 0) {
        guess = new DoubleMatrix1D(pricer.getNumCaplets(), 0.7);
      }

      CapletStrippingResult res = stripper.solve(capVols, MarketDataType.VOL, errors, guess);
      singleStrikeResults[i] = res;
      guess = res.getFitParameters();
    }

    MultiCapFloorPricerGrid pricer = new MultiCapFloorPricerGrid(getAllCapsExATM(), getYieldCurves());
    CapletStripperDirect stripper = new CapletStripperDirect(pricer, lambda);

    double[] capVols = getAllCapVolsExATM();
    int n = capVols.length;
    double[] errors = new double[n];
    Arrays.fill(errors, 1e-4); // 1bps

    double[] data = new double[pricer.getNumCaplets()];
    int pos = 0;
    for (int i = 0; i < nStrikes; i++) {
      double[] ssData = singleStrikeResults[i].getFitParameters().getData();
      int m = ssData.length;
      System.arraycopy(singleStrikeResults[i].getFitParameters().getData(), 0, data, pos, m);
      pos += m;
    }
    guess = new DoubleMatrix1D(data);
    CapletStrippingResult res = stripper.solve(capVols, MarketDataType.VOL, errors, guess);
    double expChi2 = 106.90677987330128;
    assertEquals(expChi2, res.getChiSqr(), expChi2 * 1e-8);
  }

  /**
   * Test the analytic Jacobian against finite difference when ATM cap are excluded
   */
  @Test
  public void jacobianExATMTest() {

    MultiCapFloorPricer pricer = new MultiCapFloorPricer(getAllCapsExATM(), getYieldCurves());
    int size = pricer.getNumCaplets();
    DoubleMatrix1D flat = new DoubleMatrix1D(size, 0.4);

    DiscreteVolatilityFunctionProvider volPro = new DiscreteVolatilityFunctionProviderDirect();
    CapletStrippingCore imp = new CapletStrippingCore(pricer, volPro);

    Function1D<DoubleMatrix1D, DoubleMatrix1D> pFunc = imp.getCapPriceFunction();
    Function1D<DoubleMatrix1D, DoubleMatrix2D> pJacFun = imp.getCapPriceJacobianFunction();
    Function1D<DoubleMatrix1D, DoubleMatrix2D> pJacFunFD = DIFF.differentiate(pFunc);
    compareJacobianFunc(pJacFun, pJacFunFD, flat, 1e-11);

    Function1D<DoubleMatrix1D, DoubleMatrix1D> vFunc = imp.getCapVolFunction();
    Function1D<DoubleMatrix1D, DoubleMatrix2D> vJacFun = imp.getCapVolJacobianFunction();
    Function1D<DoubleMatrix1D, DoubleMatrix2D> vJacFunFD = DIFF.differentiate(vFunc);
    compareJacobianFunc(vJacFun, vJacFunFD, flat, 1e-6);

    // random entries
    // The FD calculation of the Jacobian takes a long time
    DoubleMatrix1D x = new DoubleMatrix1D(size, 0.0);
    int nRuns = 2;
    for (int run = 0; run < nRuns; run++) {
      for (int i = 0; i < size; i++) {
        x.getData()[i] = 0.2 + 0.7 * RANDOM.nextDouble();
      }
      compareJacobianFunc(pJacFun, pJacFunFD, x, 1e-11);
      compareJacobianFunc(vJacFun, vJacFunFD, x, 1e-6);
    }

  }

  /**
   * Test the analytic Jacobian against finite difference when ATM cap are included
   */
  @Test
  public void jacobianTest() {

    MultiCapFloorPricerGrid pricer = new MultiCapFloorPricerGrid(getAllCaps(), getYieldCurves());
    int size = pricer.getGridSize();
    DoubleMatrix1D flat = new DoubleMatrix1D(size, 0.4);

    DiscreteVolatilityFunctionProvider volPro = new DiscreteVolatilityFunctionProviderDirect();
    CapletStrippingCore imp = new CapletStrippingCore(pricer, volPro);

    Function1D<DoubleMatrix1D, DoubleMatrix1D> pFunc = imp.getCapPriceFunction();
    Function1D<DoubleMatrix1D, DoubleMatrix2D> pJacFun = imp.getCapPriceJacobianFunction();
    Function1D<DoubleMatrix1D, DoubleMatrix2D> pJacFunFD = DIFF.differentiate(pFunc);
    compareJacobianFunc(pJacFun, pJacFunFD, flat, 1e-11);

    Function1D<DoubleMatrix1D, DoubleMatrix1D> vFunc = imp.getCapVolFunction();
    Function1D<DoubleMatrix1D, DoubleMatrix2D> vJacFun = imp.getCapVolJacobianFunction();
    Function1D<DoubleMatrix1D, DoubleMatrix2D> vJacFunFD = DIFF.differentiate(vFunc);
    compareJacobianFunc(vJacFun, vJacFunFD, flat, 1e-6);

    // random entries
    // The FD calculation of the Jacobian takes a long time
    DoubleMatrix1D x = new DoubleMatrix1D(size, 0.0);
    int nRuns = 2;
    for (int run = 0; run < nRuns; run++) {
      for (int i = 0; i < size; i++) {
        x.getData()[i] = 0.2 + 0.7 * RANDOM.nextDouble();
      }
      compareJacobianFunc(pJacFun, pJacFunFD, x, 1e-11);
      compareJacobianFunc(vJacFun, vJacFunFD, x, 1e-4);
    }
  }

}

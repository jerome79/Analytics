/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping.demo;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.interestrate.capletstripping.CapFloor;
import com.opengamma.analytics.financial.interestrate.capletstripping.CapletStripperDirect;
import com.opengamma.analytics.financial.interestrate.capletstripping.CapletStrippingResult;
import com.opengamma.analytics.financial.interestrate.capletstripping.CapletStrippingSetup;
import com.opengamma.analytics.financial.interestrate.capletstripping.MarketDataType;
import com.opengamma.analytics.financial.interestrate.capletstripping.MultiCapFloorPricer;
import com.opengamma.analytics.financial.interestrate.capletstripping.MultiCapFloorPricerGrid;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;

/**
 * Here we calibrate simultaneously to all the caps (on a particular index) in the market, by adjusting all the underlying
 * caplet volatilities. Since there are more caplets volatilities than caps, a penalty on the curvature of the caplet vols
 * in both the strike and expiry direction is applied.
 */
public class GlobalDirectFitDemo extends CapletStrippingSetup {

  /**
   * This calibrated to all the cap volatilities (excluding ATM) at once
   * <p>
   * The output is this surface sampled on a grid (101 by 101), such that it can be plotted as an Excel surface plot
   *  (or imported into some other visualisation tool).
   */
  @Test(description = "Demo of infering a caplet volatility surface")
  public void globalVolFitExATMDemo() {
    final double lambda = 0.03; // this is chosen to give a chi2/DoF of around 1
    final MultiCapFloorPricerGrid pricer = new MultiCapFloorPricerGrid(getAllCapsExATM(), getYieldCurves());
    final CapletStripperDirect stripper = new CapletStripperDirect(pricer, lambda);

    final double[] capVols = getAllCapVolsExATM();
    final int n = capVols.length;
    final double[] errors = new double[n];
    Arrays.fill(errors, 1e-4); // 1bps
    final DoubleMatrix1D guess = new DoubleMatrix1D(pricer.getGridSize(), 0.7);

    final CapletStrippingResult res = stripper.solve(capVols, MarketDataType.VOL, errors, guess);
    System.out.println(res);

    // print the caplet volatilities
    System.out.println("\n");
    res.printCapletVols(System.out);

    System.out.println("Caplet volatility surface");
    res.printSurface(System.out, 101, 101);
  }

  /**
   * Fit the ATM caps only. Since there are only 7 such caps (in our data set), the resultant 'surface' is close to a flat plane; this
   * recovers the & cap vols and satisfies the curvature penalties
   */
  @Test(description = "Demo of infering a caplet volatility surface")
  public void atmCapsOnlyDemo() {
    final double lambda = 0.7; // this is chosen to give a chi2/DoF of around 1
    final MultiCapFloorPricerGrid pricer = new MultiCapFloorPricerGrid(getATMCaps(), getYieldCurves());
    final CapletStripperDirect stripper = new CapletStripperDirect(pricer, lambda);

    final double[] capVols = getATMCapVols();
    final int n = capVols.length;
    final double[] errors = new double[n];
    Arrays.fill(errors, 1e-4); // 1bps
    final DoubleMatrix1D guess = new DoubleMatrix1D(pricer.getGridSize(), 0.7);

    final CapletStrippingResult res = stripper.solve(capVols, MarketDataType.VOL, errors, guess);
    System.out.println(res);

    System.out.println("Caplet volatility surface");
    res.printSurface(System.out, 101, 101);
  }

  /**
   * Fit all the caps. Since there is some inconsistency in our data between ATM cap quotes and those at absolute strikes (cap 'smiles' show
   * the ATM quotes lying off a smooth curve through the absolute strikes), we fit ATM quotes with an error of 1bps and everything else with an error
   * of 10bps. However, the resultant caplet volatility surface is less smooth (in the strike direction) than that made excluding ATM.
   */
  @Test(description = "Demo of infering a caplet volatility surface")
  public void globalVolFitDemo() {
    final double lambdaT = 0.01; // this is chosen to give a chi2/DoF of around 1
    final double lambdaK = 0.0002;
    final List<CapFloor> allCaps = getAllCaps();
    System.out.println("number of caps :" + allCaps.size());
    final List<CapFloor> atmCaps = getATMCaps();
    final MultiCapFloorPricerGrid pricer = new MultiCapFloorPricerGrid(allCaps, getYieldCurves());
    final CapletStripperDirect stripper = new CapletStripperDirect(pricer, lambdaK, lambdaT);

    final double[] capVols = getAllCapVols();
    final int n = capVols.length;
    final double[] errors = new double[n];
    Arrays.fill(errors, 1e-3); // 10bps

    final int nATM = atmCaps.size();
    for (int i = 0; i < nATM; i++) {
      final int index = allCaps.indexOf(atmCaps.get(i));
      errors[index] = 1e-4; // 1bps
    }

    final DoubleMatrix1D guess = new DoubleMatrix1D(pricer.getGridSize(), 0.7);

    final CapletStrippingResult res = stripper.solve(capVols, MarketDataType.VOL, errors, guess);
    System.out.println("chi2 per cap: " + res.getChiSqr() / allCaps.size());
    System.out.println(res);

    res.printSurface(System.out, 101, 101);
    // res.printCapletVols(System.out);
  }

  @Test(description = "Demo to show number of (phantom) caplets")
  public void sizeDemo() {
    final MultiCapFloorPricer pricer1 = new MultiCapFloorPricer(getAllCapsExATM(), getYieldCurves());
    System.out.println("number of caplet ex ATM " + pricer1.getNumCaplets());
    final MultiCapFloorPricerGrid pricer2 = new MultiCapFloorPricerGrid(getAllCaps(), getYieldCurves());
    System.out.println("number of caplet " + pricer2.getNumCaplets() + "\t" + pricer2.getGridSize());
  }

}

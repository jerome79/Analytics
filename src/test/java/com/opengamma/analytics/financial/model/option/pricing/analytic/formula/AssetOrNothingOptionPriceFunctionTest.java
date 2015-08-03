/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic.formula;

import static com.opengamma.strata.collect.TestHelper.assertThrowsIllegalArg;
import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;

/**
 * Test {@link AssetOrNothingOptionPriceFunction}.
 */
@Test
public class AssetOrNothingOptionPriceFunctionTest {

  private static final AssetOrNothingOptionPriceFunction PRICER = new AssetOrNothingOptionPriceFunction();
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);
  private static final double STRIKE = 1.8;
  private static final double FORWARD1 = 1.95;
  private static final double FORWARD2 = 1.7;
  private static final double TIME = 1.25;
  private static final double VOLATILITY = 0.087;

  private static final double TOL = 1.0e-13;
  private static final double EPS_FD = 1.0e-6;

  public void test_price() {
    double callComputed = PRICER.price(FORWARD1, STRIKE, TIME, VOLATILITY, true);
    double d1 = (Math.log(FORWARD1 / STRIKE) + 0.5 * VOLATILITY * VOLATILITY * TIME) / VOLATILITY / Math.sqrt(TIME);
    double callExpected = FORWARD1 * NORMAL.getCDF(d1);
    double callItmExp = PRICER.price(FORWARD1, STRIKE, 0d, VOLATILITY, true);
    double callAtmExp = PRICER.price(STRIKE, STRIKE, 0d, VOLATILITY, true);
    double callOtmExp = PRICER.price(FORWARD2, STRIKE, 0d, VOLATILITY, true);
    assertEquals(callComputed, callExpected, TOL);
    assertEquals(callItmExp, FORWARD1);
    assertEquals(callAtmExp, 0d);
    assertEquals(callOtmExp, 0d);
    double putComputed = PRICER.price(FORWARD1, STRIKE, TIME, VOLATILITY, false);
    double putExpected = FORWARD1 * NORMAL.getCDF(-d1);
    double putItmExp = PRICER.price(FORWARD2, STRIKE, 0d, VOLATILITY, false);
    double putAtmExp = PRICER.price(STRIKE, STRIKE, 0d, VOLATILITY, false);
    double putOtmExp = PRICER.price(FORWARD1, STRIKE, 0d, VOLATILITY, false);
    assertEquals(putComputed, putExpected, TOL);
    assertEquals(putItmExp, FORWARD2);
    assertEquals(putAtmExp, 0d);
    assertEquals(putOtmExp, 0d);
  }

  public void test_price_fail() {
    assertThrowsIllegalArg(() -> PRICER.price(-FORWARD1, STRIKE, TIME, VOLATILITY, true));
    assertThrowsIllegalArg(() -> PRICER.price(FORWARD1, -STRIKE, TIME, VOLATILITY, true));
    assertThrowsIllegalArg(() -> PRICER.price(FORWARD1, STRIKE, -TIME, VOLATILITY, true));
    assertThrowsIllegalArg(() -> PRICER.price(FORWARD1, STRIKE, TIME, -VOLATILITY, true));
    assertThrowsIllegalArg(() -> PRICER.price(FORWARD1, STRIKE, TIME, 0d, true));
  }

  public void test_delta() {
    double callComputed = PRICER.delta(FORWARD1, STRIKE, TIME, VOLATILITY, true);
    double callUp = PRICER.price(FORWARD1 + EPS_FD, STRIKE, TIME, VOLATILITY, true);
    double callDw = PRICER.price(FORWARD1 - EPS_FD, STRIKE, TIME, VOLATILITY, true);
    double callExpected = 0.5 * (callUp - callDw) / EPS_FD;
    assertEquals(callComputed, callExpected, EPS_FD);
    double putComputed = PRICER.delta(FORWARD1, STRIKE, TIME, VOLATILITY, false);
    double putUp = PRICER.price(FORWARD1 + EPS_FD, STRIKE, TIME, VOLATILITY, false);
    double putDw = PRICER.price(FORWARD1 - EPS_FD, STRIKE, TIME, VOLATILITY, false);
    double putExpected = 0.5 * (putUp - putDw) / EPS_FD;
    assertEquals(putComputed, putExpected, EPS_FD);
  }

  public void test_delta_expiry() {
    double itmCall = PRICER.delta(FORWARD1, STRIKE, 0d, VOLATILITY, true);
    assertEquals(itmCall, 1d);
    double otmCall = PRICER.delta(FORWARD2, STRIKE, 0d, VOLATILITY, true);
    assertEquals(otmCall, 0d);
    double atmCall = PRICER.delta(STRIKE, STRIKE, 0d, VOLATILITY, true);
    assertEquals(atmCall, 0d);
    double itmPut = PRICER.delta(FORWARD2, STRIKE, 0d, VOLATILITY, false);
    assertEquals(itmPut, 1d);
    double otmPut = PRICER.delta(FORWARD1, STRIKE, 0d, VOLATILITY, false);
    assertEquals(otmPut, 0d);
    double atmPut = PRICER.delta(STRIKE, STRIKE, 0d, VOLATILITY, false);
    assertEquals(atmPut, 0d);
  }

  public void test_delta_fail() {
    assertThrowsIllegalArg(() -> PRICER.delta(-FORWARD1, STRIKE, TIME, VOLATILITY, true));
    assertThrowsIllegalArg(() -> PRICER.delta(FORWARD1, -STRIKE, TIME, VOLATILITY, true));
    assertThrowsIllegalArg(() -> PRICER.delta(FORWARD1, STRIKE, -TIME, VOLATILITY, true));
    assertThrowsIllegalArg(() -> PRICER.delta(FORWARD1, STRIKE, TIME, -VOLATILITY, true));
    assertThrowsIllegalArg(() -> PRICER.delta(FORWARD1, STRIKE, TIME, 0d, true));
  }

  public void test_gamma() {
    double callComputed = PRICER.gamma(FORWARD1, STRIKE, TIME, VOLATILITY, true);
    double callUp = PRICER.delta(FORWARD1 + EPS_FD, STRIKE, TIME, VOLATILITY, true);
    double callDw = PRICER.delta(FORWARD1 - EPS_FD, STRIKE, TIME, VOLATILITY, true);
    double callExpected = 0.5 * (callUp - callDw) / EPS_FD;
    assertEquals(callComputed, callExpected, EPS_FD);
    double putComputed = PRICER.gamma(FORWARD1, STRIKE, TIME, VOLATILITY, false);
    double putUp = PRICER.delta(FORWARD1 + EPS_FD, STRIKE, TIME, VOLATILITY, false);
    double putDw = PRICER.delta(FORWARD1 - EPS_FD, STRIKE, TIME, VOLATILITY, false);
    double putExpected = 0.5 * (putUp - putDw) / EPS_FD;
    assertEquals(putComputed, putExpected, EPS_FD);
    double expiryComputed = PRICER.gamma(FORWARD1, STRIKE, 0d, VOLATILITY, true);
    assertEquals(expiryComputed, 0d);
  }

  public void test_gamma_fail() {
    assertThrowsIllegalArg(() -> PRICER.gamma(-FORWARD1, STRIKE, TIME, VOLATILITY, true));
    assertThrowsIllegalArg(() -> PRICER.gamma(FORWARD1, -STRIKE, TIME, VOLATILITY, true));
    assertThrowsIllegalArg(() -> PRICER.gamma(FORWARD1, STRIKE, -TIME, VOLATILITY, true));
    assertThrowsIllegalArg(() -> PRICER.gamma(FORWARD1, STRIKE, TIME, -VOLATILITY, true));
    assertThrowsIllegalArg(() -> PRICER.gamma(FORWARD1, STRIKE, TIME, 0d, true));
  }

  public void test_theta() {
    double callComputed = PRICER.theta(FORWARD1, STRIKE, TIME, VOLATILITY, true);
    double callUp = PRICER.price(FORWARD1, STRIKE, TIME + EPS_FD, VOLATILITY, true);
    double callDw = PRICER.price(FORWARD1, STRIKE, TIME - EPS_FD, VOLATILITY, true);
    double callExpected = -0.5 * (callUp - callDw) / EPS_FD;
    assertEquals(callComputed, callExpected, EPS_FD);
    double putComputed = PRICER.theta(FORWARD1, STRIKE, TIME, VOLATILITY, false);
    double putUp = PRICER.price(FORWARD1, STRIKE, TIME + EPS_FD, VOLATILITY, false);
    double putDw = PRICER.price(FORWARD1, STRIKE, TIME - EPS_FD, VOLATILITY, false);
    double putExpected = -0.5 * (putUp - putDw) / EPS_FD;
    assertEquals(putComputed, putExpected, EPS_FD);
    double atmExpiryComputed = PRICER.theta(STRIKE, STRIKE, 0d, VOLATILITY, true);
    assertEquals(atmExpiryComputed, 0d);
    double expiryComputed = PRICER.theta(FORWARD1, STRIKE, 0d, VOLATILITY, true);
    assertEquals(expiryComputed, 0d);
  }

  public void test_theta_fail() {
    assertThrowsIllegalArg(() -> PRICER.theta(-FORWARD1, STRIKE, TIME, VOLATILITY, true));
    assertThrowsIllegalArg(() -> PRICER.theta(FORWARD1, -STRIKE, TIME, VOLATILITY, true));
    assertThrowsIllegalArg(() -> PRICER.theta(FORWARD1, STRIKE, -TIME, VOLATILITY, true));
    assertThrowsIllegalArg(() -> PRICER.theta(FORWARD1, STRIKE, TIME, -VOLATILITY, true));
    assertThrowsIllegalArg(() -> PRICER.theta(FORWARD1, STRIKE, TIME, 0d, true));
  }

  public void test_vega() {
    double callComputed = PRICER.vega(FORWARD1, STRIKE, TIME, VOLATILITY, true);
    double callUp = PRICER.price(FORWARD1, STRIKE, TIME, VOLATILITY + EPS_FD, true);
    double callDw = PRICER.price(FORWARD1, STRIKE, TIME, VOLATILITY - EPS_FD, true);
    double callExpected = 0.5 * (callUp - callDw) / EPS_FD;
    assertEquals(callComputed, callExpected, EPS_FD);
    double putComputed = PRICER.vega(FORWARD1, STRIKE, TIME, VOLATILITY, false);
    double putUp = PRICER.price(FORWARD1, STRIKE, TIME, VOLATILITY + EPS_FD, false);
    double putDw = PRICER.price(FORWARD1, STRIKE, TIME, VOLATILITY - EPS_FD, false);
    double putExpected = 0.5 * (putUp - putDw) / EPS_FD;
    assertEquals(putComputed, putExpected, EPS_FD);
    double atmExpiryComputed = PRICER.vega(STRIKE, STRIKE, 0d, VOLATILITY, true);
    assertEquals(atmExpiryComputed, 0d);
    double expiryComputed = PRICER.vega(FORWARD1, STRIKE, 0d, VOLATILITY, true);
    assertEquals(expiryComputed, 0d);
  }

  public void test_vega_fail() {
    assertThrowsIllegalArg(() -> PRICER.vega(-FORWARD1, STRIKE, TIME, VOLATILITY, true));
    assertThrowsIllegalArg(() -> PRICER.vega(FORWARD1, -STRIKE, TIME, VOLATILITY, true));
    assertThrowsIllegalArg(() -> PRICER.vega(FORWARD1, STRIKE, -TIME, VOLATILITY, true));
    assertThrowsIllegalArg(() -> PRICER.vega(FORWARD1, STRIKE, TIME, -VOLATILITY, true));
    assertThrowsIllegalArg(() -> PRICER.vega(FORWARD1, STRIKE, TIME, 0d, true));
  }

}

/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting;

import java.util.BitSet;

import org.apache.commons.math3.random.Well44497b;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionProvider;

/**
 * Test.
 */
@Test
public class SABRModelFitterConstrainedTest extends SmileModelFitterTest<SABRFormulaData> {

  private static final double ALPHA = 0.05;
  private static final double BETA = 0.5;
  private static double RHO = -0.3;
  private static double NU = 0.2;
  private static Logger LOGGER = LoggerFactory.getLogger(SABRModelFitterConstrainedTest.class);
  private static final Well44497b RANDOM = new Well44497b(0L);

  public SABRModelFitterConstrainedTest() {
    _chiSqEps = 1e-4;
  }

  @Override
  VolatilityFunctionProvider<SABRFormulaData> getModel() {
    return new SABRHaganVolatilityFunction();
  }

  @Override
  SABRFormulaData getModelData() {
    return new SABRFormulaData(ALPHA, BETA, RHO, NU);
  }

  @Override
  SmileModelFitter<SABRFormulaData> getFitter(double forward, double[] strikes, double timeToExpiry, double[] impliedVols, double[] error, VolatilityFunctionProvider<SABRFormulaData> model) {
    return new SABRModelFitterConstrained(forward, strikes, timeToExpiry, impliedVols, error, model);
  }

  @Override
  double[][] getStartValues() {
    return new double[][] { {0.1, 0.7, 0.0, 0.3 }, {0.01, 1.2, 0.9, 0.4 }, {0.01, 0.5, -0.7, 0.6 } };
  }

  @Override
  Logger getlogger() {
    return LOGGER;
  }

  @Override
  BitSet[] getFixedValues() {
    final BitSet[] fixed = new BitSet[3];
    fixed[0] = new BitSet();
    fixed[1] = new BitSet();
    fixed[2] = new BitSet();
    fixed[2].set(1);
    return fixed;
  }

  @Override
  double[] getRandomStartValues() {
    final double alpha = 0.1 + 0.4 * RANDOM.nextDouble();
    final double beta = RANDOM.nextDouble();
    final double rho = 2 * RANDOM.nextDouble() - 1;
    final double nu = 1.5 * RANDOM.nextDouble();

    return new double[] {alpha, beta, rho, nu };
  }

  @Test
  public void doNothing() {

  }
}

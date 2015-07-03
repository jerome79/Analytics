/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting;

import java.util.BitSet;

import org.apache.commons.math3.random.Well44497b;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.volatility.smile.function.MixedLogNormalModelData;
import com.opengamma.analytics.financial.model.volatility.smile.function.MixedLogNormalVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;

/**
 * Test.
 */
@Test
public class MixedLogNormalModelFitterTest extends SmileModelFitterTest<MixedLogNormalModelData> {

  private static final Well44497b RANDOM = new Well44497b(0L);
  private static Logger LOGGER = LoggerFactory.getLogger(MixedLogNormalModelFitterTest.class);
  private static int N = 3;
  private static boolean USE_SHIFTED_MEANS = false;
  private static MixedLogNormalModelData DATA;
  private static double[] TRUE_PARAMS;

  public MixedLogNormalModelFitterTest() {
    _paramValueEps = 4e-4;
  }

  static {
    final double[] vols = new double[] {0.2, 0.7, 1.0 };
    final double[] w = new double[] {0.8, 0.08, 0.12 };
    DATA = new MixedLogNormalModelData(w, vols);
    TRUE_PARAMS = new double[DATA.getNumberOfParameters()];
    for (int i = 0; i < DATA.getNumberOfParameters(); i++) {
      TRUE_PARAMS[i] = DATA.getParameter(i) * (1 + RANDOM.nextDouble() * 0.2);
    }
  }

  @Override
  Logger getlogger() {
    return LOGGER;
  }

  @Override
  MixedLogNormalVolatilityFunction getModel() {
    return MixedLogNormalVolatilityFunction.getInstance();
  }

  @Override
  MixedLogNormalModelData getModelData() {
    return DATA;
  }

  @Override
  SmileModelFitter<MixedLogNormalModelData> getFitter(final double forward, final double[] strikes, final double timeToExpiry, final double[] impliedVols, final double[] error,
      final VolatilityFunctionProvider<MixedLogNormalModelData> model) {
    return new MixedLogNormalModelFitter(forward, strikes, timeToExpiry, impliedVols, error, model, N, USE_SHIFTED_MEANS);
  }

  @Override
  double[][] getStartValues() {
    return new double[][] { {0.2, 0.4, 0.4, 0.1, 0.1 }, {0.2, 0.4, 0.8, 0.8, 0.8 } };
  }

  @Override
  double[] getRandomStartValues() {
    final int n = USE_SHIFTED_MEANS ? 3 * N - 2 : 2 * N - 1;
    final double[] res = new double[n];
    res[0] = 0.1 + 0.3 * RANDOM.nextDouble();
    for (int i = 1; i < N; i++) {
      res[i] = 0.5 * RANDOM.nextDouble();
    }
    for (int i = N; i < n; i++) {
      res[i] = 2 * Math.PI * RANDOM.nextDouble();
    }
    return res;
  }

  @Override
  BitSet[] getFixedValues() {
    final int n = 2;
    final BitSet[] fixed = new BitSet[n];
    for (int i = 0; i < n; i++) {
      fixed[i] = new BitSet();
    }
    return fixed;
  }

  @Override
  protected DoubleMatrix1D toStandardForm(final DoubleMatrix1D from) {
    final int n = from.getNumberOfElements();
    final double[] temp = new double[n];
    final double[] f = from.getData();
    System.arraycopy(f, 0, temp, 0, N);
    for (int i = N; i < 2 * N - 1; i++) {
      temp[i] = toZeroToPiByTwo(f[i]);
    }
    if (USE_SHIFTED_MEANS) {
      for (int i = 2 * N - 1; i < 3 * N - 2; i++) {
        temp[i] = toZeroToPiByTwo(f[i]);
      }
    }
    return new DoubleMatrix1D(temp);
  }

  private double toZeroToPiByTwo(final double theta) {
    double x = theta;
    if (x < 0) {
      x = -x;
    }
    if (x > Math.PI / 2) {
      final int p = (int) (x / Math.PI);
      x -= p * Math.PI;
      if (x > Math.PI / 2) {
        x = -x + Math.PI;
      }
    }
    return x;
  }

}

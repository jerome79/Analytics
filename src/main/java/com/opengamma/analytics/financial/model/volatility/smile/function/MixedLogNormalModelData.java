/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.function;

import java.util.Arrays;

import com.opengamma.analytics.math.minimization.SumToOne;
import com.opengamma.strata.collect.ArgChecker;

/**
 * If a PDF is constructed as the weighted sum of log-normal distributions, then a European option price is give by the weighted sum of Black prices (with different volatilities and
 * (potentially) different forwards). Sufficiently many log-normal distributions can reproduce any PDF and therefore any arbitrage free smile.
 */
public class MixedLogNormalModelData implements SmileModelData {

  private static final double TOL = 1e-9;
  private final SumToOne _sto;

  private final int _nNorms;
  private final int _nParams;
  private final double[] _sigmas;
  private final double[] _w;
  private final double[] _f;
  private final boolean _shiftedMeans;

  //for a mixture of n log-normals, the parameters are ordered as: sigma_0, deltaSigma_1....deltaSigma_{n-1}, theta_1...theta_{n-1}, phi_1...phi_{n-1}
  //where sigma_0 is the lowest volatility state, and the volatility of state i, sigma_i = sigma_{i-1} + deltaSigma_i, so the volatility states are strictly increasing
  //(with  deltaSigma_i > 0). The angles theta encode the weights (via the SumToOne class) and the angles phi encode the partial forwards (if they are used). Therefore, there
  //are 3n-2 free parameters (or 2n-1 in the case that the partial forwards are all fixed to one)
  private final double[] _parameters;

  /**
   * Set up a mixed log-normal model with the means of the distributions all the same value
   * @param parameters The 2n-1 parameters (where n is the number of normals) in order as: sigma_0, deltaSigma_1....deltaSigma_{n-1}, theta_1...theta_{n-1} where sigma_0
   *  is the lowest volatility state, and the volatility of state i, sigma_i = sigma_{i-1} + deltaSigma_i, so the volatility states are strictly increasing (with  deltaSigma_i > 0).
   * The angles theta encode the weights 
   *  (via the SumToOne class). 
   */
  public MixedLogNormalModelData(final double[] parameters) {
    this(parameters, true);
  }

  /**
   * Set up a mixed log-normal model with option to have distributions with different means 
   * @param parameters The 2n-1 or 3n-2 parameters (where n is the number of normals) depending on whether useShiftedMeans is false or true. The parameters in order as:
   * sigma_0, deltaSigma_1....deltaSigma_{n-1}, theta_1...theta_{n-1}, phi_1...phi_{n-1}
   * where sigma_0 is the lowest volatility state, and the volatility of state i, sigma_i = sigma_{i-1} + deltaSigma_i, so the volatility states are strictly increasing
   * (with deltaSigma_i > 0). The angles theta encode the weights (via the SumToOne class) and the angles phi encode the partial forwards (if they are used).
   * @param useShiftedMeans If true the distributions can have different means (and 3n-2 parameters must be supplied), otherwise they are all the same (and 2n-1 parameters must be supplied)
   */
  public MixedLogNormalModelData(final double[] parameters, final boolean useShiftedMeans) {
    ArgChecker.notNull(parameters, "parameters");
    _nParams = parameters.length;
    _shiftedMeans = useShiftedMeans;
    int n;
    if (useShiftedMeans) {
      ArgChecker.isTrue(_nParams % 3 == 1, "Wrong length of parameters - length {}, but must be 3n-2, where n is an integer", _nParams);
      n = (_nParams + 2) / 3;
    } else {
      ArgChecker.isTrue(_nParams % 2 == 1, "Wrong length of parameters - length {}, but must be 2n-1, where n is an integer", _nParams);
      n = (_nParams + 1) / 2;
    }
    _nNorms = n;

    //check parameters
    for (int i = 0; i < n; i++) {
      ArgChecker.isTrue(parameters[i] >= 0.0, "parameters {} have value {}, must be >= 0", i, parameters[i]);
    }
    //Review it is not clear whether we wish to restrict the range of angles

    _sto = new SumToOne(n);
    _parameters = parameters;
    _sigmas = new double[n];
    _sigmas[0] = _parameters[0];
    for (int i = 1; i < n; i++) {
      _sigmas[i] = _sigmas[i - 1] + _parameters[i];
    }
    double[] temp = Arrays.copyOfRange(_parameters, n, 2 * n - 1);
    _w = _sto.transform(temp);
    if (useShiftedMeans) {
      temp = Arrays.copyOfRange(_parameters, 2 * n - 1, 3 * n - 2);
      final double[] a = _sto.transform(temp);
      _f = new double[n];
      for (int i = 0; i < n; i++) {
        if (_w[i] > 0) {
          _f[i] = a[i] / _w[i];
        } else {
          _f[i] = 1.0; //if the weight is zero, this will not count towards the price
        }
      }
    } else {
      _f = new double[n];
      Arrays.fill(_f, 1.0);
    }
  }

  /**
   * Set up a mixed log-normal model with the means of the distributions all the same value
   * @param weights The weights  <b>These weights must sum to 1</b> 
   * @param sigmas The standard deviation of the log of the distributions 
   */
  public MixedLogNormalModelData(final double[] weights, final double[] sigmas) {
    ArgChecker.notNull(sigmas, "null sigmas");
    ArgChecker.notNull(weights, "null weights");
    _shiftedMeans = false;
    final int n = sigmas.length;
    _nNorms = n;
    ArgChecker.isTrue(n == weights.length, "Weights not the same length as sigmas");
    ArgChecker.isTrue(n > 0, "no weights");
    double sum = 0.0;
    for (int i = 0; i < n; i++) {
      ArgChecker.isTrue(sigmas[i] > 0.0, "zero or negative sigma");
      ArgChecker.isTrue(weights[i] >= 0.0, "negative weight");
      sum += weights[i];
    }
    ArgChecker.isTrue(Math.abs(sum - 1.0) < TOL, "Weights do not sum to 1.0");
    _nParams = 2 * n - 1;
    _sigmas = sigmas;
    _w = weights;
    _f = new double[n];
    Arrays.fill(_f, 1.0);

    _sto = new SumToOne(n);
    _parameters = new double[_nParams];
    _parameters[0] = sigmas[0];
    for (int i = 1; i < n; i++) {
      final double temp = sigmas[i] - sigmas[i - 1];
      ArgChecker.isTrue(temp >= 0, "sigmas must be increasing"); //TODO drop this and parallel sort into increasing order
      _parameters[i] = temp;
    }
    final double[] theta = _sto.inverseTransform(weights);
    System.arraycopy(theta, 0, _parameters, n, n - 1);
  }

  /**
   * Set up a mixed log-normal model with the means of the distributions can take different values 
   * @param weights The weights  <b>These weights must sum to 1</b> 
   * @param sigmas The standard deviation of the log of the distributions 
   * @param relativePartialForwards The expectation of each distribution is rpf_i*forward (rpf_i is the ith relativePartialForwards)
   * <b>Must have sum w_i*rpf_i = 1.0</b>
   */
  public MixedLogNormalModelData(final double[] weights, final double[] sigmas, final double[] relativePartialForwards) {
    _shiftedMeans = true;
    ArgChecker.notNull(sigmas, "null sigmas");
    ArgChecker.notNull(weights, "null weights");
    final int n = sigmas.length;
    _nNorms = n;
    ArgChecker.isTrue(n == weights.length, "Weights not the same length as sigmas");
    ArgChecker.isTrue(n == relativePartialForwards.length, "Partial forwards not the same length as sigmas");
    ArgChecker.isTrue(n > 0, "no weights");
    double sum = 0.0;
    double sumF = 0.0;
    final double[] a = new double[n];
    for (int i = 0; i < n; i++) {
      ArgChecker.isTrue(sigmas[i] > 0.0, "zero or negative sigma");
      ArgChecker.isTrue(weights[i] >= 0.0, "negative weight");
      ArgChecker.isTrue(relativePartialForwards[i] > 0.0, "zero of negative partial forward");
      sum += weights[i];
      final double temp = weights[i] * relativePartialForwards[i];
      sumF += temp;
      a[i] = temp;
    }
    ArgChecker.isTrue(Math.abs(sum - 1.0) < TOL, "Weights do not sum to 1.0");
    ArgChecker.isTrue(Math.abs(sumF - 1.0) < TOL, "Weighted partial forwards do not sum to forward");
    _sigmas = sigmas;
    _w = weights;
    _f = relativePartialForwards;
    _nParams = 3 * n - 2;

    _sto = new SumToOne(n);
    _parameters = new double[_nParams];
    _parameters[0] = sigmas[0];
    for (int i = 1; i < n; i++) {
      final double temp = sigmas[i] - sigmas[i - 1];
      ArgChecker.isTrue(temp >= 0, "sigmas must be increasing"); //TODO drop this and parallel sort into increasing order
      _parameters[i] = temp;
    }
    final double[] theta = _sto.inverseTransform(weights);
    System.arraycopy(theta, 0, _parameters, n, n - 1);

    final double[] phi = _sto.inverseTransform(a);
    System.arraycopy(phi, 0, _parameters, 2 * n - 1, n - 1);
  }

  @Override
  public boolean isAllowed(final int index, final double value) {
    if (index < _nNorms) {
      return value >= 0.0;
    }
    return true;
  }

  public double[] getWeights() {
    return _w;
  }

  public double[] getVolatilities() {
    return _sigmas;
  }

  public double[] getRelativeForwards() {
    return _f;
  }

  /**
   * The matrix of partial derivatives of weights with respect to the angles theta
   * @return the n by n-1 Jacobian, where n is the number of normals
   */
  public double[][] getWeightsJacobian() {
    final double[] temp = Arrays.copyOfRange(_parameters, _nNorms, 2 * _nNorms - 1);
    return _sto.jacobian(temp);
  }

  /**
   * The matrix of partial derivatives of relative forwards  with respect to the angles phi
   * <b>Note</b> The returned matrix has each row multiplied by the weight
   * @return the n by n-1 Jacobian, where n is the number of normals
   */
  public double[][] getRelativeForwardsJacobian() {
    if (!_shiftedMeans) {
      throw new IllegalArgumentException("This model does not used shifted means, therefore no Jacobian exists");
    }
    final double[] temp = Arrays.copyOfRange(_parameters, 2 * _nNorms - 1, 3 * _nNorms - 2);
    return _sto.jacobian(temp);
  }

  @Override
  public int getNumberOfParameters() {
    return _nParams;
  }

  @Override
  public double getParameter(final int index) {
    final double temp = _parameters[index];
    if (temp >= 0 && temp <= Math.PI / 2) {
      return temp;
    }
    return toZeroToPiByTwo(temp);
  }

  @Override
  public SmileModelData with(final int index, final double value) {
    final double[] temp = new double[_nParams];
    System.arraycopy(_parameters, 0, temp, 0, _nParams);
    temp[index] = value;
    return new MixedLogNormalModelData(temp, _shiftedMeans);
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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_f);
    result = prime * result + (_shiftedMeans ? 1231 : 1237);
    result = prime * result + Arrays.hashCode(_sigmas);
    result = prime * result + Arrays.hashCode(_w);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final MixedLogNormalModelData other = (MixedLogNormalModelData) obj;
    if (_shiftedMeans && !Arrays.equals(_f, other._f)) {
      return false;
    }
    if (_shiftedMeans != other._shiftedMeans) {
      return false;
    }
    if (!Arrays.equals(_sigmas, other._sigmas)) {
      return false;
    }
    if (!Arrays.equals(_w, other._w)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "MixedLogNormalModelData [_sigmas=" + Arrays.toString(_sigmas) + ", _w=" + Arrays.toString(_w) + ", _f=" + Arrays.toString(_f) + "]";
  }

}

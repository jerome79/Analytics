/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import com.opengamma.analytics.math.utilities.Epsilon;

/**
 * 
 */
public class AnnuityForSpreadContPemiumApproxFunction extends AnnuityForSpreadFunction {
  private final int _n;
  private final double[] _knots;
  private final double[] _rt;
  private final double _lgd;
  private final double _eta;

  /**
  * For a given quoted spread (aka 'flat' spread), this function returns the risky annuity (aka risky PV01, RPV01 or risky duration).
   * This works by using the credit triangle approximation; that is, the premiums are assumed to be paid continuously. 
   * @param cds analytic description of a CDS traded at a certain time 
   * @param yieldCurve Calibrated yield curve 
   */
  public AnnuityForSpreadContPemiumApproxFunction(final CDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve) {
    _knots = DoublesScheduleGenerator.truncateSetInclusive(cds.getEffectiveProtectionStart(), cds.getProtectionEnd(), yieldCurve.getKnotTimes());
    _n = _knots.length;
    _rt = new double[_n];
    for (int i = 0; i < _n; i++) {
      _rt[i] = yieldCurve.getRT(_knots[i]);
    }
    _lgd = cds.getLGD();
    _eta = cds.getCoupon(0).getYFRatio();
  }

  @Override
  public Double evaluate(final Double spread) {
    final double lambda = _eta * spread / _lgd;
    return annuity(lambda);
  }

  private double annuity(final double hazardRate) {
    double rt0 = _rt[0];
    double sum = 0;
    for (int i = 1; i < _n; i++) {
      final double rt1 = _rt[i];
      final double dt = _knots[i] - _knots[i - 1];
      final double theta = rt1 - rt0 + hazardRate * dt;
      sum += dt * Math.exp(-rt0 - hazardRate * _knots[i - 1]) * Epsilon.epsilon(-theta);
      rt0 = rt1;
    }
    return _eta * sum;
  }

}

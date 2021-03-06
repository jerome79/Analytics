/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.surface;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import com.opengamma.analytics.financial.model.option.definition.SmileDeltaParameters;
import com.opengamma.analytics.financial.model.volatility.SmileAndBucketedSensitivities;
import com.opengamma.analytics.financial.model.volatility.VolatilityAndBucketedSensitivities;
import com.opengamma.analytics.financial.model.volatility.VolatilityAndBucketedSensitivitiesModel;
import com.opengamma.analytics.financial.model.volatility.curve.BlackForexTermStructureParameters;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.strata.collect.ArgChecker;
import com.opengamma.strata.collect.tuple.Triple;

/**
 * Class describing the a term structure of smiles from ATM, risk reversal and strangle as used in Forex market.
 * The delta used is the delta with respect to forward.
 */
public class SmileDeltaTermStructureParameters implements VolatilityAndBucketedSensitivitiesModel<Triple<Double, Double, Double>> {

  /**
   * Atomic used to generate a name.
   */
  private static final AtomicLong ATOMIC = new AtomicLong();
  /**
   * The name of smile parameter term strucutre.
   */
  private final String _name;
  /**
   * The time to expiration in the term structure.
   */
  private final double[] _timeToExpiration;
  /**
   * The smile description at the different time to expiration. All item should have the same deltas.
   */
  private final SmileDeltaParameters[] _volatilityTerm;
  /**
   * The interpolator/extrapolator used in the expiry dimension.
   */
  private final Interpolator1D _timeInterpolator;

  /**
   * The default interpolator: time square (total variance) with flat extrapolation.
   */
  private static final Interpolator1D DEFAULT_INTERPOLATOR_EXPIRY = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.TIME_SQUARE, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);

  /**
   * Constructor from volatility term structure.
   * 
   * @param volatilityTerm The volatility description at the different expiration.
   */
  public SmileDeltaTermStructureParameters(final SmileDeltaParameters[] volatilityTerm) {
    this(Long.toString(ATOMIC.getAndIncrement()), volatilityTerm, DEFAULT_INTERPOLATOR_EXPIRY);
  }

  /**
   * Constructor from name and volatility term structure.
   * <p>
   * The default interpolator is used to interpolate in the time dimension. 
   * 
   * @param name  the name of the smile parameter term structure
   * @param volatilityTerm The volatility description at the different expiration.
   */
  public SmileDeltaTermStructureParameters(String name, SmileDeltaParameters[] volatilityTerm) {
    this(name, volatilityTerm, DEFAULT_INTERPOLATOR_EXPIRY);
  }

  /**
   * Constructor from volatility term structure and time interpolator.
   * <p>
   * The default interpolator is used to interpolate in the time dimension. 
   * 
   * @param volatilityTerm  the volatility description at the different expiration.
   * @param interpolator  the time interpolator
   */
  public SmileDeltaTermStructureParameters(SmileDeltaParameters[] volatilityTerm, Interpolator1D interpolator) {
    this(Long.toString(ATOMIC.getAndIncrement()), volatilityTerm, interpolator);
  }

  /**
   * Constructor from name, volatility term structure and time interpolator.
   * 
   * @param name  the name of the smile parameter term structure
   * @param volatilityTerm  the volatility description at the different expiration
   * @param interpolator  the time interpolator
   */
  public SmileDeltaTermStructureParameters(
      String name,
      SmileDeltaParameters[] volatilityTerm,
      Interpolator1D interpolator) {
    _timeInterpolator = ArgChecker.notNull(interpolator, "interpolator");
    _volatilityTerm = ArgChecker.notNull(volatilityTerm, "volatilityTerm");
    _name = ArgChecker.notNull(name, "name");
    int nbExp = volatilityTerm.length;
    _timeToExpiration = new double[nbExp];
    for (int loopexp = 0; loopexp < nbExp; loopexp++) {
      _timeToExpiration[loopexp] = _volatilityTerm[loopexp].getTimeToExpiry();
    }
  }

  /**
   * Constructor from market data.
   * <p>
   * The market date consists of time to expiration, delta and volatility.  
   * <p>
   * The range of delta is common to all time to expiration. 
   * {@code volatility} should be {@code n * (2 * m + 1)}, where {@code n} is the length of {@code timeToExpiration} 
   * and {@code m} is the length of {@code delta}.
   * <p>
   * The default interpolator is used to interpolate in the time dimension. 
   * 
   * @param timeToExpiration  the time to expiration of each volatility smile 
   * @param delta  the delta at which the volatilities are given 
   * @param volatility  the volatilities at each delta 
   */
  public SmileDeltaTermStructureParameters(double[] timeToExpiration, double[] delta, double[][] volatility) {
    this(Long.toString(ATOMIC.getAndIncrement()), timeToExpiration, delta, volatility);
  }

  /**
   * Constructor from name and market data.
   * <p>
   * The market date consists of time to expiration, delta and volatility.  
   * <p>
   * The range of delta is common to all time to expiration. 
   * {@code volatility} should be {@code n * (2 * m + 1)}, where {@code n} is the length of {@code timeToExpiration} 
   * and {@code m} is the length of {@code delta}.
   * <p>
   * The default interpolator is used to interpolate in the time dimension. 
   * 
   * @param name  the name of smile parameter term structure 
   * @param timeToExpiration  the time to expiration of each volatility smile 
   * @param delta  the delta at which the volatilities are given
   * @param volatility  the volatilities at each delta 
   */
  public SmileDeltaTermStructureParameters(String name, double[] timeToExpiration, double[] delta, double[][] volatility) {
    ArgChecker.notNull(timeToExpiration, "time to expiry");
    ArgChecker.notNull(delta, "delta");
    ArgChecker.notNull(volatility, "volatility");
    _name = ArgChecker.notNull(name, "name");
    _timeToExpiration = ArgChecker.notNull(timeToExpiration, "timeToExpiration");
    int nbExp = timeToExpiration.length;
    ArgChecker.isTrue(volatility.length == nbExp,
        "Volatility array length {} should be equal to the number of expiries {}", volatility.length, nbExp);
    ArgChecker.isTrue(volatility[0].length == 2 * delta.length + 1,
        "Volatility array {} should be equal to (2 * number of deltas) + 1, have {}",
        volatility[0].length, 2 * delta.length + 1);
    _volatilityTerm = new SmileDeltaParameters[nbExp];
    for (int loopexp = 0; loopexp < nbExp; loopexp++) {
      _volatilityTerm[loopexp] = new SmileDeltaParameters(timeToExpiration[loopexp], delta, volatility[loopexp]);
    }
    _timeInterpolator = DEFAULT_INTERPOLATOR_EXPIRY;
    ArgChecker.isTrue(_volatilityTerm[0].getVolatility().length > 1,
        "Need more than one volatility value to perform interpolation");
  }

  /**
   * Constructor from market data.
   * <p>
   * The market data consists of time to expiration, delta, ATM volatilities, risk reversal figures and 
   * strangle figures.
   * <p>
   * The range of delta is common to all time to expiration. 
   * {@code riskReversal} and {@code strangle} should be {@code n * m}, and the length of {@code atm} should {@code n}, 
   * where {@code n} is the length of {@code timeToExpiration} and {@code m} is the length of {@code delta}. 
   * <p>
   * The default interpolator is used to interpolate in the time dimension. 
   * 
   * @param timeToExpiration  the time to expiration of each volatility smile 
   * @param delta  the delta at which the volatilities are given 
   * @param atm  the ATM volatilities for each time to expiration
   * @param riskReversal  the risk reversal figures
   * @param strangle  the strangle figure
   */
  public SmileDeltaTermStructureParameters( 
      double[] timeToExpiration,  
      double[] delta, 
      double[] atm,  
      double[][] riskReversal,  
      double[][] strangle) {
    this(Long.toString(ATOMIC.getAndIncrement()), timeToExpiration, delta, atm, riskReversal, strangle);
  }

  /**
   * Constructor from name and market data.
   * <p>
   * The market data consists of time to expiration, delta, ATM volatilities, risk reversal figures and 
   * strangle figures.
   * <p>
   * The range of delta is common to all time to expiration. 
   * {@code riskReversal} and {@code strangle} should be {@code n * m}, and the length of {@code atm} should {@code n}, 
   * where {@code n} is the length of {@code timeToExpiration} and {@code m} is the length of {@code delta}. 
   * <p>
   * The default interpolator is used to interpolate in the time dimension. 
   * 
   * @param name  the name of smile parameter term structure 
   * @param timeToExpiration  the time to expiration of each volatility smile 
   * @param delta  the delta at which the volatilities are given 
   * @param atm  the ATM volatilities for each time to expiration
   * @param riskReversal  the risk reversal figures
   * @param strangle  the strangle figure
   */
  public SmileDeltaTermStructureParameters(
      String name,
      double[] timeToExpiration, 
      double[] delta, 
      double[] atm, 
      double[][] riskReversal,  
      double[][] strangle) {
    this(name, timeToExpiration, delta, atm, riskReversal, strangle, DEFAULT_INTERPOLATOR_EXPIRY);
  }

  /**
   * Constructor from market data and time interpolator.
   * <p>
   * The market data consists of time to expiration, delta, ATM volatilities, risk reversal figures and 
   * strangle figures.
   * <p>
   * The range of delta is common to all time to expiration. 
   * {@code riskReversal} and {@code strangle} should be {@code n * m}, and the length of {@code atm} should {@code n}, 
   * where {@code n} is the length of {@code timeToExpiration} and {@code m} is the length of {@code delta}. 
   * 
   * @param timeToExpiration  the time to expiration of each volatility smile 
   * @param delta  the delta at which the volatilities are given 
   * @param atm  the ATM volatilities for each time to expiration
   * @param riskReversal  the risk reversal figures
   * @param strangle  the strangle figure
   * @param timeInterpolator  the interpolator to be used in the time dimension
   */
  public SmileDeltaTermStructureParameters(
      double[] timeToExpiration,
      double[] delta,
      double[] atm,
      double[][] riskReversal,
      double[][] strangle,
      Interpolator1D timeInterpolator) {
    this(Long.toString(ATOMIC.getAndIncrement()), timeToExpiration, delta, atm, riskReversal, strangle,
        timeInterpolator);
  }

  /**
   * Constructor from name, market data and time interpolator.
   * <p>
   * The market data consists of time to expoiration, delta, ATM volatilities, risk reversal figures and 
   * strangle figures.
   * <p>
   * The range of delta is common to all time to expiration. 
   * {@code riskReversal} and {@code strangle} should be {@code n * m}, and the length of {@code atm} should {@code n}, 
   * where {@code n} is the length of {@code timeToExpiration} and {@code m} is the length of {@code delta}. 
   * 
   * @param name  the name of the smile parameter term structure 
   * @param timeToExpiration  the time to expiration of each volatility smile 
   * @param delta  the delta at which the volatilities are given 
   * @param atm  the ATM volatilities for each time to expiration
   * @param riskReversal  the risk reversal figures
   * @param strangle  the strangle figure
   * @param timeInterpolator  the interpolator to be used in the time dimension
   */
  public SmileDeltaTermStructureParameters(
      String name,
      double[] timeToExpiration,
      double[] delta,
      double[] atm,
      double[][] riskReversal,
      double[][] strangle,
      Interpolator1D timeInterpolator) {
    ArgChecker.notNull(delta, "delta");
    ArgChecker.notNull(atm, "ATM");
    ArgChecker.notNull(riskReversal, "risk reversal");
    ArgChecker.notNull(strangle, "strangle");
    _timeInterpolator = ArgChecker.notNull(timeInterpolator, "timeInterpolator");
    _name = ArgChecker.notNull(name, "name");
    _timeToExpiration = ArgChecker.notNull(timeToExpiration, "timeToExpiration");
    int nbExp = timeToExpiration.length;
    ArgChecker.isTrue(atm.length == nbExp, "ATM length should be coherent with time to expiration length");
    ArgChecker.isTrue(riskReversal.length == nbExp,
        "Risk reversal length should be coherent with time to expiration length");
    ArgChecker.isTrue(strangle.length == nbExp, "Strangle length should be coherent with time to expiration length");
    ArgChecker.isTrue(riskReversal[0].length == delta.length,
        "Risk reversal size should be coherent with time to delta length");
    ArgChecker.isTrue(strangle[0].length == delta.length, "Strangle size should be coherent with time to delta length");
    _volatilityTerm = new SmileDeltaParameters[nbExp];
    for (int loopexp = 0; loopexp < nbExp; loopexp++) {
      _volatilityTerm[loopexp] = new SmileDeltaParameters(timeToExpiration[loopexp], atm[loopexp], delta,
          riskReversal[loopexp], strangle[loopexp]);
    }
    ArgChecker.isTrue(_volatilityTerm[0].getVolatility().length > 1,
        "Need more than one volatility value to perform interpolation");
  }

  /**
   * Obtains a copy of this {@link SmileDeltaTermStructureParameters}. 
   * 
   * @return the copy
   */
  public SmileDeltaTermStructureParameters copy() {
    return new SmileDeltaTermStructureParameters(getName(), getVolatilityTerm(), getTimeInterpolator());
  }

  /**
   * Get smile at a given time. The smile is described by the volatilities at a given delta. The smile is obtained from the data by the given interpolator.
   * @param time The time to expiration.
   * @return The smile.
   */
  public SmileDeltaParameters getSmileForTime(final double time) {
    final int nbVol = _volatilityTerm[0].getVolatility().length;
    final int nbTime = _timeToExpiration.length;
    ArgChecker.isTrue(nbTime > 1, "Need more than one time value to perform interpolation");
    final double[] volatilityT = new double[nbVol];
    for (int loopvol = 0; loopvol < nbVol; loopvol++) {
      final double[] volDelta = new double[nbTime];
      for (int looptime = 0; looptime < nbTime; looptime++) {
        volDelta[looptime] = _volatilityTerm[looptime].getVolatility()[loopvol];
      }
      Interpolator1DDataBundle interpData =
          _timeInterpolator.getDataBundleFromSortedArrays(_timeToExpiration, volDelta);
      volatilityT[loopvol] = _timeInterpolator.interpolate(interpData, time);
    }
    final SmileDeltaParameters smile = new SmileDeltaParameters(time, _volatilityTerm[0].getDelta(), volatilityT);
    return smile;
  }

  /**
   * Get the smile at a given time and the sensitivities with respect to the volatilities.
   * @param time The time to expiration.
   * @param volatilityAtTimeSensitivity The sensitivity to the volatilities of the smile at the given time.
   * After the methods, it contains the volatility sensitivity to the data points.
   * @return The smile
   */
  public SmileAndBucketedSensitivities getSmileAndSensitivitiesForTime(final double time, final double[] volatilityAtTimeSensitivity) {
    final int nbVol = _volatilityTerm[0].getVolatility().length;
    ArgChecker.isTrue(volatilityAtTimeSensitivity.length == nbVol, "Sensitivity with incorrect size");
    ArgChecker.isTrue(nbVol > 1, "Need more than one volatility value to perform interpolation");
    final int nbTime = _timeToExpiration.length;
    ArgChecker.isTrue(nbTime > 1, "Need more than one time value to perform interpolation");
    final double[] volatilityT = new double[nbVol];
    final double[][] volatilitySensitivity = new double[nbTime][nbVol];
    for (int loopvol = 0; loopvol < nbVol; loopvol++) {
      final double[] volDelta = new double[nbTime];
      for (int looptime = 0; looptime < nbTime; looptime++) {
        volDelta[looptime] = _volatilityTerm[looptime].getVolatility()[loopvol];
      }
      Interpolator1DDataBundle interpData =
          _timeInterpolator.getDataBundleFromSortedArrays(_timeToExpiration, volDelta);
      final double[] volatilitySensitivityVol = _timeInterpolator.getNodeSensitivitiesForValue(interpData, time);
      for (int looptime = 0; looptime < nbTime; looptime++) {
        volatilitySensitivity[looptime][loopvol] = volatilitySensitivityVol[looptime] * volatilityAtTimeSensitivity[loopvol];
      }
      volatilityT[loopvol] = _timeInterpolator.interpolate(interpData, time);
    }
    final SmileDeltaParameters smile = new SmileDeltaParameters(time, _volatilityTerm[0].getDelta(), volatilityT);
    return new SmileAndBucketedSensitivities(smile, volatilitySensitivity);
  }

  /**
   * Gets the name of smile parameter term structure.
   * 
   * @return the name
   */
  public String getName() {
    return _name;
  }

  /**
   * Gets the times to expiration.
   * @return The times.
   */
  public double[] getTimeToExpiration() {
    return _timeToExpiration;
  }

  /**
   * Gets the number of expirations.
   * @return The number of expirations.
   */
  public int getNumberExpiration() {
    return _timeToExpiration.length;
  }

  /**
   * Gets the time interpolator
   * @return The time interpolator
   */
  public Interpolator1D getTimeInterpolator() {
    return _timeInterpolator;
  }

  /**
   * Gets the volatility smiles from delta.
   * @return The volatility smiles.
   */
  public SmileDeltaParameters[] getVolatilityTerm() {
    return _volatilityTerm;
  }

  /**
   * Gets the number of strikes (common to all dates).
   * @return The number of strikes.
   */
  public int getNumberStrike() {
    return _volatilityTerm[0].getVolatility().length;
  }

  /**
   * Gets delta (common to all time to expiration).
   * @return The delta.
   */
  public double[] getDelta() {
    return _volatilityTerm[0].getDelta();
  }

  /**
   * Gets put delta absolute value for all strikes. The ATM is 0.50 delta and the x call are transformed in 1-x put.
   * @return The delta.
   */
  public double[] getDeltaFull() {
    final int nbDelta = _volatilityTerm[0].getDelta().length;
    final double[] result = new double[2 * nbDelta + 1];
    for (int loopdelta = 0; loopdelta < nbDelta; loopdelta++) {
      result[loopdelta] = _volatilityTerm[0].getDelta()[loopdelta];
      result[nbDelta + 1 + loopdelta] = 1.0 - _volatilityTerm[0].getDelta()[nbDelta - 1 - loopdelta];
    }
    result[nbDelta] = 0.50;
    return result;
  }

  /**
   * Get the volatility from a triple.
   * @param tsf The Time, Strike, Forward triple, not null
   * @return The volatility.
   */
  @Override
  public Double getVolatility(final Triple<Double, Double, Double> tsf) {
    throw new UnsupportedOperationException();
  }

  @Override
  public VolatilityAndBucketedSensitivities getVolatilityAndSensitivities(final Triple<Double, Double, Double> tsf) {
    throw new UnsupportedOperationException();
  }

  public BlackForexTermStructureParameters toTermStructureOnlyData(final Interpolator1D interpolator) {
    ArgChecker.notNull(interpolator, "interpolator");
    final int n = _timeToExpiration.length;
    final double[] timesToExpiry = new double[n];
    System.arraycopy(_timeToExpiration, 0, timesToExpiry, 0, n);
    final double[] vols = new double[n];
    final int atmIndex = (_volatilityTerm[0].getVolatility().length - 1) / 2;
    for (int i = 0; i < n; i++) {
      vols[i] = _volatilityTerm[i].getVolatility()[atmIndex];
    }
    return new BlackForexTermStructureParameters(InterpolatedDoublesCurve.fromSorted(timesToExpiry, vols, interpolator));
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _name.hashCode();
    result = prime * result + Arrays.hashCode(_timeToExpiration);
    result = prime * result + Arrays.hashCode(_volatilityTerm);
    result = prime * result + _timeInterpolator.hashCode();
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
    final SmileDeltaTermStructureParameters other = (SmileDeltaTermStructureParameters) obj;
    if (!Objects.equals(_name, other._name)) {
      return false;
    }
    if (!Arrays.equals(_timeToExpiration, other._timeToExpiration)) {
      return false;
    }
    if (!Arrays.equals(_volatilityTerm, other._volatilityTerm)) {
      return false;
    }
    if (!Objects.equals(_timeInterpolator, other._timeInterpolator)) {
      return false;
    }
    return true;
  }

}

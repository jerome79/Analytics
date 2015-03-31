/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.strata.collect.ArgChecker;
import com.opengamma.strata.collect.tuple.DoublesPair;

/**
 * Utilities to manipulate present value sensitivities.
 * <p>
 * This is a thread-safe static utility class.
 */
public class InterestRateCurveSensitivityUtils {

  /**
   * Restricted constructor.
   */
  protected InterestRateCurveSensitivityUtils() {
    super();
  }

  /**
   * Takes a list of curve sensitivities (i.e. an unordered list of pairs of times and sensitivities) and returns a list order by ascending
   * time, and with sensitivities that occur at the same time netted (zero net sensitivities are removed)
   * @param old An unordered list of pairs of times and sensitivities
   * @param relTol Relative tolerance - if the net divided by gross sensitivity is less than this it is ignored/removed
   * @param absTol Absolute tolerance  - is the net sensitivity is less than this it is ignored/removed
   * @return A time ordered netted list
   */
  static final List<DoublesPair> clean(final List<DoublesPair> old, final double relTol, final double absTol) {

    ArgChecker.notNull(old, "null list");
    ArgChecker.isTrue(relTol >= 0.0 && absTol >= 0.0, "Tolerances must be greater than zero");
    if (old.size() == 0) {
      return new ArrayList<>();
    }
    final List<DoublesPair> res = new ArrayList<>();
    final DoublesPair[] sort = old.toArray(new DoublesPair[old.size()]);
    Arrays.sort(sort);
    final DoublesPair pairOld = sort[0];
    double tOld = pairOld.getFirst();
    double sum = pairOld.getSecond();
    double scale = Math.abs(sum);
    double t = tOld;
    for (int i = 1; i < sort.length; i++) {
      final DoublesPair pair = sort[i];
      t = pair.getFirst();
      if (t > tOld) {
        if (Math.abs(sum) > absTol && Math.abs(sum) / scale > relTol) {
          res.add(DoublesPair.of(tOld, sum));
        }
        tOld = t;
        sum = pair.getSecond();
        scale = Math.abs(sum);
      } else {
        sum += pair.getSecond();
        scale += Math.abs(pair.getSecond());
      }
    }

    if (Math.abs(sum) > absTol && Math.abs(sum) / scale > relTol) {
      res.add(DoublesPair.of(t, sum));
    }

    return res;
  }

  /**
   * Takes a map of curve sensitivities (i.e. a map between curve names and a unordered lists of pairs of times and sensitivities)
   *  and returns a similar map where the lists order by ascending time, and with sensitivities that occur at the same time netted
   *  (zero net sensitivities are removed)
   * @param old A map between curve names and unordered lists of pairs of times and sensitivities
   * @param relTol Relative tolerance - if the net divided by gross sensitivity is less than this it is ignored/removed
   * @param absTol Absolute tolerance  - is the net sensitivity is less than this it is ignored/removed
   * @return A map between curve names and time ordered netted lists
   */
  public static Map<String, List<DoublesPair>> clean(final Map<String, List<DoublesPair>> old, final double relTol, final double absTol) {
    final Map<String, List<DoublesPair>> res = new HashMap<>();
    for (final Map.Entry<String, List<DoublesPair>> entry : old.entrySet()) {
      final List<DoublesPair> cleanList = clean(entry.getValue(), relTol, absTol);
      if (!cleanList.isEmpty()) {
        res.put(entry.getKey(), cleanList);
      }
    }
    return res;
  }

  /**
   * Add two list representing sensitivities into one. No attempt is made to net off sensitivities occurring at the same time - Use clean()
   * to do this
   * @param sensi1 First list of sensitivities
   * @param sensi2 Second list of sensitivities
   * @return combined list
   */
  public static List<DoublesPair> addSensitivity(final List<DoublesPair> sensi1, final List<DoublesPair> sensi2) {
    final List<DoublesPair> result = new ArrayList<>(sensi1);
    result.addAll(sensi2);
    return result;
  }

  //-------------------------------------------------------------------------
  /**
   * Add two maps representing sensitivities into one.
   * 
   * @param sensi1  the first sensitivity, not null
   * @param sensi2  the second sensitivity, not null
   * @return the total sensitivity, not null
   */
  public static Map<String, List<DoublesPair>> addSensitivity(final Map<String, List<DoublesPair>> sensi1, final Map<String, List<DoublesPair>> sensi2) {
    ArgChecker.notNull(sensi1, "sensitivity");
    ArgChecker.notNull(sensi2, "sensitivity");
    final Map<String, List<DoublesPair>> result = new HashMap<>();
    for (final Map.Entry<String, List<DoublesPair>> entry : sensi1.entrySet()) {
      final String name = entry.getKey();
      if (sensi2.containsKey(name)) {
        result.put(name, addSensitivity(entry.getValue(), sensi2.get(name)));
      } else {
        result.put(name, entry.getValue());
      }
    }
    for (final Map.Entry<String, List<DoublesPair>> entry : sensi2.entrySet()) {
      final String name = entry.getKey();
      if (!result.containsKey(name)) {
        result.put(name, entry.getValue());
      }
    }
    return result;
  }

  /**
   * Add the list representing the sensitivity to one curve to the map of sensitivities to several curves.
   * @param sensi The multi-curves sensitivity. Not null.
   * @param curveName  The name of the curve the sensitivity of which is added. Not null.
   * @param list The sensitivity as a list. Not null.
   * @return The total sensitivity, not null
   */
  public static Map<String, List<DoublesPair>> addSensitivity(final Map<String, List<DoublesPair>> sensi, final String curveName, final List<DoublesPair> list) {
    ArgChecker.notNull(sensi, "sensitivity");
    ArgChecker.notNull(list, "sensitivity");
    final Map<String, List<DoublesPair>> result = new HashMap<>();
    for (final Map.Entry<String, List<DoublesPair>> entry : sensi.entrySet()) {
      final String name = entry.getKey();
      if (name.equals(curveName)) {
        result.put(name, addSensitivity(entry.getValue(), list));
      } else {
        result.put(name, entry.getValue());
      }
    }
    if (!result.containsKey(curveName)) {
      result.put(curveName, list);
    }
    return result;
  }

  //TODO smarter way to do this?
  public static Map<String, List<DoublesPair>> addSensitivity(final Map<String, List<DoublesPair>> sensi1, final Map<String, List<DoublesPair>> sensi2, final Map<String, List<DoublesPair>> sensi3) {
    return addSensitivity(addSensitivity(sensi1, sensi2), sensi3);
  }

  /**
   * Multiply a sensitivity map by a common factor.
   * 
   * @param sensitivity  the original sensitivity, not null
   * @param factor  the multiplicative factor, not null
   * @return the multiplied sensitivity, not null
   */
  public static Map<String, List<DoublesPair>> multiplySensitivity(final Map<String, List<DoublesPair>> sensitivity, final double factor) {
    ArgChecker.notNull(sensitivity, "sensitivity");
    final Map<String, List<DoublesPair>> result = new HashMap<>();
    for (final Map.Entry<String, List<DoublesPair>> entry : sensitivity.entrySet()) {
      result.put(entry.getKey(), multiplySensitivity(entry.getValue(), factor));
    }
    return result;
  }

  public static List<DoublesPair> multiplySensitivity(final List<DoublesPair> sensitivity, final double factor) {
    ArgChecker.notNull(sensitivity, "sensitivity");
    final List<DoublesPair> curveSensi = new ArrayList<>();
    for (final DoublesPair pair : sensitivity) {
      curveSensi.add(DoublesPair.of(pair.getFirst(), pair.getSecond() * factor));
    }
    return curveSensi;
  }

  /**
   * Compare two lists of sensitivities with a given tolerance. The tolerance is used for both the time and the value. The two sensitivities are suppose to be in the same time order.
   * @param sensi1 The first sensitivity (as a list).
   * @param sensi2 The second sensitivity (as a list).
   * @param tolerance The tolerance.
   * @return True if the difference is below the tolerance and False if not.
   */
  public static boolean compare(final List<DoublesPair> sensi1, final List<DoublesPair> sensi2, final double tolerance) {
    if (sensi1.size() != sensi2.size()) {
      return false;
    }
    for (int looptime = 0; looptime < sensi1.size(); looptime++) {
      if ((Math.abs(sensi1.get(looptime).getFirst() - sensi2.get(looptime).getFirst()) > tolerance) || (Math.abs(sensi1.get(looptime).getSecond() - sensi2.get(looptime).getSecond()) > tolerance)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Compare two maps of sensitivities with a given tolerance. The tolerance is used for both the time and the value. The two sensitivities are suppose to be in the same time order.
   * @param sensi1 The first sensitivity (as a map).
   * @param sensi2 The second sensitivity (as a map).
   * @param tolerance The tolerance.
   * @return True if the difference is below the tolerance and False if not. If the curves are not the same it returns False.
   */
  public static boolean compare(final Map<String, List<DoublesPair>> sensi1, final Map<String, List<DoublesPair>> sensi2, final double tolerance) {
    ArgChecker.notNull(sensi1, "sensitivity");
    ArgChecker.notNull(sensi2, "sensitivity");
    for (final Map.Entry<String, List<DoublesPair>> entry : sensi1.entrySet()) {
      if (sensi2.containsKey(entry.getKey())) {
        if (!compare(entry.getValue(), sensi2.get(entry.getKey()), tolerance)) {
          return false;
        }
      } else {
        return false;
      }
    }
    for (final String name : sensi2.keySet()) {
      if (!(sensi1.containsKey(name))) {
        return false;
      }
    }
    return true;
  }
}

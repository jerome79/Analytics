/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation.weight;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 */
public final class WeightingFunctionFactory {
  /** Sine weighting function name */
  public static final String SINE_WEIGHTING_FUNCTION_NAME = "SineWeightingFunction";
  /** Linear weighting function name */
  public static final String LINEAR_WEIGHTING_FUNCTION_NAME = "LinearWeightingFunction";
  /** Sine weighting function */
  public static final SineWeightingFunction SINE_WEIGHTING_FUNCTION = SineWeightingFunction.getInstance();
  /** Linear weighting function */
  public static final LinearWeightingFunction LINEAR_WEIGHTING_FUNCTION = LinearWeightingFunction.getInstance();
  private static final Map<String, WeightingFunction> s_staticInstances;
  private static final Map<Class<?>, String> s_instanceNames;

  static {
    final Map<String, WeightingFunction> staticInstances = new HashMap<>();
    final Map<Class<?>, String> instanceNames = new HashMap<>();
    staticInstances.put(LINEAR_WEIGHTING_FUNCTION_NAME, LINEAR_WEIGHTING_FUNCTION);
    instanceNames.put(LinearWeightingFunction.class, LINEAR_WEIGHTING_FUNCTION_NAME);
    staticInstances.put(SINE_WEIGHTING_FUNCTION_NAME, SINE_WEIGHTING_FUNCTION);
    instanceNames.put(SineWeightingFunction.class, SINE_WEIGHTING_FUNCTION_NAME);
    s_staticInstances = new HashMap<>(staticInstances);
    s_instanceNames = new HashMap<>(instanceNames);
  }

  private WeightingFunctionFactory() {
  }

  public static WeightingFunction getWeightingFunction(final String weightingFunctionName) {
    final WeightingFunction function = s_staticInstances.get(weightingFunctionName);
    if (function != null) {
      return function;
    }
    throw new IllegalArgumentException("Weighting function not handled: " + weightingFunctionName);
  }

  public static String getWeightingFunctionName(final WeightingFunction function) {
    if (function == null) {
      return null;
    }
    return s_instanceNames.get(function.getClass());
  }
}

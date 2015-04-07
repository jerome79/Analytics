/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.local;

import java.util.Objects;

import com.opengamma.analytics.financial.model.tree.RecombiningTree;
import com.opengamma.strata.collect.ArgChecker;

/**
 * 
 */
public class ImpliedTreeResult {
  private final RecombiningTree<Double> _spotPrices;
  private final RecombiningTree<Double> _localVolatilities;

  public ImpliedTreeResult(final RecombiningTree<Double> spotPrices, final RecombiningTree<Double> localVolatilities) {
    ArgChecker.notNull(spotPrices, "spot price tree");
    ArgChecker.notNull(localVolatilities, "local volatility tree");
    if (spotPrices.getDepth() - 1 != localVolatilities.getDepth()) {
      throw new IllegalArgumentException("Spot price tree must have depth equal to the depth of the local volatility tree plus one; have " + spotPrices.getDepth() + " and "
          + localVolatilities.getDepth());
    }
    _spotPrices = spotPrices;
    _localVolatilities = localVolatilities;
  }

  public RecombiningTree<Double> getSpotPriceTree() {
    return _spotPrices;
  }

  public RecombiningTree<Double> getLocalVolatilityTree() {
    return _localVolatilities;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _localVolatilities.hashCode();
    result = prime * result + _spotPrices.hashCode();
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
    final ImpliedTreeResult other = (ImpliedTreeResult) obj;
    return Objects.equals(_localVolatilities, other._localVolatilities) && Objects.equals(_spotPrices, other._spotPrices);
  }
}

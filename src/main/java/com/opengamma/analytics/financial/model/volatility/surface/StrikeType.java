/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.surface;

import com.opengamma.strata.collect.tuple.ObjectDoublePair;

/**
 * The type of strike.
 * <p>
 * The strike of option instruments is represented in different ways. 
 * For example, the strike types include delta, moneyness, logmoneyness, and strike itself. 
 */
public interface StrikeType {

  /**
   * Creates an new instance of the same strike type with value. 
   * 
   * @param value  the value
   * @return  the strike type
   */
  public StrikeType with(final double value);

  /**
   * Obtains the name of the strike type. 
   * 
   * @return the name
   */
  public String type();

  /**
   * Obtains the name of the strike type and the value. 
   * 
   * @return the name and value
   */
  public default ObjectDoublePair<String> typeAndValue() {
    return ObjectDoublePair.of(type(), getValue());
  }

  /**
  * Obtains the value expressed in the strike type. 
  * 
  * @return the value
  */
  double getValue();

}

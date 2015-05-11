/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.curve;

import java.time.ZonedDateTime;
import java.util.Set;

/**
 * @param <T> Type of the data 
 */
public interface DiscountCurveModel<T> {

  YieldAndDiscountCurve getCurve(Set<T> data, ZonedDateTime date);
}

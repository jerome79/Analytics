/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.analytics.convention.daycount.DayCount;
import com.opengamma.analytics.convention.daycount.DayCounts;
import com.opengamma.strata.basics.currency.Currency;

/**
 * Description of ON indexes available for tests.
 */
public final class IndexONMaster {

  /**
   * The method unique instance.
   */
  private static final IndexONMaster INSTANCE = new IndexONMaster();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static IndexONMaster getInstance() {
    return INSTANCE;
  }

  /**
   * The map with the list of Ibor Indexes and their conventions.
   */
  private final Map<String, IndexON> _on;

  /**
   * Private constructor.
   */
  private IndexONMaster() {
    final DayCount act360 = DayCounts.ACT_360;
    final DayCount act365 = DayCounts.ACT_365F;
    final DayCount Bus252 = DayCounts.BUSINESS_252;
    _on = new HashMap<>();
    _on.put("EONIA", new IndexON("EONIA", Currency.EUR, act360, 0));
    _on.put("FED FUND", new IndexON("FED FUND", Currency.USD, act360, 1));
    _on.put("SONIA", new IndexON("SONIA", Currency.GBP, act365, 0));
    _on.put("RBA ON", new IndexON("RBA ON", Currency.AUD, act365, 0));
    _on.put("DKK TN", new IndexON("DKK TN", Currency.DKK, act360, 1));
    _on.put("TONAR", new IndexON("TONAR", Currency.JPY, act365, 0));
    _on.put("CDI", new IndexON("CDI", Currency.BRL, Bus252, 1));
  }

  public IndexON getIndex(final String name) {
    final IndexON indexNoCalendar = _on.get(name);
    if (indexNoCalendar == null) {
      throw new RuntimeException("Could not get ON index for " + name);
    }
    return new IndexON(indexNoCalendar.getName(), indexNoCalendar.getCurrency(), indexNoCalendar.getDayCount(), indexNoCalendar.getPublicationLag());
  }

}

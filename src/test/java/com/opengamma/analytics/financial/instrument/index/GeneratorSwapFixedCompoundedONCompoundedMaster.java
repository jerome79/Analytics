/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.analytics.convention.businessday.BusinessDayConvention;
import com.opengamma.analytics.convention.businessday.BusinessDayConventions;
import com.opengamma.analytics.convention.daycount.DayCount;
import com.opengamma.analytics.convention.daycount.DayCounts;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.basics.date.HolidayCalendars;

/**
 * A list of generators for swaps Fixed/ON available for tests.
 */
public class GeneratorSwapFixedCompoundedONCompoundedMaster {

  /**
   * The method unique instance.
   */
  private static final GeneratorSwapFixedCompoundedONCompoundedMaster INSTANCE = new GeneratorSwapFixedCompoundedONCompoundedMaster();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static GeneratorSwapFixedCompoundedONCompoundedMaster getInstance() {
    return INSTANCE;
  }

  /**
   * The map with the list of names and the swap generators.
   */
  private final Map<String, GeneratorSwapFixedCompoundedONCompounded> _generatorSwap;

  /**
   * Private constructor.
   */
  private GeneratorSwapFixedCompoundedONCompoundedMaster() {
    final IndexONMaster indexONMaster = IndexONMaster.getInstance();
    final HolidayCalendar baseCalendar = HolidayCalendars.NO_HOLIDAYS;
    final DayCount bus252 = DayCounts.BUSINESS_252;
    final BusinessDayConvention modFol = BusinessDayConventions.MODIFIED_FOLLOWING;
    _generatorSwap = new HashMap<>();
    final IndexON cdi = indexONMaster.getIndex("CDI");
    _generatorSwap.put("BRLCDI", new GeneratorSwapFixedCompoundedONCompounded("BRLCDI", cdi, bus252, modFol, true, 0, 0, baseCalendar));

  }

  public GeneratorSwapFixedCompoundedONCompounded getGenerator(final String name, final HolidayCalendar cal) {
    final GeneratorSwapFixedCompoundedONCompounded generatorNoCalendar = _generatorSwap.get(name);
    if (generatorNoCalendar == null) {
      throw new RuntimeException("Could not get Swap Fixed/ON Compounded generator for " + name);
    }
    final IndexON indexNoCalendar = generatorNoCalendar.getIndex();
    final IndexON index = new IndexON(indexNoCalendar.getName(), indexNoCalendar.getCurrency(), indexNoCalendar.getDayCount(), indexNoCalendar.getPublicationLag());
    return new GeneratorSwapFixedCompoundedONCompounded(generatorNoCalendar.getName(), index, generatorNoCalendar.getFixedLegDayCount(), generatorNoCalendar.getBusinessDayConvention(),
        generatorNoCalendar.isEndOfMonth(), generatorNoCalendar.getSpotLag(), generatorNoCalendar.getPaymentLag(), cal);
  }

}

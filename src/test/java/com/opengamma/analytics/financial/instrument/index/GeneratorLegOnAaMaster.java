/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import java.time.Period;
import java.util.HashMap;
import java.util.Map;

import com.opengamma.analytics.convention.StubType;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.date.BusinessDayConventions;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.basics.date.HolidayCalendars;

/**
 * A list of swap generators that can be used in the tests.
 */
public final class GeneratorLegOnAaMaster {

  /**
   * The method unique instance.
   */
  private static final GeneratorLegOnAaMaster INSTANCE = new GeneratorLegOnAaMaster();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static GeneratorLegOnAaMaster getInstance() {
    return INSTANCE;
  }

  /**
   * The map with the list of names and the swap generators.
   */
  private final Map<String, GeneratorLegONArithmeticAverage> _generatorLeg;

  /**
   * Private constructor.
   */
  private GeneratorLegOnAaMaster() {
    final IndexONMaster indexONMaster = IndexONMaster.getInstance();
    final HolidayCalendar baseCalendar = HolidayCalendars.NO_HOLIDAYS;
    _generatorLeg = new HashMap<>();
    IndexON fedFund = indexONMaster.getIndex("FED FUND");
    _generatorLeg.put("USDFEDFUNDAA3M", new GeneratorLegONArithmeticAverage("USDFEDFUNDAA3M", Currency.USD, fedFund, 
        Period.ofMonths(3), 2, 0, BusinessDayConventions.MODIFIED_FOLLOWING, true, StubType.SHORT_START, false,
        baseCalendar, baseCalendar));
  }

  public GeneratorLegONArithmeticAverage getGenerator(final String name, final HolidayCalendar cal) {
    final GeneratorLegONArithmeticAverage generatorNoCalendar = _generatorLeg.get(name);
    if (generatorNoCalendar == null) {
      throw new RuntimeException("Could not get Ibor index for " + name);
    }
    return new GeneratorLegONArithmeticAverage(generatorNoCalendar.getName(), generatorNoCalendar.getCurrency(), 
        generatorNoCalendar.getIndexON(), generatorNoCalendar.getPaymentPeriod(), generatorNoCalendar.getSpotOffset(), 
        generatorNoCalendar.getPaymentOffset(), generatorNoCalendar.getBusinessDayConvention(), 
        generatorNoCalendar.isEndOfMonth(), generatorNoCalendar.getStubType(), generatorNoCalendar.isExchangeNotional(), 
        cal, cal);
  }

}

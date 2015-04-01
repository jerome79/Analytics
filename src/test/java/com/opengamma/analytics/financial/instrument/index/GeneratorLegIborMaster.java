/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import java.time.Period;
import java.util.HashMap;
import java.util.Map;

import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.basics.date.HolidayCalendars;
import com.opengamma.strata.basics.schedule.StubConvention;

/**
 * A list of swap generators that can be used in the tests.
 */
public final class GeneratorLegIborMaster {

  /**
   * The method unique instance.
   */
  private static final GeneratorLegIborMaster INSTANCE = new GeneratorLegIborMaster();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static GeneratorLegIborMaster getInstance() {
    return INSTANCE;
  }

  /**
   * The map with the list of names and the swap generators.
   */
  private final Map<String, GeneratorLegIbor> _generatorSwap;

  /**
   * The list of Ibor indexes for test purposes.
   */
  private final IndexIborMaster _iborIndexMaster;

  /**
   * Private constructor.
   */
  private GeneratorLegIborMaster() {
    _iborIndexMaster = IndexIborMaster.getInstance();
    final HolidayCalendar baseCalendar = HolidayCalendars.NO_HOLIDAYS;
    _generatorSwap = new HashMap<>();
    IborIndex usdlibor3M = _iborIndexMaster.getIndex("USDLIBOR3M");
    IborIndex usdlibor6M = _iborIndexMaster.getIndex("USDLIBOR6M");
    IborIndex eurEuribor3M = _iborIndexMaster.getIndex("EURIBOR3M");
    IborIndex eurEuribor6M = _iborIndexMaster.getIndex("EURIBOR6M");
    _generatorSwap.put("USDLIBOR3M", new GeneratorLegIbor("USDLIBOR3M", Currency.USD, usdlibor3M, Period.ofMonths(3),
        2, 0, usdlibor3M.getBusinessDayConvention(), true, StubConvention.SHORT_INITIAL, false, baseCalendar, baseCalendar));
    _generatorSwap.put("USDLIBOR3M_X", new GeneratorLegIbor("USDLIBOR3M_X", Currency.USD, usdlibor3M, Period.ofMonths(3), 
        2, 0, usdlibor3M.getBusinessDayConvention(), true, StubConvention.SHORT_INITIAL, true, baseCalendar, baseCalendar));
    _generatorSwap.put("USDLIBOR6M", new GeneratorLegIbor("USDLIBOR6M", Currency.USD, usdlibor6M, Period.ofMonths(6), 
        2, 0, usdlibor6M.getBusinessDayConvention(), true, StubConvention.SHORT_INITIAL, false, baseCalendar, baseCalendar));
    _generatorSwap.put("EUREURIBOR3M", new GeneratorLegIbor("EUREURIBOR3M", Currency.EUR, eurEuribor3M, 
        Period.ofMonths(3), 2, 0, eurEuribor3M.getBusinessDayConvention(), true, StubConvention.SHORT_INITIAL, false, 
        baseCalendar, baseCalendar));
    _generatorSwap.put("EUREURIBOR3M_X", new GeneratorLegIbor("EUREURIBOR3M_X", Currency.EUR, eurEuribor3M, 
        Period.ofMonths(3), 2, 0, eurEuribor3M.getBusinessDayConvention(), true, StubConvention.SHORT_INITIAL, true, 
        baseCalendar, baseCalendar));
    _generatorSwap.put("EUREURIBOR6M", new GeneratorLegIbor("EUREURIBOR6M", Currency.EUR, eurEuribor6M, 
        Period.ofMonths(6), 2, 0, eurEuribor6M.getBusinessDayConvention(), true, StubConvention.SHORT_INITIAL, false, 
        baseCalendar, baseCalendar));
  }

  public GeneratorLegIbor getGenerator(final String name, final HolidayCalendar cal) {
    final GeneratorLegIbor generatorNoCalendar = _generatorSwap.get(name);
    if (generatorNoCalendar == null) {
      throw new RuntimeException("Could not get Ibor index for " + name);
    }
    return new GeneratorLegIbor(generatorNoCalendar.getName(), generatorNoCalendar.getCurrency(), 
        generatorNoCalendar.getIndexIbor(), generatorNoCalendar.getPaymentPeriod(), generatorNoCalendar.getSpotOffset(), 
        generatorNoCalendar.getPaymentOffset(), generatorNoCalendar.getBusinessDayConvention(), 
        generatorNoCalendar.isEndOfMonth(), generatorNoCalendar.getStubType(), generatorNoCalendar.isExchangeNotional(), 
        cal, cal);
  }

}

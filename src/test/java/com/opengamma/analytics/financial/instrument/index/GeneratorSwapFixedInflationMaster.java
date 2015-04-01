/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.strata.basics.date.BusinessDayConvention;
import com.opengamma.strata.basics.date.BusinessDayConventions;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.basics.date.HolidayCalendars;

/**
 * 
 */
public final class GeneratorSwapFixedInflationMaster {

  /**
   * The method unique instance.
   */
  private static final GeneratorSwapFixedInflationMaster INSTANCE = new GeneratorSwapFixedInflationMaster();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static GeneratorSwapFixedInflationMaster getInstance() {
    return INSTANCE;
  }

  /**
   * The map with the list of names and the swap generators.
   */
  private final Map<String, GeneratorSwapFixedInflationZeroCoupon> _generatorSwap;

  /**
   * Private constructor.
   */
  private GeneratorSwapFixedInflationMaster() {
    final IndexPriceMaster priceIndexMaster = IndexPriceMaster.getInstance();

//    final DoubleTimeSeries<ZonedDateTime> eurPriceIndexTimeSerie = MulticurveProviderDiscountDataSets.euroHICPXFrom2009();
//    final DoubleTimeSeries<ZonedDateTime> usPriceIndexTimeSerie = MulticurveProviderDiscountDataSets.usCpiFrom2009();
//    final DoubleTimeSeries<ZonedDateTime> ukPriceIndexTimeSerie = MulticurveProviderDiscountDataSets.ukRpiFrom2010();

    final BusinessDayConvention modFol = BusinessDayConventions.MODIFIED_FOLLOWING;
    final HolidayCalendar baseCalendar = HolidayCalendars.NO_HOLIDAYS;
    final HolidayCalendar londonBaseCalendar = HolidayCalendars.GBLO;
    final boolean endOfMonth = true;
    final int monthLag = 3;
    final int spotLag = 2;
    final boolean linear = true;
    final boolean piecewiseconstant = false;
    _generatorSwap = new HashMap<>();
    _generatorSwap.put("EURHICP",
        new GeneratorSwapFixedInflationZeroCoupon("EUR HICP", priceIndexMaster.getIndex("EURHICP"), modFol, 
            baseCalendar, endOfMonth, monthLag, spotLag, piecewiseconstant));
    _generatorSwap.put("UKRPI",
        new GeneratorSwapFixedInflationZeroCoupon("UK RPI", priceIndexMaster.getIndex("UKRPI"), modFol, 
            londonBaseCalendar, endOfMonth, monthLag, spotLag, piecewiseconstant));
    _generatorSwap.put("USCPI",
        new GeneratorSwapFixedInflationZeroCoupon("US CPI", priceIndexMaster.getIndex("USCPI"), modFol, 
            baseCalendar, endOfMonth, monthLag, spotLag, linear));
  }

  public GeneratorSwapFixedInflationZeroCoupon getGenerator(final String name) {
    final GeneratorSwapFixedInflationZeroCoupon generatorNoCalendar = _generatorSwap.get(name);
    if (generatorNoCalendar == null) {
      throw new IllegalStateException("Could not get price index index for " + name);
    }
    return new GeneratorSwapFixedInflationZeroCoupon(generatorNoCalendar.getName(), generatorNoCalendar.getIndexPrice(), generatorNoCalendar.getBusinessDayConvention(),
        generatorNoCalendar.getCalendar(), generatorNoCalendar.isEndOfMonth(), generatorNoCalendar.getMonthLag(), generatorNoCalendar.getSpotLag(),
        generatorNoCalendar.isLinear());
  }
}

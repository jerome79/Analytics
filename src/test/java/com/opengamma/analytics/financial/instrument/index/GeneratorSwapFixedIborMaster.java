/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import java.time.Period;
import java.util.HashMap;
import java.util.Map;

import com.opengamma.analytics.convention.daycount.DayCounts;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.basics.date.HolidayCalendars;

/**
 * A list of swap generators that can be used in the tests.
 */
public final class GeneratorSwapFixedIborMaster {

  /**
   * Reference to a AUD fix vs BBSW 6M float swap.
   */
  public static final String AUD6MBBSW6M = "AUD6MBBSW6M";

  /**
   * Reference to a AUD 3M fix vs BBSW 3M float swap.
   */
  public static final String AUD3MBBSW3M = "AUD3MBBSW3M";

  /**
   * Reference to a JPY 6M fix vs LIBOR 6M float swap.
   */
  public static final String JPY6MLIBOR6M = "JPY6MLIBOR6M";

  /**
   * Reference to a JPY 6M fix vs LIBOR 3M float swap.
   */
  public static final String JPY6MLIBOR3M = "JPY6MLIBOR3M";

  /**
   * Reference to a DKK 1Y  fix vs CIBOR 6M float
   */
  public static final String DKK1YCIBOR6M = "DKK1YCIBOR6M";

  /**
   * Reference to a GBP 6M fix vs LIBOR 6M float
   */
  public static final String GBP6MLIBOR6M = "GBP6MLIBOR6M";
   /** Reference to a GBP 3M fix vs LIBOR 3M float */
  public static final String GBP3MLIBOR3M = "GBP3MLIBOR3M";

  /**
   * Reference to a GBP 1Y fix vs LIBOR 3M float
   */
  public static final String GBP1YLIBOR3M = "GBP1YLIBOR3M";

  /**
   * Reference to a EUR 1Y fix vs LIBOR 6M float
   */
  public static final String EUR1YEURIBOR6M = "EUR1YEURIBOR6M";

  /**
   * Reference to a EUR 1Y fix vs LIBOR 3M float
   */
  public static final String EUR1YEURIBOR3M = "EUR1YEURIBOR3M";

  /**
   * Reference to a USD 6M fix vs LIBOR 6M float
   */
  public static final String USD6MLIBOR6M = "USD6MLIBOR6M";

  /** Reference to a USD 1Y ACT/360 fix vs LIBOR 1M float */
  public static final String USD1YLIBOR1M = "USD1YLIBOR1M";
  /** Reference to a USD 1Y ACT/360 fix vs LIBOR 3M float */
  public static final String USD1YLIBOR3M = "USD1YLIBOR3M";

  /**
   * Reference to a USD 6M fix vs LIBOR 3M float
   */
  public static final String USD6MLIBOR3M = "USD6MLIBOR3M";

  /**
   * Reference to a USD 6M fix vs LIBOR 1M float
   */
  public static final String USD6MLIBOR1M = "USD6MLIBOR1M";

  /**
   * The method unique instance.
   */
  private static final GeneratorSwapFixedIborMaster INSTANCE = new GeneratorSwapFixedIborMaster();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static GeneratorSwapFixedIborMaster getInstance() {
    return INSTANCE;
  }

  /**
   * The map with the list of names and the swap generators.
   */
  private final Map<String, GeneratorSwapFixedIbor> _generatorSwap;

  /**
   * The list of Ibor indexes for test purposes.
   */
  private final IndexIborMaster _iborIndexMaster;

  /**
   * Private constructor.
   */
  private GeneratorSwapFixedIborMaster() {
    _iborIndexMaster = IndexIborMaster.getInstance();
    final HolidayCalendar baseCalendar = HolidayCalendars.NO_HOLIDAYS;
    _generatorSwap = new HashMap<>();
    _generatorSwap.put(USD6MLIBOR1M,
                       new GeneratorSwapFixedIbor(USD6MLIBOR1M,
                                                  Period.ofMonths(6),
                                                  DayCounts.THIRTY_U_360,
                                                  _iborIndexMaster.getIndex(IndexIborMaster.USDLIBOR1M),
                                                  baseCalendar));
    _generatorSwap.put(USD6MLIBOR3M,
                       new GeneratorSwapFixedIbor(USD6MLIBOR3M,
                                                  Period.ofMonths(6),
                                                  DayCounts.THIRTY_U_360,
                                                  _iborIndexMaster.getIndex(IndexIborMaster.USDLIBOR3M),
                                                  baseCalendar));
    _generatorSwap.put(USD1YLIBOR1M,
        new GeneratorSwapFixedIbor(USD1YLIBOR1M, Period.ofMonths(12), DayCounts.ACT_360,
            _iborIndexMaster.getIndex(IndexIborMaster.USDLIBOR1M), baseCalendar));
    _generatorSwap.put(USD1YLIBOR3M,
                       new GeneratorSwapFixedIbor(USD1YLIBOR3M,
                                                  Period.ofMonths(12),
                                                  DayCounts.ACT_360,
                                                  _iborIndexMaster.getIndex(IndexIborMaster.USDLIBOR3M),
                                                  baseCalendar));
    _generatorSwap.put(USD6MLIBOR6M,
                       new GeneratorSwapFixedIbor(USD6MLIBOR6M,
                                                  Period.ofMonths(6),
                                                  DayCounts.THIRTY_U_360,
                                                  _iborIndexMaster.getIndex(IndexIborMaster.USDLIBOR6M),
                                                  baseCalendar));
    _generatorSwap.put(EUR1YEURIBOR3M,
                       new GeneratorSwapFixedIbor(EUR1YEURIBOR3M,
                                                  Period.ofMonths(12),
                                                  DayCounts.THIRTY_U_360,
                                                  _iborIndexMaster.getIndex(IndexIborMaster.EURIBOR3M),
                                                  baseCalendar));
    _generatorSwap.put(EUR1YEURIBOR6M,
                       new GeneratorSwapFixedIbor(EUR1YEURIBOR6M,
                                                  Period.ofMonths(12),
                                                  DayCounts.THIRTY_U_360,
                                                  _iborIndexMaster.getIndex(IndexIborMaster.EURIBOR6M),
                                                  baseCalendar));
    _generatorSwap.put(GBP1YLIBOR3M,
                       new GeneratorSwapFixedIbor(GBP1YLIBOR3M,
                                                  Period.ofMonths(12),
                                                  DayCounts.ACT_365,
                                                  _iborIndexMaster.getIndex(IndexIborMaster.GBPLIBOR3M),
                                                  baseCalendar));
    _generatorSwap.put(GBP6MLIBOR6M,
                       new GeneratorSwapFixedIbor(GBP6MLIBOR6M,
                                                  Period.ofMonths(6),
                                                  DayCounts.ACT_365,
                                                  _iborIndexMaster.getIndex(IndexIborMaster.GBPLIBOR6M),
                                                  baseCalendar));
    _generatorSwap.put(GBP3MLIBOR3M, 
        new GeneratorSwapFixedIbor(GBP3MLIBOR3M, Period.ofMonths(3), DayCounts.ACT_365, 
            _iborIndexMaster.getIndex(IndexIborMaster.GBPLIBOR3M), baseCalendar));
    _generatorSwap.put(DKK1YCIBOR6M,
                       new GeneratorSwapFixedIbor(DKK1YCIBOR6M,
                                                  Period.ofMonths(12),
                                                  DayCounts.THIRTY_U_360,
                                                  _iborIndexMaster.getIndex(IndexIborMaster.DKKCIBOR6M),
                                                  baseCalendar));
    _generatorSwap.put(JPY6MLIBOR3M,
                       new GeneratorSwapFixedIbor(JPY6MLIBOR3M,
                                                  Period.ofMonths(6),
                                                  DayCounts.ACT_365,
                                                  _iborIndexMaster.getIndex(IndexIborMaster.JPYLIBOR3M),
                                                  baseCalendar));
    _generatorSwap.put(JPY6MLIBOR6M,
                       new GeneratorSwapFixedIbor(JPY6MLIBOR6M,
                                                  Period.ofMonths(6),
                                                  DayCounts.ACT_365,
                                                  _iborIndexMaster.getIndex(IndexIborMaster.JPYLIBOR6M),
                                                  baseCalendar));
    _generatorSwap.put(AUD3MBBSW3M,
                       new GeneratorSwapFixedIbor(AUD3MBBSW3M,
                                                  Period.ofMonths(3),
                                                  DayCounts.ACT_365,
                                                  _iborIndexMaster.getIndex(IndexIborMaster.AUDBB3M),
                                                  baseCalendar));
    _generatorSwap.put(AUD6MBBSW6M,
                       new GeneratorSwapFixedIbor(AUD6MBBSW6M,
                                                  Period.ofMonths(6),
                                                  DayCounts.ACT_365,
                                                  _iborIndexMaster.getIndex(IndexIborMaster.AUDBB6M),
                                                  baseCalendar));
  }

  public GeneratorSwapFixedIbor getGenerator(final String name, final HolidayCalendar cal) {
    final GeneratorSwapFixedIbor generatorNoCalendar = _generatorSwap.get(name);
    if (generatorNoCalendar == null) {
      throw new RuntimeException("Could not get Ibor index for " + name);
    }
    return new GeneratorSwapFixedIbor(generatorNoCalendar.getName(), generatorNoCalendar.getFixedLegPeriod(), generatorNoCalendar.getFixedLegDayCount(),
        _iborIndexMaster.getIndex(generatorNoCalendar.getIborIndex().getName()), cal);
  }

}

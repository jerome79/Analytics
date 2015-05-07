/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import java.time.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Generator (or template) for single currency leg. 
 * Abstract class.
 */
public abstract class GeneratorLeg extends GeneratorInstrument<GeneratorAttributeIR> {

  /** The leg generator currency. */
  private final Currency _ccy;

  /** 
   * Constructor.
   * @param name The generator name.
   * @param ccy The leg generator currency.
   */
  public GeneratorLeg(String name, Currency ccy) {
    super(name);
    ArgChecker.notNull(ccy, "currency");
    _ccy = ccy;
  }

  /**
   * Returns the leg generator currency.
   * @return The currency.
   */
  public Currency getCurrency() {
    return _ccy;
  }

  @Override
  /**
   * The leg generated is a receiver leg. To obtain a payer leg, use a negative notional.
   */
  public abstract AnnuityDefinition<?> generateInstrument(ZonedDateTime date, double marketQuote,
      double notional, GeneratorAttributeIR attribute);

}

/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.derivative;

import java.time.ZonedDateTime;

import com.opengamma.analytics.financial.commodity.definition.SettlementType;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.collect.ArgChecker;
import com.opengamma.strata.collect.id.StandardId;

/**
 * Metal  commodity derivative
 */
public class EnergyForward extends CommodityForward {

  /**
   * @param expiry Time (in years as a double) until the date-time at which the forward expires
   * @param underlying Identifier of the underlying commodity
   * @param unitAmount Size of a unit
   * @param firstDeliveryDate Date of first delivery - PHYSICAL settlement
   * @param lastDeliveryDate Date of last delivery - PHYSICAL settlement
   * @param amount Number of units
   * @param unitName Description of unit size
   * @param settlementType Settlement type - PHYISCAL or CASH
   * @param settlement time (in years as a double) until the date-time at which the future is settled
   * @param referencePrice reference price
   * @param currency the currency
   */
  public EnergyForward(final double expiry, final StandardId underlying, final double unitAmount, final ZonedDateTime firstDeliveryDate, final ZonedDateTime lastDeliveryDate,
      final double amount, final String unitName, final SettlementType settlementType, final double settlement, final double referencePrice, final Currency currency) {
    super(expiry, underlying, unitAmount, firstDeliveryDate, lastDeliveryDate, amount, unitName, settlementType, settlement, referencePrice, currency);
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    ArgChecker.notNull(visitor, "visitor");
    return visitor.visitEnergyForward(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgChecker.notNull(visitor, "visitor");
    return visitor.visitEnergyForward(this);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof EnergyForward)) {
      return false;
    }
    return super.equals(obj);
  }
}

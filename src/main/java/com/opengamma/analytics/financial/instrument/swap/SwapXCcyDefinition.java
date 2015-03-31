/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.swap;


import java.time.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.util.timeseries.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Class describing a generic Cross currency swap. The two legs should be in different currencies.
 * @deprecated Remove the class when the curve names are removed from instruments (provider in production).
 */
// TODO: This class was created only to deal with curve name of XCcy swaps. It should be deleted as soon as the curve names are removed from instrument description.
@Deprecated
public class SwapXCcyDefinition extends SwapDefinition {

  /**
   * Constructor of the ibor-ibor swap from its two legs. The currency of hte two curves should be different.
   * @param firstLeg The first Ibor leg.
   * @param secondLeg The second Ibor leg.
   */
  public SwapXCcyDefinition(final AnnuityDefinition<? extends PaymentDefinition> firstLeg, final AnnuityDefinition<? extends PaymentDefinition> secondLeg) {
    super(firstLeg, secondLeg);
    ArgChecker.isTrue(firstLeg.getCurrency() != secondLeg.getCurrency(), "Currencies should be different");
  }

  /**
   * {@inheritDoc}
   * Convert to derivative version.
   * @param date The system date.
   * @return The derivative.
   */
  @Override
  public Swap<Payment, Payment> toDerivative(final ZonedDateTime date) {
    final Annuity<Payment> firstLeg = (Annuity<Payment>) getFirstLeg().toDerivative(date);
    final Annuity<Payment> secondLeg = (Annuity<Payment>) getSecondLeg().toDerivative(date);
    return new Swap<>(firstLeg, secondLeg);
  }

  /**
   * {@inheritDoc}
   * Convert to derivative version.
   * @param date The system date.
   * @return The derivative.
   */
  @Override
  public Swap<Payment, Payment> toDerivative(final ZonedDateTime date, final ZonedDateTimeDoubleTimeSeries[] indexDataTS) {
    ArgChecker.notNull(indexDataTS, "index data time series array");
    ArgChecker.isTrue(indexDataTS.length > 1, "index data time series must contain at least two elements");
    final Annuity<Payment> firstLeg = (Annuity<Payment>) getFirstLeg().toDerivative(date, indexDataTS[0]);
    final Annuity<Payment> secondLeg = (Annuity<Payment>) getSecondLeg().toDerivative(date, indexDataTS[1]);
    return new Swap<>(firstLeg, secondLeg);
  }
}

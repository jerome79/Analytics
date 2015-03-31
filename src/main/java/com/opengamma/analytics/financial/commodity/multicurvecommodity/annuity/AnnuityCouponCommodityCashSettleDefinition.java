/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.multicurvecommodity.annuity;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import com.opengamma.analytics.financial.commodity.multicurvecommodity.definition.CouponCommodityCashSettleDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.util.timeseries.DoubleTimeSeries;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.strata.collect.ArgChecker;

/**
 * A wrapper class for an annuity containing AnnuityCouponCommodityCashSettleDefinition. 
 */
public class AnnuityCouponCommodityCashSettleDefinition extends AnnuityCouponCommodityDefinition<CouponCommodityCashSettleDefinition> {

  /**
   * Constructor from a list of overnight arithmetic average coupons with spread.
   * @param payments The coupons.
   * @param calendar The holiday calendar
   */
  public AnnuityCouponCommodityCashSettleDefinition(final CouponCommodityCashSettleDefinition[] payments, final Calendar calendar) {
    super(payments, calendar);
  }

  @Override
  public Annuity<? extends Payment> toDerivative(final ZonedDateTime valZdt, final DoubleTimeSeries<ZonedDateTime> indexFixingTS) {
    ArgChecker.notNull(valZdt, "date");
    ArgChecker.notNull(indexFixingTS, "index fixing time series");
    final List<Payment> resultList = new ArrayList<>();
    final CouponCommodityCashSettleDefinition[] payments = getPayments();
    final ZonedDateTime valZdtInPaymentZone = valZdt.withZoneSameInstant(payments[0].getPaymentDate().getZone());
    final LocalDate valDate = valZdtInPaymentZone.toLocalDate();

    for (int loopcoupon = 0; loopcoupon < payments.length; loopcoupon++) {
      if (!valDate.isAfter(payments[loopcoupon].getPaymentDate().toLocalDate())) {
        resultList.add(payments[loopcoupon].toDerivative(valZdt, indexFixingTS));
      }
    }
    return new Annuity<>(resultList.toArray(new Coupon[resultList.size()]));
  }
}

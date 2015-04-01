/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;


import java.time.LocalDate;
import java.time.Period;
import java.time.ZonedDateTime;

import com.opengamma.analytics.convention.StubType;
import com.opengamma.analytics.convention.rolldate.RollConvention;
import com.opengamma.analytics.financial.instrument.NotionalProvider;
import com.opengamma.analytics.financial.instrument.annuity.AdjustedDateParameters;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.annuity.CompoundingMethod;
import com.opengamma.analytics.financial.instrument.annuity.FloatingAnnuityDefinitionBuilder;
import com.opengamma.analytics.financial.instrument.annuity.OffsetAdjustedDateParameters;
import com.opengamma.analytics.financial.instrument.annuity.OffsetType;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.date.BusinessDayConvention;
import com.opengamma.strata.basics.date.BusinessDayConventions;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Generator (or template) for leg paying composition of overnight rate compounded - OIS style - (plus a spread).
 */
public class GeneratorLegONCompounded extends GeneratorLegONAbstract {

  /**
   * Constructor from all the details.
   * @param name The generator name.
   * @param ccy The leg currency.
   * @param indexON The overnight index underlying the leg.
   * @param paymentPeriod The period between two payments.
   * @param spotOffset The offset in business days between trade and settlement date (usually 2 or 0).
   * @param paymentOffset The offset in business days between the end of the accrual period and the coupon payment.
   * @param businessDayConvention The business day convention for the payments.
   * @param endOfMonth The flag indicating if the end-of-month rule is used.
   * @param stubType The stub type.
   * @param isExchangeNotional Whether the notional exchanged (at start and at end).
   * @param indexCalendar The calendar associated with the overnight index.
   * @param paymentCalendar The calendar used for the payments.
   */
  public GeneratorLegONCompounded(String name, Currency ccy, IndexON indexON, Period paymentPeriod, int spotOffset,
      int paymentOffset, BusinessDayConvention businessDayConvention, boolean endOfMonth, StubType stubType, 
      boolean isExchangeNotional, HolidayCalendar indexCalendar, HolidayCalendar paymentCalendar) {
    super(name, ccy, indexON, paymentPeriod, spotOffset, paymentOffset, businessDayConvention, endOfMonth, stubType, 
        isExchangeNotional, indexCalendar, paymentCalendar);
  }

  @Override
  public AnnuityDefinition<?> generateInstrument(final ZonedDateTime date, final double marketQuote,
      final double notional, final GeneratorAttributeIR attribute) {
    ArgChecker.notNull(date, "Reference date");
    ArgChecker.notNull(attribute, "Attributes");
    final ZonedDateTime spot = ScheduleCalculator.getAdjustedDate(date, getSpotOffset(), getPaymentCalendar());
    final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(spot, attribute.getStartPeriod(), 
        getBusinessDayConvention(), getPaymentCalendar(), isEndOfMonth());
    final ZonedDateTime endDate = startDate.plus(attribute.getEndPeriod());
    NotionalProvider notionalProvider = new NotionalProvider() {
      @Override
      public double getAmount(final LocalDate date) {
        return notional;
      }
    };
    AdjustedDateParameters adjustedDateIndex = new AdjustedDateParameters(getIndexCalendar(), getBusinessDayConvention());
    OffsetAdjustedDateParameters offsetFixing = new OffsetAdjustedDateParameters(0, OffsetType.BUSINESS, getIndexCalendar(),
        BusinessDayConventions.FOLLOWING);
    OffsetAdjustedDateParameters offsetPayment = new OffsetAdjustedDateParameters(getPaymentOffset(), OffsetType.BUSINESS, 
        getPaymentCalendar(), BusinessDayConventions.FOLLOWING);
    AnnuityDefinition<?> leg = new FloatingAnnuityDefinitionBuilder().
        payer(false).notional(notionalProvider).startDate(startDate.toLocalDate()).endDate(endDate.toLocalDate()).
        index(getIndexON()).accrualPeriodFrequency(getPaymentPeriod()).
        rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).resetDateAdjustmentParameters(adjustedDateIndex).
        accrualPeriodParameters(adjustedDateIndex).dayCount(getIndexON().getDayCount()).
        fixingDateAdjustmentParameters(offsetFixing).currency(getIndexON().getCurrency()).spread(marketQuote).
        compoundingMethod(CompoundingMethod.SPREAD_EXCLUSIVE).paymentDateAdjustmentParameters(offsetPayment).build();
    return leg;
  }

}

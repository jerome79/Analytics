/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;


import java.time.LocalDate;
import java.time.Period;
import java.time.ZonedDateTime;

import com.opengamma.analytics.convention.rolldate.RollConvention;
import com.opengamma.analytics.financial.instrument.NotionalProvider;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.annuity.CompoundingMethod;
import com.opengamma.analytics.financial.instrument.annuity.FloatingAnnuityDefinitionBuilder;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.date.BusinessDayAdjustment;
import com.opengamma.strata.basics.date.BusinessDayConvention;
import com.opengamma.strata.basics.date.BusinessDayConventions;
import com.opengamma.strata.basics.date.DaysAdjustment;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.basics.schedule.StubConvention;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Generator (or template) for leg paying an Ibor rate (plus a spread).
 */
public class GeneratorLegIborCompounding extends GeneratorLeg {

  /** The ON index on which the fixing is done. */
  private final IborIndex _indexIbor;
  /** The period between two payments. */
  private final Period _paymentPeriod;
  /** The compounding method for the different Ibor fixings. */
  private final CompoundingMethod _compoundingMethod;
  /** The offset in business days between trade and settlement date (usually 2 or 0). */
  private final int _spotOffset;
  /** The offset in days between end of the accrual period and the payment. */
  private final int _paymentOffset;
  /** The business day convention for the payments. */
  private final BusinessDayConvention _businessDayConvention;
  /** The flag indicating if the end-of-month rule is used. */
  private final boolean _endOfMonth;
  /** The stub type. */
  private final StubConvention _stubType;
  /** Whether the notional exchanged (at start and at end). */
  private final boolean _isExchangeNotional;
  /** The calendar associated with the overnight index. */
  private final HolidayCalendar _indexCalendar;
  /** The calendar used for the payments. */
  private final HolidayCalendar _paymentCalendar;

  /**
   * Constructor from all the details.
   * @param name The generator name.
   * @param ccy The leg currency.
   * @param indexIbor The overnight index underlying the leg.
   * @param paymentPeriod The period between two payments.
   * @param compoundingMethod The compounding method.
   * @param spotOffset The offset in business days between trade and settlement date (usually 2 or 0).
   * @param paymentOffset The offset in days between the last ON fixing date and the coupon payment.
   * @param businessDayConvention The business day convention for the payments.
   * @param endOfMonth The flag indicating if the end-of-month rule is used.
   * @param stubType The stub type.
   * @param isExchangeNotional Whether the notional exchanged (at start and at end).
   * @param indexCalendar The calendar associated with the overnight index.
   * @param paymentCalendar The calendar used for the payments.
   */
  public GeneratorLegIborCompounding(String name, Currency ccy, IborIndex indexIbor, Period paymentPeriod, 
      CompoundingMethod compoundingMethod, int spotOffset, int paymentOffset, 
      BusinessDayConvention businessDayConvention, boolean endOfMonth, StubConvention stubType,  boolean isExchangeNotional, 
      HolidayCalendar indexCalendar, HolidayCalendar paymentCalendar) {
    super(name, ccy);
    ArgChecker.notNull(indexIbor, "Index Ibor");
    ArgChecker.notNull(paymentPeriod, "payment period");
    ArgChecker.notNull(compoundingMethod, "compounding method");
    ArgChecker.notNull(businessDayConvention, "Business day convention");
    ArgChecker.notNull(stubType, "stub type");
    ArgChecker.notNull(indexCalendar, "index calendar");
    ArgChecker.notNull(paymentCalendar, "payment calendar");
    _indexIbor = indexIbor;
    _paymentPeriod = paymentPeriod;
    _compoundingMethod = compoundingMethod;
    _spotOffset = spotOffset;
    _paymentOffset = paymentOffset;
    _businessDayConvention = businessDayConvention;
    _endOfMonth = endOfMonth;
    _stubType = stubType;
    _isExchangeNotional = isExchangeNotional;
    _indexCalendar = indexCalendar;
    _paymentCalendar = paymentCalendar;
  }

  /**
   * Gets the underlying Ibor index.
   * @return The index.
   */
  public IborIndex getIndexIbor() {
    return _indexIbor;
  }

  /**
   * Gets the payment period.
   * @return the payment period.
   */
  public Period getPaymentPeriod() {
    return _paymentPeriod;
  }

  /**
   * Gets the compounding method.
   * @return The compounding method.
   */
  public CompoundingMethod getCompoundingMethod() {
    return _compoundingMethod;
  }

  /**
   * Gets the spot offset.
   * @return the spot offset.
   */
  public int getSpotOffset() {
    return _spotOffset;
  }

  /**
   * Gets the payment offset.
   * @return the paymentOffset
   */
  public int getPaymentOffset() {
    return _paymentOffset;
  }

  /**
   * Gets the business day convention.
   * @return the businessDayConvention
   */
  public BusinessDayConvention getBusinessDayConvention() {
    return _businessDayConvention;
  }

  /**
   * Gets the end-of-month flag.
   * @return the endOfMonth
   */
  public boolean isEndOfMonth() {
    return _endOfMonth;
  }

  /**
   * Gets the stubType.
   * @return the stubType
   */
  public StubConvention getStubType() {
    return _stubType;
  }

  /**
   * Gets the notional exchange flag.
   * @return the isExchangeNotional
   */
  public boolean isExchangeNotional() {
    return _isExchangeNotional;
  }

  /**
   * Gets the indexCalendar.
   * @return the indexCalendar
   */
  public HolidayCalendar getIndexCalendar() {
    return _indexCalendar;
  }

  /**
   * Gets the paymentCalendar.
   * @return the paymentCalendar
   */
  public HolidayCalendar getPaymentCalendar() {
    return _paymentCalendar;
  }

  @Override
  public AnnuityDefinition<?> generateInstrument(final ZonedDateTime date, final double marketQuote,
      final double notional, final GeneratorAttributeIR attribute) {
    ArgChecker.notNull(date, "Reference date");
    ArgChecker.notNull(attribute, "Attributes");
    final ZonedDateTime spot = ScheduleCalculator.getAdjustedDate(date, _spotOffset, _paymentCalendar);
    final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(spot, attribute.getStartPeriod(), 
        _businessDayConvention, _paymentCalendar, _endOfMonth);
    final ZonedDateTime endDate = startDate.plus(attribute.getEndPeriod());
    NotionalProvider notionalProvider = new NotionalProvider() {
      @Override
      public double getAmount(final LocalDate date) {
        return notional;
      }
    };
    BusinessDayAdjustment adjustedDateIndex = BusinessDayAdjustment.of(_businessDayConvention, _indexCalendar);
    DaysAdjustment offsetFixing = DaysAdjustment.ofBusinessDays(
        -_indexIbor.getSpotLag(),
        _indexCalendar,
        BusinessDayAdjustment.of(BusinessDayConventions.FOLLOWING, _indexCalendar));  // TODO: LIBOR
    AnnuityDefinition<?> leg = new FloatingAnnuityDefinitionBuilder().
        payer(false).notional(notionalProvider).startDate(startDate.toLocalDate()).endDate(endDate.toLocalDate()).
        index(_indexIbor).accrualPeriodFrequency(_paymentPeriod).
        rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).resetDateAdjustmentParameters(adjustedDateIndex).
        accrualPeriodParameters(adjustedDateIndex).dayCount(_indexIbor.getDayCount()).
        fixingDateAdjustmentParameters(offsetFixing).currency(_indexIbor.getCurrency()).spread(marketQuote).
        exchangeInitialNotional(_isExchangeNotional).startDateAdjustmentParameters(adjustedDateIndex).
        exchangeFinalNotional(_isExchangeNotional).endDateAdjustmentParameters(adjustedDateIndex).
        compoundingMethod(_compoundingMethod).build();
    return leg;
  }

}

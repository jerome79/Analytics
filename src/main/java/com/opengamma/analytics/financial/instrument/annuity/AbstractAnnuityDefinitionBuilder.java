/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.annuity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;

import com.opengamma.analytics.convention.businessday.BusinessDayDateUtils;
import com.opengamma.analytics.convention.daycount.DayCount;
import com.opengamma.analytics.convention.rolldate.RollDateAdjuster;
import com.opengamma.analytics.financial.instrument.NotionalProvider;
import com.opengamma.analytics.financial.instrument.VariableNotionalProvider;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.date.BusinessDayAdjustment;
import com.opengamma.strata.basics.date.DaysAdjustment;
import com.opengamma.strata.basics.schedule.StubConvention;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Base class for building annuity definitions.
 * @param <T> the implementing class
 */
public abstract class AbstractAnnuityDefinitionBuilder<T extends AbstractAnnuityDefinitionBuilder<T>> {

  /**
   * Description of the coupon stub.
   */
  public static class CouponStub {
    private StubConvention _stubType;
    private double _stubRate = Double.NaN;
    private LocalDate _effectiveDate;
    private IborIndex _firstIborIndex;
    private IborIndex _secondIborIndex;

    public CouponStub(StubConvention stubType) {
      _stubType = stubType;
    }

    public CouponStub(StubConvention stubType, LocalDate effectiveDate) {
      _stubType = stubType;
      _effectiveDate = effectiveDate;
    }

    public CouponStub(StubConvention stubType, LocalDate effectiveDate, double stubRate) {
      _stubType = stubType;
      _effectiveDate = effectiveDate;
      _stubRate = stubRate;
    }

    public CouponStub(StubConvention stubType, IborIndex firstStubIndex, IborIndex secondStubIndex) {
      _stubType = stubType;
      _firstIborIndex = firstStubIndex;
      _secondIborIndex = secondStubIndex;
    }

    public CouponStub(StubConvention stubType, LocalDate effectiveDate, IborIndex firstStubIndex, IborIndex secondStubIndex) {
      _stubType = stubType;
      _effectiveDate = effectiveDate;
      _firstIborIndex = firstStubIndex;
      _secondIborIndex = secondStubIndex;
    }

    public StubConvention getStubType() {
      return _stubType;
    }

    public double getStubRate() {
      return _stubRate;
    }

    public boolean hasStubRate() {
      return !Double.isNaN(_stubRate);
    }

    public LocalDate getEffectiveDate() {
      return _effectiveDate;
    }

    public IborIndex getFirstIborIndex() {
      return _firstIborIndex;
    }

    public IborIndex getSecondIborIndex() {
      return _secondIborIndex;
    }

    public boolean isInterpolated() {
      return _firstIborIndex != null && _secondIborIndex != null
          && !_firstIborIndex.equals(_secondIborIndex);
    }
  }

  /**
   * Flag to describe the direction of the coupons. This is a required field.
   */
  private boolean _payer;

  /**
   * The daycount of the annuity. This is a required field.
   */
  private DayCount _dayCount;

  /**
   * The currency of the coupons in the annuity. This is a required field.
   */
  private Currency _currency;

  /**
   * The notional of the annuity. This is a required field.
   */
  private NotionalProvider _notional;

  /**
   * The start date of the annuity. This is a required field.
   */
  private LocalDate _startDate;

  /**
   * The end date of the annuity. This is a required field.
   */
  private LocalDate _endDate;

  /**
   * The stub type at the start of the series of coupons. This is an optional field and will default to a short start stub.
   */
  private CouponStub _startStub = new CouponStub(StubConvention.SHORT_INITIAL);

  /**
   * The stub type at the end of the series of coupons. This is an optional field, and will default to none if unset.
   */
  private CouponStub _endStub = new CouponStub(StubConvention.NONE);

  /**
   * The roll date adjuster used to adjust the accrual dates. This is an optional field.
   */
  private RollDateAdjuster _rollDateAdjuster;

  /**
   * Flag to exchange initial notional, defaults to false.
   */
  private boolean _exchangeInitialNotional;

  /**
   * Flag to exchange final notional, defaults to false.
   */
  private boolean _exchangeFinalNotional;

  /**
   * The frequency of the accrual periods.
   */
  private Period _accrualPeriodFrequency;

  /**
   * Parameters used to adjust the accrual period dates. This is an optional field.
   */
  private BusinessDayAdjustment _adjustedAccrualDateParameters;

  /**
   * Parameters used to adjust the start date of the annuity. This is an optional field.
   */
  private BusinessDayAdjustment _adjustedStartDateParameters;

  /**
   * Parameters used to adjust the end date of the annuity. This is an optional field.
   */
  private BusinessDayAdjustment _adjustedEndDateParameters;

  /**
   * Flag to indicate the payment date relative to accrual period. This is an optional field, and will default to the 
   * end of the accrual period.
   */
  private DateRelativeTo _paymentDateRelativeTo = DateRelativeTo.END;

  /**
   * Parameters used to create payment dates relative to the accrual periods of the annuity. This is an optional field.
   */
  private DaysAdjustment _adjustedPaymentDateParameters;

  /**
   * The compounding.
   */
  private CompoundingMethod _compoundingMethod;

  protected boolean isPayer() {
    return _payer;
  }

  protected DayCount getDayCount() {
    return _dayCount;
  }

  protected Currency getCurrency() {
    return _currency;
  }

  protected NotionalProvider getNotional() {
    return _notional;
  }

  /**
   * If notional provider is VariableNotionalProvider and the date set is null, construct a new provider with the computed dates
   * @param dates Set of dates specifying notional, i.e., accrual start date for coupon payment, adjusted start/end date for notional exchange
   */
  protected void resetNotionalProvider(ZonedDateTime[] dates) {
    if (getNotional() instanceof VariableNotionalProvider) {
      VariableNotionalProvider provider = (VariableNotionalProvider) getNotional();
      if (provider.getDates() == null) {
        ArrayList<ZonedDateTime> list = new ArrayList<>();
        if (isExchangeInitialNotional()) {
          ZonedDateTime startDate = BusinessDayDateUtils.applyConvention(
              getStartDateAdjustmentParameters().getConvention(),
              getStartDate(),
              getStartDateAdjustmentParameters().getCalendar());
          list.add(startDate);
        }
        int nDates = dates.length;
        for (int i = 0; i < nDates; ++i) {
          list.add(dates[i]);
        }
        if (isExchangeFinalNotional()) {
          ZonedDateTime endDate = BusinessDayDateUtils.applyConvention(
              getEndDateAdjustmentParameters().getConvention(),
              getEndDate(),
              getEndDateAdjustmentParameters().getCalendar());
          list.add(endDate);
        }
        _notional = provider.withZonedDateTime(list);
      }
    }
  }

  /**
   * Returns the unadjusted start date of the annuity.
   * @return the unadjusted start date of the annuity.
   */
  protected ZonedDateTime getStartDate() {
    return _startDate.atTime(LocalTime.MIN).atZone(ZoneOffset.UTC);
  }

  /**
   * Returns the unadjusted end date of the annuity.
   * @return the unadjusted end date of the annuity.
   */
  protected ZonedDateTime getEndDate() {
    return _endDate.atTime(LocalTime.MIN).atZone(ZoneOffset.UTC);
  }

  protected CouponStub getStartStub() {
    return _startStub;
  }

  protected CouponStub getEndStub() {
    return _endStub;
  }

  protected RollDateAdjuster getRollDateAdjuster() {
    return _rollDateAdjuster;
  }

  protected boolean isExchangeInitialNotional() {
    return _exchangeInitialNotional;
  }

  protected boolean isExchangeFinalNotional() {
    return _exchangeFinalNotional;
  }

  protected Period getAccrualPeriodFrequency() {
    return _accrualPeriodFrequency;
  }

  protected BusinessDayAdjustment getAccrualPeriodAdjustmentParameters() {
    return _adjustedAccrualDateParameters;
  }

  protected BusinessDayAdjustment getStartDateAdjustmentParameters() {
    return _adjustedStartDateParameters;
  }

  protected BusinessDayAdjustment getEndDateAdjustmentParameters() {
    return _adjustedEndDateParameters;
  }

  protected DateRelativeTo getPaymentDateRelativeTo() {
    return _paymentDateRelativeTo;
  }

  protected DaysAdjustment getPaymentDateAdjustmentParameters() {
    return _adjustedPaymentDateParameters;
  }

  /**
   * Sets the flag to describe whether the annuity is paying or receiving. This is a required field.
   * @param payer whether the annuity is paying or receiving.
   * @return itself.
   */
  @SuppressWarnings("unchecked")
  public T payer(boolean payer) {
    _payer = payer;
    return (T) this;
  }

  /**
   * Sets the daycount of the annuity. This is a required field.
   * @param dayCount the daycount of the annuity.
   * @return itself
   */
  @SuppressWarnings("unchecked")
  public T dayCount(DayCount dayCount) {
    _dayCount = dayCount;
    return (T) this;
  }

  /**
   * Sets the notional of the annuity. This is a required field.
   * @param notional the notional of the annuity.
   * @return itself
   */
  @SuppressWarnings("unchecked")
  public T notional(NotionalProvider notional) {
    _notional = notional;
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  public T currency(Currency currency) {
    _currency = currency;
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  public T startDate(LocalDate startDate) {
    _startDate = startDate;
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  public T endDate(LocalDate endDate) {
    _endDate = endDate;
    return (T) this;
  }

  /**
   * Sets the stub type at the start of the series of coupons. This is optional and will default to StubConvention.NONE if unset.
   * @param startStub the stub type at the end of the series of coupons.
   * @return itself
   */
  @SuppressWarnings("unchecked")
  public T startStub(CouponStub startStub) {
    if (startStub == null) {
      _startStub = null;
    } else {
      ArgChecker.isFalse(startStub.getStubType() == StubConvention.SHORT_FINAL ||
          startStub.getStubType() == StubConvention.LONG_FINAL, "startStub should be start stub type, but {}",
          startStub.getStubType());
      _startStub = startStub;
      if (startStub.getStubType() != StubConvention.BOTH && startStub.getStubType() != StubConvention.NONE) {
        _endStub = null; // reset end stub.
      }
    }
    return (T) this;
  }

  /**
   * Sets the stub type at the end of the series of coupons. This is optional and will default to StubConvention.NONE if unset.
   * @param endStub the stub type at the end of the series of coupons.
   * @return itself
   */
  @SuppressWarnings("unchecked")
  public T endStub(CouponStub endStub) {
    if (endStub == null) {
      _endStub = null;
    } else {
      ArgChecker.isFalse(endStub.getStubType() == StubConvention.SHORT_INITIAL ||
          endStub.getStubType() == StubConvention.LONG_INITIAL, "endStub should be end stub type, but {}",
          endStub.getStubType());
      _endStub = endStub;
      if (endStub.getStubType() != StubConvention.BOTH && endStub.getStubType() != StubConvention.NONE) {
        _startStub = null; // reset start stub.
      }
    }
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  public T rollDateAdjuster(RollDateAdjuster rollDateAdjuster) {
    _rollDateAdjuster = rollDateAdjuster;
    return (T) this;
  }

  /**
   * Sets the flag indicating whether the notional is exchanged at the start of the annuity.
   * @param exchangeInitialNotional the flag indicating whether the notional is exchanged at the start of the annuity.
   * @return itself
   */
  @SuppressWarnings("unchecked")
  public T exchangeInitialNotional(boolean exchangeInitialNotional) {
    _exchangeInitialNotional = exchangeInitialNotional;
    return (T) this;
  }

  /**
   * Sets the flag indicating whether the notional is exchanged at the end of the annuity.
   * @param exchangeFinalNotional the flag indicating whether the notional is exchanged at the end of the annuity.
   * @return itself
   */
  @SuppressWarnings("unchecked")
  public T exchangeFinalNotional(boolean exchangeFinalNotional) {
    _exchangeFinalNotional = exchangeFinalNotional;
    return (T) this;
  }

  /**
   * Sets the accrual period frequency of the annuity. This is a required field.
   * @param accrualPeriodFrequency the frequency of the coupons of the annuity.
   * @return itself
   */
  @SuppressWarnings("unchecked")
  public T accrualPeriodFrequency(Period accrualPeriodFrequency) {
    _accrualPeriodFrequency = accrualPeriodFrequency;
    return (T) this;
  }

  /**
   * Sets the parameters used to adjust the accrual period dates. This is an optional field.
   * @param accrualDateAdjustmentParameters the parameters used to adjust the accrual periods.
   * @return the parameters used to adjust the accrual periods.
   */
  @SuppressWarnings("unchecked")
  public T accrualPeriodParameters(BusinessDayAdjustment accrualDateAdjustmentParameters) {
    _adjustedAccrualDateParameters = accrualDateAdjustmentParameters;
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  public T startDateAdjustmentParameters(BusinessDayAdjustment startDateAdjustmentParameters) {
    _adjustedStartDateParameters = startDateAdjustmentParameters;
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  public T endDateAdjustmentParameters(BusinessDayAdjustment endDateAdjustmentParameters) {
    if (_adjustedAccrualDateParameters != null
        && _adjustedAccrualDateParameters.getConvention() == null
        && _adjustedEndDateParameters.getConvention() != null) {
      throw new IllegalArgumentException("End date adjustment business day convention does not match accrual period business day convention");
    }
    _adjustedEndDateParameters = endDateAdjustmentParameters;
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  public T paymentDateRelativeTo(DateRelativeTo paymentDateRelativeTo) {
    _paymentDateRelativeTo = paymentDateRelativeTo;
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  public T paymentDateAdjustmentParameters(DaysAdjustment paymentDateAdjustmentParameters) {
    _adjustedPaymentDateParameters = paymentDateAdjustmentParameters;
    return (T) this;
  }

  /**
   * Returns the accrual end dates, adjusted if the parameters are set.
   * @return the accrual end dates, adjusted if the parameters are set.
   */
  protected ZonedDateTime[] getAccrualEndDates() {
    return getAccrualEndDates(true);
  }

  protected ZonedDateTime[] getAccrualEndDates(boolean adjusted) {
    StubConvention stubType = null;
    if (_startStub != null && _startStub.getStubType() != StubConvention.NONE) {
      stubType = _startStub.getStubType();
    } else if (_endStub != null) {
      stubType = _endStub.getStubType();
    }

    if (stubType == null) {
      stubType = StubConvention.NONE;
    }

    ZonedDateTime actualStartDate = getStartDate();
    ZonedDateTime actualEndDate = getEndDate();

    ZonedDateTime startDate;
    ZonedDateTime endDate;
    if (StubConvention.BOTH == stubType) {
      startDate = ZonedDateTime.of(_startStub.getEffectiveDate(), LocalTime.of(0, 0), ZoneId.of("UTC"));
      endDate = ZonedDateTime.of(_endStub.getEffectiveDate(), LocalTime.of(0, 0), ZoneId.of("UTC"));
    } else {
      startDate = actualStartDate;
      endDate = actualEndDate;
    }

    ZonedDateTime[] accrualEndDates;
    if (adjusted && _adjustedAccrualDateParameters != null) {
      accrualEndDates = ScheduleCalculator.getAdjustedDateSchedule(
          startDate,
          endDate,
          _accrualPeriodFrequency,
          stubType,
          _adjustedAccrualDateParameters.getConvention(),
          _adjustedAccrualDateParameters.getCalendar(),
          getRollDateAdjuster());
    } else {
      accrualEndDates = ScheduleCalculator.getUnadjustedDateSchedule(
          startDate, endDate, _accrualPeriodFrequency, stubType);
    }

    if (StubConvention.BOTH == stubType) {
      ZonedDateTime[] bothStubAccrualEndDates = new ZonedDateTime[accrualEndDates.length + 2];
      System.arraycopy(accrualEndDates, 0, bothStubAccrualEndDates, 1, accrualEndDates.length);
      bothStubAccrualEndDates[0] = startDate;
      bothStubAccrualEndDates[bothStubAccrualEndDates.length - 1] = actualEndDate;
      accrualEndDates = bothStubAccrualEndDates;
    }
    return accrualEndDates;
  }

  protected ZonedDateTime[] getPaymentDates(ZonedDateTime[] accrualDates) {
    if (_adjustedPaymentDateParameters != null) {
      return ScheduleCalculator.getAdjustedDateSchedule(
          accrualDates,
          _adjustedPaymentDateParameters.getAdjustment().getConvention(),
          _adjustedPaymentDateParameters.getCalendar(),
          _adjustedPaymentDateParameters.getDays());
    } else {
      return accrualDates;
    }
  }

  protected CouponFixedDefinition getExchangeInitialNotionalCoupon() {
    if (!_exchangeInitialNotional) {
      return null;
    }
    ZonedDateTime startDate = BusinessDayDateUtils.applyConvention(
        getStartDateAdjustmentParameters().getConvention(),
        getStartDate(),
        getStartDateAdjustmentParameters().getCalendar());

    return new CouponFixedDefinition(
        _currency,
        startDate, // payment
        startDate, // accrual start
        startDate, // accrual end
        1.0, // year frac
        (isPayer() ? 1 : -1) * getNotional().getAmount(_startDate), // The initial notional has opposite sign.
        1.0); // rate
  }

  protected CouponFixedDefinition getExchangeFinalNotionalCoupon() {
    ZonedDateTime endDate = BusinessDayDateUtils.applyConvention(
        getEndDateAdjustmentParameters().getConvention(),
        getEndDate(),
        getEndDateAdjustmentParameters().getCalendar());

    return new CouponFixedDefinition(_currency,
        endDate, // payment
        endDate, // accrual start
        endDate, // accrual end
        1.0, // year frac
        (isPayer() ? -1 : 1) * getNotional().getAmount(_endDate),
        1.0); // rate
  }

  public CompoundingMethod getCompoundingMethod() {
    return _compoundingMethod;
  }

  @SuppressWarnings("unchecked")
  public T compoundingMethod(CompoundingMethod compoundingMethod) {
    _compoundingMethod = compoundingMethod;
    return (T) this;
  }

  public abstract AnnuityDefinition<?> build();
}

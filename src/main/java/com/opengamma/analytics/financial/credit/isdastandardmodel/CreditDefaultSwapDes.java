/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import java.time.LocalDate;
import java.time.Period;

import com.opengamma.analytics.convention.daycount.DayCount;
import com.opengamma.strata.basics.date.BusinessDayConvention;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.collect.ArgChecker;

/**
 * 
 */
public class CreditDefaultSwapDes {

  private final CDSCouponDes[] _coupons;
  private final boolean _payAccOnDefault;
  private final DayCount _accrualDayCount;

  public CreditDefaultSwapDes(final LocalDate accStartDate, final LocalDate protectionStartDate, final LocalDate protectionEndDate, final boolean payAccOnDefault, final Period paymentInterval,
      final StubType stubType, final boolean isProtectStart, final BusinessDayConvention businessdayAdjustmentConvention, final HolidayCalendar calendar, final DayCount accrualDayCount) {
    ArgChecker.notNull(accStartDate, "accStartDate");
    ArgChecker.notNull(protectionStartDate, "protectionStartDate");
    ArgChecker.notNull(protectionEndDate, "protectionEndDate");
    ArgChecker.notNull(paymentInterval, "tenor");
    ArgChecker.notNull(stubType, "stubType");
    ArgChecker.notNull(businessdayAdjustmentConvention, "businessdayAdjustmentConvention");
    ArgChecker.notNull(accrualDayCount, "accuralDayCount");
    ArgChecker.isTrue(protectionEndDate.isAfter(protectionStartDate), "protectionEndDate ({}) must be after protectionStartDate ({})", protectionStartDate, protectionEndDate);

    final ISDAPremiumLegSchedule fullPaymentSchedule = new ISDAPremiumLegSchedule(accStartDate, protectionEndDate, paymentInterval, stubType, businessdayAdjustmentConvention, calendar, isProtectStart);
    final ISDAPremiumLegSchedule paymentSchedule = ISDAPremiumLegSchedule.truncateSchedule(protectionStartDate, fullPaymentSchedule);

    _coupons = CDSCouponDes.makeCoupons(paymentSchedule, accrualDayCount);
    _payAccOnDefault = payAccOnDefault;
    _accrualDayCount = accrualDayCount;
  }

  /**
   * Gets the coupons.
   * @return the coupons
   */
  public CDSCouponDes[] getCoupons() {
    return _coupons;
  }

  /**
   * Gets the payAccOnDefault.
   * @return the payAccOnDefault
   */
  public boolean isPayAccOnDefault() {
    return _payAccOnDefault;
  }

  /**
   * Gets the accrualDayCount.
   * @return the accrualDayCount
   */
  public DayCount getAccrualDayCount() {
    return _accrualDayCount;
  }

}

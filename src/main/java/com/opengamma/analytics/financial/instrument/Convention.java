/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument;

import java.util.Objects;

import com.opengamma.analytics.convention.daycount.DayCount;
import com.opengamma.strata.basics.date.BusinessDayConvention;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.collect.ArgChecker;

/**
 * 
 */
public class Convention {
  private final int _settlementDays;
  private final DayCount _dayCount;
  private final BusinessDayConvention _businessDayConvention;
  private final HolidayCalendar _workingDayCalendar;
  private final String _name;

  public Convention(final int settlementDays, final DayCount dayCount, final BusinessDayConvention businessDayConvention, final HolidayCalendar workingDayCalendar, final String name) {
    ArgChecker.isTrue(settlementDays >= 0);
    ArgChecker.notNull(dayCount, "dayCount");
    ArgChecker.notNull(businessDayConvention, "businessDayConvention");
    ArgChecker.notNull(workingDayCalendar, "workingDayCalendar");
    ArgChecker.notNull(name, "name");
    _settlementDays = settlementDays;
    _dayCount = dayCount;
    _businessDayConvention = businessDayConvention;
    _workingDayCalendar = workingDayCalendar;
    _name = name;
  }

  public int getSettlementDays() {
    return _settlementDays;
  }

  public DayCount getDayCount() {
    return _dayCount;
  }

  public BusinessDayConvention getBusinessDayConvention() {
    return _businessDayConvention;
  }

  public HolidayCalendar getWorkingDayCalendar() {
    return _workingDayCalendar;
  }

  public String getName() {
    return _name;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _businessDayConvention.hashCode();
    result = prime * result + _dayCount.hashCode();
    result = prime * result + _settlementDays;
    result = prime * result + _workingDayCalendar.hashCode();
    result = prime * result + _name.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Convention other = (Convention) obj;
    if (!Objects.equals(_businessDayConvention, other._businessDayConvention)) {
      return false;
    }
    if (!Objects.equals(_dayCount, other._dayCount)) {
      return false;
    }
    if (_settlementDays != other._settlementDays) {
      return false;
    }
    if (!Objects.equals(_name, other._name)) {
      return false;
    }
    return Objects.equals(_workingDayCalendar, other._workingDayCalendar);
  }

}

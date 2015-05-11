/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import java.time.ZonedDateTime;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.financial.model.option.Moneyness;
import com.opengamma.analytics.util.time.DateUtils;
import com.opengamma.analytics.util.time.Expiry;
import com.opengamma.strata.collect.ArgChecker;

/**
 * 
 */
public class ForwardStartOptionDefinition extends OptionDefinition {
  private static final Logger s_logger = LoggerFactory.getLogger(ForwardStartOptionDefinition.class);
  private final OptionExerciseFunction<StandardOptionDataBundle> _exerciseFunction = new EuropeanExerciseFunction<>();
  private final OptionPayoffFunction<StandardOptionDataBundle> _payoffFunction = new OptionPayoffFunction<StandardOptionDataBundle>() {

    @Override
    public double getPayoff(final StandardOptionDataBundle data, final Double optionPrice) {
      final ZonedDateTime date = data.getDate();
      if (date.isBefore(getStartTime().getExpiry())) {
        throw new IllegalArgumentException("Cannot get strike before start time: it has not been defined");
      }
      final double spot = data.getSpot();
      final double alpha = getAlpha();
      final double strike = spot * alpha;
      return isCall() ? Math.max(0, spot - strike) : Math.max(0, strike - spot);
    }

  };

  private final Expiry _startTime;
  private final double _percent;
  private final Moneyness _moneyness;
  private final double _alpha;

  public ForwardStartOptionDefinition(final Expiry expiry, final Boolean isCall, final Expiry startTime) {
    this(expiry, isCall, startTime, 1, Moneyness.ATM);
  }

  public ForwardStartOptionDefinition(final Expiry expiry, final Boolean isCall, final Expiry startTime, final double percent, final Moneyness moneyness) {
    super(null, expiry, isCall);
    ArgChecker.notNull(startTime, "startTime");
    ArgChecker.notNegative(percent, "percent");
    ArgChecker.notNull(moneyness, "moneyness");
    if (expiry.getExpiry().isBefore(startTime.getExpiry())) {
      throw new IllegalArgumentException("The forward start time must be before the expiry of the option");
    }
    if (moneyness == Moneyness.ATM && percent != 1) {
      s_logger.info("Option is ATM but percentage is not one; ignoring value for percent");
    }
    _startTime = startTime;
    _percent = percent;
    _moneyness = moneyness;
    switch (moneyness) {
      case ITM:
        _alpha = isCall ? 1 - percent : 1 + percent;
        break;
      case OTM:
        _alpha = isCall ? percent + 1 : 1 - percent;
        break;
      case ATM:
        _alpha = 1;
        break;
      default:
        throw new IllegalArgumentException("Can only handle ITM, OTM and ATM");
    }
  }

  public Expiry getStartTime() {
    return _startTime;
  }

  public double getAlpha() {
    return _alpha;
  }

  public double getPercent() {
    return _percent;
  }

  public Moneyness getMoneyness() {
    return _moneyness;
  }

  @Override
  public OptionExerciseFunction<StandardOptionDataBundle> getExerciseFunction() {
    return _exerciseFunction;
  }

  @Override
  public OptionPayoffFunction<StandardOptionDataBundle> getPayoffFunction() {
    return _payoffFunction;
  }

  public double getTimeToStart(final ZonedDateTime date) {
    if (date.isAfter(getStartTime().getExpiry())) {
      throw new IllegalArgumentException("Date " + date + " is after startTime " + getStartTime());
    }
    return DateUtils.getDifferenceInYears(date, getStartTime().getExpiry());
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((_moneyness == null) ? 0 : _moneyness.hashCode());
    long temp;
    temp = Double.doubleToLongBits(_percent);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + ((_startTime == null) ? 0 : _startTime.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final ForwardStartOptionDefinition other = (ForwardStartOptionDefinition) obj;
    if (!Objects.equals(_moneyness, other._moneyness)) {
      return false;
    }
    if (Double.doubleToLongBits(_percent) != Double.doubleToLongBits(other._percent)) {
      return false;
    }
    return Objects.equals(_startTime, other._startTime);
  }

}

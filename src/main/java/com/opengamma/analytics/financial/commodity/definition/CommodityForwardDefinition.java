/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.definition;

import java.time.ZonedDateTime;
import java.util.Objects;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.collect.ArgChecker;
import com.opengamma.strata.collect.id.StandardId;

/**
 * Abstract commodity forward definition.
 *
 * @param <T> concrete derivative class toDerivative() returns
 */
public abstract class CommodityForwardDefinition<T extends InstrumentDerivative> implements InstrumentDefinitionWithData<T, Double> {
  /** Expiry date */
  private final ZonedDateTime _expiryDate;
  /** Identifier of the underlying commodity */
  private final StandardId _underlying;
  /** Size of a unit */
  private final double _unitAmount;
  /** Date of first delivery - PHYSICAL settlement */
  private final ZonedDateTime _firstDeliveryDate;
  /** Date of last delivery - PHYSICAL settlement */
  private final ZonedDateTime _lastDeliveryDate;
  /** Number of units */
  private final double _amount;
  /** Description of unit size */
  private final String _unitName;
  /** Settlement type - PHYISCAL or CASH */
  private final SettlementType _settlementType;
  /** reference price */
  private final double _referencePrice;
  /** currency */
  private final Currency _currency;
  /** Settlement date */
  private final ZonedDateTime _settlementDate;

  /**
   * Constructor for forwards with delivery dates (i.e. physical settlement)
   *
   * @param expiryDate  the time and the day that a particular delivery month of a forward contract stops trading, as well as the final settlement price for that contract
   * @param underlying  identifier of the underlying commodity
   * @param unitAmount  size of a unit
   * @param firstDeliveryDate  date of first delivery - PHYSICAL settlement
   * @param lastDeliveryDate  date of last delivery - PHYSICAL settlement
   * @param amount  number of units
   * @param unitName  description of unit size
   * @param settlementType  settlement type - PHYSICAL or CASH
   * @param referencePrice reference price
   * @param currency currency
   * @param settlementDate settlement date
   */
  public CommodityForwardDefinition(final ZonedDateTime expiryDate, final StandardId underlying, final double unitAmount, final ZonedDateTime firstDeliveryDate, final ZonedDateTime lastDeliveryDate,
      final double amount, final String unitName, final SettlementType settlementType, final double referencePrice, final Currency currency, final ZonedDateTime settlementDate) {
    ArgChecker.notNull(expiryDate, "expiry time");
    ArgChecker.notNull(underlying, "underlying");

    ArgChecker.notEmpty(unitName, "unit name");
    ArgChecker.notNull(settlementType, "settlement type");
    ArgChecker.notNull(settlementDate, "settlement date");
    if (settlementType.equals(SettlementType.PHYSICAL)) {
      ArgChecker.inOrderOrEqual(firstDeliveryDate, lastDeliveryDate, "first delivery date", "last delivery date");
    } else {
      ArgChecker.isTrue(firstDeliveryDate == null, "first delivery date must be null for non physical settlement");
      ArgChecker.isTrue(lastDeliveryDate == null, "last delivery date must be null for non physical settlement");
    }
    ArgChecker.notNull(currency, "currency");

    _expiryDate = expiryDate;
    _underlying = underlying;
    _unitAmount = unitAmount;
    _firstDeliveryDate = firstDeliveryDate;
    _lastDeliveryDate = lastDeliveryDate;
    _amount = amount;
    _unitName = unitName;
    _settlementType = settlementType;
    _referencePrice = referencePrice;
    _currency = currency;
    _settlementDate = settlementDate;
  }

  /**
   * Constructor for forward without delivery dates (e.g. cash settlement)
   *
   * @param expiryDate  the time and the day that a particular delivery month of a forwards contract stops trading, as well as the final settlement price for that contract
   * @param underlying  identifier of the underlying commodity
   * @param unitAmount  size of a unit
   * @param amount  number of units
   * @param unitName  description of unit size
   * @param referencePrice reference price
   * @param currency currency
   * @param settlementDate settlement date
   */
  public CommodityForwardDefinition(final ZonedDateTime expiryDate, final StandardId underlying, final double unitAmount,
      final double amount, final String unitName, final double referencePrice, final Currency currency, final ZonedDateTime settlementDate) {
    this(expiryDate, underlying, unitAmount, null, null, amount, unitName, SettlementType.CASH, referencePrice, currency, settlementDate);
  }

  /**
   * Gets the expiryDate.
   * @return the expiryDate
   */
  public ZonedDateTime getExpiryDate() {
    return _expiryDate;
  }

  /**
   * Gets the underlying.
   * @return the underlying
   */
  public StandardId getUnderlying() {
    return _underlying;
  }

  /**
   * Gets the unitAmount.
   * @return the unitAmount
   */
  public double getUnitAmount() {
    return _unitAmount;
  }

  /**
   * Gets the firstDeliveryDate.
   * @return the firstDeliveryDate
   */
  public ZonedDateTime getFirstDeliveryDate() {
    return _firstDeliveryDate;
  }

  /**
   * Gets the lastDeliveryDate.
   * @return the lastDeliveryDate
   */
  public ZonedDateTime getLastDeliveryDate() {
    return _lastDeliveryDate;
  }

  /**
   * Gets the amount.
   * @return the amount
   */
  public double getAmount() {
    return _amount;
  }

  /**
   * Gets the unitName.
   * @return the unitName
   */
  public String getUnitName() {
    return _unitName;
  }

  /**
   * Gets the settlementType.
   * @return the settlementType
   */
  public SettlementType getSettlementType() {
    return _settlementType;
  }

  /**
   * Gets the referencePrice.
   * @return the referencePrice
   */
  public double getReferencePrice() {
    return _referencePrice;
  }

  /**
   * Gets the currency.
   * @return the currency
   */
  public Currency getCurrency() {
    return _currency;
  }

  /**
   * Gets the settlement date.
   * @return the settlement date
   */
  public ZonedDateTime getSettlementDate() {
    return _settlementDate;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _underlying.hashCode();
    result = prime * result + _expiryDate.hashCode();
    if (_firstDeliveryDate != null) {
      result = prime * result + _firstDeliveryDate.hashCode();
    }
    if (_lastDeliveryDate != null) {
      result = prime * result + _lastDeliveryDate.hashCode();
    }
    result = prime * result + _unitName.hashCode();
    result = prime * result + _settlementType.hashCode();
    result = prime * result + _currency.hashCode();
    result = prime * result + _settlementDate.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_amount);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_unitAmount);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_referencePrice);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof CommodityForwardDefinition)) {
      return false;
    }
    final CommodityForwardDefinition<?> other = (CommodityForwardDefinition<?>) obj;
    if (!Objects.equals(_expiryDate, other._expiryDate)) {
      return false;
    }
    if (!Objects.equals(_underlying, other._underlying)) {
      return false;
    }
    if (!Objects.equals(_firstDeliveryDate, other._firstDeliveryDate)) {
      return false;
    }
    if (!Objects.equals(_lastDeliveryDate, other._lastDeliveryDate)) {
      return false;
    }
    if (!Objects.equals(_unitName, other._unitName)) {
      return false;
    }
    if (!Objects.equals(_settlementType, other._settlementType)) {
      return false;
    }
    if (!Objects.equals(_currency, other._currency)) {
      return false;
    }
    if (!Objects.equals(_settlementDate, other._settlementDate)) {
      return false;
    }
    if (Double.compare(_amount, other._amount) != 0) {
      return false;
    }
    if (Double.compare(_unitAmount, other._unitAmount) != 0) {
      return false;
    }
    if (Double.compare(_referencePrice, other._referencePrice) != 0) {
      return false;
    }

    return true;
  }

}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.derivative.ForexNonDeliverableForward;
import com.opengamma.analytics.financial.forex.derivative.ForexNonDeliverableOption;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionDigital;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionSingleBarrier;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.forex.derivative.ForexSwap;
import com.opengamma.analytics.financial.instrument.index.IndexDeposit;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponIborRatchet;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillTotalReturnSwap;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondIborSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondIborTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondInterestIndexedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondInterestIndexedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondTotalReturnSwap;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositCounterpart;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositIbor;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositZero;
import com.opengamma.analytics.financial.interestrate.fra.derivative.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuture;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFutureOptionPremiumSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFutureOptionPremiumTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesYieldAverageSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesYieldAverageTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.FederalFundsFutureSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.FederalFundsFutureTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.SwapFuturesPriceDeliverableSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.SwapFuturesPriceDeliverableTransaction;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CapFloorInflationYearOnYearInterpolation;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CapFloorInflationYearOnYearMonthly;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CapFloorInflationZeroCouponInterpolation;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CapFloorInflationZeroCouponMonthly;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationYearOnYearInterpolation;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationYearOnYearInterpolationWithMargin;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationYearOnYearMonthly;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationYearOnYearMonthlyWithMargin;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponInterpolation;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponInterpolationGearing;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponMonthly;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponMonthlyGearing;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorCMS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorCMSSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponCMS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedAccruedCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedFxReset;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborAverage;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborAverageFixingDates;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborAverageFixingDatesCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborAverageFixingDatesCompoundingFlatSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompoundingFlatSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompoundingSimpleSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompoundingSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborFxReset;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborGearing;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponON;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONArithmeticAverage;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONArithmeticAverageSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONArithmeticAverageSpreadSimplified;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONCompounded;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.DepositIndexCoupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.InterpolatedStubCoupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCompoundingCoupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapMultileg;
import com.opengamma.analytics.financial.interestrate.swap.derivative.TotalReturnSwap;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionBermudaFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedCompoundedONCompounded;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedCompoundedONCompounded;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;

/**
 *
 * @param <DATA_TYPE> The type of the data
 * @param <RESULT_TYPE> The return type of the calculation
 */
@SuppressWarnings("deprecation")
public interface InstrumentDerivativeVisitor<DATA_TYPE, RESULT_TYPE> {

  // Two arguments

  /**
   * Fixed-coupon bond security method that takes data.
   * @param bond A fixed-coupon bond security
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitBondFixedSecurity(BondFixedSecurity bond, DATA_TYPE data);

  /**
   * Fixed-coupon bond transaction method that takes data.
   * @param bond A fixed-coupon bond transaction
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitBondFixedTransaction(BondFixedTransaction bond, DATA_TYPE data);

  /**
   * Ibor bond security method that takes data.
   * @param bond An ibor bond security
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitBondIborSecurity(BondIborSecurity bond, DATA_TYPE data);

  /**
   * Ibor bond transaction method that takes data.
   * @param bond An ibor bond transaction
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitBondIborTransaction(BondIborTransaction bond, DATA_TYPE data);

  /**
   * Bill security method that takes data.
   * @param bill A bill security
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitBillSecurity(BillSecurity bill, DATA_TYPE data);

  /**
   * Bill transaction method that takes data.
   * @param bill A bill transaction
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitBillTransaction(BillTransaction bill, DATA_TYPE data);

  /**
   * Generic annuity method that takes data.
   * @param genericAnnuity A generic annuity
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitGenericAnnuity(Annuity<? extends Payment> genericAnnuity, DATA_TYPE data);

  /**
   * Fixed-coupon annuity method that takes data.
   * @param fixedCouponAnnuity A fixed-coupon annuity
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitFixedCouponAnnuity(AnnuityCouponFixed fixedCouponAnnuity, DATA_TYPE data);

  /**
   * Ratcheting ibor coupon method that takes data.
   * @param annuity A annuity
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitAnnuityCouponIborRatchet(AnnuityCouponIborRatchet annuity, DATA_TYPE data);

  /**
   * Fixed-coupon swap method that takes data.
   * @param swap A fixed-coupon swap
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitFixedCouponSwap(SwapFixedCoupon<?> swap, DATA_TYPE data);

  /**
   * Fixed-compounding swap method that takes data.
   * @param swap A fixed-compounding swap
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitFixedCompoundingCouponSwap(SwapFixedCompoundingCoupon<?> swap, DATA_TYPE data);

  /**
   * Cash-settled swaption method that takes data.
   * @param swaption A cash-settled swaption
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitSwaptionCashFixedIbor(SwaptionCashFixedIbor swaption, DATA_TYPE data);

  /**
   * Physically-settled swaption method that takes data.
   * @param swaption A physically-settled swaption
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitSwaptionPhysicalFixedIbor(SwaptionPhysicalFixedIbor swaption, DATA_TYPE data);

  /**
   * Bermudan swaption method that takes data.
   * @param swaption A Bermudan swaption
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitSwaptionBermudaFixedIbor(SwaptionBermudaFixedIbor swaption, DATA_TYPE data);

  /**
   * A BRL-type swaption method that takes data.
   * @param swaption A swaption
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitSwaptionCashFixedCompoundedONCompounded(SwaptionCashFixedCompoundedONCompounded swaption, DATA_TYPE data);

  RESULT_TYPE visitSwaptionPhysicalFixedCompoundedONCompounded(SwaptionPhysicalFixedCompoundedONCompounded swaption, DATA_TYPE data);

  RESULT_TYPE visitCash(Cash cash, DATA_TYPE data);

  RESULT_TYPE visitFixedPayment(PaymentFixed payment, DATA_TYPE data);

  RESULT_TYPE visitCouponCMS(CouponCMS payment, DATA_TYPE data);

  RESULT_TYPE visitCapFloorIbor(CapFloorIbor payment, DATA_TYPE data);

  RESULT_TYPE visitCapFloorCMS(CapFloorCMS payment, DATA_TYPE data);

  RESULT_TYPE visitCapFloorCMSSpread(CapFloorCMSSpread payment, DATA_TYPE data);

  RESULT_TYPE visitForwardRateAgreement(ForwardRateAgreement fra, DATA_TYPE data);

  RESULT_TYPE visitBondCapitalIndexedSecurity(BondCapitalIndexedSecurity<?> bond, DATA_TYPE data);

  RESULT_TYPE visitBondCapitalIndexedTransaction(BondCapitalIndexedTransaction<?> bond, DATA_TYPE data);

  RESULT_TYPE visitBondInterestIndexedTransaction(BondInterestIndexedTransaction<?, ?> bond, DATA_TYPE data);

  // One argument

  RESULT_TYPE visitBondFixedSecurity(BondFixedSecurity bond);

  RESULT_TYPE visitBondFixedTransaction(BondFixedTransaction bond);

  RESULT_TYPE visitBondIborSecurity(BondIborSecurity bond);

  RESULT_TYPE visitBondIborTransaction(BondIborTransaction bond);

  RESULT_TYPE visitBillSecurity(BillSecurity bill);

  RESULT_TYPE visitBillTransaction(BillTransaction bill);

  RESULT_TYPE visitGenericAnnuity(Annuity<? extends Payment> genericAnnuity);

  RESULT_TYPE visitFixedCouponAnnuity(AnnuityCouponFixed fixedCouponAnnuity);

  RESULT_TYPE visitAnnuityCouponIborRatchet(AnnuityCouponIborRatchet annuity);

  RESULT_TYPE visitFixedCouponSwap(SwapFixedCoupon<?> swap);

  RESULT_TYPE visitFixedCompoundingCouponSwap(SwapFixedCompoundingCoupon<?> swap);

  RESULT_TYPE visitSwaptionCashFixedIbor(SwaptionCashFixedIbor swaption);

  RESULT_TYPE visitSwaptionPhysicalFixedIbor(SwaptionPhysicalFixedIbor swaption);

  RESULT_TYPE visitSwaptionCashFixedCompoundedONCompounded(SwaptionCashFixedCompoundedONCompounded swaption);

  RESULT_TYPE visitSwaptionPhysicalFixedCompoundedONCompounded(SwaptionPhysicalFixedCompoundedONCompounded swaption);

  RESULT_TYPE visitSwaptionBermudaFixedIbor(SwaptionBermudaFixedIbor swaption);

  RESULT_TYPE visitCash(Cash cash);

  RESULT_TYPE visitFixedPayment(PaymentFixed payment);

  RESULT_TYPE visitCouponCMS(CouponCMS payment);

  RESULT_TYPE visitCapFloorIbor(CapFloorIbor payment);

  RESULT_TYPE visitCapFloorCMS(CapFloorCMS payment);

  RESULT_TYPE visitCapFloorCMSSpread(CapFloorCMSSpread payment);

  RESULT_TYPE visitForwardRateAgreement(ForwardRateAgreement fra);

  RESULT_TYPE visitBondCapitalIndexedSecurity(BondCapitalIndexedSecurity<?> bond);

  RESULT_TYPE visitBondCapitalIndexedTransaction(BondCapitalIndexedTransaction<?> bond);

  RESULT_TYPE visitBondInterestIndexedTransaction(BondInterestIndexedTransaction<?, ?> bond);

  RESULT_TYPE visitBondInterestIndexedSecurity(BondInterestIndexedSecurity<?, ?> bond);

  RESULT_TYPE visitBondInterestIndexedSecurity(BondInterestIndexedSecurity<?, ?> bond, DATA_TYPE data);

  // -----     Coupons     -----

  RESULT_TYPE visitCouponFixed(CouponFixed payment, DATA_TYPE data);

  RESULT_TYPE visitCouponFixed(CouponFixed payment);

  RESULT_TYPE visitCouponFixedCompounding(CouponFixedCompounding payment, DATA_TYPE data);

  RESULT_TYPE visitCouponFixedCompounding(CouponFixedCompounding payment);

  RESULT_TYPE visitCouponFixedAccruedCompounding(CouponFixedAccruedCompounding payment, DATA_TYPE data);

  RESULT_TYPE visitCouponFixedAccruedCompounding(CouponFixedAccruedCompounding payment);

  RESULT_TYPE visitInterpolatedStubCoupon(InterpolatedStubCoupon<? extends DepositIndexCoupon<? extends IndexDeposit>, ? extends IndexDeposit> payment, DATA_TYPE data);

  RESULT_TYPE visitInterpolatedStubCoupon(InterpolatedStubCoupon<? extends DepositIndexCoupon<? extends IndexDeposit>, ? extends IndexDeposit> payment);

  RESULT_TYPE visitCouponFixedFxReset(CouponFixedFxReset payment, DATA_TYPE data);

  RESULT_TYPE visitCouponFixedFxReset(CouponFixedFxReset payment);

  RESULT_TYPE visitCouponIborFxReset(CouponIborFxReset payment, DATA_TYPE data);

  RESULT_TYPE visitCouponIborFxReset(CouponIborFxReset payment);

  RESULT_TYPE visitCouponIbor(CouponIbor payment, DATA_TYPE data);

  RESULT_TYPE visitCouponIbor(CouponIbor payment);

  RESULT_TYPE visitCouponIborAverage(CouponIborAverage payment, DATA_TYPE data);

  RESULT_TYPE visitCouponIborAverage(CouponIborAverage payment);

  RESULT_TYPE visitCouponIborSpread(CouponIborSpread payment, DATA_TYPE data);

  RESULT_TYPE visitCouponIborSpread(CouponIborSpread payment);

  RESULT_TYPE visitCouponIborGearing(CouponIborGearing payment);

  RESULT_TYPE visitCouponIborGearing(CouponIborGearing payment, DATA_TYPE data);

  RESULT_TYPE visitCouponIborCompounding(CouponIborCompounding payment);

  RESULT_TYPE visitCouponIborCompounding(CouponIborCompounding payment, DATA_TYPE data);

  RESULT_TYPE visitCouponIborCompoundingSpread(CouponIborCompoundingSpread payment);

  RESULT_TYPE visitCouponIborCompoundingSpread(CouponIborCompoundingSpread payment, DATA_TYPE data);

  RESULT_TYPE visitCouponIborCompoundingFlatSpread(CouponIborCompoundingFlatSpread payment);

  RESULT_TYPE visitCouponIborCompoundingFlatSpread(CouponIborCompoundingFlatSpread payment, DATA_TYPE data);

  RESULT_TYPE visitCouponIborCompoundingSimpleSpread(CouponIborCompoundingSimpleSpread payment);

  RESULT_TYPE visitCouponIborCompoundingSimpleSpread(CouponIborCompoundingSimpleSpread payment, DATA_TYPE data);

  RESULT_TYPE visitCouponOIS(CouponON payment, DATA_TYPE data);

  RESULT_TYPE visitCouponOIS(CouponON payment);

  RESULT_TYPE visitCouponONCompounded(CouponONCompounded payment, DATA_TYPE data);

  RESULT_TYPE visitCouponONCompounded(CouponONCompounded payment);

  RESULT_TYPE visitCouponONSpread(CouponONSpread payment, DATA_TYPE data);

  RESULT_TYPE visitCouponONSpread(CouponONSpread payment);

  RESULT_TYPE visitCouponONArithmeticAverage(CouponONArithmeticAverage payment, DATA_TYPE data);

  RESULT_TYPE visitCouponONArithmeticAverage(CouponONArithmeticAverage payment);

  RESULT_TYPE visitCouponONArithmeticAverageSpread(CouponONArithmeticAverageSpread payment, DATA_TYPE data);

  RESULT_TYPE visitCouponONArithmeticAverageSpread(CouponONArithmeticAverageSpread payment);

  RESULT_TYPE visitCouponONArithmeticAverageSpreadSimplified(CouponONArithmeticAverageSpreadSimplified payment, DATA_TYPE data);

  RESULT_TYPE visitCouponONArithmeticAverageSpreadSimplified(CouponONArithmeticAverageSpreadSimplified payment);

  RESULT_TYPE visitCouponIborAverageFixingDates(CouponIborAverageFixingDates payment, DATA_TYPE data);

  RESULT_TYPE visitCouponIborAverageFixingDates(CouponIborAverageFixingDates payment);

  RESULT_TYPE visitCouponIborAverageCompounding(CouponIborAverageFixingDatesCompounding payment, DATA_TYPE data);

  RESULT_TYPE visitCouponIborAverageCompounding(CouponIborAverageFixingDatesCompounding payment);

  RESULT_TYPE visitCouponIborAverageFlatCompoundingSpread(CouponIborAverageFixingDatesCompoundingFlatSpread payment, DATA_TYPE data);

  RESULT_TYPE visitCouponIborAverageFlatCompoundingSpread(CouponIborAverageFixingDatesCompoundingFlatSpread payment);

  // -----     Swap     -----

  RESULT_TYPE visitSwap(Swap<?, ?> swap, DATA_TYPE data);

  RESULT_TYPE visitSwap(Swap<?, ?> swap);

  RESULT_TYPE visitSwapMultileg(SwapMultileg swap, DATA_TYPE data);

  RESULT_TYPE visitSwapMultileg(SwapMultileg swap);

  // -----     Inflation     -----

  RESULT_TYPE visitCouponInflationZeroCouponMonthly(CouponInflationZeroCouponMonthly coupon, DATA_TYPE data);

  RESULT_TYPE visitCouponInflationZeroCouponMonthly(CouponInflationZeroCouponMonthly coupon);

  RESULT_TYPE visitCouponInflationZeroCouponMonthlyGearing(CouponInflationZeroCouponMonthlyGearing coupon, DATA_TYPE data);

  RESULT_TYPE visitCouponInflationZeroCouponMonthlyGearing(CouponInflationZeroCouponMonthlyGearing coupon);

  RESULT_TYPE visitCouponInflationZeroCouponInterpolation(CouponInflationZeroCouponInterpolation coupon, DATA_TYPE data);

  RESULT_TYPE visitCouponInflationZeroCouponInterpolation(CouponInflationZeroCouponInterpolation coupon);

  RESULT_TYPE visitCouponInflationZeroCouponInterpolationGearing(CouponInflationZeroCouponInterpolationGearing coupon, DATA_TYPE data);

  RESULT_TYPE visitCouponInflationZeroCouponInterpolationGearing(CouponInflationZeroCouponInterpolationGearing coupon);

  RESULT_TYPE visitCouponInflationYearOnYearMonthly(CouponInflationYearOnYearMonthly coupon, DATA_TYPE data);

  RESULT_TYPE visitCouponInflationYearOnYearMonthly(CouponInflationYearOnYearMonthly coupon);

  RESULT_TYPE visitCouponInflationYearOnYearMonthlyWithMargin(CouponInflationYearOnYearMonthlyWithMargin coupon, DATA_TYPE data);

  RESULT_TYPE visitCouponInflationYearOnYearMonthlyWithMargin(CouponInflationYearOnYearMonthlyWithMargin coupon);

  RESULT_TYPE visitCouponInflationYearOnYearInterpolation(CouponInflationYearOnYearInterpolation coupon, DATA_TYPE data);

  RESULT_TYPE visitCouponInflationYearOnYearInterpolation(CouponInflationYearOnYearInterpolation coupon);

  RESULT_TYPE visitCouponInflationYearOnYearInterpolationWithMargin(CouponInflationYearOnYearInterpolationWithMargin coupon, DATA_TYPE data);

  RESULT_TYPE visitCouponInflationYearOnYearInterpolationWithMargin(CouponInflationYearOnYearInterpolationWithMargin coupon);

  RESULT_TYPE visitCapFloorInflationZeroCouponInterpolation(CapFloorInflationZeroCouponInterpolation coupon, DATA_TYPE data);

  RESULT_TYPE visitCapFloorInflationZeroCouponInterpolation(CapFloorInflationZeroCouponInterpolation coupon);

  RESULT_TYPE visitCapFloorInflationZeroCouponMonthly(CapFloorInflationZeroCouponMonthly coupon, DATA_TYPE data);

  RESULT_TYPE visitCapFloorInflationZeroCouponMonthly(CapFloorInflationZeroCouponMonthly coupon);

  RESULT_TYPE visitCapFloorInflationYearOnYearInterpolation(CapFloorInflationYearOnYearInterpolation coupon, DATA_TYPE data);

  RESULT_TYPE visitCapFloorInflationYearOnYearInterpolation(CapFloorInflationYearOnYearInterpolation coupon);

  RESULT_TYPE visitCapFloorInflationYearOnYearMonthly(CapFloorInflationYearOnYearMonthly coupon, DATA_TYPE data);

  RESULT_TYPE visitCapFloorInflationYearOnYearMonthly(CapFloorInflationYearOnYearMonthly coupon);

  // -----     Futures   -----

  RESULT_TYPE visitBondFuture(BondFuture bondFuture, DATA_TYPE data);

  RESULT_TYPE visitBondFuture(BondFuture future);

  RESULT_TYPE visitBondFuturesSecurity(BondFuturesSecurity bondFutures, DATA_TYPE data);

  RESULT_TYPE visitBondFuturesSecurity(BondFuturesSecurity bondFutures);

  RESULT_TYPE visitBondFuturesTransaction(BondFuturesTransaction bondFutures, DATA_TYPE data);

  RESULT_TYPE visitBondFuturesTransaction(BondFuturesTransaction bondFutures);

  RESULT_TYPE visitBondFuturesYieldAverageSecurity(BondFuturesYieldAverageSecurity bondFutures, DATA_TYPE data);

  RESULT_TYPE visitBondFuturesYieldAverageSecurity(BondFuturesYieldAverageSecurity bondFutures);

  RESULT_TYPE visitYieldAverageBondFuturesTransaction(BondFuturesYieldAverageTransaction bondFutures, DATA_TYPE data);

  RESULT_TYPE visitYieldAverageBondFuturesTransaction(BondFuturesYieldAverageTransaction bondFutures);

  RESULT_TYPE visitInterestRateFutureTransaction(InterestRateFutureTransaction future, DATA_TYPE data);

  RESULT_TYPE visitInterestRateFutureTransaction(InterestRateFutureTransaction future);

  RESULT_TYPE visitInterestRateFutureSecurity(InterestRateFutureSecurity future, DATA_TYPE data);

  RESULT_TYPE visitInterestRateFutureSecurity(InterestRateFutureSecurity future);

  RESULT_TYPE visitFederalFundsFutureSecurity(FederalFundsFutureSecurity future, DATA_TYPE data);

  RESULT_TYPE visitFederalFundsFutureSecurity(FederalFundsFutureSecurity future);

  RESULT_TYPE visitFederalFundsFutureTransaction(FederalFundsFutureTransaction future, DATA_TYPE data);

  RESULT_TYPE visitFederalFundsFutureTransaction(FederalFundsFutureTransaction future);

  RESULT_TYPE visitSwapFuturesPriceDeliverableSecurity(SwapFuturesPriceDeliverableSecurity futures, DATA_TYPE data);

  RESULT_TYPE visitSwapFuturesPriceDeliverableSecurity(SwapFuturesPriceDeliverableSecurity futures);

  RESULT_TYPE visitSwapFuturesPriceDeliverableTransaction(SwapFuturesPriceDeliverableTransaction futures, DATA_TYPE data);

  RESULT_TYPE visitSwapFuturesPriceDeliverableTransaction(SwapFuturesPriceDeliverableTransaction futures);

  // -----     Futures options   -----

  RESULT_TYPE visitBondFuturesOptionMarginSecurity(BondFuturesOptionMarginSecurity option, DATA_TYPE data);

  RESULT_TYPE visitBondFuturesOptionMarginSecurity(BondFuturesOptionMarginSecurity option);

  RESULT_TYPE visitBondFuturesOptionMarginTransaction(BondFuturesOptionMarginTransaction option, DATA_TYPE data);

  RESULT_TYPE visitBondFuturesOptionMarginTransaction(BondFuturesOptionMarginTransaction option);

  RESULT_TYPE visitBondFutureOptionPremiumSecurity(BondFutureOptionPremiumSecurity option, DATA_TYPE data);

  RESULT_TYPE visitBondFutureOptionPremiumSecurity(BondFutureOptionPremiumSecurity option);

  RESULT_TYPE visitBondFutureOptionPremiumTransaction(BondFutureOptionPremiumTransaction option, DATA_TYPE data);

  RESULT_TYPE visitBondFutureOptionPremiumTransaction(BondFutureOptionPremiumTransaction option);

  RESULT_TYPE visitInterestRateFutureOptionMarginSecurity(InterestRateFutureOptionMarginSecurity option, DATA_TYPE data);

  RESULT_TYPE visitInterestRateFutureOptionMarginSecurity(InterestRateFutureOptionMarginSecurity option);

  RESULT_TYPE visitInterestRateFutureOptionMarginTransaction(InterestRateFutureOptionMarginTransaction option, DATA_TYPE data);

  RESULT_TYPE visitInterestRateFutureOptionMarginTransaction(InterestRateFutureOptionMarginTransaction option);

  RESULT_TYPE visitInterestRateFutureOptionPremiumSecurity(InterestRateFutureOptionPremiumSecurity option, DATA_TYPE data);

  RESULT_TYPE visitInterestRateFutureOptionPremiumSecurity(InterestRateFutureOptionPremiumSecurity option);

  RESULT_TYPE visitInterestRateFutureOptionPremiumTransaction(InterestRateFutureOptionPremiumTransaction option, DATA_TYPE data);

  RESULT_TYPE visitInterestRateFutureOptionPremiumTransaction(InterestRateFutureOptionPremiumTransaction option);

  // -----     Deposit     -----

  RESULT_TYPE visitDepositIbor(DepositIbor deposit, DATA_TYPE data);

  RESULT_TYPE visitDepositIbor(DepositIbor deposit);

  RESULT_TYPE visitDepositCounterpart(DepositCounterpart deposit, DATA_TYPE data);

  RESULT_TYPE visitDepositCounterpart(DepositCounterpart deposit);

  RESULT_TYPE visitDepositZero(DepositZero deposit, DATA_TYPE data);

  RESULT_TYPE visitDepositZero(DepositZero deposit);

  // -----     Forex     -----

  RESULT_TYPE visitForex(Forex derivative, DATA_TYPE data);

  RESULT_TYPE visitForex(Forex derivative);

  RESULT_TYPE visitForexSwap(ForexSwap derivative, DATA_TYPE data);

  RESULT_TYPE visitForexSwap(ForexSwap derivative);

  RESULT_TYPE visitForexOptionVanilla(ForexOptionVanilla derivative, DATA_TYPE data);

  RESULT_TYPE visitForexOptionVanilla(ForexOptionVanilla derivative);

  RESULT_TYPE visitForexOptionSingleBarrier(ForexOptionSingleBarrier derivative, DATA_TYPE data);

  RESULT_TYPE visitForexOptionSingleBarrier(ForexOptionSingleBarrier derivative);

  RESULT_TYPE visitForexNonDeliverableForward(ForexNonDeliverableForward derivative, DATA_TYPE data);

  RESULT_TYPE visitForexNonDeliverableForward(ForexNonDeliverableForward derivative);

  RESULT_TYPE visitForexNonDeliverableOption(ForexNonDeliverableOption derivative, DATA_TYPE data);

  RESULT_TYPE visitForexNonDeliverableOption(ForexNonDeliverableOption derivative);

  RESULT_TYPE visitForexOptionDigital(ForexOptionDigital derivative, DATA_TYPE data);

  RESULT_TYPE visitForexOptionDigital(ForexOptionDigital derivative);

  //  -----     Variance and volatility swaps     -----

  /**
   * The total return swap method.
   * @param totalReturnSwap A total return swap
   * @return The result
   */
  RESULT_TYPE visitTotalReturnSwap(TotalReturnSwap totalReturnSwap);

  /**
   * The total return swap method.
   * @param totalReturnSwap A total return swap
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitTotalReturnSwap(TotalReturnSwap totalReturnSwap, DATA_TYPE data);

  /**
   * The bond total return swap method.
   * @param totalReturnSwap A bond total return swap
   * @return The result
   */
  RESULT_TYPE visitBondTotalReturnSwap(BondTotalReturnSwap totalReturnSwap);

  /**
   * The bond total return swap method.
   * @param totalReturnSwap A bond total return swap
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitBondTotalReturnSwap(BondTotalReturnSwap totalReturnSwap, DATA_TYPE data);

  /**
   * The bill total return swap method.
   * @param totalReturnSwap A bill total return swap
   * @return The result
   */
  RESULT_TYPE visitBillTotalReturnSwap(BillTotalReturnSwap totalReturnSwap);

  /**
   * The bill total return swap method.
   * @param totalReturnSwap A bill total return swap
   * @param data The data
   * @return The result
   */
  RESULT_TYPE visitBillTotalReturnSwap(BillTotalReturnSwap totalReturnSwap, DATA_TYPE data);

}

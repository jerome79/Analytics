/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;


import java.time.LocalDate;
import java.time.ZonedDateTime;

import com.opengamma.analytics.convention.calendar.Calendar;
import com.opengamma.analytics.convention.daycount.DayCount;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.interestrate.swap.provider.SwapFixedCouponDiscountingMethod;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.util.timeseries.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.strata.basics.currency.MultiCurrencyAmount;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Computes par rate (excluding accrued interests) and accrued interest. <br> 
 * <b>Use {@link ParRateDiscountingCalculator} for standard definition of par rate.<b>
 */
public class SwapCleanDiscountingCalculator {
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final SwapFixedCouponDiscountingMethod METHOD_SWAP = SwapFixedCouponDiscountingMethod.getInstance();

  /**
   * Computes par rate excluding accrued interest.
   * @param swapDefinition Fixed vs Ibor swap definition
   * @param fixedLegDayCount Day count for fixed leg 
   * @param iborLegDayCount Day count for Ibor leg
   * @param calendar The calendar
   * @param valuationDate The valuation date
   * @param indexTimeSeries Index fixing time series 
   * @param multicurves The multi-curve
   * @return The par rate
   */
  public Double parRate(SwapFixedIborDefinition swapDefinition, DayCount fixedLegDayCount,
      DayCount iborLegDayCount, Calendar calendar, ZonedDateTime valuationDate,
      ZonedDateTimeDoubleTimeSeries indexTimeSeries, MulticurveProviderDiscount multicurves) {
    ArgChecker.notNull(swapDefinition, "swapDefinition");
    ArgChecker.notNull(fixedLegDayCount, "fixedLegDayCount");
    ArgChecker.notNull(iborLegDayCount, "iborLegDayCount");
    ArgChecker.notNull(calendar, "calendar");
    ArgChecker.notNull(valuationDate, "valuationDate");
    ArgChecker.notNull(indexTimeSeries, "indexTimeSeries");
    ArgChecker.notNull(multicurves, "multicurves");
    checkNotionalAndFixedRate(swapDefinition);

    Annuity<? extends Coupon> iborLeg = swapDefinition.getIborLeg().toDerivative(valuationDate, indexTimeSeries);
    double dirtyIborLegPV = iborLeg.accept(PVDC, multicurves).getAmount(iborLeg.getCurrency()).getAmount();
    double iborLegAccruedInterest = getAccrued(iborLegDayCount, calendar, valuationDate,
        swapDefinition.getIborLeg(), indexTimeSeries);
    double cleanFloatingPV = (dirtyIborLegPV - iborLegAccruedInterest) *
        Math.signum(iborLeg.getNthPayment(0).getNotional());
    
    AnnuityCouponFixed fixedLeg = swapDefinition.getFixedLeg().toDerivative(valuationDate);
    double accruedAmount = getAccrued(fixedLegDayCount, calendar, valuationDate, swapDefinition.getFixedLeg(),
        indexTimeSeries) * Math.signum(fixedLeg.getNthPayment(0).getNotional());
    double dirtyAnnuity = METHOD_SWAP.presentValueBasisPoint(new SwapFixedCoupon<>(fixedLeg, iborLeg),
        multicurves);
    double cleanAnnuity = dirtyAnnuity - accruedAmount;

    return cleanFloatingPV / cleanAnnuity;
  }

  /**
   * Computes accrued interest
   * @param swapDefinition Fixed vs Ibor swap definition
   * @param fixedLegDayCount Day count for fixed leg 
   * @param iborLegDayCount Day count for Ibor leg
   * @param calendar The calendar
   * @param valuationDate The valuation date
   * @param indexTimeSeries Index fixing time series 
   * @param multicurves The multi-curve
   * @return The accrued interest
   */
  public MultiCurrencyAmount accruedInterest(SwapFixedIborDefinition swapDefinition, DayCount fixedLegDayCount,
      DayCount iborLegDayCount, Calendar calendar, ZonedDateTime valuationDate,
      ZonedDateTimeDoubleTimeSeries indexTimeSeries, MulticurveProviderDiscount multicurves) {
    ArgChecker.notNull(swapDefinition, "swapDefinition");
    ArgChecker.notNull(fixedLegDayCount, "fixedLegDayCount");
    ArgChecker.notNull(iborLegDayCount, "iborLegDayCount");
    ArgChecker.notNull(calendar, "calendar");
    ArgChecker.notNull(valuationDate, "valuationDate");
    ArgChecker.notNull(indexTimeSeries, "indexTimeSeries");
    ArgChecker.notNull(multicurves, "multicurves");
    checkNotionalAndFixedRate(swapDefinition);

    double iborLegAccruedInterest = getAccrued(iborLegDayCount, calendar, valuationDate, swapDefinition.getIborLeg(),
        indexTimeSeries);

    CouponFixedDefinition refFixed = swapDefinition.getFixedLeg().getNthPayment(0);
    double fixedLegAccruedInterest = getAccrued(fixedLegDayCount, calendar, valuationDate,
        swapDefinition.getFixedLeg(), indexTimeSeries) * refFixed.getRate();

    return MultiCurrencyAmount.of(swapDefinition.getCurrency(), iborLegAccruedInterest + fixedLegAccruedInterest);
  }

  private double getAccrued(DayCount dayCount, Calendar calendar, ZonedDateTime valuationDate,
      AnnuityDefinition<? extends CouponDefinition> annuity, ZonedDateTimeDoubleTimeSeries indexTimeSeries) {
    LocalDate date = valuationDate.toLocalDate();
    double res = 0.0;
    CouponDefinition[] payments = annuity.getPayments();
    for (CouponDefinition payment : payments) {
      if (payment.getAccrualStartDate().toLocalDate().isBefore(date) &&
          !payment.getPaymentDate().toLocalDate().isBefore(date)) {
        double rate;
        if (payment instanceof CouponIborDefinition) {
          CouponIborDefinition casted = (CouponIborDefinition) payment;
          Coupon coupon = casted.toDerivative(valuationDate, indexTimeSeries);
          ArgChecker.isTrue(coupon instanceof CouponFixed,
              "index should be fixed before accrual starts for standard vanilla swap");
          CouponFixed couponFixed = (CouponFixed) coupon;
          rate = couponFixed.getFixedRate();
        } else if (payment instanceof CouponFixedDefinition) {
          rate = 1.0;
        } else {
          throw new IllegalArgumentException("This annuity type is not supported");
        }
        res += getAccrued(dayCount, calendar, valuationDate, payment) * rate;
      }
    }
    return res;
  }

  private double getAccrued(DayCount dayCount, Calendar calendar, ZonedDateTime valuationDate, CouponDefinition coupon) {
    double accruedYearFraction = dayCount.getDayCountFraction(coupon.getAccrualStartDate(), valuationDate, calendar);
    return accruedYearFraction * coupon.getNotional();
  }

  private void checkNotionalAndFixedRate(SwapFixedIborDefinition swapDefinition) {
    AnnuityCouponIborDefinition iborLeg = swapDefinition.getIborLeg();
    int nIbor = iborLeg.getNumberOfPayments();
    double notioanl = iborLeg.getNthPayment(0).getNotional();
    for (int i = 1; i < nIbor; ++i) {
      ArgChecker.isTrue(notioanl == iborLeg.getNthPayment(i).getNotional(),
          "Notional should be constant in both the legs");
    }
    AnnuityCouponFixedDefinition fixedLeg = swapDefinition.getFixedLeg();
    int nFixed = fixedLeg.getNumberOfPayments();
    double rate = fixedLeg.getNthPayment(0).getRate();
    notioanl *= -1.0; // payer/receiver conversion
    ArgChecker.isTrue(notioanl == fixedLeg.getNthPayment(0).getNotional(),
        "Notional should be constant in both the legs");
    for (int i = 1; i < nFixed; ++i) {
      ArgChecker.isTrue(rate == fixedLeg.getNthPayment(i).getRate(), "Fixed rate should be constant");
      ArgChecker.isTrue(notioanl == fixedLeg.getNthPayment(i).getNotional(),
          "Notional should be constant in both the legs");
    }
  }
}

/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swap.provider;

import static org.testng.AssertJUnit.assertEquals;

import java.time.LocalDate;
import java.time.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.convention.rolldate.RollConvention;
import com.opengamma.analytics.financial.instrument.NotionalProvider;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.annuity.FloatingAnnuityDefinitionBuilder;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.payment.CouponDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedFxResetDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborFxResetDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.interestrate.datasets.StandardDataSetsMulticurveEUR;
import com.opengamma.analytics.financial.interestrate.datasets.StandardDataSetsMulticurveUSD;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.util.time.DateUtils;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.currency.FxMatrix;
import com.opengamma.strata.basics.currency.MultiCurrencyAmount;
import com.opengamma.strata.basics.date.BusinessDayAdjustment;
import com.opengamma.strata.basics.date.BusinessDayConventions;
import com.opengamma.strata.basics.date.DaysAdjustment;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.basics.date.HolidayCalendars;
import com.opengamma.strata.collect.tuple.Pair;

@Test
public class SwapCrossCurrencyUsdEurE2ETest {

  private static final ZonedDateTime VALUATION_DATE = DateUtils.getUTCDate(2014, 1, 22);
  private static final Currency EUR = Currency.EUR;
  private static final Currency USD = Currency.USD;
  private static final HolidayCalendar LON = HolidayCalendars.SAT_SUN;
  private static final HolidayCalendar NYC = HolidayCalendars.SAT_SUN;
  private static final HolidayCalendar TARGET = HolidayCalendars.SAT_SUN;
  private static final BusinessDayAdjustment ADJUSTED_DATE_LIBOR_LONNYC =
      BusinessDayAdjustment.of(BusinessDayConventions.MODIFIED_FOLLOWING, NYC); // HolidayCalendar should be LON+NYC
  private static final DaysAdjustment OFFSET_ADJ_LIBOR_TAR_2 =
      DaysAdjustment.ofBusinessDays(-2, TARGET, BusinessDayAdjustment.of(BusinessDayConventions.FOLLOWING, TARGET));
  private static final DaysAdjustment OFFSET_ADJ_LIBOR_LON_2 =
      DaysAdjustment.ofBusinessDays(-2, LON, BusinessDayAdjustment.of(BusinessDayConventions.FOLLOWING, LON));

  /** Curve providers */
  private static final FxMatrix FX_MATRIX = FxMatrix.builder().addRate(EUR, USD, 1.20).build();
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_USD_PAIR =
      StandardDataSetsMulticurveUSD.getCurvesUSDOisL3();
  private static final IborIndex USDLIBOR3M = MULTICURVE_USD_PAIR.getFirst().getIndexesIbor().iterator().next();
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_EUR_PAIR =
      StandardDataSetsMulticurveEUR.getCurvesEurOisE3();
  private static final IborIndex EUREURIBOR3M = MULTICURVE_EUR_PAIR.getFirst().getIndexesIbor().iterator().next();
  private static final MulticurveProviderDiscount MULTICURVE = MULTICURVE_USD_PAIR.getFirst();
  static {
    MULTICURVE.setCurve(EUR, MULTICURVE_EUR_PAIR.getFirst().getCurve(EUR));
    MULTICURVE.setCurve(EUREURIBOR3M, MULTICURVE_EUR_PAIR.getFirst().getCurve(EUREURIBOR3M));
    MULTICURVE.setForexMatrix(FX_MATRIX);
  }
  private static final CurveBuildingBlockBundle BLOCK = MULTICURVE_USD_PAIR.getSecond();
  static {
    BLOCK.addAll(MULTICURVE_EUR_PAIR.getSecond());
  }

  /** EUREURIBOR3M + spread v USDLIBOR3M */
  private static final LocalDate EFFECTIVE_DATE = LocalDate.of(2014, 1, 24);
  private static final LocalDate MATURITY_DATE = LocalDate.of(2016, 1, 24);
  private static final double SPREAD = 0.0020;
  private static final boolean PAYER = true;
  private static final double NOTIONAL_EUR = 100_000_000; // EUR
  private static final double NOTIONAL_USD = 120_000_000; // USD
  /** Fixed notional */
  private static final SwapDefinition XCCY_EUR_USD_NOT_DEFINITION =
      xccyEurE3SUsdL3(EFFECTIVE_DATE, MATURITY_DATE, SPREAD, PAYER, NOTIONAL_EUR, NOTIONAL_USD, true);
  private static final Swap<? extends Payment, ? extends Payment> XCCY_GBP_USD_NOT =
      XCCY_EUR_USD_NOT_DEFINITION.toDerivative(VALUATION_DATE);
  /** FX Reset notional */
  private static final SwapDefinition XCCY_EUR_USD_FXRESETNOT_DEFINITION =
      xccyEurE3SUsdL3FXReset(EFFECTIVE_DATE, MATURITY_DATE, SPREAD, PAYER, NOTIONAL_EUR, NOTIONAL_USD);
  private static final Swap<? extends Payment, ? extends Payment> XCCY_EUR_USD_FXRESETNOT =
      XCCY_EUR_USD_FXRESETNOT_DEFINITION.toDerivative(VALUATION_DATE);

  /** Calculators. */
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();

  /** Tolerances */
  private static final double TOLERANCE_PV = 1.0E-2;

  @Test
  public void presentValueXNot() {
    MultiCurrencyAmount pv = XCCY_GBP_USD_NOT.accept(PVDC, MULTICURVE);
    double pvUsdExpected = 431944.6868;
    double pvEurExpected = -731021.1778;
    assertEquals("XCcy Swap - Present Value - USD", pvUsdExpected, pv.getAmount(USD).getAmount(), TOLERANCE_PV);
    assertEquals("XCcy Swap - Present Value - EUR", pvEurExpected, pv.getAmount(EUR).getAmount(), TOLERANCE_PV);
  }

  @Test
  public void presentValueFxReset() {
    MultiCurrencyAmount pv = XCCY_EUR_USD_FXRESETNOT.accept(PVDC, MULTICURVE);
    double pvUsdExpected = 518623.5163;
    double pvEurExpected = -731021.1778;
    assertEquals("XCcy Swap - Present Value - USD", pvUsdExpected, pv.getAmount(USD).getAmount(), TOLERANCE_PV);
    assertEquals("XCcy Swap - Present Value - EUR", pvEurExpected, pv.getAmount(EUR).getAmount(), TOLERANCE_PV);
  }

  private static SwapDefinition xccyEurE3SUsdL3(final LocalDate effectiveDate, final LocalDate maturityDate,
      final double spreadEur, boolean payerEur, final double notionalEur, final double notionalUsd,
      boolean exchangeNotional) {
    NotionalProvider notionalUsdProvider = date -> notionalUsd;
    FloatingAnnuityDefinitionBuilder iborUsdBuilder =
        new FloatingAnnuityDefinitionBuilder().payer(!payerEur).
            notional(notionalUsdProvider).startDate(effectiveDate).endDate(maturityDate).index(USDLIBOR3M).
            accrualPeriodFrequency(USDLIBOR3M.getTenor()).rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).
            resetDateAdjustmentParameters(ADJUSTED_DATE_LIBOR_LONNYC).accrualPeriodParameters(ADJUSTED_DATE_LIBOR_LONNYC).
            dayCount(USDLIBOR3M.getDayCount()).fixingDateAdjustmentParameters(OFFSET_ADJ_LIBOR_LON_2).
            currency(USDLIBOR3M.getCurrency());
    if (exchangeNotional) {
      iborUsdBuilder = iborUsdBuilder.
          exchangeInitialNotional(true).startDateAdjustmentParameters(ADJUSTED_DATE_LIBOR_LONNYC).
          exchangeFinalNotional(true).endDateAdjustmentParameters(ADJUSTED_DATE_LIBOR_LONNYC);
    }
    AnnuityDefinition<?> legIborEurDefinition =
        legEurE3Notional(effectiveDate, maturityDate, spreadEur, payerEur, notionalEur, exchangeNotional);
    AnnuityDefinition<?> legIborUsdDefinition = iborUsdBuilder.build();
    return new SwapDefinition(legIborEurDefinition, legIborUsdDefinition);
  }

  private static SwapDefinition xccyEurE3SUsdL3FXReset(final LocalDate effectiveDate, final LocalDate maturityDate,
      final double spreadEur, boolean payerEur, final double notionalEur, final double notionalUsd) {
    AnnuityDefinition<?> legIborEurDefinition =
        legEurE3Notional(effectiveDate, maturityDate, spreadEur, payerEur, notionalEur, true);
    AnnuityDefinition<? extends CouponDefinition> legUsdL3FXResetDefinition;
    double sign = payerEur ? 1.0d : -1.0d;
    int nbCpn1 = legIborEurDefinition.getNumberOfPayments() - 2; // Remove notional
    CouponDefinition[] cpnFxReset = new CouponDefinition[3 * nbCpn1];
    for (int loopcpn = 0; loopcpn < nbCpn1; loopcpn++) {
      CouponIborSpreadDefinition cpnLoop = (CouponIborSpreadDefinition) legIborEurDefinition.getNthPayment(loopcpn + 1);
      cpnFxReset[3 * loopcpn] = new CouponFixedFxResetDefinition(USD, cpnLoop.getAccrualStartDate(),
          cpnLoop.getAccrualStartDate(), cpnLoop.getAccrualStartDate(), 1.0d, -sign * notionalUsd, 1.0d, EUR,
          cpnLoop.getFixingDate(), cpnLoop.getAccrualStartDate()); // Notional
      cpnFxReset[1 + 3 * loopcpn] = new CouponIborFxResetDefinition(USD, cpnLoop.getPaymentDate(),
          cpnLoop.getAccrualStartDate(), cpnLoop.getAccrualEndDate(), cpnLoop.getPaymentYearFraction(),
          sign * notionalUsd, cpnLoop.getFixingDate(), USDLIBOR3M, 0.0, NYC, EUR, cpnLoop.getFixingDate(),
          cpnLoop.getAccrualStartDate());
      cpnFxReset[2 + 3 * loopcpn] = new CouponFixedFxResetDefinition(USD, cpnLoop.getAccrualEndDate(),
          cpnLoop.getAccrualEndDate(), cpnLoop.getAccrualEndDate(), 1.0d, sign * notionalUsd, 1.0d, EUR,
          cpnLoop.getFixingDate(), cpnLoop.getAccrualStartDate()); // Notional
    }
    legUsdL3FXResetDefinition = new AnnuityDefinition<>(cpnFxReset, NYC);
    return new SwapDefinition(legIborEurDefinition, legUsdL3FXResetDefinition);
  }

  private static AnnuityDefinition<?> legEurE3Notional(final LocalDate effectiveDate, final LocalDate maturityDate,
      final double spreadEur, boolean payerEur, final double notionalEur, boolean exchangeNotional) {
    NotionalProvider notionalEurProvider = date -> notionalEur;
    FloatingAnnuityDefinitionBuilder iborEurBuilder =
        new FloatingAnnuityDefinitionBuilder().payer(payerEur).
            notional(notionalEurProvider).startDate(effectiveDate).endDate(maturityDate).index(EUREURIBOR3M).
            accrualPeriodFrequency(EUREURIBOR3M.getTenor()).rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).
            resetDateAdjustmentParameters(ADJUSTED_DATE_LIBOR_LONNYC).accrualPeriodParameters(ADJUSTED_DATE_LIBOR_LONNYC).
            dayCount(EUREURIBOR3M.getDayCount()).fixingDateAdjustmentParameters(OFFSET_ADJ_LIBOR_TAR_2).
            currency(EUREURIBOR3M.getCurrency()).spread(spreadEur);
    if (exchangeNotional) {
      iborEurBuilder = iborEurBuilder.
          exchangeInitialNotional(true).startDateAdjustmentParameters(ADJUSTED_DATE_LIBOR_LONNYC).
          exchangeFinalNotional(true).endDateAdjustmentParameters(ADJUSTED_DATE_LIBOR_LONNYC);
    }
    return iborEurBuilder.build();
  }

}

/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.sensitivity.normalswaption;

import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.NormalSwaptionExpiryTenorProvider;
import com.opengamma.analytics.financial.provider.description.interestrate.NormalSwaptionProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.currency.MultiCurrencyAmount;
import com.opengamma.strata.collect.ArgChecker;
import com.opengamma.strata.collect.tuple.Pair;

/**
 * For an instrument, computes the sensitivity of a value (often the present value or a par spread) to the parameters 
 * used in the curve. The computation is done by shifting each node point in each curve; 
 * the curves must be interpolated yield curves for discounting and forward curves.
 * The return format is ParameterSensitivity object.
 * This is a very inefficient way to compute the sensitivities. It should be used only for tests purposes or when speed is irrelevant.
 */
public class ParameterSensitivityNormalSwaptionExpiryTenorDiscountInterpolatedFDCalculator {

  /**
   * The value calculator.
   */
  private final InstrumentDerivativeVisitor<NormalSwaptionProviderInterface, MultiCurrencyAmount> _valueCalculator;
  /**
   * The shift used for finite difference.
   */
  private final double _shift;

  /**
   * Constructor
   * @param valueCalculator The value calculator.
   * @param shift The shift used for finite difference.
   */
  public ParameterSensitivityNormalSwaptionExpiryTenorDiscountInterpolatedFDCalculator(final InstrumentDerivativeVisitor<NormalSwaptionProviderInterface, MultiCurrencyAmount> valueCalculator,
      final double shift) {
    ArgChecker.notNull(valueCalculator, "Calculator");
    _valueCalculator = valueCalculator;
    _shift = shift;
  }

  /**
   * Compute the sensitivity by finite difference on all points. The curves must be interpolated yield curves.
   * Only the discounting and forward curves sensitivity is computed.
   * @param instrument The instrument.
   * @param normalMulticurve The market (all discounting and forward curves should be of the type YieldCurve with InterpolatedDoublesCurve.
   * @return The parameter sensitivity.
   */
  public MultipleCurrencyParameterSensitivity calculateSensitivity(final InstrumentDerivative instrument,
      final NormalSwaptionExpiryTenorProvider normalMulticurve) {
    MultipleCurrencyParameterSensitivity result = new MultipleCurrencyParameterSensitivity();
    final MultiCurrencyAmount pvInit = instrument.accept(_valueCalculator, normalMulticurve);
    final int nbCcy = pvInit.size();
    final List<Currency> ccyList = ImmutableList.copyOf(pvInit.getCurrencies());

    MulticurveProviderInterface multicurveGeneric = normalMulticurve.getMulticurveProvider();
    ArgChecker.isTrue(multicurveGeneric instanceof MulticurveProviderDiscount,
        "underlying multicurve should be of type MulticurveProviderDiscount");
    MulticurveProviderDiscount multicurve = (MulticurveProviderDiscount) multicurveGeneric;
    // Discounting
    final Set<Currency> ccyDiscounting = normalMulticurve.getMulticurveProvider().getCurrencies();
    for (final Currency ccy : ccyDiscounting) {
      final YieldAndDiscountCurve curve = multicurve.getCurve(ccy);
      ArgChecker.isTrue(curve instanceof YieldCurve, "Curve should be a YieldCurve");
      final YieldCurve curveYield = (YieldCurve) curve;
      ArgChecker.isTrue(curveYield.getCurve() instanceof InterpolatedDoublesCurve,
          "Yield curve should be based on InterpolatedDoublesCurve");
      final InterpolatedDoublesCurve curveInt = (InterpolatedDoublesCurve) curveYield.getCurve();
      final int nbNodePoint = curveInt.getXDataAsPrimitive().length;
      final double[][] sensitivity = new double[nbCcy][nbNodePoint];
      for (int loopnode = 0; loopnode < nbNodePoint; loopnode++) {
        double[] yieldBumpedPlus = curveInt.getYDataAsPrimitive().clone();
        yieldBumpedPlus[loopnode] += _shift;
        YieldAndDiscountCurve dscBumpedPlus = new YieldCurve(curveInt.getName(),
            new InterpolatedDoublesCurve(curveInt.getXDataAsPrimitive(), yieldBumpedPlus, curveInt.getInterpolator(), true));
        NormalSwaptionExpiryTenorProvider marketDscBumpedPlus = new NormalSwaptionExpiryTenorProvider(
            multicurve.withDiscountFactor(ccy, dscBumpedPlus), normalMulticurve.getParameterSurface(),
            normalMulticurve.getGeneratorSwap());
        final MultiCurrencyAmount pvBumpedPlus = instrument.accept(_valueCalculator, marketDscBumpedPlus);
        final double[] yieldBumpedMinus = curveInt.getYDataAsPrimitive().clone();
        yieldBumpedMinus[loopnode] -= _shift;
        final YieldAndDiscountCurve dscBumpedMinus = new YieldCurve(curveInt.getName(),
            new InterpolatedDoublesCurve(curveInt.getXDataAsPrimitive(), yieldBumpedMinus, curveInt.getInterpolator(), true));
        final NormalSwaptionExpiryTenorProvider marketDscBumpedMinus = new NormalSwaptionExpiryTenorProvider(
            multicurve.withDiscountFactor(ccy, dscBumpedMinus), normalMulticurve.getParameterSurface(),
            normalMulticurve.getGeneratorSwap());
        final MultiCurrencyAmount pvBumpedMinus = instrument.accept(_valueCalculator, marketDscBumpedMinus);
        final MultiCurrencyAmount pvDiff = pvBumpedPlus.plus(pvBumpedMinus.multipliedBy(-1.0));
        for (int loopccypv = 0; loopccypv < nbCcy; loopccypv++) {
          sensitivity[loopccypv][loopnode] = pvDiff.getAmount(ccyList.get(loopccypv)).getAmount() / (2 * _shift);
        }
      }
      final String name = normalMulticurve.getMulticurveProvider().getName(ccy);
      for (int loopccypv = 0; loopccypv < nbCcy; loopccypv++) {
        result = result.plus(Pair.of(name, ccyList.get(loopccypv)), new DoubleMatrix1D(sensitivity[loopccypv]));
      }
    }
    // Forward ON
    final Set<IndexON> indexON = normalMulticurve.getMulticurveProvider().getIndexesON();
    for (final IndexON index : indexON) {
      final YieldAndDiscountCurve curve = multicurve.getCurve(index);
      ArgChecker.isTrue(curve instanceof YieldCurve, "Curve should be a YieldCurve");
      final YieldCurve curveYield = (YieldCurve) curve;
      ArgChecker.isTrue(curveYield.getCurve() instanceof InterpolatedDoublesCurve,
          "Yield curve should be based on InterpolatedDoublesCurve");
      final InterpolatedDoublesCurve curveInt = (InterpolatedDoublesCurve) curveYield.getCurve();
      final int nbNodePoint = curveInt.getXDataAsPrimitive().length;
      final double[][] sensitivity = new double[nbCcy][nbNodePoint];
      for (int loopnode = 0; loopnode < nbNodePoint; loopnode++) {
        final double[] yieldBumpedPlus = curveInt.getYDataAsPrimitive().clone();
        yieldBumpedPlus[loopnode] += _shift;
        YieldAndDiscountCurve dscBumpedPlus = new YieldCurve(curveInt.getName(),
            new InterpolatedDoublesCurve(curveInt.getXDataAsPrimitive(), yieldBumpedPlus, curveInt.getInterpolator(), true));
        NormalSwaptionExpiryTenorProvider marketFwdBumpedPlus = new NormalSwaptionExpiryTenorProvider(
            multicurve.withForward(index, dscBumpedPlus), normalMulticurve.getParameterSurface(),
            normalMulticurve.getGeneratorSwap());
        final MultiCurrencyAmount pvBumpedPlus = instrument.accept(_valueCalculator, marketFwdBumpedPlus);
        final double[] yieldBumpedMinus = curveInt.getYDataAsPrimitive().clone();
        yieldBumpedMinus[loopnode] -= _shift;
        final YieldAndDiscountCurve dscBumpedMinus = new YieldCurve(curveInt.getName(),
            new InterpolatedDoublesCurve(curveInt.getXDataAsPrimitive(), yieldBumpedMinus, curveInt.getInterpolator(), true));
        NormalSwaptionExpiryTenorProvider marketFwdBumpedMinus = new NormalSwaptionExpiryTenorProvider(
            multicurve.withForward(index, dscBumpedMinus), normalMulticurve.getParameterSurface(),
            normalMulticurve.getGeneratorSwap());
        final MultiCurrencyAmount pvBumpedMinus = instrument.accept(_valueCalculator, marketFwdBumpedMinus);
        final MultiCurrencyAmount pvDiff = pvBumpedPlus.plus(pvBumpedMinus.multipliedBy(-1.0));
        for (int loopccypv = 0; loopccypv < nbCcy; loopccypv++) {
          sensitivity[loopccypv][loopnode] = pvDiff.getAmount(ccyList.get(loopccypv)).getAmount() / (2 * _shift);
        }
      }
      final String name = normalMulticurve.getMulticurveProvider().getName(index);
      for (int loopccypv = 0; loopccypv < nbCcy; loopccypv++) {
        result = result.plus(Pair.of(name, ccyList.get(loopccypv)), new DoubleMatrix1D(sensitivity[loopccypv]));
      }
    }
    // Forward Ibor
    final Set<IborIndex> indexForward = normalMulticurve.getMulticurveProvider().getIndexesIbor();
    for (final IborIndex index : indexForward) {
      final YieldAndDiscountCurve curve = multicurve.getCurve(index);
      ArgChecker.isTrue(curve instanceof YieldCurve, "Curve should be a YieldCurve");
      final YieldCurve curveYield = (YieldCurve) curve;
      ArgChecker.isTrue(curveYield.getCurve() instanceof InterpolatedDoublesCurve, "Yield curve should be based on InterpolatedDoublesCurve");
      final InterpolatedDoublesCurve curveInt = (InterpolatedDoublesCurve) curveYield.getCurve();
      final int nbNodePoint = curveInt.getXDataAsPrimitive().length;
      final double[][] sensitivity = new double[nbCcy][nbNodePoint];
      for (int loopnode = 0; loopnode < nbNodePoint; loopnode++) {
        final double[] yieldBumpedPlus = curveInt.getYDataAsPrimitive().clone();
        yieldBumpedPlus[loopnode] += _shift;
        final YieldAndDiscountCurve dscBumpedPlus = new YieldCurve(curveInt.getName(), new InterpolatedDoublesCurve(curveInt.getXDataAsPrimitive(), yieldBumpedPlus, curveInt.getInterpolator(), true));
        NormalSwaptionExpiryTenorProvider marketFwdBumpedPlus = new NormalSwaptionExpiryTenorProvider(
            multicurve.withForward(index, dscBumpedPlus), normalMulticurve.getParameterSurface(),
            normalMulticurve.getGeneratorSwap());
        final MultiCurrencyAmount pvBumpedPlus = instrument.accept(_valueCalculator, marketFwdBumpedPlus);
        final double[] yieldBumpedMinus = curveInt.getYDataAsPrimitive().clone();
        yieldBumpedMinus[loopnode] -= _shift;
        final YieldAndDiscountCurve dscBumpedMinus = new YieldCurve(curveInt.getName(),
            new InterpolatedDoublesCurve(curveInt.getXDataAsPrimitive(), yieldBumpedMinus, curveInt.getInterpolator(), true));
        NormalSwaptionExpiryTenorProvider marketFwdBumpedMinus = new NormalSwaptionExpiryTenorProvider(
            multicurve.withForward(index, dscBumpedMinus), normalMulticurve.getParameterSurface(),
            normalMulticurve.getGeneratorSwap());
        final MultiCurrencyAmount pvBumpedMinus = instrument.accept(_valueCalculator, marketFwdBumpedMinus);
        final MultiCurrencyAmount pvDiff = pvBumpedPlus.plus(pvBumpedMinus.multipliedBy(-1.0));
        for (int loopccypv = 0; loopccypv < nbCcy; loopccypv++) {
          sensitivity[loopccypv][loopnode] = pvDiff.getAmount(ccyList.get(loopccypv)).getAmount() / (2 * _shift);
        }
      }
      final String name = normalMulticurve.getMulticurveProvider().getName(index);
      for (int loopccypv = 0; loopccypv < nbCcy; loopccypv++) {
        result = result.plus(Pair.of(name, ccyList.get(loopccypv)), new DoubleMatrix1D(sensitivity[loopccypv]));
      }
    }
    return result;
  }
}

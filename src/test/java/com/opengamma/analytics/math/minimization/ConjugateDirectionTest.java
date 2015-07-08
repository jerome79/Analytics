/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.minimization;

import org.testng.annotations.Test;

/**
 * Test.
 */
@Test
public class ConjugateDirectionTest extends MultidimensionalMinimizerTestCase {

  // EPS set to 5e-6, not 1e-6, for 32-bit Windows
  private static final double EPS = 5e-6;

  private static ScalarMinimizer LINE_MINIMIZER = new BrentMinimizer1D();
  private static ConjugateDirectionVectorMinimizer MINIMIZER =
      new ConjugateDirectionVectorMinimizer(LINE_MINIMIZER, EPS, 100000);

  @Test
  public void testSolvingRosenbrock() {
    super.assertSolvingRosenbrock(MINIMIZER, EPS);
  }

  /**
   * This function is inherently challenging, giant number of iterations needed
   * as the direction vector ricochets around in a groove. 
   */
  @Test
  public void testSolvingCoupledRosenbrock() {
    super.assertSolvingCoupledRosenbrock(MINIMIZER, EPS);
  }

}

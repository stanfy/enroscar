package com.stanfy.enroscar.goro.test;

import com.stanfy.enroscar.goro.internal.GoroValidationProcessor;

import java.util.Arrays;
import java.util.List;

import javax.annotation.processing.Processor;

/**
 * Test utilities.
 */
final class TestUtil {

  private TestUtil() {
    // no instances
  }

  public static List<Processor> goroProcessors() {
    return Arrays.<Processor>asList(
      new GoroValidationProcessor()
    );
  }

}

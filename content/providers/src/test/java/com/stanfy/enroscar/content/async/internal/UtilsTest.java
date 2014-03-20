package com.stanfy.enroscar.content.async.internal;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests for Utils.
 */
public class UtilsTest {

  @Test
  public void generatedClassName() {
    assertThat(Utils.getGeneratedClassName("test.package", "test.package.Some$ClassName"))
        .isEqualTo("Some$ClassName$$Loader");
    assertThat(Utils.getGeneratedClassName("test.package", "test.package.Some.ClassName"))
        .isEqualTo("Some$ClassName$$Loader");
  }

}

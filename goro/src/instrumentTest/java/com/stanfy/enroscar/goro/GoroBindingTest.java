package com.stanfy.enroscar.goro;

import android.content.Intent;
import android.os.Binder;
import android.test.ServiceTestCase;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests for {@link com.stanfy.enroscar.goro.Goro}.
 */
public class GoroBindingTest extends ServiceTestCase<GoroService> {

  public GoroBindingTest() {
    super(GoroService.class);
  }

  public void testGoroFromBinder() {
    Goro goro = Goro.from(bindService(new Intent()));
    assertThat(goro).isNotNull();
  }

  public void testGoroFromBadBinderShouldThrow() {
    try {
      Goro.from(new Binder());
      fail("Missing exception");
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessageContaining("Cannot get Goro");
    }
  }

}

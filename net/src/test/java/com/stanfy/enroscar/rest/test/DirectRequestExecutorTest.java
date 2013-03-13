package com.stanfy.enroscar.rest.test;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import com.stanfy.enroscar.net.test.Runner;
import com.stanfy.enroscar.rest.DirectRequestExecutor;

/**
 * Direct request executor test.
 */
@RunWith(Runner.class)
public class DirectRequestExecutorTest {

  @Test
  public void hooksShouldNotBeNull() {
    DirectRequestExecutor executor = new DirectRequestExecutor(Robolectric.application);
    assertThat(executor.getHooks(), instanceOf(DirectRequestExecutor.EmptyHooks.class));
  }
  
}

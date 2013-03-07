package com.stanfy.enroscar.assist.test;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import com.stanfy.enroscar.assist.DefaultBeansManager;
import com.stanfy.enroscar.beans.BeansManager;

/**
 * Tests for {@link DefaultBeansManager}.
 */
@RunWith(Runner.class)
public class DefaultBeansManagerTest {

  @Test
  public void afterUseCalledBeansManagerGetShouldReturnDefaultOne() {
    DefaultBeansManager manager = DefaultBeansManager.use(Robolectric.application);
    assertSame(manager, BeansManager.get(Robolectric.application));
  }
  
}

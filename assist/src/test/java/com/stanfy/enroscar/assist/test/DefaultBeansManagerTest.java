package com.stanfy.enroscar.assist.test;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Before;
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

  /** Manager. */
  private DefaultBeansManager manager;
  
  @Before
  public void useDefaultBeansManager() {
    manager = DefaultBeansManager.get(Robolectric.application);
  }
  
  @Test
  public void afterGetCalledBeansManagerGetShouldReturnDefaultOne() {
    assertThat(BeansManager.get(Robolectric.application), instanceOf(DefaultBeansManager.class));
  }
  
  @Test
  public void buffersPoolShouldBePut() {
    manager.edit().commit();
    assertThat(manager.getMainBuffersPool(), notNullValue());
  }
  
}

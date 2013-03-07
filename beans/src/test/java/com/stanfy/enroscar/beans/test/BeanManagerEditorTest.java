package com.stanfy.enroscar.beans.test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import com.stanfy.enroscar.beans.BeansManager;
import com.stanfy.enroscar.beans.EnroscarBean;

/**
 * Tests for {@link BeansManager.Editor}.
 */
@RunWith(Runner.class)
public class BeanManagerEditorTest {

  /** Beans manager instance. */
  private BeansManager beansManager;

  @Before
  public void initBeans() {
    beansManager = BeansManager.get(Robolectric.application);
    beansManager.edit()
      .put(BeanA.class)
      .put(BeanB.class)
      .commit();
  }
  
  @Test
  public void beanWithSameNamesShouldBeSabstitued() {
    assertThat(beansManager.getContainer().getBean(BeanA.class), is(instanceOf(BeanB.class)));
  }
  
  /** Example bean. */
  @EnroscarBean("mybean")
  public static class BeanA {
  }
  
  /** Will rewrite BeanA. */
  public static class BeanB extends BeanA {
  }
  
}

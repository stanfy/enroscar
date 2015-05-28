package com.stanfy.enroscar.beans.test;

import com.stanfy.enroscar.beans.DefaultBeansContainer;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Tests for DefaultBeansContainer.
 */
public class DefaultBeansContainerTest {
  
  /** Instance to test. */
  private DefaultBeansContainer container;

  @Before
  public void create() {
    container = new DefaultBeansContainer();
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowOnRemoveWhenBeanNotFoundByName() {
    container.removeEntityInstance("any");
  }
  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowOnRemoveWhenBeanNotFoundByInstance() {
    container.removeEntityInstance(new Object());
  }

  @Test
  public void shouldAddAndRemoveTemporaryInstance() {
    Object tempBean = new Object();
    String name = container.putTemporaryInstance(tempBean);
    assertThat(container.containsBean(name)).isTrue();
    container.removeEntityInstance(tempBean);
    assertThat(container.containsBean(name)).isFalse();
  }

  @Test
  public void shouldRemoveInstanceByName() {
    container.putEntityInstance("b1", new Object());
    assertThat(container.containsBean("b1")).isTrue();
    container.removeEntityInstance("b1");
    assertThat(container.containsBean("b1")).isFalse();
  }

}

package com.stanfy.enroscar.shared.test;

import org.fest.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowLog;

import android.app.Application;

import com.stanfy.enroscar.beans.BeansManager;

/**
 * Base test class.
 */
public abstract class AbstractEnroscarTest {

  /** Beans manager. */
  private BeansManager beansManager;

  public BeansManager getBeansManager() {
    return beansManager;
  }

  public Application getApplication() {
    return (Application)Robolectric.application;
  }

  {
    // XXX load Arrays class
    Arrays.format(new String[]{"a"});
  }

  @Before
  public final void initLogs() {
    ShadowLog.stream = System.out;
  }
  
  @Before
  public final void configureBeans() {
    beansManager = BeansManager.get(Robolectric.application);
    final BeansManager.Editor editor = beansManager.edit();
    configureBeansManager(editor);
    editor.commit();
    whenBeansConfigured();
  }

  /**
   * Put beans.
   * @param editor beans manager editor 
   */
  protected void configureBeansManager(final BeansManager.Editor editor) {
    // nothig
  }

  /**
   * Called immediately after beans are initialized.
   */
  protected void whenBeansConfigured() {
    // nothing
  }

  @After
  public void destroyBeansContainer() throws Exception {
    BeansManager.get(Robolectric.application).getContainer().destroy();
  }

}

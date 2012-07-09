package com.stanfy.test;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;

import com.stanfy.app.beans.BeansManager;
import com.stanfy.net.EnroscarConnectionsEngine;
import com.stanfy.net.EnroscarConnectionsEngine.Config;
import com.stanfy.net.EnroscarConnectionsEngineMode;
import com.stanfy.utils.AppUtils;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.shadows.ShadowLog;


/**
 * Base class for all tests.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
@RunWith(EnroscarTestRunner.class)
public abstract class AbstractEnroscarTest {

  protected static <T> void assertThat(final T actual, final Matcher<T> matcher) {
    Assert.assertThat(actual, matcher);
  }
  protected static <T> void assertThat(final String reason, final T actual, final Matcher<T> matcher) {
    Assert.assertThat(reason, actual, matcher);
  }

  protected static <T> T directlyOn(final T on) { return Robolectric.directlyOn(on); }

  /** Configuration. */
  private EnroscarConfiguration config;

  /** Beans manager. */
  private BeansManager beansManager;

  public BeansManager getBeansManager() {
    return beansManager;
  }

  public Application getApplication() {
    return (Application)Robolectric.application;
  }

  @Before
  public final void connectionsEngineSetup() {
    System.err.println("Run test " + getClass());
    ShadowLog.stream = System.out;

    if (config == null) {
      EnroscarConnectionsEngineMode.testMode();
      config = AppUtils.getAnnotationFromHierarchy(getClass(), EnroscarConfiguration.class);
    }

    beansManager = BeansManager.get(Robolectric.application);
    final BeansManager.Editor editor = beansManager.edit();
    configureBeansManager(editor);
    editor.commit();

    if (config != null && config.connectionEngineRequired()) {
      final Config config = EnroscarConnectionsEngine.config();
      configureConnectionsEngine(config);
      config.install(Robolectric.application);
    }

    whenBeansConfigured();
  }

  protected void configureBeansManager(final BeansManager.Editor editor) {
    editor.required();
  }

  protected void configureConnectionsEngine(final EnroscarConnectionsEngine.Config config) {
    // nothing
  }

  protected void whenBeansConfigured() {
    // nothing
  }

  @After
  public void connectionsEngineShutdown() throws Exception {
    EnroscarConnectionsEngine.uninstall();
    BeansManager.get(Robolectric.application).getContainer().destroy();
  }

}

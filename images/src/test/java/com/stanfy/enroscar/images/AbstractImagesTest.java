package com.stanfy.enroscar.images;

import com.google.mockwebserver.MockWebServer;
import com.stanfy.enroscar.beans.BeansManager;
import com.stanfy.enroscar.io.BuffersPool;
import com.stanfy.enroscar.net.EnroscarConnectionsEngine;
import com.stanfy.enroscar.sdkdep.SDKDependentUtilsFactory;
import com.stanfy.enroscar.shared.test.AbstractEnroscarTest;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;

/**
 * Base test class.
 */
@RunWith(RobolectricTestRunner.class)
public abstract class AbstractImagesTest extends AbstractEnroscarTest {

  /** Images manager. */
  ImagesManager manager;

  /** Server. */
  MockWebServer server;

  @Before
  public void startServer() throws IOException {
    server = new MockWebServer();
    server.play();
  }

  @After
  public void stopServer() throws IOException {
    server.shutdown();
  }

  @Override
  protected void configureBeansManager(BeansManager.Editor editor) {
    super.configureBeansManager(editor);
    editor.put(BuffersPool.class);
    editor.put(SDKDependentUtilsFactory.class);
    BeanSetup.setup(editor);
  }

  @Override
  protected void whenBeansConfigured() {
    super.whenBeansConfigured();
    EnroscarConnectionsEngine.config().install(Robolectric.application);
    manager = BeansManager.get(Robolectric.application).getContainer().getBean(ImagesManager.class);
    manager.setDebug(true);
  }

}

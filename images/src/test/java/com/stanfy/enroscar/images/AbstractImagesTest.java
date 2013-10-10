package com.stanfy.enroscar.images;

import com.stanfy.enroscar.beans.BeansManager;
import com.stanfy.enroscar.io.BuffersPool;
import com.stanfy.enroscar.net.EnroscarConnectionsEngine;
import com.stanfy.enroscar.sdkdep.SDKDependentUtilsFactory;
import com.stanfy.enroscar.shared.test.AbstractEnroscarTest;

import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

/**
 * Base test class.
 */
@RunWith(RobolectricTestRunner.class)
public abstract class AbstractImagesTest extends AbstractEnroscarTest {

  /** Images manager. */
  ImagesManager manager;

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

package com.stanfy.enroscar.images;

import android.test.AndroidTestCase;

import com.stanfy.enroscar.beans.BeansManager;
import com.stanfy.enroscar.net.EnroscarConnectionsEngine;

public abstract class BaseAndroidTest extends AndroidTestCase {

  /** Images manager. */
  ImagesManager imagesManager;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    EnroscarConnectionsEngine.config().setup(getContext());
    BeansManager.Editor editor = BeansManager.get(getContext()).edit();
    BeanSetup.setup(editor);
    editor.commit();

    imagesManager = BeansManager.get(getContext()).getContainer().getBean(ImagesManager.class);
  }

}

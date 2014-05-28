package com.stanfy.enroscar.assist.util;

import com.stanfy.enroscar.activities.CrucialGUIOperationManager;
import com.stanfy.enroscar.beans.BeansContainer;
import com.stanfy.enroscar.beans.EnroscarBean;
import com.stanfy.enroscar.beans.InitializingBean;
import com.stanfy.enroscar.images.ImagesManager;

/**
 * Pause image loading on crucial GUI operations.
 * @author Olexandr Tereshchuk - "Stanfy"
 * @since 28.05.14
 */
@EnroscarBean(value = ImagesManagerCrucialGUIOperationListener.BEAN_NAME)
public class ImagesManagerCrucialGUIOperationListener implements CrucialGUIOperationManager.CrucialGUIOperationListener, InitializingBean {

  /** Bean name. */
  public static final String BEAN_NAME = "ImagesManagerCrucialGUIOperationListener";

  /** Images manager. */
  private ImagesManager imagesManager;

  @Override
  public void onStartCrucialGUIOperation() {
    if (imagesManager != null) {
      imagesManager.pauseLoading();
    }
  }

  @Override
  public void onFinishCrucialGUIOperation() {
    if (imagesManager != null) {
      imagesManager.resumeLoading();
    }
  }

  @Override
  public void onInitializationFinished(final BeansContainer beansContainer) {
    imagesManager = beansContainer.getBean(ImagesManager.class);
    final CrucialGUIOperationManager manager = beansContainer.getBean(CrucialGUIOperationManager.class);
    if (manager != null) {
      manager.addCrucialGUIOperationListener(this);
    }
  }
}

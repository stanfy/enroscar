package com.stanfy.enroscar.sdkdep;

import com.stanfy.enroscar.beans.BeansManager;

import android.content.Context;

/**
 * Shortcut to get instance of {@link SdkDependentUtils}.
 */
public final class SdkDepUtils {

  /** Utils instance. */
  private static SdkDependentUtils instance;
  
  private SdkDepUtils() { }
  
  public static SdkDependentUtils get(final Context context) {
    if (instance == null) {
      SDKDependentUtilsFactory factory = BeansManager.get(context).getContainer()
          .getBean(SDKDependentUtilsFactory.BEAN_NAME, SDKDependentUtilsFactory.class);
      if (factory == null) {
        throw new IllegalStateException("SDKDependentUtilsFactory is not defined. "
            + "Either try DefaultBeansManager from Enroscar Assist or set your put(SDKDependentUtilsFactory.class) call to be the first in beans definitions");
      }
      instance = factory.createSdkDependentUtils();
    }
    return instance;
  }
  
}

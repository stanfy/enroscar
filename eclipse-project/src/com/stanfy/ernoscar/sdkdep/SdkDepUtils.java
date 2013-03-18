package com.stanfy.ernoscar.sdkdep;

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
      instance = BeansManager.get(context).getContainer()
          .getBean(SDKDependentUtilsFactory.BEAN_NAME, SDKDependentUtilsFactory.class).createSdkDependentUtils();
    }
    return instance;
  }
  
}

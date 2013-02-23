package com.stanfy.utils.sdk;

import android.os.Build;
import android.util.Log;

import com.stanfy.enroscar.beans.Bean;
import com.stanfy.enroscar.beans.EnroscarBean;

/**
 * Factory for {@link SDKDependentUtils}.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
@EnroscarBean(SDKDependentUtilsFactory.BEAN_NAME)
public class SDKDependentUtilsFactory implements Bean {

  /** Bean name. */
  public static final String BEAN_NAME = "SDKDependentUtilsFactory";

  /** Logging tag. */
  private static final String TAG = BEAN_NAME;

  public SDKDependentUtils createSdkDependentUtils() {
    final String classsName = null;
    SDKDependentUtils sdkDependentUtils = null;
    try {
      sdkDependentUtils = (SDKDependentUtils)Class.forName(classsName).newInstance();
    } catch (final Exception e) {
      sdkDependentUtils = new LowestSDKDependentUtils();
    } finally {
      Log.d(TAG, "SDK depended utils: " + sdkDependentUtils);
    }
    return sdkDependentUtils;
  }

  protected String getUtilsClassName() {
    final int version = Build.VERSION.SDK_INT;
    if (version >= Build.VERSION_CODES.JELLY_BEAN) {
      return "com.stanfy.utils.sdk.JellyBeanUtils";
    }
    if (version >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
      return "com.stanfy.utils.sdk.IcsUtils";
    }
    if (version >= Build.VERSION_CODES.HONEYCOMB_MR1) {
      return "com.stanfy.utils.sdk.HoneycombMr1Utils";
    }
    if (version >= Build.VERSION_CODES.HONEYCOMB) {
      return "com.stanfy.utils.sdk.HoneycombUtils";
    }
    if (version >= Build.VERSION_CODES.GINGERBREAD) {
      return "com.stanfy.utils.sdk.GingerbreadUtils";
    }
    return "com.stanfy.utils.sdk.LowestSDKDependentUtils";
  }

}

package com.stanfy.ernoscar.sdkdep;

import android.os.Build;
import android.util.Log;

import com.stanfy.enroscar.beans.Bean;
import com.stanfy.enroscar.beans.EnroscarBean;

/**
 * Factory for {@link SdkDependentUtils}.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
@EnroscarBean(SDKDependentUtilsFactory.BEAN_NAME)
public class SDKDependentUtilsFactory implements Bean {

  /** Bean name. */
  public static final String BEAN_NAME = "SDKDependentUtilsFactory";

  /** Logging tag. */
  private static final String TAG = BEAN_NAME;

  public SdkDependentUtils createSdkDependentUtils() {
    final String classsName = getUtilsClassName();
    SdkDependentUtils sdkDependentUtils = null;
    try {
      sdkDependentUtils = (SdkDependentUtils)Class.forName(classsName).newInstance();
    } catch (final Exception e) {
      sdkDependentUtils = new LowestSDKDependentUtils();
    } finally {
      Log.d(TAG, "SDK depended utils: " + sdkDependentUtils);
    }
    return sdkDependentUtils;
  }

  /**
   * @return implementation full class name
   */
  protected String getUtilsClassName() {
    String packageName = SdkDependentUtils.class.getPackage().getName();
    final int version = Build.VERSION.SDK_INT;
    if (version >= Build.VERSION_CODES.JELLY_BEAN) {
      return packageName + ".JellyBeanUtils";
    }
    if (version >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
      return packageName + ".IcsUtils";
    }
    if (version >= Build.VERSION_CODES.HONEYCOMB_MR1) {
      return packageName + ".HoneycombMr1Utils";
    }
    if (version >= Build.VERSION_CODES.HONEYCOMB) {
      return packageName + ".HoneycombUtils";
    }
    if (version >= Build.VERSION_CODES.GINGERBREAD) {
      return packageName + ".GingerbreadUtils";
    }
    return packageName + ".LowestSDKDependentUtils";
  }

}

package com.stanfy.utils.sdk;

import com.stanfy.integration.httpresponsecache.PortedHttpResponseCacheInstaller;
import com.stanfy.utils.AppUtils;

/**
 * Can configure {@link SDKDependentUtils} to use a ported version of HTTP response cache.
 */
public final class PortedHttpResponseCacheConfigurator {

  private PortedHttpResponseCacheConfigurator() { /* hidden */ }

  public static void setupCacheInstaller() {
    final SDKDependentUtils utils = AppUtils.getSdkDependentUtils();
    if (utils instanceof SdkDependentUtilsConfigurator) {
      ((SdkDependentUtilsConfigurator) utils).setCacheInstaller(PortedHttpResponseCacheInstaller.getInstance());
    }
  }

}

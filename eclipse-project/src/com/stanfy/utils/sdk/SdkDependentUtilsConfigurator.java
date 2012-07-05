package com.stanfy.utils.sdk;

import com.stanfy.net.cache.CacheInstaller;

/**
 * {@link SDKDependentUtils} configurator.
 */
interface SdkDependentUtilsConfigurator {

  /**
   * This method can be used to configure system cache installer.
   * @param cacheInstaller cache installer instance
   */
  void setCacheInstaller(final CacheInstaller<?> cacheInstaller);

}

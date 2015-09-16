package com.stanfy.enroscar.beans;

import android.content.res.Configuration;

/**
 * Interface of a bean that depends on configuration.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 * @deprecated Beans subsystem is deprecated now. Use some dependency injection framework (e.g. Dagger) instead.
 */
@Deprecated
public interface ConfigurationDependentBean {

  /**
   * Trigger configuration change.
   * @param config configuration instance
   */
  void triggerConfigurationChange(Configuration config);

}

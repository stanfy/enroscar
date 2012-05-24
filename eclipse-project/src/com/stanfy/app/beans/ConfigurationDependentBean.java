package com.stanfy.app.beans;

import android.content.res.Configuration;

/**
 * Interface of a bean that depends on configuration.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 *
 */
public interface ConfigurationDependentBean {

  /**
   * Trigger configuration change.
   * @param config configuration instance
   */
  void triggerConfigurationChange(Configuration config);

}

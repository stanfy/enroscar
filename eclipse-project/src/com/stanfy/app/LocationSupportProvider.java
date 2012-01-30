package com.stanfy.app;

import com.stanfy.utils.LocationMethodsSupport;

/**
 * Interface for activity that provide location methods support.
 * @see LocationMethodsSupport
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public interface LocationSupportProvider {

  /**
   * @return location methods support instance
   */
  LocationMethodsSupport getLocationSupport();

  /**
   * Setup location support.
   */
  void setupLocationSupport();

}

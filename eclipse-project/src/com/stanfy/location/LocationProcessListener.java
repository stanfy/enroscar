package com.stanfy.location;

import android.location.Location;

/**
 * Location process listener.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public interface LocationProcessListener {

  /** Start location listening. */
  void onLocationStart();

  /** Stop location listening. */
  void onLocationStop();

  /**
   * @param location new location
   */
  void onLocationUpdate(final Location location);

}

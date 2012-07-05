package com.stanfy.app;

import android.content.res.Configuration;

import com.stanfy.app.beans.BeansManager;

/**
 * Base application class.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class Application extends android.app.Application {

  @Override
  public void onCreate() {
    super.onCreate();
  }

  @Override
  public void onLowMemory() {
    BeansManager.onLowMemory(this);
  }
  @Override
  public void onConfigurationChanged(final Configuration newConfig) {
    BeansManager.onConfigurationChanged(this, newConfig);
  }

  /**
   * @return true if location methods implementation should be accessible from service
   * @deprecated waiting for new implementation
   */
  @Deprecated
  public boolean addLocationSupportToService() { return false; }

}

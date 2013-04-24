package com.stanfy.enroscar.beans;

import android.content.res.Configuration;


/**
 * Base application class.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class BeansApplication extends android.app.Application {

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    BeansManager.onLowMemory(this);
  }
  @Override
  public void onConfigurationChanged(final Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    BeansManager.onConfigurationChanged(this, newConfig);
  }

}

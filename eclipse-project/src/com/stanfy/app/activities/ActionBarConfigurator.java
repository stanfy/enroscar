package com.stanfy.app.activities;

import com.stanfy.app.ActionBarSupport;

/**
 * Activity that can configure its action bar.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public interface ActionBarConfigurator {

  /**
   * @param actionBarSupport action bar suppot instance
   */
  void onInitializeActionBar(final ActionBarSupport actionBarSupport);

}

package com.stanfy.enroscar.test;

import com.stanfy.app.beans.BeansManager;
import com.stanfy.net.EnroscarConnectionsEngine;

/**
 * Custom application class.
 * For future purposes...
 * @author Olexandr Tereshchuk - Stanfy (http://www.stanfy.com)
 */
public class Application extends com.stanfy.app.Application {

  @Override
  public void onCreate() {
    super.onCreate();
    BeansManager.get(this).edit().defaults().commit();
    EnroscarConnectionsEngine.config().install(this);
  }
}

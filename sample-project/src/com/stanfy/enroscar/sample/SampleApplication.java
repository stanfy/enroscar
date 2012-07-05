package com.stanfy.enroscar.sample;

import com.stanfy.app.Application;
import com.stanfy.app.beans.BeansManager;
import com.stanfy.net.EnroscarConnectionsEngine;

/**
 * Sample application.
 */
public class SampleApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    BeansManager.get(this).edit().defaults().remoteServerApi("json").commit();
    EnroscarConnectionsEngine.config().install(this);
  }

}

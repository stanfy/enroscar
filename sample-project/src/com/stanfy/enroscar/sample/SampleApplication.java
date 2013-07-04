package com.stanfy.enroscar.sample;

import android.app.Application;

import com.stanfy.enroscar.assist.DefaultBeansManager;

/**
 * Sample application.
 */
public class SampleApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();

    DefaultBeansManager.get(this)
        .edit().defaults().remoteServerApi("json").commit();
  }

}

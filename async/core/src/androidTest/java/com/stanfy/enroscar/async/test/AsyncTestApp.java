package com.stanfy.enroscar.async.test;

import android.annotation.TargetApi;
import android.app.Application;
import android.os.StrictMode;

/**
 * Test application.
 */
@TargetApi(9)
public class AsyncTestApp extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    strictMode();
  }

  protected void strictMode() {
    StrictMode.setVmPolicy(
        new StrictMode.VmPolicy.Builder()
            .detectAll()
            .penaltyLog()
            .penaltyDeath()
            .build()
    );
    StrictMode.setThreadPolicy(
        new StrictMode.ThreadPolicy.Builder()
            .detectAll()
            .penaltyLog()
            .penaltyDeath()
            .build()
    );
  }

}

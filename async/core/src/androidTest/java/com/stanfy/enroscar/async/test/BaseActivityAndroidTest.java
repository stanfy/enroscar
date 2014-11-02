package com.stanfy.enroscar.async.test;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;

abstract class BaseActivityAndroidTest<T extends Activity> extends ActivityInstrumentationTestCase2<T> {

  public BaseActivityAndroidTest(final Class<T> activityClass) {
    super(activityClass);
  }

  @Override
  protected void setUp() throws Exception {
    System.gc();
    super.setUp();
  }

}

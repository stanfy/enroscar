package com.stanfy.enroscar.async.rx.test;

import android.test.ActivityInstrumentationTestCase2;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
public class OnResetOnCompleteAndroidTest extends ActivityInstrumentationTestCase2<UserActivity> {

  /** Activity instance. */
  private UserActivity activity;

  public OnResetOnCompleteAndroidTest() {
    super(UserActivity.class);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    activity = getActivity();
  }

  public void testOnCompleteWhenLoaderIsReset() throws Throwable {
    runTestOnUiThread(new Runnable() {
      @Override
      public void run() {
        activity.completeCalled = false;

        getInstrumentation().callActivityOnDestroy(activity);
        setActivity(null);
        assertThat(activity.completeCalled).isTrue();
      }
    });
    getInstrumentation().waitForIdleSync();
  }

}

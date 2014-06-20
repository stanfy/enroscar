package com.stanfy.enroscar.async.rx.test;

import android.test.ActivityInstrumentationTestCase2;

import java.util.concurrent.TimeUnit;

import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
public class UserActivityAndroidTest extends ActivityInstrumentationTestCase2<UserActivity> {

  /** Activity instance. */
  private UserActivity activity;

  public UserActivityAndroidTest() {
    super(UserActivity.class);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    activity = getActivity();
  }

  public void testLoadThing() throws Throwable {
    assertThat(activity.loadSync.await(1, TimeUnit.SECONDS)).isTrue();
    runTestOnUiThread(new Runnable() {
      @Override
      public void run() {
        assertThat(activity.view).hasTextString("thing 1");
      }
    });
  }

  public void testSendThing() throws Throwable {
    activity.completeCalled = false;
    assertThat(activity.loadSync.await(1, TimeUnit.SECONDS)).isTrue();

    runTestOnUiThread(new Runnable() {
      @Override
      public void run() {
        activity.view.performClick();
      }
    });

    assertThat(activity.sendSync.await(1, TimeUnit.SECONDS)).isTrue();
    runTestOnUiThread(new Runnable() {
      @Override
      public void run() {
        assertThat(activity.view).hasTextString("thing 1thing 2");

        assertThat(activity.getSupportLoaderManager().getLoader(UserActivity.LOADER_SEND))
            .describedAs("loader is not destroyed")
            .isNull();
      }
    });

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
  }

}

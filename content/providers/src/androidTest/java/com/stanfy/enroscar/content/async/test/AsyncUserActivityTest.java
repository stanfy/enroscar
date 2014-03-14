package com.stanfy.enroscar.content.async.test;

import android.test.ActivityInstrumentationTestCase2;

import java.util.concurrent.TimeUnit;

import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Test of AsyncUserActivity.
 */
public class AsyncUserActivityTest extends ActivityInstrumentationTestCase2<AsyncUserActivity> {

  /** Activity instance. */
  private AsyncUserActivity activity;

  public AsyncUserActivityTest() {
    super(AsyncUserActivity.class);
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
        assertThat(activity.thing).isNotNull();
        assertThat(activity.textView).hasTextString(activity.thing.toString());
      }
    });
  }

  public void testSendThing() throws Throwable {
    assertThat(activity.loadSync.await(1, TimeUnit.SECONDS)).isTrue();
    final AsyncUserActivity.Thing lastThing = activity.thing;

    runTestOnUiThread(new Runnable() {
      @Override
      public void run() {
        activity.textView.performClick();
      }
    });

    assertThat(activity.sendSync.await(1, TimeUnit.SECONDS)).isTrue();
    runTestOnUiThread(new Runnable() {
      @Override
      public void run() {
        assertThat(activity.thing).isNotSameAs(lastThing).isNotNull();
        assertThat(activity.textView).hasTextString(
            lastThing.toString() + "Send result: " + activity.thing
        );
      }
    });
  }

  public void xtestRetainLoadedThingAfterRecreation() throws Throwable {
    assertThat(activity.loadSync.await(1, TimeUnit.SECONDS)).isTrue();
    AsyncUserActivity.Thing thing = activity.thing;
    assertThat(thing).isNotNull();
    runTestOnUiThread(new Runnable() {
      @Override
      public void run() {
        getInstrumentation().callActivityOnPause(activity);
        getInstrumentation().callActivityOnStop(activity);
        getInstrumentation().callActivityOnDestroy(activity);
      }
    });
    getInstrumentation().waitForIdleSync();
    setActivity(null);
    activity = getActivity();
    assertThat(activity.thing).isSameAs(thing);
  }

}

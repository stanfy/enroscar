package com.stanfy.enroscar.async.test;

import java.util.concurrent.TimeUnit;

import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Test of AsyncUserActivity.
 */
public class AsyncUserActivityAndroidTest extends BaseActivityAndroidTest<AsyncUserActivity> {

  /** Activity instance. */
  private AsyncUserActivity activity;

  public AsyncUserActivityAndroidTest() {
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
        assertThat(activity.thing.param).isEqualTo(1);
        assertThat(activity.textViewLoad).hasTextString(activity.thing.toString());
      }
    });
  }

  public void testSendThing() throws Throwable {
    assertThat(activity.loadSync.await(1, TimeUnit.SECONDS)).isTrue();
    final AsyncUserActivity.Thing lastThing = activity.thing;

    runTestOnUiThread(new Runnable() {
      @Override
      public void run() {
        activity.textViewSend.performClick();
      }
    });

    assertThat(activity.sendSync.await(1, TimeUnit.SECONDS)).isTrue();
    runTestOnUiThread(new Runnable() {
      @Override
      public void run() {
        assertThat(activity.thing).isNotSameAs(lastThing).isNotNull();
        assertThat(activity.thing.param).isEqualTo(2);
        assertThat(activity.textViewSend).hasTextString(activity.thing.toString());
      }
    });
  }

  public void testLazyLoadThing() throws Throwable {
    assertThat(activity.loadSync.await(1, TimeUnit.SECONDS)).isTrue();
    final AsyncUserActivity.Thing lastThing = activity.thing;

    runTestOnUiThread(new Runnable() {
      @Override
      public void run() {
        activity.textViewLoad.performClick();
      }
    });

    assertThat(activity.lazyLoadSync.await(1, TimeUnit.SECONDS)).isTrue();
    runTestOnUiThread(new Runnable() {
      @Override
      public void run() {
        assertThat(activity.thing).isNotSameAs(lastThing).isNotNull();
        assertThat(activity.thing.param).isEqualTo(3);
        assertThat(activity.textViewLoad).hasTextString(lastThing.toString() + activity.thing);
      }
    });
  }

  public void testStartActionInvocation() throws Throwable {
    activity.startInvoked = false;
    runTestOnUiThread(new Runnable() {
      @Override
      public void run() {
        activity.textViewSend.performClick();
        assertThat(activity.startInvoked).isTrue();
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

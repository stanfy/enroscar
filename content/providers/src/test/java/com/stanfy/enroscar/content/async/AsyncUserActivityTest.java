package com.stanfy.enroscar.content.async;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Test of AsyncUserActivity.
 */
@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = android.os.Build.VERSION_CODES.JELLY_BEAN_MR2)
public class AsyncUserActivityTest {

  /** Activity instance. */
  private AsyncUserActivity activity;

  @Before
  public void init() {
    Robolectric.getBackgroundScheduler().pause();
    activity = Robolectric.setupActivity(AsyncUserActivity.class);
  }

  @Test
  public void shouldLoadThing() {
    assertThat(activity.textView).isEmpty();
    Robolectric.runBackgroundTasks();
    assertThat(activity.textView).isNotEmpty();
  }

  @Test
  public void shouldRetainLoadedThingAfterREcreation() {
    Robolectric.runBackgroundTasks();
    AsyncUserActivity.Thing thing = activity.thing;
    assertThat(thing).isNotNull();
    activity.thing = null;
    Robolectric.shadowOf(activity).recreate();
    assertThat(activity.thing).isSameAs(thing);
  }

}

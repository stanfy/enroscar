package com.stanfy.enroscar.async.test;

import android.test.ActivityInstrumentationTestCase2;

import java.util.concurrent.TimeUnit;

import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;

public class CursorAsyncUserActivityAndroidTest extends ActivityInstrumentationTestCase2<CursorAsyncUserActivity> {

  public CursorAsyncUserActivityAndroidTest() {
    super(CursorAsyncUserActivity.class);
  }

  public void testCursorLoading() throws Exception {
    CursorAsyncUserActivity activity = getActivity();
    assertThat(activity.cursorSync.await(10, TimeUnit.SECONDS)).isTrue();
    assertThat(activity.cursor)
        .hasColumns("a", "b")
        .hasCount(1);
    assertThat(activity.cursor.moveToFirst()).isTrue();
    assertThat(activity.cursor.getString(0)).isEqualTo("one fish");
    assertThat(activity.cursor.getString(1)).isEqualTo("two fish");
  }

}

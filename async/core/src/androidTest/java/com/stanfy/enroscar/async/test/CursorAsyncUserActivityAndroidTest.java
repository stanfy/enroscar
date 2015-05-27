package com.stanfy.enroscar.async.test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.android.api.Assertions.assertThat;

public class CursorAsyncUserActivityAndroidTest extends BaseActivityAndroidTest<CursorAsyncUserActivity> {

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

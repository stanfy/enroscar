package com.stanfy.enroscar.async.test;

import android.database.Cursor;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.android.api.Assertions.assertThat;

public class CursorCloseAndroidTest extends BaseActivityAndroidTest<CursorAsyncUserActivity> {

  public CursorCloseAndroidTest() {
    super(CursorAsyncUserActivity.class);
  }

  public void testCursorReset() throws Throwable {
    final CursorAsyncUserActivity activity = getActivity();
    assertThat(activity.cursorSync.await(10, TimeUnit.SECONDS)).isTrue();
    assertThat(activity.cursor).isNotNull();
    final Cursor c = activity.cursor;

    runTestOnUiThread(new Runnable() {
      @Override
      public void run() {
        getInstrumentation().callActivityOnDestroy(activity);
        setActivity(null);
        assertThat(activity.cursor).isNull();
        assertThat(c).isClosed();
      }
    });
  }

}

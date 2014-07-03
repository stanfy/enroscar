package com.stanfy.enroscar.async.test;

import android.database.Cursor;
import android.test.ActivityInstrumentationTestCase2;

import java.util.concurrent.TimeUnit;

import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;

public class CursorCloseAndroidTest extends ActivityInstrumentationTestCase2<CursorAsyncUserActivity> {

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

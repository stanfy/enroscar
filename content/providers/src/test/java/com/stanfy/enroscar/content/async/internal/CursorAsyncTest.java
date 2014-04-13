package com.stanfy.enroscar.async.internal;

import android.database.Cursor;
import android.net.Uri;

import com.stanfy.enroscar.async.Async;
import com.stanfy.enroscar.async.AsyncObserver;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static com.stanfy.enroscar.async.Tools.asyncCursor;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.robolectric.Robolectric.application;
import static org.robolectric.Robolectric.shadowOf;

/**
 * Tests for CursorAsync.
 */
@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = android.os.Build.VERSION_CODES.JELLY_BEAN_MR2)
public class CursorAsyncTest {

  private final Uri uri = Uri.parse("content://test/uri");

  @SuppressWarnings("unchecked")
  @Test
  public void shouldRegisterContentObserver() {
    assertThat(shadowOf(application.getContentResolver()).getContentObserver(uri)).isNull();

    asyncCursor(application)
        .uri(uri)
        .get()
        .subscribe(mock(AsyncObserver.class));

    assertThat(shadowOf(application.getContentResolver()).getContentObserver(uri)).isNotNull();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void shouldUnregisterObserver() {
    Async<Cursor> async = asyncCursor(application)
        .uri(uri)
        .get();
    async.subscribe(mock(AsyncObserver.class));
    async.cancel();
    assertThat(shadowOf(application.getContentResolver()).getContentObserver(uri)).isNull();
  }

}

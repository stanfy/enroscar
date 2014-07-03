package com.stanfy.enroscar.async.content;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import com.stanfy.enroscar.async.Async;
import com.stanfy.enroscar.async.AsyncObserver;
import com.stanfy.enroscar.async.Releaser;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowContentResolver;

import static com.stanfy.enroscar.async.Tools.asyncCursor;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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

  @Test
  public void replicateShouldReturnNewInstance() {
    Async<Cursor> async = asyncCursor(application).uri(uri).get();
    assertThat(async.replicate()).isNotSameAs(async).isNotNull();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void closesCursor() {
    Async<Cursor> async = asyncCursor(application).uri(uri).get();
    Cursor cursor = mock(Cursor.class);
    ((Releaser<Cursor>) async).release(cursor);
    verify(cursor).close();
  }

  @Test
  public void shouldPassAllParametersToContentProvider() {
    final MatrixCursor testData = new MatrixCursor(new String[] {"a"});
    final Object[] params = {null, null, null, null, null};
    ShadowContentResolver.registerProvider(uri.getAuthority(), new ContentProvider() {
      @Override
      public boolean onCreate() {
        return true;
      }

      @Override
      public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        params[0] = uri;
        params[1] = projection;
        params[2] = selection;
        params[3] = selectionArgs;
        params[4] = sortOrder;
        return testData;
      }

      @Override
      public String getType(Uri uri) {
        return null;
      }

      @Override
      public Uri insert(Uri uri, ContentValues values) {
        return null;
      }

      @Override
      public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
      }

      @Override
      public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
      }
    });

    final Cursor[] result = {null};
    asyncCursor(application)
        .uri(uri)
        .projection(new String[] {"p"})
        .selection("where")
        .selectionArgs(new String[] {"arg"})
        .sort("asc")
        .get().subscribe(new AsyncObserver<Cursor>() {
      @Override
      public void onError(Throwable e) {
        throw new AssertionError(e);
      }

      @Override
      public void onResult(Cursor data) {
        result[0] = data;
      }

      @Override
      public void onReset() {
        // nothing
      }
    });

    Robolectric.runBackgroundTasks();

    assertThat(params)
        .containsExactly(uri, new String[] {"p"}, "where", new String[] {"arg"}, "asc");
    assertThat(result).containsExactly(testData);
  }

}

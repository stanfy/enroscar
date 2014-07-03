package com.stanfy.enroscar.async.content;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import com.stanfy.enroscar.async.Async;
import com.stanfy.enroscar.async.AsyncObserver;
import com.stanfy.enroscar.async.Releaser;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowContentResolver;

import java.util.List;

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
public class ConvertedCursorAsyncTest {

  private final Uri uri = Uri.parse("content://test/uri");
  private MatrixCursor testData;

  @Before
  public void setUp() throws Exception {
    testData = new MatrixCursor(new String[] {"a"});
    ShadowContentResolver.registerProvider(uri.getAuthority(), new ContentProvider() {
      @Override
      public boolean onCreate() {
        return true;
      }

      @Override
      public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
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
  }

  @SuppressWarnings("unchecked")
  @Test
  public void shouldRegisterContentObserver() {
    assertThat(shadowOf(application.getContentResolver()).getContentObserver(uri)).isNull();

    asyncCursor(application)
        .uri(uri)
        .convert(mock(CursorConverter.class))
        .get()
        .subscribe(mock(AsyncObserver.class));

    assertThat(shadowOf(application.getContentResolver()).getContentObserver(uri)).isNotNull();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void shouldUnregisterObserver() {
    Async<?> async = asyncCursor(application)
        .convert(mock(CursorConverter.class))
        .uri(uri)
        .get();
    async.subscribe(mock(AsyncObserver.class));
    async.cancel();
    assertThat(shadowOf(application.getContentResolver()).getContentObserver(uri)).isNull();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void replicateShouldReturnNewInstance() {
    Async<?> async = asyncCursor(application)
        .uri(uri)
        .convertFirst(mock(CursorConverter.class))
        .get();
    assertThat(async.replicate()).isNotSameAs((Async) async).isNotNull();
  }

  @Test
  public void converterDelegation() {
    final String[] result = {null};
    asyncCursor(application)
        .uri(uri)
        .convert(new CursorConverter<String>() {
          @Override
          public String toObject(Cursor cursor) {
            return "ok";
          }
        })
        .get().subscribe(new AsyncObserver<String>() {
      @Override
      public void onError(Throwable e) {
        throw new AssertionError(e);
      }

      @Override
      public void onResult(String data) {
        result[0] = data;
      }

      @Override
      public void onReset() {
        // nothing
      }
    });

    Robolectric.runBackgroundTasks();

    assertThat(result).containsExactly("ok");
  }

  @Test
  public void convertFirst() {
    assertThat(testData.getPosition()).isNotZero();
    testData.addRow(new Object[] {"first"});
    final String[] result = {null};
    asyncCursor(application)
        .uri(uri)
        .convertFirst(new CursorConverter<String>() {
          @Override
          public String toObject(Cursor cursor) {
            return "f";
          }
        })
        .get().subscribe(new AsyncObserver<String>() {
      @Override
      public void onError(Throwable e) {
        throw new AssertionError(e);
      }

      @Override
      public void onResult(String data) {
        result[0] = data;
      }

      @Override
      public void onReset() {
        // nothing
      }
    });

    Robolectric.runBackgroundTasks();

    assertThat(result).containsExactly("f");
    assertThat(testData.getPosition()).isZero();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void convertList() {
    assertThat(testData.getPosition()).isEqualTo(-1);
    testData.addRow(new Object[] {"first"});
    testData.addRow(new Object[] {"second"});
    final List<String>[] result = new List[]{null};
    asyncCursor(application)
        .uri(uri)
        .convertList(new CursorConverter<String>() {
          @Override
          public String toObject(Cursor cursor) {
            return cursor.getString(0);
          }
        })
        .get().subscribe(new AsyncObserver<List<String>>() {
      @Override
      public void onError(Throwable e) {
        throw new AssertionError(e);
      }

      @Override
      public void onResult(List<String> data) {
        result[0] = data;
      }

      @Override
      public void onReset() {
        // nothing
      }
    });

    Robolectric.runBackgroundTasks();

    assertThat(result[0]).containsExactly("first", "second");
  }
}

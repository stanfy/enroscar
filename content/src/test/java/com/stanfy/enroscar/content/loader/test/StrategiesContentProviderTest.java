package com.stanfy.enroscar.content.loader.test;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.stanfy.enroscar.content.StrategiesContentProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Tests for StrategiesContentProvider.
 */
@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18)
public class StrategiesContentProviderTest {

  /** Provider under the test. */
  private StrategiesContentProvider<SQLiteOpenHelper> provider;

  /** Test strategy. */
  private StrategiesContentProvider.Strategy<SQLiteOpenHelper> testStrategy;

  /** Last requested URI. */
  private Uri lastUri;

  @Before
  public void create() {
    provider = new StrategiesContentProvider<SQLiteOpenHelper>() {
      @Override
      protected void onStrategyMatcherCreate(final StrategyMatcher<SQLiteOpenHelper> matcher) {
        testStrategy = new SimpleStrategy<SQLiteOpenHelper>() {
          @Override
          public Cursor query(final SQLiteOpenHelper appDbManager, final Uri uri, final String[] projection, final String selection, final String[] selectionArgs, final String sortOrder) {
            lastUri = uri;
            return super.query(appDbManager, uri, projection, selection, selectionArgs, sortOrder);
          }
        };
        matcher.registerStrategy("test", "test", testStrategy);
      }

      @Override
      protected SQLiteOpenHelper getDatabaseManager(final Context context) {
        return null;
      }
    };
    provider.onCreate();
  }

  @Test
  public void shouldCallStrategiesCreateCallbackOnCreate() {
    assertThat(testStrategy).describedAs("onStrategyMatcherCreate is not called").isNotNull();
  }

  @Test
  public void shouldUseStrategies() {
    lastUri = null;
    provider.query(Uri.parse("content://test/test"), null, null, null, null, null);
    assertThat(lastUri).describedAs("Query is not delegated to strategy").isNotNull();
    lastUri = null;
    provider.query(Uri.parse("content://test/test2"), null, null, null, null, null);
    assertThat(lastUri).describedAs("Incorrect strategy used").isNull();
  }

}

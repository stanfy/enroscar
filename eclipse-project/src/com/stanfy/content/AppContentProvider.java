package com.stanfy.content;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

/**
 * Default application content provider. Provides access to API and images cache.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class AppContentProvider extends ContentProvider {

  /** MIME types. */
  protected static final String MIME_DIR = "vnd.android.cursor.dir",
                                MIME_ITEM = "vnd.android.cursor.item";

  /** DB manager. */
  private AppDatabaseManager dbManager;

  /** URI matcher. */
  private UriMatcher uriMatcher;

  /** @return app database manager */
  protected AppDatabaseManager createAppDatabaseManager() { return null; } //new AppDatabaseManager(getContext(), null, 1); }
  /** @return app database manager */
  protected AppDatabaseManager getAppDatabaseManager() { return dbManager; }

  /**
   * Configure URI matcher.
   * @param uriMatcher URI matcher instance
   */
  protected void onUriMatcherCreate(final UriMatcher uriMatcher) {

  }

  /**
   * Match given URI with internal matcher.
   * @param uri URI
   * @return match result
   */
  protected int match(final Uri uri) { return uriMatcher.match(uri); }

  @Override
  public boolean onCreate() {
    dbManager = createAppDatabaseManager();
    uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    onUriMatcherCreate(uriMatcher);
    return true;
  }

  @Override
  public Cursor query(final Uri uri, final String[] projection, final String selection, final String[] selectionArgs, final String sortOrder) {
    switch (uriMatcher.match(uri)) {


    default:
      return null;
    }
  }

  @Override
  public String getType(final Uri uri) {
    switch (uriMatcher.match(uri)) {
    default:
      return null;
    }
  }

  @Override
  public Uri insert(final Uri uri, final ContentValues values) {
    switch (uriMatcher.match(uri)) {

    default:
      return null;
    }
  }

  @Override
  public int delete(final Uri uri, final String selection, final String[] selectionArgs) {
    switch (uriMatcher.match(uri)) {

    default:
      return 0;
    }
  }

  @Override
  public int update(final Uri uri, final ContentValues values, final String selection, final String[] selectionArgs) {
    switch (uriMatcher.match(uri)) {

    default:
      return 0;
    }
  }

}

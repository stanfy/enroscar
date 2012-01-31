package com.stanfy.content;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import com.stanfy.images.model.CachedImage;
import com.stanfy.serverapi.cache.APICacheDAO;

/**
 * Default application content provider. Provides access to API and images cache.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class AppContentProvider extends ContentProvider {

  /** Some paths. */
  public static final String PATH_API_CACHE = "apicache",
                             PATH_IMAGES_CACHE = "imgcache";

  /** MIME types. */
  protected static final String MIME_DIR = "vnd.android.cursor.dir",
                                MIME_ITEM = "vnd.android.cursor.item";

  /** API cache. */
  protected static final int API_CACHE = 10;
  /** Images cache. */
  protected static final int IMAGES_CACHE = 11;
  /** Base value for user codes. */
  protected static final int USER_BASE = 100;

  /** DB manager. */
  private AppDatabaseManager dbManager;

  /** URI matcher. */
  private UriMatcher uriMatcher;

  /** @return app database manager */
  protected AppDatabaseManager createAppDatabaseManager() { return new AppDatabaseManager(getContext(), null, 1); }
  /** @return app database manager */
  protected AppDatabaseManager getAppDatabaseManager() { return dbManager; }

  /**
   * Configure URI matcher.
   * @param uriMatcher URI matcher instance
   */
  protected void onUriMatcherCreate(final UriMatcher uriMatcher) {

  }

  /**
   * Configure URI matcher to recognize API and images cache requests.
   * @param authority application authority
   * @param uriMatcher URI matcher instance
   */
  protected final void configureCacheDAO(final String authority, final UriMatcher uriMatcher) {
    uriMatcher.addURI(authority, PATH_API_CACHE, API_CACHE);
    uriMatcher.addURI(authority, PATH_IMAGES_CACHE, IMAGES_CACHE);
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

    case API_CACHE:
      return dbManager.getReadableDatabase().query(APICacheDAO.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
    case IMAGES_CACHE:
      return dbManager.getReadableDatabase().query(CachedImage.Contract.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);

    default:
      return null;
    }
  }

  @Override
  public String getType(final Uri uri) {
    switch (uriMatcher.match(uri)) {
    case API_CACHE: return MIME_DIR + "/vnd.stanfy.apicache";
    case IMAGES_CACHE: return MIME_DIR + "/vnd.stanfy.imgcache";
    default:
      return null;
    }
  }

  @Override
  public Uri insert(final Uri uri, final ContentValues values) {
    switch (uriMatcher.match(uri)) {

    case API_CACHE:
      final long apiId = dbManager.getWritableDatabase().insert(APICacheDAO.TABLE_NAME, null, values);
      return Uri.withAppendedPath(uri, String.valueOf(apiId));
    case IMAGES_CACHE:
      final long imageId = dbManager.getWritableDatabase().insert(CachedImage.Contract.TABLE_NAME, null, values);
      return Uri.withAppendedPath(uri, String.valueOf(imageId));

    default:
      return null;
    }
  }

  @Override
  public int delete(final Uri uri, final String selection, final String[] selectionArgs) {
    switch (uriMatcher.match(uri)) {

    case API_CACHE:
      return dbManager.getWritableDatabase().delete(APICacheDAO.TABLE_NAME, selection, selectionArgs);
    case IMAGES_CACHE:
      return dbManager.getWritableDatabase().delete(CachedImage.Contract.TABLE_NAME, selection, selectionArgs);

    default:
      return 0;
    }
  }

  @Override
  public int update(final Uri uri, final ContentValues values, final String selection, final String[] selectionArgs) {
    switch (uriMatcher.match(uri)) {

    case API_CACHE:
      return dbManager.getWritableDatabase().update(APICacheDAO.TABLE_NAME, values, selection, selectionArgs);
    case IMAGES_CACHE:
      return dbManager.getWritableDatabase().update(CachedImage.Contract.TABLE_NAME, values, selection, selectionArgs);

    default:
      return 0;
    }
  }

}

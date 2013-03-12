package com.stanfy.enroscar.content;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.SparseArray;

/**
 * Content provider that uses strategies.
 * Provides you API similar to {@link UriMatcher} but allowing binding
 * {@link Strategy}s instead of identifiers to URI patterns.
 * <p>
 *   Override method {@code onStrategyMatcherCreate} in order to configure bindings 
 *   and {@code getDatabaseManager} in order to provide {@link SQLiteOpenHelper} instance.
 * </p>
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public abstract class StrategiesContentProvider extends ContentProvider {

  /** URI matcher. */
  private StrategyMatcher strategyMatcher;

  /** @return StrategyMatcher instance */
  protected StrategyMatcher getStrategyMatcher() { return strategyMatcher; }

  @Override
  public boolean onCreate() {
    strategyMatcher = new StrategyMatcher(new UriMatcher(UriMatcher.NO_MATCH));
    return strategyMatcher.isConfigured();
  }

  /**
   * Configure URI matcher.
   * <p>
   *   Example:
   *   <pre>
   *     matcher.registerStrategy("com.example.authority", "/path/#", new SimpleStrategy() {
   *         public Cursor query(SQLiteOpenHelper dbManager, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
   *           return dbManager.getReadableDatabase().rawQuery("select * from example", null);
   *         }
   *     });
   *   </pre>
   * </p>
   * @param uriMatcher URI matcher instance
   */
  protected abstract void onStrategyMatcherCreate(final StrategyMatcher uriMatcher);

  /**
   * @return context application context
   * @return database manager instance
   */
  protected abstract SQLiteOpenHelper getDatabaseManager(final Context context);
  
  @Override
  public Cursor query(final Uri uri, final String[] projection, final String selection, final String[] selectionArgs, final String sortOrder) {
    final Strategy strategy = strategyMatcher.choose(uri);
    return strategy != null ? strategy.query(getDatabaseManager(getContext()), uri, projection, selection, selectionArgs, sortOrder) : null;
  }

  @Override
  public String getType(final Uri uri) {
    final Strategy strategy = strategyMatcher.choose(uri);
    return strategy != null ? strategy.getType(getDatabaseManager(getContext()), uri) : null;
  }

  @Override
  public Uri insert(final Uri uri, final ContentValues values) {
    final Strategy strategy = strategyMatcher.choose(uri);
    return strategy != null ? strategy.insert(getDatabaseManager(getContext()), uri, values) : null;
  }

  @Override
  public int delete(final Uri uri, final String selection, final String[] selectionArgs) {
    final Strategy strategy = strategyMatcher.choose(uri);
    return strategy != null ? strategy.delete(getDatabaseManager(getContext()), uri, selection, selectionArgs) : 0;
  }

  @Override
  public int update(final Uri uri, final ContentValues values, final String selection, final String[] selectionArgs) {
    final Strategy strategy = strategyMatcher.choose(uri);
    return strategy != null ? strategy.update(getDatabaseManager(getContext()), uri, values, selection, selectionArgs) : 0;
  }

  /**
   * A utility class that allows to register different content provider strategies for different URLs.
   * @author Roman Mazur (Stanfy - http://stanfy.com)
   * @see android.content.UriMatcher
   */
  public static class StrategyMatcher {
    /** Worker instance. */
    private final UriMatcher idsMatcher;
    /** Strategies register. */
    private final SparseArray<Strategy> strategiesRegister = new SparseArray<Strategy>(10);

    /** Default strategy. */
    private Strategy defaultStrategy;

    /** Counter. */
    private int counter = 0;

    public StrategyMatcher(final UriMatcher idsMatcher) {
      this.idsMatcher = idsMatcher;
    }

    public void setDefaultStrategy(final Strategy defaultStrategy) {
      this.defaultStrategy = defaultStrategy;
    }

    public void registerStrategy(final String authority, final String path, final Strategy strategy) {
      ++counter;
      idsMatcher.addURI(authority, path, counter);
      strategiesRegister.put(counter, strategy);
    }

    public Strategy choose(final Uri uri) {
      final int id = idsMatcher.match(uri);
      if (id == UriMatcher.NO_MATCH) { return defaultStrategy; }
      return strategiesRegister.get(id, defaultStrategy);
    }

    public boolean isConfigured() { return counter > 0 || defaultStrategy != null; }
  }

  /** Content provider strategy. */
  public interface Strategy {
    /**
     * @see android.content.ContentProvider#query(Uri, String[], String, String[], String)
     */
    Cursor query(final SQLiteOpenHelper appDbManager, final Uri uri, final String[] projection, final String selection, final String[] selectionArgs, final String sortOrder);

    /**
     * @see android.content.ContentProvider#getType(Uri)
     */
    String getType(final SQLiteOpenHelper appDbManager, final Uri uri);

    /**
     * @see android.content.ContentProvider#insert(Uri, ContentValues)
     */
    Uri insert(final SQLiteOpenHelper appDbManager, final Uri uri, final ContentValues values);

    /**
     * @see android.content.ContentProvider#delete(Uri, String, String[])
     */
    int delete(final SQLiteOpenHelper appDbManager, final Uri uri, final String selection, final String[] selectionArgs);

    /**
     * @see android.content.ContentProvider#update(Uri, ContentValues, String, String[])
     */
    int update(final SQLiteOpenHelper appDbManager, final Uri uri, final ContentValues values, final String selection, final String[] selectionArgs);
  }

  /** Simple strategy, use it when you do not want to implement all the methods of {@link Strategy}. */
  public static class SimpleStrategy implements Strategy {

    @Override
    public Cursor query(final SQLiteOpenHelper appDbManager, final Uri uri, final String[] projection, final String selection, final String[] selectionArgs, final String sortOrder) {
      return null;
    }

    @Override
    public String getType(final SQLiteOpenHelper appDbManager, final Uri uri) {
      return null;
    }

    @Override
    public Uri insert(final SQLiteOpenHelper appDbManager, final Uri uri, final ContentValues values) {
      return null;
    }

    @Override
    public int delete(final SQLiteOpenHelper appDbManager, final Uri uri, final String selection, final String[] selectionArgs) {
      return 0;
    }

    @Override
    public int update(final SQLiteOpenHelper appDbManager, final Uri uri, final ContentValues values, final String selection, final String[] selectionArgs) {
      return 0;
    }

  }

}

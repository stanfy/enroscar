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
 *
 * @param <T> type of used SQLiteOpenHelper
 *
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public abstract class StrategiesContentProvider<T extends SQLiteOpenHelper> extends ContentProvider {

  /** URI matcher. */
  private StrategyMatcher<T> strategyMatcher;

  /** @return StrategyMatcher instance */
  protected StrategyMatcher<T> getStrategyMatcher() { return strategyMatcher; }

  @Override
  public boolean onCreate() {
    strategyMatcher = new StrategyMatcher<T>(new UriMatcher(UriMatcher.NO_MATCH));
    onStrategyMatcherCreate(strategyMatcher);
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
   * @param matcher URI matcher instance
   */
  protected abstract void onStrategyMatcherCreate(final StrategyMatcher<T> matcher);

  /**
   * @param context application context
   * @return database manager instance
   */
  protected abstract T getDatabaseManager(final Context context);
  
  @Override
  public Cursor query(final Uri uri, final String[] projection, final String selection, final String[] selectionArgs, final String sortOrder) {
    final Strategy<T> strategy = strategyMatcher.choose(uri);
    return strategy != null ? strategy.query(getDatabaseManager(getContext()), uri, projection, selection, selectionArgs, sortOrder) : null;
  }

  @Override
  public String getType(final Uri uri) {
    final Strategy<T> strategy = strategyMatcher.choose(uri);
    return strategy != null ? strategy.getType(getDatabaseManager(getContext()), uri) : null;
  }

  @Override
  public Uri insert(final Uri uri, final ContentValues values) {
    final Strategy<T> strategy = strategyMatcher.choose(uri);
    return strategy != null ? strategy.insert(getDatabaseManager(getContext()), uri, values) : null;
  }

  @Override
  public int delete(final Uri uri, final String selection, final String[] selectionArgs) {
    final Strategy<T> strategy = strategyMatcher.choose(uri);
    return strategy != null ? strategy.delete(getDatabaseManager(getContext()), uri, selection, selectionArgs) : 0;
  }

  @Override
  public int update(final Uri uri, final ContentValues values, final String selection, final String[] selectionArgs) {
    final Strategy<T> strategy = strategyMatcher.choose(uri);
    return strategy != null ? strategy.update(getDatabaseManager(getContext()), uri, values, selection, selectionArgs) : 0;
  }

  /**
   * A utility class that allows to register different content provider strategies for different URLs.
   * @see android.content.UriMatcher
   * @param <T> type of used SQLiteOpenHelper
   */
  public static class StrategyMatcher<T extends SQLiteOpenHelper> {
    /** Worker instance. */
    private final UriMatcher idsMatcher;
    /** Strategies register. */
    private final SparseArray<Strategy<T>> strategiesRegister = new SparseArray<Strategy<T>>(10);

    /** Default strategy. */
    private Strategy<T> defaultStrategy;

    /** Counter. */
    private int counter = 0;

    public StrategyMatcher(final UriMatcher idsMatcher) {
      this.idsMatcher = idsMatcher;
    }

    public void setDefaultStrategy(final Strategy<T> defaultStrategy) {
      this.defaultStrategy = defaultStrategy;
    }

    public void registerStrategy(final String authority, final String path, final Strategy<T> strategy) {
      ++counter;
      idsMatcher.addURI(authority, path, counter);
      strategiesRegister.put(counter, strategy);
    }

    public Strategy<T> choose(final Uri uri) {
      final int id = idsMatcher.match(uri);
      if (id == UriMatcher.NO_MATCH) { return defaultStrategy; }
      return strategiesRegister.get(id, defaultStrategy);
    }

    public boolean isConfigured() { return counter > 0 || defaultStrategy != null; }
  }

  /**
   * Content provider strategy.
   * @param <T> type of SQLiteOpenHelper
   */
  public interface Strategy<T extends SQLiteOpenHelper> {
    /**
     * @see android.content.ContentProvider#query(Uri, String[], String, String[], String)
     */
    Cursor query(final T appDbManager, final Uri uri, final String[] projection, final String selection, final String[] selectionArgs, final String sortOrder);

    /**
     * @see android.content.ContentProvider#getType(Uri)
     */
    String getType(final T appDbManager, final Uri uri);

    /**
     * @see android.content.ContentProvider#insert(Uri, ContentValues)
     */
    Uri insert(final T appDbManager, final Uri uri, final ContentValues values);

    /**
     * @see android.content.ContentProvider#delete(Uri, String, String[])
     */
    int delete(final T appDbManager, final Uri uri, final String selection, final String[] selectionArgs);

    /**
     * @see android.content.ContentProvider#update(Uri, ContentValues, String, String[])
     */
    int update(final T appDbManager, final Uri uri, final ContentValues values, final String selection, final String[] selectionArgs);
  }

  /**
   * Simple strategy, use it when you do not want to implement all the methods of {@link Strategy}.
   * @param <T> type of SQLiteOpenHelper
   */
  public static class SimpleStrategy<T extends SQLiteOpenHelper> implements Strategy<T> {

    @Override
    public Cursor query(final T appDbManager, final Uri uri, final String[] projection, final String selection, final String[] selectionArgs, final String sortOrder) {
      return null;
    }

    @Override
    public String getType(final T appDbManager, final Uri uri) {
      return null;
    }

    @Override
    public Uri insert(final T appDbManager, final Uri uri, final ContentValues values) {
      return null;
    }

    @Override
    public int delete(final T appDbManager, final Uri uri, final String selection, final String[] selectionArgs) {
      return 0;
    }

    @Override
    public int update(final T appDbManager, final Uri uri, final ContentValues values, final String selection, final String[] selectionArgs) {
      return 0;
    }

  }

}

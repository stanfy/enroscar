package com.stanfy.content;

import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.SparseArray;

/**
 * Content provider that uses strategies. Not sure whether it'll be useful.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public abstract class StrategiesContentProvider extends AppContentProvider {

  /** URI matcher. */
  private StrategyMatcher strategyMatcher;

  /** @return StrategyMatcher instance */
  protected StrategyMatcher getStrategyMatcher() { return strategyMatcher; }

  @Override
  public boolean onCreate() {
    return super.onCreate() && strategyMatcher.isConfigured();
  }

  @Override
  protected final void onUriMatcherCreate(final UriMatcher uriMatcher) {
    strategyMatcher = new StrategyMatcher(getUriMatcher());
    onStrategyMatcherCreate(strategyMatcher);
  }

  /**
   * Configure URI matcher.
   * @param uriMatcher URI matcher instance
   */
  protected abstract void onStrategyMatcherCreate(final StrategyMatcher uriMatcher);

  @Override
  public Cursor query(final Uri uri, final String[] projection, final String selection, final String[] selectionArgs, final String sortOrder) {
    final Strategy strategy = strategyMatcher.choose(uri);
    return strategy != null ? strategy.query(getAppDatabaseManager(), uri, projection, selection, selectionArgs, sortOrder) : null;
  }

  @Override
  public String getType(final Uri uri) {
    final Strategy strategy = strategyMatcher.choose(uri);
    return strategy != null ? strategy.getType(getAppDatabaseManager(), uri) : null;
  }

  @Override
  public Uri insert(final Uri uri, final ContentValues values) {
    final Strategy strategy = strategyMatcher.choose(uri);
    return strategy != null ? strategy.insert(getAppDatabaseManager(), uri, values) : null;
  }

  @Override
  public int delete(final Uri uri, final String selection, final String[] selectionArgs) {
    final Strategy strategy = strategyMatcher.choose(uri);
    return strategy != null ? strategy.delete(getAppDatabaseManager(), uri, selection, selectionArgs) : 0;
  }

  @Override
  public int update(final Uri uri, final ContentValues values, final String selection, final String[] selectionArgs) {
    final Strategy strategy = strategyMatcher.choose(uri);
    return strategy != null ? strategy.update(getAppDatabaseManager(), uri, values, selection, selectionArgs) : 0;
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
    Cursor query(final AppDatabaseManager appDbManager, final Uri uri, final String[] projection, final String selection, final String[] selectionArgs, final String sortOrder);

    /**
     * @see android.content.ContentProvider#getType(Uri)
     */
    String getType(final AppDatabaseManager appDbManager, final Uri uri);

    /**
     * @see android.content.ContentProvider#insert(Uri, ContentValues)
     */
    Uri insert(final AppDatabaseManager appDbManager, final Uri uri, final ContentValues values);

    /**
     * @see android.content.ContentProvider#delete(Uri, String, String[])
     */
    int delete(final AppDatabaseManager appDbManager, final Uri uri, final String selection, final String[] selectionArgs);

    /**
     * @see android.content.ContentProvider#update(Uri, ContentValues, String, String[])
     */
    int update(final AppDatabaseManager appDbManager, final Uri uri, final ContentValues values, final String selection, final String[] selectionArgs);
  }

  /** Simple strategy, use it when you do not want to implement all the methods of {@link Strategy}. */
  public static class SimpleStrategy implements Strategy {

    @Override
    public Cursor query(final AppDatabaseManager appDbManager, final Uri uri, final String[] projection, final String selection, final String[] selectionArgs, final String sortOrder) {
      return null;
    }

    @Override
    public String getType(final AppDatabaseManager appDbManager, final Uri uri) {
      return null;
    }

    @Override
    public Uri insert(final AppDatabaseManager appDbManager, final Uri uri, final ContentValues values) {
      return null;
    }

    @Override
    public int delete(final AppDatabaseManager appDbManager, final Uri uri, final String selection, final String[] selectionArgs) {
      return 0;
    }

    @Override
    public int update(final AppDatabaseManager appDbManager, final Uri uri, final ContentValues values, final String selection, final String[] selectionArgs) {
      return 0;
    }

  }

}

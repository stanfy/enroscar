package com.stanfy.content;

import android.content.ContentProvider;
import android.content.UriMatcher;

import com.stanfy.enroscar.beans.BeansManager;

/**
 * Default application content provider. Provides access to API and images cache.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public abstract class AppContentProvider extends ContentProvider {

  /** MIME types. */
  protected static final String MIME_DIR = "vnd.android.cursor.dir",
                                MIME_ITEM = "vnd.android.cursor.item";

  /** URI matcher. */
  private UriMatcher uriMatcher;

  /** @return application database manager */
  public AppDatabaseManager getAppDatabaseManager() { return BeansManager.get(getContext()).getAppDatabaseManager(); }

  /** @return URI matcher instance */
  protected UriMatcher getUriMatcher() { return uriMatcher; }

  /**
   * Configure URI matcher.
   * @param uriMatcher URI matcher instance
   */
  protected void onUriMatcherCreate(final UriMatcher uriMatcher) {

  }

  @Override
  public boolean onCreate() {
    uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    onUriMatcherCreate(uriMatcher);
    return true;
  }

}

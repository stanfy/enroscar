package com.stanfy.enroscar.async.content;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.stanfy.enroscar.async.Async;
import com.stanfy.enroscar.async.content.CursorAsync.ContentProviderQuery;

/**
 * Builder for {@link com.stanfy.enroscar.async.Async} that loads a {@link Cursor}.
 * Built operation will query a content provider and register an observer for the specified uri.
 * Subscribers will receive {@link com.stanfy.enroscar.async.AsyncObserver#onResult(Object)}
 * each time the content resolver is notified about changes.
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
public final class CursorAsyncBuilder {

  /** Content resolver instance used to make queries. */
  final ContentResolver resolver;

  /** Query parameters. */
  final Params params = new Params();

  public CursorAsyncBuilder(final ContentResolver resolver) {
    this.resolver = resolver;
  }

  public CursorAsyncBuilder(final Context context) {
    this.resolver = context.getContentResolver();
  }

  /**
   * Set an URI used to query a content provider.
   * @see android.content.CursorLoader#setUri(android.net.Uri)
   */
  public CursorAsyncBuilder uri(final Uri uri) {
    params.uri = uri;
    return this;
  }

  /**
   * Define a projection passed to content provider.
   * @see android.content.CursorLoader#setProjection(String[])
   */
  public CursorAsyncBuilder projection(final String[] projection) {
    params.projection = projection;
    return this;
  }

  /**
   * Define a selection passed to content provider.
   * @see android.content.CursorLoader#setSelection(String)
   */
  public CursorAsyncBuilder selection(final String selection) {
    params.selection = selection;
    return this;
  }

  /**
   * Define selection arguments passed to content provider.
   * @see android.content.CursorLoader#setSelectionArgs(String[])
   */
  public CursorAsyncBuilder selectionArgs(final String[] selectionArgs) {
    params.selectionArgs = selectionArgs;
    return this;
  }

  /**
   * Define a sort order passed to content provider.
   * @see android.content.CursorLoader#setSortOrder(String)
   */
  public CursorAsyncBuilder sort(final String sort) {
    params.sort = sort;
    return this;
  }

  /**
   * Set to {@code true} if you want to observe descendant URIs.
   * If <code>true</code> changes to URIs beginning with what is set with
   * {@link #uri(android.net.Uri)} will also induce delivering results to subscribers.
   * @see {@link android.content.ContentResolver#registerContentObserver(android.net.Uri, boolean, android.database.ContentObserver)}
   */
  public CursorAsyncBuilder observeDescendants(final boolean value) {
    params.observeDescendants = value;
    return this;
  }

  /**
   * Construct an asynchronous operation.
   * @return instance of {@link Async} that may be returned in methods
   *         annotated with {@link com.stanfy.enroscar.async.Load}
   */
  public Async<Cursor> get() {
    return new CursorAsync(new ContentProviderQuery(resolver, params), params.observeDescendants);
  }

  /** Loader params. */
  static class Params {

    /** Request URI. */
    Uri uri;

    /** Projection. */
    String[] projection;

    /** Selection. */
    String selection;

    /** Selection arguments. */
    String[] selectionArgs;

    /** Sort order. */
    String sort;

    /** Observation flag. */
    boolean observeDescendants;
  }
}

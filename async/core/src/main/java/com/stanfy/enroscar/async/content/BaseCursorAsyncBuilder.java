package com.stanfy.enroscar.async.content;

import android.content.ContentResolver;
import android.net.Uri;

import com.stanfy.enroscar.async.Async;

import java.util.concurrent.Executor;

/**
 * Base class for {@link com.stanfy.enroscar.async.Async} operations
 * that load {@link android.database.Cursor}.
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
public abstract class BaseCursorAsyncBuilder<B extends BaseCursorAsyncBuilder<?, ?>, D> {

  /** Content resolver instance used to make queries. */
  final ContentResolver resolver;

  /** Query parameters. */
  final Params params;

  Executor executor;

  private final B self;

  @SuppressWarnings("unchecked")
  protected BaseCursorAsyncBuilder(final ContentResolver resolver, final Params params) {
    this.resolver = resolver;
    this.params = params;
    this.self = (B) this;
  }

  BaseCursorAsyncBuilder(final ContentResolver resolver) {
    this(resolver, new Params());
  }

  /**
   * Set an URI used to query a content provider.
   * @see android.content.CursorLoader#setUri(android.net.Uri)
   */
  public B uri(final Uri uri) {
    params.uri = uri;
    return self;
  }

  /**
   * Define a projection passed to content provider.
   * @see android.content.CursorLoader#setProjection(String[])
   */
  public B projection(final String[] projection) {
    params.projection = projection;
    return self;
  }

  /**
   * Define a selection passed to content provider.
   * @see android.content.CursorLoader#setSelection(String)
   */
  public B selection(final String selection) {
    params.selection = selection;
    return self;
  }

  /**
   * Define selection arguments passed to content provider.
   * @see android.content.CursorLoader#setSelectionArgs(String[])
   */
  public B selectionArgs(final String[] selectionArgs) {
    params.selectionArgs = selectionArgs;
    return self;
  }

  /**
   * Define a sort order passed to content provider.
   * @see android.content.CursorLoader#setSortOrder(String)
   */
  public B sort(final String sort) {
    params.sort = sort;
    return self;
  }

  /**
   * Set to {@code true} if you want to observe descendant URIs.
   * If <code>true</code> changes to URIs beginning with what is set with
   * {@link #uri(android.net.Uri)} will also induce delivering results to subscribers.
   * @see android.content.ContentResolver#registerContentObserver(android.net.Uri, boolean, android.database.ContentObserver)
   */
  public B observeDescendants(final boolean value) {
    params.observeDescendants = value;
    return self;
  }

  /**
   * Define what executor is used to perform a query.
   * {@code null} means a default {@link android.os.AsyncTask} executor.
   */
  public B onExecutor(final Executor executor) {
    this.executor = executor;
    return self;
  }

  /**
   * Construct an asynchronous operation.
   * @return instance of {@link Async} that may be returned in methods
   *         annotated with {@link com.stanfy.enroscar.async.Load}
   */
  public abstract Async<D> get();

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

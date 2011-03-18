package com.stanfy.content;

import java.lang.ref.WeakReference;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

/**
 * Slightly more abstract {@link AsyncQueryHandler} that helps keep a
 * {@link WeakReference} back to a listener. Will properly close any
 * {@link Cursor} if the listener ceases to exist.
 * <p>
 * This pattern can be used to perform background queries without leaking
 * {@link Context} objects.
 */
public class NotifyingAsyncQueryHandler extends AsyncQueryHandler {

  /** Reference. */
  private WeakReference<AsyncQueryListener> mListener;

  /**
   * Interface to listen for completed query operations.
   */
  public interface AsyncQueryListener {
    /**
     * Is called when an sync query is completed (after startQuery).
     * @param token unique token
     * @param cookie object associated with the current request
     * @param cursor resulting cursor
     */
    void onQueryComplete(int token, Object cookie, Cursor cursor);
    /**
     * Is called when an async update is completed (after startUpdate).
     * @param token unique token
     * @param cookie object associated with the current request
     * @param resultCount result of update operation
     */
    void onUpdateComplete(int token, Object cookie, int resultCount);
    /**
     * Is called when an async delete is completed (after startDelete).
     * @param token unique token
     * @param cookie object associated with the current request
     * @param resultCount result of delete operation
     */
    void onDeleteComplete(int token, Object cookie, int resultCount);
  }

  public NotifyingAsyncQueryHandler(final ContentResolver resolver, final AsyncQueryListener listener) {
    super(resolver);
    setQueryListener(listener);
  }

  /**
   * Assign the given {@link AsyncQueryListener} to receive query events from
   * asynchronous calls. Will replace any existing listener.
   */
  public void setQueryListener(final AsyncQueryListener listener) {
    mListener = new WeakReference<AsyncQueryListener>(listener);
  }

  /**
   * Clear any {@link AsyncQueryListener} set through.
   * {@link #setQueryListener(AsyncQueryListener)}
   */
  public void clearQueryListener() {
    mListener = null;
  }

  /**
   * Begin an asynchronous query with the given arguments. When finished,
   * {@link AsyncQueryListener#onQueryComplete(int, Object, Cursor)} is
   * called if a valid {@link AsyncQueryListener} is present.
   */
  public void startQuery(final Uri uri, final String[] projection) {
    startQuery(-1, null, uri, projection, null, null, null);
  }

  /**
   * Begin an asynchronous query with the given arguments. When finished,
   * {@link AsyncQueryListener#onQueryComplete(int, Object, Cursor)} is called
   * if a valid {@link AsyncQueryListener} is present.
   *
   * @param token Unique identifier passed through to
   *            {@link AsyncQueryListener#onQueryComplete(int, Object, Cursor)}
   */
  public void startQuery(final int token, final Uri uri, final String[] projection) {
    startQuery(token, null, uri, projection, null, null, null);
  }

  /**
   * Begin an asynchronous query with the given arguments. When finished,
   * {@link AsyncQueryListener#onQueryComplete(int, Object, Cursor)} is called
   * if a valid {@link AsyncQueryListener} is present.
   */
  public void startQuery(final Uri uri, final String[] projection, final String sortOrder) {
    startQuery(-1, null, uri, projection, null, null, sortOrder);
  }

  /**
   * Begin an asynchronous query with the given arguments. When finished,
   * {@link AsyncQueryListener#onQueryComplete(int, Object, Cursor)} is called
   * if a valid {@link AsyncQueryListener} is present.
   */
  public void startQuery(final Uri uri, final String[] projection, final String selection,
      final String[] selectionArgs, final String orderBy) {
    startQuery(-1, null, uri, projection, selection, selectionArgs, orderBy);
  }

  /**
   * Begin an asynchronous update with the given arguments.
   */
  public void startUpdate(final Uri uri, final ContentValues values) {
    startUpdate(-1, null, uri, values, null, null);
  }

  public void startInsert(final Uri uri, final ContentValues values) {
    startInsert(-1, null, uri, values);
  }

  public void startDelete(final Uri uri) {
    startDelete(-1, null, uri, null, null);
  }

  @Override
  protected void onQueryComplete(final int token, final Object cookie, final Cursor cursor) {
    final AsyncQueryListener listener = mListener == null ? null : mListener.get();
    if (listener != null) {
      listener.onQueryComplete(token, cookie, cursor);
    } else if (cursor != null) {
      cursor.close();
    }
  }

  @Override
  protected void onUpdateComplete(final int token, final Object cookie, final int result) {
    final AsyncQueryListener listener = mListener == null ? null : mListener.get();
    if (listener != null) {
      listener.onUpdateComplete(token, cookie, result);
    }
  }

  @Override
  protected void onDeleteComplete(final int token, final Object cookie, final int result) {
    final AsyncQueryListener listener = mListener == null ? null : mListener.get();
    if (listener != null) {
      listener.onDeleteComplete(token, cookie, result);
    }
  }

}

package com.stanfy.enroscar.async.content;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;

import com.stanfy.enroscar.async.Releaser;
import com.stanfy.enroscar.async.content.CursorAsyncBuilder.Params;
import com.stanfy.enroscar.async.internal.TaskAsync;

import java.util.concurrent.Callable;

/**
 * {@link com.stanfy.enroscar.async.Async} implementation that queries a cursor from
 * content resolver.
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
final class CursorAsync extends TaskAsync<Cursor, CursorAsync.ContentProviderQuery>
    implements Releaser<Cursor> {

  /** Main thread handler. */
  private static final Handler H = new Handler(Looper.getMainLooper());

  /** Content observer. */
  private ContentObserver observer;

  /** Whether to observe descendants. */
  private final boolean observeDescendants;

  CursorAsync(final ContentProviderQuery query, final boolean observeDescendants) {
    super(query);
    this.observeDescendants = observeDescendants;
  }

  @Override
  public CursorAsync replicate() {
    return new CursorAsync(getTask(), observeDescendants);
  }

  @Override
  protected void onTrigger() {
    // it's assumed we are running on a main thread
    super.onTrigger();
    if (observer == null) {
      observer = new ContentObserver(H) {
        @Override
        public void onChange(boolean selfChange) {
          onTrigger();
        }
      };
      ContentProviderQuery task = getTask();
      task.resolver.registerContentObserver(task.params.uri, observeDescendants, observer);
    }
  }

  @Override
  protected void onCancel() {
    super.onCancel();
    if (observer != null) {
      getTask().resolver.unregisterContentObserver(observer);
    }
  }

  @Override
  public void release(final Cursor data) {
    data.close();
  }

  /**
   * Queries content provider.
   */
  static final class ContentProviderQuery implements Callable<Cursor> {

    /** Resolver instance. */
    final ContentResolver resolver;

    /** Parameters. */
    final Params params;

    ContentProviderQuery(final ContentResolver resolver, final Params params) {
      this.resolver = resolver;
      this.params = params;
    }

    @Override
    public Cursor call() {
      return resolver.query(params.uri, params.projection,
          params.selection, params.selectionArgs, params.sort);
    }

  }

}

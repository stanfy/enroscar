package com.stanfy.enroscar.content.async.internal;

import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;

import com.stanfy.enroscar.content.async.ContentProviderQuery;

/**
 * {@link com.stanfy.enroscar.content.async.Async} implementation that queries a cursor from
 * content resolver.
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
public class CursorAsync extends TaskAsync<Cursor, ContentProviderQuery> {

  /** Main thread handler. */
  private static final Handler H = new Handler(Looper.getMainLooper());

  /** Content observer. */
  private ContentObserver observer;

  /** Whether to observe descendants. */
  private final boolean observeDescendants;

  public CursorAsync(final ContentProviderQuery query, final boolean observeDescendants) {
    super(query);
    this.observeDescendants = observeDescendants;
  }

  @Override
  protected void onTrigger() {
    super.onTrigger();
    if (observer == null) {
      observer = new ContentObserver(H) {
        @Override
        public void onChange(boolean selfChange) {
          onTrigger();
        }
      };
      task.getResolver().registerContentObserver(task.getUri(), observeDescendants, observer);
    }
  }

  @Override
  protected void onCancel() {
    super.onCancel();
    if (observer != null) {
      task.getResolver().unregisterContentObserver(observer);
    }
  }

}

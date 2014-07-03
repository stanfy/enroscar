package com.stanfy.enroscar.async.content;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Looper;

import com.stanfy.enroscar.async.internal.TaskAsync;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

import static com.stanfy.enroscar.async.content.BaseCursorAsyncBuilder.Params;

/**
 * Async implementation that can register a content observer.
 */
abstract class ContentObserverAsync<D, T extends Callable<D>> extends TaskAsync<D, T> {

  /** Main thread handler. */
  private static final Handler H = new Handler(Looper.getMainLooper());

  /** Content observer. */
  private ContentObserver observer;

  public ContentObserverAsync(final T task, final Executor executor) {
    super(task, executor);
  }

  @Override
  public TaskAsync<D, T> replicate() {
    throw new UnsupportedOperationException("not implemented");
  }

  protected abstract ContentResolver getResolver();
  protected abstract Params getParams();

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
      Params params = getParams();
      getResolver().registerContentObserver(params.uri, params.observeDescendants, observer);
    }
  }

  @Override
  protected void onCancel() {
    super.onCancel();
    if (observer != null) {
      getResolver().unregisterContentObserver(observer);
    }
  }

}

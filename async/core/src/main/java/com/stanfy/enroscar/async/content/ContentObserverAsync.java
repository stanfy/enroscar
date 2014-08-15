package com.stanfy.enroscar.async.content;

import android.content.ContentResolver;
import android.database.ContentObserver;

import com.stanfy.enroscar.async.internal.TaskAsync;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

import static com.stanfy.enroscar.async.content.BaseCursorAsyncBuilder.Params;
import static com.stanfy.enroscar.async.internal.Utils.MAIN_THREAD_HANDLER;

/**
 * Async implementation that can register a content observer.
 */
abstract class ContentObserverAsync<D, T extends Callable<D>> extends TaskAsync<D, T> {

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
      observer = new ContentObserver(MAIN_THREAD_HANDLER) {
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
      observer = null;
    }
  }

}

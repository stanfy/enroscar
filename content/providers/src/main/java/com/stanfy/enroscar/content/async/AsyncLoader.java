package com.stanfy.enroscar.content.async;

import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.Loader;

/**
 * Loader that backs {@link Async} result.
 * @param <D> data type
 *
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
final class AsyncLoader<D> extends Loader<AsyncLoader.Result<D>> {

  /** Main thread handler. */
  private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

  /** Executor. */
  private final AsyncExecutor<D> executor;

  /** Pending result. */
  private Async<D> async;

  /** Received result. */
  private Result<D> result;

  /** Data observer. */
  private final AsyncObserver<D> observer = new AsyncObserver<D>() {
    @Override
    public void onError(final Throwable e) {
      post(new Result<D>(null, e));
    }
    @Override
    public void onResult(final D data) {
      post(new Result<>(data, null));
    }
  };

  public AsyncLoader(final AsyncExecutor<D> executor) {
    super(executor.provideContext());
    this.executor = executor;
  }

  @Override
  protected void onForceLoad() {
    if (async != null) {
      async.cancel();
    }
    async = executor.startExecution();
    async.subscribe(observer);
  }

  @Override
  protected void onStartLoading() {
    if (result != null) {
      deliverResult(result);
    }
    if (takeContentChanged() || async == null) {
      forceLoad();
    }
  }

  @Override
  public void deliverResult(final Result<D> data) {
    if (isReset()) {
      onReleaseData(data);
      return;
    }

    Result<D> oldData = result;
    result = data;

    if (isStarted()) {
      super.deliverResult(data);
    }

    if (oldData != null && data != oldData) {
      onReleaseData(oldData);
    }
  }

  @Override
  protected void onStopLoading() {
    if (async != null) {
      async.cancel();
      async = null;
    }
  }

  @Override
  protected void onReset() {
    if (result != null) {
      onReleaseData(result);
    }
  }

  private void onReleaseData(final Result<D> result) {
    if (result.data != null) {
      executor.releaseData(result.data);
    }
  }

  private void post(final Result<D> result) {
    if (Looper.getMainLooper() == Looper.myLooper()) {
      deliverResult(result);
    } else {
      MAIN_HANDLER.post(new Runnable() {
        @Override
        public void run() {
          deliverResult(result);
        }
      });
    }
  }

  /** Execution result. */
  static final class Result<D> {
    final D data;
    final Throwable error;

    Result(final D data, final Throwable error) {
      this.data = data;
      this.error = error;
    }

    @Override
    public boolean equals(final Object o) {
      if (!(o instanceof Result)) {
        return false;
      }
      @SuppressWarnings("unchecked")
      Result<D> r = (Result<D>) o;
      return data != null ? data.equals(r.data) : error.equals(r.error);
    }

    @Override
    public int hashCode() {
      return data != null ? data.hashCode() : error.hashCode();
    }
  }

}

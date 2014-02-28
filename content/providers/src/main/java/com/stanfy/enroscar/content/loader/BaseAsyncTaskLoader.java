package com.stanfy.enroscar.content.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;


/**
 * @author Olexandr Tereshchuk - <a href="http://stanfy.com">Stanfy</a>
 *
 * @param <T> data type
 */
public abstract class BaseAsyncTaskLoader<T> extends AsyncTaskLoader<T> {

  /** Logging tag. */
  private static final String TAG = "BaseAsyncTaskLoader";

  /** Result data. */
  private T result;

  public BaseAsyncTaskLoader(final Context ctx) {
    super(ctx);
  }

  @Override
  public void deliverResult(final T data) {
    if (isReset()) { return; }

    result = data;

    if (isStarted()) {
      super.deliverResult(data);
    }
  }

  @Override
  protected void onStartLoading() {
    final boolean contentChanges = takeContentChanged();
    if (!contentChanges && result != null) {
      deliverResult(result);
    }

    if (contentChanges || result == null) {
      forceLoad();
    }
  }

  @Override
  protected void onStopLoading() {
    cancelLoad();
  }

  @Override
  protected void onReset() {
    onStopLoading();
    result = null;
  }

}

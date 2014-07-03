package com.stanfy.enroscar.async.internal;

import android.os.AsyncTask;

import java.util.concurrent.Callable;

final class AsyncTaskWithDelegate<D> extends AsyncTask<Void, Void, D> {

  /** Delegate. */
  private final BaseAsync<D> async;

  /** Task. */
  private final Callable<D> task;

  /** Caught error. */
  private Exception error;

  protected AsyncTaskWithDelegate(final Callable<D> task, final BaseAsync<D> async) {
    this.async = async;
    this.task = task;
  }

  @Override
  protected final D doInBackground(Void... params) {
    try {
      return task.call();
    } catch (Exception e) {
      error = e;
      return null;
    }
  }

  @Override
  protected final void onPostExecute(final D d) {
    if (error != null) {
      async.postError(error);
    } else {
      async.postResult(d);
      async.postReset();
    }
  }

}

package com.stanfy.enroscar.content.async.internal;

import android.os.AsyncTask;

import com.stanfy.enroscar.content.async.Async;
import com.stanfy.enroscar.content.async.AsyncObserver;

import java.util.concurrent.Callable;

/**
 * Implementation of {@link Async} based on AsyncTask.
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
public final class TaskAsync<D> extends AsyncTask<Void, Void, D> implements Async<D> {

  /** Helper. */
  private final BaseAsync<D> baseAsync = new BaseAsync<D>() {
    @Override
    protected void onTrigger() {
      execute();
    }

    @Override
    protected void onCancel() {
      TaskAsync.this.cancel(true);
    }
  };

  /** Execution core. */
  private final Callable<D> task;

  /** Caught error. */
  private Exception error;

  public TaskAsync(final Callable<D> task) {
    this.task = task;
  }

  @Override
  public void subscribe(final AsyncObserver<D> observer) {
    baseAsync.subscribe(observer);
  }

  @Override
  public void cancel() {
    baseAsync.cancel();
  }

  @Override
  protected D doInBackground(final Void... params) {
    try {
      return task.call();
    } catch (Exception e) {
      error = e;
      return null;
    }
  }

  @Override
  protected void onPostExecute(final D d) {
    if (error != null) {
      baseAsync.postError(error);
    } else {
      baseAsync.postResult(d);
    }
  }

}

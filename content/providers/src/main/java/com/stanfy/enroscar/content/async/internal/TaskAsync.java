package com.stanfy.enroscar.content.async.internal;

import com.stanfy.enroscar.content.async.Async;
import com.stanfy.enroscar.content.async.AsyncObserver;

import java.util.concurrent.Callable;

/**
 * Implementation of {@link Async} based on AsyncTask.
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
public class TaskAsync<D, T extends Callable<D>> extends BaseAsync<D> {

  /** Task instance. */
  final T task;

  /** Android AsyncTask. */
  private AsyncTaskWithDelegate<D> asyncTask;

  public TaskAsync(final T task) {
    this.task = task;
  }

  @Override
  protected void onTrigger() {
    doCancel();
    asyncTask = new AsyncTaskWithDelegate<>(task, this);
    asyncTask.execute();
  }

  @Override
  protected void onCancel() {
    doCancel();
  }

  private void doCancel() {
    if (asyncTask != null) {
      asyncTask.cancel(true);
    }
  }

}

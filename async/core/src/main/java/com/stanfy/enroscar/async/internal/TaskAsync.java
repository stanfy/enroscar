package com.stanfy.enroscar.async.internal;

import android.annotation.SuppressLint;
import android.os.Build;

import com.stanfy.enroscar.async.Async;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

/**
 * Implementation of {@link Async} based on AsyncTask.
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
public class TaskAsync<D, T extends Callable<D>> extends BaseAsync<D> {

  /** Task instance. */
  final T task;

  /** Android AsyncTask. */
  private AsyncTaskWithDelegate<D> asyncTask;

  /** Executor for this task. */
  private final Executor executor;

  public TaskAsync(final T task, final Executor executor) {
    this.task = task;
    this.executor = executor;
  }

  protected Executor getExecutor() {
    return executor;
  }

  @Override
  public TaskAsync<D, T> replicate() {
    return new TaskAsync<>(task, executor);
  }

  @SuppressLint("NewApi")
  @Override
  protected void onTrigger() {
    doCancel();
    asyncTask = new AsyncTaskWithDelegate<>(task, this);
    if (executor == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
      asyncTask.execute();
    } else {
      asyncTask.executeOnExecutor(executor);
    }
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

  protected T getTask() {
    return task;
  }
}

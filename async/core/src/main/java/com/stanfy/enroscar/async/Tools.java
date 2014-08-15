package com.stanfy.enroscar.async;

import android.content.Context;

import com.stanfy.enroscar.async.content.CursorAsyncBuilder;
import com.stanfy.enroscar.async.internal.TaskAsync;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

/**
 * A set of utility methods that provide basic implementations of {@link Async}.
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
public final class Tools {

  private Tools() { }

  /**
   * Create an asynchronous operation that executes a provided task creating
   * an Android's {@link android.os.AsyncTask}.
   * @param task job to do
   * @param <D> result data type
   * @return implementation of {@link Async}, never {@code null}
   */
  public static <D> Async<D> async(final Callable<D> task) {
    return async(null, task);
  }

  /**
   * Create an asynchronous operation that executes a provided task creating
   * an Android's {@link android.os.AsyncTask} with a specified executor.
   * @param task job to do
   * @param executor executor used by AsyncTask
   * @param <D> result data type
   * @return implementations of {@link Async}, never {@code null}
   */
  public static <D> Async<D> async(final Executor executor, final Callable<D> task) {
    return new TaskAsync<>(task, executor);
  }

  /**
   * Create a builder for asynchronous operation that loads a {@link android.database.Cursor}
   * using Android's {@link android.content.ContentResolver}.
   * @param context context used to get a resolver instance
   * @return builder of {@code Cursor} load operation
   */
  public static CursorAsyncBuilder asyncCursor(final Context context) {
    return new CursorAsyncBuilder(context.getContentResolver());
  }

}

package com.stanfy.enroscar.async;

import android.content.Context;

import com.stanfy.enroscar.async.internal.TaskAsync;
import com.stanfy.enroscar.async.content.CursorAsyncBuilder;

import java.util.concurrent.Callable;

/**
 * Set of utility methods that provide basic implementations of {@link Async}.
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
public class Tools {

  /**
   * Create an asynchronous operations that executed a provided task creating
   * an Android's {@link android.os.AsyncTask}.
   * @param task job to do
   * @param <D> result data type
   * @return implementations of {@link Async}, never {@code null}
   */
  public static <D> Async<D> async(final Callable<D> task) {
    return new TaskAsync<>(task);
  }

  /**
   * Create a builder for asynchronous operation that loads a {@link android.database.Cursor}
   * using Android's {@link android.content.ContentResolver}.
   * @param context context used to get a resolver instance
   * @return builder of {@code Cursor} load operation
   */
  public static CursorAsyncBuilder asyncCursor(final Context context) {
    return new CursorAsyncBuilder(context);
  }

}

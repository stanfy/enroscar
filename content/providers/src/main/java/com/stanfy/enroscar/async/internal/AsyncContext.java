package com.stanfy.enroscar.async.internal;

import android.content.Context;

import com.stanfy.enroscar.async.Async;

/**
 * @param <D> data type
 */
class AsyncContext<D> {

  /** Application context. */
  final Context applicationContext;

  /** Delegate instance. */
  final Async<D> async;

  public AsyncContext(final Context context, final Async<D> async) {
    if (async == null) {
      throw new IllegalArgumentException(
          "Async operation instance is null. Have you implemented @Load or @Send method?"
      );
    }
    this.applicationContext = context.getApplicationContext();
    this.async = async;
  }

  protected void releaseData(final D data) {
    // TODO
  }

}

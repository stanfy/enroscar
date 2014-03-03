package com.stanfy.enroscar.content.async.internal;

import android.content.Context;

import com.stanfy.enroscar.content.async.Async;

/**
 * This class may be extended in generated code to provide implementation
 * of {@link #releaseData(Object)}.
 *
 * @param <D> data type
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
public class AsyncContext<D> {

  /** Our core. */
  final Async<D> async;

  /** Application context. */
  final Context applicationContext;

  public AsyncContext(final Async<D> async, final Context context) {
    this.async = async;
    this.applicationContext = context.getApplicationContext();
  }

  protected void releaseData(D data) { }

}

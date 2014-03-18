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
public abstract class AsyncContext<D> implements AsyncProvider<D> {

  /** Application context. */
  final Context applicationContext;

  public AsyncContext(final Context context) {
    this.applicationContext = context.getApplicationContext();
  }

  protected void releaseData(D data) { }

  /**
   * Holds a reference to an Async instance.
   * @param <D> data type.
   */
  public static class DirectContext<D> extends AsyncContext<D> {
    /** Async instance. */
    private final Async<D> async;

    public DirectContext(Async<D> async, Context context) {
      super(context);
      this.async = async;
    }

    @Override
    public Async<D> provideAsync() {
      return async;
    }
  }

  /**
   * Delegates Async providing to another object.
   * @param <D> data type.
   */
  public static class DelegatedContext<D> extends AsyncContext<D> {

    /** Delegate instance. */
    private AsyncProvider<D> delegate;

    public DelegatedContext(final Context context) {
      super(context);
    }

    public void setDelegate(final AsyncProvider<D> delegate) {
      this.delegate = delegate;
    }

    @Override
    public Async<D> provideAsync() {
      if (delegate == null) {
        throw new IllegalStateException("Delegate is not set");
      }
      return delegate.provideAsync();
    }

  }

}

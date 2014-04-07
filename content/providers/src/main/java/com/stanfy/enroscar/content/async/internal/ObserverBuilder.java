package com.stanfy.enroscar.content.async.internal;

import com.stanfy.enroscar.content.async.Action;
import com.stanfy.enroscar.content.async.AsyncError;
import com.stanfy.enroscar.content.async.AsyncObserver;

/**
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
public final class ObserverBuilder<D, T extends LoaderDescription> {

  /** Building target. */
  private final T description;

  /** Observer instance. */
  private final Observer<D> observer = new Observer<>();

  public ObserverBuilder(final int loaderId, final T description) {
    this.description = description;
    this.description.addObserver(loaderId, observer);
  }

  public ObserverBuilder<D, T> doOnResult(final Action<D> resultAction) {
    observer.resultAction = resultAction;
    return this;
  }

  public ObserverBuilder<D, T> doOnError(final Action<Throwable> errorAction) {
    observer.errorAction = errorAction;
    return this;
  }

  public T alsoWhen() {
    return description;
  }

  /**
   * {@link AsyncObserver} based on {@link Action}s.
   * @param <D> data type
   */
  private static class Observer<D> implements AsyncObserver<D> {

    /** On result. */
    Action<D> resultAction;
    /** On error. */
    Action<Throwable> errorAction;

    @Override
    public void onError(final Throwable e) {
      if (errorAction != null) {
        errorAction.act(e);
      } else {
        throw new AsyncError(e);
      }
    }

    @Override
    public void onResult(final D data) {
      if (resultAction != null) {
        resultAction.act(data);
      }
    }

  }

}

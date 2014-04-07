package com.stanfy.enroscar.content.async.internal;

import android.support.v4.app.LoaderManager;
import android.util.SparseArray;

import com.stanfy.enroscar.content.async.AsyncObserver;

import static com.stanfy.enroscar.content.async.internal.OperatorBase.OperatorContext;

/**
 * Describes what actions should be taken when some operation completes.
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
public abstract class LoaderDescription {

  /** Observers mapping. */
  private final SparseArray<AsyncObserver<?>> observers = new SparseArray<>();

  /** Operator context. */
  private final OperatorContext<?> operatorContext;

  protected LoaderDescription(final OperatorContext<?> operatorContext) {
    this.operatorContext = operatorContext;
  }

  void addObserver(final int loaderId, final AsyncObserver<?> observer) {
    observers.put(loaderId, observer);
  }

  public <D> LoaderManager.LoaderCallbacks<?> makeCallbacks(final int loaderId,
                                                            final AsyncProvider<D> provider,
                                                            final boolean destroyOnFinish) {
    @SuppressWarnings("unchecked")
    AsyncObserver<D> observer = (AsyncObserver<D>) observers.get(loaderId);
    return new ObserverCallbacks<>(provider, operatorContext, observer, destroyOnFinish);
  }

}

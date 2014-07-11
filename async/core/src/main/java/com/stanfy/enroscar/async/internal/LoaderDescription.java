package com.stanfy.enroscar.async.internal;

import android.support.v4.app.LoaderManager;
import android.util.SparseArray;

import com.stanfy.enroscar.async.AsyncObserver;

import static com.stanfy.enroscar.async.internal.OperatorBase.OperatorContext;

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

  <D> LoaderManager.LoaderCallbacks<?> makeCallbacks(final int loaderId,
                                                     final AsyncProvider<D> provider,
                                                     final boolean destroyOnFinish) {
    return new ObserverCallbacks<>(provider, operatorContext, this, loaderId, destroyOnFinish);
  }

  @SuppressWarnings("unchecked")
  <D> AsyncObserver<D> getObserver(final int id) {
    return (AsyncObserver<D>) observers.get(id);
  }

}

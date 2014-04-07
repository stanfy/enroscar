package com.stanfy.enroscar.content.async.internal;

import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.stanfy.enroscar.content.async.Async;
import com.stanfy.enroscar.content.async.AsyncObserver;
import com.stanfy.enroscar.content.async.internal.OperatorBase.OperatorContext;
import com.stanfy.enroscar.content.async.internal.WrapAsyncLoader.Result;

/**
 * Loader callbacks based on {@link Async} and {@link AsyncObserver}.
 * @author Roman Mazur
 */
final class ObserverCallbacks<D> implements LoaderManager.LoaderCallbacks<Result<D>> {

  /** Async provider (for lazy creation of Async in onCreateLoader). */
  private final AsyncProvider<D> provider;

  /** Operator context. */
  private final OperatorContext<?> operatorContext;

  /** Observer instance. */
  private final AsyncObserver<D> observer;

  /** Whether to destroy loader when it is finished. */
  private final boolean destroyOnFinish;

  public ObserverCallbacks(final AsyncProvider<D> asyncProvider,
                           final OperatorContext<?> operatorContext,
                           final AsyncObserver<D> observer,
                           final boolean destroyOnFinish) {
    this.provider = asyncProvider;
    this.operatorContext = operatorContext;
    this.observer = observer;
    this.destroyOnFinish = destroyOnFinish;
  }

  @Override
  public Loader<Result<D>> onCreateLoader(final int id, final Bundle args) {
    return new WrapAsyncLoader<>(new AsyncContext<>(
        operatorContext.context, provider.provideAsync()
    ));
  }

  @Override
  public void onLoadFinished(final Loader<Result<D>> loader, final Result<D> result) {
    try {
      if (result.error != null) {
        observer.onError(result.error);
      } else {
        observer.onResult(result.data);
      }
    } finally {
      if (destroyOnFinish) {
        operatorContext.getLoaderManager().destroyLoader(loader.getId());
      }
    }
  }

  @Override
  public void onLoaderReset(final Loader<Result<D>> loader) {
    // nothing
  }

}

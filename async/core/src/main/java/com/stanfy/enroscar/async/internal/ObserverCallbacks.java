package com.stanfy.enroscar.async.internal;

import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.stanfy.enroscar.async.Async;
import com.stanfy.enroscar.async.AsyncObserver;
import com.stanfy.enroscar.async.internal.OperatorBase.OperatorContext;
import com.stanfy.enroscar.async.internal.WrapAsyncLoader.Result;

/**
 * Loader callbacks based on {@link Async} and {@link AsyncObserver}.
 * @author Roman Mazur
 */
final class ObserverCallbacks<D> implements LoaderManager.LoaderCallbacks<Result<D>> {

  /** Async provider (for lazy creation of Async in onCreateLoader). */
  private final AsyncProvider<D> provider;

  /** Operator context. */
  private final OperatorContext<?> operatorContext;

  /** Observers collection. */
  private final LoaderDescription description;

  /** Associated loader ID. */
  private final int loaderId;

  /** Whether to destroy loader when it is finished. */
  private final boolean destroyOnFinish;

  public ObserverCallbacks(final AsyncProvider<D> asyncProvider,
                           final OperatorContext<?> operatorContext,
                           final LoaderDescription description,
                           final int loaderId,
                           final boolean destroyOnFinish) {
    this.provider = asyncProvider;
    this.operatorContext = operatorContext;
    this.description = description;
    this.loaderId = loaderId;
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
    AsyncObserver<D> observer = description.getObserver(loaderId);
    try {
      if (observer == null) {
        return;
      }
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
    AsyncObserver<D> observer = description.getObserver(loaderId);
    if (observer != null) {
      observer.onReset();
    }
  }

}

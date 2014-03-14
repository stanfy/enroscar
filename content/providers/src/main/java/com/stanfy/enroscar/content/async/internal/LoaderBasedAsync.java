package com.stanfy.enroscar.content.async.internal;

import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import static android.support.v4.app.LoaderManager.LoaderCallbacks;
import static com.stanfy.enroscar.content.async.internal.WrapAsyncLoader.Result;

/**
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
abstract class LoaderBasedAsync<D> extends BaseAsync<D> implements LoaderCallbacks<Result<D>> {
  /** Context. */
  final AsyncContext<D> asyncContext;
  /** Loader manager. */
  final LoaderManager loaderManager;
  /** Loader ID. */
  final int loaderId;

  public LoaderBasedAsync(final LoaderManager loaderManager, final AsyncContext<D> asyncContext,
                          final int loaderId) {
    this.loaderId = loaderId;
    this.loaderManager = loaderManager;
    this.asyncContext = asyncContext;
  }

  // === Async adapter ===

  @Override
  protected void onCancel() {
    Loader<Result<D>> loader = getLoader();
    if (loader != null) {
      loader.stopLoading();
    }
  }

  protected final Loader<Result<D>> getLoader() {
    return loaderManager.getLoader(loaderId);
  }

  protected final void initLoader() {
    loaderManager.initLoader(loaderId, null, this);
  }

  protected final void restartLoader() {
    loaderManager.restartLoader(loaderId, null, this);
  }

  protected final void destroyLoader() {
    loaderManager.destroyLoader(loaderId);
  }

  // === Loader callbacks ===

  @Override
  public Loader<Result<D>> onCreateLoader(final int id, final Bundle args) {
    if (id != loaderId) {
      throw new IllegalStateException(
          "Incorrect loader ID supplied " + id + ", my id " + loaderId + ", context " + asyncContext
      );
    }
    return new WrapAsyncLoader<>(asyncContext);
  }

  @Override
  public void onLoadFinished(final Loader<Result<D>> loader, final Result<D> result) {
    if (result.error != null) {
      postError(result.error);
    } else {
      postResult(result.data);
    }
  }

  @Override
  public void onLoaderReset(Loader<Result<D>> loader) {
    // nothing
  }
}

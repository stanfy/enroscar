package com.stanfy.enroscar.content.async.internal;

import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import static android.support.v4.app.LoaderManager.LoaderCallbacks;
import static com.stanfy.enroscar.content.async.internal.WrapAsyncLoader.Result;

/**
 * Wraps Async into Loader.
 */
public final class LoadAsync<D> extends BaseAsync<D> implements LoaderCallbacks<Result<D>> {

  /** Context. */
  private final AsyncContext<D> asyncContext;

  /** Loader manager. */
  private final LoaderManager loaderManager;

  /** Loader ID. */
  private final int loaderId;

  public LoadAsync(final LoaderManager loaderManager, final AsyncContext<D> asyncContext,
                   final int loaderId) {
    this.loaderManager = loaderManager;
    this.asyncContext = asyncContext;
    this.loaderId = loaderId;
  }

  // === Async adapter ===

  @Override
  protected void onTrigger() {
    loaderManager.initLoader(loaderId, null, this);
  }

  @Override
  protected void onCancel() {
    Loader<Result<D>> loader = loaderManager.getLoader(loaderId);
    if (loader != null) {
      loader.stopLoading();
    }
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

package com.stanfy.enroscar.content.async.internal;

import android.support.v4.app.LoaderManager;

/**
 * Wraps Async into Loader.
 * Used for implementing methods with {@link com.stanfy.enroscar.content.async.Load} annotation.
 */
public final class LoadAsync<D> extends LoaderBasedAsync<D> {

  public LoadAsync(final LoaderManager loaderManager, final AsyncContext<D> asyncContext,
                   final int loaderId) {
    super(loaderManager, asyncContext, loaderId);
  }

  @Override
  protected void onTrigger() {
    super.onTrigger();
    initLoader();
  }
}

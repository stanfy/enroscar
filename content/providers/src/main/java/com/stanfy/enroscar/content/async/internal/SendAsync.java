package com.stanfy.enroscar.content.async.internal;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import static com.stanfy.enroscar.content.async.internal.WrapAsyncLoader.Result;

/**
 * Uses Android loaders to wrap Async.
 * This class is used for implementing methods annotated with
 * {@link com.stanfy.enroscar.content.async.Send}.
 *
 * During its construction objects of this class check whether there is a loader created with
 * a corresponding ID. If such a loader exists callbacks are bound to it immediately with
 * {@link android.support.v4.app.LoaderManager#initLoader(int, android.os.Bundle, android.support.v4.app.LoaderManager.LoaderCallbacks)}.
 *
 * @param <D> data type
 *
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
public final class SendAsync<D> extends LoaderBasedAsync<D> {

  public SendAsync(final LoaderManager loaderManager, final AsyncContext<D> asyncContext,
                   final int loaderId) {
    super(loaderManager, asyncContext, loaderId);
    if (getLoader() != null) {
      initLoader();
    }
  }

  @Override
  protected void onTrigger() {
    super.onTrigger();
    restartLoader();
  }

  @Override
  public void onLoadFinished(final Loader<Result<D>> loader, final Result<D> result) {
    destroyLoader();
    super.onLoadFinished(loader, result);
  }

}

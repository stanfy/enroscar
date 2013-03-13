package com.stanfy.utils;

import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;

import com.stanfy.enroscar.rest.request.RequestBuilder;
import com.stanfy.enroscar.rest.response.ResponseData;

/**
 * Methods of activities and fragments that are oriented to operating with one request/model.
 * @param <MT> model type
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public interface OneRequestModelBehavior<MT> {

  /** Loader ID. */
  int MODEL_LOADER_ID = 1;

  /** @return new request builder instance used to get the model instance */
  RequestBuilder<MT> createRequestBuilder();

  /**
   * Process response results. Called in GUI thread.
   * @param data response data
   */
  void processSuccess(ResponseData<MT> data);

  void processError(ResponseData<MT> data);

  LoaderManager getSupportLoaderManager();

  /**
   * One request behavior helper.
   * @author Roman Mazur (Stanfy - http://www.stanfy.com)
   */
  public static final class OneRequestModelHelper<MT> implements LoaderCallbacks<ResponseData<MT>> {

    /** Behavior instance. */
    private final OneRequestModelBehavior<MT> behavior;

    public OneRequestModelHelper(final OneRequestModelBehavior<MT> behavior) { this.behavior = behavior; }

    public void fetch() {
      behavior.getSupportLoaderManager().initLoader(MODEL_LOADER_ID, null, this);
    }

    @Override
    public Loader<ResponseData<MT>> onCreateLoader(final int id, final Bundle args) {
      if (id == MODEL_LOADER_ID) {
        return behavior.createRequestBuilder().getLoader();
      }
      throw new IllegalArgumentException("Cannot create loader with id=" + id);
    }

    @Override
    public void onLoadFinished(final Loader<ResponseData<MT>> loader, final ResponseData<MT> data) {
      if (data.isSuccessful()) {
        behavior.processSuccess(data);
      } else {
        behavior.processError(data);
      }
    }

    @Override
    public void onLoaderReset(final Loader<ResponseData<MT>> data) {
      // nothing
    }

  }

}

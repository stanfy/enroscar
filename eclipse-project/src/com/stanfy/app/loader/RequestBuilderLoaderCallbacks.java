package com.stanfy.app.loader;

import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;

import com.stanfy.serverapi.response.ResponseData;

/**
 * Callbacks for {@link RequestBuilderLoader}.
 * @param <D> data type
 * @see LoaderCallbacks
 */
public interface RequestBuilderLoaderCallbacks<D> extends LoaderCallbacks<ResponseData<D>> {

  @Override
  RequestBuilderLoader<D> onCreateLoader(int id, Bundle args);

}

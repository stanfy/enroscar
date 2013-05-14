package com.stanfy.enroscar.content.loader;

import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;

/**
 * Callbacks for that deal with {@link ResponseData}.
 * @param <D> data type
 * @see LoaderCallbacks
 */
public interface ResponseDataLoaderCallbacks<D> extends LoaderCallbacks<ResponseData<D>> {

  @Override
  Loader<ResponseData<D>> onCreateLoader(int id, Bundle args);

}

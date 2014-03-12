package com.stanfy.enroscar.views.list.adapter;

import android.content.Context;

import com.stanfy.enroscar.content.loader.ResponseData;
import com.stanfy.enroscar.views.StateHelper;

import java.util.List;

/**
 * Adapter that consumes {@link ResponseData}. Assumes that a wrapped adapter is instance of ModelListAdapter.
 * @param <T> model type
 * @param <LT> list type
 */
public class ResponseDataLoaderAdapter<T, LT extends List<T>> extends LoaderAdapter<ResponseData<LT>> {

  public ResponseDataLoaderAdapter(final Context context, final ReplaceableListAdapter<T> coreAdapter) {
    super(context, coreAdapter);
  }

  public ResponseDataLoaderAdapter(final Context context, final ReplaceableListAdapter<T> coreAdapter, final StateHelper stateHelper) {
    super(context, coreAdapter, stateHelper);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected ReplaceableListAdapter<T> getCore() { return (ReplaceableListAdapter<T>) super.getCore(); }

  @Override
  protected boolean isResponseSuccessful(final ResponseData<LT> data) { return data.isSuccessful(); }
  @Override
  protected boolean isResponseEmpty(final ResponseData<LT> data) {
    final LT list = data.getModel();
    return list == null || list.isEmpty();
  }

  @Override
  protected void replaceDataInCore(final ResponseData<LT> data) {
    getCore().replace(data.getModel());
  }

}

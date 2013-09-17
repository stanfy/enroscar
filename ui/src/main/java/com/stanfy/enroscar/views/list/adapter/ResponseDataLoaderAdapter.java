package com.stanfy.enroscar.views.list.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.stanfy.enroscar.content.UniqueObject;
import com.stanfy.enroscar.content.loader.ResponseData;
import com.stanfy.enroscar.views.StateHelper;

/**
 * Adapter that consumes {@link ResponseData}. Assumes that a wrapped adapter is instance of ModelListAdapter.
 * @param <T> model type
 * @param <LT> list type
 */
public class ResponseDataLoaderAdapter<T, LT extends List<T>> extends LoaderAdapter<ResponseData<LT>> {

  public ResponseDataLoaderAdapter(final Context context, final RendererBasedAdapter<T> coreAdapter) {
    super(context, coreAdapter);
  }

  public ResponseDataLoaderAdapter(final Context context, final RendererBasedAdapter<T> coreAdapter, final StateHelper stateHelper) {
    super(context, coreAdapter, stateHelper);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected RendererBasedAdapter<T> getCore() { return (RendererBasedAdapter<T>) super.getCore(); }

  @Override
  protected boolean isResponseSuccessful(final ResponseData<LT> data) { return data.isSuccessful(); }
  @Override
  protected boolean isResponseEmpty(final ResponseData<LT> data) {
    final LT list = data.getModel();
    return list == null || list.isEmpty();
  }

  @Override
  protected void replaceDataInCore(final ResponseData<LT> data) {
    final RendererBasedAdapter<T> core = getCore();
    final List<T> list = data.getModel();
    if (list instanceof ArrayList) {
      core.replace((ArrayList<T>)list);
    } else {
      core.replace(new ArrayList<T>(list));
    }
  }

  @Override
  public void notifyDataSetChanged() {
    getCore().notifyDataSetChanged();
  }

}

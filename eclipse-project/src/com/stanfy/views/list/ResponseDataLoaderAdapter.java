package com.stanfy.views.list;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.stanfy.enroscar.content.UniqueObject;
import com.stanfy.serverapi.response.ResponseData;
import com.stanfy.views.StateHelper;

/**
 * Adapter that consumes {@link ResponseData}.
 * @param <T> model type
 * @param <LT> list type
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public class ResponseDataLoaderAdapter<T extends UniqueObject, LT extends List<T>> extends LoaderAdapter<ResponseData<LT>> {

  public ResponseDataLoaderAdapter(final Context context, final ModelListAdapter<T> coreAdapter) {
    super(context, coreAdapter);
  }

  public ResponseDataLoaderAdapter(final Context context, final ModelListAdapter<T> coreAdapter, final StateHelper stateHelper) {
    super(context, coreAdapter, stateHelper);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected ModelListAdapter<T> getCore() { return (ModelListAdapter<T>) super.getCore(); }

  @Override
  protected boolean isResponseSuccessful(final ResponseData<LT> data) { return data.isSuccessful(); }
  @Override
  protected boolean isResponseEmpty(final ResponseData<LT> data) {
    final LT list = data.getModel();
    return list == null || list.isEmpty();
  }

  @Override
  protected void replaceDataInCore(final ResponseData<LT> data) {
    final ModelListAdapter<T> core = getCore();
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

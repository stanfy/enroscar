package com.stanfy.app.fragments.list;

import android.content.Context;

import com.stanfy.app.Application;
import com.stanfy.app.BaseFragmentActivity;
import com.stanfy.content.UniqueObject;
import com.stanfy.serverapi.request.RequestBuilder;
import com.stanfy.views.list.ModelListAdapter.ElementRenderer;
import com.stanfy.views.list.OneTimeLoadAdapter;
import com.stanfy.views.list.RequestBuilderAdapter.RBAdapterCallback;

/**
 * Fragment that contains a list view with elements loaded with one request (no paging or 'load more' features).
 * @param <AT> application type
 * @param <MT> model type
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public abstract class OneTimeLoadListFragment<AT extends Application, MT extends UniqueObject> extends RequestBuilderListFragment<AT, MT, RequestBuilder, OneTimeLoadAdapter<MT>> {

  @Override
  protected OneTimeLoadAdapter<MT> createAdapter(final Context context, final ElementRenderer<MT> renderer) {
    return new OneTimeLoadAdapter<MT>(context, renderer, getRequestToken());
  }
  @Override
  protected RBAdapterCallback<MT, RequestBuilder, OneTimeLoadAdapter<MT>> createCallback(final BaseFragmentActivity<AT> owner) {
    return new RBAdapterCallback<MT, RequestBuilder, OneTimeLoadAdapter<MT>>();
  }

  @Override
  public OneTimeLoadAdapter<MT> getAdapter() { return (OneTimeLoadAdapter<MT>)super.getAdapter(); }

}

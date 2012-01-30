package com.stanfy.app.fragments.list;

import android.content.Context;

import com.stanfy.app.Application;
import com.stanfy.app.BaseFragmentActivity;
import com.stanfy.content.UniqueObject;
import com.stanfy.serverapi.request.ListRequestBuilder;
import com.stanfy.views.list.Fetcher;
import com.stanfy.views.list.Fetcher.FetcherRequestCallback;
import com.stanfy.views.list.ModelListAdapter.ElementRenderer;

/**
 * @param <AT> application type
 * @param <MT> model type
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public abstract class FetchingListFragment<AT extends Application, MT extends UniqueObject> extends RequestBuilderListFragment<AT, MT, ListRequestBuilder, Fetcher<MT>> {

  @Override
  protected FetcherRequestCallback<MT> createCallback(final BaseFragmentActivity<AT> owner) { return new FetcherRequestCallback<MT>(); }
  @Override
  public Fetcher<MT> createAdapter(final Context context, final ElementRenderer<MT> renderer) {
    return new Fetcher<MT>(context, renderer, getRequestToken(), isClientSideOffsetsMode());
  }

  @Override
  public Fetcher<MT> getAdapter() { return (Fetcher<MT>)super.getAdapter(); }

  /**
   * @return {@code true} of offset must be tracked on the client side or {@code false} in case it must be performed using
   * {@link com.stanfy.content.OffsetInfoTag#getCurrentOffset()}; default is true
   */
  protected boolean isClientSideOffsetsMode() { return true; }

}

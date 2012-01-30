package com.stanfy.views.list;

import android.content.Context;

import com.stanfy.content.UniqueObject;
import com.stanfy.serverapi.request.RequestBuilder;

/**
 * Adapter that loads data one time only.
 * @param <T> model type
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public class OneTimeLoadAdapter<T extends UniqueObject> extends RequestBuilderAdapter<T, RequestBuilder> {

  public OneTimeLoadAdapter(final Context context, final ModelListAdapter.ElementRenderer<T> renderer, final int token) {
    super(context, renderer, token);
  }
  
  public OneTimeLoadAdapter(final OneTimeLoadAdapter<T> adapter) {
    super(adapter);
  }

  @Override
  public boolean moreElementsAvailable() { return getCount() == 0; }

}

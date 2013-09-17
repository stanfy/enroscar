package com.stanfy.enroscar.views.list.adapter;

import android.content.Context;

import com.stanfy.enroscar.content.UniqueObject;

/**
 * @param <T> model type
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public class ModelListAdapter<T extends UniqueObject> extends RendererBasedAdapter<T> {

  public ModelListAdapter(final Context context, final ElementRenderer<T> renderer) {
    super(context, renderer);
  }

  public ModelListAdapter(final RendererBasedAdapter<T> adapter) {
    super(adapter);
  }

  @Override
  public long getItemId(final int position) {
    return getItem(position).getId();
  }

  @Override
  public boolean hasStableIds() { return true; }

}

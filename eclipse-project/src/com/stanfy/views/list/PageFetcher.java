package com.stanfy.views.list;

import android.content.Context;

import com.stanfy.content.UniqueObject;

/**
 * Page-oriented fetcher.
 * @param <T> model type
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class PageFetcher<T extends UniqueObject> extends Fetcher<T> {

  public PageFetcher(final Context context, final ElementRenderer<T> renderer, final int token) {
    super(context, renderer, token);
  }

  @Override
  protected void setupRequestBuilderOffset() {
    getRequestBuilder().setOffset(((FetcherState)state).offset++);
  }

}

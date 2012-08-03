package com.stanfy.views.list;

import android.widget.ListAdapter;

/**
 * Indicator interface for fetchable views like {@link FetchableListView} and {@link FetchableGridView}.
 * Contains nothing but common methods used in {@link com.stanfy.app.fragments.list.RequestBuilderListFragment}
 * @author Olexandr Tereshchuk (Stanfy - http://www.stanfy.com)
 */
public interface FetchableView {

  /**
   * @param listAdapter new adapter to set
   */
  void setAdapter(ListAdapter listAdapter);

  /** @return current adapter */
  ListAdapter getAdapter();

  /** @param i new selection */
  void setSelection(int i);

}

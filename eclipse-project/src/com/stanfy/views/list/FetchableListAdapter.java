package com.stanfy.views.list;

import android.widget.ListAdapter;

/**
 * Adapter with a possibility to load more records.
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public interface FetchableListAdapter extends ListAdapter {

  /**
   * Load more records.
   */
  void loadMoreRecords();

  /**
   * @return true if more elements are available
   */
  boolean moreElementsAvailable();

  /**
   * @return whether the fetcher is currently busy
   */
  boolean isBusy();

  /** @see android.widget.BaseAdapter#notifyDataSetChanged() */
  void notifyDataSetChanged();

}

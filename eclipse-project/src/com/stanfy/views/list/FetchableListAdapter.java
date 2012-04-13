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

  /**
   * Clear elements.
   */
  void clear();

  /**
   * Restore the adapter state.
   * @param state adapter state
   */
  void restoreState(final State state);

  /**
   * @return adapter state
   */
  State getState();

  /** @see android.widget.BaseAdapter#notifyDataSetChanged() */
  void notifyDataSetChanged();

  /** @see android.widget.BaseAdapter#notifyDataSetInvalidated() */
  void notifyDataSetInvalidated();

  /**
   * @author Roman Mazur (Stanfy - http://www.stanfy.com)
   */
  public static class State {
    /** State message. */
    String message;
    /** State level. */
    int level;
    /** Has more elements. */
    boolean hasMoreElements;

    /** @return the hasMoreElements */
    public boolean isHasMoreElements() { return hasMoreElements; }
    /** @param hasMoreElements the hasMoreElements to set */
    public void setHasMoreElements(final boolean hasMoreElements) { this.hasMoreElements = hasMoreElements; }
  }

}

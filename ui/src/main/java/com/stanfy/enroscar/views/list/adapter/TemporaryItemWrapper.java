package com.stanfy.enroscar.views.list.adapter;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.WrapperListAdapter;

/**
 * List adapter item that injects a temporary item either at the beginning or at the end.
 * @param <T> temporary item type
 */
// TODO: write tests
public abstract class TemporaryItemWrapper<T> extends BaseAdapter implements WrapperListAdapter {

  /** Core adapter. */
  private final ListAdapter core;

  /** Where to put the temp item (true means end). */
  private boolean atTheEnd = true;

  /** Temporary item. */
  private T tempItem;

  /** Core adapter observer. */
  private final DataSetObserver coreObserver = new DataSetObserver() {
    @Override
    public void onChanged() {
      notifyDataSetChanged();
    }
    @Override
    public void onInvalidated() {
      notifyDataSetInvalidated();
    }
  };

  public TemporaryItemWrapper(final ListAdapter core) {
    this.core = core;
    core.registerDataSetObserver(coreObserver);
  }

  public void setTempItem(final T tempItem) {
    this.tempItem = tempItem;
    notifyDataSetChanged();
  }

  public void setAtTheEnd(final boolean atTheEnd) {
    this.atTheEnd = atTheEnd;
    notifyDataSetChanged();
  }

  private boolean isTempItemPosition(final int position) {
    return tempItem != null && (!atTheEnd && position == 0 || atTheEnd && position == getCount() - 1);
  }

  private int convertPosition(final int position) {
    if (tempItem == null) { return position; }
    return atTheEnd ? position : position - 1;
  }

  @Override
  public ListAdapter getWrappedAdapter() { return core; }

  @Override
  public boolean areAllItemsEnabled() { return core.areAllItemsEnabled(); }

  @Override
  public boolean isEnabled(final int position) {
    if (isTempItemPosition(position)) {
      return isTempItemEnabled();
    }
    return core.isEnabled(convertPosition(position));
  }

  /**
   * @return true if temporary item should be enabled
   */
  protected boolean isTempItemEnabled() { return true; }

  @Override
  public int getCount() {
    return tempItem == null ? core.getCount() : core.getCount() + 1;
  }

  @Override
  public Object getItem(final int position) {
    if (isTempItemPosition(position)) {
      return tempItem;
    }
    return core.getItem(convertPosition(position));
  }

  @Override
  public long getItemId(final int position) {
    if (isTempItemPosition(position)) {
      return getTemporaryItemId(position);
    }
    return core.getItemId(convertPosition(position));
  }

  /**
   * @param position item position
   * @return temporary item ID
   * @see #getItemId(int)
   */
  protected abstract long getTemporaryItemId(final int position);

  @Override
  public boolean hasStableIds() { return core.hasStableIds(); }

  @Override
  public View getView(final int position, final View convertView, final ViewGroup parent) {
    if (isTempItemPosition(position)) {
      return getTempItemView(position, convertView, parent);
    }
    return core.getView(convertPosition(position), convertView, parent);
  }

  /**
   * @param position item position
   * @param convertView convert view instance
   * @param parent adapter view instance
   * @return view for a temporary object
   * @see #getView(int, android.view.View, android.view.ViewGroup)
   */
  protected abstract View getTempItemView(final int position, final View convertView, final ViewGroup parent);

  @Override
  public int getItemViewType(final int position) {
    if (isTempItemPosition(position)) {
      return getTempItemViewType(position);
    }
    return core.getItemViewType(convertPosition(position));
  }

  /**
   * @param position item position
   * @return view type for a temporary object
   * @see #getItemViewType(int)
   */
  protected int getTempItemViewType(final int position) {
    return ListAdapter.IGNORE_ITEM_VIEW_TYPE;
  }

  @Override
  public int getViewTypeCount() { return core.getViewTypeCount(); }

  @Override
  public boolean isEmpty() { return tempItem == null && core.isEmpty(); }
}

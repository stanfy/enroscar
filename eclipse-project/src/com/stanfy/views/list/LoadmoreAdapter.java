package com.stanfy.views.list;

import android.annotation.SuppressLint;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.WrapperListAdapter;

import com.stanfy.views.R;

/**
 * Adapter that shows load more footer.
 */
@SuppressLint("FieldGetter")
public class LoadmoreAdapter extends BaseAdapter implements WrapperListAdapter, Filterable {

  /** Main adapter. */
  final FetchableListAdapter core;

  /** Footer that indicates the loading process. */
  private View loadView;
  /** True if we need to display 'load more' footer. */
  private boolean loadFlag = false;
  /** Load tag. */
  private Object loadTag;

  /** Load view layout ID. */
  private int loadViewLayoutId = R.layout.footer_loading;
  /** Layout inflater instance. */
  private final LayoutInflater inflater;

  /** Core adapter observer. */
  private final DataSetObserver coreObserver = new DataSetObserver() {
    @Override
    public void onChanged() {
      setLoadFlag(false, false);
      notifyDataSetChanged();
    }
    @Override
    public void onInvalidated() {
      notifyDataSetInvalidated();
    }
  };

  public LoadmoreAdapter(final LayoutInflater inflater, final FetchableListAdapter core) {
    this.core = core;
    core.registerDataSetObserver(coreObserver);
    this.inflater = inflater;
  }

  public void setLoadViewLayoutId(final int loadViewLayoutId) {
    this.loadViewLayoutId = loadViewLayoutId;
  }
  
  /** @param loadView Load view. Layout params for the view should be set beforehand. */
  public void setLoadView(final View loadView) {
    this.loadView = loadView;
  }

  public void setLoadTag(final Object loadTag) {
    this.loadTag = loadTag;
  }

  public void setLoadFlag(final boolean loadFlag) {
    setLoadFlag(loadFlag, true);
  }
  public void setLoadFlag(final boolean loadFlag, final boolean notify) {
    final boolean prev = this.loadFlag;
    this.loadFlag = loadFlag;
    if (notify && prev != loadFlag) {
      notifyDataSetChanged();
    }
  }
  
  protected boolean isLoadViewPosition(final int position) {
    return loadFlag && position == core.getCount();
  }

  protected int transformPositionForCore(final int position) { return position; }

  @Override
  public boolean areAllItemsEnabled() { return false; }

  @Override
  public boolean isEnabled(final int position) {
    if (isLoadViewPosition(position)) { return true; }
    final boolean allEnabled = core.areAllItemsEnabled();
    if (allEnabled) { return true; }
    return core.isEnabled(transformPositionForCore(position));
  }

  @Override
  public int getCount() {
    int count = core.getCount();
    if (loadFlag) { ++count; }
    return count;
  }
  @Override
  public boolean isEmpty() {
    return !loadFlag && core.isEmpty();
  }

  @Override
  public Object getItem(final int position) {
    return isLoadViewPosition(position)
        ? loadTag
            : core.getItem(transformPositionForCore(position));
  }

  @Override
  public long getItemId(final int position) {
    return isLoadViewPosition(position)
        ? -1
            : core.getItemId(transformPositionForCore(position));
  }

  @Override
  public boolean hasStableIds() { return core.hasStableIds(); }

  @Override
  public int getViewTypeCount() { return core.getViewTypeCount(); }

  @Override
  public int getItemViewType(final int position) {
    return isLoadViewPosition(position)
        ? AdapterView.ITEM_VIEW_TYPE_IGNORE
            : core.getItemViewType(transformPositionForCore(position));
  }

  @Override
  public View getView(final int position, final View convertView, final ViewGroup parent) {
    return isLoadViewPosition(position)
        ? getLoadView(parent)
            : core.getView(position, convertView, parent);
  }
  
  private View getLoadView(final ViewGroup parent) {
    if (loadView == null) {
      loadView = inflater.inflate(loadViewLayoutId, parent, false);
    }
    return loadView;
  }

  @Override
  public Filter getFilter() {
    if (core instanceof Filterable) {
      return ((Filterable) core).getFilter();
    }
    return null;
  }

  @Override
  public FetchableListAdapter getWrappedAdapter() { return core; }

}

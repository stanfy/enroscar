package android.support.v4.widget;

import java.util.ArrayList;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListAdapter;
import android.widget.WrapperListAdapter;

/**
 * Copypaste from {@link android.widget.HeaderViewListAdapter}.
 */
class HeaderViewListAdapter implements WrapperListAdapter, Filterable {

  private final ListAdapter mAdapter;

  // These two ArrayList are assumed to NOT be null.
  // They are indeed created when declared in ListView and then shared.
  ArrayList<StaggeredGridView.FixedViewInfo> mHeaderViewInfos;
  ArrayList<StaggeredGridView.FixedViewInfo> mFooterViewInfos;

  // Used as a placeholder in case the provided info views are indeed null.
  // Currently only used by some CTS tests, which may be removed.
  static final ArrayList<StaggeredGridView.FixedViewInfo> EMPTY_INFO_LIST =
      new ArrayList<StaggeredGridView.FixedViewInfo>();

  boolean mAreAllFixedViewsSelectable;

  private final boolean mIsFilterable;

  public HeaderViewListAdapter(final ArrayList<StaggeredGridView.FixedViewInfo> headerViewInfos,
                               final ArrayList<StaggeredGridView.FixedViewInfo> footerViewInfos,
                               final ListAdapter adapter) {
      mAdapter = adapter;
      mIsFilterable = adapter instanceof Filterable;

      if (headerViewInfos == null) {
          mHeaderViewInfos = EMPTY_INFO_LIST;
      } else {
          mHeaderViewInfos = headerViewInfos;
      }

      if (footerViewInfos == null) {
          mFooterViewInfos = EMPTY_INFO_LIST;
      } else {
          mFooterViewInfos = footerViewInfos;
      }

      mAreAllFixedViewsSelectable =
              areAllListInfosSelectable(mHeaderViewInfos)
              && areAllListInfosSelectable(mFooterViewInfos);
  }

  public int getHeadersCount() {
      return mHeaderViewInfos.size();
  }

  public int getFootersCount() {
      return mFooterViewInfos.size();
  }

  public boolean isEmpty() {
      return mAdapter == null || mAdapter.isEmpty();
  }

  private boolean areAllListInfosSelectable(final ArrayList<StaggeredGridView.FixedViewInfo> infos) {
      if (infos != null) {
          for (StaggeredGridView.FixedViewInfo info : infos) {
              if (!info.isSelectable) {
                  return false;
              }
          }
      }
      return true;
  }

  public boolean removeHeader(final View v) {
      for (int i = 0; i < mHeaderViewInfos.size(); i++) {
          StaggeredGridView.FixedViewInfo info = mHeaderViewInfos.get(i);
          if (info.view == v) {
              mHeaderViewInfos.remove(i);

              mAreAllFixedViewsSelectable =
                      areAllListInfosSelectable(mHeaderViewInfos)
                      && areAllListInfosSelectable(mFooterViewInfos);

              return true;
          }
      }

      return false;
  }

  public boolean removeFooter(final View v) {
      for (int i = 0; i < mFooterViewInfos.size(); i++) {
          StaggeredGridView.FixedViewInfo info = mFooterViewInfos.get(i);
          if (info.view == v) {
              mFooterViewInfos.remove(i);

              mAreAllFixedViewsSelectable =
                      areAllListInfosSelectable(mHeaderViewInfos)
                      && areAllListInfosSelectable(mFooterViewInfos);

              return true;
          }
      }

      return false;
  }

  public void removeHeaders() {
    mHeaderViewInfos.clear();
    mAreAllFixedViewsSelectable =
        areAllListInfosSelectable(mHeaderViewInfos)
        && areAllListInfosSelectable(mFooterViewInfos);
  }

  public void removeFooters() {
    mFooterViewInfos.clear();
    mAreAllFixedViewsSelectable =
        areAllListInfosSelectable(mHeaderViewInfos)
        && areAllListInfosSelectable(mFooterViewInfos);
  }

  public int getCount() {
      if (mAdapter != null) {
          return getFootersCount() + getHeadersCount() + mAdapter.getCount();
      } else {
          return getFootersCount() + getHeadersCount();
      }
  }

  public boolean areAllItemsEnabled() {
      if (mAdapter != null) {
          return mAreAllFixedViewsSelectable && mAdapter.areAllItemsEnabled();
      } else {
          return true;
      }
  }

  public boolean isEnabled(final int position) {
      // Header (negative positions will throw an ArrayIndexOutOfBoundsException)
      int numHeaders = getHeadersCount();
      if (position < numHeaders) {
          return mHeaderViewInfos.get(position).isSelectable;
      }

      // Adapter
      final int adjPosition = position - numHeaders;
      int adapterCount = 0;
      if (mAdapter != null) {
          adapterCount = mAdapter.getCount();
          if (adjPosition < adapterCount) {
              return mAdapter.isEnabled(adjPosition);
          }
      }

      // Footer (off-limits positions will throw an ArrayIndexOutOfBoundsException)
      return mFooterViewInfos.get(adjPosition - adapterCount).isSelectable;
  }

  public Object getItem(final int position) {
      // Header (negative positions will throw an ArrayIndexOutOfBoundsException)
      int numHeaders = getHeadersCount();
      if (position < numHeaders) {
          return mHeaderViewInfos.get(position).data;
      }

      // Adapter
      final int adjPosition = position - numHeaders;
      int adapterCount = 0;
      if (mAdapter != null) {
          adapterCount = mAdapter.getCount();
          if (adjPosition < adapterCount) {
              return mAdapter.getItem(adjPosition);
          }
      }

      // Footer (off-limits positions will throw an ArrayIndexOutOfBoundsException)
      return mFooterViewInfos.get(adjPosition - adapterCount).data;
  }

  public long getItemId(final int position) {
      int numHeaders = getHeadersCount();
      if (mAdapter != null && position >= numHeaders) {
          int adjPosition = position - numHeaders;
          int adapterCount = mAdapter.getCount();
          if (adjPosition < adapterCount) {
              return mAdapter.getItemId(adjPosition);
          }
      }
      return -1;
  }

  public boolean hasStableIds() {
      if (mAdapter != null) {
          return mAdapter.hasStableIds();
      }
      return false;
  }

  public View getView(final int position, final View convertView, final ViewGroup parent) {
      // Header (negative positions will throw an ArrayIndexOutOfBoundsException)
      int numHeaders = getHeadersCount();
      if (position < numHeaders) {
          return mHeaderViewInfos.get(position).view;
      }

      // Adapter
      final int adjPosition = position - numHeaders;
      int adapterCount = 0;
      if (mAdapter != null) {
          adapterCount = mAdapter.getCount();
          if (adjPosition < adapterCount) {
              return mAdapter.getView(adjPosition, convertView, parent);
          }
      }

      // Footer (off-limits positions will throw an ArrayIndexOutOfBoundsException)
      return mFooterViewInfos.get(adjPosition - adapterCount).view;
  }

  public int getItemViewType(final int position) {
      int numHeaders = getHeadersCount();
      if (mAdapter != null && position >= numHeaders) {
          int adjPosition = position - numHeaders;
          int adapterCount = mAdapter.getCount();
          if (adjPosition < adapterCount) {
              return mAdapter.getItemViewType(adjPosition);
          }
      }

      return AdapterView.ITEM_VIEW_TYPE_HEADER_OR_FOOTER;
  }

  public int getViewTypeCount() {
      if (mAdapter != null) {
          return mAdapter.getViewTypeCount();
      }
      return 1;
  }

  public void registerDataSetObserver(final DataSetObserver observer) {
      if (mAdapter != null) {
          mAdapter.registerDataSetObserver(observer);
      }
  }

  public void unregisterDataSetObserver(final DataSetObserver observer) {
      if (mAdapter != null) {
          mAdapter.unregisterDataSetObserver(observer);
      }
  }

  public Filter getFilter() {
      if (mIsFilterable) {
          return ((Filterable) mAdapter).getFilter();
      }
      return null;
  }

  public ListAdapter getWrappedAdapter() {
      return mAdapter;
  }
}

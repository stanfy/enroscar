package com.stanfy.views.list;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.WrapperListAdapter;

import com.stanfy.DebugFlags;
import com.stanfy.views.R;
import com.stanfy.views.gallery.AdapterView;
import com.stanfy.views.list.FetchableListAdapter.OnListItemsLoadedListener;

/**
 * Implementation of {@link FetchableAbsListView} which uses {@link GridView} as main view.
 * @author Vladislav Lipskiy - Stanfy (http://www.stanfy.com)
 *
 */
public class FetchableGridView extends GridView implements OnScrollListener, FetchableView {

  /** Gap to load more elements. */
  public static final int LOAD_GAP_DEFAULT = 15;

  /** Debug flag. */
  private static final boolean DEBUG = DebugFlags.DEBUG_GUI;

  /** Adapter. */
  private LoadmoreAdapter adapter;

  /** Saved index. */
  private int savedFirstVisibleItem = 0;

  public FetchableGridView(final Context context) {
    this(context, null);
  }

  public FetchableGridView(final Context context, final AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public FetchableGridView(final Context context, final AttributeSet attrs, final int defStyle) {
    super(context, attrs, defStyle);
    init();
  }

  /**
   * Initialize the view.
   */
  private void init() {
    if (DEBUG) { Log.d(VIEW_LOG_TAG, "New fetchable grid view"); }
    setOnScrollListener(this);

    //   final LayoutInflater inflater = LayoutInflater.from(getContext());
    //   inflater.inflate(R.layout.fetchable_list_view, this, true);
    //
    //   listView = (ListView)findViewById(R.id.fetchable_list);
    //
    //   stateWindowHelper = new StateWindowHelper(findViewById(R.id.state_panel), listView);
    //
    //   setupListView();
  }

  protected int getLoadGap() { return LOAD_GAP_DEFAULT; }

  @Override
  public void setAdapter(final ListAdapter listAdapter) {
    if (!(listAdapter instanceof FetchableListAdapter)) { throw new IllegalArgumentException("adapter must implement " + FetchableListAdapter.class); }
    final FetchableListAdapter adapter = (FetchableListAdapter) listAdapter;
    this.adapter = adapter != null ? new LoadmoreAdapter(LayoutInflater.from(getContext()), adapter) : null;
    super.setAdapter(this.adapter);
  }

  @Override
  public final void onScrollStateChanged(final AbsListView view, final int scrollState) { /* empty */ }
  @Override
  public final void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount, final int totalItemCount) {
    final LoadmoreAdapter adapter = this.adapter;
    if (adapter == null) { return; }

    final int oldFirst = savedFirstVisibleItem;
    savedFirstVisibleItem = firstVisibleItem;
    if (oldFirst >= firstVisibleItem) { return; } // direction check: wait for top->down scroll

    if (adapter.isEmpty()) { return; }
    if (totalItemCount - firstVisibleItem - visibleItemCount > getLoadGap()) { return; }

    final FetchableListAdapter coreAdapter = adapter.getWrappedAdapter();
    if (!coreAdapter.moreElementsAvailable()) {
      adapter.setLoadFlag(false);
      return;
    }
    if (coreAdapter.isBusy()) { return; }

    if (DEBUG) { Log.d(VIEW_LOG_TAG, "Load more"); }
    adapter.setLoadFlag(true);
    coreAdapter.loadMoreRecords();
  }

  /**
   * Adapter that shows load more footer.
   */
  protected static class LoadmoreAdapter implements WrapperListAdapter, Filterable {

    /** Main adapter. */
    private final FetchableListAdapter core;

    /** Footer that indicates the loading process. */
    private final View loadView;
    /** True if we need to display 'load more' footer. */
    private boolean loadFlag = false;
    /** Load tag. */
    private Object loadTag;

    /** Load listener. */
    private final OnListItemsLoadedListener loadListener = new OnListItemsLoadedListener() {
      @Override
      public void onListItemsLoaded() {
        setLoadFlag(false);
      }
    };

    public LoadmoreAdapter(final LayoutInflater inflater, final FetchableListAdapter core) {
      this.core = core;
      this.loadView = createLoadView(inflater);
      core.setOnListItemsLoadedListener(loadListener);
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
        core.notifyDataSetChanged();
      }
    }

    protected View createLoadView(final LayoutInflater inflater) {
      final View result = inflater.inflate(R.layout.footer_loading, null);
      final ListView.LayoutParams params = new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, ListView.LayoutParams.WRAP_CONTENT);
      result.setLayoutParams(params);
      return result;
    }

    protected boolean isLoadFooterPosition(final int position) {
      return loadFlag && position == core.getCount();
    }

    protected int transformPositionForCore(final int position) { return position; }

    @Override
    public void registerDataSetObserver(final DataSetObserver observer) {
      core.registerDataSetObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(final DataSetObserver observer) {
      core.unregisterDataSetObserver(observer);
    }

    @Override
    public boolean areAllItemsEnabled() { return false; }

    @Override
    public boolean isEnabled(final int position) {
      if (isLoadFooterPosition(position)) { return true; }
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
      return isLoadFooterPosition(position)
          ? loadTag
              : core.getItem(transformPositionForCore(position));
    }

    @Override
    public long getItemId(final int position) {
      return isLoadFooterPosition(position)
          ? -1
              : core.getItemId(transformPositionForCore(position));
    }

    @Override
    public boolean hasStableIds() { return core.hasStableIds(); }

    @Override
    public int getViewTypeCount() { return core.getViewTypeCount(); }

    @Override
    public int getItemViewType(final int position) {
      return isLoadFooterPosition(position)
          ? AdapterView.ITEM_VIEW_TYPE_IGNORE
              : core.getItemViewType(transformPositionForCore(position));
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
      return isLoadFooterPosition(position)
          ? loadView
              : core.getView(position, convertView, parent);
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

}

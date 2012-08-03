package com.stanfy.views.list;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.GridView;
import android.widget.ListAdapter;
import com.stanfy.DebugFlags;
import com.stanfy.views.list.FetchableListView.LoadmoreAdapter;

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

}

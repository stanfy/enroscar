package com.stanfy.views.list;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListAdapter;

import com.stanfy.DebugFlags;
import com.stanfy.views.R;

/**
 * List view that can call to load more records on scrolling.
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public class FetchableListView extends ListView implements OnScrollListener, FetchableView {

  /** Gap to load more elements. */
  public static final int LOAD_GAP_DEFAULT = 5;

  /** Debug flag. */
  private static final boolean DEBUG = DebugFlags.DEBUG_GUI;

  /** Adapter. */
  private LoadmoreAdapter adapter;

  /** Saved index. */
  private int savedFirstVisibleItem = 0;

  /** Footer layout ID. */
  private int footerLayoutId = R.layout.footer_loading;

  public FetchableListView(final Context context) {
    this(context, null);
  }
  public FetchableListView(final Context context, final AttributeSet attrs) {
    super(context, attrs);
    init();
  }
  public FetchableListView(final Context context, final AttributeSet attrs, final int defStyle) {
    super(context, attrs, defStyle);
    init();
  }

  /**
   * Initialize the view.
   */
  private void init() {
    if (DEBUG) { Log.d(VIEW_LOG_TAG, "New fetchable list view"); }
    setOnScrollListener(this);
  }

  protected int getLoadGap() { return LOAD_GAP_DEFAULT; }

  protected LoadmoreAdapter createLoadmoreAdapter(final FetchableListAdapter core) {
    LoadmoreAdapter adapter = new LoadmoreAdapter(LayoutInflater.from(getContext()), core);
    adapter.setFooterLayoutId(footerLayoutId);
    return adapter;
  }

  public void setFooterLayoutId(final int footerLayoutId) {
    this.footerLayoutId = footerLayoutId;
  }

  @Override
  public void setAdapter(final ListAdapter listAdapter) {
    if (!(listAdapter instanceof FetchableListAdapter)) { throw new IllegalArgumentException("adapter must implement " + FetchableListAdapter.class); }
    final FetchableListAdapter adapter = (FetchableListAdapter) listAdapter;
    this.adapter = adapter != null ? createLoadmoreAdapter(adapter) : null;
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

    final FetchableListAdapter coreAdapter = adapter.core;
    if (!coreAdapter.moreElementsAvailable()) {
      adapter.setLoadFlag(false);
      return;
    }
    if (coreAdapter.isBusy()) { return; }

    if (DEBUG) { Log.d(VIEW_LOG_TAG, "Load more"); }
    adapter.setLoadFlag(true);
    coreAdapter.loadMoreRecords();
  }

  @Override
  public boolean performItemClick(final View view, final int position, final long id) {
    // do not allow click on footer (footer is enabled in order to maintain dividers)
    if (adapter != null && adapter.isLoadFooterPosition(position)) { return false; }
    return super.performItemClick(view, position, id);
  }

}

package com.stanfy.views.list;

import android.content.Context;
import android.support.v4.widget.StaggeredGridView;
import android.support.v4.widget.StaggeredGridView.OnScrollListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.ListAdapter;

import com.stanfy.DebugFlags;
import com.stanfy.views.R;
import com.stanfy.views.StateHelper;

/**
 * Beta.
 * https://plus.google.com/u/0/103829716466878605055/posts
 * @author Vladislav Lipskiy - Stanfy (http://www.stanfy.com).
 */
public class FetchableStaggeredGridView extends GUICrucialStaggeredGridView implements FetchableView, OnScrollListener {

  /** Gap to load more elements. */
  public static final int LOAD_GAP_DEFAULT = 15;

  /** Debug flag. */
  private static final boolean DEBUG = DebugFlags.DEBUG_GUI;

  /** Adapter. */
  private LoadmoreAdapter adapter;

  /** Saved index. */
  private int savedFirstVisibleItem = 0;

  /** Load view layout ID. */
  private int loadViewLayoutId = R.layout.loadview_grid;

  public FetchableStaggeredGridView(final Context context) {
    super(context);
    init();
  }

  public FetchableStaggeredGridView(final Context context, final AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public FetchableStaggeredGridView(final Context context, final AttributeSet attrs,
      final int defStyle) {
    super(context, attrs, defStyle);
    init();
  }

  /**
   * Initialize the view.
   */
  private void init() {
    if (DEBUG) { Log.d(VIEW_LOG_TAG, "New FetchableStaggeredGridView"); }
    setOnScrollListener(this);
  }

  protected int getLoadGap() { return LOAD_GAP_DEFAULT; }

  @Override
  public void setSelection(final int i) {
    // TODO Auto-generated method stub
  }

  @Override
  public void setAdapter(final ListAdapter listAdapter) {
    if (!(listAdapter instanceof FetchableListAdapter)) { throw new IllegalArgumentException("adapter must implement " + FetchableListAdapter.class); }
    final FetchableListAdapter adapter = (FetchableListAdapter) listAdapter;
    this.adapter = adapter != null ? createLoadmoreAdapter(adapter) : null;
    super.setAdapter(this.adapter);
  }

  protected LoadmoreAdapter createLoadmoreAdapter(final FetchableListAdapter core) {
    LoadmoreAdapter adapter = new LoadmoreAdapter(LayoutInflater.from(getContext()), core);
    adapter.setLoadViewLayoutId(loadViewLayoutId);
    return adapter;
  }
  
  public void setLoadViewLayoutId(final int loadViewLayoutId) {
    this.loadViewLayoutId = loadViewLayoutId;
  }

  @Override
  public final void onScroll(final StaggeredGridView view, final int firstVisibleItem, final int visibleItemCount, final int totalItemCount) {
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
  public void onScrollStateChanged(final StaggeredGridView view, final int scrollState) {
    /* empty */
  }

  @Override
  public boolean onTouchEvent(final MotionEvent ev) {
    if (adapter != null && adapter.getWrappedAdapter() instanceof LoaderAdapter<?>) {
      final LoaderAdapter<?> a = (LoaderAdapter<?>) adapter.getWrappedAdapter();
      if (a.getState() != StateHelper.STATE_NORMAL) { return false; }
    }
    return super.onTouchEvent(ev);
  }

  @Override
  public boolean onInterceptTouchEvent(final MotionEvent ev) {
    if (adapter != null && adapter.getWrappedAdapter() instanceof LoaderAdapter<?>) {
      final LoaderAdapter<?> a = (LoaderAdapter<?>) adapter.getWrappedAdapter();
      if (a.getState() != StateHelper.STATE_NORMAL) { return false; }
    }
    return super.onInterceptTouchEvent(ev);
  }


}


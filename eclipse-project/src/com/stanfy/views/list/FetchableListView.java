package com.stanfy.views.list;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.stanfy.DebugFlags;
import com.stanfy.views.R;
import com.stanfy.views.StateWindowHelper;

/**
 * List view that can call to load more records on scrolling.
 * It contains a {@link ListView} rather than subclasses it.
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public class FetchableListView extends FrameLayout implements OnScrollListener {

  /** Gap to load more elements. */
  public static final int LOAD_GAP = 5;

  /** Debug flag. */
  private static final boolean DEBUG = DebugFlags.DEBUG_GUI;

  /** List view. */
  private ListView listView;

  /** Footer that indicates the loading process. */
  private View footer;

  /** State window helper. */
  private StateWindowHelper stateWindowHelper;

  /** Footer flag. */
  private volatile boolean footerShown = false;

  /** Adapter. */
  private FetchableListAdapter adapter;

  /** Listener. */
  private OnItemsLoadedListener onItemsLoadedListener;

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

  public void setOnItemClickListener(final OnItemClickListener clickListener) {
    listView.setOnItemClickListener(clickListener);
  }

  public StateWindowHelper getStateWindowHelper() { return stateWindowHelper; }

  /**
   * Initialize the view.
   */
  private void init() {
    if (DEBUG) { Log.d(VIEW_LOG_TAG, "New fetchable list view"); }

    final LayoutInflater inflater = LayoutInflater.from(getContext());
    inflater.inflate(R.layout.fetchable_list_view, this, true);
    footer = inflater.inflate(R.layout.footer_loading, this, false);

    listView = (ListView)findViewById(R.id.fetchable_list);

    stateWindowHelper = new StateWindowHelper(findViewById(R.id.state_panel), listView);

    listView.setOnScrollListener(this);

    final ListView.LayoutParams params = new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, ListView.LayoutParams.WRAP_CONTENT);
    footer.setLayoutParams(params);

    setupListView();
  }

  public FetchableListAdapter getAdapter() { return adapter; }

  /** @return the listView */
  public ListView getCoreListView() { return listView; }

  /**
   * Set a fetchable adapter.
   * @see android.widget.ListView#setAdapter(android.widget.ListAdapter)
   * @param adapter adapter instance.
   */
  public void setAdapter(final FetchableListAdapter adapter) {
    this.adapter = adapter;
    if (adapter != null) { listView.addFooterView(footer); }
    listView.setAdapter(adapter);
    if (adapter != null) { listView.removeFooterView(footer); }
  }

  @Override
  public final void onScrollStateChanged(final AbsListView view, final int scrollState) { /* empty */ }
  @Override
  public final void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount, final int totalItemCount) {
    if (totalItemCount != visibleItemCount && LOAD_GAP < totalItemCount - firstVisibleItem - visibleItemCount) { return; }
    if (DEBUG) { Log.d(VIEW_LOG_TAG, "Scroll fetchable list"); }
    final FetchableListAdapter adapter = getAdapter();
    if (adapter == null || !adapter.moreElementsAvailable() || adapter.isBusy()) { return; }
    if (adapter.getCount() == 0) {
      setupWait();
    } else {
      listView.addFooterView(footer, null, false);
      footerShown = true;
    }
    if (DEBUG) { Log.d(VIEW_LOG_TAG, "Load more"); }
    adapter.loadMoreRecords();
  }

  public void setOnItemsLoadedListener(final OnItemsLoadedListener onItemsLoadedListener) {
    this.onItemsLoadedListener = onItemsLoadedListener;
  }

  /**
   * Restore list view state.
   * @param hasMoreElements has more elements flag from adapter
   * @param stateLevel saved state level
   * @param message saved user message
   */
  public void restoreState(final FetchableListAdapter.State state) {
    adapter.restoreState(state);
    if (adapter.getCount() == 0 && !state.hasMoreElements) { setupMessageView(state.level, state.message); }
  }

  /**
   * Restart observation.
   */
  public void restartObserve() {
    setupWait();
    final FetchableListAdapter adapter = getAdapter();
    if (adapter.getCount() > 0) { adapter.clear(); }
    adapter.loadMoreRecords();
  }

  public void setupWait() {
    stateWindowHelper.showProgress();
  }
  public void setupListView() {
    stateWindowHelper.showMain();
    if (footerShown) {
      footerShown = false;
      listView.removeFooterView(footer);
    }
  }
  public void setupMessageView(final int state, final String message) {
    if (DEBUG) { Log.d(VIEW_LOG_TAG, "Setup messsage view " + state + " / " + message); }
    stateWindowHelper.resolveState(state, message);
  }

  public void itemsLoaded(final boolean empty) {
    if (onItemsLoadedListener != null) {
      onItemsLoadedListener.onItemsLoaded(empty);
    }
  }

  /**
   * @author Roman Mazur (Stanfy - http://www.stanfy.com)
   */
  public interface OnItemsLoadedListener {
    /**
     * Called when new items are loaded.
     * @param empty whether elements list is empty
     */
    void onItemsLoaded(final boolean empty);
  }

}

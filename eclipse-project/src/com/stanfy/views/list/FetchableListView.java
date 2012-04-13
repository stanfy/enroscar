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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.WrapperListAdapter;

import com.stanfy.DebugFlags;
import com.stanfy.views.R;
import com.stanfy.views.StateWindowHelper;
import com.stanfy.views.gallery.AdapterView;

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

  /** State window helper. */
  private StateWindowHelper stateWindowHelper;

  /** Adapter. */
  private LoadmoreAdapter adapter;

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

    listView = (ListView)findViewById(R.id.fetchable_list);

    stateWindowHelper = new StateWindowHelper(findViewById(R.id.state_panel), listView);

    listView.setOnScrollListener(this);

    setupListView();
  }

  public FetchableListAdapter getAdapter() { return adapter.getWrappedAdapter(); }

  /** @return the listView */
  public ListView getCoreListView() { return listView; }

  /**
   * Set a fetchable adapter.
   * @see android.widget.ListView#setAdapter(android.widget.ListAdapter)
   * @param adapter adapter instance.
   */
  public void setAdapter(final FetchableListAdapter adapter) {
    this.adapter = adapter != null ? new LoadmoreAdapter(LayoutInflater.from(getContext()), adapter) : null;
    listView.setAdapter(this.adapter);
  }

  @Override
  public final void onScrollStateChanged(final AbsListView view, final int scrollState) { /* empty */ }
  @Override
  public final void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount, final int totalItemCount) {
    if (totalItemCount != visibleItemCount && LOAD_GAP < totalItemCount - firstVisibleItem - visibleItemCount) { return; }
    if (DEBUG) { Log.d(VIEW_LOG_TAG, "Scroll fetchable list"); }
    final LoadmoreAdapter adapter = this.adapter;
    if (adapter == null || !adapter.moreElementsAvailable() || adapter.isBusy()) { return; }
    if (adapter.getCount() == 0) {
      setupWait();
    } else {
      adapter.setLoadFlag(true);
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
    stateWindowHelper.showMain(true);
    if (adapter != null) {
      adapter.setLoadFlag(false);
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

  /**
   * Adapter that shows load more footer.
   */
  protected static class LoadmoreAdapter implements WrapperListAdapter, FetchableListAdapter, Filterable {

    /** Main adapter. */
    private final FetchableListAdapter core;

    /** Footer that indicates the loading process. */
    private final View loadView;
    /** True if we need to display 'load more' footer. */
    private boolean loadFlag = false;
    /** Load tag. */
    private Object loadTag;

    public LoadmoreAdapter(final LayoutInflater inflater, final FetchableListAdapter core) {
      this.core = core;
      this.loadView = createLoadView(inflater);
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
    public void loadMoreRecords() { core.loadMoreRecords(); }

    @Override
    public boolean moreElementsAvailable() { return core.moreElementsAvailable(); }

    @Override
    public boolean isBusy() { return core.isBusy(); }

    @Override
    public void clear() { core.clear(); }

    @Override
    public void restoreState(final State state) { core.restoreState(state); }
    @Override
    public State getState() { return core.getState(); }

    @Override
    public Filter getFilter() {
      if (core instanceof Filterable) {
        return ((Filterable) core).getFilter();
      }
      return null;
    }

    @Override
    public FetchableListAdapter getWrappedAdapter() { return core; }

    @Override
    public void notifyDataSetChanged() {
      core.notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetInvalidated() {
      core.notifyDataSetInvalidated();
    }

  }

}

package com.stanfy.enroscar.views.list.adapter;

import static com.stanfy.enroscar.views.StateHelper.STATE_EMPTY;
import static com.stanfy.enroscar.views.StateHelper.STATE_LOADING;
import static com.stanfy.enroscar.views.StateHelper.STATE_MESSAGE;
import static com.stanfy.enroscar.views.StateHelper.STATE_NORMAL;

import java.util.ArrayList;

import android.content.Context;
import android.database.DataSetObserver;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.WrapperListAdapter;

import com.stanfy.enroscar.activities.CrucialGUIOperationManager.CrucialGUIOperationListener;
import com.stanfy.enroscar.content.loader.LoadmoreLoader;
import com.stanfy.enroscar.views.StateHelper;

/**
 * Adapter that communicates with {@link Loader}.
 * @param <MT> container model type
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public abstract class LoaderAdapter<MT> extends BaseAdapter implements WrapperListAdapter, FetchableListAdapter, CrucialGUIOperationListener {

  /** Logging tag. */
  private static final String TAG = "LoaderAdapter";

  /** Debug flag. */
  private static final boolean DEBUG = false;

  /** Context. */
  private final Context context;

  /** Core adapter. */
  private final ListAdapter core;

  /** Loader instance. */
  private Loader<MT> loader;

  /** Pending operations. */
  private final ArrayList<Runnable> afterAnimationOperations = new ArrayList<Runnable>(3);
  /** Animations running flag. */
  private boolean animationsRunning = false;

  /** State helper. */
  private final StateHelper stateHelper;
  /** State code. */
  private int state = STATE_LOADING;

  /** Last response data. */
  private MT lastResponseData;

  /** Used to sync states with core. */
  private final DataSetObserver observer;

  public LoaderAdapter(final Context context, final ListAdapter coreAdapter) {
    this(context, coreAdapter, new StateHelper());
  }
  public LoaderAdapter(final Context context, final ListAdapter coreAdapter, final StateHelper stateHelper) {
    this.stateHelper = stateHelper;
    this.context = context;
    this.core = coreAdapter;
    this.observer = new Observer();
    this.core.registerDataSetObserver(observer);
  }

  protected void setState(final int state) { this.state = state; }
  protected MT getLastResponseData() { return lastResponseData; }
  protected ListAdapter getCore() { return core; }
  public StateHelper getStateHelper() { return stateHelper; }
  public int getState() { return state; }

  // =========================== Wrapping ===========================

  @Override
  public boolean areAllItemsEnabled() {
    if (state != STATE_NORMAL) { return false; }
    return core.areAllItemsEnabled();
  }

  @Override
  public boolean isEnabled(final int position) {
    if (state != STATE_NORMAL) { return false; }
    return core.isEnabled(position);
  }

  @Override
  public int getCount() {
    if (state == STATE_NORMAL) { return core.getCount(); }
    return stateHelper.hasState(state) ? 1 : 0;
  }

  @Override
  public Object getItem(final int position) {
    if (state != STATE_NORMAL) { return null; }
    return core.getItem(position);
  }

  @Override
  public long getItemId(final int position) {
    if (state != STATE_NORMAL) { return -1; }
    return core.getItemId(position);
  }

  @Override
  public boolean hasStableIds() { return core.hasStableIds(); }

  @Override
  public View getView(final int position, final View convertView, final ViewGroup parent) {
    if (state == STATE_NORMAL) {
      return core.getView(position, convertView, parent);
    }
    return stateHelper.getCustomStateView(state, context, lastResponseData, parent);
  }

  @Override
  public int getItemViewType(final int position) {
    return state == STATE_NORMAL ? core.getItemViewType(position) : AdapterView.ITEM_VIEW_TYPE_IGNORE;
  }

  @Override
  public int getViewTypeCount() { return core.getViewTypeCount(); }

  @Override
  public boolean isEmpty() { return getCount() == 0; }

  @Override
  public void loadMoreRecords() {
    if (loader instanceof LoadmoreLoader) {
      if (DEBUG) { Log.d(TAG, "Force loader to fetch more elements"); }
      ((LoadmoreLoader) loader).forceLoadMore();
    }
  }

  @Override
  public boolean moreElementsAvailable() {
    if (state != STATE_NORMAL) { return false; }
    return loader instanceof LoadmoreLoader
        ? ((LoadmoreLoader) loader).moreElementsAvailable()
            : false;
  }

  @Override
  public boolean isBusy() {
    final boolean loaderBusy = loader instanceof LoadmoreLoader
        ? ((LoadmoreLoader) loader).isBusy()
        : false;
    return loaderBusy || !afterAnimationOperations.isEmpty();
  }

  @Override
  public final ListAdapter getWrappedAdapter() { return core; }

  // =========================== Crucial animations ===========================
  @Override
  public void onStartCrucialGUIOperation() {
    animationsRunning = true;
  }
  @Override
  public void onFinishCrucialGUIOperation() {
    animationsRunning = false;
    final ArrayList<Runnable> afterAnimationOperations = this.afterAnimationOperations;
    final int count = afterAnimationOperations.size();
    for (int i = 0; i < count; i++) {
      afterAnimationOperations.get(i).run();
    }
    afterAnimationOperations.clear();
  }

  // ================================= Loader =================================

  public void onLoadStart() {
    state = STATE_LOADING;
    notifyDataSetChanged();
  }

  public void onLoaderReset(final Loader<MT> loader) {
    if (DEBUG) { Log.i(TAG, "Loader <" + loader + "> reset; adapter: <" + this + ">"); }
    lastResponseData = null;
  }

  public void onLoadFinished(final Loader<MT> loader, final MT data) {
    this.loader = loader;
    lastResponseData = data;
    if (isResponseSuccessful(data)) {

      if (isResponseEmpty(data)) {
        onDataEmpty(data);
      } else {
        onDataSuccess(data);
      }

    } else {
      onDataError(data);
    }
  }

  protected abstract boolean isResponseSuccessful(final MT data);
  protected abstract boolean isResponseEmpty(final MT data);
  protected abstract void replaceDataInCore(final MT data);

  final void addNewData(final MT data) {
    state = STATE_NORMAL;
    // this will cause notifyDataSetChanged
    replaceDataInCore(data);
  }

  protected void onDataSuccess(final MT data) {
    if (animationsRunning) {
      afterAnimationOperations.add(new AddDataRunnable(data));
    } else {
      addNewData(data);
    }
  }

  protected void onDataError(final MT data) {
    state = STATE_MESSAGE;
    notifyDataSetChanged();
  }

  protected void onDataEmpty(final MT data) {
    if (core.isEmpty()) {
      state = STATE_EMPTY;
    }
    notifyDataSetChanged();
  }

  /** Add data runnable. */
  private final class AddDataRunnable implements Runnable {

    /** Data. */
    private final MT data;

    public AddDataRunnable(final MT data) { this.data = data; }

    @Override
    public void run() { addNewData(data); }
  }

  /** Data set observer. */
  private final class Observer extends DataSetObserver {

    @Override
    public void onChanged() {
      if (state == STATE_EMPTY && core.isEmpty()) {
        return;
      }
      if (state == STATE_NORMAL && core.isEmpty()) {
        state = STATE_EMPTY;
      } else if (!core.isEmpty()) {
        state = STATE_NORMAL;
      }
      notifyDataSetChanged();
    }

  }

}

package com.stanfy.app.fragments.list;

import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stanfy.DebugFlags;
import com.stanfy.app.Application;
import com.stanfy.app.BaseFragment;
import com.stanfy.app.BaseFragmentActivity;
import com.stanfy.content.UniqueObject;
import com.stanfy.serverapi.request.Operation;
import com.stanfy.serverapi.request.RequestBuilder;
import com.stanfy.utils.AppUtils;
import com.stanfy.views.list.EmptyAdapter;
import com.stanfy.views.list.FetchableListAdapter;
import com.stanfy.views.list.FetchableListView;
import com.stanfy.views.list.FetchableListView.OnItemsLoadedListener;
import com.stanfy.views.list.ModelListAdapter.ElementRenderer;
import com.stanfy.views.list.RequestBuilderAdapter;
import com.stanfy.views.list.RequestBuilderAdapter.RBAdapterCallback;

/**
 * Base fragment that displays fetchable lists. Such fragment retains its state.
 * @param <AppT> application type
 * @param <MT> model type
 * @param <RBT> request builder type
 * @param <AT> adapter type
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public abstract class RequestBuilderListFragment<AppT extends Application, MT extends UniqueObject, RBT extends RequestBuilder, AT extends RequestBuilderAdapter<MT, RBT>> extends BaseFragment<AppT> implements OnItemsLoadedListener {

  /** Logging tag. */
  protected static final String TAG = "RBListFragment";
  /** Debug flag. */
  private static final boolean DEBUG = DebugFlags.DEBUG_GUI;

  /** Token. */
  private static final String EXTRA_TOKEN = "token";

  /** Adapter. */
  private AT adapter;
  /** Request callback. */
  private RBAdapterCallback<MT, RBT, AT> requestCallback;
  /** List view instance. */
  private FetchableListView listView;

  /** Flag that enables {@link FetchableListView#restartObserve()} method call when we reset the request builder. */
  private boolean restartObserveEnabled = true;
  /** Flag. */
  private boolean shouldRelaod = false;

  /** Request token. */
  private int requestToken;
  /** Fragment state to be retained. */
  private final StateHolder<MT> state = createState();

  /** Previous locale. */
  private Locale prevLocale;

  /** Empty adapter. */
  private static final EmptyAdapter EMPTY_ADAPTER = new EmptyAdapter();

  /** @return a requests callback instance */
  protected abstract RBAdapterCallback<MT, RBT, AT> createCallback(final BaseFragmentActivity<AppT> owner);
  /** @return adapter instance */
  protected abstract AT createAdapter(final Context context, final ElementRenderer<MT> renderer);
  /** @return renderer instance */
  protected abstract ElementRenderer<MT> createRenderer();

  /** @return request token */
  protected int getRequestToken() { return requestToken; }

  // =============================== STATES ===============================
  @Override
  public void onAttach(final Activity activity) {
    super.onAttach(activity);
    @SuppressWarnings("unchecked")
    final BaseFragmentActivity<AppT> ownerActivity = (BaseFragmentActivity<AppT>)activity;
    requestCallback = createCallback(ownerActivity);
    ownerActivity.addRequestCallback(requestCallback);
  }

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRetainInstance(true);
    requestToken = savedInstanceState != null ? savedInstanceState.getInt(EXTRA_TOKEN, AppUtils.rand()) : AppUtils.rand();
    state.refreshed = false;
  }

  @Override
  public final View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
    final View result = createView(inflater, container, savedInstanceState);

    final FetchableListView listView = (FetchableListView)result.findViewById(android.R.id.list);
    this.listView = listView;
    listView.setOnItemsLoadedListener(this);
    setAdapter(createAdapter(getOwnerActivity(), createRenderer()));

    if (isDataLocaleDependent()) {
      final Configuration config = getResources().getConfiguration();
      final Locale newLocale = config.locale;
      if (prevLocale != null && !prevLocale.equals(newLocale)) { resetState(); }
      prevLocale = newLocale;
    }

    // resolve adapter state
    restartObserveEnabled = true;
    final StateHolder<MT> state = this.state;
    if (state.refreshed && !state.reset) {
      if (state.elements != null) {
        adapter.replace(state.elements);
        listView.restoreState(state.adapterState);
        onItemsLoaded(state.elements.isEmpty());
        restartObserveEnabled = false;
      } else {
        listView.restoreState(state.adapterState);
        onItemsLoaded(true);
      }
      state.refreshed = false;
    }
    state.reset = false;

    requestCallback.setList(listView);
    return result;
  }

  @Override
  protected void onRestart() {
    if (shouldRelaod || (!adapter.isBusy() && adapter.isEmpty())) {
      reload();
    }
  }

  @Override
  public void onStop() {
    shouldRelaod = adapter.isBusy();
    super.onStop();
    adapter.onStop();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();

    if (getRetainInstance() && !isRemoving()) { fixState(); }

    adapter.destroy();
    listView.setOnItemsLoadedListener(null);
    listView = null;
  }

  @Override
  public void onDetach() {
    super.onDetach();
    if (DEBUG) { Log.v(TAG, "onDetach " + this); }
    getOwnerActivity().removeRequestCallback(requestCallback);
    requestCallback = null;
  }

  @Override
  public void onSaveInstanceState(final Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putInt(EXTRA_TOKEN, requestToken);
  }

  // ======================================================================

  protected StateHolder<MT> createState() { return new StateHolder<MT>(); }

  protected void fixState() {
    final StateHolder<MT> state = this.state;
    if (adapter != null) {
      state.elements = adapter.copyElements();
      state.adapterState = adapter.getState();
      state.refreshed = true;
    } else {
      state.refreshed = false;
    }
  }

  protected StateHolder<MT> getState() { return state; }

  protected View createView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
    final FetchableListView result = new FetchableListView(getOwnerActivity());
    result.setId(android.R.id.list);
    // configure layout parameters for the list
    setupLayoutParameters(result, container);
    return result;
  }

  /** @return fetchable list view instance from the layout */
  public FetchableListView getListView() {
    if (listView != null) { return listView; }
    listView = (FetchableListView)getOwnerActivity().findViewById(android.R.id.list);
    return listView;
  }

  /**
   * Default policy is to conservative. So let the user to decide if he want fully reload the list.
   * @param rb request builder
   * @param forceClear force reload or not
   * @return does list affected or not
   */
  public boolean setRequestBuilder(final RequestBuilder rb, final boolean forceClear) {
    restartObserveEnabled |= forceClear;
    if (forceClear) {
      listView.setAdapter(EMPTY_ADAPTER);
      listView.setAdapter(adapter);
    }
    return setRequestBuilder(rb);
  }

  /**
   * @param rb request builder
   * @return does list affected or not
   */
  @SuppressWarnings("unchecked")
  public boolean setRequestBuilder(final RequestBuilder rb) {
    final boolean result = restartObserveEnabled;
    adapter.setRequestBuilder((RBT)rb, result);
    reload();
    restartObserveEnabled = true;
    return result;
  }

  /** Operation for the fragment's adapter. */
  public Operation getOperation() { return adapter.getOperation(); }

  /**
   * Force reloading.
   */
  public void reload() {
    if (restartObserveEnabled) {
      adapter.clear();
      listView.restartObserve();
    } else if (!adapter.isBusy()) {
      listView.getStateWindowHelper().showMain();
    }
  }

  /**
   * Return type of this method was changed to {@link RequestBuilderAdapter} due to compatibility issues.
   * @return the adapter
   */
  public RequestBuilderAdapter<MT, RBT> getAdapter() { return adapter; }

  /**
   * Setting adapter.
   */
  protected void setAdapter(final AT adapter) {
    if (DEBUG) { Log.d(TAG, "New adapter " + adapter); }
    adapter.setImagesManagerContext(getOwnerActivity().getApp().getImagesContext());
    this.adapter = adapter;
    listView.setAdapter(adapter);
  }

  @Override
  public void onItemsLoaded(final boolean empty) {
    shouldRelaod = false;
  }

  /**
   * Reset the fragment data; it will cause the data to be reloaded on view recreation.
   */
  public void resetState() {
    state.reset = true;
  }

  /** @return true if data should be reloaded on locale changes */
  protected boolean isDataLocaleDependent() { return false; }

  @Override
  public void onConfigurationChanged(final Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    if (isDataLocaleDependent()) {
      final RequestBuilderAdapter<MT, RBT> adapter = getAdapter();
      final RBT requestBuilder = adapter == null ? null : adapter.getRequestBuilder();
      final Locale newLocale = newConfig.locale;
      if (DEBUG) {
        Log.d(TAG, "Prev locale: " + prevLocale);
        Log.d(TAG, "New locale: " + newLocale);
      }
      if (requestBuilder != null && prevLocale != null && !prevLocale.equals(newLocale)) {
        setRequestBuilder(requestBuilder, true);
      }
      prevLocale = newLocale;
    }
  }

  /**
   * State holder. This data is retained.
   * @author Roman Mazur - Stanfy (http://www.stanfy.com)
   * @param <MT> model type
   */
  public static class StateHolder<MT extends UniqueObject> {
    /** Elements list. */
    ArrayList<MT> elements;
    /** Adapter state. */
    FetchableListAdapter.State adapterState;
    /** Refresh flag. */
    boolean refreshed;
    /** Reset flag. */
    boolean reset;
    /** @return the refreshed */
    public final boolean isRefreshed() { return refreshed; }
    /** @return the reset */
    public final boolean isReset() { return reset; }
  }

  // =====================================================================================



//  /** @return true to enable reload menu */
//  protected abstract boolean canBeReloaded();

//  @Override
//  public boolean onCreateOptionsMenu(final Menu menu) {
//    if (canBeReloaded()) {
//      getMenuInflater().inflate(R.menu.refresh_menu, menu);
//      return true;
//    }
//    return super.onCreateOptionsMenu(menu);
//  }
//  @Override
//  public boolean onOptionsItemSelected(final MenuItem item) {
//    switch (item.getItemId()) {
//    case R.id.menu_refresh:
//      // reload
//      reload();
//      return true;
//
//    default:
//      return super.onOptionsItemSelected(item);
//    }
//  }



}

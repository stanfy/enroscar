package com.stanfy.app.fragments.list;

import java.util.List;
import java.util.Locale;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stanfy.DebugFlags;
import com.stanfy.app.BaseFragment;
import com.stanfy.app.CrucialGUIOperationManager;
import com.stanfy.app.beans.BeansManager;
import com.stanfy.content.UniqueObject;
import com.stanfy.serverapi.request.RequestBuilder;
import com.stanfy.serverapi.response.ResponseData;
import com.stanfy.views.list.FetchableListView;
import com.stanfy.views.list.ModelListAdapter;
import com.stanfy.views.list.ModelListAdapter.ElementRenderer;
import com.stanfy.views.list.ResponseDataLoaderAdapter;

/**
 * Base fragment that displays fetchable lists. This fragment retains its state.
 * @param <MT> model type
 * @param <LT> list type
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public abstract class RequestBuilderListFragment<MT extends UniqueObject, LT extends List<MT>> extends BaseFragment implements LoaderCallbacks<ResponseData<LT>> {

  /** Logging tag. */
  protected static final String TAG = "RBListFragment";
  /** Debug flag. */
  private static final boolean DEBUG = DebugFlags.DEBUG_GUI;

  /** List loader ID. */
  protected static final int LIST_LOADER_ID = 1;

  /** Adapter. */
  private ResponseDataLoaderAdapter<MT, LT> adapter;
  /** List view instance. */
  private FetchableListView listView;

  /** Previous locale. */
  private Locale prevLocale;

  /** GUI operations manager. */
  private CrucialGUIOperationManager crucialGUIOperationManager;

  /** @return request builder instance */
  protected abstract RequestBuilder<LT> createRequestBuilder();
  /** @return renderer instance */
  protected abstract ElementRenderer<MT> createRenderer();

  /** @return adapter with renderer */
  protected ModelListAdapter<MT> createAdapter(final ElementRenderer<MT> renderer) {
    return new ModelListAdapter<MT>(getActivity(), renderer);
  }

  /** @return request builder adapter */
  protected ResponseDataLoaderAdapter<MT, LT> wrapAdapter(final ModelListAdapter<MT> adapter) {
    return new ResponseDataLoaderAdapter<MT, LT>(getActivity(), adapter);
  }

  /** @return true if data should be reloaded on locale changes */
  protected boolean isDataLocaleDependent() { return false; }

  // =============================== STATES ===============================
  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    crucialGUIOperationManager = BeansManager.get(getActivity()).getCrucialGUIOperationManager();
    setRetainInstance(true);
  }

  @Override
  public final View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
    final View result = createView(inflater, container, savedInstanceState);

    final FetchableListView listView = (FetchableListView)result.findViewById(android.R.id.list);
    this.listView = listView;

    if (isDataLocaleDependent()) {
      final Configuration config = getResources().getConfiguration();
      final Locale newLocale = config.locale;
      if (prevLocale != null && !prevLocale.equals(newLocale)) {
        getLoaderManager().destroyLoader(LIST_LOADER_ID);
      }
      prevLocale = newLocale;
    }

    return result;
  }

  @Override
  public void onStart() {
    super.onStart();
    if (adapter != null) {
      crucialGUIOperationManager.addCrucialGUIOperationListener(adapter);
    }
  }

  @Override
  public void onStop() {
    super.onStop();
    if (adapter != null) {
      crucialGUIOperationManager.removeCrucialGUIOperationListener(adapter);
    }
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    listView = null;
  }

  @Override
  public void onConfigurationChanged(final Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    if (isDataLocaleDependent()) {
      final Locale newLocale = newConfig.locale;
      if (DEBUG) {
        Log.d(TAG, "Prev locale: " + prevLocale);
        Log.d(TAG, "New locale: " + newLocale);
      }
      if (prevLocale != null && !prevLocale.equals(newLocale)) {
        reload();
      }
      prevLocale = newLocale;
    }
  }

  // ======================================================================

  @Override
  public Loader<ResponseData<LT>> onCreateLoader(final int id, final Bundle bundle) {
    if (id == LIST_LOADER_ID) {
      return createRequestBuilder().getLoader();
    }
    throw new IllegalArgumentException("Cannot create loader with id=" + id);
  }

  @Override
  public void onLoaderReset(final Loader<ResponseData<LT>> loader) {
    if (adapter != null) {
      this.adapter.onLoaderReset(loader);
    }
  }

  @Override
  public void onLoadFinished(final Loader<ResponseData<LT>> loader, final ResponseData<LT> data) {
    if (adapter != null) {
      this.adapter.onLoadFinished(loader, data);
    }
  }

  /**
   * Start loading.
   * Call it from {@link #onActivityCreated(Bundle)}.
   */
  public void startLoad() {
    if (this.listView.getAdapter() != null) { return; }

    // create adapter
    this.adapter = wrapAdapter(createAdapter(createRenderer()));
    // init loader, data can passed to adapter at once
    final Loader<ResponseData<LT>> loader = getLoaderManager().initLoader(LIST_LOADER_ID, null, this);
    // connect adapter and loader
    this.adapter.setLoader(loader);

    // connect list and adapter
    this.listView.setAdapter(this.adapter);

    if (isVisible()) {
      crucialGUIOperationManager.addCrucialGUIOperationListener(adapter);
    }
  }

  /** Restart loader. */
  public void reload() {
    getLoaderManager().restartLoader(LIST_LOADER_ID, null, this);
  }

  protected View createView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
    final FetchableListView result = new FetchableListView(getActivity());
    final ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    result.setLayoutParams(params);
    result.setId(android.R.id.list);
    return result;
  }

  /** @return fetchable list view instance from the layout */
  public FetchableListView getListView() {
    if (listView != null) { return listView; }
    listView = (FetchableListView)getActivity().findViewById(android.R.id.list);
    return listView;
  }

//  /**
//   * Default policy is to conservative. So let the user to decide if he want fully reload the list.
//   * @param rb request builder
//   * @param forceClear force reload or not
//   * @return does list affected or not
//   */
//  public boolean setRequestBuilder(final RequestBuilder rb, final boolean forceClear) {
//    restartObserveEnabled |= forceClear;
//    if (forceClear) {
//      listView.setAdapter(EMPTY_ADAPTER);
//      listView.setAdapter(adapter);
//    }
//    return setRequestBuilder(rb);
//  }
//
//  /**
//   * @param rb request builder
//   * @return does list affected or not
//   */
//  @SuppressWarnings("unchecked")
//  public boolean setRequestBuilder(final RequestBuilder rb) {
//    final boolean result = restartObserveEnabled;
//    adapter.setRequestBuilder((RBT)rb, result);
//    reload();
//    restartObserveEnabled = true;
//    return result;
//  }
//
//  /**
//   * Force reloading.
//   */
//  public void reload() {
//    if (restartObserveEnabled) {
//      adapter.clear();
//      listView.restartObserve();
//    } else if (!adapter.isBusy()) {
//      listView.getStateWindowHelper().showMain();
//    }
//  }

//  private void setAdapter(final AT adapter, final boolean setupCrucialGUIOperations) {
//    if (DEBUG) { Log.d(TAG, "New adapter " + adapter); }
//    if (setupCrucialGUIOperations) {
//      crucialGUIOperationManager.removeCrucialGUIOperationListener(this.adapter);
//      crucialGUIOperationManager.addCrucialGUIOperationListener(adapter);
//    }
//    this.adapter = adapter;
//    listView.setAdapter(adapter);
//  }
//
//  /**
//   * Setting adapter.
//   */
//  protected void setAdapter(final AT adapter) {
//    setAdapter(adapter, isResumed());
//  }

}

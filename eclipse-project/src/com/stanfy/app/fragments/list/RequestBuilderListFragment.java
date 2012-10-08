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
import com.stanfy.views.list.FetchableView;
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

  /** Core adapter instance. */
  private ModelListAdapter<MT> coreAdapter;
  /** Adapter. */
  private ResponseDataLoaderAdapter<MT, LT> rbAdapter;
  /** List view instance. */
  private FetchableView listView;

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

  /**
   * A good place for configuring main loader, e.g set offset incrementor.
   * @param loader loader instance
   * @return loader instance
   */
  protected Loader<ResponseData<LT>> modifyLoader(final Loader<ResponseData<LT>> loader) {
    return loader;
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

    final FetchableView listView = (FetchableView)result.findViewById(android.R.id.list);
    this.listView = listView;

    // create adapter
    this.coreAdapter = createAdapter(createRenderer());
    this.rbAdapter = wrapAdapter(this.coreAdapter);

    // connect list and adapter
    listView.setAdapter(this.rbAdapter);

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
    if (DEBUG) { Log.v(TAG, "Start " + this); }
    if (rbAdapter != null) {
      crucialGUIOperationManager.addCrucialGUIOperationListener(rbAdapter);
    }
  }

  @Override
  public void onStop() {
    super.onStop();
    if (DEBUG) { Log.v(TAG, "Stop " + this); }
    if (rbAdapter != null) {
      crucialGUIOperationManager.removeCrucialGUIOperationListener(rbAdapter);
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
      if (rbAdapter != null) {
        rbAdapter.onLoadStart();
      }
      return modifyLoader(createRequestBuilder().getLoader());
    }
    throw new IllegalArgumentException("Cannot create loader with id=" + id);
  }

  @Override
  public void onLoaderReset(final Loader<ResponseData<LT>> loader) {
    if (DEBUG) { Log.v(TAG, "onLoaderReset, " + this); }
    if (rbAdapter != null) {
      this.rbAdapter.onLoaderReset(loader);
    }
  }

  @Override
  public void onLoadFinished(final Loader<ResponseData<LT>> loader, final ResponseData<LT> data) {
    if (DEBUG) { Log.v(TAG, "onLoadFinished, " + this); }
    if (rbAdapter != null) {
      this.rbAdapter.onLoadFinished(loader, data);
    }
  }

  protected void onLoadStart() {
    // nothing
  }

  /**
   * @return instance of an adapter created by {@link #createAdapter(ElementRenderer)} method
   */
  public ModelListAdapter<MT> getCoreAdapter() { return coreAdapter; }

  /**
   * @return instance of an adapter returned by {@link #wrapAdapter(ModelListAdapter)}
   */
  public ResponseDataLoaderAdapter<MT, LT> getAdapter() { return rbAdapter; }

  /**
   * Start loading.
   * Call it from {@link #onActivityCreated(Bundle)}.
   */
  public void startLoad() {
    if (DEBUG) { Log.v(TAG, "startLoad, " + this); }
    // init loader, data can passed to adapter at once
    onLoadStart();
    getLoaderManager().initLoader(LIST_LOADER_ID, null, this);
  }

  /** Restart loader. */
  public void reload() {
    if (this.rbAdapter != null && !this.rbAdapter.isEmpty()) {
      getListView().setSelection(0);
    }
    onLoadStart();
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
  public FetchableView getListView() {
    if (listView != null) { return listView; }
    listView = (FetchableView) getView().findViewById(android.R.id.list);
    return listView;
  }

}

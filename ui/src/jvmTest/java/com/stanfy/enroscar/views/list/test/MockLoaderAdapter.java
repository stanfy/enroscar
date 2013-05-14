package com.stanfy.enroscar.views.list.test;

import java.util.List;

import android.content.Context;
import android.support.v4.content.Loader;
import android.widget.ArrayAdapter;

import com.stanfy.enroscar.views.StateHelper;
import com.stanfy.enroscar.views.list.LoaderAdapter;

/**
 * Test loader adapter.
 * @param <T> type
 * @author Vladislav Lipskiy - Stanfy (http://www.stanfy.com)
 *
 */
public class MockLoaderAdapter<T> extends LoaderAdapter<List<T>> {

  /** What was called? */
  boolean onSuccessCalled, onEmptyCalled, onErrorCalled;

  public MockLoaderAdapter(final Context context, final ArrayAdapter<T> coreAdapter) {
    super(context, coreAdapter);
  }

  public MockLoaderAdapter(final Context context, final ArrayAdapter<T> coreAdapter,
      final StateHelper stateHelper) {
    super(context, coreAdapter, stateHelper);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected ArrayAdapter<T> getCore() {
    return (ArrayAdapter<T>) super.getCore();
  }

  @Override
  public void notifyDataSetChanged() {
    getCore().notifyDataSetChanged();
  }

  @Override
  protected boolean isResponseSuccessful(final List<T> data) {
    return data != null;
  }

  @Override
  protected boolean isResponseEmpty(final List<T> data) {
    return data != null ? data.isEmpty() : true;
  }

  @Override
  protected void replaceDataInCore(final List<T> data) {
    final ArrayAdapter<T> core = getCore();
    core.clear();
    for (final T element : data) {
      core.add(element);
    }
    notifyDataSetChanged();
  }

  @Override
  public void onLoadFinished(final Loader<List<T>> loader, final List<T> data) {
    onSuccessCalled = false;
    onEmptyCalled = false;
    onErrorCalled = false;
    super.onLoadFinished(loader, data);
  }

  @Override
  protected void onDataSuccess(final List<T> data) {
    onSuccessCalled = true;
    super.onDataSuccess(data);
  }

  @Override
  protected void onDataEmpty(final List<T> data) {
    onEmptyCalled = true;
    super.onDataEmpty(data);
  }

  @Override
  protected void onDataError(final List<T> data) {
    onErrorCalled = true;
    super.onDataError(data);
  }

}

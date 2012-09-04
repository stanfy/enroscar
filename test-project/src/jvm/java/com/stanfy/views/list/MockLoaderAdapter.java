package com.stanfy.views.list;

import java.util.List;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.stanfy.views.StateHelper;

/**
 * Test loader adapter.
 * @param <T> type
 * @author Vladislav Lipskiy - Stanfy (http://www.stanfy.com)
 *
 */
public class MockLoaderAdapter<T> extends LoaderAdapter<List<T>> {

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
  }

}

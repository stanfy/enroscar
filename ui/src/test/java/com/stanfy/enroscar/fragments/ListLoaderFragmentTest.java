package com.stanfy.enroscar.fragments;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stanfy.enroscar.content.loader.ResponseData;
import com.stanfy.enroscar.views.list.adapter.RendererBasedAdapter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class ListLoaderFragmentTest {

  /** Test loader ID. */
  private static final int LOADER_ID = 100;

  /** Fragment under the test. */
  private ListLoaderFragment<String, List<String>> fragment;

  /** Items adapter. */
  private RendererBasedAdapter<String> adapter;

  /** Last used loader ID. */
  private int lastUserLoaderId;

  /** Method call flag. */
  private boolean createViewCalled, modifyLoaderCalled;

  @Before
  public void init() {

    createViewCalled = false;

    adapter = new RendererBasedAdapter<String>(Robolectric.application, null) {
      @Override
      public long getItemId(final int position) {
        return position;
      }
    };

    fragment = new ListLoaderFragment<String, List<String>>() {

      @Override
      protected Loader<ResponseData<List<String>>> createLoader() {
        return new ListLoader(Robolectric.application);
      }

      @Override
      protected RendererBasedAdapter<String> createAdapter() {
        return adapter;
      }

      @Override
      public Loader<ResponseData<List<String>>> onCreateLoader(final int id, final Bundle bundle) {
        lastUserLoaderId = id;
        return super.onCreateLoader(id, bundle);
      }

      @Override
      protected View createView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        createViewCalled = true;
        return super.createView(inflater, container, savedInstanceState);
      }

      @Override
      protected Loader<ResponseData<List<String>>> modifyLoader(final Loader<ResponseData<List<String>>> loader) {
        modifyLoaderCalled = true;
        return super.modifyLoader(loader);
      }
    };

    fragment.setLoaderId(LOADER_ID);

    FragmentActivity activity = Robolectric.buildActivity(FragmentActivity.class).attach().create().start().get();
    activity.getSupportFragmentManager().beginTransaction()
        .add(android.R.id.content, fragment)
        .commit();
  }

  @Test
  public void shouldUseDefinedLoaderId() {
    fragment.startLoad();
    assertThat(lastUserLoaderId).isEqualTo(LOADER_ID).isEqualTo(fragment.getLoaderId());
  }

  @Test
  public void shouldProvideCoreAdapter() {
    assertThat(fragment.getCoreAdapter()).isSameAs(adapter);
  }

  @Test
  public void shouldProvideLoaderAdapter() {
    assertThat(fragment.getAdapter()).isNotNull();
  }

  @Test
  public void shouldProvideListView() {
    assertThat(fragment.getListView()).isNotNull();
  }

  @Test
  public void shouldDelegateToCreateView() {
    assertThat(createViewCalled).isTrue();
  }

  private static class ListLoader extends Loader<ResponseData<List<String>>> {

    public ListLoader(final Context context) {
      super(context);
    }

    @Override
    protected void onForceLoad() {
      ResponseData<List<String>> data = new ResponseData<>(Arrays.asList("a", "b", "c"));
      deliverResult(data);
    }
  }

}
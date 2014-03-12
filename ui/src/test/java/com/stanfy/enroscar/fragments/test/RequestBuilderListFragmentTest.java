package com.stanfy.enroscar.fragments.test;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.mockwebserver.MockResponse;
import com.stanfy.enroscar.beans.BeansManager;
import com.stanfy.enroscar.content.loader.ResponseData;
import com.stanfy.enroscar.fragments.RequestBuilderListFragment;
import com.stanfy.enroscar.net.test.AbstractMockServerTest;
import com.stanfy.enroscar.net.operation.RequestBuilder;
import com.stanfy.enroscar.net.operation.SimpleRequestBuilder;
import com.stanfy.enroscar.rest.response.handler.GsonContentHandler;
import com.stanfy.enroscar.views.list.adapter.RendererBasedAdapter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.net.HttpURLConnection;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests for RequestBuilderListFragment.
 */
@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18)
public class RequestBuilderListFragmentTest extends AbstractMockServerTest {

  /** Test loader ID. */
  private static final int LOADER_ID = 100;

  /** Fragment under the test. */
  private RequestBuilderListFragment<String, List<String>> fragment;

  /** Items adapter. */
  private RendererBasedAdapter<String> adapter;

  /** Last used loader ID. */
  private int lastUserLoaderId;

  /** Method call flag. */
  private boolean createViewCalled, modifyLoaderCalled;

  private void scheduleResponse() {
    getWebServer().enqueue(new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK).setBody("['a', 'b', 'c']"));
  }

  @Override
  protected void configureBeansManager(final BeansManager.Editor editor) {
    super.configureBeansManager(editor);
    editor.put(GsonContentHandler.class);
  }

  @Override
  protected void whenBeansConfigured() {
    super.whenBeansConfigured();
    initContentHandler(GsonContentHandler.BEAN_NAME);
  }

  @Before
  public void init() {

    createViewCalled = false;
    modifyLoaderCalled = false;

    adapter = new RendererBasedAdapter<String>(Robolectric.application, null) {
      @Override
      public long getItemId(final int position) {
        return position;
      }
    };

    fragment = new RequestBuilderListFragment<String, List<String>>() {
      @Override
      protected RequestBuilder<List<String>> createRequestBuilder() {
        return new SimpleRequestBuilder<List<String>>(getActivity()) { }
            .setUrl(getWebServer().getUrl("/").toString());
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
    scheduleResponse();
    fragment.startLoad();
    assertThat(lastUserLoaderId).isEqualTo(LOADER_ID).isEqualTo(fragment.getLoaderId());
    assertThat(modifyLoaderCalled).as("modifyLoader method should be called").isTrue();
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

}

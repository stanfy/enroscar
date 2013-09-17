package com.stanfy.enroscar.fragments.test;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.Loader;

import com.google.mockwebserver.MockResponse;
import com.google.mockwebserver.MockWebServer;
import com.stanfy.enroscar.beans.BeansManager;
import com.stanfy.enroscar.content.loader.ResponseData;
import com.stanfy.enroscar.fragments.RequestBuilderListFragment;
import com.stanfy.enroscar.net.EnroscarConnectionsEngine;
import com.stanfy.enroscar.rest.RemoteServerApiConfiguration;
import com.stanfy.enroscar.rest.request.RequestBuilder;
import com.stanfy.enroscar.rest.request.SimpleRequestBuilder;
import com.stanfy.enroscar.rest.response.handler.GsonContentHandler;
import com.stanfy.enroscar.views.list.adapter.ElementRenderer;
import com.stanfy.enroscar.views.list.adapter.RendererBasedAdapter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.net.HttpURLConnection;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests for RequestBuilderListFragment.
 */
@RunWith(RobolectricTestRunner.class)
public class RequestBuilderListFragmentTest {

  /** Test loader ID. */
  private static final int LOADER_ID = 100;

  /** Fragment under the test. */
  private RequestBuilderListFragment<String, List<String>> fragment;

  /** Mock web server. */
  private MockWebServer mockWebServer;

  /** Last used loader ID. */
  private int lastUserLoaderId;

  @Before
  public void init() throws Exception {
    EnroscarConnectionsEngine.config().install(Robolectric.application);
    BeansManager.get(Robolectric.application).edit()
        .put(RemoteServerApiConfiguration.class)
        .put(GsonContentHandler.class)
        .commit();

    mockWebServer = new MockWebServer();
    mockWebServer.play();

    mockWebServer.enqueue(new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK).setBody("['a', 'b', 'c']"));

    fragment = new RequestBuilderListFragment<String, List<String>>() {
      @Override
      protected RequestBuilder<List<String>> createRequestBuilder() {
        return new SimpleRequestBuilder<List<String>>(getActivity()) { }
            .setUrl(mockWebServer.getUrl("test").toString());
      }

      @Override
      protected RendererBasedAdapter<String> createAdapter() {
        return new RendererBasedAdapter<String>(getActivity(), null) {
          @Override
          public long getItemId(final int position) {
            return position;
          }
        };
      }

      @Override
      public Loader<ResponseData<List<String>>> onCreateLoader(final int id, final Bundle bundle) {
        lastUserLoaderId = id;
        return super.onCreateLoader(id, bundle);
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
    assertThat(lastUserLoaderId).isEqualTo(LOADER_ID).isEqualTo(fragment.getLoaderId());
  }

  @After
  public void stopWebServer() throws Exception {
    mockWebServer.shutdown();
  }

}

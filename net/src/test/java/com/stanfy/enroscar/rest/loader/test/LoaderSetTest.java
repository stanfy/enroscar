package com.stanfy.enroscar.rest.loader.test;

import static org.fest.assertions.api.Assertions.assertThat;

import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.Ignore;
import org.robolectric.annotation.Config;
import org.robolectric.util.FragmentTestUtil;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;

import com.google.mockwebserver.MockResponse;
import com.stanfy.enroscar.beans.BeansManager.Editor;
import com.stanfy.enroscar.content.loader.LoaderSet;
import com.stanfy.enroscar.content.loader.LoaderSetAccess;
import com.stanfy.enroscar.content.loader.ResponseData;
import com.stanfy.enroscar.rest.response.handler.StringContentHandler;

/**
 * Test for {@link LoaderSet}.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
@Config(emulateSdk = 18)
public class LoaderSetTest extends AbstractLoaderTest {

  @Override
  protected void configureBeansManager(final Editor editor) {
    super.configureBeansManager(editor);
    editor.put(StringContentHandler.class);
  }

  @Override
  protected void whenBeansConfigured() {
    super.whenBeansConfigured();
    initContentHandler(StringContentHandler.BEAN_NAME);
  }
  
  private static Fragment createFragment() throws Exception {
    final Fragment fragment = new Fragment();
    FragmentTestUtil.startFragment(fragment);
    return fragment;
  }

  @Ignore
  @Test
  public void shouldWaitFor3Requests() throws Throwable {

    // enqueue
    getWebServer().enqueue(new MockResponse().setBody("R1"));
    getWebServer().enqueue(new MockResponse().setBody("R2"));
    getWebServer().enqueue(new MockResponse().setBody("R3"));

    final URL url = getWebServer().getUrl("/");

    final Fragment fragment = createFragment();
    final LoaderManager loaderManager = fragment.getLoaderManager();
    assertThat(loaderManager).isNotNull();

    final CountDownLatch waiter = new CountDownLatch(1);


    // describe loader
    final LoaderSet set = LoaderSet.build(getApplication())
        .withManager(loaderManager)

        // load R1
        .withCallbacks(new LoaderSet.SetCallbacksAdapter<ResponseData<String>>() {
          @Override
          public Loader<ResponseData<String>> onCreateLoader(final int id, final Bundle args) {
            return new MyRequestBuilder<String>(getApplication()) { }
              .setUrl(url.toString())
              .setFormat("string")
              .getLoader();
          }
        }, 1)

        // load R2, R3
        .withCallbacks(new LoaderSet.SetCallbacksAdapter<ResponseData<String>>() {
          @Override
          public Loader<ResponseData<String>> onCreateLoader(final int id, final Bundle args) {
            return new MyRequestBuilder<String>(getApplication()) { }
              .setUrl(url.toString())
              .setFormat("string")
              .getLoader();
          }
        }, 2, 3)

        .create();

    final LoaderSet.LoaderSetCallback callbacks = new LoaderSet.LoaderSetCallback() {
      @Override
      public void onLoadFinished(final Object[] data) {
        Log.i("ChainTest", Arrays.toString(data));
        waiter.countDown();
      }
    };

    set.init(null, callbacks);

    // TODO finish it
    waitAndAssert(new Waiter<Object[]>() {
      @Override
      public Object[] waitForData() {
        try {
          waiter.await(2, TimeUnit.SECONDS);
          return LoaderSetAccess.getResults(set);
        } catch (final InterruptedException e) {
          return null;
        }
      }
    }, new Asserter<Object[]>() {
      @SuppressWarnings("unchecked")
      @Override
      public void makeAssertions(final Object[] data) throws Exception {
        assertThat(data.length).isEqualTo(3);
        assertThat(((ResponseData<String>)data[0]).getModel()).isEqualTo("R1");
        assertThat(((ResponseData<String>)data[1]).getModel()).isEqualTo("R2");
        assertThat(((ResponseData<String>)data[2]).getModel()).isEqualTo("R3");
      }
    });

  }


}

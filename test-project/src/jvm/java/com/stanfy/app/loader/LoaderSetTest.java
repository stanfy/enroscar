package com.stanfy.app.loader;

import static org.hamcrest.Matchers.notNullValue;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManagerImplAccess;
import android.support.v4.content.Loader;
import android.util.Log;

import com.google.mockwebserver.MockResponse;
import com.stanfy.app.beans.BeansManager.Editor;
import com.stanfy.serverapi.response.ResponseData;
import com.stanfy.test.AbstractApplicationServiceTest;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.bytecode.DirectCallPolicy.FullStackDirectCallPolicy;

/**
 * Test for {@link LoaderChain}.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public class LoaderSetTest extends AbstractApplicationServiceTest {

  @Override
  protected void configureBeansManager(final Editor editor) {
    editor.required().remoteServerApi("string");
  }

  private static Fragment createFragment() throws Exception {
    final FragmentActivity activity = new FragmentActivity();
    final Fragment fragment = new Fragment();

    final Field field = Fragment.class.getDeclaredField("mActivity");
    field.setAccessible(true);
    field.set(fragment, activity);

    Robolectric.shadowOf(fragment).setActivity(activity);

    return fragment;
  }

  @Test
  public void shouldWaitFor3Requests() throws Throwable {

    // enqueue
    getWebServer().enqueue(new MockResponse().setBody("R1"));
    getWebServer().enqueue(new MockResponse().setBody("R2"));
    getWebServer().enqueue(new MockResponse().setBody("R3"));

    final URL url = getWebServer().getUrl("/");

    final Fragment fragment = createFragment();
    final LoaderManager loaderManager = Robolectric.directlyOnFullStack(
        FullStackDirectCallPolicy.build(fragment).include(FragmentActivity.class)
    ).getLoaderManager();
    assertThat(loaderManager, notNullValue());

    final FragmentActivity activity = fragment.getActivity();
    Robolectric.directlyOn(LoaderManagerImplAccess.class);
    LoaderManagerImplAccess.initLoaderManager(loaderManager, activity);

    final CountDownLatch waiter = new CountDownLatch(1);



    // describe loader
    final LoaderSet set = LoaderSet.build(getApplication())
        .withManager(loaderManager)

        // load R1
        .withCallbacks(new LoaderSet.SetCallbacksAdapter<ResponseData<String>>() {
          @Override
          public Loader<ResponseData<String>> onCreateLoader(final int id, final Bundle args) {
            return initLoader(new MyRequestBuilder<String>(getApplication()) { }
              .setStartedLoader(true)
              .setUrl(url.toString())
              .setFormat("string")
              .getLoader());
          }
        }, 1)

        // load R2, R3
        .withCallbacks(new LoaderSet.SetCallbacksAdapter<ResponseData<String>>() {
          @Override
          public Loader<ResponseData<String>> onCreateLoader(final int id, final Bundle args) {
            return initLoader(new MyRequestBuilder<String>(getApplication()) { }
              .setStartedLoader(true)
              .setUrl(url.toString())
              .setFormat("string")
              .getLoader());
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

    Robolectric.directlyOnFullStack(FullStackDirectCallPolicy
        .build(loaderManager)
        .include(Arrays.asList("android.support.v4", "com.stanfy.test.AbstractMockServerTest"))
    );
    set.init(null, callbacks);

    waitAndAssert(new Waiter<Object[]>() {
      @Override
      public Object[] waitForData() {
        try {
          waiter.await(2, TimeUnit.SECONDS);
          return set.getResults();
        } catch (final InterruptedException e) {
          return null;
        }
      }
    }, new Asserter<Object[]>() {
      @Override
      public void makeAssertions(final Object[] data) throws Exception {
        // TODO finish it
//        assertThat(data.length, equalTo(3));
//        assertThat((String)data[0], equalTo("R1"));
//        assertThat((String)data[1], equalTo("R2"));
//        assertThat((String)data[2], equalTo("R3"));
      }
    });

  }


}

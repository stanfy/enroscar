package com.stanfy.app.loader;

import java.lang.reflect.Field;

import org.junit.Test;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.google.mockwebserver.MockResponse;
import com.stanfy.app.beans.BeansManager.Editor;
import com.stanfy.serverapi.request.SimpleRequestBuilder;
import com.stanfy.serverapi.response.ResponseData;
import com.stanfy.test.AbstractApplicationServiceTest;
import com.xtremelabs.robolectric.Robolectric;

/**
 * Test for {@link LoaderChain}.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public class ChainLoaderTest extends AbstractApplicationServiceTest {

  @Override
  protected void configureBeansManager(final Editor editor) {
    editor.required().remoteServerApi("string");
  }

  @Test
  public void shouldWaitFor3Requests() throws Exception {

    // enqueue
    getWebServer().enqueue(new MockResponse().setBody("R1"));
    getWebServer().enqueue(new MockResponse().setBody("R2"));
    getWebServer().enqueue(new MockResponse().setBody("R3"));

    final FragmentActivity activity = new FragmentActivity();
    final Fragment fragment = new Fragment();
//    final FragmentManager fm = activity.getSupportFragmentManager();
//    fm.beginTransaction()
//      .add(fragment, "f")
//      .commit();
//    fm.executePendingTransactions();
    final Field field = Fragment.class.getDeclaredField("mActivity");
    field.setAccessible(true);
    field.set(fragment, activity);

//    Robolectric.directlyOn(fragment).onAttach(activity);
//    Robolectric.shadowOf(fragment).setActivity(activity);

    final LoaderManager loaderManager = Robolectric.directlyOn(fragment).getLoaderManager();

    // describe loader
    final LoaderChain chain = LoaderChain.build(getApplication())
        .withManager(loaderManager)

        // load R1
        .withCallbacks(new LoaderChain.ChainCallbacksAdapter<ResponseData<String>>() {
          @Override
          public Loader<ResponseData<String>> onCreateLoader(final int id, final Bundle args) {
            return new SimpleRequestBuilder<String>(getApplication()) { }
              .setFormat("string")
              .getLoader();
          }
        }, 1)

        // load R2, R3
        .withCallbacks(new LoaderChain.ChainCallbacksAdapter<ResponseData<String>>() {
          @Override
          public Loader<ResponseData<String>> onCreateLoader(final int id, final Bundle args) {
            return new SimpleRequestBuilder<String>(getApplication()) { }
              .setFormat("string")
              .getLoader();
          }
        }, 2, 3)

        .create();

    // TODO implement the test

//    loaderManager.initLoader(0, null, new LoaderCallbacks<Object[]>() {
//      @Override
//      public Loader<Object[]> onCreateLoader(final int id, final Bundle args) {
//        return chain;
//      }
//
//      @Override
//      public void onLoadFinished(final Loader<Object[]> loader, final Object[] data) {
//        Log.i("ChainTest", Arrays.toString(data));
//      }
//
//      @Override
//      public void onLoaderReset(final Loader<Object[]> loader) {
//      }
//    });

  }


}

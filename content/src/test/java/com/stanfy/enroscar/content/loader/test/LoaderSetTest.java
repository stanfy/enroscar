package com.stanfy.enroscar.content.loader.test;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.util.FragmentTestUtil;
import org.robolectric.annotation.Config;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;

import com.stanfy.enroscar.content.loader.LoaderSet;

/**
 * Tests for {@link LoaderSet}.
 */
@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18)
public class LoaderSetTest {

  private Loader<String> createLoader(final String data) {
    return new Loader<String>(Robolectric.application) {
      @Override
      protected void onStartLoading() {
        deliverResult(data);
      }
      @Override
      protected void onForceLoad() {
        deliverResult(data);
      }
    };
  };
  
  @Test
  public void shouldCombineAllResults() throws Throwable {

    final Fragment fragment = new Fragment();
    FragmentTestUtil.startFragment(fragment);
    final LoaderManager loaderManager = fragment.getLoaderManager();
    assertThat(loaderManager).isNotNull();

    // describe loader
    final LoaderSet set = LoaderSet.build(Robolectric.application)
        .withManager(loaderManager)

        // load R1
        .withCallbacks(new LoaderSet.SetCallbacksAdapter<String>() {
          @Override
          public Loader<String> onCreateLoader(final int id, final Bundle args) {
            return createLoader("R1");
          }
        }, 1)

        // load R2, R3
        .withCallbacks(new LoaderSet.SetCallbacksAdapter<String>() {
          @Override
          public Loader<String> onCreateLoader(final int id, final Bundle args) {
            return createLoader("R" + id);
          }
        }, 2, 3)

        .create();

    final LoaderSet.LoaderSetCallback callbacks = new LoaderSet.LoaderSetCallback() {
      @Override
      public void onLoadFinished(final Object[] data) {
        Log.i("ChainTest", Arrays.toString(data));
        assertThat(data.length).isEqualTo(3);
        assertThat(((String)data[0])).isEqualTo("R1");
        assertThat(((String)data[1])).isEqualTo("R2");
        assertThat(((String)data[2])).isEqualTo("R3");
      }
    };

    set.init(null, callbacks);

  }
  
}

package com.stanfy.enroscar.rest.loader.test;

import org.junit.Before;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowLooper;

import android.support.v4.content.Loader;
import android.support.v4.content.Loader.OnLoadCompleteListener;

import com.stanfy.enroscar.content.loader.ResponseData;
import com.stanfy.enroscar.net.test.AbstractMockServerTest;
import com.stanfy.enroscar.rest.loader.LoaderAccess;
import com.stanfy.enroscar.rest.loader.RequestBuilderLoader;
import com.stanfy.enroscar.shared.test.EnroscarConfiguration;

/**
 * Base test class for loaders.
 */
@EnroscarConfiguration(connectionEngineRequired = true)
public class AbstractLoaderTest extends AbstractMockServerTest {

  @Before
  public void logging() {
    System.out.println("================================================");
  }
  
  <T> void waitAndAssertForLoader(final Loader<ResponseData<T>> loader, final Asserter<ResponseData<T>> asserter) throws Throwable {
    waitAndAssert(new LoaderWaiter<T>((RequestBuilderLoader<T>)loader, true), asserter);
  }

  <T> void waitForLoader(final Loader<ResponseData<T>> loader) throws Throwable {
    waitAndAssert(new LoaderWaiter<T>((RequestBuilderLoader<T>)loader, false), null);
  }
  
  /** Loader waiter. */
  private class LoaderWaiter<T> implements Waiter<ResponseData<T>> {
    /** Loader. */
    private final RequestBuilderLoader<T> loader;

    /** Loaded data. */
    private ResponseData<T> data;

    public LoaderWaiter(final RequestBuilderLoader<T> loader, final boolean setListener) {
      this.loader = loader;
      if (setListener) {
        System.out.println("Set loader listener");
        loader.registerListener(1, new OnLoadCompleteListener<ResponseData<T>>() {
          @Override
          public void onLoadComplete(final Loader<ResponseData<T>> loader, final ResponseData<T> data) {
            if (data == null) {
              throw new IllegalStateException("null data");
            }
            LoaderWaiter.this.data = data;
          }
        });
      }
    }

    @Override
    public ResponseData<T> waitForData() {
      LoaderAccess.waitForLoader(loader);
      Robolectric.shadowOf(ShadowLooper.getMainLooper()).runToEndOfTasks();
      loader.startLoading(); // force listener to be called at this point
      return data;
    }

  }

}

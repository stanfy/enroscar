package com.stanfy.enroscar.rest.loader.test;

import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowLooper;

import android.support.v4.content.Loader;
import android.support.v4.content.Loader.OnLoadCompleteListener;

import com.stanfy.enroscar.beans.BeansManager;
import com.stanfy.enroscar.beans.BeansManager.Editor;
import com.stanfy.enroscar.content.loader.ResponseData;
import com.stanfy.enroscar.io.BuffersPool;
import com.stanfy.enroscar.net.test.AbstractMockServerTest;
import com.stanfy.enroscar.rest.RemoteServerApiConfiguration;
import com.stanfy.enroscar.rest.loader.LoaderAccess;
import com.stanfy.enroscar.rest.loader.RequestBuilderLoader;

/**
 * Base test class for loaders.
 */
public class AbstractLoaderTest extends AbstractMockServerTest {

  @Override
  protected void configureBeansManager(final Editor editor) {
    super.configureBeansManager(editor);
    editor.put(BuffersPool.class).put(RemoteServerApiConfiguration.class);
  }
  
  void initContentHandler(final String name) {
    BeansManager.get(getApplication()).getContainer().getBean(RemoteServerApiConfiguration.class)
      .setDefaultContentHandlerName(name);
  }
  
  <T> void waitAndAssertForLoader(final Loader<ResponseData<T>> loader, final Asserter<ResponseData<T>> asserter) throws Throwable {
    waitAndAssert(new LoaderWaiter<T>((RequestBuilderLoader<T>)loader), asserter);
  }

  /** Loader waiter. */
  public class LoaderWaiter<T> implements Waiter<ResponseData<T>> {
    /** Loader. */
    private final RequestBuilderLoader<T> loader;

    /** Loaded data. */
    private ResponseData<T> data;

    public LoaderWaiter(final RequestBuilderLoader<T> loader) {
      this.loader = loader;
      loader.registerListener(1, new OnLoadCompleteListener<ResponseData<T>>() {
        @Override
        public void onLoadComplete(final Loader<ResponseData<T>> loader, final ResponseData<T> data) {
          LoaderWaiter.this.data = data;
        }
      });
    }

    @Override
    public ResponseData<T> waitForData() {
      LoaderAccess.waitForLoader(loader);
      Robolectric.shadowOf(ShadowLooper.getMainLooper()).runToEndOfTasks();
      return data;
    }

  }

}

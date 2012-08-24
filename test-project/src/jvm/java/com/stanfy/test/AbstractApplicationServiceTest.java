package com.stanfy.test;

import java.lang.reflect.Field;

import android.support.v4.content.Loader;
import android.support.v4.content.Loader.OnLoadCompleteListener;

import com.stanfy.app.beans.BeansManager.Editor;
import com.stanfy.app.loader.LoaderAccess;
import com.stanfy.app.loader.RbLoaderAccess;
import com.stanfy.app.loader.RequestBuilderLoader;
import com.stanfy.serverapi.response.ResponseData;
import com.stanfy.test.Application.ApplicationService;
import com.xtremelabs.robolectric.Robolectric;

/**
 * Base class for tests that require {@link ApplicationService} communication.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 *
 */
@EnroscarConfiguration(connectionEngineRequired = true)
public abstract class AbstractApplicationServiceTest extends AbstractMockServerTest {

  /** Error. */
  private AssertionError error;

  @Override
  protected void configureBeansManager(final Editor editor) {
    editor.required().remoteServerApi();
  }

  @Override
  protected void whenBeansConfigured() {
    super.whenBeansConfigured();
    final Application application = (Application)Robolectric.application;
    application.setAppServiceInstance(createApplicationService(application));
  }

  protected ApplicationService createApplicationService(final Application app) { return app.new ApplicationService(); }

  protected <T> void waitAndAssertForLoader(final RequestBuilderLoader<T> loader, final Asserter<ResponseData<T>> asserter) {
    waitAndAssert(new LoaderWaiter<T>(loader), asserter);
  }

  protected static <T extends Loader<?>> T directLoaderCall(final T loader) {
    return Robolectric.directlyOnFullStack(initLoader(loader));
  }

  private static <T extends Loader<?>> T initLoader(final T loader) {
    try {
      final Field contextField = Loader.class.getDeclaredField("mContext");
      contextField.setAccessible(true);
      contextField.set(loader, Robolectric.application);

      if (loader instanceof RequestBuilderLoader<?>) {
        RbLoaderAccess.initLoader((RequestBuilderLoader<?>)loader);
      }

      return loader;
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected <T> void waitAndAssert(final Waiter<T> waiter, final Asserter<T> asserter) {
    final Thread checker = new Thread() {
      @Override
      public void run() {
        final T data = waiter.waitForData();
        try {
          asserter.makeAssertions(data);
        } catch (final AssertionError e) {
          error = e;
        } catch (final Exception e) {
          throw new RuntimeException(e);
        }
      }
    };
    checker.setUncaughtExceptionHandler(Thread.currentThread().getUncaughtExceptionHandler());
    checker.start();

    // boilerplate to make posted operations run
    getApplication().getApiMainShadowLooper().runToEndOfTasks();

    try {
      checker.join();
    } catch (final InterruptedException e) {
      throw new RuntimeException(e);
    }
    if (error != null) { throw error; }
  }

  /** Can wait. */
  public interface Waiter<T> {
    T waitForData();
  }
  /** Can make assertions. */
  public interface Asserter<T> {
    void makeAssertions(final T data) throws Exception;
  }

  /** Loader waiter. */
  public class LoaderWaiter<T> implements Waiter<ResponseData<T>> {
    /** Loader. */
    private final RequestBuilderLoader<T> loader;

    /** Loaded data. */
    private ResponseData<T> data;

    public LoaderWaiter(final RequestBuilderLoader<T> loader) {
      this.loader = loader;
    }

    @Override
    public ResponseData<T> waitForData() {
      loader.registerListener(1, new OnLoadCompleteListener<ResponseData<T>>() {
        @Override
        public void onLoadComplete(final Loader<ResponseData<T>> loader, final ResponseData<T> data) {
          LoaderWaiter.this.data = data;
        }
      });
      LoaderAccess.waitForLoader(loader);
      return data;
    }

  }

}

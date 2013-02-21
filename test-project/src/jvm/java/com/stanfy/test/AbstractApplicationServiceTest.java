package com.stanfy.test;

import java.lang.Thread.UncaughtExceptionHandler;

import android.support.v4.content.Loader;
import android.support.v4.content.Loader.OnLoadCompleteListener;

import com.stanfy.app.beans.BeansManager.Editor;
import com.stanfy.app.loader.LoaderAccess;
import com.stanfy.app.loader.RequestBuilderLoader;
import com.stanfy.serverapi.response.ResponseData;
import com.stanfy.test.Application.ApplicationService;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.shadows.ShadowLooper;

/**
 * Base class for tests that require {@link ApplicationService} communication.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 *
 */
@EnroscarConfiguration(connectionEngineRequired = true)
public abstract class AbstractApplicationServiceTest extends AbstractMockServerTest {

  /** Error. */
  private Throwable error;

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

  protected <T> void waitAndAssertForLoader(final Loader<ResponseData<T>> loader, final Asserter<ResponseData<T>> asserter) throws Throwable {
    waitAndAssert(new LoaderWaiter<T>((RequestBuilderLoader<T>)loader), asserter);
  }

  protected <T> void waitAndAssert(final Waiter<T> waiter, final Asserter<T> asserter) throws Throwable {
    final Thread checker = new Thread() {
      @Override
      public void run() {
        final T data = waiter.waitForData();
        try {
          asserter.makeAssertions(data);
        } catch (final Exception e) {
          error = e;
        }
      }
    };
    checker.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
      @Override
      public void uncaughtException(final Thread thread, final Throwable ex) {
        ex.printStackTrace();
        error = ex;
        throw new AssertionError("Exception occured: " + ex.getMessage());
      }
    });
    checker.start();

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
      directLoaderCall(loader).registerListener(1, new OnLoadCompleteListener<ResponseData<T>>() {
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

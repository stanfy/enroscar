package com.stanfy.enroscar.net.test;

import static org.fest.assertions.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowLog;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.Loader;

import com.google.mockwebserver.MockWebServer;
import com.stanfy.enroscar.beans.BeanUtils;
import com.stanfy.enroscar.beans.BeansManager;
import com.stanfy.enroscar.beans.BeansManager.Editor;
import com.stanfy.enroscar.content.loader.ResponseData;
import com.stanfy.enroscar.io.BuffersPool;
import com.stanfy.enroscar.io.IoUtils;
import com.stanfy.enroscar.net.EnroscarConnectionsEngine;
import com.stanfy.enroscar.net.EnroscarConnectionsEngine.Config;
import com.stanfy.enroscar.net.EnroscarConnectionsEngineMode;
import com.stanfy.enroscar.net.UrlConnectionWrapper;
import com.stanfy.enroscar.rest.RemoteServerApiConfiguration;
import com.stanfy.enroscar.rest.RequestMethod;
import com.stanfy.enroscar.rest.executor.ApiMethods;
import com.stanfy.enroscar.rest.executor.ApplicationService;
import com.stanfy.enroscar.rest.loader.LoadMoreListLoader;
import com.stanfy.enroscar.rest.loader.RequestBuilderLoader;
import com.stanfy.enroscar.rest.request.ListRequestBuilderWrapper;
import com.stanfy.enroscar.rest.request.RequestBuilder;
import com.stanfy.enroscar.rest.request.RequestDescription;
import com.stanfy.enroscar.rest.request.SimpleRequestBuilder;
import com.stanfy.enroscar.shared.test.AbstractEnroscarTest;
import com.stanfy.enroscar.shared.test.EnroscarConfiguration;

/**
 * Mock server test.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
@RunWith(RobolectricTestRunner.class)
public abstract class AbstractMockServerTest extends AbstractEnroscarTest {

  /** Straight resolver.  */
  private static final StreamResolver STRAIGHT_RESOLVER = new StreamResolver() {
    @Override
    public InputStream getStream(final URLConnection connection) throws IOException {
      return IoUtils.getUncompressedInputStream(connection);
    }
  };

  /** Error. */
  private Throwable error;

  /** Configuration. */
  private EnroscarConfiguration config;

  /** Web server. */
  private MockWebServer webServer;

  /** Requests counter. */
  private int requestsCounter = 0;

  public MockWebServer getWebServer() { return webServer; }

  @Before
  public void configureLogs() {
    ShadowLog.stream = System.out;
  }
  
  /**
   * @param rb request builder instance
   * @return {@link URLConnection} instance
   * @throws Exception if error happens
   */
  protected static URLConnection makeConnection(final RequestBuilder<?> rb) throws Exception {
    return ((MyRequestBuilder<?>)rb).getResult().makeConnection(Robolectric.application);
  }

  /**
   * @param connection {@link URLConnection} instance
   * @return response as string
   * @throws IOException if error happens
   */
  protected static String read(final URLConnection connection) throws IOException {
    return IoUtils.streamToString(connection.getInputStream());
  }

  @Override
  protected void configureBeansManager(final Editor editor) {
    super.configureBeansManager(editor);
    editor.put(BuffersPool.class).put(RemoteServerApiConfiguration.class);
  }
  
  /**
   * @param name default content handler name.
   */
  protected void initContentHandler(final String name) {
    BeansManager.get(getApplication()).getContainer().getBean(RemoteServerApiConfiguration.class)
      .setDefaultContentHandlerName(name);
  }
  
  @Override
  protected void whenBeansConfigured() {
    super.whenBeansConfigured();
    BeansManager.get(getApplication()).getContainer().getBean(RemoteServerApiConfiguration.class).setDefaultRequestMethod(new RequestMethod() {
      @Override
      protected void before(final Context systemContext, final RequestDescription description) {
        // do not use TrafficStats
      }
      @Override
      protected void after(final Context systemContext, final RequestDescription description) {
        // do not use TrafficStats
      }
    });
    
    if (config == null) {
      EnroscarConnectionsEngineMode.testMode();
      config = BeanUtils.getAnnotationFromHierarchy(getClass(), EnroscarConfiguration.class);
    }
    if (config != null && config.connectionEngineRequired()) {
      final Config config = EnroscarConnectionsEngine.config();
      configureConnectionsEngine(config);
      config.install(Robolectric.application);
    }

    configureServiceBind();
  }

  private void configureServiceBind() {
    ShadowApplication app = Robolectric.shadowOf(getApplication());
    ApplicationService service = new ApplicationService();
    service.onCreate();
    app.setComponentNameAndServiceForBindService(
        new ComponentName(ApplicationService.class.getPackage().getName(), ApplicationService.class.getSimpleName()),
        service.onBind(new Intent().setAction(ApiMethods.class.getName()))
    );
  }
  
  /**
   * Configure connections engine. Called immediately after beans are initialized. 
   * @param config configuration instance.
   */
  protected void configureConnectionsEngine(final EnroscarConnectionsEngine.Config config) {
    // nothing
  }

  @After
  public final void shudownConnectionsEngine() {
    EnroscarConnectionsEngine.uninstall();
  }
  
  @Before
  public void runServer() throws IOException {
    webServer = new MockWebServer();
    webServer.play();
  }

  @After
  public void shutDownServer() throws IOException, InterruptedException {
    webServer.shutdown();
  }

  @Before
  public void resetCounters() {
    requestsCounter = 0;
  }

  private void doAssertResponse(final URLConnection connection, final String expectedResponse, final boolean cached, final StreamResolver resolver) throws IOException {
    final URLConnection realConnection = UrlConnectionWrapper.unwrap(connection);
    assertThat(realConnection).isInstanceOf(HttpURLConnection.class);

    final HttpURLConnection http = (HttpURLConnection)realConnection;
    final String response = IoUtils.streamToString(resolver.getStream(connection));
    assertThat(response).isEqualTo(expectedResponse);
    assertThat(http.getResponseCode()).isEqualTo(HttpURLConnection.HTTP_OK);

    // real request has been performed or not
    if (!cached) {
      ++requestsCounter;
    }
    assertThat(getWebServer().getRequestCount()).isEqualTo(requestsCounter);
  }

  /**
   * @param connection connection to read stream from
   * @param expectedResponse expected response string
   * @param cached whether response should be cached
   * @throws IOException if error happens
   */
  protected void assertResponse(final URLConnection connection, final String expectedResponse, final boolean cached) throws IOException {
    doAssertResponse(connection, expectedResponse, cached, STRAIGHT_RESOLVER);
  }

  public <T> void waitAndAssert(final Waiter<T> waiter, final Asserter<T> asserter) throws Throwable {
    final Thread checker = new Thread() {
      @Override
      public void run() {
        final T data = waiter.waitForData();
        if (asserter != null) {
          try {
            asserter.makeAssertions(data);
          } catch (final Exception e) {
            error = e;
          }
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

    final boolean[] res = new boolean[1];

//    final Thread current = Thread.currentThread();
//    Thread watchdog = new Thread("watchdog") {
//      @Override
//      public void run() {
//        try {
//          final long max = 5000;
//          Thread.sleep(max);
//          if (!res[0]) {
//            current.interrupt();
//            checker.interrupt();
//          }
//        // CHECKSTYLE:OFF
//        } catch (InterruptedException e) {
//          // ignore
//        }
//        // CHECKSTYLE:ON
//      };
//    };
//    watchdog.start();
    
    try {
      checker.join();
      res[0] = true;
//      watchdog.interrupt();
    } catch (final InterruptedException e) {
      throw new RuntimeException(e);
    }
    if (error != null) { throw error; }
    if (!res[0]) { throw new AssertionError("Too long"); }
  }

  /** Stream resolver. */
  private interface StreamResolver {
    InputStream getStream(final URLConnection connection) throws IOException;
  }

  /** Can wait. */
  public interface Waiter<T> {
    T waitForData();
  }
  /** Can make assertions. */
  public interface Asserter<T> {
    void makeAssertions(final T data) throws Exception;
  }

  /**
   * Test request builder.
   */
  public static class MyRequestBuilder<MT> extends SimpleRequestBuilder<MT> {

    public MyRequestBuilder(final Context context) {
      super(context);
    }

    @Override
    public RequestDescription getResult() { return super.getResult(); }

    @Override
    public Loader<ResponseData<MT>> getLoader() {
      return new RequestBuilderLoader<MT>(this) {
        @Override
        protected void deliverDispatchCallback(final Runnable dispatcher) {
          System.out.println("Dispatch service results");
          dispatcher.run();
        }
      };
    }
    
    @Override
    protected <T, LT extends List<T>> ListRequestBuilderWrapper<LT, T> createLoadMoreListWrapper() {
      final MyRequestBuilderListWrapper<LT, T> wrapper = new MyRequestBuilderListWrapper<LT, T>(this);
      return wrapper;
    }

  }

  /** Test request builder wrapper. */
  public static final class MyRequestBuilderListWrapper<LT extends List<MT>, MT> extends ListRequestBuilderWrapper<LT, MT> {

    public MyRequestBuilderListWrapper(final MyRequestBuilder<?> core) {
      super(core);
    }

    @Override
    public LoadMoreListLoader<MT, LT> getLoader() {
      return new LoadMoreListLoader<MT, LT>(this) {
        @Override
        protected void deliverDispatchCallback(final Runnable dispatcher) {
          dispatcher.run();
        }
      };
    }

  }

}

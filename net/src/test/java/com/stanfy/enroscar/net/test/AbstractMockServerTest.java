package com.stanfy.enroscar.net.test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URLConnection;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import android.content.Context;

import com.google.mockwebserver.MockWebServer;
import com.stanfy.enroscar.beans.BeanUtils;
import com.stanfy.enroscar.io.IoUtils;
import com.stanfy.enroscar.net.EnroscarConnectionsEngine;
import com.stanfy.enroscar.net.EnroscarConnectionsEngine.Config;
import com.stanfy.enroscar.net.EnroscarConnectionsEngineMode;
import com.stanfy.enroscar.net.UrlConnectionWrapper;
import com.stanfy.enroscar.rest.request.RequestBuilder;
import com.stanfy.enroscar.rest.request.RequestDescription;
import com.stanfy.enroscar.rest.request.SimpleRequestBuilder;
import com.stanfy.enroscar.shared.test.AbstractEnroscarTest;
import com.stanfy.enroscar.shared.test.EnroscarConfiguration;

/**
 * Mock server test.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
@RunWith(Runner.class)
public abstract class AbstractMockServerTest extends AbstractEnroscarTest {

  /** Straight resolver.  */
  private static final StreamResolver STRAIGHT_RESOLVER = new StreamResolver() {
    @Override
    public InputStream getStream(final URLConnection connection) throws IOException {
      return IoUtils.getUncompressedInputStream(connection);
    }
  };

  /** Configuration. */
  private EnroscarConfiguration config;

  /** Web server. */
  private MockWebServer webServer;

  /** Requests counter. */
  private int requestsCounter = 0;

  public MockWebServer getWebServer() { return webServer; }

//  protected static <T extends Loader<?>> T directLoaderCall(final T loader) {
//    return Robolectric.directlyOnFullStack(FullStackDirectCallPolicy.build(initLoader(loader)).include(Arrays.asList("android.support.v4")));
//  }
//
//  protected static <T extends Loader<?>> T initLoader(final T loader) {
//    try {
//      final Field contextField = Loader.class.getDeclaredField("mContext");
//      contextField.setAccessible(true);
//      contextField.set(loader, Robolectric.application);
//
//      if (loader instanceof RequestBuilderLoader<?>) {
//        RbLoaderAccess.initLoader((RequestBuilderLoader<?>)loader);
//      }
//
//      return loader;
//    } catch (final Exception e) {
//      throw new RuntimeException(e);
//    }
//  }

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
  protected void whenBeansConfigured() {
    super.whenBeansConfigured();
    if (config == null) {
      EnroscarConnectionsEngineMode.testMode();
      config = BeanUtils.getAnnotationFromHierarchy(getClass(), EnroscarConfiguration.class);
    }
    if (config != null && config.connectionEngineRequired()) {
      final Config config = EnroscarConnectionsEngine.config();
      configureConnectionsEngine(config);
      config.install(Robolectric.application);
    }

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
  public void shutDownServer() throws IOException {
    webServer.shutdown();
  }

  @Before
  public void resetCounters() {
    requestsCounter = 0;
  }

  private void doAssertResponse(final URLConnection connection, final String expectedResponse, final boolean cached, final StreamResolver resolver) throws IOException {
    final URLConnection realConnection = UrlConnectionWrapper.unwrap(connection);
    assertThat(realConnection, instanceOf(HttpURLConnection.class));

    final HttpURLConnection http = (HttpURLConnection)realConnection;
    final String response = IoUtils.streamToString(resolver.getStream(connection));
    assertThat(response, equalTo(expectedResponse));
    assertThat(http.getResponseCode(), equalTo(cached ? -1 : HttpURLConnection.HTTP_OK));

    // real request has been performed or not
    if (!cached) {
      ++requestsCounter;
    }
    assertThat(getWebServer().getRequestCount(), equalTo(requestsCounter));
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

  /** Stream resolver. */
  private interface StreamResolver {
    InputStream getStream(final URLConnection connection) throws IOException;
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

//    @Override
//    public RequestBuilderLoader<MT> getLoader() {
//      return (RequestBuilderLoader<MT>) (startedLoader ? new StartedLoader<MT>(this) : super.getLoader());
//    }
//
//    @Override
//    protected <T, LT extends List<T>> ListRequestBuilderWrapper<LT, T> createLoadMoreListWrapper() {
//      if (!startedLoader) { return super.createLoadMoreListWrapper(); }
//      final MyRequestBuilderListWrapper<LT, T> wrapper = new MyRequestBuilderListWrapper<LT, T>(this);
//      return wrapper;
//    }

  }

//  /** Test request builder wrapper. */
//  public static final class MyRequestBuilderListWrapper<LT extends List<MT>, MT> extends ListRequestBuilderWrapper<LT, MT> {
//
//    public MyRequestBuilderListWrapper(final MyRequestBuilder<?> core) {
//      super(core);
//    }
//
//    @Override
//    public LoadMoreListLoader<MT, LT> getLoader() {
//      return new StartedLoadmoreLoader<MT, LT>(this);
//    }
//
//  }
//
//  /** Started loader. */
//  public static class StartedLoader<MT> extends RequestBuilderLoader<MT> {
//
//    public StartedLoader(final RequestBuilder<MT> requestBuilder) {
//      super(requestBuilder);
//    }
//
//    @Override
//    public boolean isReset() { return false; }
//    @Override
//    public boolean isStarted() { return true; }
//    @Override
//    public boolean isAbandoned() { return false; }
//
//    @Override
//    public void deliverResult(final ResponseData<MT> data) {
//      directLoaderCall(this);
//      super.deliverResult(data);
//    }
//
//  }
//
//  /** Started loader. */
//  public static class StartedLoadmoreLoader<MT, LT extends List<MT>> extends LoadMoreListLoader<MT, LT> {
//
//    public StartedLoadmoreLoader(final ListRequestBuilder<LT, MT> requestBuilder) {
//      super(requestBuilder);
//    }
//
//    @Override
//    public boolean isReset() { return false; }
//    @Override
//    public boolean isStarted() { return true; }
//    @Override
//    public boolean isAbandoned() { return false; }
//
//    @Override
//    public void deliverResult(final ResponseData<LT> data) {
//      directLoaderCall(this);
//      super.deliverResult(data);
//    }
//
//  }

}

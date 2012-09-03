package com.stanfy.test;

import static org.hamcrest.Matchers.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;

import android.content.Context;
import android.support.v4.content.Loader;

import com.google.mockwebserver.MockWebServer;
import com.stanfy.app.loader.LoadMoreListLoader;
import com.stanfy.app.loader.RbLoaderAccess;
import com.stanfy.app.loader.RequestBuilderLoader;
import com.stanfy.io.IoUtils;
import com.stanfy.net.UrlConnectionWrapper;
import com.stanfy.serverapi.request.ListRequestBuilder;
import com.stanfy.serverapi.request.ListRequestBuilderWrapper;
import com.stanfy.serverapi.request.RequestBuilder;
import com.stanfy.serverapi.request.RequestDescription;
import com.stanfy.serverapi.request.SimpleRequestBuilder;
import com.stanfy.serverapi.response.ResponseData;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.bytecode.DirectCallPolicy.FullStackDirectCallPolicy;

/**
 * Mock server test.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public abstract class AbstractMockServerTest extends AbstractEnroscarTest {

  /** Web server. */
  private MockWebServer webServer;

  public MockWebServer getWebServer() { return webServer; }

  /** Straight resolver.  */
  private static final StreamResolver STRAIGHT_RESOLVER = new StreamResolver() {
    @Override
    public InputStream getStream(final URLConnection connection) throws IOException {
      return IoUtils.getUncompressedInputStream(connection);
    }
  };

  /** Requests counter. */
  private int requestsCounter = 0;

  protected static <T extends Loader<?>> T directLoaderCall(final T loader) {
    return Robolectric.directlyOnFullStack(FullStackDirectCallPolicy.build(initLoader(loader)).include(Arrays.asList("android.support.v4")));
  }

  protected static <T extends Loader<?>> T initLoader(final T loader) {
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

  protected static URLConnection makeConnection(final RequestBuilder<?> rb) throws Exception {
    return ((MyRequestBuilder<?>)rb).getResult().makeConnection(Robolectric.application);
  }

  protected static String read(final URLConnection connection) throws IOException {
    return IoUtils.streamToString(connection.getInputStream());
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

  @SuppressWarnings("unchecked")
  private void doAssertResponse(final URLConnection connection, final String expectedResponse, final boolean cached, final StreamResolver resolver) throws IOException {
    final URLConnection realConnection = UrlConnectionWrapper.unwrap(connection);
    assertThat(realConnection, instanceOf(HttpURLConnection.class));

    final HttpURLConnection http = (HttpURLConnection)realConnection;
    final String response = IoUtils.streamToString(resolver.getStream(connection));
    assertThat(response, equalTo(expectedResponse));
    assertThat(http.getResponseCode(), anyOf(equalTo(HttpURLConnection.HTTP_OK), equalTo(-1)));

    // real request has been performed or not
    if (!cached) {
      ++requestsCounter;
    }
    assertThat(getWebServer().getRequestCount(), equalTo(requestsCounter));
  }

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

    /** Started loader option. */
    private boolean startedLoader = false;

    public MyRequestBuilder(final Context context) {
      super(context);
    }

    @Override
    public RequestDescription getResult() { return super.getResult(); }

    public MyRequestBuilder<MT> setStartedLoader(final boolean startedLoader) {
      this.startedLoader = startedLoader;
      return this;
    }

    @Override
    public RequestBuilderLoader<MT> getLoader() {
      return startedLoader ? new StartedLoader<MT>(this) : super.getLoader();
    }

    @Override
    protected <T, LT extends List<T>> ListRequestBuilderWrapper<LT, T> createLoadMoreListWrapper() {
      if (!startedLoader) { return super.createLoadMoreListWrapper(); }
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
      return new StartedLoadmoreLoader<MT, LT>(this);
    }

  }

  /** Started loader. */
  public static class StartedLoader<MT> extends RequestBuilderLoader<MT> {

    public StartedLoader(final RequestBuilder<MT> requestBuilder) {
      super(requestBuilder);
    }

    @Override
    public boolean isReset() { return false; }
    @Override
    public boolean isStarted() { return true; }
    @Override
    public boolean isAbandoned() { return false; }

    @Override
    public void deliverResult(final ResponseData<MT> data) {
      directLoaderCall(this);
      super.deliverResult(data);
    }

  }

  /** Started loader. */
  public static class StartedLoadmoreLoader<MT, LT extends List<MT>> extends LoadMoreListLoader<MT, LT> {

    public StartedLoadmoreLoader(final ListRequestBuilder<LT, MT> requestBuilder) {
      super(requestBuilder);
    }

    @Override
    public boolean isReset() { return false; }
    @Override
    public boolean isStarted() { return true; }
    @Override
    public boolean isAbandoned() { return false; }

    @Override
    public void deliverResult(final ResponseData<LT> data) {
      directLoaderCall(this);
      super.deliverResult(data);
    }

  }

}

package com.stanfy.test;

import static org.hamcrest.Matchers.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URLConnection;

import org.junit.After;
import org.junit.Before;

import com.google.mockwebserver.MockWebServer;
import com.stanfy.io.IoUtils;
import com.stanfy.net.UrlConnectionWrapper;

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

}

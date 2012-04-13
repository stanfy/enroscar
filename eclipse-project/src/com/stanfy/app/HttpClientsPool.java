package com.stanfy.app;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthState;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.util.Log;

import com.stanfy.DebugFlags;
import com.stanfy.Destroyable;
import com.stanfy.utils.Base64;
import com.stanfy.utils.Time;

/**
 * Pool of HTTP clients.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class HttpClientsPool implements Destroyable {

  /** Logging tag. */
  private static final String TAG = "HTTPPool";

  /** Debug flag. */
  private static final boolean DEBUG = DebugFlags.DEBUG_UTILS;

  /** Some headers. */
  private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding",
                              HEADER_AUTH = "Authorization";
  /** Encoding. */
  private static final String ENCODING_GZIP = "gzip";

  /** HTTP port. */
  public static final int PORT_HTTP = 80, PORT_HTTPS = 443;
  /** Max count of simultaneous connections. */
  public static final int DEFAULT_CONNECTIONS_COUNT = 5;

  /** Pool of http clients. */
  private final LinkedList<HttpClient> httpClients = new LinkedList<HttpClient>();

  /** Context reference. */
  private final WeakReference<Context> contextRef;

  /** User agent string. */
  private String userAgent;

  /** Cookie store. */
  private CookieStore cookieStore = null;

  /**
   * Create a pool.
   * @param context context instance
   */
  public HttpClientsPool(final Context context) {
    if (DEBUG) { Log.d(TAG, "HTTP pool created"); }
    this.contextRef = new WeakReference<Context>(context);
  }

  /**
   * Flush resources.
   */
  public void flush() {
    synchronized (httpClients) {
      for (final HttpClient client : httpClients) {
        client.getConnectionManager().shutdown();
      }
      httpClients.clear();
    }
  }

  /**
   * @return instance of HTTP client
   */
  public HttpClient getHttpClient() {
    synchronized (httpClients) {
      if (DEBUG) { Log.d(TAG, "Http pool size (before get): " + httpClients.size()); }
      return addCookieNature(httpClients.isEmpty() ? createHttpClient(contextRef.get()) : httpClients.removeFirst());
    }
  }

  /**
   * @param httpClient HTTP client to be recycled
   */
  public void releaseHttpClient(final HttpClient httpClient) {
    synchronized (httpClients) {
      if (httpClient == null || httpClients.contains(httpClient)) { return; }
      httpClients.addLast(httpClient);
      if (DEBUG) { Log.d(TAG, "Http pool size (after release): " + httpClients.size()); }
    }
  }

  /**
   * @param context context instance
   * @return user agent string
   */
  protected String buildUserAgent(final Context context) {
    if (context == null) { return null; }
    try {
      final PackageManager manager = context.getPackageManager();
      final PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
      return "Android client (" + Build.VERSION.RELEASE + " / api" + Build.VERSION.SDK_INT + "), "
          + info.packageName + "/" + info.versionName + " (" + info.versionCode + ") (" + ENCODING_GZIP + ")";
    } catch (final NameNotFoundException e) {
      return null;
    }
  }

  private String getUserAgentString(final Context context) {
    if (userAgent == null) {
      userAgent = buildUserAgent(context);
    }
    return userAgent;
  }

  private HttpClient addCookieNature(final HttpClient client) {
    if (isCookieSupported()) {
      if (client != null && client instanceof AbstractHttpClient) {
        final AbstractHttpClient c = (AbstractHttpClient) client;
        if (cookieStore == null) {
          cookieStore = new BasicCookieStore();
          if (DEBUG) { Log.d(TAG, "Created new cookie store"); }
        } else {
          if (DEBUG) { Log.d(TAG, "Reusing cookie store"); }
          c.setCookieStore(cookieStore);
        }
      } else {
        Log.w(TAG, "Strange problem with client: " + client);
      }
    }
    return client;
  }

  /** @return basic authentication info */
  protected BasicAuthInfo getBasicAuthInfo() { return null; }
  /** @return is cookie storage supported */
  protected boolean isCookieSupported() { return false; }

  /**
   * Setup timeouts, buffer size, user agent string, HTTP protocol version, etc.
   * @param context context instance
   * @param params {@link HttpParams} instance that should be configured
   * @see HttpConnectionParams
   * @see HttpProtocolParams
   */
  protected void configureHttpParameters(final Context context, final HttpParams params) {
    // Use generous timeouts for slow mobile networks
    final int timeout = 20;
    HttpConnectionParams.setConnectionTimeout(params, timeout * Time.SECONDS);
    HttpConnectionParams.setSoTimeout(params, timeout * Time.SECONDS);
    final int bufferSize = 8192;
    HttpConnectionParams.setSocketBufferSize(params, bufferSize);

    HttpProtocolParams.setUserAgent(params, getUserAgentString(context));
    HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);

    ConnManagerParams.setMaxTotalConnections(params, DEFAULT_CONNECTIONS_COUNT);
    ConnManagerParams.setMaxConnectionsPerRoute(params, new ConnPerRouteBean(DEFAULT_CONNECTIONS_COUNT));
  }

  /**
   * @param context context instance
   * @param params {@link HttpParams} instance configured with {@link #configureHttpParameters(Context, HttpParams)}
   * @return client connection manager
   */
  protected ClientConnectionManager createClientConnectionManager(final Context context, final HttpParams params) {
    final SchemeRegistry schemeRegistry = new SchemeRegistry();
    schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), PORT_HTTP));
    schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), PORT_HTTPS));
    final ThreadSafeClientConnManager connectionsManager = new ThreadSafeClientConnManager(params, schemeRegistry);
    return connectionsManager;
  }

  /**
   * This implementation adds gzip and basic authentication support.
   * @param context context instance
   * @return new instance of HTTP client
   */
  protected HttpClient createHttpClient(final Context context) {
    if (context == null) {
      Log.w(TAG, "Null context");
      return null;
    }
    final HttpParams params = new BasicHttpParams();

    configureHttpParameters(context, params);

    final DefaultHttpClient client = new DefaultHttpClient(createClientConnectionManager(context, params), params);

    final EnroscarInterceptor interceptor = new EnroscarInterceptor(getBasicAuthInfo());
    client.addRequestInterceptor(interceptor);
    client.addResponseInterceptor(interceptor);
    return client;
  }

  @Override
  public void destroy() {
    flush();
    synchronized (httpClients) {
      if (cookieStore != null) {
        cookieStore.clear();
        cookieStore = null;
      }
    }
    if (DEBUG) { Log.d(TAG, "Http clients pool is destroyed"); }
  }

  /**
   * This interceptor keeps track on gzip usage and basic authentication setup.
   * @author Roman Mazur (Stanfy - http://www.stanfy.com)
   */
  public static class EnroscarInterceptor implements HttpRequestInterceptor, HttpResponseInterceptor {

    /** Context attribute name. */
    protected static final String GZIP_USED = "enroscarGzipUsed";

    /** Auth info. */
    private final BasicAuthInfo authInfo;

    /**
     * @param entity entity instance
     * @param context context instance
     */
    public static void ensureClosedStreams(final HttpEntity entity, final HttpContext context) {
      if (context.getAttribute(GZIP_USED) != null) {
        GzipInflatingEntity.ensureContentClosed(entity);
      }
    }

    public EnroscarInterceptor(final BasicAuthInfo info) { authInfo = info; }

    @Override
    public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
      // Add header to accept gzip content
      if (!request.containsHeader(HEADER_ACCEPT_ENCODING)) {
        request.addHeader(HEADER_ACCEPT_ENCODING, ENCODING_GZIP);
      }
      // add authorization
      if (authInfo != null) {
        final AuthState authState = (AuthState)context.getAttribute(ClientContext.TARGET_AUTH_STATE);
        final HttpHost targetHost = (HttpHost)context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
        if (authState.getAuthScheme() == null && authInfo.host.equals(targetHost.getHostName())) {
          final String creds = "Basic " + Base64.encodeBytes((authInfo.login + ":" + authInfo.password).getBytes());
          request.addHeader(HEADER_AUTH, creds);
        }
      }
    }

    @Override
    public void process(final HttpResponse response, final HttpContext context) throws HttpException, IOException {
      // Inflate any responses compressed with gzip
      final HttpEntity entity = response.getEntity();
      final Header encoding = entity.getContentEncoding();
      if (encoding != null) {
        for (final HeaderElement element : encoding.getElements()) {
          if (element.getName().equalsIgnoreCase(ENCODING_GZIP)) {
            context.setAttribute(GZIP_USED, Boolean.TRUE);
            response.setEntity(new GzipInflatingEntity(response.getEntity()));
            break;
          }
        }
      }
    }
  }

  /**
   * Simple {@link HttpEntityWrapper} that inflates the wrapped
   * {@link HttpEntity} by passing it through {@link GZIPInputStream}.
   */
  public static class GzipInflatingEntity extends HttpEntityWrapper {

    /** Close marker for {@link #writeTo(OutputStream)} method. */
    private static final OutputStream CLOSE_MARKER = new OutputStream() {
      @Override
      public void write(final int oneByte) throws IOException { /* nothing */ }
    };

    static void ensureContentClosed(final HttpEntity entity) {
      try {
        entity.writeTo(CLOSE_MARKER);
      } catch (final IOException e) {
        Log.e(TAG, "ensureContentClosed(entity) failed", e);
      }
    }

    /** Content stream instance. */
    private InputStream contentStream;

    public GzipInflatingEntity(final HttpEntity wrapped) {
      super(wrapped);
    }

    @Override
    public InputStream getContent() throws IOException {
      contentStream = new GZIPInputStream(wrappedEntity.getContent());
      return contentStream;
    }

    @Override
    public long getContentLength() {
      return -1;
    }

    @Override
    public void writeTo(final OutputStream outstream) throws IOException {
      if (outstream == CLOSE_MARKER) {
        final InputStream myStream = this.contentStream;
        if (myStream != null) { myStream.close(); }
      } else {
        super.writeTo(outstream);
      }
    }

  }

  /**
   * Information required for basic authentication.
   * @author Roman Mazur (Stanfy - http://www.stanfy.com)
   */
  public static class BasicAuthInfo {
    /** Properties. */
    final String host, login, password;

    public BasicAuthInfo(final String host, final String login, final String password) {
      this.host = host;
      this.login = login;
      this.password = password;
    }
  }

}

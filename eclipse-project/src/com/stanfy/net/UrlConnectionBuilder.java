package com.stanfy.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import android.net.Uri;

import com.stanfy.net.cache.CacheControlUrlConnection;
import com.stanfy.serverapi.response.ModelTypeToken;
import com.stanfy.utils.Time;

/**
 * Builder for creating URL connections.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 * @author Michael Pustovit (Stanfy - http://www.stanfy.com) (proxy methods)
 */
public class UrlConnectionBuilder {

  /** Default connection timeout. */
  public static final int TIMEOUT_CONNECTION_DEFAULT = 30 * (int)Time.SECONDS;
  /** Default read timeout. */
  public static final int TIMEOUT_READ_DEFAULT = 30 * (int)Time.SECONDS;

  /** URL to create a connection. */
  private URL url;

  /** Connection timeout. */
  private final int connectTimeout = TIMEOUT_CONNECTION_DEFAULT;
  /** Read timeout. */
  private final int readTimeout = TIMEOUT_READ_DEFAULT;

  /** Cache manager name. */
  private String cacheManagerName;
  /** Content handler name. */
  private String contentHandlerName;
  /** Model type. */
  private ModelTypeToken modelType;

  /** SSL socket factory. */
  private SSLSocketFactory sslSF;

  /** Proxy host. */
  private String proxyHost;
  
  /** Proxy port. */
  private int proxyPort;
  
  public UrlConnectionBuilder setUrl(final URL url) {
    this.url = url;
    return this;
  }

  public UrlConnectionBuilder setUrl(final String url) {
    try {
      this.url = new URL(url);
    } catch (final MalformedURLException e) {
      throw new IllegalArgumentException("Bad URL string: " + url, e);
    }
    return this;
  }

  public UrlConnectionBuilder setUrl(final Uri url) {
    return this.setUrl(url.toString());
  }

  public UrlConnectionBuilder setCacheManagerName(final String cacheManagerName) {
    this.cacheManagerName = cacheManagerName;
    return this;
  }

  public UrlConnectionBuilder setContentHandlerName(final String contentHandlerName) {
    this.contentHandlerName = contentHandlerName;
    return this;
  }

  public UrlConnectionBuilder setModelType(final ModelTypeToken modelType) {
    this.modelType = modelType;
    return this;
  }

  public UrlConnectionBuilder setSslSocketFactory(final SSLSocketFactory factory) {
    this.sslSF = factory;
    return this;
  }

  public UrlConnectionBuilder setProxy(final String proxyHost, final int proxyPort) {
    this.proxyHost = proxyHost;
    this.proxyPort = proxyPort;
    
    return this;
  }
  
  /**
   * @return the proxy host (null if proxy isn't used)
   */
  public String getProxyHost() {
    return proxyHost;
  }
  
  /**
   * @return the proxy port
   */
  public int getProxyPort() {
    return proxyPort;
  }
  
  /**
   * **WARNING: this implementation won't work correctly in case HTTPS throw HTTP-proxy**.
   * @param url the URL of resource to which we make connection
   * @return the opened URLConnection
   * @throws IOException during connection opening some IOException can be raised
   */
  protected URLConnection openConnection(final URL url) throws IOException {
    URLConnection connection;
    
    if (proxyHost != null) {
      final Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
      connection = url.openConnection(proxy);
    } else {
      connection = url.openConnection();
    }
    
    return connection;
  }

  /**
   * **WARNING: this implementation won't work correctly in case HTTPS throw HTTP-proxy**.
   * @param connection the URLConnection for which we try to enable cache
   * @return connection with right tuned cache
   */
  protected URLConnection prepareCache(final URLConnection connection) {
    if (cacheManagerName != null) {
      connection.setUseCaches(true); // core
      final URLConnection wrappedConnection = new CacheControlUrlConnection(connection, cacheManagerName);
      wrappedConnection.setUseCaches(true); // wrapper
      return wrappedConnection;
    } else {
      return connection;
    }
  }
  
  public URLConnection create() throws IOException {
    // open connection
    URLConnection connection = openConnection(url);
    
    // SSL
    if (sslSF != null && connection instanceof HttpsURLConnection) {
      ((HttpsURLConnection) connection).setSSLSocketFactory(sslSF);
    }

    // cache
    connection = prepareCache(connection);

    // content handler
    if (contentHandlerName != null || modelType != null) {
      final ContentControlUrlConnection control = new ContentControlUrlConnection(connection);
      control.setModelType(modelType);
      control.setContentHandlerName(contentHandlerName);
      connection = control;
    }

    // timeouts
    connection.setConnectTimeout(connectTimeout);
    connection.setReadTimeout(readTimeout);
    return connection;
  }

}

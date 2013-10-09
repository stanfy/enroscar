package com.stanfy.enroscar.net;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import android.net.Uri;

import com.stanfy.enroscar.net.cache.CacheControlUrlConnection;
import com.stanfy.enroscar.rest.ModelTypeToken;
import com.stanfy.enroscar.utils.Time;

/**
 * Builder for creating URL connections.
 * 
 * **WARNING: this implementation won't work correctly in case HTTPS through HTTP-proxy**.
 * 
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
  private int connectTimeout = TIMEOUT_CONNECTION_DEFAULT;
  /** Read timeout. */
  private int readTimeout = TIMEOUT_READ_DEFAULT;

  /** Cache manager name. */
  private String cacheManagerName;
  /** Content handler name. */
  private String contentHandlerName;
  /** Model type. */
  private ModelTypeToken modelType;

  /** SSL socket factory. */
  private SSLSocketFactory sslSF;

  /** Proxy object. */
  private Proxy proxy;
  
  public UrlConnectionBuilder setUrl(final URL url) {
    this.url = url;
    return this;
  }

  /**
   * @param connectTimeout the connect timeout
   * @return instance for queuing
   */
  public UrlConnectionBuilder setConnectTimeout(final int connectTimeout) {
    this.connectTimeout = connectTimeout;
    return this;
  }
  
  /**
   * @param readTimeout the read timeout
   * @return instance for queuing
   */
  public UrlConnectionBuilder setReadTimeout(final int readTimeout) {
    this.readTimeout = readTimeout;
    return this;
  }
  
  /**
   * @return the connect timeout
   */
  public int getConnectTimeout() {
    return connectTimeout;
  }
  
  /**
   * @return the read timeout
   */
  public int getReadTimeout() {
    return readTimeout;
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

  /**
   * @param proxy the proxy
   * @return instance for queing
   */
  public UrlConnectionBuilder setProxy(final Proxy proxy) {
    this.proxy = proxy;
    return this;
  }
  
  /**
   * @return the connection proxy (null if proxy isn'used)
   */
  public Proxy getProxy() {
    return proxy;
  }
  
  /**
   * @param url the URL of resource to which we make connection
   * @return the opened URLConnection
   * @throws IOException during connection opening some IOException can be raised
   */
  protected URLConnection openConnection(final URL url) throws IOException {
    URLConnection connection;
    
    if (proxy != null) {
      connection = url.openConnection(proxy);
    } else {
      connection = url.openConnection();
    }
    
    return connection;
  }

  /**
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

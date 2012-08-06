package com.stanfy.net;

import java.io.IOException;
import java.net.MalformedURLException;
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

  public URLConnection create() throws IOException {
    URLConnection connection = url.openConnection();

    // SSL
    if (sslSF != null && connection instanceof HttpsURLConnection) {
      ((HttpsURLConnection) connection).setSSLSocketFactory(sslSF);
    }

    // cache
    if (cacheManagerName != null) {
      connection.setUseCaches(true); // core
      connection = new CacheControlUrlConnection(connection, cacheManagerName);
      connection.setUseCaches(true); // wrapper
    }

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

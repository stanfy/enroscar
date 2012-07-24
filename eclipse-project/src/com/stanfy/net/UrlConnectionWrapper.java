package com.stanfy.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

/**
 * URL connection wrapper.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public class UrlConnectionWrapper extends URLConnection {

  /** Core connection instance. */
  private URLConnection core;

  public UrlConnectionWrapper(final URLConnection urlConnection) {
    super(urlConnection.getURL());
    this.core = urlConnection;
  }

  public static URLConnection unwrap(final URLConnection connection) {
    URLConnection result = connection;
    while (result instanceof UrlConnectionWrapper) {
      result = ((UrlConnectionWrapper)result).getCore();
    }
    return result;
  }
  public static <T extends UrlConnectionWrapper> T getWrapper(final URLConnection connection, final Class<T> clazz) {
    URLConnection result = connection;
    if (!(result instanceof UrlConnectionWrapper)) { return null; }
    boolean found = false;
    do {
      if (!clazz.isInstance(result)) {
        result = ((UrlConnectionWrapper)result).getCore();
      } else {
        found = true;
      }
    } while (!found && result instanceof UrlConnectionWrapper);
    return found ? clazz.cast(result) : null;
  }

  public URLConnection getCore() { return core; }

  @Override
  public void connect() throws IOException {
    core.connect();
    connected = true;
  }

  @Override
  public boolean getAllowUserInteraction() { return core.getAllowUserInteraction(); }

  @Override
  public Object getContent() throws IOException { return core.getContent(); }

  @Override
  @SuppressWarnings("rawtypes")
  public Object getContent(final Class[] types) throws IOException { return core.getContent(types); }

  @Override
  public String getContentEncoding() { return core.getContentEncoding(); }

  @Override
  public int getContentLength() { return core.getContentLength();  }

  @Override
  public String getContentType() { return core.getContentType(); }

  @Override
  public long getDate() { return core.getDate(); }

  @Override
  public boolean getDefaultUseCaches() { return core.getDefaultUseCaches(); }

  @Override
  public boolean getDoInput() { return core.getDoInput(); }

  @Override
  public boolean getDoOutput() { return core.getDoInput(); }

  @Override
  public long getExpiration() { return core.getExpiration(); }

  @Override
  public String getHeaderField(final int pos) { return core.getHeaderField(pos); }

  @Override
  public Map<String, List<String>> getHeaderFields() { return core.getHeaderFields(); }

  @Override
  public Map<String, List<String>> getRequestProperties() { return core.getRequestProperties(); }

  @Override
  public void addRequestProperty(final String field, final String newValue) {
    core.addRequestProperty(field, newValue);
  }

  @Override
  public String getHeaderField(final String key) { return core.getHeaderField(key); }

  @Override
  public long getHeaderFieldDate(final String field, final long defaultValue) { return core.getHeaderFieldDate(field, defaultValue); }

  @Override
  public int getHeaderFieldInt(final String field, final int defaultValue) { return core.getHeaderFieldInt(field, defaultValue); }

  @Override
  public String getHeaderFieldKey(final int posn) { return core.getHeaderFieldKey(posn); }

  @Override
  public long getIfModifiedSince() { return core.getIfModifiedSince(); }

  @Override
  public InputStream getInputStream() throws IOException { return core.getInputStream(); }

  @Override
  public long getLastModified() { return core.getLastModified(); }

  @Override
  public OutputStream getOutputStream() throws IOException { return core.getOutputStream(); }

  @Override
  public java.security.Permission getPermission() throws IOException { return core.getPermission(); }

  @Override
  public String getRequestProperty(final String field) { return core.getRequestProperty(field); }

  @Override
  public URL getURL() { return url; }

  @Override
  public boolean getUseCaches() { return core.getUseCaches(); }

  @Override
  public void setAllowUserInteraction(final boolean newValue) {
    core.setAllowUserInteraction(newValue);
  }

  @Override
  public void setDefaultUseCaches(final boolean newValue) {
    core.setDefaultUseCaches(newValue);
  }

  @Override
  public void setDoInput(final boolean newValue) {
    core.setDoInput(newValue);
  }

  @Override
  public void setDoOutput(final boolean newValue) {
    core.setDoOutput(newValue);
  }

  @Override
  public void setIfModifiedSince(final long newValue) {
    core.setIfModifiedSince(newValue);
  }

  @Override
  public void setRequestProperty(final String field, final String newValue) {
    core.setRequestProperty(field, newValue);
  }

  @Override
  public void setUseCaches(final boolean newValue) {
    core.setUseCaches(newValue);
  }

  @Override
  public void setConnectTimeout(final int timeout) {
    core.setConnectTimeout(timeout);
  }

  @Override
  public int getConnectTimeout() { return core.getConnectTimeout(); }

  @Override
  public void setReadTimeout(final int timeout) {
    core.setReadTimeout(timeout);
  }

  @Override
  public int getReadTimeout() { return core.getReadTimeout(); }

}

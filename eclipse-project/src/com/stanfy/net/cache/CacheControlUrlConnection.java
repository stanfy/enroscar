package com.stanfy.net.cache;

import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;

import android.util.Log;

import com.stanfy.net.UrlConnectionWrapper;

/**
 * {@link URLConnection} that supplies information about required cache manager.
 * <p><b>NB!</b> You cannot share this connection between multiple threads.</p>
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public class CacheControlUrlConnection extends UrlConnectionWrapper {

  /** Cache manager name. */
  private final String cacheManagerName;

  /** Flag that indicates that connections stack has been cleared. */
  private boolean done = false;

  public CacheControlUrlConnection(final URLConnection urlConnection, final String cacheManagerName) {
    super(urlConnection);
    this.cacheManagerName = cacheManagerName;
  }

  public String getResponseCacheName() { return cacheManagerName; }

  /*
   * Note: this method is not synchronized since CacheControlUrlConnection must be accessed from one thread only.
   */
  void restoreConnectionsStack() {
    if (!done) {
      ResponseCacheSwitcher.restoreUrlConnection(this);
      done = true;
    } else {
      Log.w("CacheControl", "restoreConnectionsStack() has been called twice, there is something wrong with it");
    }
  }

  @Override
  public void connect() throws IOException {
    if (connected) { return; }
    if (getDoInput() || getDoOutput()) {
      ResponseCacheSwitcher.saveUrlConnection(this);
    }
    super.connect();
  }

  @Override
  public InputStream getInputStream() throws IOException {
    connect();
    if (getDoInput()) {
      return new CacheControlInputStream();
    }
    return super.getInputStream();
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    connect();
    if (!getDoInput() && getDoOutput()) {
      return new CacheControlOutputStream();
    }
    return super.getOutputStream();
  }

  /**
   * Stream that restores connections stack when it's closed.
   */
  protected class CacheControlInputStream extends FilterInputStream {

    public CacheControlInputStream() throws IOException {
      super(CacheControlUrlConnection.super.getInputStream());
    }

    @Override
    public void close() throws IOException {
      try {
        super.close();
      } finally {
        restoreConnectionsStack();
      }
    }

  }

  /**
   * Stream that restores connections stack when it's closed.
   */
  protected class CacheControlOutputStream extends FilterOutputStream {

    public CacheControlOutputStream() throws IOException {
      super(CacheControlUrlConnection.super.getOutputStream());
    }

    @Override
    public void close() throws IOException {
      try {
        super.close();
      } finally {
        restoreConnectionsStack();
      }
    }

  }

}

package com.stanfy.images;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import com.stanfy.app.HttpClientsPool;

/**
 * Default images downloader implementation. Uses {@link HttpClientsPool} to retrieve HTTP clients.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class DefaultDownloader implements Downloader {

  /** HTTP client instance. */
  private HttpClient httpClient;
  /** Pool instance. */
  private final HttpClientsPool clientsPool;

  public DefaultDownloader(final HttpClientsPool clientsPool) {
    this.clientsPool = clientsPool;
  }

  @Override
  public InputStream download(final String url) throws IOException {
    synchronized (this) {
      if (httpClient == null) {
        httpClient = clientsPool.getHttpClient();
      }
    }
    return httpClient.execute(new HttpGet(url)).getEntity().getContent();
  }

  @Override
  public void finish(final String url) {
    // nothing
  }

  @Override
  public void flush() {
    synchronized (this) {
      clientsPool.releaseHttpClient(httpClient);
      httpClient = null;
    }
  }

}

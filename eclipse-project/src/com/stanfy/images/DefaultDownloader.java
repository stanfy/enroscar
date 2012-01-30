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

  /** Pool instance. */
  private final HttpClientsPool httpPool;

  public DefaultDownloader(final HttpClientsPool clientsPool) {
    this.httpPool = clientsPool;
  }

  @Override
  public InputStream download(final String url) throws IOException {
    final HttpClient client = httpPool.getHttpClient();
    return client.execute(new HttpGet(url)).getEntity().getContent();
  }

  @Override
  public void finish(final String url) {
  }

}

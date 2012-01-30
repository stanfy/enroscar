package com.stanfy.images;

import java.io.IOException;
import java.io.InputStream;

/**
 * Images downloader.
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public interface Downloader {

  /**
   * @param url URL
   * @return input stream
   * @throws IOException if ever
   */
  InputStream download(final String url) throws IOException;

  /**
   * @param url URL with a finished connection
   */
  void finish(final String url);

}

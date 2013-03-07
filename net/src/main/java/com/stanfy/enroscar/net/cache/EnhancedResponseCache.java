package com.stanfy.enroscar.net.cache;

import java.io.IOException;
import java.net.CacheResponse;
import java.net.URI;
import java.net.URLConnection;

/**
 * Enhanced response cache.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public interface EnhancedResponseCache {

  CacheResponse get(final URI uri, URLConnection connection) throws IOException;

  /**
   * Delete an entry obtained though GET request, so that it can be identified by URL only.
   * This method can be used for fine-tuned cache control.
   * @param url entry URL
   * @return whether entry was removed
   * @throws IOException in case of disk cache operation error
   */
  boolean deleteGetEntry(final String url) throws IOException;

  /**
   * @param url content URL
   * @return true if image for specified URL exists in cache
   */
  boolean contains(final String url);

  /**
   * @param url content URL
   * @return path to the local file with cached content or null of this content is not cached
   */
  String getLocalPath(final String url);

}

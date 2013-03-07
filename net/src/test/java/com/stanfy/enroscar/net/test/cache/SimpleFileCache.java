package com.stanfy.enroscar.net.test.cache;

import java.io.File;

import org.robolectric.Robolectric;

import com.stanfy.enroscar.net.cache.BaseFileResponseCache;
import com.stanfy.enroscar.net.cache.CacheEntry;

/**
 * Cache for testing.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public class SimpleFileCache extends BaseFileResponseCache {

  /** Name. */
  private final String name;

  public SimpleFileCache(final String name) {
    this.name = name;
    final int maxSize = 1024 * 1024 * 1;
    setWorkingDirectory(new File(Robolectric.application.getFilesDir(), name));
    setMaxSize(maxSize);
  }

  public String getName() { return name; }

  @Override
  protected CacheEntry createCacheEntry() { return new CacheEntry(); }

}

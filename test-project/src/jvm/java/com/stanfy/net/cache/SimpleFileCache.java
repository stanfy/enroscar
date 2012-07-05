package com.stanfy.net.cache;

import java.io.File;

import com.xtremelabs.robolectric.Robolectric;

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

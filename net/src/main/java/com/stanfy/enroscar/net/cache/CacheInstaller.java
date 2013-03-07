package com.stanfy.enroscar.net.cache;

import java.io.File;
import java.io.IOException;
import java.net.ResponseCache;

/**
 * Installer for a particular type of {@link ResponseCache}.
 * @param <T> response cache type
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public interface CacheInstaller<T extends ResponseCache> {

  T install(final File cacheDir, final long maxSize) throws IOException;

  void delete(final T cache) throws IOException;

  void close(final T cache) throws IOException;

}

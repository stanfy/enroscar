package com.stanfy.enroscar.net.cache;

import java.io.File;
import java.net.ResponseCache;

/**
 * Base cache with limited maximum size.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public abstract class BaseSizeRestrictedCache extends ResponseCache {

  /** Default maximum size. */
  public static final long MAX_SIZE_DEFUALT = 6 * 1024 * 1024;

  /** Max size. */
  private long maxSize = MAX_SIZE_DEFUALT;

  /** Working directory. */
  private File workingDirectory;

  /**
   * Set cache working directory.
   * @param workingDirectory cache working directory
   */
  protected void setWorkingDirectory(final File workingDirectory) {
    this.workingDirectory = workingDirectory;
  }
  /**
   * Set cache size limit.
   * @param maxSize cache maximum size
   */
  protected void setMaxSize(final long maxSize) {
    this.maxSize = maxSize;
  }

  public File getWorkingDirectory() { return workingDirectory; }
  public long getMaxSize() { return maxSize; }

}

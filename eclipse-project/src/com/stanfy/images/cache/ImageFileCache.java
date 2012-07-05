/**
 *
 */
package com.stanfy.images.cache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;

import android.content.Context;
import android.os.Environment;

import com.stanfy.app.beans.EnroscarBean;
import com.stanfy.images.ImagesManager;
import com.stanfy.net.cache.BaseFileResponseCache;
import com.stanfy.net.cache.CacheEntry;
import com.stanfy.net.cache.CacheTimeRule;
import com.stanfy.utils.AppUtils;

/**
 * File-based cache used by images manager.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
@EnroscarBean(value = ImagesManager.CACHE_BEAN_NAME, contextDependent = true)
public class ImageFileCache extends BaseFileResponseCache {

  public ImageFileCache(final Context context) {
    final String eState = Environment.getExternalStorageState();
    final File baseDir = Environment.MEDIA_MOUNTED.equals(eState)
        ? AppUtils.getSdkDependentUtils().getExternalCacheDir(context)
        : context.getCacheDir();
    setWorkingDirectory(new File(baseDir, "images"));
  }

  @Override
  protected CacheEntry createCacheEntry() { return new ImageCacheEntry(); }

  /** Image cache entry. */
  public static class ImageCacheEntry extends CacheEntry {
    /** Image type identifier. */
    int imageType = -1;

    @Override
    protected void writeMetaData(final Writer writer) throws IOException {
      writeInt(writer, imageType);
    }

    @Override
    protected void readMetaData(final InputStream in) throws IOException {
      imageType = readInt(in);
    }

  }

  /** Cache rule. */
  public static class ImageTypeBasedCacheRule extends CacheTimeRule {
    /** Image type identifier. */
    final int imageType;

    public ImageTypeBasedCacheRule(final int imageType, final long time) {
      super(time);
      this.imageType = imageType;
    }

    @Override
    public boolean matches(final CacheEntry cacheEntry) {
      return cacheEntry instanceof ImageCacheEntry && imageType == ((ImageCacheEntry)cacheEntry).imageType;
    }

    @Override
    protected String matcherToString() {
      return "imageType=" + imageType;
    }

  }

  /** 'Until' cache rule. */
  public static class ImageTypeBasedUntilCacheRule extends ImageTypeBasedCacheRule {

    public ImageTypeBasedUntilCacheRule(final int imageType, final long time) {
      super(imageType, time);
    }

    @Override
    public boolean isActual(final long createTime) { return isUntilActual(createTime, getTime()); }

  }

}

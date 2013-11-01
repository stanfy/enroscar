package com.stanfy.enroscar.images;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.util.Log;

import com.stanfy.enroscar.beans.BeansManager;
import com.stanfy.enroscar.io.BuffersPool;
import com.stanfy.enroscar.io.FlushedInputStream;
import com.stanfy.enroscar.io.IoUtils;
import com.stanfy.enroscar.io.PoolableBufferedInputStream;
import com.stanfy.enroscar.io.PoolableBufferedOutputStream;
import com.stanfy.enroscar.net.UrlConnectionBuilderFactory;
import com.stanfy.enroscar.net.cache.EnhancedResponseCache;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;

import static com.stanfy.enroscar.images.ImagesManager.TAG;

/**
 * Image loading request.
 * Contains information about max allowed size of an image.
 * It's the size an image will be scaled down to before storing on disk.
 */
public class ImageRequest {

  /** Mark for getting bounds info. */
  private static final int BOUNDS_INFO_MARK = 65536;

  /** Images manager. */
  private final ImagesManager manager;

  /** Image URL. */
  final String url;

  /** Maximum allowed height. */
  private final int maxAllowedWidth, maxAllowedHeight;

  /** Required size. */
  private int requiredWidth, requiredHeight;

  /** Skip scaling before memory cache. */
  private boolean skipScaleBeforeMemCache;

  /** Image format. */
  private Bitmap.Config format = Bitmap.Config.ARGB_8888;

  /**
   * @param manager images manager instance
   * @param url image URL
   * @param maxRelativeSize max allowed size of an image relatively to screen size,
   *                        negative value means allowed size will be ignored
   */
  ImageRequest(final ImagesManager manager, final String url, final float maxRelativeSize) {
    this.manager = manager;
    this.url = url;
    if (maxRelativeSize < 0) {
      maxAllowedWidth = -1;
      maxAllowedHeight = -1;
    } else {
      DisplayMetrics metrics = manager.getContext().getResources().getDisplayMetrics();
      maxAllowedWidth = (int) (metrics.widthPixels * maxRelativeSize);
      maxAllowedHeight = (int) (metrics.heightPixels * maxRelativeSize);
    }
  }

  public String getUrl() {
    return url;
  }

  public int getRequiredWidth() {
    return requiredWidth == 0 && hasAllowedSize() ? maxAllowedWidth : requiredWidth;
  }

  public void setRequiredWidth(final int requiredWidth) {
    this.requiredWidth = requiredWidth;
  }

  public int getRequiredHeight() {
    return requiredHeight == 0 && hasAllowedSize() ? maxAllowedHeight : requiredHeight;
  }

  public void setRequiredHeight(final int requiredHeight) {
    this.requiredHeight = requiredHeight;
  }

  public boolean isSkipScaleBeforeMemCache() {
    return skipScaleBeforeMemCache;
  }

  public void setSkipScaleBeforeMemCache(final boolean skipScaleBeforeMemCache) {
    this.skipScaleBeforeMemCache = skipScaleBeforeMemCache;
  }

  public void setFormat(final Bitmap.Config format) {
    this.format = format;
  }

  public String getKey() {
    return url + "!" + getRequiredHeight() + "x" + getRequiredHeight();
  }

  public String getCacheKey() {
    return url;
  }

  public boolean hasAllowedSize() {
    return maxAllowedWidth > 0 && maxAllowedHeight > 0;
  }

  /**
   * Store image to the disk cache.
   * If max allowed size is set image may be rescaled on disk.
   * @throws IOException if error happens
   */
  public void storeToDisk() throws IOException {
    if (manager.isPresentOnDisk(url)) {
      return;
    }

    if (!hasAllowedSize()) {
      IoUtils.consumeStream(newConnection().getInputStream(), manager.getBuffersPool());
      return;
    }

    ImageResult result = decodeStream(newConnection().getInputStream(), true);
    if (result.getType() == ImageSourceType.NETWORK && result.getBitmap() != null) {
      // image was scaled
      writeBitmapToDisk(result.getBitmap());
    }
  }

  /**
   * @return drawable with loaded image
   * @throws IOException if error happens
   */
  public ImageResult readImage() throws IOException {
    return decodeStream(newConnection().getInputStream(), false);
  }

  URLConnection newConnection() throws IOException {
    UrlConnectionBuilderFactory builderFactory =
        BeansManager.get(manager.getContext())
            .getContainer()
            .getBean(ImagesManager.CONNECTION_BUILDER_FACTORY_NAME, UrlConnectionBuilderFactory.class);

    return builderFactory.newUrlConnectionBuilder()
        .setUrl(url)
        .setCacheManagerName(ImagesManager.CACHE_BEAN_NAME)
        .create();
  }

  private ImageResult decodeStream(final InputStream is, boolean onlyIfNeedsRescale) throws IOException {
    final BitmapFactory.Options options = createBitmapOptions();

    final InputStream src = prepareInputStream(is);

    try {

      ImageResult result = new ImageResult();
      result.setType(manager.isPresentOnDisk(url) ? ImageSourceType.DISK : ImageSourceType.NETWORK);

      // get scale factor
      options.inSampleSize = resolveSampleFactor(src, options);

      if (options.inSampleSize > 1 || !onlyIfNeedsRescale) {
        // actually decode
        result.setBitmap(doStreamDecode(src, options));
      } else {
        // consume input in order to cache it
        IoUtils.consumeStream(src, manager.getBuffersPool());
      }

      if (manager.debug) {
        Log.d(TAG, "Image decoded: " + result);
      }
      return result;

    } catch (final OutOfMemoryError e) {

      // wrap OOM in IO
      throw new IOException("out of memory for " + getKey());

    } finally {

      recycle(options);
      src.close();

    }

  }

  Bitmap doStreamDecode(final InputStream input, final BitmapFactory.Options options) throws IOException {
    return BitmapFactory.decodeStream(input, null, options);
  }

  private BitmapFactory.Options createBitmapOptions() {
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inTempStorage = manager.getBuffersPool().get(BuffersPool.DEFAULT_SIZE_FOR_IMAGES);
    options.inPreferredConfig = format;
    return options;
  }

  private void recycle(final BitmapFactory.Options options) {
    manager.getBuffersPool().release(options.inTempStorage);
  }

  private InputStream prepareInputStream(final InputStream is) throws IOException {
    InputStream src = new FlushedInputStream(is);
    if (!src.markSupported()) {
      src = new PoolableBufferedInputStream(src, BuffersPool.DEFAULT_SIZE_FOR_IMAGES, manager.getBuffersPool());
    }
    return src;
  }

  /**
   * @param is image input stream
   * @return sampling factor
   * @throws IOException if error happens
   */
  private int resolveSampleFactor(final InputStream is, final BitmapFactory.Options options) throws IOException {
    if (!is.markSupported()) { throw new IllegalStateException("Input stream does not support marks!"); }

    options.inJustDecodeBounds = true;
    int result = 1;
    try {

      MarkableInputStream markableStream = new MarkableInputStream(is); // Thanks to Square guys :)
      long mark = markableStream.savePosition(BOUNDS_INFO_MARK);
      doStreamDecode(is, options);

      result = ImagesManager.calculateSampleFactor(options.outWidth, options.outHeight,
          getRequiredWidth(), getRequiredHeight());

      markableStream.reset(mark);

    } finally {
      options.inJustDecodeBounds = false;
    }
    return result;
  }

  void writeBitmapToDisk(final Bitmap bitmap) throws IOException {
    EnhancedResponseCache cache = (EnhancedResponseCache) manager.getImagesResponseCache();
    OutputStream output = new FileOutputStream(cache.getLocalPath(url));
    output = new PoolableBufferedOutputStream(output, BuffersPool.DEFAULT_SIZE_FOR_IMAGES, manager.getBuffersPool());
    try {
      final int quality = 100;
      bitmap.compress(Bitmap.CompressFormat.PNG, quality, output);
    } finally {
      IoUtils.closeQuietly(output);
    }
  }

}

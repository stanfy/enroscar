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
class ImageRequest {

  /** Mark for getting bounds info. */
  private static final int BOUNDS_INFO_MARK = 65536;

  /** Images manager. */
  private final ImagesManager manager;

  /** Image URL. */
  final String url;

  /** Maximum allowed height. */
  final int maxAllowedWidth, maxAllowedHeight;

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
  public ImageRequest(final ImagesManager manager, final String url, final float maxRelativeSize) {
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

  public int getRequiredWidth() {
    return requiredWidth;
  }

  public void setRequiredWidth(final int requiredWidth) {
    this.requiredWidth = requiredWidth;
  }

  public int getRequiredHeight() {
    return requiredHeight;
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
    return url + "!" + requiredWidth + "x" + requiredHeight;
  }

  public String getCacheKey() {
    return url;
  }

  public boolean hasAllowedSize() {
    return maxAllowedWidth > 0 && maxAllowedHeight > 0;
  }

  public void storeToDisk() throws IOException {
    IoUtils.consumeStream(newConnection().getInputStream(), manager.getBuffersPool());
  }

  /**
   * @return drawable with loaded image
   * @throws IOException if error happens
   */
  public ImageResult readImage() throws IOException {
    ImageResult result = decodeStream(newConnection().getInputStream());
    if (hasAllowedSize() && result.getType() == ImageResult.ResultType.NETWORK) {
      manager.getImageTaskExecutor().execute(createRescaleTask(result.getBitmap()));
    }
    return result;
  }

  private URLConnection newConnection() throws IOException {
    UrlConnectionBuilderFactory builderFactory =
        BeansManager.get(manager.getContext())
            .getContainer()
            .getBean(ImagesManager.CONNECTION_BUILDER_FACTORY_NAME, UrlConnectionBuilderFactory.class);

    return builderFactory.newUrlConnectionBuilder()
        .setUrl(url)
        .setCacheManagerName(ImagesManager.CACHE_BEAN_NAME)
        .create();
  }

  /**
   * @param is image input stream
   * @return drawable with a loaded image
   * @throws IOException if error happens
   */
  private ImageResult decodeStream(final InputStream is) throws IOException {
    final BitmapFactory.Options options = new BitmapFactory.Options();
    prepareBitmapOptions(options);

    final InputStream src = prepareInputStream(is);

    try {

      ImageResult result = new ImageResult();
      result.setType(manager.isPresentInFileCache(url) ? ImageResult.ResultType.CACHE : ImageResult.ResultType.NETWORK);

      // get scale factor
      options.inSampleSize = resolveSampleFactor(src, options);

      // actually decode
      result.setBitmap(BitmapFactory.decodeStream(src, null, options));

      if (manager.debug) {
        Log.d(TAG, "Image decoded: " + result);
      }
      return result;

    } catch (final OutOfMemoryError e) {

      // wrap OOM in IO
      throw new IOException("out of memory for " + getKey(), e);

    } finally {

      // recycle
      manager.getBuffersPool().release(options.inTempStorage);
      src.close();

    }

  }

  private void prepareBitmapOptions(final BitmapFactory.Options options) {
    options.inTempStorage = manager.getBuffersPool().get(BuffersPool.DEFAULT_SIZE_FOR_IMAGES);
    options.inPreferredConfig = format;
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
      BitmapFactory.decodeStream(is, null, options);

      final int inW = options.outWidth, inH = options.outHeight;
      int width = requiredWidth, height = requiredHeight;
      if (width == 0 && height == 0 && hasAllowedSize()) {
        width = maxAllowedWidth;
        height = maxAllowedHeight;
      }

      if (inW > width || inH > height) {
        result = ImagesManager.calculateSampleFactor(inW, inH, width, height);
      }

      markableStream.reset(mark);

    } finally {
      options.inJustDecodeBounds = false;
    }
    return result;
  }

  private Runnable createRescaleTask(final Bitmap bitmap) {
    return new Runnable() {
      @Override
      public void run() {
        EnhancedResponseCache cache = (EnhancedResponseCache) manager.getImagesResponseCache();
        if (cache.contains(url)) {
          OutputStream output = null;
          try {
            final int quality = 90;
            output = new FileOutputStream(cache.getLocalPath(url));
            bitmap.compress(Bitmap.CompressFormat.PNG, quality, output);
          } catch (IOException e) {
            if (manager.debug) {
              Log.e(TAG, "Cannot rescale image on disk " + url, e);
            }
          } finally {
            IoUtils.closeQuietly(output);
          }
        }
      }
    };
  }

}

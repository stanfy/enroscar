package com.stanfy.images;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.regex.Pattern;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.stanfy.DebugFlags;
import com.stanfy.images.model.CachedImage;
import com.stanfy.views.utils.AppUtils;
import com.stanfy.views.utils.Task;
import com.stanfy.views.utils.ThreadUtils;

/**
 * @param <T> cached image type
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public class ImagesManager<T extends CachedImage> {

  /** Logging tag. */
  private static final String TAG = "ImagesManager";

  /** Images quality. */
  private static final int IMAGES_QUALITY = 80;

  /** Pattern to cut the images sources from HTML. */
  protected static final Pattern IMG_URL_PATTERN = Pattern.compile("<img.*?src=\"(.*?)\".*?>");

  /** Debug flag. */
  private static final boolean DEBUG_IO = DebugFlags.DEBUG_IO;

  /** Debug flag. */
  private static final boolean DEBUG = DebugFlags.DEBUG_IMAGES;

  /** Empty drawable. */
  protected static final ColorDrawable EMPTY_DRAWABLE = new ColorDrawable(0xeeeeee);

  /** Hidden constructor. */
  protected ImagesManager() { /* nothing to do */ }

  /** MB. */
  private static final int MB = 20;

  /** Memory cache. */
  private final ImageMemoryCache memCache = new ImageMemoryCache(2 << MB);

  /** Buffers pool. */
  private final BuffersPool buffersPool = new BuffersPool(new int[][] {
    {4, BuffersPool.DEFAULT_SIZE_FOR_IMAGES}
  });

  protected String setCachedImagePath(final T image) {
    if (image.getPath() != null) { return image.getPath(); }
    final long id = image.getId();
    final String path = AppUtils.buildFilePathById(id, "image-" + id);
    image.setPath(path);
    return path;
  }

  public void ensureImages(final ImagesDAO<T> imagesDao, final Downloader downloader, final Context context, final List<T> images) {
    final File imagesDir = getImageDir(context);
    for (final T image : images) {
      if (image.isLoaded() && new File(imagesDir, image.getPath()).exists()) { continue; }
      try {
        final Drawable d = download(image, downloader);
        saveCachedImage(imagesDao, context, image, d);
      } catch (final IOException e) {
        if (DEBUG_IO) { Log.e(TAG, "IO error for " + image.getUrl() + ": " + e.getMessage()); }
      }
    }
  }

  public void clearCache(final Context context, final String path, final String url) {
    memCache.remove(url);
    final File f = new File(getImageDir(context), path);
    delete(f);
  }

  /**
   * @return an executor for image tasks
   */
  protected Executor getImageTaskExecutor() { return ThreadUtils.getImageTasksExecutor(); }
  /**
   * @return an executor for main tasks
   */
  protected Executor getMainTaskExecutor() { return ThreadUtils.getMainTasksExecutor(); }

  protected Drawable getLoadingDrawable(@SuppressWarnings("unused") final Context context) { return EMPTY_DRAWABLE; }

  protected void cancelTasks(final ImageLoader<T> loader) {
    ThreadUtils.cancelImageTask(loader.getName());
  }

  protected void setImage(final ImageView imageView, final Drawable drawable) {
    imageView.setImageDrawable(drawable);
    imageView.setTag(null);
  }

  protected Drawable getFromMemCache(final String url) {
    return memCache.getElement(url);
  }

  protected ImageLoader<T> createImageLoaderTask(final ImageView imageView, final String url, final ImagesDAO<T> imagesDAO, final Downloader downloader) {
    return new ImageLoader<T>(imageView, url, this, imagesDAO, downloader);
  }

  public void populateImage(final ImageView imageView, final String url, final ImagesDAO<T> imagesDAO, final Downloader downloader) {
    if (TextUtils.isEmpty(url)) {
      setImage(imageView, getLoadingDrawable(imageView.getContext()));
      return;
    }
    final Drawable memCached = getFromMemCache(url);
    if (memCached != null) {
      setImage(imageView, memCached);
      return;
    }
    setImage(imageView, getLoadingDrawable(imageView.getContext()));
    final ImageLoader<T> loader = createImageLoaderTask(imageView, url, imagesDAO, downloader);
    cancelTasks(loader);
    getImageTaskExecutor().execute(loader);
  }

  public File getImageDir(final Context context) {
    final String eState = Environment.getExternalStorageState();
    if (Environment.MEDIA_MOUNTED.equals(eState)) {
      return AppUtils.getSdkDependentUtils().getExternalCacheDir(context);
    }
    return context.getCacheDir();
  }

  private static void delete(final File file) {
    if (!file.exists()) { return; }
    final File parent = file.getParentFile();
    if (file.delete()) {
      delete(parent);
    }
  }

  protected void saveCachedImage(final ImagesDAO<T> imagesDao, final Context context, final T image, final Drawable d) throws IOException {
    if (d == null) { return; }
    if (!(d instanceof BitmapDrawable)) {
      Log.w(TAG, "Unsupported drawable " + d.getClass() + ". Local image won't be saved.");
      return;
    }
    final BitmapDrawable bd = (BitmapDrawable)d;
    final String path = setCachedImagePath(image);

    final File f = new File(getImageDir(context), path);
    final File parent = f.getParentFile();
    parent.mkdirs();
    if (!parent.exists()) {
      Log.e(TAG, "Directories not created for " + f.getParent() + ". Local image won't be saved.");
      return;
    }

    final Bitmap bm = bd.getBitmap();
    if (bm == null) {
      Log.e(TAG, "Broken drawable " + bd + ", bitmap is null");
      return;
    }
    final FileOutputStream out = new FileOutputStream(f);
    bm.compress(CompressFormat.JPEG, IMAGES_QUALITY, out);
    out.close();

    image.setLoaded(true);
    imagesDao.updateImage(image);
  }

  protected void memCacheImage(final String url, final Drawable d) {
    if (d instanceof BitmapDrawable) {
      memCache.putElement(url, (BitmapDrawable)d);
    }
  }

  protected Drawable download(final T cachedImage, final Downloader downloader) throws IOException {
    final InputStream imageInput = downloader.download(cachedImage.getUrl());
    final Drawable d = decodeStream(imageInput);
    return d;
  }

  protected Drawable readLocal(final T cachedImage, final Context context) throws IOException {
    final File file = new File(getImageDir(context), cachedImage.getPath());
    if (!file.exists()) {
      if (DEBUG_IO) { Log.w(TAG, "Local file " + file.getAbsolutePath() + "does not exist."); }
      return null;
    }
    final Drawable d = decodeStream(new FileInputStream(file));
    return d;
  }

  protected Drawable decodeStream(final InputStream is) throws IOException {
    final BuffersPool bp = buffersPool;
    final int bCapacity = BuffersPool.DEFAULT_SIZE_FOR_IMAGES;
    InputStream src = is;
    if (!src.markSupported()) { src = new PoolableBufferedInputStream(src, bCapacity, bp); }

    final BitmapFactory.Options opts = new BitmapFactory.Options();
    opts.inTempStorage = bp.get(bCapacity);
    final Bitmap bm = BitmapFactory.decodeResourceStream(null, null, src, null, opts);

    // recycle
    bp.release(opts.inTempStorage);
    src.close();

    return bm != null ? new BitmapDrawable(bm) : null;
  }

  /**
   * Image loader task.
   * @param <T> image type
   * @author Roman Mazur - Stanfy (http://www.stanfy.com)
   */
  protected static class ImageLoader<T extends CachedImage> extends Task {

    /** Logging tag. */
    private static final String TAG = "ImageLoader";

    /** GUI view. */
    private final ImageView imageView;

    /** Image URL. */
    private final String url;

    /** Images manager. */
    private final ImagesManager<T> imagesManager;

    /** Images DAO. */
    private final ImagesDAO<T> imagesDAO;

    /** Downloader. */
    private final Downloader downloader;

    public ImageLoader(final ImageView imageView, final String url, final ImagesManager<T> imagesManager,
        final ImagesDAO<T> imagesDAO, final Downloader downloader) {
      super("image-" + imageView.hashCode());
      this.imagesManager = imagesManager;
      this.url = url;
      this.imageView = imageView;
      this.downloader = downloader;
      this.imagesDAO = imagesDAO;
    }

    protected void safeImageSet(final T cachedImage, final Drawable d) {
      if (d == null) { return; }
      final ImageView imageView = this.imageView;
      final long id = cachedImage.getId();
      imageView.post(new Runnable() {
        @Override
        public void run() {
          synchronized (imageView) {
            final Object tag = imageView.getTag();
            if (tag != null && id == ((Long)tag).longValue()) { imagesManager.setImage(imageView, d); }
          }
        }
      });
    }

    protected Drawable setLocalImage(final T cachedImage) throws IOException {
      final Drawable d = imagesManager.readLocal(cachedImage, imageView.getContext());
      safeImageSet(cachedImage, d);
      return d;
    }

    protected Drawable setRemoteImage(final T cachedImage) throws IOException {
      final Drawable d = imagesManager.download(cachedImage, downloader);
      safeImageSet(cachedImage, d);
      return d;
    }

    private BitmapDrawable prepare(final BitmapDrawable bd) {
      int dstW = imageView.getWidth(), dstH = imageView.getHeight();

      final Bitmap map = bd.getBitmap();
      final int w = map.getWidth(), h = map.getHeight();

      if (w <= dstW && h <= dstH) { return bd; }

      final double ratio = (double)w / h;
      if (w > h) {
        dstH = (int)(dstW / ratio);
      } else {
        dstW = (int)(dstH * ratio);
      }

      if (dstW <= 0 || dstH <= 0) { return bd; }

      final Bitmap scaled = Bitmap.createScaledBitmap(map, dstW, dstH, true);
      return new BitmapDrawable(scaled);
    }

    protected void memCacheImage(final Drawable d) {
      Drawable result = d;
      if (d instanceof BitmapDrawable) {
        result = prepare((BitmapDrawable)d);
      }
      imagesManager.memCacheImage(url, result);
    }

    @Override
    protected void safeSQLRun() {
      try {
        final T cachedImage = imagesDAO.getCachedImage(url);
        if (cachedImage == null) {
          Log.w(TAG, "Cached image info was not created for " + url);
          return;
        }

        imageView.setTag(cachedImage.getId());

        Drawable d = null;

        if (cachedImage.isLoaded()) {
          d = setLocalImage(cachedImage);
          if (DEBUG) { Log.d(TAG, "Image " + cachedImage.getId() + "-local"); }
        }

        if (d == null) {
          d = setRemoteImage(cachedImage);
          imagesManager.saveCachedImage(imagesDAO, imageView.getContext(), cachedImage, d);
          if (DEBUG) { Log.d(TAG, "Image " + cachedImage.getId() + "-remote"); }
        }

        memCacheImage(d);

      } catch (final MalformedURLException e) {
        Log.e(TAG, "Bad URL: " + url + ". Loading canceled.", e);
      } catch (final IOException e) {
        if (DEBUG_IO) { Log.e(TAG, "IO error for " + url + ": " + e.getMessage()); }
      }
    }

  }

}

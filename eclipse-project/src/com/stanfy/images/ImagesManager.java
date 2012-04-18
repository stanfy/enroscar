package com.stanfy.images;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import com.stanfy.DebugFlags;
import com.stanfy.images.cache.ImageMemoryCache;
import com.stanfy.images.model.CachedImage;
import com.stanfy.utils.AppUtils;
import com.stanfy.views.ImagesLoadListenerProvider;
import com.stanfy.views.RemoteImageDensityProvider;

/**
 * A manager that encapsulates the images downloading and caching logic.
 * @param <T> cached image type
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public class ImagesManager<T extends CachedImage> {

  /** Logging tag. */
  static final String TAG = "ImagesManager";

  /** Pattern to cut the images sources from HTML. */
  protected static final Pattern IMG_URL_PATTERN = Pattern.compile("<img.*?src=\"(.*?)\".*?>");

  /** Debug flag. */
  private static final boolean DEBUG_IO = DebugFlags.DEBUG_IO;

  /** Debug flag. */
  private static final boolean DEBUG = DebugFlags.DEBUG_IMAGES;

  /** Empty drawable. */
  protected static final ColorDrawable EMPTY_DRAWABLE = new ColorDrawable(0xeeeeee);

  /** Memory cache. */
  private ImageMemoryCache memCache;

  /** Buffers pool. */
  private final BuffersPool buffersPool = new BuffersPool(new int[][] {
    {4, BuffersPool.DEFAULT_SIZE_FOR_IMAGES}
  });

  /** Resources. */
  private final Resources resources;

  /** Target density from source. */
  private int sourceDensity = 0;

  /** Images format. */
  private Bitmap.Config imagesFormat = Bitmap.Config.RGB_565;

  /** Current loads. */
  private final ConcurrentHashMap<String, ImageLoader<T>> currentLoads = new ConcurrentHashMap<String, ImageLoader<T>>(Threading.imagesWorkersCount);

  /** Paused state. */
  private boolean paused = false;

  /** Hidden constructor. */
  public ImagesManager(final Resources resources) {
    this.resources = resources;
  }

  /** @param memCache the memCache to set */
  public void setMemCache(final ImageMemoryCache memCache) { this.memCache = memCache; }

  /** @param imagesFormat the imagesFormat to set */
  public void setImagesFormat(final Bitmap.Config imagesFormat) { this.imagesFormat = imagesFormat; }

  /** @param sourceDensity the sourceDensity to set */
  public void setSourceDensity(final int sourceDensity) { this.sourceDensity = sourceDensity; }

  /**
   * Ensure that all the images are loaded. Not loaded images will be downloaded in this thread.
   * @param imagesDao images DAO
   * @param downloader downloader instance
   * @param context context instance
   * @param images images collection
   */
  public void ensureImages(final ImagesDAO<T> imagesDao, final Downloader downloader, final Context context, final List<T> images) {
    final File imagesDir = getImageDir(context);
    for (final T image : images) {
      if (image.isLoaded() && new File(imagesDir, image.getPath()).exists()) { continue; }
      try {
        makeImageLocal(imagesDao, context, image, downloader);
      } catch (final IOException e) {
        if (DEBUG_IO) { Log.e(TAG, "IO error for " + image.getUrl() + ": " + e.getMessage()); }
      }
    }
  }

  /**
   * Clear the cached entities.
   * @param context context instance
   * @param path image file system path
   * @param url image URL
   * @return size of the deleted file
   */
  public long clearCache(final Context context, final String path, final String url) {
    memCache.remove(url, false);
    long size = 0;
    if (path != null) {
      final File f = new File(getImageDir(context), path);
      size = f.length();
      delete(f);
    }
    return size;
  }

  /**
   * Flush resources.
   */
  public void flush() {
    memCache.clear(false);
    buffersPool.flush();
  }

  /**
   * Populate the requested image to the specified view. Called from the GUI thread.
   * @param view view instance
   * @param url image URL
   * @param imagesDAO images DAO
   * @param downloader downloader instance
   */
  public void populateImage(final View view, final String url, final ImagesManagerContext<T> imagesContext) {
    final Object tag = view.getTag();
    ImageHolder imageHolder = null;
    if (tag == null) {
      imageHolder = createImageHolder(view);
      view.setTag(imageHolder);
    } else {
      if (!(tag instanceof ImageHolder)) { throw new IllegalStateException("View already has a tag " + tag); }
      imageHolder = (ImageHolder)tag;
    }
    populateImage(imageHolder, url, imagesContext);
  }

  /**
   * Cancel image loading for a view.
   * @param view view that hold an image
   */
  public void cancelImageLoading(final View view) {
    final Object tag = view.getTag();
    if (tag != null && tag instanceof ImageHolder) {
      cancelImageLoading((ImageHolder)tag);
    }
  }
  /**
   * Cancel image loading for a holder.
   * @param holder image holder instance
   */
  public void cancelImageLoading(final ImageHolder holder) {
    holder.performCancellingLoader();
  }

  /**
   * Create an image holder instance for the defined view.
   * @param view view instance
   * @return image holder instance
   */
  protected ImageHolder createImageHolder(final View view) { return ImageHolders.createImageHolder(view); }

  /**
   * @param url image URL
   * @return true if image is cached in memory
   */
  public boolean isMemCached(final String url) { return memCache.contains(url); }

  /**
   * @param url image URL
   * @param view that contains an image holder
   * @return image bitmap from memory cache
   */
  public Drawable getMemCached(final String url, final View view) {
    final Object tag = view.getTag();
    if (tag == null || !(tag instanceof ImageHolder)) { return null; }
    final Drawable res = getFromMemCache(url, (ImageHolder)tag);
    if (res == null) { return null; }
    return decorateDrawable((ImageHolder)tag, res);
  }

  public void populateImage(final ImageHolder imageHolder, final String url, final ImagesManagerContext<T> imagesContext) {
    if (imageHolder.isMatchingParentButNotMeasured()) {
      imageHolder.postpone(new Runnable() {
        @Override
        public void run() { populateImageNow(imageHolder, url, imagesContext); }
      });
    } else {
      populateImageNow(imageHolder, url, imagesContext);
    }
  }

  public void populateImageNow(final ImageHolder imageHolder, final String url, final ImagesManagerContext<T> imagesContext) {
    if (DEBUG) { Log.d(TAG, "Process url " + url); }
    if (TextUtils.isEmpty(url)) {
      setLoadingImage(imageHolder);
      return;
    }

    imageHolder.performCancellingLoader();

    final Drawable memCached = getFromMemCache(url, imageHolder);
    if (memCached != null) {
      imageHolder.onStart(null, url);
      setImage(imageHolder, memCached, false, false);
      imageHolder.onFinish(url, memCached);
      return;
    }

    if (DEBUG) { Log.d(TAG, "Set loading for " + url); }
    setLoadingImage(imageHolder);
    imageHolder.currentUrl = url; // we are in GUI thread
    startImageLoaderTask(imageHolder, imagesContext);
  }

  private void setLoadingImage(final ImageHolder holder) {
    if (!holder.skipLoadingImage()) {
      final Drawable d = !holder.isDynamicSize() ? getLoadingDrawable(holder) : null;
      setImage(holder, d, true, false/* ignored */);
    }
  }

  /**
   * @param image image to process
   * @return local file system path to that image
   */
  public String setCachedImagePath(final T image) {
    if (image.getPath() != null) { return image.getPath(); }
    final long id = image.getId();
    final String path = AppUtils.buildFilePathById(id, "image-" + id);
    image.setPath(path);
    return path;
  }

  /**
   * @return an executor for image tasks
   */
  protected Executor getImageTaskExecutor() { return Threading.getImageTasksExecutor(); }

  /**
   * @param context context
   * @return a drawable to display while the image is loading
   */
  protected Drawable getLoadingDrawable(final ImageHolder holder) {
    final Drawable d = holder.getLoadingImage();
    return d != null ? d : EMPTY_DRAWABLE;
  }

  /**
   * @param context context instance
   * @param drawable resulting drawable
   * @return decorated drawable
   */
  protected Drawable decorateDrawable(final ImageHolder holder, final Drawable drawable) { return drawable; }

  /**
   * It must be executed in the main thread.
   * @param imageView image view instance
   * @param drawable incoming drawable
   * @param preloader preloading image flag
   * @param animate whether to animate image change
   */
  protected final void setImage(final ImageHolder imageHolder, final Drawable drawable, final boolean preloader, final boolean animate) {
    final Drawable d = decorateDrawable(imageHolder, drawable);
    if (preloader) {
      imageHolder.setLoadingImage(d);
    } else {
      imageHolder.setImage(d, animate);
    }
    imageHolder.reset();
  }

  /**
   * @param url image URL
   * @return cached drawable
   */
  protected Drawable getFromMemCache(final String url, final ImageHolder holder) {
    synchronized (holder) {
      if (holder.currentUrl != null && !holder.currentUrl.equals(url)) { return null; }
    }
    final Bitmap map = memCache.getElement(url);
    if (map == null) {
      if (DEBUG) { Log.v(TAG, "Not in mem " + url); }
      return null;
    }
    final BitmapDrawable result = new BitmapDrawable(holder.context.getResources(), map);
    final int gap = 5;
    final boolean suits = holder.isDynamicSize()
        || holder.skipScaleBeforeCache()
        || Math.abs(holder.getRequiredWidth() - result.getIntrinsicWidth()) < gap
        || Math.abs(holder.getRequiredHeight() - result.getIntrinsicHeight()) < gap;
    if (DEBUG) { Log.v(TAG, "Use mem cache " + suits + " for " + url); }
    return suits ? result : null;
  }

  /**
   * @param imageHolder image holder to process
   * @param imagesDAO images DAO
   * @param downloader downloader instance
   * @return loader task instance
   */
  protected void startImageLoaderTask(final ImageHolder imageHolder, final ImagesManagerContext<T> imagesContext) {
    final String key = imageHolder.getLoaderKey();
    if (DEBUG) { Log.d(TAG, "Key " + key); }
    ImageLoader<T> loader = currentLoads.get(key);
    if (loader != null) {
      final boolean added = loader.addTarget(imageHolder);
      if (!added) { loader = null; }
    }
    if (loader == null) {
      if (DEBUG) { Log.d(TAG, "Start a new task"); }
      loader = new ImageLoader<T>(imageHolder.currentUrl, key, imagesContext);
      final boolean added = loader.addTarget(imageHolder);
      if (!added) { throw new IllegalStateException("Cannot add target to the new loader"); }
      currentLoads.put(key, loader);
      final Executor executor = getImageTaskExecutor();
      executor.execute(loader.future);
    } else if (DEBUG) {
      Log.d(TAG, "Joined to the existing task");
    }
  }

  /**
   * @param context context instance
   * @return base dir to save images
   */
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

  protected void makeImageLocal(final ImagesDAO<T> imagesDao, final Context context, final T image, final Downloader downloader) throws IOException {
    final String path = setCachedImagePath(image);

    final File f = new File(getImageDir(context), path);
    final File parent = f.getParentFile();
    parent.mkdirs();
    if (!parent.exists()) {
      Log.e(TAG, "Directories not created for " + f.getParent() + ". Local image won't be saved.");
      return;
    }

    final InputStream in = new PoolableBufferedInputStream(downloader.download(image.getUrl()), BuffersPool.DEFAULT_SIZE_FOR_IMAGES, buffersPool);
    final FileOutputStream out = new FileOutputStream(f);
    final byte[] buffer = buffersPool.get(BuffersPool.DEFAULT_SIZE_FOR_IMAGES);
    if (buffer == null) { return; }
    int cnt;
    try {
      do {
        cnt = in.read(buffer);
        if (cnt != -1) { out.write(buffer, 0, cnt); }
      } while (cnt != -1);
    } finally {
      in.close();
      out.close();
      buffersPool.release(buffer);
    }
    downloader.finish(image.getUrl());

    image.setLoaded(true);
    final long time = System.currentTimeMillis();
    image.setTimestamp(time);
    image.setUsageTimestamp(time);
    imagesDao.updateImage(image);
  }

  protected void memCacheImage(final String url, final Drawable d) {
    if (d instanceof BitmapDrawable) {
      if (DEBUG) { Log.d(TAG, "Memcache for " + url); }
      memCache.putElement(url, ((BitmapDrawable)d).getBitmap());
    }
  }

  protected void setupDensityAndFormat(final BitmapFactory.Options options, final int sourceDensity) {
    options.inDensity = sourceDensity > 0 ? sourceDensity : this.sourceDensity;
    options.inPreferredConfig = imagesFormat;
  }

  protected Drawable readLocal(final T cachedImage, final Context context, final ImageHolder holder) throws IOException {
    final File file = new File(getImageDir(context), cachedImage.getPath());
    if (!file.exists()) {
      if (DEBUG_IO) { Log.w(TAG, "Local file " + file.getAbsolutePath() + "does not exist."); }
      return null;
    }
    final BitmapFactory.Options options = new BitmapFactory.Options();
    if (holder.useSampling() && !holder.isDynamicSize()) {
      options.inSampleSize = resolveSampleFactor(new FileInputStream(file), holder.getSourceDensity(), holder.getRequiredWidth(), holder.getRequiredHeight());
    }
    final Drawable d = decodeStream(new FileInputStream(file), holder.getSourceDensity(), options);
    return d;
  }

  private InputStream prepareImageOptionsAndInput(final InputStream is, final int sourceDensity, final BitmapFactory.Options options) {
    final BuffersPool bp = buffersPool;
    final int bCapacity = BuffersPool.DEFAULT_SIZE_FOR_IMAGES;
    InputStream src = new FlushedInputStream(is);
    if (!src.markSupported()) { src = new PoolableBufferedInputStream(src, bCapacity, bp); }
    final BitmapFactory.Options opts = options != null ? options : new BitmapFactory.Options();
    opts.inTempStorage = bp.get(bCapacity);
    setupDensityAndFormat(opts, sourceDensity);
    return src;
  }

  private void onImageDecodeFinish(final InputStream src, final BitmapFactory.Options opts) throws IOException {
    buffersPool.release(opts.inTempStorage);
    src.close();
  }

  static int nearestPowerOf2(final int value) {
    if (value <= 0) { return -1; }
    int result = -1, x = value;
    while (x > 0) {
      ++result;
      x >>>= 1;
    }
    return 1 << result;
  }

  protected int resolveSampleFactor(final InputStream is, final int sourceDensity, final int width, final int height) throws IOException {
    final BitmapFactory.Options opts = new BitmapFactory.Options();
    opts.inJustDecodeBounds = true;
    final InputStream src = prepareImageOptionsAndInput(is, sourceDensity, opts);
    int result = 1;
    try {
      BitmapFactory.decodeResourceStream(null, null, src, null, opts);
      final int inW = opts.outWidth, inH = opts.outHeight;
      if (inW > width || inH > height) {
        final int factor = inW > inH ? inW / width : inH / height;
        if (factor > 1) {
          result = factor;
          final int p = nearestPowerOf2(factor);
          final int maxDistance = 3;
          if (result - p < maxDistance) { result = p; }
          if (DEBUG) { Log.d(TAG, "Sampling: factor=" + factor + ", p=" + p + ", result=" + result); }
        }
      }
    } finally {
      // recycle
      onImageDecodeFinish(src, opts);
    }
    return result;
  }

  protected Drawable decodeStream(final InputStream is, final int sourceDensity, final BitmapFactory.Options options) throws IOException {
    final BitmapFactory.Options opts = options != null ? options : new BitmapFactory.Options();
    final InputStream src = prepareImageOptionsAndInput(is, sourceDensity, opts);
    try {
      final Bitmap bm = BitmapFactory.decodeResourceStream(null, null, src, null, opts);
      final Drawable res = bm != null ? new BitmapDrawable(resources, bm) : null;
      if (DEBUG) { Log.d(TAG, "Image decoded: " + opts.outWidth + "x" + opts.outHeight + ", res=" + res); }
      return res;
    } catch (final OutOfMemoryError e) {
      // I know, it's bad to catch error but files can be VERY big!
      return null;
    } finally {
      // recycle
      onImageDecodeFinish(src, opts);
    }
  }

  /**
   * This our barrier for images loading tasks.
   * @return true if task can continue it's work and false if it's interrupted
   */
  synchronized boolean waitForPause() {
    try {
      while (paused) { wait(); }
      return true;
    } catch (final InterruptedException e) {
      return false;
    }
  }

  /**
   * Pause all future loading tasks.
   */
  public synchronized void pauseLoading() {
    this.paused = true;
  }
  /**
   * Resume all the loading tasks.
   */
  public synchronized void resumeLoading() {
    this.paused = false;
    notifyAll();
  }

  /**
   * Image loader task.
   * @param <T> image type
   * @author Roman Mazur - Stanfy (http://www.stanfy.com)
   */
  protected static class ImageLoader<T extends CachedImage> implements Callable<Void> {

    /** Image URL. */
    private final String url;
    /** Loader key. */
    private final String key;

    /** Images manager. */
    private final ImagesManager<T> imagesManager;
    /** Images DAO. */
    private final ImagesDAO<T> imagesDAO;
    /** Downloader. */
    private final Downloader downloader;

    /** Future task instance. */
    final FutureTask<Void> future;

    /** Targets. */
    private final ArrayList<ImageHolder> targets = new ArrayList<ImageHolder>();
    /** Main GUI view. */
    private ImageHolder mainTarget;
    /** Result drawable. */
    private Drawable resultDrawable;
    /** Resolved result flag. */
    private boolean resultResolved = false, started = false;

    public ImageLoader(final String url, final String key, final ImagesManagerContext<T> imagesContext) {
      this.url = url;
      this.key = key;
      this.imagesManager = imagesContext.getImagesManager();
      this.downloader = imagesContext.getDownloader();
      this.imagesDAO = imagesContext.getImagesDAO();
      this.future = new FutureTask<Void>(this);
    }

    /** Called from UI thread. */
    public boolean addTarget(final ImageHolder imageHolder) {
      if (future.isCancelled()) { return false; } // we should start a new task
      if (!future.isDone()) {
        // normal case
        synchronized (this) {
          if (started) { imageHolder.onStart(this, url); }
          if (resultResolved) {
            imagesManager.setImage(imageHolder, resultDrawable, false, false);
            imageHolder.onFinish(url, resultDrawable);
          } else {
            imageHolder.currentLoader = this;
            targets.add(imageHolder);
            if (mainTarget == null) { mainTarget = imageHolder; }
          }
        }
      } else {
        // we finished
        imagesManager.setImage(imageHolder, resultDrawable, false, false);
      }
      return true;
    }

    public void removeTarget(final ImageHolder imageHolder) {
      imageHolder.onCancel(url);
      synchronized (this) {
        targets.remove(imageHolder);
        if (targets.isEmpty()) { future.cancel(true); }
      }
    }

    protected void safeImageSet(final T cachedImage, final Drawable source) {
      final Drawable d;
      synchronized (this) {
        resultResolved = true;
        if (source == null) { return; }
        d = memCacheImage(source);
        resultDrawable = d;
      }

      final String url = this.url;
      if (DEBUG) { Log.v(TAG, "Post setting drawable for " + cachedImage.getUrl()); }
      mainTarget.post(new Runnable() {
        @Override
        public void run() {
          if (DEBUG) { Log.v(TAG, "Set drawable for " + cachedImage.getUrl()); }

          final ArrayList<ImageHolder> targets = ImageLoader.this.targets;
          final int count = targets.size();
          if (count > 0) {
            for (int i = 0; i < count; i++) {
              final ImageHolder imageHolder = targets.get(i);
              if (DEBUG) { Log.d(TAG, "Try to set " + imageHolder + " - " + url); }
              setImageToHolder(imageHolder, d);
            }
          } else if (DEBUG) {
            Log.w(TAG, "set drawable: have no targets in list");
          }

        }
      });
    }

    private void setImageToHolder(final ImageHolder imageHolder, final Drawable d) {
      synchronized (imageHolder) {
        final String currentUrl = imageHolder.currentUrl;
        if (currentUrl != null && currentUrl.equals(url)) {
          imagesManager.setImage(imageHolder, d, false, true);
        } else {
          if (DEBUG) { Log.d(TAG, "Skip set for " + imageHolder); }
        }
      }
    }

    protected Drawable setLocalImage(final T cachedImage) throws IOException {
      final Context x = mainTarget.context;
      if (x == null) { throw new IOException("Context is null"); }
      final Drawable d = imagesManager.readLocal(cachedImage, x, mainTarget);
      if (d != null) {
        imagesDAO.updateUsageTimestamp(cachedImage);
      }
      safeImageSet(cachedImage, d);
      return d;
    }

    protected Drawable setRemoteImage(final T cachedImage) throws IOException {
      if (!url.startsWith("http")) { return null; }
      final Context x = mainTarget.context;
      if (x == null) { throw new IOException("Context is null"); }
      cachedImage.setType(mainTarget.getImageType());
      imagesManager.makeImageLocal(imagesDAO, x, cachedImage, downloader);
      return setLocalImage(cachedImage);
    }

    private BitmapDrawable prepare(final BitmapDrawable bd) {
      int dstW = mainTarget.getRequiredWidth(), dstH = mainTarget.getRequiredHeight();
      if (dstW <= 0 || dstH <= 0 || mainTarget.skipScaleBeforeCache()) {
        if (DEBUG) { Log.d(TAG, "Skip scaling for " + mainTarget + " skip flag: " + mainTarget.skipScaleBeforeCache()); }
        return bd;
      }

      final Bitmap map = bd.getBitmap();
      final int w = bd.getIntrinsicWidth(), h = bd.getIntrinsicHeight();

      if (w <= dstW && h <= dstH) { return bd; }

      final double ratio = (double)w / h;
      if (w > h) {
        dstH = (int)(dstW / ratio);
      } else {
        dstW = (int)(dstH * ratio);
      }

      if (dstW <= 0 || dstH <= 0) { return bd; }

      final Bitmap scaled = Bitmap.createScaledBitmap(map, dstW, dstH, true);
      scaled.setDensity(imagesManager.resources.getDisplayMetrics().densityDpi);
      return new BitmapDrawable(imagesManager.resources, scaled);
    }

    private Drawable memCacheImage(final Drawable d) {
      Drawable result = d;
      if (d instanceof BitmapDrawable) {
        final BitmapDrawable bmd = (BitmapDrawable)d;
        result = prepare(bmd);
        if (result != bmd) { bmd.getBitmap().recycle(); }
      }
      imagesManager.memCacheImage(url, result);
      return result;
    }

    private void start() {
      synchronized (this) {
        final ArrayList<ImageHolder> targets = this.targets;
        final int count = targets.size();
        final String url = this.url;
        for (int i = 0; i < count; i++) {
          targets.get(i).onStart(this, url);
        }
        started = true;
      }
    }

    private void finish() {
      final ArrayList<ImageHolder> targets = this.targets;
      final int count = targets.size();
      final Drawable d = this.resultDrawable;
      for (int i = 0; i < count; i++) {
        targets.get(i).onFinish(url, d);
      }
    }

    private void cancel() {
      final ArrayList<ImageHolder> targets = this.targets;
      final int count = targets.size();
      final String url = this.url;
      for (int i = 0; i < count; i++) {
        targets.get(i).onCancel(url);
      }
    }

    private void error(final Throwable e) {
      final ArrayList<ImageHolder> targets = this.targets;
      final int count = targets.size();
      final String url = this.url;
      for (int i = 0; i < count; i++) {
        targets.get(i).onError(url, e);
      }
    }

    @SuppressWarnings("unused")
    @Override
    public Void call() {
      if (DEBUG) { Log.d(TAG, "Start image task"); }
      final String url = this.url;
      try {
        start();

        if (!imagesManager.waitForPause()) {
          cancel();
          return null;
        }

        T cachedImage = imagesDAO.getCachedImage(url);
        if (cachedImage == null) {
          cachedImage = imagesDAO.createCachedImage(url);
          if (cachedImage == null) {
            Log.w(TAG, "Cached image info was not created for " + url);
            return null;
          }
        }

        Drawable d = null;

        if (cachedImage.isLoaded()) {
          if (Thread.interrupted()) {
            cancel();
            return null;
          }
          d = setLocalImage(cachedImage);
          if (DEBUG) { Log.d(TAG, "Image " + cachedImage.getId() + "-local"); }
        }

        if (d == null) {
          if (Thread.interrupted()) {
            cancel();
            return null;
          }
          d = setRemoteImage(cachedImage);
          if (DEBUG) { Log.d(TAG, "Image " + cachedImage.getId() + "-remote"); }
        }

        if (d == null) {
          Log.w(TAG, "Image " + cachedImage.getUrl() + " is not resolved");
        }
        finish();

      } catch (final MalformedURLException e) {
        Log.e(TAG, "Bad URL: " + url + ". Loading canceled.", e);
        error(e);
      } catch (final IOException e) {
        if (DEBUG_IO) { Log.e(TAG, "IO error for " + url + ": " + e.getMessage()); }
        error(e);
      } catch (final Exception e) {
        Log.e(TAG, "Cannot load image " + url, e);
        error(e);
      } finally {
        final boolean removed = imagesManager.currentLoads.remove(key, this);
        if (!removed && DEBUG) {
          Log.w(TAG, "Incorrect loader in currents for " + key);
        }
      }
      return null;
    }

  }

  /**
   * An InputStream that skips the exact number of bytes provided, unless it reaches EOF.
   * See http://code.google.com/p/android/issues/detail?id=6066.
   */
  static class FlushedInputStream extends FilterInputStream {
    public FlushedInputStream(final InputStream inputStream) {
      super(inputStream);
    }

    @Override
    public long skip(final long n) throws IOException {
      long totalBytesSkipped = 0L;
      final InputStream in = this.in;
      while (totalBytesSkipped < n) {
        long bytesSkipped = in.skip(n - totalBytesSkipped);
        if (bytesSkipped == 0L) {
          final int b = read();
          if (b < 0) {
            break;  // we reached EOF
          } else {
            bytesSkipped = 1; // we read one byte
          }
        }
        totalBytesSkipped += bytesSkipped;
      }
      return totalBytesSkipped;
    }
  }

  /**
   * Image holder view.
   * @author Roman Mazur - Stanfy (http://www.stanfy.com)
   * @param <T> view type
   */
  public abstract static class ImageHolder {
    /** Context instance. */
    Context context;
    /** Current image URL. Set this field from the GUI thread only! */
    String currentUrl;
    /** Listener. */
    ImagesLoadListener listener;
    /** Current loader. */
    ImageLoader<?> currentLoader;
    /** Loader key. */
    private String loaderKey;

    /** @param context context instance */
    public ImageHolder(final Context context) {
      this.context = context;
      reset();
    }

    /* access */
    /** @param listener the listener to set */
    public final void setListener(final ImagesLoadListener listener) { this.listener = listener; }
    /** @return the context */
    public Context getContext() { return context; }

    /* actions */
    /** Reset holder state. Must be called from the main thread. */
    void reset() {
      currentUrl = null;
      loaderKey = null;
    }
    public void touch() { }
    public abstract void setImage(final Drawable d, final boolean animate);
    public void setLoadingImage(final Drawable d) { setImage(d, false); }
    public abstract void post(final Runnable r);
    public void postpone(final Runnable r) { post(r); }
    public void destroy() {
      context = null;
    }
    final void performCancellingLoader() {
      final String url = currentUrl;
      if (DEBUG) { Log.d(TAG, "Cancel " + url); }
      if (url != null) {
        final ImageLoader<?> loader = this.currentLoader;
        if (loader != null) { loader.removeTarget(this); }
      }
    }

    final void onStart(final ImageLoader<?> loader, final String url) {
      this.currentLoader = loader;
      if (listener != null) { listener.onLoadStart(this, url); }
    }
    final void onFinish(final String url, final Drawable drawable) {
      this.currentLoader = null;
      if (listener != null) { listener.onLoadFinished(this, url, drawable); }
    }
    final void onError(final String url, final Throwable exception) {
      this.currentLoader = null;
      if (listener != null) { listener.onLoadError(this, url, exception); }
    }
    final void onCancel(final String url) {
      this.currentLoader = null;
      if (listener != null) { listener.onLoadCancel(this, url); }
      this.currentUrl = null;
    }

    /* parameters */
    public abstract int getRequiredWidth();
    public abstract int getRequiredHeight();
    public boolean isDynamicSize() { return getRequiredWidth() <= 0 || getRequiredHeight() <= 0; }
    public boolean isMatchingParentButNotMeasured() { return false; }
    public Drawable getLoadingImage() { return null; }
    public int getImageType() { return 0; }
    public int getSourceDensity() { return -1; }
    String getLoaderKey() {
      if (loaderKey == null) {
        loaderKey = currentUrl + "!" + getRequiredWidth() + "x" + getRequiredWidth();
      }
      return loaderKey;
    }

    /* options */
    public boolean skipScaleBeforeCache() { return false; }
    public boolean skipLoadingImage() { return false; }
    public boolean useSampling() { return false; }
  }

  /**
   * Image holder views.
   * @author Roman Mazur (Stanfy - http://www.stanfy.com)
   * @param <T> view type
   */
  public abstract static class ViewImageHolder<T extends View> extends ImageHolder {
    /** View instance. */
    T view;
    /** Handler instance. */
    private Handler handler;

    public ViewImageHolder(final T view) {
      super(view.getContext());
      this.view = view;
      touch();
    }
    @Override
    public int getSourceDensity() {
      if (view instanceof RemoteImageDensityProvider) {
        return ((RemoteImageDensityProvider)view).getSourceDensity();
      }
      return super.getSourceDensity();
    }
    @Override
    public void touch() {
      final T view = this.view;
      if (view != null && view instanceof ImagesLoadListenerProvider) {
        this.listener = ((ImagesLoadListenerProvider)view).getImagesLoadListener();
      }
    }
    @Override
    public void post(final Runnable r) {
      if (context instanceof Activity) {
        ((Activity)context).runOnUiThread(r);
      } else {
        if (DEBUG) { Log.d(TAG, "Context is not an activity, cannot use runOnUiThread"); }
        view.post(r);
      }
    }
    @Override
    public void postpone(final Runnable r) {
      post(new Runnable() {
        @Override
        public void run() {
          if (handler == null) { handler = new Handler(); }
          handler.post(r);
        }
      });
    }
    @Override
    public int getRequiredHeight() {
      final View view = this.view;
      final LayoutParams params = view.getLayoutParams();
      if (params == null || params.height == LayoutParams.WRAP_CONTENT) { return -1; }
      final int h = view.getHeight();
      return h > 0 ? h : params.height;
    }
    @Override
    public int getRequiredWidth() {
      final View view = this.view;
      final LayoutParams params = view.getLayoutParams();
      if (params == null || params.width == LayoutParams.WRAP_CONTENT) { return -1; }
      final int w = view.getWidth();
      return w > 0 ? w : params.width;
    }
    @Override
    public boolean isMatchingParentButNotMeasured() {
      final View view = this.view;
      final LayoutParams params = view.getLayoutParams();
      if (params == null) { return false; }
      return params.width == LayoutParams.MATCH_PARENT && view.getWidth() == 0
          || params.height == LayoutParams.MATCH_PARENT && view.getHeight() == 0;
    }
    @Override
    public void destroy() {
      super.destroy();
      view = null;
    }
  }

}

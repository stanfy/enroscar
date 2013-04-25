package com.stanfy.enroscar.images;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.ResponseCache;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.stanfy.enroscar.beans.BeansContainer;
import com.stanfy.enroscar.beans.EnroscarBean;
import com.stanfy.enroscar.beans.InitializingBean;
import com.stanfy.enroscar.images.cache.ImageMemoryCache;
import com.stanfy.enroscar.io.BuffersPool;
import com.stanfy.enroscar.io.FlushedInputStream;
import com.stanfy.enroscar.io.IoUtils;
import com.stanfy.enroscar.io.PoolableBufferedInputStream;
import com.stanfy.enroscar.net.UrlConnectionBuilder;
import com.stanfy.enroscar.net.cache.EnhancedResponseCache;

/**
 * A manager that encapsulates the images downloading and caching logic.
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
@EnroscarBean(value = ImagesManager.BEAN_NAME, contextDependent = true)
public class ImagesManager implements InitializingBean {

  /** Bean name. */
  public static final String BEAN_NAME = "ImagesManager";

  /** Images cache bean name. */
  public static final String CACHE_BEAN_NAME = "ImagesCache";

  /** Image consumer factory bean name. */
  public static final String IMAGE_CONSUMER_FACTORY_NAME = "ViewImageConsumerFactory";
  
  /** Logging tag. */
  static final String TAG = BEAN_NAME;

  /** Max distance between sample factor and its nearest power of 2 to use the latter. */
  static final int MAX_POWER_OF_2_DISTANCE = 3;

  /** Pattern to cut the images sources from HTML. */
  protected static final Pattern IMG_URL_PATTERN = Pattern.compile("<img.*?src=\"(.*?)\".*?>");

  /** Debug flag. */
  private static final boolean DEBUG_IO = false;

  /** Debug flag. */
  static final boolean DEBUG = false;

  /** Resources. */
  private final Resources resources;

  /** Buffers pool. */
  private BuffersPool buffersPool;
  /** Memory cache. */
  private ImageMemoryCache memCache;
  /** Images response cache. */
  private ResponseCache imagesResponseCache;
  /** Consumer factory. */
  private ViewImageConsumerFactory consumerFactory;

  /** Target density from source. */
  private int defaultSourceDensity = 0;

  /** Images format. */
  private Bitmap.Config imagesFormat = Bitmap.Config.ARGB_8888;

  /** Current loads. */
  private final ConcurrentHashMap<String, ImageLoader> currentLoads = new ConcurrentHashMap<String, ImageLoader>(Threading.imagesWorkersCount);

  /** Paused state. */
  private boolean paused = false;

  public ImagesManager(final Context context) {
    this.resources = context.getResources();
  }

  /**
   * Set count of working threads used to load images.
   * @param count count of threads
   */
  public static void configureImageTaskExecutorsCount(final int count) {
    Threading.configureImageTasksExecutor(count);
  }

  @Override
  public void onInitializationFinished(final BeansContainer beansContainer) {
    this.buffersPool = beansContainer.getBean(BuffersPool.BEAN_NAME, BuffersPool.class);
    this.memCache = beansContainer.getBean(ImageMemoryCache.BEAN_NAME, ImageMemoryCache.class);
    if (buffersPool == null || memCache == null) {
      throw new IllegalStateException("Buffers pool and images memory cache must be initialized before images manager.");
    }
    this.imagesResponseCache = beansContainer.getBean(CACHE_BEAN_NAME, ResponseCache.class);
    if (imagesResponseCache == null) {
      Log.w(TAG, "Response cache for images is not defined");
    }
    this.consumerFactory = beansContainer.getBean(IMAGE_CONSUMER_FACTORY_NAME, ViewImageConsumerFactory.class);
  }

  /** @return images response cache instance */
  public ResponseCache getImagesResponseCache() { return imagesResponseCache; }

  /** @param imagesFormat the imagesFormat to set */
  public void setImagesFormat(final Bitmap.Config imagesFormat) { this.imagesFormat = imagesFormat; }
  /** @return images format used to create bitmaps */
  public Bitmap.Config getImagesFormat() { return imagesFormat; }

  /**
   * Configure default density of images. Every {@link ImageConsumer} can return its own density.
   * @param sourceDensity the sourceDensity to set
   */
  public void setDefaultSourceDensity(final int sourceDensity) { this.defaultSourceDensity = sourceDensity; }

  /**
   * Ensure that all the images are loaded. Not loaded images will be downloaded in this thread or in a seperate thread.
   * @param images image URLs collection
   * @param newThread true if separate thread is required
   */
  public void ensureImages(final List<String> images, final boolean newThread) {
    ensureImages(images, newThread ? getImageTaskExecutor() : null);
  }

  /**
   * @param images list of URLs to load
   * @param executor executor to run the task (if null task is solved in the current thread)
   */
  protected void ensureImages(final List<String> images, final Executor executor) {
    final EnhancedResponseCache enhancedResponseCache = imagesResponseCache instanceof EnhancedResponseCache
        ? (EnhancedResponseCache)imagesResponseCache : null;
    final Runnable task = new Runnable() {
      @Override
      public void run() {
        for (final String url : images) {
          try {
            final boolean mustRead = enhancedResponseCache == null || !enhancedResponseCache.contains(url);
            if (mustRead) {
              IoUtils.consumeStream(newConnection(url).getInputStream(), buffersPool);
            }
          } catch (final IOException e) {
            if (DEBUG_IO) { Log.e(TAG, "IO error for " + url + ": " + e.getMessage()); }
          } catch (final Exception e) {
            Log.e(TAG, "Ignored error for ensureImages", e);
          }
        }
      }
    };
    if (executor == null) {
      task.run();
    } else {
      executor.execute(task);
    }
  }
  
  /**
   * Clear the cached entities.
   * @param url image URL
   * @return true if entry was deleted
   */
  @SuppressWarnings("unused")
  public boolean clearCache(final String url) {
    memCache.remove(url);
    if (imagesResponseCache instanceof EnhancedResponseCache) {
      try {
        return ((EnhancedResponseCache) imagesResponseCache).deleteGetEntry(url);
      } catch (final IOException e) {
        Log.w(TAG, "Cannot clear disk cache for " + url, e);
        return false;
      }
    }
    if (DEBUG && imagesResponseCache != null) {
      Log.i(TAG, "Images response cache does not implement " + EnhancedResponseCache.class);
    }
    return false;
  }

  /**
   * Populate the requested image to the specified view. Called from the GUI thread.
   * @param view view instance
   * @param url image URL
   */
  public void populateImage(final View view, final String url) {
    final Object tag = view.getTag();
    ImageConsumer imageHolder = null;
    if (tag == null) {
      imageHolder = createImageConsumer(view);
      view.setTag(imageHolder);
    } else {
      if (!(tag instanceof ImageConsumer)) { throw new IllegalStateException("View already has a tag " + tag); }
      imageHolder = (ImageConsumer)tag;
    }
    populateImage(imageHolder, url);
  }

  /**
   * Cancel image loading for a view.
   * @param view view that hold an image
   */
  public void cancelImageLoading(final View view) {
    final Object tag = view.getTag();
    if (tag != null && tag instanceof ImageConsumer) {
      cancelImageLoading((ImageConsumer)tag);
    }
  }
  /**
   * Cancel image loading for a holder.
   * @param holder image holder instance
   */
  public void cancelImageLoading(final ImageConsumer holder) {
    holder.performCancellingLoader();
  }

  /**
   * Create an image holder instance for the defined view.
   * @param view view instance
   * @return image holder instance
   */
  protected ImageConsumer createImageConsumer(final View view) {
    if (consumerFactory != null) {
      return consumerFactory.createConsumer(view);
    }
    return null;
  }

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
    if (tag == null || !(tag instanceof ImageConsumer)) { return null; }
    final Drawable res = getFromMemCache(url, (ImageConsumer)tag);
    if (res == null) { return null; }
    return decorateDrawable((ImageConsumer)tag, res);
  }

  public void populateImage(final ImageConsumer imageHolder, final String url) {
    if (imageHolder.isMatchingParentButNotMeasured()) {
      imageHolder.post(new Runnable() {
        @Override
        public void run() { populateImageNow(imageHolder, url); }
      });
    } else {
      populateImageNow(imageHolder, url);
    }
  }

  public void populateImageNow(final ImageConsumer imageHolder, final String url) {
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
    startImageLoaderTask(imageHolder);
  }

  /**
   * @return an executor for image tasks
   */
  protected Executor getImageTaskExecutor() { return Threading.getImageTasksExecutor(); }

  /**
   * @param holder image consumer
   * @return drawable to display while image is being loaded
   */
  protected Drawable getLoadingDrawable(final ImageConsumer holder) {
    final Drawable d = holder.getLoadingImage();
    final int color = 0xffeeeeee;
    return d != null ? d : new ColorDrawable(color);
  }

  /**
   * @param holder image consumer
   * @param drawable resulting drawable
   * @return decorated drawable
   */
  protected Drawable decorateDrawable(final ImageConsumer holder, final Drawable drawable) { return drawable; }

  /**
   * It must be executed in the main thread.
   * @param imageHolder image consumer
   * @param drawable incoming drawable
   * @param preloader preloading image flag
   * @param animate whether to animate image change
   */
  protected final void setImage(final ImageConsumer imageHolder, final Drawable drawable, final boolean preloader, final boolean animate) {
    final Drawable d = decorateDrawable(imageHolder, drawable);
    if (preloader) {
      imageHolder.setLoadingImage(d);
    } else {
      imageHolder.setImage(d, animate);
    }
    imageHolder.reset();
  }
  /**
   * Set preloader.
   * @param holder image holder instance
   */
  private void setLoadingImage(final ImageConsumer holder) {
    if (!holder.skipLoadingImage()) {
      final Drawable d = !holder.isDynamicSize() ? getLoadingDrawable(holder) : null;
      setImage(holder, d, true, false/* ignored */);
    }
  }

  /**
   * @param url image URL
   * @param holder image consumer (to get target size)
   * @return cached drawable
   */
  protected Drawable getFromMemCache(final String url, final ImageConsumer holder) {
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
        || holder.allowSmallImagesFromCache()
        || holder.skipScaleBeforeCache()
        || Math.abs(holder.getRequiredWidth() - result.getIntrinsicWidth()) < gap
        || Math.abs(holder.getRequiredHeight() - result.getIntrinsicHeight()) < gap;
    if (DEBUG) { Log.v(TAG, "Use mem cache " + suits + " for " + url); }
    return suits ? result : null;
  }

  /**
   * @param imageHolder image holder to process
   */
  protected void startImageLoaderTask(final ImageConsumer imageHolder) {
    final String key = imageHolder.getLoaderKey();
    if (DEBUG) { Log.d(TAG, "Key " + key); }
    ImageLoader loader = currentLoads.get(key);
    if (loader != null) {
      final boolean added = loader.addTarget(imageHolder);
      if (!added) { loader = null; }
    }
    if (loader == null) {
      if (DEBUG) { Log.d(TAG, "Start a new task"); }
      loader = new ImageLoader(imageHolder.currentUrl, key, this);
      final boolean added = loader.addTarget(imageHolder);
      if (!added) { throw new IllegalStateException("Cannot add target to the new loader"); }
      currentLoads.put(key, loader);
      if (DEBUG) { Log.d(TAG, "Current loaders count: " + currentLoads.size()); }
      final Executor executor = getImageTaskExecutor();
      executor.execute(loader.future);
    } else if (DEBUG) {
      Log.d(TAG, "Joined to the existing task");
    }
  }

  /**
   * Add image to memory cache.
   * @param url image URL
   * @param d drawable instance
   */
  protected void memCacheImage(final String url, final Drawable d) {
    if (d instanceof BitmapDrawable) {
      if (DEBUG) { Log.d(TAG, "Memcache for " + url); }
      memCache.putElement(url, ((BitmapDrawable)d).getBitmap());
    }
  }

  /**
   * @param url image URL
   * @param holder image consumer
   * @return drawable with loaded image
   * @throws IOException if error happens
   */
  protected Drawable readImage(final String url, final ImageConsumer holder) throws IOException {
    if (holder == null) { return null; }
    InputStream imageStream = newConnection(url).getInputStream();
    final BitmapFactory.Options options = new BitmapFactory.Options();

    if (holder.useSampling() && !holder.isDynamicSize()) {
      options.inSampleSize = resolveSampleFactor(imageStream, holder.getSourceDensity(), holder.getRequiredWidth(), holder.getRequiredHeight());
      // image must have been cached now
      imageStream = newConnection(url).getInputStream();
    }

    final Drawable d = decodeStream(imageStream, holder.getSourceDensity(), options);
    return d;
  }

  private InputStream prepareImageOptionsAndInput(final InputStream is, final int sourceDensity, final BitmapFactory.Options options) {
    final BuffersPool bp = buffersPool;
    final int bCapacity = BuffersPool.DEFAULT_SIZE_FOR_IMAGES;

    InputStream src = new FlushedInputStream(is);
    if (!src.markSupported()) { src = new PoolableBufferedInputStream(src, bCapacity, bp); }

    final BitmapFactory.Options opts = options != null ? options : new BitmapFactory.Options();
    opts.inTempStorage = bp.get(bCapacity);
    options.inDensity = sourceDensity > 0 ? sourceDensity : this.defaultSourceDensity;
    options.inPreferredConfig = imagesFormat;
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

  /**
   * Possible factors: <code>2, 4, 7, 8, (8 + {@link #MAX_POWER_OF_2_DISTANCE} = 11), 12, 13, 14, 15, 16, 16 + {@link #MAX_POWER_OF_2_DISTANCE}...</code>
   */
  static int calculateSampleFactor(final int inW, final int inH, final int width, final int height) {
    int result = 1;
    final int factor = inW > inH ? inW / width : inH / height;
    if (factor > 1) {
      result = factor;
      final int p = nearestPowerOf2(factor);
      if (result - p < MAX_POWER_OF_2_DISTANCE) { result = p; }
      if (DEBUG) { Log.d(TAG, "Sampling: factor=" + factor + ", p=" + p + ", result=" + result); }
    }
    return result;
  }

  /**
   * @param is image input stream
   * @param sourceDensity known image density
   * @param width target width
   * @param height target height
   * @return sampling factor
   * @throws IOException if error happens
   */
  protected int resolveSampleFactor(final InputStream is, final int sourceDensity, final int width, final int height) throws IOException {
    final BitmapFactory.Options opts = new BitmapFactory.Options();
    opts.inJustDecodeBounds = true;
    final InputStream src = prepareImageOptionsAndInput(is, sourceDensity, opts);
    int result = 1;
    try {
      BitmapFactory.decodeResourceStream(null, null, src, null, opts);
      final int inW = opts.outWidth, inH = opts.outHeight;
      if (inW > width || inH > height) {
        result = calculateSampleFactor(inW, inH, width, height);
      }
    } finally {
      // recycle
      onImageDecodeFinish(src, opts);
    }
    return result;
  }

  /**
   * @param is image input stream
   * @param sourceDensity known image density
   * @param options decoding options
   * @return drawable with a loaded image
   * @throws IOException if error happens
   */
  protected Drawable decodeStream(final InputStream is, final int sourceDensity, final BitmapFactory.Options options) throws IOException {
    final BitmapFactory.Options opts = options != null ? options : new BitmapFactory.Options();
    final InputStream src = prepareImageOptionsAndInput(is, sourceDensity, opts);
    try {
      final Bitmap bm = BitmapFactory.decodeResourceStream(null, null, src, null, opts);
      final Drawable res = bm != null ? new BitmapDrawable(resources, bm) : null;
      if (DEBUG) { Log.d(TAG, "Image decoded: " + opts.outWidth + "x" + opts.outHeight + ", res=" + res); }
      return res;
    } catch (final OutOfMemoryError e) {
      // I know, it's bad to catch errors but files can be VERY big!
      return null;
    } finally {
      // recycle
      onImageDecodeFinish(src, opts);
    }
  }

  /**
   * @param url image URL
   * @return URL connection that can be handled by images cache bean ({@link #CACHE_BEAN_NAME})
   * @throws IOException in case of I/O errors
   */
  protected URLConnection newConnection(final String url) throws IOException {
    return new UrlConnectionBuilder().setUrl(url).setCacheManagerName(CACHE_BEAN_NAME).create();
  }

  /**
   * This our barrier for images loading tasks.
   * @return true if task can continue it's work and false if it's interrupted
   */
  final synchronized boolean waitForPause() {
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
  public final synchronized void pauseLoading() {
    this.paused = true;
  }
  /**
   * Resume all the loading tasks.
   */
  public final synchronized void resumeLoading() {
    this.paused = false;
    notifyAll();
  }

  /**
   * Image loader task.
   * @author Roman Mazur - Stanfy (http://www.stanfy.com)
   */
  public static class ImageLoader implements Callable<Void> {

    /** Image URL. */
    private final String url;
    /** Loader key. */
    private final String key;

    /** Images manager. */
    private final ImagesManager imagesManager;

    /** Future task instance. */
    final FutureTask<Void> future;

    /** Targets. */
    private final ArrayList<ImageConsumer> targets = new ArrayList<ImageConsumer>();
    /** Main GUI view. */
    private ImageConsumer mainTarget;
    /** Result drawable. */
    private Drawable resultDrawable;
    /** Resolved result flag. */
    private boolean resultResolved = false, started = false;

    /**
     * @param url image URL
     * @param key image loader key
     * @param imagesManager manager instance
     */
    public ImageLoader(final String url, final String key, final ImagesManager imagesManager) {
      this.url = url;
      this.key = key;
      this.imagesManager = imagesManager;
      this.future = new FutureTask<Void>(this);
    }

    /* Called from UI thread. */
    public boolean addTarget(final ImageConsumer imageHolder) {
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
            if (mainTarget == null) {
              mainTarget = imageHolder;
            }
          }
        }
      } else {
        // we finished
        imagesManager.setImage(imageHolder, resultDrawable, false, false);
      }
      return true;
    }

    public void removeTarget(final ImageConsumer imageHolder) {
      imageHolder.onCancel(url);
      synchronized (this) {
        targets.remove(imageHolder);
        if (mainTarget == imageHolder) {
          if (DEBUG) { Log.d(TAG, "Dropping main target to null"); }
          mainTarget = null;
        }
        if (targets.isEmpty()) {
          if (!future.cancel(true)) {
            if (DEBUG) { Log.d(TAG, "Can't cancel task so let's try to remove loader manually"); }
            imagesManager.currentLoads.remove(key, this);
          }
        }
      }
    }

    private void safeImageSet(final Drawable source) {
      final Drawable d;
      synchronized (this) {
        resultResolved = true;
        if (source == null) { return; }
        d = memCacheImage(source);
        resultDrawable = d;
      }

      final String url = this.url;
      if (DEBUG) { Log.v(TAG, "Post setting drawable for " + url); }
      if (mainTarget == null && targets != null && targets.size() > 0) { mainTarget = targets.get(0); }
      if (mainTarget == null) { return; }
      mainTarget.post(new Runnable() {
        @Override
        public void run() {
          if (DEBUG) { Log.v(TAG, "Set drawable for " + url); }

          final ArrayList<ImageConsumer> targets = ImageLoader.this.targets;
          final int count = targets.size();
          if (count > 0) {
            for (int i = 0; i < count; i++) {
              final ImageConsumer imageHolder = targets.get(i);
              if (DEBUG) { Log.d(TAG, "Try to set " + imageHolder + " - " + url); }
              setImageToHolder(imageHolder, d);
            }
          } else if (DEBUG) {
            Log.w(TAG, "set drawable: have no targets in list");
          }

        }
      });
    }

    private void setImageToHolder(final ImageConsumer imageHolder, final Drawable d) {
      synchronized (imageHolder) {
        final String currentUrl = imageHolder.currentUrl;
        if (currentUrl != null && currentUrl.equals(url)) {
          imagesManager.setImage(imageHolder, d, false, true);
        } else {
          if (DEBUG) { Log.d(TAG, "Skip set for " + imageHolder); }
        }
      }
    }

    private Bitmap prepare(final Bitmap map) {
      if (mainTarget == null) { return map; }
      int dstW = mainTarget.getRequiredWidth(), dstH = mainTarget.getRequiredHeight();
      if (dstW <= 0 || dstH <= 0 || mainTarget.skipScaleBeforeCache()) {
        if (DEBUG) { Log.d(TAG, "Skip scaling for " + mainTarget + " skip flag: " + mainTarget.skipScaleBeforeCache()); }
        return map;
      }

      final int w = map.getWidth(), h = map.getHeight();

      if (w <= dstW && h <= dstH) { return map; }

      final double ratio = (double)w / h;
      if (w > h) {
        dstH = (int)(dstW / ratio);
      } else {
        dstW = (int)(dstH * ratio);
      }

      if (dstW <= 0 || dstH <= 0) { return map; }

      final Bitmap scaled = Bitmap.createScaledBitmap(map, dstW, dstH, true);
      scaled.setDensity(imagesManager.resources.getDisplayMetrics().densityDpi);
      return scaled;
    }

    private Drawable memCacheImage(final Drawable d) {
      Drawable result = d;
      if (d instanceof BitmapDrawable) {
        Bitmap incomeBitmap = ((BitmapDrawable) d).getBitmap();
        Bitmap resultBitmap = prepare(incomeBitmap);
        if (resultBitmap != incomeBitmap) { incomeBitmap.recycle(); }
        result = new BitmapDrawable(imagesManager.resources, resultBitmap);
      }
      imagesManager.memCacheImage(url, result);
      return result;
    }

    private void start() {
      synchronized (this) {
        final ArrayList<ImageConsumer> targets = this.targets;
        final int count = targets.size();
        final String url = this.url;
        for (int i = 0; i < count; i++) {
          targets.get(i).onStart(this, url);
        }
        started = true;
      }
    }

    private void finish() {
      final ArrayList<ImageConsumer> targets = this.targets;
      final int count = targets.size();
      final Drawable d = this.resultDrawable;
      for (int i = 0; i < count; i++) {
        targets.get(i).onFinish(url, d);
      }
    }

    private void cancel() {
      final ArrayList<ImageConsumer> targets = this.targets;
      final int count = targets.size();
      final String url = this.url;
      for (int i = 0; i < count; i++) {
        targets.get(i).onCancel(url);
      }
    }

    private void error(final Throwable e) {
      final ArrayList<ImageConsumer> targets = this.targets;
      final int count = targets.size();
      final String url = this.url;
      for (int i = 0; i < count; i++) {
        targets.get(i).onError(url, e);
      }
    }

    @Override
    public Void call() {
      if (DEBUG) { Log.d(TAG, "Start image task"); }
      try {

        start();

        if (!imagesManager.waitForPause()) {
          cancel();
          return null;
        }

        final Drawable d = imagesManager.readImage(url, mainTarget);
        if (d == null) {
          Log.w(TAG, "Image " + url + " is not resolved");
        }
        safeImageSet(d);

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
        if (DEBUG) {
          Log.d(TAG, "Current loaders count: " + imagesManager.currentLoads.size());
          if (!removed) { Log.w(TAG, "Incorrect loader in currents for " + key); }
        }

      }
      return null;
    }

  }

}

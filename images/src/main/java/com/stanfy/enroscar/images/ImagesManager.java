package com.stanfy.enroscar.images;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import com.stanfy.enroscar.beans.BeansContainer;
import com.stanfy.enroscar.beans.EnroscarBean;
import com.stanfy.enroscar.beans.InitializingBean;
import com.stanfy.enroscar.images.cache.ImageMemoryCache;
import com.stanfy.enroscar.io.BuffersPool;
import com.stanfy.enroscar.net.cache.EnhancedResponseCache;

import java.io.IOException;
import java.net.ResponseCache;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

/**
 * A manager that encapsulates the images downloading and caching logic.
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
@EnroscarBean(value = ImagesManager.BEAN_NAME, contextDependent = true)
public class ImagesManager implements InitializingBean {

  /** Bean name. */
  public static final String BEAN_NAME = "enroscar.ImagesManager";

  /** Images cache bean name. */
  public static final String CACHE_BEAN_NAME = "enroscar.ImagesCache";

  /** Image consumer factory bean name. */
  public static final String IMAGE_CONSUMER_FACTORY_NAME = "enroscar.ViewImageConsumerFactory";

  /** UrlConnectionBuilderFactory bean name. */
  public static final String CONNECTION_BUILDER_FACTORY_NAME = "enroscar.ImagesUrlConnectionBuilderFactory";

  /** Logging tag. */
  static final String TAG = BEAN_NAME;
  /** Debug flag. */
  static final boolean DEBUG = false;
  /** Debug flag. */
  static final boolean DEBUG_IO = false;

  /** Max distance between sample factor and its nearest power of 2 to use the latter. */
  static final int MAX_POWER_OF_2_DISTANCE = 3;

  /** Current loads. */
  // TODO make private
  final ConcurrentHashMap<String, ImageLoader> currentLoads = new ConcurrentHashMap<String, ImageLoader>(Threading.imagesWorkersCount);

  /** Application context. */
  private final Context context;

  /** Buffers pool. */
  private BuffersPool buffersPool;
  /** Memory cache. */
  private ImageMemoryCache memCache;
  /** Images response cache. */
  private EnhancedResponseCache imagesResponseCache;
  /** Consumer factory. */
  private ViewImageConsumerFactory consumerFactory;

  /** Paused state. */
  private boolean paused = false;

  /** Debug flag. */
  boolean debug = DEBUG;

  public ImagesManager(final Context context) {
    this.context = context.getApplicationContext();
  }

  /**
   * Set count of working threads used to load images.
   * @param count count of threads
   */
  public static void configureImageTaskExecutorsCount(final int count) {
    Threading.configureImageTasksExecutor(count);
  }


  /** @return application context */
  public Context getContext() {
    return context;
  }

  /** @return application resources */
  public Resources getResources() {
    return context.getResources();
  }

  /**
   * Set verbose logging flag.
   * @param value debug flag value
   */
  public void setDebug(final boolean value) {
    this.debug = value;
  }


  BuffersPool getBuffersPool() { return buffersPool; }

  @Override
  public void onInitializationFinished(final BeansContainer beansContainer) {
    this.buffersPool = beansContainer.getBean(BuffersPool.class.getName(), BuffersPool.class);
    this.memCache = beansContainer.getBean(ImageMemoryCache.BEAN_NAME, ImageMemoryCache.class);
    if (buffersPool == null || memCache == null) {
      throw new IllegalStateException("Buffers pool and images memory cache must be initialized before images manager.");
    }

    this.imagesResponseCache = (EnhancedResponseCache) beansContainer.getBean(CACHE_BEAN_NAME, ResponseCache.class);
    if (imagesResponseCache == null) {
      Log.w(TAG, "Response cache for images is not defined");
    }

    this.consumerFactory = beansContainer.getBean(IMAGE_CONSUMER_FACTORY_NAME, ViewImageConsumerFactory.class);

    if (!beansContainer.containsBean(CONNECTION_BUILDER_FACTORY_NAME)) {
      throw new IllegalStateException("Images connection builder factory is not defined: bean ImagesManager.CONNECTION_BUILDER_FACTORY_NAME ("
          + CONNECTION_BUILDER_FACTORY_NAME + ")");
    }
  }

  /** @return images response cache instance */
  public ResponseCache getImagesResponseCache() { return (ResponseCache)imagesResponseCache; }

  public boolean isPresentOnDisk(final String url) {
    return url.startsWith(ContentResolver.SCHEME_FILE)
        || url.startsWith(ContentResolver.SCHEME_CONTENT)
        || url.startsWith(ContentResolver.SCHEME_ANDROID_RESOURCE)
        || imagesResponseCache.contains(url);
  }

  /**
   * @param images list of requests to load
   * @param executor executor to run the task (if null task is solved in the current thread)
   */
  public void ensureImages(final List<ImageRequest> images, final Executor executor) {
    final Runnable task = new Runnable() {
      @Override
      public void run() {
        for (final ImageRequest request : images) {
          try {
            request.storeToDisk();
          } catch (final IOException e) {
            if (DEBUG_IO) { Log.e(TAG, "IO error for " + request.url + ": " + e.getMessage()); }
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
   * Start building image requests.
   * @return image requests builder
   */
  public ImageRequestsBuilder load() {
    return new ImageRequestsBuilder(this);
  }

  /**
   * Clear the cached entities.
   * @param url image URL
   * @return true if entry was deleted
   */
  public boolean clearCache(final String url) {
    memCache.remove(url);
    try {
      return imagesResponseCache.deleteGetEntry(url);
    } catch (final IOException e) {
      Log.w(TAG, "Cannot clear disk cache for " + url, e);
      return false;
    }
  }

  /**
   * Populate the requested image to the specified view. Called from the GUI thread.
   * @param view view instance
   * @param url image URL
   */
  public void populateImage(final View view, final String url) {
    final Object tag = view.getTag();
    ImageConsumer consumer = null;
    if (tag == null) {
      consumer = createImageConsumer(view);
      view.setTag(consumer);
    } else {
      if (!(tag instanceof ImageConsumer)) {
        throw new IllegalStateException("View already has a tag " + tag + ". Cannot store consumer");
      }
      consumer = (ImageConsumer)tag;
    }
    populateImage(consumer, url);
  }

  /**
   * Cancel image loading for a view.
   * @param view view that hold an image
   */
  public void cancelImageLoading(final View view) {
    checkThread();
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
    checkThread();
    holder.cancelCurrentLoading();
  }

  /**
   * Create an image holder instance for the defined view.
   * @param view view instance
   * @return image holder instance
   */
  protected ImageConsumer createImageConsumer(final View view) {
    if (this.consumerFactory == null) {
      throw new IllegalStateException("Image consumers factory bean not found in container. Take a look at DefaultBeansManager.edit().images() method in assist package.");
    }
    return consumerFactory.createConsumer(view);
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
  public Bitmap getMemCached(final String url, final View view) {
    checkThread();
    Object tag = view.getTag();
    if (tag == null) {
      tag = createImageConsumer(view);
    }
    if (!(tag instanceof ImageConsumer)) { throw new IllegalStateException("View already has a tag"); }
    return getMemCached(url, (ImageConsumer) tag);
  }

  public Bitmap getMemCached(final String url, final ImageConsumer consumer) {
    ImageRequest request = createImageRequest(url, consumer);
    final ImageResult res = getFromMemCache(request, consumer);
    if (res == null) { return null; }
    decorateResult(consumer, res);
    return res.getBitmap();
  }

  private ImageRequest createImageRequest(String url, ImageConsumer consumer) {
    ImageRequest request = new ImageRequest(this, url, -1);
    consumer.prepareImageRequest(request);
    return request;
  }

  @SuppressWarnings("ConstantConditions")
  public void populateImage(final ImageConsumer consumer, final String url) {
    if (consumer.isMatchingParentButNotMeasured()) {

      if (consumer instanceof ViewImageConsumer) {
        final View view = ((ViewImageConsumer) consumer).getView();
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
          @SuppressWarnings("deprecation")
          @Override
          public void onGlobalLayout() {
            view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            populateImageNow(consumer, url);
          }
        });
        return;
      }

      consumer.post(new Runnable() {
        @Override
        public void run() {
          populateImageNow(consumer, url);
        }
      });
    } else {
      populateImageNow(consumer, url);
    }
  }

  private void checkThread() {
    if (Looper.myLooper() != Looper.getMainLooper()) {
      throw new IllegalStateException("populateImageNow must be called from the main thread");
    }
  }

  public void populateImageNow(final ImageConsumer consumer, final String url) {
    checkThread();
    if (debug) { Log.d(TAG, "Process url " + url); }
    if (TextUtils.isEmpty(url)) {
      setLoadingImage(consumer);
      return;
    }

    consumer.cancelCurrentLoading();

    ImageRequest request = createImageRequest(url, consumer);

    final ImageResult result = getFromMemCache(request, consumer);
    if (result != null) {
      consumer.onStart(null, url);
      setResultImage(consumer, result, false);
      consumer.onFinish(url, result);
      return;
    }

    if (debug) { Log.d(TAG, "Set loading for " + request.getKey()); }
    setLoadingImage(consumer);
    startImageLoaderTask(consumer, request);
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
   * @param consumer image consumer
   * @param result result instance
   */
  protected void decorateResult(final ImageConsumer consumer, final ImageResult result) {
    // nothing
  }

  private BitmapDrawable createDrawable(final Bitmap bitmap) {
    return new BitmapDrawable(getResources(), bitmap);
  }

  /**
   * It must be executed in the main thread.
   * @param consumer image consumer
   * @param result result instance
   * @param animate whether to animate image change
   */
  protected final void setResultImage(final ImageConsumer consumer, final ImageResult result, final boolean animate) {
    decorateResult(consumer, result);
    consumer.setImage(createDrawable(result.getBitmap()), animate);
    consumer.reset();
  }

  /**
   * Set preloader.
   * @param consumer image consumer instance
   */
  private void setLoadingImage(final ImageConsumer consumer) {
    if (!consumer.skipLoadingImage()) {
      Drawable d = getLoadingDrawable(consumer);
      if (!consumer.hasUndefinedSize() || (d.getIntrinsicWidth() != 0 && d.getIntrinsicHeight() != 0)) {
        consumer.setLoadingImage(d);
      }
    }
  }

  /**
   * @param request image request
   * @param consumer image consumer (to get target size)
   * @return cached drawable
   */
  protected ImageResult getFromMemCache(final ImageRequest request, final ImageConsumer consumer) {
    ImageLoader loader = consumer.currentLoader;
    String cacheKey = request.getCacheKey();
    if (loader != null && !loader.request.getCacheKey().equals(cacheKey)) {
      return null;
    }

    final Bitmap map = memCache.getElement(cacheKey);
    if (map == null) {
      if (debug) { Log.v(TAG, "Not in mem " + cacheKey); }
      return null;
    }

    // check bitmap size
    final boolean suits = consumer.allowSmallImagesFromCache() || consumer.checkBitmapSize(map);
    if (debug) { Log.v(TAG, "Use mem cache " + suits + " for " + cacheKey); }
    return suits ? new ImageResult(map, ImageSourceType.MEMORY) : null;
  }

  /**
   * Executed in the main thread.
   * @param request image request
   * @param consumer image holder to process
   */
  private void startImageLoaderTask(final ImageConsumer consumer, final ImageRequest request) {
    final String key = request.getKey();
    if (debug) { Log.d(TAG, "Key " + key); }

    ImageLoader loader = currentLoads.get(key);
    if (loader != null) {
      final boolean added = loader.addTarget(consumer);
      if (!added) { loader = null; }
    }

    if (loader == null) {

      if (DEBUG) { Log.d(TAG, "Start a new task"); }
      loader = new ImageLoader(request, this);
      if (!loader.addTarget(consumer)) {
        throw new IllegalStateException("Cannot add target to the new loader");
      }

      currentLoads.put(key, loader);
      if (debug) { Log.d(TAG, "Current loaders count: " + currentLoads.size()); }
      final Executor executor = getImageTaskExecutor();
      executor.execute(loader.future);

    } else if (debug) {
      Log.d(TAG, "Joined to the existing task " + key);
    }
  }

  /**
   * Add image to memory cache.
   * @param url image URL
   * @param bitmap bitmap
   */
  protected void memCacheImage(final String url, final Bitmap bitmap) {
    if (DEBUG) { Log.d(TAG, "Memcache for " + url); }
    memCache.putElement(url, bitmap);
  }

  static int calculateSampleFactor(final int inW, final int inH, final int width, final int height) {
    if (inW <= width && inH <= height) {
      return 1;
    }
    if (width == 0 && height == 0) {
      return 1;
    }

    final int factor;
    if (width == 0) {
      factor = inH / height;
    } else if (height == 0) {
      factor = inW / width;
    } else {
      factor = inW > inH ? inW / width : inH / height;
    }

    return factor;
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

}

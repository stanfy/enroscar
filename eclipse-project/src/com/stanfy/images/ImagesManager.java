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

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.stanfy.DebugFlags;
import com.stanfy.images.ImageMemoryCache.CacheRecord;
import com.stanfy.images.model.CachedImage;
import com.stanfy.views.ImagesLoadListenerProvider;
import com.stanfy.views.LoadableImageView;
import com.stanfy.views.utils.AppUtils;
import com.stanfy.views.utils.Task;
import com.stanfy.views.utils.ThreadUtils;

/**
 * A manager that encapsulates the images downloading and caching logic.
 * @param <T> cached image type
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public class ImagesManager<T extends CachedImage> {

  /** Logging tag. */
  private static final String TAG = "ImagesManager";

  /** Pattern to cut the images sources from HTML. */
  protected static final Pattern IMG_URL_PATTERN = Pattern.compile("<img.*?src=\"(.*?)\".*?>");

  /** Debug flag. */
  private static final boolean DEBUG_IO = DebugFlags.DEBUG_IO;

  /** Debug flag. */
  private static final boolean DEBUG = DebugFlags.DEBUG_IMAGES;

  /** Empty drawable. */
  protected static final ColorDrawable EMPTY_DRAWABLE = new ColorDrawable(0xeeeeee);

  /** Memory cache. */
  private final ImageMemoryCache memCache = new ImageMemoryCache();

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

  /** Hidden constructor. */
  protected ImagesManager(final Resources resources) {
    this.resources = resources;
  }

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
   */
  public void clearCache(final Context context, final String path, final String url) {
    memCache.remove(url);
    final File f = new File(getImageDir(context), path);
    delete(f);
  }

  /**
   * Flush resources.
   */
  public void flush() {
    memCache.clear(false);
    buffersPool.flush();
  }

  /**
   * Populate the requested image to the specified view.
   * @param view view instance
   * @param url image URL
   * @param imagesDAO images DAO
   * @param downloader downloader instance
   */
  public <VT extends View> void populateImage(final VT view, final String url, final ImagesDAO<T> imagesDAO, final Downloader downloader) {
    final Object tag = view.getTag();
    ImageHolder imageHolder = null;
    if (tag == null) {
      imageHolder = createImageHolder(view);
      view.setTag(imageHolder);
    } else {
      if (!(tag instanceof ImageHolder)) { throw new IllegalStateException("View already has a tag " + tag); }
      imageHolder = (ImageHolder)tag;
    }
    populateImage(imageHolder, url, imagesDAO, downloader);
  }

  protected ImageHolder createImageHolder(final View view) {
    if (view instanceof LoadableImageView) { return new LoadableImageViewHolder((LoadableImageView)view); }
    if (view instanceof ImageView) { return new ImageViewHolder((ImageView)view); }
    if (view instanceof CompoundButton) { return new CompoundButtonHolder((CompoundButton)view); }
    if (view instanceof TextView) { return new TextViewHolder((TextView)view); }
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
    if (tag == null || !(tag instanceof ImageHolder)) { return null; }
    final Drawable res = getFromMemCache(url, (ImageHolder)tag);
    if (res == null) { return null; }
    return decorateDrawable(view.getContext(), res);
  }

  public void populateImage(final ImageHolder imageHolder, final String url, final ImagesDAO<T> imagesDAO, final Downloader downloader) {
    if (DEBUG) { Log.d(TAG, "Process url" + url); }
    if (TextUtils.isEmpty(url)) {
      setLoadingImage(imageHolder);
      return;
    }

    final Drawable memCached = getFromMemCache(url, imageHolder);
    if (memCached != null) {
      imageHolder.start(url);
      setImage(imageHolder, memCached);
      imageHolder.finish(url, memCached);
      return;
    }

    if (DEBUG) { Log.d(TAG, "Set loading for " + url); }
    setLoadingImage(imageHolder);
    final ImageLoader<T> loader = createImageLoaderTask(imageHolder, url, imagesDAO, downloader);
    cancelTasks(loader);
    getImageTaskExecutor().execute(loader);
  }

  private void setLoadingImage(final ImageHolder holder) {
    if (!holder.skipLoadingImage()) {
      final Drawable d = !holder.isDynamicSize() ? getLoadingDrawable(holder) : null;
      setImage(holder, d);
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
  protected Executor getImageTaskExecutor() { return ThreadUtils.getImageTasksExecutor(); }
  /**
   * @return an executor for main tasks
   */
  protected Executor getMainTaskExecutor() { return ThreadUtils.getMainTasksExecutor(); }

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
  protected Drawable decorateDrawable(@SuppressWarnings("unused") final Context context, final Drawable drawable) { return drawable; }

  /**
   * @param loader load instance
   */
  protected void cancelTasks(final ImageLoader<T> loader) {
    ThreadUtils.cancelImageTask(loader.getName());
  }

  /**
   * @param imageView image view instance
   * @param drawable incoming drawable
   */
  protected final void setImage(final ImageHolder imageHolder, final Drawable drawable) {
    final Drawable d = decorateDrawable(imageHolder.context, drawable);
    imageHolder.setImage(d);
    imageHolder.reset();
  }

  /**
   * @param url image URL
   * @return cached drawable
   */
  protected Drawable getFromMemCache(final String url, final ImageHolder holder) {
    final CacheRecord record = memCache.getElement(url);
    synchronized (holder) {
      if (record == null || (holder.cachedImageId > 0 && record.getImageId() != holder.cachedImageId)) { return null; }
    }
    final Bitmap map = record.getBitmap();
    final int gap = 5;
    return map != null && (
        holder.isDynamicSize()
        || Math.abs(holder.getRequiredWidth() - map.getWidth()) < gap
        || Math.abs(holder.getRequiredHeight() - map.getHeight()) < gap
    ) ? new BitmapDrawable(holder.context.getResources(), map) : null;
  }

  /**
   * @param imageHolder image holder to process
   * @param url image URL
   * @param imagesDAO images DAO
   * @param downloader downloader instance
   * @return loader task instance
   */
  protected ImageLoader<T> createImageLoaderTask(final ImageHolder imageHolder, final String url, final ImagesDAO<T> imagesDAO, final Downloader downloader) {
    return new ImageLoader<T>(imageHolder, url, this, imagesDAO, downloader);
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

    image.setLoaded(true);
    image.setTimestamp(System.currentTimeMillis());
    imagesDao.updateImage(image);
  }

  protected void memCacheImage(final String url, final Drawable d, final long id) {
    if (d instanceof BitmapDrawable) {
      if (DEBUG) { Log.d(TAG, "Memcache for " + url); }
      memCache.putElement(url, ((BitmapDrawable)d).getBitmap(), id);
    }
  }

  protected Drawable readLocal(final T cachedImage, final Context context, final ImageHolder holder) throws IOException {
    final File file = new File(getImageDir(context), cachedImage.getPath());
    if (!file.exists()) {
      if (DEBUG_IO) { Log.w(TAG, "Local file " + file.getAbsolutePath() + "does not exist."); }
      return null;
    }
    final BitmapFactory.Options options = new BitmapFactory.Options();
    if (holder.useSampling() && !holder.isDynamicSize()) {
      options.inSampleSize = resolveSampleFactor(new FileInputStream(file), holder.getRequiredWidth(), holder.getRequiredHeight());
    }
    final Drawable d = decodeStream(new FileInputStream(file), options);
    return d;
  }

  private InputStream prepareImageOptionsAndInput(final InputStream is, final BitmapFactory.Options options) {
    final BuffersPool bp = buffersPool;
    final int bCapacity = BuffersPool.DEFAULT_SIZE_FOR_IMAGES;
    InputStream src = is;
    if (!src.markSupported()) { src = new PoolableBufferedInputStream(src, bCapacity, bp); }
    final BitmapFactory.Options opts = options != null ? options : new BitmapFactory.Options();
    opts.inTempStorage = bp.get(bCapacity);
    opts.inPreferredConfig = imagesFormat;
    opts.inDensity = sourceDensity;
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

  protected int resolveSampleFactor(final InputStream is, final int width, final int height) throws IOException {
    final BitmapFactory.Options opts = new BitmapFactory.Options();
    opts.inJustDecodeBounds = true;
    final InputStream src = prepareImageOptionsAndInput(is, opts);
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

  protected Drawable decodeStream(final InputStream is, final BitmapFactory.Options options) throws IOException {
    final BitmapFactory.Options opts = options != null ? options : new BitmapFactory.Options();
    final InputStream src = prepareImageOptionsAndInput(is, opts);
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
   * Image loader task.
   * @param <T> image type
   * @author Roman Mazur - Stanfy (http://www.stanfy.com)
   */
  protected static class ImageLoader<T extends CachedImage> extends Task {

    /** GUI view. */
    private final ImageHolder imageHolder;

    /** Image URL. */
    private final String url;

    /** Images manager. */
    private final ImagesManager<T> imagesManager;

    /** Images DAO. */
    private final ImagesDAO<T> imagesDAO;

    /** Downloader. */
    private final Downloader downloader;

    public ImageLoader(final ImageHolder imageHolder, final String url, final ImagesManager<T> imagesManager,
        final ImagesDAO<T> imagesDAO, final Downloader downloader) {
      super("image-" + imageHolder.hashCode());
      this.imagesManager = imagesManager;
      this.url = url;
      this.imageHolder = imageHolder;
      this.downloader = downloader;
      this.imagesDAO = imagesDAO;
    }

    protected void safeImageSet(final T cachedImage, final Drawable source) {
      if (source == null) { return; }
      final long id = cachedImage.getId();
      final Drawable d = memCacheImage(source, id);
      final ImageHolder imageHolder = this.imageHolder;
      imageHolder.post(new Runnable() {
        @Override
        public void run() {
          if (DEBUG) { Log.d(TAG, "Try to set " + imageHolder + " - " + url); }
          synchronized (imageHolder) {
            final long savedId = imageHolder.cachedImageId;
            if (id == savedId) {
              imagesManager.setImage(imageHolder, d);
            } else {
              if (DEBUG) { Log.d(TAG, "Skip set for " + imageHolder); }
            }
          }
        }
      });
    }

    protected Drawable setLocalImage(final T cachedImage) throws IOException {
      final Context x = imageHolder.context;
      if (x == null) { throw new IOException("Context is null"); }
      final Drawable d = imagesManager.readLocal(cachedImage, x, imageHolder);
      safeImageSet(cachedImage, d);
      return d;
    }

    protected Drawable setRemoteImage(final T cachedImage) throws IOException {
      if (!url.startsWith("http")) { return null; }
      final Context x = imageHolder.context;
      if (x == null) { throw new IOException("Context is null"); }
      imagesManager.makeImageLocal(imagesDAO, x, cachedImage, downloader);
      return setLocalImage(cachedImage);
    }

    private BitmapDrawable prepare(final BitmapDrawable bd) {
      int dstW = imageHolder.getRequiredWidth(), dstH = imageHolder.getRequiredHeight();
      if (dstW <= 0 || dstH <= 0 || imageHolder.skipScaleBeforeCache()) {
        if (DEBUG) { Log.d(TAG, "Skip scaling for " + imageHolder); }
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
      return new BitmapDrawable(scaled);
    }

    private Drawable memCacheImage(final Drawable d, final long id) {
      Drawable result = d;
      if (d instanceof BitmapDrawable) {
        final BitmapDrawable bmd = (BitmapDrawable)d;
        result = prepare(bmd);
        if (result != bmd) { bmd.getBitmap().recycle(); }
      }
      imagesManager.memCacheImage(url, result, id);
      return result;
    }

    @Override
    protected void safeSQLRun() {
      if (DEBUG) { Log.d(TAG, "Start image task"); }
      try {
        imageHolder.start(url);
        T cachedImage = imagesDAO.getCachedImage(url);
        if (cachedImage == null) {
          cachedImage = imagesDAO.createCachedImage(url);
          if (cachedImage == null) {
            Log.w(TAG, "Cached image info was not created for " + url);
            return;
          }
        }

        synchronized (imageHolder) {
          imageHolder.cachedImageId = cachedImage.getId();
        }

        Drawable d = null;

        if (cachedImage.isLoaded()) {
          d = setLocalImage(cachedImage);
          if (DEBUG) { Log.d(TAG, "Image " + cachedImage.getId() + "-local"); }
        }

        if (d == null) {
          d = setRemoteImage(cachedImage);
          if (DEBUG) { Log.d(TAG, "Image " + cachedImage.getId() + "-remote"); }
        }

        if (d == null) {
          Log.w(TAG, "Image " + cachedImage.getUrl() + " is not resolved");
        }
        imageHolder.finish(url, d);

      } catch (final MalformedURLException e) {
        Log.e(TAG, "Bad URL: " + url + ". Loading canceled.", e);
        imageHolder.error(url, e);
      } catch (final IOException e) {
        if (DEBUG_IO) { Log.e(TAG, "IO error for " + url + ": " + e.getMessage()); }
        imageHolder.error(url, e);
      }
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
    /** Tag. */
    long cachedImageId;
    /** Listener. */
    ImagesLoadListener listener;
    /** @param context context instance */
    public ImageHolder(final Context context) { this.context = context; }

    /** @param listener the listener to set */
    public final void setListener(final ImagesLoadListener listener) { this.listener = listener; }

    /* actions */
    public void touch() { }
    public abstract void setImage(final Drawable d);
    public abstract void post(final Runnable r);
    public void reset() { cachedImageId = -1; }
    public void destroy() {
      context = null;
    }
    final void start(final String url) {
      if (listener != null) { listener.onLoadStart(this, url); }
    }
    final void finish(final String url, final Drawable drawable) {
      if (listener != null) { listener.onLoadFinished(this, url, drawable); }
    }
    final void error(final String url, final Throwable exception) {
      if (listener != null) { listener.onLoadError(this, url, exception); }
    }

    /* parameters */
    public abstract int getRequiredWidth();
    public abstract int getRequiredHeight();
    public boolean isDynamicSize() { return getRequiredWidth() <= 0 || getRequiredHeight() <= 0; }
    public Drawable getLoadingImage() { return null; }

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
    public ViewImageHolder(final T view) {
      super(view.getContext());
      this.view = view;
      touch();
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
        view.post(r);
      }
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
    public void destroy() {
      super.destroy();
      view = null;
    }
  }

  /**
   * Image holder for {@link ImageView}.
   * @author Roman Mazur - Stanfy (http://www.stanfy.com)
   */
  static class ImageViewHolder extends ViewImageHolder<ImageView> {
    public ImageViewHolder(final ImageView view) { super(view); }
    @Override
    public void setImage(final Drawable d) { view.setImageDrawable(d); }
  }

  /**
   * Image holder for {@link ImageView}.
   * @author Roman Mazur - Stanfy (http://www.stanfy.com)
   */
  static class LoadableImageViewHolder extends ImageViewHolder {
    public LoadableImageViewHolder(final LoadableImageView view) { super(view); }
    @Override
    public boolean skipScaleBeforeCache() { return ((LoadableImageView)view).isSkipScaleBeforeCache(); }
    @Override
    public boolean skipLoadingImage() { return ((LoadableImageView)view).isSkipLoadingImage(); }
    @Override
    public boolean useSampling() { return ((LoadableImageView)view).isUseSampling(); }
    @Override
    public Drawable getLoadingImage() { return ((LoadableImageView)view).getLoadingImage(); }
  }

  /**
   * Image holder for {@link CompoundButton}.
   * @author Roman Mazur - Stanfy (http://www.stanfy.com)
   */
  static class CompoundButtonHolder extends ViewImageHolder<CompoundButton> {
    public CompoundButtonHolder(final CompoundButton view) { super(view); }
    @Override
    public void setImage(final Drawable d) { view.setButtonDrawable(d); }
  }

  /**
   * Image holder for {@link TextView}.
   * @author Olexandr Tereshchuk - Stanfy (http://www.stanfy.com)
   */
  static class TextViewHolder extends ViewImageHolder<TextView> {
    public TextViewHolder(final TextView view) { super(view); }
    @Override
    public void setImage(final Drawable d) { view.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null); }
    @Override
    public int getRequiredHeight() { return -1; }
    @Override
    public int getRequiredWidth() { return -1; }
  }


}

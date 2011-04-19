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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.stanfy.DebugFlags;
import com.stanfy.images.model.CachedImage;
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

  /** Images format. */
  private Bitmap.Config imagesFormat = Bitmap.Config.RGB_565;

  /** Hidden constructor. */
  protected ImagesManager() { /* nothing to do */ }

  /** @param imagesFormat the imagesFormat to set */
  public void setImagesFormat(final Bitmap.Config imagesFormat) { this.imagesFormat = imagesFormat; }

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
   * Destroy the context.
   */
  public void destroy() {
    memCache.clear();
    buffersPool.destroy();
    EMPTY_DRAWABLE.setCallback(null);
    System.gc();
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
    if (view instanceof ImageView) { return new ImageViewHolder((ImageView)view); }
    if (view instanceof CompoundButton) { return new CompoundButtonHolder((CompoundButton)view); }
    if (view instanceof TextView) { return new TextViewHolder((TextView)view); }
    return null;
  }

  public void populateImage(final ImageHolder imageHolder, final String url, final ImagesDAO<T> imagesDAO, final Downloader downloader) {
    if (DEBUG) { Log.d(TAG, "Process url" + url); }
    if (TextUtils.isEmpty(url)) {
      setLoadingImage(imageHolder);
      return;
    }
    final Drawable memCached = getFromMemCache(url, imageHolder);
    if (memCached != null) {
      setImage(imageHolder, memCached);
      return;
    }
    if (DEBUG) { Log.d(TAG, "Set loading for " + url); }
    setLoadingImage(imageHolder);
    final ImageLoader<T> loader = createImageLoaderTask(imageHolder, url, imagesDAO, downloader);
    cancelTasks(loader);
    getImageTaskExecutor().execute(loader);
  }

  private void setLoadingImage(final ImageHolder holder) {
    if (holder.getRequiredWidth() > 0 && holder.getRequiredHeight() > 0) {
      setImage(holder, getLoadingDrawable(holder.context));
    }
  }

  /**
   * @param image image to process
   * @return local file system path to that image
   */
  protected String setCachedImagePath(final T image) {
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
  protected Drawable getLoadingDrawable(@SuppressWarnings("unused") final Context context) { return EMPTY_DRAWABLE; }

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
    imageHolder.cachedImageId = -1;
  }

  /**
   * @param url image URL
   * @return cached drawable
   */
  protected Drawable getFromMemCache(final String url, final ImageHolder holder) {
    final Bitmap map = memCache.getElement(url);
    final int gap = 5;
    return map != null && Math.abs(holder.getRequiredWidth() - map.getWidth()) < gap && Math.abs(holder.getRequiredHeight() - map.getHeight()) < gap
        ? new BitmapDrawable(holder.context.getResources(), map) : null;
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
  protected File getImageDir(final Context context) {
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

  protected void memCacheImage(final String url, final Drawable d) {
    if (d instanceof BitmapDrawable) {
      if (DEBUG) { Log.d(TAG, "Memcache for " + url); }
      memCache.putElement(url, ((BitmapDrawable)d).getBitmap());
    }
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
    opts.inPreferredConfig = imagesFormat;
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
      final Drawable d = memCacheImage(source);
      final ImageHolder imageHolder = this.imageHolder;
      final long id = cachedImage.getId();
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
      final Drawable d = imagesManager.readLocal(cachedImage, x);
      safeImageSet(cachedImage, d);
      return d;
    }

    protected Drawable setRemoteImage(final T cachedImage) throws IOException {
      final Context x = imageHolder.context;
      if (x == null) { throw new IOException("Context is null"); }
      imagesManager.makeImageLocal(imagesDAO, x, cachedImage, downloader);
      return setLocalImage(cachedImage);
    }

    private BitmapDrawable prepare(final BitmapDrawable bd) {
      int dstW = imageHolder.getRequiredWidth(), dstH = imageHolder.getRequiredHeight();
      if (dstW <= 0 || dstH <= 0) {
        if (DEBUG) { Log.d(TAG, "Skip scaling for " + imageHolder); }
        return bd;
      }

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

    @Override
    protected void safeSQLRun() {
      if (DEBUG) { Log.d(TAG, "Start image task"); }
      try {
        T cachedImage = imagesDAO.getCachedImage(url);
        if (cachedImage == null) {
          cachedImage = imagesDAO.createCachedImage(url);
          if (cachedImage == null) {
            Log.w(TAG, "Cached image info was not created for " + url);
            return;
          }
        }

        imageHolder.cachedImageId = cachedImage.getId();

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

      } catch (final MalformedURLException e) {
        Log.e(TAG, "Bad URL: " + url + ". Loading canceled.", e);
      } catch (final IOException e) {
        if (DEBUG_IO) { Log.e(TAG, "IO error for " + url + ": " + e.getMessage()); }
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
    /** @param context context instance */
    public ImageHolder(final Context context) { this.context = context; }
    public abstract void setImage(final Drawable d);
    public abstract void post(final Runnable r);
    public abstract int getRequiredWidth();
    public abstract int getRequiredHeight();
    public void destroy() {
      context = null;
    }
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
      final int h = view.getLayoutParams().height;
      if (h > 0) { return h; }
      return -1;
    }
    @Override
    public int getRequiredWidth() {
      final int w = view.getLayoutParams().width;
      if (w > 0) { return w; }
      return -1;
    }
    @Override
    public void destroy() {
      super.destroy();
      view = null;
    }
  }

  /**
   * @author Roman Mazur - Stanfy (http://www.stanfy.com)
   */
  static class ImageViewHolder extends ViewImageHolder<ImageView> {
    public ImageViewHolder(final ImageView view) { super(view); }
    @Override
    public void setImage(final Drawable d) { view.setImageDrawable(d); }
  }

  /**
   * @author Roman Mazur - Stanfy (http://www.stanfy.com)
   */
  static class CompoundButtonHolder extends ViewImageHolder<CompoundButton> {
    public CompoundButtonHolder(final CompoundButton view) { super(view); }
    @Override
    public void setImage(final Drawable d) { view.setButtonDrawable(d); }
  }

  /**
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

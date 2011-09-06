package com.stanfy.views;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.TreeMap;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;

import com.stanfy.images.decorator.ImageDecorator;
import com.stanfy.images.decorator.MaskImageDecorator;

/**
 * Image view that slightly extends possibilities of a standard image view.
 * <p>
 *   Features:
 *   <ul>
 *     <li><b>rounded corners</b> - set cornersRadius > 0 (attribute {@code android:radius})</li>
 *   </ul>
 * </p>
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public class ImageView extends android.widget.ImageView {

  /** Corners decorators store. */
  private static final RadiusDecoratorsCache CORNERS_DOCORATORS = new RadiusDecoratorsCache();

  /** Image decorator. */
  private ImageDecorator imageDecorator;

  /** Draw matrix. */
  private Matrix drawMatrix = null;

  /** @see android.widget.ImageView#mHaveFrame */
  private boolean haveFrame = false;

  /** Flag to block layout requests. */
  private boolean blockLayoutRequests = false;

  /** Stored scale type. */
  private ScaleType storedScaleType = null;

  /** Flag to minimize count of layout requests. */
  private boolean minimizeLayoutRequests = false;

  /** Decorated cache bitmap. */
  private SoftReference<Bitmap> decoratedCache;
  /** Flag that decorated cache bitmap is actual. */
  private boolean decoratedCacheActual;

  /** Flag to respect {@link Drawable#getIntrinsicWidth()} and {@link Drawable#getIntrinsicHeight()} values. */
  private boolean respectIntrinsicDrawableSize = false;

  public ImageView(final Context context) {
    super(context);
    init(context, null);
  }

  public ImageView(final Context context, final AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public ImageView(final Context context, final AttributeSet attrs, final int defStyle) {
    super(context, attrs, defStyle);
    init(context, attrs);
  }

  private void init(final Context context, final AttributeSet attrs) {
    final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ImageView);
    final int cornersRadius = a.getDimensionPixelSize(R.styleable.ImageView_android_radius, 0);
    a.recycle();

    setCornersRadius(cornersRadius);
  }

  /** @param blockLayoutRequests the blockLayoutRequests to set */
  protected void setBlockLayoutRequests(final boolean blockLayoutRequests) { this.blockLayoutRequests = blockLayoutRequests; }

  /** @param minimizeLayoutRequests the minimizeLayoutRequests to set */
  public void setMinimizeLayoutRequests(final boolean minimizeLayoutRequests) { this.minimizeLayoutRequests = minimizeLayoutRequests; }

  @Override
  public void requestLayout() {
    if (!blockLayoutRequests) { super.requestLayout(); }
  }

  /** @param respectIntrinsicDrawableSize the respectIntrinsicDrawableSize to set */
  public void setRespectIntrinsicDrawableSize(final boolean respectIntrinsicDrawableSize) {
    this.respectIntrinsicDrawableSize = respectIntrinsicDrawableSize;
  }

  /**
   * This method replaces the scale type of an image view without call to layout requests.
   * @param type new scale type
   * @return previous scale type
   */
  public ScaleType replaceScaleType(final ScaleType type) {
    blockLayoutRequests = true;
    final ScaleType result = getScaleType();
    setScaleType(type);
    blockLayoutRequests = false;
    return result;
  }

  /**
   * Set a scale type and store a previous one that will be restores after the next call to {@link #setImageDrawable(Drawable)}.
   * @param scaleType
   */
  public void setTemporaryScaleType(final ScaleType scaleType) {
    final ScaleType old = replaceScaleType(scaleType);
    if (storedScaleType == null) { storedScaleType = old; }
  }

  /** @param imageDecorator the imageDecorator to set */
  public void setImageDecorator(final ImageDecorator imageDecorator) {
    this.imageDecorator = imageDecorator;
    setDrawingCacheEnabled(true);
  }

  /**
   * Set a radius of corners when drawing a bitmap image.
   * @param r radius to set
   */
  public void setCornersRadius(final int r) {
    imageDecorator = r > 0 ? CORNERS_DOCORATORS.getDecorator(r) : null;
  }

  /** @param drawMatrix the drawMatrix to set */
  void setDrawMatrix(final Matrix drawMatrix) {
    this.drawMatrix = drawMatrix;
    clearDecorateCache();
  }
  private void clearDecorateCache() {
    final Bitmap cache = decoratedCache != null ? decoratedCache.get() : null;
    if (cache != null) { cache.eraseColor(0); }
    decoratedCacheActual = false;
  }
  /** @return the haveFrame */
  boolean isHaveFrame() { return haveFrame; }

  @Override
  protected void drawableStateChanged() {
    super.drawableStateChanged();
    final ImageDecorator d = imageDecorator;
    if (d != null && d.dependsOnDrawableState()) {
      clearDecorateCache();
      invalidate();
    }
  }

  @Override
  protected void onDraw(final Canvas canvas) {
    final ImageDecorator imageDecorator = this.imageDecorator;
    if (imageDecorator == null) {
      super.onDraw(canvas);
      return;
    }

    final Drawable d = getDrawable();
    if (d == null) { return; }

    final int pl = getPaddingLeft(), pt = getPaddingTop();
    final int resultW = getMeasuredWidth() - pl - getPaddingRight(), resultH = getMeasuredHeight() - pt - getPaddingBottom();
    if (resultW <= 0 || resultH <= 0) {
      super.onDraw(canvas);
      return;
    }

    int realW = resultW, realH = resultH;
    if (respectIntrinsicDrawableSize || d instanceof BitmapDrawable) {
      realW = d.getIntrinsicWidth();
      realH = d.getIntrinsicHeight();
      if (realW == 0 || realH == 0) { return; } // nothing to draw
    }

    final Bitmap decorated;

    Bitmap bitmap = decoratedCache != null ? decoratedCache.get() : null;
    if (bitmap == null || bitmap.getWidth() != resultW || bitmap.getHeight() != resultH) {
      final Bitmap old = bitmap;
      bitmap = Bitmap.createBitmap(resultW, resultH, Bitmap.Config.ARGB_8888);
      if (old != null) { old.recycle(); }
      decoratedCache = new SoftReference<Bitmap>(bitmap);
      decoratedCacheActual = false;
    }

    if (!decoratedCacheActual) {
      imageDecorator.setup(resultW, resultH, getDrawableState(), d.getLevel(), realW, realH);

      final Canvas bitmapCanvas = new Canvas(bitmap);
      if (drawMatrix == null) {
        d.draw(bitmapCanvas);
      } else {
        final int saveCount = bitmapCanvas.save();
        bitmapCanvas.concat(drawMatrix);
        d.draw(bitmapCanvas);
        bitmapCanvas.restoreToCount(saveCount);
      }

      decorated = imageDecorator.decorateBitmap(bitmap, bitmapCanvas);
      if (decorated != bitmap) {
        bitmap.recycle();
        decoratedCache = new SoftReference<Bitmap>(decorated);
      }
      decoratedCacheActual = true;
    } else {
      decorated = bitmap;
    }

    if (pl == 0 && pt == 0) {
      canvas.drawBitmap(decorated, 0, 0, null);
    } else {
      final int saveCount = canvas.save();
      canvas.translate(pl, pt);
      canvas.drawBitmap(decorated, 0, 0, null);
      canvas.restoreToCount(saveCount);
    }
  }

  @Override
  public void setImageMatrix(final Matrix matrix) {
    if ((matrix == null && !getImageMatrix().isIdentity())
        || (matrix != null && !getImageMatrix().equals(matrix))) {
      ImageViewHiddenMethods.configureBounds(this);
    }
    super.setImageMatrix(matrix);
  }

  @Override
  public void setImageDrawable(final Drawable drawable) {
    if (storedScaleType != null) {
      replaceScaleType(storedScaleType);
      storedScaleType = null;
    }
    if (minimizeLayoutRequests) { blockLayoutRequests = true; }
    super.setImageDrawable(drawable);
    if (drawable != null) { ImageViewHiddenMethods.configureBounds(this); }
    blockLayoutRequests = false;
  }

  @Override
  public void setImageURI(final Uri uri) {
    super.setImageURI(uri);
    if (getDrawable() != null) { ImageViewHiddenMethods.configureBounds(this); }
  }

  @Override
  protected boolean setFrame(final int l, final int t, final int r, final int b) {
    final boolean changed = super.setFrame(l, t, r, b);
    haveFrame = true;
    ImageViewHiddenMethods.configureBounds(this);
    return changed;
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    // destroy cache
    final Bitmap cache = decoratedCache != null ? decoratedCache.get() : null;
    if (cache != null) { cache.recycle(); }
    decoratedCache = null;
    decoratedCacheActual = false;
  }

  /**
   * @author Roman Mazur (Stanfy - http://www.stanfy.com)
   */
  private static class RadiusDecoratorsCache {
    /** Cache of decorators. */
    private final TreeMap<Integer, WeakReference<MaskImageDecorator>> decoratorsCache = new TreeMap<Integer, WeakReference<MaskImageDecorator>>();

    public MaskImageDecorator getDecorator(final int radius) {
      final WeakReference<MaskImageDecorator> ref = decoratorsCache.get(radius);
      MaskImageDecorator d = ref == null ? null : ref.get();
      if (d == null) {
        d = new MaskImageDecorator(radius);
        decoratorsCache.put(radius, new WeakReference<MaskImageDecorator>(d));
      }
      return d;
    }
  }

}

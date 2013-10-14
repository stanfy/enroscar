package com.stanfy.enroscar.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.SystemClock;
import android.util.AttributeSet;

import com.stanfy.enroscar.images.decorator.ImageDecorator;
import com.stanfy.enroscar.images.decorator.MaskImageDecorator;
import com.stanfy.enroscar.ui.R;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.TreeMap;

/**
 * Image view that slightly extends possibilities of a standard image view.
 * <p>
 *   Features:
 *   <ul>
 *     <li><b>rounded corners</b> - set cornersRadius > 0 (attribute {@code android:radius})</li>
 *     <li><b>other image decorators</b> - see {@link #setImageDecorator(ImageDecorator)} and {@link ImageDecorator}</li>
 *     <li><b>image transitions</b> - see {@link #setImageDrawableWithTransition(Drawable, int, boolean)}</li>
 *   </ul>
 * </p>
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public class ImageView extends android.widget.ImageView {

  /** Default transition duration (in millis). */
  private static final int TRANSITION_DURATION_DEFAULT = 300;

  /** Corners decorators store. */
  private static final RadiusDecoratorsCache CORNERS_DOCORATORS = new RadiusDecoratorsCache();

  /** Very small bitmap to be set to decoration canvas ({@link #decorationCanvas}). */
  private static final Bitmap NULL_BITMAP = Bitmap.createBitmap(1, 1, Config.ALPHA_8);

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

  /** Decoration canvas. */
  private final Canvas decorationCanvas = new Canvas();
  /** Decorated cache bitmap. */
  private SoftReference<Bitmap> decoratedCache;
  /** Flag that decorated cache bitmap is actual. */
  private boolean decoratedCacheActual;

  /** Flag to respect {@link Drawable#getIntrinsicWidth()} and {@link Drawable#getIntrinsicHeight()} values. */
  private Boolean respectIntrinsicDrawableSize = null;

  /** Vectors. */
  private float[] bufferSrcVector = new float[2], bufferDstVector = new float[2];

  /** Transition array. */
  private final Drawable[] transitionArray = new Drawable[2];

  /** Last transition. */
  private TransitionDrawable lastTransition;

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

  /** @return image decorator */
  public ImageDecorator getImageDecorator() { return imageDecorator; }

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

  // XXX this is a separate method to avoid lint warnings only
  // we do make an allocation here but expect it ot be a very rare event
  private void updateDecorationCacheReference(final Bitmap decorated) {
    decoratedCache = new SoftReference<Bitmap>(decorated);
  }

  @Override
  protected void onDraw(final Canvas canvas) {
    final Drawable d = getDrawable();
    if (d == null) { return; }

    final ImageDecorator imageDecorator = this.imageDecorator;
    if (imageDecorator == null) {
      super.onDraw(canvas);
      return;
    }

    final int pl = getPaddingLeft(), pt = getPaddingTop();
    final int resultW = getMeasuredWidth() - pl - getPaddingRight(), resultH = getMeasuredHeight() - pt - getPaddingBottom();
    if (resultW <= 0 || resultH <= 0) {
      super.onDraw(canvas);
      return;
    }

    final Matrix drawMatrix = this.drawMatrix;
    int realW = resultW, realH = resultH;
    final Boolean respectIntrinsicDrawableSize = this.respectIntrinsicDrawableSize;
    final boolean useIntrinsic = respectIntrinsicDrawableSize == null ? d instanceof BitmapDrawable : respectIntrinsicDrawableSize;
    if (useIntrinsic) {
      realW = d.getIntrinsicWidth();
      realH = d.getIntrinsicHeight();
      if (realW == 0 || realH == 0) { return; } // nothing to draw
      if (drawMatrix != null) {
        final float[] src = bufferSrcVector, dst = bufferDstVector;
        src[0] = realW;
        src[1] = 0;
        drawMatrix.mapVectors(dst, src);
        realW = (int)dst[0];
        src[0] = 0;
        src[1] = realH;
        drawMatrix.mapVectors(dst, src);
        realH = (int)dst[1];
      }
    }

    final Bitmap decorated;

    Bitmap bitmap = decoratedCache != null ? decoratedCache.get() : null;
    if (bitmap == null || bitmap.getWidth() != resultW || bitmap.getHeight() != resultH) {
      final Bitmap old = bitmap;
      bitmap = Bitmap.createBitmap(resultW, resultH, Bitmap.Config.ARGB_8888);
      bitmap.setDensity(getResources().getDisplayMetrics().densityDpi);
      if (old != null) { old.recycle(); }
      updateDecorationCacheReference(bitmap);
      decoratedCacheActual = false;
    }

    if (!decoratedCacheActual) {
      /*
       * it's important to set it prior to drawable drawing since
       * it can be reset by invalidateSelf call (see TransitionDrawable)
       */
      decoratedCacheActual = true;

      imageDecorator.setup(resultW, resultH, getDrawableState(), d.getLevel(), realW, realH);

      final Canvas bitmapCanvas = decorationCanvas;
      bitmapCanvas.setBitmap(bitmap);
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
        updateDecorationCacheReference(decorated);
      }

      // we used null here but some devices fail with NP
      bitmapCanvas.setBitmap(NULL_BITMAP);
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
  public void invalidateDrawable(final Drawable dr) {
    if (dr == getDrawable()) { clearDecorateCache(); }
    super.invalidateDrawable(dr);
  }
  @Override
  public void scheduleDrawable(final Drawable who, final Runnable what, final long when) {
    if (who == getDrawable()) { clearDecorateCache(); }
    super.scheduleDrawable(who, what, when);
  }

  @Override
  public void setImageMatrix(final Matrix matrix) {
    if ((matrix == null && !getImageMatrix().isIdentity())
        || (matrix != null && !getImageMatrix().equals(matrix))) {
      ImageViewHiddenMethods.configureBounds(this);
    }
    super.setImageMatrix(matrix);
  }

  private void resetTransitionArrayCallbacks() {
    final Drawable[] transitionArray = this.transitionArray;
    if (transitionArray[0] != null) { transitionArray[0].setCallback(null); }
    if (transitionArray[1] != null) { transitionArray[1].setCallback(null); }
  }

  public void setImageDrawableWithTransition(final Drawable drawable, final boolean crossfade) {
    setImageDrawableWithTransition(drawable, TRANSITION_DURATION_DEFAULT, false);
  }
  public void setImageDrawableWithTransition(final Drawable drawable, final int duration, final boolean crossfade) {
    final Drawable[] transitionArray = this.transitionArray;
    final Drawable prev = getDrawable();

    if (prev != null) {

      transitionArray[0] = prev;
      transitionArray[1] = drawable;
      resetTransitionArrayCallbacks();
      final TransitionDrawable transition = new TransitionDrawable(transitionArray);
      setImageDrawable(transition);
      transition.startTransition(duration);
      lastTransition = transition;

    } else {

      setImageDrawable(drawable);

    }

  }

  @Override
  public void setImageDrawable(final Drawable drawable) {
    // reset last transition
    if (lastTransition != null) {
      lastTransition.resetTransition();
      lastTransition = null;
    }

    // check for temporary scale types
    if (storedScaleType != null && drawable != getDrawable()) {
      replaceScaleType(storedScaleType);
      storedScaleType = null;
    }

    // block layout requests if needed
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
    resetTransitionArrayCallbacks();
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

  /**
   * Custom implementation of transition drawable.
   * This implementation differs from standard {@link android.graphics.drawable.TransitionDrawable} by next points:
   * <ul>
   *   <li>it does not set layers alpha to <code>0</code> or <code>0xFF</code> after drawing</li>
   *   <li>it calls {@link Drawable#invalidateSelf()} <b>before</b> drawing the layers</li>
   * </ul>
   *
   * @see android.graphics.drawable.TransitionDrawable
   * @see LayerDrawable
   *
   * @author Roman Mazur (Stanfy - http://stanfy.com)
   */
  private static class TransitionDrawable extends LayerDrawable {

    /** A transition is about to start. */
    private static final int TRANSITION_STARTING = 0;
    /** The transition has started and the animation is in progress. */
    private static final int TRANSITION_RUNNING = 1;
    /** No transition will be applied. */
    private static final int TRANSITION_NONE = 2;
    /** Final alpha value. */
    private static final int FINAL_ALPHA = 0xFF;

    /**
     * The current state of the transition. One of {@link #TRANSITION_STARTING},
     * {@link #TRANSITION_RUNNING} and {@link #TRANSITION_NONE}
     */
    private int transitionState = TRANSITION_NONE;

    /** Reverse transition flag. */
    private boolean reverse;
    /** Start time. */
    private long startTimeMillis;
    /** Alpha: from. */
    private int from;
    /** Alpha: to. */
    private int to;
    /** Transition duration. */
    private int duration;
    /** Original duration. */
    private int originalDuration;
    /** Alpha value. */
    private int alpha = 0;
    /** Cross fade option flag. */
    private boolean crossFade;

    /** Alphas. */
    private int firstAlpha = 0, secondAlpha = 0;

    /**
     * Create a new transition drawable with the specified list of layers.
     * 2 layers are required for this drawable to work properly.
     */
    public TransitionDrawable(final Drawable[] layers) {
      super(layers);
      if (layers.length != 2) { throw new IllegalArgumentException("You must supply 2 layers for transition"); }
    }

    /**
     * Begin the second layer on top of the first layer.
     * @param durationMillis The length of the transition in milliseconds
     */
    public void startTransition(final int durationMillis) {
      from = 0;
      to = FINAL_ALPHA;
      alpha = 0;
      originalDuration = durationMillis;
      duration = originalDuration;
      reverse = false;
      transitionState = TRANSITION_STARTING;
      invalidateSelf();
    }

    /**
     * Show only the first layer.
     */
    public void resetTransition() {
      alpha = 0;
      transitionState = TRANSITION_NONE;
      invalidateSelf();
    }

    /**
     * Reverses the transition, picking up where the transition currently is.
     * If the transition is not currently running, this will start the transition
     * with the specified duration. If the transition is already running, the last
     * known duration will be used.
     *
     * @param duration The duration to use if no transition is running.
     */
    @SuppressWarnings("unused")
    public void reverseTransition(final int duration) {
      final long time = SystemClock.uptimeMillis();
      // Animation is over
      if (time - startTimeMillis > duration) {
        if (to == 0) {
          from = 0;
          to = FINAL_ALPHA;
          alpha = 0;
          reverse = false;
        } else {
          from = FINAL_ALPHA;
          to = 0;
          alpha = FINAL_ALPHA;
          reverse = true;
        }
        originalDuration = duration;
        this.duration = originalDuration;
        transitionState = TRANSITION_STARTING;
        invalidateSelf();
        return;
      }

      reverse = !reverse;
      from = alpha;
      to = reverse ? 0 : FINAL_ALPHA;
      this.duration = (int) (reverse ? time - startTimeMillis : originalDuration - (time - startTimeMillis));
      transitionState = TRANSITION_STARTING;
    }

    @Override
    public void draw(final Canvas canvas) {
      boolean done = true;

      switch (transitionState) {
      case TRANSITION_STARTING:
        startTimeMillis = SystemClock.uptimeMillis();
        done = false;
        transitionState = TRANSITION_RUNNING;
        break;

      case TRANSITION_RUNNING:
        if (startTimeMillis >= 0) {
          float normalized = (float)
              (SystemClock.uptimeMillis() - startTimeMillis) / duration;
          done = normalized >= 1.0f;
          normalized = Math.min(normalized, 1.0f);
          alpha = (int) (from  + (to - from) * normalized);
        }
        break;

      default:
        done = true;
      }

      final int alpha = this.alpha;
      final boolean crossFade = this.crossFade;
      final Drawable first = getDrawable(0), second = getDrawable(1);

      if (done) {
        if (crossFade && firstAlpha != 0 && alpha == FINAL_ALPHA) {
          firstAlpha = 0;
          first.setAlpha(0);
        }
        if (secondAlpha != FINAL_ALPHA) {
          secondAlpha = FINAL_ALPHA;
          second.setAlpha(FINAL_ALPHA);
        }

        if (alpha == 0 || (!crossFade && second.getOpacity() != PixelFormat.OPAQUE)) {
          first.draw(canvas);
        }
        if (alpha == FINAL_ALPHA) {
          second.draw(canvas);
        }
        return;
      }

      invalidateSelf();

      if (crossFade) {
        firstAlpha = FINAL_ALPHA - alpha;
        first.setAlpha(firstAlpha);
      }
      first.draw(canvas);

      if (alpha > 0) {
        secondAlpha = alpha;
        second.setAlpha(alpha);
        second.draw(canvas);
      }

    }

    /**
     * Enables or disables the cross fade of the drawables. When cross fade
     * is disabled, the first drawable is always drawn opaque. With cross
     * fade enabled, the first drawable is drawn with the opposite alpha of
     * the second drawable. Cross fade is disabled by default.
     *
     * @param enabled True to enable cross fading, false otherwise.
     */
    @SuppressWarnings("unused")
    public void setCrossFadeEnabled(final boolean enabled) {
      crossFade = enabled;
    }

    @Override
    public int getIntrinsicWidth() {
      final int topIntrinsicWidth = getDrawable(1).getIntrinsicWidth();
      return topIntrinsicWidth == 0 ? getDrawable(0).getIntrinsicWidth() : topIntrinsicWidth;
    }

    @Override
    public int getIntrinsicHeight() {
      final int topIntrinsicHeight = getDrawable(1).getIntrinsicHeight();
      return topIntrinsicHeight == 0 ? getDrawable(0).getIntrinsicHeight() : topIntrinsicHeight;
    }

  }

}

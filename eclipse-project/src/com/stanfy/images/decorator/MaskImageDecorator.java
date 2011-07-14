package com.stanfy.images.decorator;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Xfermode;

/**
 * Decorate an image using mask (for corners rounding, for example).
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class MaskImageDecorator implements ImageDecorator {

  /** Custom mask mode. */
  private static final int CUSTOM_MASK = 0;
  /** Corners mask mode. */
  private static final int CORNERS_MASK = 1;

  /** Mask color. */
  private static final int MASK_COLOR = Color.RED;
  /** Xfermode. */
  private static final Xfermode XFERMODE = new PorterDuffXfermode(Mode.SRC_IN);

  /** Decorator mode. */
  private final int mode;

  /** Buffer bitmap. */
  private Bitmap bitmap;
  /** Buffer canvas. */
  private Canvas bitmapCanvas;

  /** Mask bitmap. */
  private final Bitmap mask;
  /** Radius array. */
  private final float[] radiusArray;
  /** Single radius. */
  private final float radius;

  /** Paints. */
  private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG),
                      maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

  /** Cached path. */
  private final Path path = new Path();
  /** Cached rect. */
  private final RectF rect = new RectF();

  {
    fillPaint.setColor(MASK_COLOR);
    maskPaint.setXfermode(XFERMODE);
  }

  /**
   * @param mask custom mask bitmap
   */
  public MaskImageDecorator(final Bitmap mask) {
    this.mode = CUSTOM_MASK;
    this.mask = mask;
    this.radiusArray = null;
    this.radius = 0;
  }

  /**
   * @param radius corners radius
   */
  public MaskImageDecorator(final float radius) {
    this.mode = CORNERS_MASK;
    this.mask = null;
    this.radiusArray = null;
    this.radius = radius;
  }

  /**
   * @param radius corners radius array
   */
  public MaskImageDecorator(final float[] radius) {
    this.mode = CORNERS_MASK;
    this.mask = null;
    this.radiusArray = radius;
    this.radius = 0;
  }

  @Override
  public Bitmap decorateBitmap(final Bitmap bitmap) {
    if (bitmap == null) { return null; }
    final Bitmap buffer = this.bitmap;
    if (buffer == null) { return bitmap; }
    buffer.eraseColor(0);
    final Canvas canvas = this.bitmapCanvas;

    final int width = bitmap.getWidth(), height = bitmap.getHeight();
    rect.set(0, 0, width, height);

    // prepare mask
    switch (mode) {
    case CUSTOM_MASK:
      canvas.drawBitmap(mask, 0, 0, fillPaint);
      break;
    case CORNERS_MASK:
      if (radiusArray == null) {
        float radius = this.radius;
        final float r = Math.min(width, height) * 0.5f;
        if (radius > r) { radius = r; }
        canvas.drawRoundRect(rect, radius, radius, fillPaint);
      } else {
        final Path path = this.path;
        path.reset();
        path.addRoundRect(rect, radiusArray, Path.Direction.CW);
        canvas.drawPath(path, fillPaint);
      }
      break;
    default: return bitmap;
    }

    // main drawing
    canvas.drawBitmap(bitmap, 0, 0, maskPaint);

    // modify source
    bitmap.eraseColor(0);
    new Canvas(bitmap).drawBitmap(buffer, 0, 0, null);

    // return source
    return bitmap;
  }

  @Override
  public void setup(final int width, final int height) {
    if (width <= 0 || height <= 0) { this.bitmap = null; return; }
    Bitmap bitmap = this.bitmap;

    // need a new buffer?
    if (bitmap == null || bitmap.getWidth() != width || bitmap.getHeight() != height) {
      // recycle old buffer
      if (bitmap != null) { bitmap.recycle(); }
      bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888); // high quality
      this.bitmap = bitmap;
      this.bitmapCanvas = new Canvas(bitmap);
    }
  }

}

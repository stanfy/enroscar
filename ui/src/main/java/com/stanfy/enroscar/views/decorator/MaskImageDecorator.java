package com.stanfy.enroscar.views.decorator;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;

/**
 * Decorate an image using mask (for corners rounding, for example).
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class MaskImageDecorator extends BufferBitmapDecorator {

  /** Custom mask mode. */
  private static final int CUSTOM_MASK = 0;
  /** Corners mask mode. */
  private static final int CORNERS_MASK = 1;

  /** Mask color. */
  private static final int MASK_COLOR = Color.RED;

  /** Decorator mode. */
  private final int mode;

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
    maskPaint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
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
  public Bitmap processBitmap(final Bitmap source, final Canvas sourceCanvas) {
    if (source == null) { return null; }
    final Bitmap buffer = this.bitmap;
    if (buffer == null) { return source; }
    buffer.eraseColor(0);
    final Canvas bufferCanvas = this.bitmapCanvas;

    final RectF rect = this.rect;
    // prepare mask
    switch (mode) {
    case CUSTOM_MASK:
      bufferCanvas.drawBitmap(mask, 0, 0, fillPaint);
      break;
    case CORNERS_MASK:
      if (radiusArray == null) {
        float radius = this.radius;
        final float r = Math.min(rect.width(), rect.height()) * 0.5f;
        if (radius > r) { radius = r; }
        bufferCanvas.drawRoundRect(rect, radius, radius, fillPaint);
      } else {
        final Path path = this.path;
        path.reset();
        path.addRoundRect(rect, radiusArray, Path.Direction.CW);
        bufferCanvas.drawPath(path, fillPaint);
      }
      break;
    default: return source;
    }

    // main drawing
    bufferCanvas.drawBitmap(source, 0, 0, maskPaint);

    // modify source
    source.eraseColor(0);
    sourceCanvas.drawBitmap(buffer, 0, 0, null);

    // return source
    return source;
  }

  @Override
  public void setup(final int width, final int height, final int[] state, final int level, final int sourceWidth, final int sourceHeight) {
    final int w = fitSourcePolicy ? Math.min(width, sourceWidth) : width;
    final int h = fitSourcePolicy ? Math.min(height, sourceHeight) : height;
    rect.set(0, 0, w, h);
    if (w <= 0 || h <= 0) { this.bitmap = null; return; }
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

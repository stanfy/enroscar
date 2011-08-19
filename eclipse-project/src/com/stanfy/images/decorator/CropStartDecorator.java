package com.stanfy.images.decorator;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

/**
 * Crop image decorator.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class CropStartDecorator extends BufferBitmapDecorator {

  /** Source bounds. */
  private final Rect srcBounds = new Rect();

  /** Destination bounds. */
  private final Rect dstBounds = new Rect();

  @Override
  public void setup(final int width, final int height, final int[] state, final int level, final int sourceWidth, final int sourceHeight) {
    this.bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888); // high quality
    this.bitmapCanvas = new Canvas(bitmap);
    this.dstBounds.set(0, 0, Math.min(width, sourceWidth), Math.min(height, sourceHeight));
  }

  @Override
  protected Bitmap processBitmap(final Bitmap bitmap, final Canvas canvas) {
    final Rect srcBounds = this.srcBounds, dstBounds = this.dstBounds;
    srcBounds.set(0, 0, Math.min(bitmap.getWidth(), dstBounds.right), Math.min(bitmap.getHeight(), dstBounds.bottom));
    bitmapCanvas.drawBitmap(bitmap, srcBounds, dstBounds, null);
    return this.bitmap;
  }

}

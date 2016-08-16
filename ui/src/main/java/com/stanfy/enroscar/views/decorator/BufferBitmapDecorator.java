package com.stanfy.enroscar.views.decorator;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Decorator that requires a buffer. It caches the bitmap whenever it's possible.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public abstract class BufferBitmapDecorator extends ImageDecoratorAdapter {

  /** Buffer bitmap. */
  Bitmap bitmap;
  /** Buffer canvas. */
  Canvas bitmapCanvas;

  /**
   * Prepare for new width and hight.
   * @param width new width
   * @param height new height
   */
  protected void reset(final int width, final int height) {
    Bitmap bitmap = this.bitmap;
    // recycle old buffer
    if (bitmap != null) { bitmap.recycle(); }
    bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888); // high quality
    this.bitmap = bitmap;
    this.bitmapCanvas = new Canvas(bitmap);
  }

  @Override
  public void setup(final int width, final int height, final int[] state, final int level, final int sourceWidth, final int sourceHeight) {
    if (width <= 0 || height <= 0) { this.bitmap = null; return; }
    final Bitmap bitmap = this.bitmap;

    // need a new buffer?
    if (bitmap == null || bitmap.getWidth() != width || bitmap.getHeight() != height) {
      reset(width, height);
    }
  }

  /** @return the bitmap */
  protected Bitmap getBitmap() { return bitmap; }
  /** @return the bitmapCanvas */
  protected Canvas getBitmapCanvas() { return bitmapCanvas; }

}

package com.stanfy.enroscar.views.decorator;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * Shadow decorator.
 * Always behaves like  {@link #fitSourcePolicy} = true.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class ShadowDecorator extends BufferBitmapDecorator {

  /** Internal coefficient. */
  private static final int BLUR_C = 7;

  /** Shadow paint. */
  private final Paint shadowPaint = new Paint();
  /** Shadow size and blur. */
  private final float size, blur;

  /** Working region. */
  private Rect src = new Rect(), dst = new Rect();

  public ShadowDecorator(final Resources res, final int size) {
    this(res, size, size * BLUR_C);
  }
  public ShadowDecorator(final Resources res, final int size, final float blur) {
    this.size = size * res.getDisplayMetrics().density;
    this.blur = blur;
    shadowPaint.setColor(Color.BLACK);
  }

  @Override
  protected Bitmap processBitmap(final Bitmap source, final Canvas sourceCanvas) {
    final Bitmap buffer = this.bitmap;
    final Canvas bufferCanvas = this.bitmapCanvas;
    final Paint shadowPaint = this.shadowPaint;

    buffer.eraseColor(0);

    final float size = this.size;
    shadowPaint.setShadowLayer(blur, size, size, Color.BLACK);
    bufferCanvas.drawRect(dst, shadowPaint);
    bufferCanvas.drawBitmap(source, src, dst, null);

    source.eraseColor(0);
    sourceCanvas.drawBitmap(buffer, 0, 0, null);

    return source;
  }

  @Override
  public void setup(final int width, final int height, final int[] state, final int level, final int sourceWidth, final int sourceHeight) {
    super.setup(width, height, state, level, sourceWidth, sourceHeight);
    final int w = Math.min(width, sourceWidth);
    final int h = Math.min(height, sourceHeight);
    src.set(0, 0, w, h);
    dst.set(0, 0, (int)(w - size), (int)(h - size));
  }

}

package com.stanfy.images.decorator;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Simple adapter.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public abstract class ImageDecoratorAdapter implements ImageDecorator {

  /** Consider source sizes instead of canvas. */
  boolean fitSourcePolicy;

  /** @param fitSourcePolicy the fitSourcePolicy to set */
  public void setFitSourcePolicy(final boolean fitSourcePolicy) { this.fitSourcePolicy = fitSourcePolicy; }
  /** @return the fitSourcePolicy */
  public boolean isFitSourcePolicy() { return fitSourcePolicy; }

  @Override
  public final Bitmap decorateBitmap(final Bitmap bitmap, final Canvas canvas) {
    return processBitmap(bitmap, canvas == null ? new Canvas(bitmap) : canvas);
  }

  /**
   * @param bitmap bitmap to process
   * @param canvas bitmap canvas (never null)
   * @return result bitmap
   */
  protected abstract Bitmap processBitmap(Bitmap bitmap, Canvas canvas);

  @Override
  public boolean dependsOnDrawableState() { return false; }

}

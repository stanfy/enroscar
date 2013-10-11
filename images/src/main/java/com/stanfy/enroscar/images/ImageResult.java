package com.stanfy.enroscar.images;

import android.graphics.Bitmap;

/**
 * Image loading result.
 */
public class ImageResult {

  /** Drawable. */
  private Bitmap bitmap;

  /** Result type. */
  private ImageSourceType type;

  ImageResult() {

  }

  ImageResult(final Bitmap bitmap, final ImageSourceType type) {
    this.bitmap = bitmap;
    this.type = type;
  }

  void setBitmap(final Bitmap bitmap) {
    this.bitmap = bitmap;
  }

  void setType(final ImageSourceType type) {
    this.type = type;
  }

  public Bitmap getBitmap() {
    return bitmap;
  }

  public ImageSourceType getType() {
    return type;
  }

  @Override
  public String toString() {
    if (bitmap == null) { return "not ready"; }
    return "Bitmap " + bitmap.getWidth() + "x" + bitmap.getHeight() + " from " + type;
  }

}

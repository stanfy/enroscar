package com.stanfy.enroscar.images;

import android.graphics.Bitmap;

/**
 * Image loading result.
 */
public class ImageResult {

  /** Drawable. */
  private Bitmap bitmap;

  /** Result type. */
  private ResultType type;

  ImageResult() {

  }

  ImageResult(final Bitmap bitmap, final ResultType type) {
    this.bitmap = bitmap;
    this.type = type;
  }

  void setBitmap(final Bitmap bitmap) {
    this.bitmap = bitmap;
  }

  void setType(final ResultType type) {
    this.type = type;
  }

  public Bitmap getBitmap() {
    return bitmap;
  }

  public ResultType getType() {
    return type;
  }

  @Override
  public String toString() {
    if (bitmap == null) { return "not ready"; }
    return "Bitmap " + bitmap.getWidth() + "x" + bitmap.getHeight() + " from " + type;
  }

  /** Image loading result type. */
  public enum ResultType {
    MEMORY, CACHE, NETWORK
  }

}

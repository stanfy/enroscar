package com.stanfy.enroscar.utils;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Build;

/**
 * Bitmap utilities.
 */
public class Bitmaps {

  @SuppressLint("NewApi")
  public static int bitmapSize(final Bitmap bitmap) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) {
      return bitmap.getRowBytes() * bitmap.getHeight();
    }
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
      return bitmap.getByteCount();
    }
    return bitmap.getAllocationByteCount();
  }

}

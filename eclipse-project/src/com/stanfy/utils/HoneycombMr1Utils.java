package com.stanfy.utils;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.Build;

/**
 * Honeycomb utilities (API level 12).
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
public class HoneycombMr1Utils extends HoneycombUtils {

  @Override
  public int getBitmapSize(final Bitmap bitmap) {
    return bitmap.getByteCount();
  }

}

package com.stanfy.views.utils;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;

/**
 * Helper class to determine the current background color. Designed to be called from the GUI thread only.
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public final class BGColorGetter {

  private BGColorGetter() { /* hide */ }

  /** Buffer. */
  private static Bitmap bufferBM = Bitmap.createBitmap(1, 1, Config.ARGB_8888);
  /** Buffer. */
  private static Canvas bufferCanvas = new Canvas(bufferBM);
  /** Transformation matrix. */
  private static final Matrix T = new Matrix();
  /** Initial state. */
  private static final float[] T_INIT = new float[] {
    1, 0, 0,
    0, 1, 0,
    0, 0, 1
  };
  static { T.setValues(T_INIT); }

  /**
   * @param bg drawable
   * @param x coordinate
   * @param y coordinate
   * @return color of pixel at (x, y)
   */
  public static int getBGColor(final Drawable bg, final int x, final int y) {
    if (bg == null) { return 0; }
    T.preTranslate(-x, -y);
    final Canvas bgCanvas = bufferCanvas;
    bgCanvas.setMatrix(T);
    bg.draw(bgCanvas);
    return bufferBM.getPixel(0, 0);
  }

}

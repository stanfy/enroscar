package com.stanfy.images.decorator;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/**
 * Draws the specified image on top of input one.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class ComposerDecorator extends ImageDecoratorAdapter {

  /** 'Left' option. */
  public static final int LEFT = 1;
  /** 'Top' option. */
  public static final int TOP = 2;
  /** 'Right' option. */
  public static final int RIGHT = 4;
  /** 'Bottom' option. */
  public static final int BOTTOM = 8;
  /** 'Center vertical' option. */
  public static final int CENTER_VERTICAL = 16;
  /** 'Center horizontal' option. */
  public static final int CENTER_HORIZONTAL = 32;
  /** 'Center' option. */
  public static final int CENTER = CENTER_VERTICAL | CENTER_HORIZONTAL;

  /** Justify flags. */
  private final int justify;

  /** Drawable. */
  private final Drawable drawable;
  /** Drawable size. */
  private final int dWidth, dHeight;
  /** Drawable bounds. */
  private final Rect bounds = new Rect();

  public ComposerDecorator(final Drawable drawable) {
    this(drawable, CENTER);
  }

  public ComposerDecorator(final Drawable drawable, final int justify) {
    this.justify = justify;
    this.drawable = drawable;
    this.dWidth = drawable.getIntrinsicWidth();
    this.dHeight = drawable.getIntrinsicHeight();
  }

  @Override
  public Bitmap processBitmap(final Bitmap bitmap, final Canvas bitmapVanvas) {
    final Drawable d = this.drawable;
    d.setBounds(bounds);
    d.draw(bitmapVanvas);
    return bitmap;
  }

  @Override
  public void setup(final int width, final int height) {
    final int flags = this.justify;
    final Rect bounds = this.bounds;
    final int dWidth = this.dWidth, dHeight = this.dHeight;

    // horizontal
    if ((flags & LEFT) != 0) {
      bounds.left = 0;
      bounds.right = dWidth;
    } else if ((flags & RIGHT) != 0) {
      bounds.left = width - dWidth;
      bounds.right = width;
    } else if ((flags & CENTER_HORIZONTAL) != 0) {
      final int p = (width - dWidth) / 2;
      bounds.left = p;
      bounds.right = width - p;
    }

    // vertical
    if ((flags & TOP) != 0) {
      bounds.top = 0;
      bounds.bottom = dHeight;
    } else if ((flags & BOTTOM) != 0) {
      bounds.top = height - dHeight;
      bounds.bottom = height;
    } else if ((flags & CENTER_VERTICAL) != 0) {
      final int p = (height - dHeight) / 2;
      bounds.top = p;
      bounds.bottom = height - p;
    }
  }

}

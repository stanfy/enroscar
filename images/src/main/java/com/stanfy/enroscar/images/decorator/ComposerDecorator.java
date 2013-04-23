package com.stanfy.enroscar.images.decorator;

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
  /** 'Stretch vertical' option. */
  public static final int STRETCH_VERTICAL = 64;
  /** 'Stretch horizontal' option. */
  public static final int STRETCH_HORIZONTAL = 128;
  /** 'Center' option. */
  public static final int CENTER = CENTER_VERTICAL | CENTER_HORIZONTAL;
  /** 'Stretch' option. */
  public static final int STRETCH = STRETCH_VERTICAL | STRETCH_HORIZONTAL;

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
  public boolean dependsOnDrawableState() { return drawable.isStateful(); }

  @Override
  public Bitmap processBitmap(final Bitmap bitmap, final Canvas bitmapVanvas) {
    final Drawable d = this.drawable;
    d.setBounds(bounds);
    d.draw(bitmapVanvas);
    return bitmap;
  }

  @Override
  public void setup(final int canvasWidth, final int canvasHeight, final int[] state, final int level, final int sourceWidth, final int sourceHeight) {
    final int flags = this.justify;
    final Rect bounds = this.bounds;
    int dWidth = this.dWidth, dHeight = this.dHeight;
    final int width = fitSourcePolicy ? Math.min(canvasWidth, sourceWidth) : canvasWidth;
    final int height = fitSourcePolicy ? Math.min(canvasHeight, sourceHeight) : canvasHeight;
    if (dWidth <= 0) { dWidth = width; }
    if (dHeight <= 0) { dHeight = height; }

    final Drawable d = this.drawable;
    d.setState(state);
    d.setLevel(level);

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
    } else if ((flags & STRETCH_HORIZONTAL) != 0) {
      bounds.left = 0;
      bounds.right = width;
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
    } else if ((flags & STRETCH_VERTICAL) != 0) {
      bounds.top = 0;
      bounds.bottom = height;
    }
  }

  /** @return current bounds */
  protected Rect getBounds() { return bounds; }

}

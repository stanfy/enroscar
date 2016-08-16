package com.stanfy.enroscar.views.decorator;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Chain of decorators.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class ChainDecorator extends ImageDecoratorAdapter {

  /** Internal decorators. */
  private final ImageDecorator[] chain;

  /** Stateful flag. */
  private final boolean stateful;

  public ChainDecorator(final ImageDecorator... decorators) {
    this.chain = decorators;
    for (final ImageDecorator d : decorators) {
      if (d.dependsOnDrawableState()) {
        this.stateful = true;
        return;
      }
    }
    this.stateful = false;
  }

  @Override
  public Bitmap processBitmap(final Bitmap bitmap, final Canvas bitmapVanvas) {
    Bitmap result = bitmap;
    final ImageDecorator[] chain = this.chain;
    for (int i = 0; i < chain.length; i++) {
      final Bitmap next = chain[i].decorateBitmap(result, bitmapVanvas);
      if (next != result) { result.recycle(); }
      result = next;
    }
    return result;
  }

  @Override
  public boolean dependsOnDrawableState() { return stateful; }

  @Override
  public void setup(final int width, final int height, final int[] state, final int level, final int sourceWidth, final int sourceHeight) {
    final ImageDecorator[] chain = this.chain;
    for (int i = 0; i < chain.length; i++) {
      chain[i].setup(width, height, state, level, sourceWidth, sourceHeight);
    }
  }

  public ImageDecorator[] getChain() {
    return chain;
  }

}

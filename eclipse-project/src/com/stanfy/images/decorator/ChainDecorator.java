package com.stanfy.images.decorator;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Chain of decorators.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class ChainDecorator extends ImageDecoratorAdapter {

  /** Internal decorators. */
  private final ImageDecorator[] chain;

  public ChainDecorator(final ImageDecorator... decorators) {
    this.chain = decorators;
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
  public void setup(final int width, final int height, final int[] state, final int level) {
    final ImageDecorator[] chain = this.chain;
    for (int i = 0; i < chain.length; i++) {
      chain[i].setup(width, height, state, level);
    }
  }

  public ImageDecorator[] getChain() {
    return chain;
  }

}

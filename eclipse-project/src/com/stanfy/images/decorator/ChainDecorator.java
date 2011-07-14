package com.stanfy.images.decorator;

import android.graphics.Bitmap;

/**
 * Chain of decorators.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class ChainDecorator implements ImageDecorator {

  /** Internal decorators. */
  private final ImageDecorator[] chain;

  public ChainDecorator(final ImageDecorator... decorators) {
    this.chain = decorators;
  }

  @Override
  public Bitmap decorateBitmap(final Bitmap bitmap) {
    Bitmap result = bitmap;
    final ImageDecorator[] chain = this.chain;
    for (int i = 0; i < chain.length; i++) {
      final Bitmap next = chain[i].decorateBitmap(result);
      if (next != result) { result.recycle(); }
      result = next;
    }
    return result;
  }

  @Override
  public void setup(final int width, final int height) {
    final ImageDecorator[] chain = this.chain;
    for (int i = 0; i < chain.length; i++) {
      chain[i].setup(width, height);
    }
  }

}

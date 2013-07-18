package com.stanfy.enroscar.images;

import android.graphics.drawable.Drawable;
import org.robolectric.Robolectric;

/** Test consumer. */
class Consumer extends ImageConsumer {

  /** Size. */
  final int w, h;

  public Consumer(final int w, final int h) {
    super(Robolectric.application);
    this.w = w;
    this.h = h;
  }

  @Override
  public void setImage(final Drawable d, final boolean animate) {
    // nothing
  }

  @Override
  public void post(final Runnable r) {
    // nothing
  }

  @Override
  protected int getTargetWidth() { return w; }

  @Override
  protected int getTargetHeight() { return h; }

}

package com.stanfy.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.TextView;

import com.stanfy.images.ImagesManagerContext;

/**
 * Text view that can load a remote images and use it as comppound drawables.
 * Early implementation that supports only one drawble(drawableLeft).
 * @author Olexandr Tereshchuk - Stanfy (http://www.stanfy.com)
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public class LoadableTextView extends TextView {

  /** Images manager context. */
  private ImagesManagerContext<?> imagesManagerContext;

  /** Left drawable width/height. */
  private int drawableLeftWidth, drawableLeftHeight;

  public LoadableTextView(final Context context) {
    this(context, null);
  }

  public LoadableTextView(final Context context, final AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs, 0);
  }

  public LoadableTextView(final Context context, final AttributeSet attrs, final int defStyle) {
    super(context, attrs, defStyle);
    init(context, attrs, defStyle);
  }

  private void init(final Context context, final AttributeSet attrs, final int defStyle) {
    final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LoadableTextView, defStyle, 0);
    drawableLeftWidth = a.getDimensionPixelSize(R.styleable.LoadableTextView_drawableLeftWidth, -1);
    drawableLeftHeight = a.getDimensionPixelSize(R.styleable.LoadableTextView_drawableLeftHeight, -1);
    a.recycle();
  }

  /** @param imagesManagerContext the imagesManager context to set */
  public void setImagesManagerContext(final ImagesManagerContext<?> imagesManagerContext) {
    this.imagesManagerContext = imagesManagerContext;
  }

  public void setDrawableLeft(final Uri uri) {
    if (imagesManagerContext != null && ImagesManagerContext.check(uri)) {
      imagesManagerContext.populateTextView(this, uri != null ? uri.toString() : null);
    }
  }

  @Override
  public void setCompoundDrawables(final Drawable left, final Drawable top, final Drawable right, final Drawable bottom) {
    final int lw = drawableLeftWidth, lh = drawableLeftHeight;
    if (left != null && lw != -1 && lh != -1) { left.setBounds(0, 0, lw, lh); }
    super.setCompoundDrawables(left, top, right, bottom);
  }

}

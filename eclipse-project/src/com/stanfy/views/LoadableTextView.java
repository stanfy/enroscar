package com.stanfy.views;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.TextView;

import com.stanfy.images.ImagesManagerContext;

/**
 * Text view that can load a remote images and use it as comppound drawables.
 * Early implementation that supports only one drawble(drawableLeft).
 * @author Olexandr Tereshchuk - Stanfy (http://www.stanfy.com)
 */
public class LoadableTextView extends TextView {
  
  /** Image URI. */
  private Uri imageUri;
  /** Images manager context. */
  private ImagesManagerContext<?> imagesManagerContext;

  public LoadableTextView(final Context context) {
    super(context);
  }

  public LoadableTextView(final Context context, final AttributeSet attrs) {
    super(context, attrs);
  }

  public LoadableTextView(final Context context, final AttributeSet attrs, final int defStyle) {
    super(context, attrs, defStyle);
  }
  
  /** @param imagesManagerContext the imagesManager context to set */
  public void setImagesManagerContext(final ImagesManagerContext<?> imagesManagerContext) {
    this.imagesManagerContext = imagesManagerContext;
  }
  
  public void setDrawableLeft(final Uri uri) {
    if (imageUri != null && imageUri.equals(uri)) { return; }
    imageUri = uri;
    if (imagesManagerContext != null && ImagesManagerContext.check(uri)) {
      imagesManagerContext.populateTextView(this, uri != null ? uri.toString() : null);
    }
  }
}

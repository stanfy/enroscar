package com.stanfy.views;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;

import com.stanfy.images.ImagesManagerContext;

/**
 * Image view that can load a remote image.
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public class LoadableImageView extends ImageView {

  /** Images manager context. */
  private ImagesManagerContext<?> imagesManagerContext;

  public LoadableImageView(final Context context) {
    super(context);
  }

  public LoadableImageView(final Context context, final AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public LoadableImageView(final Context context, final AttributeSet attrs, final int defStyle) {
    super(context, attrs, defStyle);
  }

  /** @param imagesManagerContext the imagesManager context to set */
  public void setImagesManagerContext(final ImagesManagerContext<?> imagesManagerContext) {
    this.imagesManagerContext = imagesManagerContext;
  }

  @Override
  public void setImageURI(final Uri uri) {
    if (imagesManagerContext != null && uri != null && uri.getScheme().startsWith("http")) {
      imagesManagerContext.populateImage(this, uri.toString());
      return;
    }
    super.setImageURI(uri);
  }

}

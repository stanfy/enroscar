package com.stanfy.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.net.Uri;
import android.util.AttributeSet;

import com.stanfy.images.ImagesManagerContext;

/**
 * Image view that can load a remote image.
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public class LoadableImageView extends ImageView {

  /** Skip scaling before caching. */
  private boolean skipScaleBeforeCache;
  /** Image URI. */
  private Uri loadImageUri;
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
    init(context, attrs);
  }

  private void init(final Context context, final AttributeSet attrs) {
    final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LoadableImageView);
    final boolean skipCache = a.getBoolean(R.styleable.LoadableImageView_skipScaleBeforeCache, false);
    a.recycle();

    setSkipScaleBeforeCache(skipCache);
  }

  /** @param imagesManagerContext the imagesManager context to set */
  public void setImagesManagerContext(final ImagesManagerContext<?> imagesManagerContext) {
    this.imagesManagerContext = imagesManagerContext;
  }

  /** @param skipScaleBeforeCache the skipScaleBeforeCache to set */
  public void setSkipScaleBeforeCache(final boolean skipScaleBeforeCache) { this.skipScaleBeforeCache = skipScaleBeforeCache; }

  /** @return the skipScaleBeforeCache */
  public boolean isSkipScaleBeforeCache() { return skipScaleBeforeCache; }

  @Override
  public void setImageURI(final Uri uri) {
    if (loadImageUri != null && loadImageUri.equals(uri)) { return; }
    loadImageUri = uri;
    if (imagesManagerContext != null && ImagesManagerContext.check(uri)) {
      imagesManagerContext.populateImageView(this, uri != null ? uri.toString() : null);
      return;
    }
    super.setImageURI(uri);
  }

}

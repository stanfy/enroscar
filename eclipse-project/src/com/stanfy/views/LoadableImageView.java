package com.stanfy.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;

import com.stanfy.app.beans.BeansManager;
import com.stanfy.images.ImagesLoadListener;
import com.stanfy.images.ImagesManager;
import com.stanfy.images.ImagesManager.ImageHolder;

/**
 * Image view that can load a remote image.
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public class LoadableImageView extends ImageView implements ImagesLoadListenerProvider, RemoteImageDensityProvider {

  /** Use transition. */
  public static final int USE_TRANSITION_NO = 0, USE_TRANSITION_YES = 1, USE_TRANSITION_CROSSFADE = 2;

  /** Skip scaling before caching flag. */
  private boolean skipScaleBeforeCache;
  /** Skip loading indicator flag.  */
  private boolean skipLoadingImage;
  /** Use sampling flag. Sampling can affect the image quality. */
  private boolean useSampling;
  /** Image type. */
  private int imageType;
  /** Use transition option. */
  private int useTransition;

  /** Images load listener. */
  private ImagesLoadListener listener;

  /** Source density. */
  private int sourceDensity;

  /** Image URI. */
  private Uri loadImageUri;
  /** Images manager. */
  private ImagesManager imagesManager;

  /** Loading image. */
  private Drawable loadingImage;


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
    final boolean skipLoadIndicator = a.getBoolean(R.styleable.LoadableImageView_skipLoadingImage, false);
    final Drawable loadingImage = a.getDrawable(R.styleable.LoadableImageView_loadingImage);
    final int type = a.getInt(R.styleable.LoadableImageView_imageType, 0);
    final int sourceDensity = a.getInt(R.styleable.LoadableImageView_sourceDensity, -1);
    final int useTransition = a.getInt(R.styleable.LoadableImageView_useTransition, USE_TRANSITION_NO);
    a.recycle();

    setSourceDensity(sourceDensity);
    setSkipScaleBeforeCache(skipCache);
    setSkipLoadingImage(skipLoadIndicator);
    if (loadingImage != null) {
      setLoadingImageDrawable(loadingImage);
    }
    setImageType(type);
    setUseTransitionMode(useTransition);

    this.imagesManager = BeansManager.get(context).getImagesManager();
  }

  /** @param mode mode specification (see {@link #USE_TRANSITION_NO}, {@link #USE_TRANSITION_YES}, {@link #USE_TRANSITION_CROSSFADE}) */
  public void setUseTransitionMode(final int mode) { this.useTransition = mode; }
  /** @return whether this view wants to use transitions */
  public boolean isUseTransition() { return this.useTransition != USE_TRANSITION_NO; }
  /** @return whether transition must be performed with crossfade option */
  public boolean isTransitionCrossfade() { return this.useTransition == USE_TRANSITION_CROSSFADE; }

  /** @param imageType the imageType to set */
  public void setImageType(final int imageType) { this.imageType = imageType; }
  /** @return the imageType */
  public int getImageType() { return imageType; }

  /** @param skipScaleBeforeCache the skipScaleBeforeCache to set */
  public void setSkipScaleBeforeCache(final boolean skipScaleBeforeCache) { this.skipScaleBeforeCache = skipScaleBeforeCache; }
  /** @return the skipScaleBeforeCache */
  public boolean isSkipScaleBeforeCache() { return skipScaleBeforeCache; }

  /** @param skipLoadingImage the skipLoadingImage to set */
  public void setSkipLoadingImage(final boolean skipLoadingImage) { this.skipLoadingImage = skipLoadingImage; }
  /** @return the skipLoadingImage */
  public boolean isSkipLoadingImage() { return skipLoadingImage; }

  /** @param useSampling the useSampling to set */
  public void setUseSampling(final boolean useSampling) { this.useSampling = useSampling; }
  /** @return the useSampling */
  public boolean isUseSampling() { return useSampling; }

  /** @param listener load listener */
  public void setImagesLoadListener(final ImagesLoadListener listener) {
    this.listener = listener;
    final Object tag = getTag();
    if (tag != null && tag instanceof ImageHolder) { ((ImageHolder) tag).touch(); }
  }
  @Override
  public ImagesLoadListener getImagesLoadListener() { return listener; }

  /** @param sourceDensity the sourceDensity to set */
  public void setSourceDensity(final int sourceDensity) { this.sourceDensity = sourceDensity; }
  @Override
  public int getSourceDensity() { return sourceDensity; }

  /** @param loadingImage the loadingImage to set */
  public void setLoadingImageDrawable(final Drawable loadingImage) {
    if (this.loadingImage != null) { this.loadingImage.setCallback(null); }
    this.loadingImage = loadingImage;
  }
  /** @param loadingImage the loadingImage to set */
  public void setLoadingImageResourceId(final int loadingImage) { setLoadingImageDrawable(getResources().getDrawable(loadingImage)); }
  /** @return the loadingImage */
  public Drawable getLoadingImage() { return loadingImage; }

  @Override
  public void setImageURI(final Uri uri) {
    if (loadImageUri != null && loadImageUri.equals(uri)) { return; }
    loadImageUri = uri;
    if (imagesManager != null) {
      imagesManager.populateImage(this, uri != null ? uri.toString() : null);
      return;
    }
    super.setImageURI(uri);
  }

  private void cancelLoading() {
    if (loadImageUri != null && imagesManager != null) {
      imagesManager.cancelImageLoading(this);
      loadImageUri = null;
    }
  }
  @Override
  public void onStartTemporaryDetach() {
    super.onStartTemporaryDetach();
    cancelLoading();
  }
  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    cancelLoading();
  }

}

package com.stanfy.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.CompoundButton;

import com.stanfy.enroscar.beans.BeansManager;
import com.stanfy.images.ImagesManager;

/**
 * Compound button that can have a drawable to be drawn and scaled on the center.
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public class LoadableComppoundButton extends CompoundButton implements RemoteImageDensityProvider {

  /** Button resource. */
  private int buttonResource;

  /** Buttton drawable. */
  private Drawable buttonDrawable;

  /** Source density. */
  private int sourceDensity;

  /** Image URI. */
  private Uri imageUri;

  /** Images manager. */
  private final ImagesManager imagesManager;

  public LoadableComppoundButton(final Context context) {
    this(context, null);
  }

  public LoadableComppoundButton(final Context context, final AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public LoadableComppoundButton(final Context context, final AttributeSet attrs, final int defStyle) {
    super(context, attrs, defStyle);

    final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LoadableComppoundButton);
    final Drawable d = a.getDrawable(R.styleable.LoadableComppoundButton_android_src);
    final int sourceDensity = a.getInt(R.styleable.LoadableComppoundButton_sourceDensity, -1);
    a.recycle();

    setSourceDensity(sourceDensity);
    if (d != null) { setButtonDrawable(d); }
    imagesManager = BeansManager.get(context).getContainer().getBean(ImagesManager.class);
  }

  /** @param sourceDensity the sourceDensity to set */
  public void setSourceDensity(final int sourceDensity) { this.sourceDensity = sourceDensity; }
  @Override
  public int getSourceDensity() { return sourceDensity; }

  @Override
  public void setButtonDrawable(final int resid) {
    if (resid != 0 && resid == buttonResource) { return; }

    buttonResource = resid;

    Drawable d = null;
    if (buttonResource != 0) {
      d = getResources().getDrawable(buttonResource);
    }
    setButtonDrawable(d);
  }

  @Override
  public void setButtonDrawable(final Drawable d) {
    if (d != null) {
      if (buttonDrawable != null) {
        buttonDrawable.setCallback(null);
        unscheduleDrawable(buttonDrawable);
      }
      d.setCallback(this);
      if (d.isStateful()) { d.setState(getDrawableState()); }
      d.setVisible(getVisibility() == VISIBLE, false);
      buttonDrawable = d;
      buttonDrawable.setState(null);
    }

    refreshDrawableState();
  }

  public void setButtonDrawableUri(final Uri uri) {
    if (imageUri != null && imageUri.equals(uri)) { return; }
    imageUri = uri;
    if (imagesManager != null) {
      imagesManager.populateImage(this, uri != null ? uri.toString() : null);
    }
  }

  @Override
  protected void drawableStateChanged() {
    super.drawableStateChanged();

    if (buttonDrawable != null) {
      final int[] myDrawableState = getDrawableState();

      // Set the state of the Drawable
      buttonDrawable.setState(myDrawableState);

      invalidate();
    }
  }

  @Override
  protected boolean verifyDrawable(final Drawable who) { return super.verifyDrawable(who) || who == buttonDrawable; }

  @Override
  protected void onDraw(final Canvas canvas) {
    super.onDraw(canvas);
    final Drawable d = buttonDrawable;
    if (d == null) { return; }
    int l = getPaddingLeft(), t = getPaddingTop();
    final int r = getPaddingRight(), b = getPaddingBottom();
    final int w = d.getIntrinsicWidth(), h = d.getIntrinsicHeight(), ww = getWidth() - l - r, hh = getHeight() - t - b;
    if (w > ww || h > hh) {
      d.setBounds(l, t, l + ww, t + hh);
    } else {
      l += (ww - w) >> 1;
      t += (hh - h) >> 1;
      d.setBounds(l, t, l + w, t + h);
    }
    d.draw(canvas);
  }

  private void cancelLoading() {
    if (imageUri != null && imagesManager != null) {
      imagesManager.cancelImageLoading(this);
      imageUri = null;
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

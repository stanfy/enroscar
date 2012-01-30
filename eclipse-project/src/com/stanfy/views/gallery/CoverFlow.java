package com.stanfy.views.gallery;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Transformation;

/**
 * Idea from <a href="http://www.inter-fuser.com/2010/02/android-coverflow-widget-v2.html">CoverFlow widget</a>.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class CoverFlow extends Gallery {

  /** The maximum angle the child ImageView will be rotated by. */
  public static final int DEFAULT_MAX_ROTATION_ANGLE = 50;

  /** Graphics Camera used for transforming the matrix of ImageViews. */
  private Camera mCamera = new Camera();

  /** The maximum angle the Child ImageView will be rotated by. */
  private int mMaxRotationAngle = DEFAULT_MAX_ROTATION_ANGLE;

  /** The center of the Coverflow. */
  private int mCovertflowCenter;

  /** Angle changed flag. */
  private boolean zoomChanged = true;

  /** Zoom factor. */
  private float zoomFactor = 0;

  public CoverFlow(final Context context) {
    super(context);
    init();
  }

  public CoverFlow(final Context context, final AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public CoverFlow(final Context context, final AttributeSet attrs, final int defStyle) {
    super(context, attrs, defStyle);
    init();
  }

  private void init() {
    setStaticTransformationsEnabled(true);
  }

  /** @return the max rotational angle of the image */
  public int getMaxRotationAngle() { return mMaxRotationAngle; }
  /** @param maxRotationAngle the max rotational angle of the image */
  public void setMaxRotationAngle(final int maxRotationAngle) { mMaxRotationAngle = maxRotationAngle; }

  @Override
  protected boolean getChildStaticTransformation(final View child, final Transformation t) {
    final int childCenter = getCenterOfView(child);
    if (childCenter == mCovertflowCenter) { return false;  }

    final int childWidth = child.getWidth();

    t.clear();
    t.setTransformationType(Transformation.TYPE_MATRIX);

    int rotationAngle = (int)(((float)(mCovertflowCenter - childCenter) / childWidth) *  mMaxRotationAngle);
    if (Math.abs(rotationAngle) > mMaxRotationAngle) {
      rotationAngle = (rotationAngle < 0) ? -mMaxRotationAngle : mMaxRotationAngle;
    }
    transformImageBitmap(child, t, rotationAngle);

    return true;
  }

  @Override
  protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    if (zoomChanged) {
      final boolean[] scrap = new boolean[1];
      final View view = obtainView(getSelectedItemPosition(), scrap);
      if (view != null) {
        // Return to scrap
        mRecycler.addScrapView(view);
        final int myWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        zoomFactor = (float)myWidth / view.getMeasuredWidth() / 2;
      }
    }
  }

  @Override
  protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
    mCovertflowCenter = getCenterOfGallery();
    zoomChanged = true;
    super.onSizeChanged(w, h, oldw, oldh);
  }

  /**
   * Transform the image bitmap by the angle passed.
   * @param imageView ImageView the ImageView whose bitmap we want to rotate
   * @param t transformation
   * @param rotationAngle the Angle by which to rotate the Bitmap
   */
  private void transformImageBitmap(final View child, final Transformation t, final int rotationAngle) {
    mCamera.save();
    final Matrix imageMatrix = t.getMatrix();
    final int imageHeight = child.getWidth();
    final int imageWidth = child.getHeight();
    final int rotation = Math.min(Math.abs(rotationAngle), mMaxRotationAngle);

    //As the angle of the view gets less, zoom in
    mCamera.translate(0.0f, 0.0f, rotation * zoomFactor);

    mCamera.rotateY(rotationAngle);
    mCamera.getMatrix(imageMatrix);
    imageMatrix.preTranslate(-(imageWidth / 2), -(imageHeight / 2));
    imageMatrix.postTranslate((imageWidth / 2), (imageHeight / 2));
    mCamera.restore();
  }

}

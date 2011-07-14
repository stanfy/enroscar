package com.stanfy.views;

import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.widget.ImageView.ScaleType;

/**
 * Methods that are hidden inside the ImageView Android implementation.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
final class ImageViewHiddenMethods {

  /** Temp objects. */
  private static final RectF SRC_RECT = new RectF(), DST_RECT = new RectF();

  private ImageViewHiddenMethods() { /* hide */ }

  private static Matrix.ScaleToFit scaleTypeToScaleToFit(final ScaleType st)  {
    switch (st) {
    case FIT_XY: return Matrix.ScaleToFit.FILL;
    case FIT_START: return Matrix.ScaleToFit.START;
    case FIT_CENTER: return Matrix.ScaleToFit.CENTER;
    case FIT_END: return Matrix.ScaleToFit.END;
    default:
      return null;
    }
  }

  public static void configureBounds(final ImageView imageView) {
    final Drawable mDrawable = imageView.getDrawable();
    if (mDrawable == null || !imageView.isHaveFrame()) {
      return;
    }

    Matrix mDrawMatrix = null;

    final int mPaddingLeft = imageView.getPaddingLeft();
    final int mPaddingTop = imageView.getPaddingTop();
    final int mPaddingRight = imageView.getPaddingRight();
    final int mPaddingBottom = imageView.getPaddingBottom();

    final ScaleType mScaleType = imageView.getScaleType();

    final Matrix mMatrix = imageView.getImageMatrix();

    final int dwidth = mDrawable.getIntrinsicWidth();
    final int dheight = mDrawable.getIntrinsicHeight();

    final int vwidth = imageView.getWidth() - mPaddingLeft - mPaddingRight;
    final int vheight = imageView.getHeight() - mPaddingTop - mPaddingBottom;

    final boolean fits = (dwidth < 0 || vwidth == dwidth) && (dheight < 0 || vheight == dheight);

    final float half = 0.5f;

    final RectF mTempSrc = SRC_RECT, mTempDst = DST_RECT;

    if (dwidth <= 0 || dheight <= 0 || ScaleType.FIT_XY == mScaleType) {
      /* If the drawable has no intrinsic size, or we're told to
            scaletofit, then we just fill our entire view.
       */
      mDrawable.setBounds(0, 0, vwidth, vheight);
      mDrawMatrix = null;
    } else {
      // We need to do the scaling ourself, so have the drawable
      // use its native size.
      mDrawable.setBounds(0, 0, dwidth, dheight);

      if (ScaleType.MATRIX == mScaleType) {
        // Use the specified matrix as-is.
        if (mMatrix.isIdentity()) {
          mDrawMatrix = null;
        } else {
          mDrawMatrix = mMatrix;
        }
      } else if (fits) {
        // The bitmap fits exactly, no transform needed.
        mDrawMatrix = null;
      } else if (ScaleType.CENTER == mScaleType) {
        // Center bitmap in view, no scaling.
        mDrawMatrix = mMatrix;
        mDrawMatrix.setTranslate((int) ((vwidth - dwidth) * half + half),
            (int) ((vheight - dheight) * half + half));
      } else if (ScaleType.CENTER_CROP == mScaleType) {
        mDrawMatrix = mMatrix;

        float scale;
        float dx = 0, dy = 0;

        if (dwidth * vheight > vwidth * dheight) {
          scale = (float) vheight / (float) dheight;
          dx = (vwidth - dwidth * scale) * half;
        } else {
          scale = (float) vwidth / (float) dwidth;
          dy = (vheight - dheight * scale) * half;
        }

        mDrawMatrix.setScale(scale, scale);
        mDrawMatrix.postTranslate((int) (dx + half), (int) (dy + half));
      } else if (ScaleType.CENTER_INSIDE == mScaleType) {
        mDrawMatrix = mMatrix;
        float scale;
        float dx;
        float dy;

        if (dwidth <= vwidth && dheight <= vheight) {
          scale = 1.0f;
        } else {
          scale = Math.min((float) vwidth / (float) dwidth,
              (float) vheight / (float) dheight);
        }

        dx = (int) ((vwidth - dwidth * scale) * half + half);
        dy = (int) ((vheight - dheight * scale) * half + half);

        mDrawMatrix.setScale(scale, scale);
        mDrawMatrix.postTranslate(dx, dy);
      } else {
        // Generate the required transform.
        mTempSrc.set(0, 0, dwidth, dheight);
        mTempDst.set(0, 0, vwidth, vheight);

        mDrawMatrix = mMatrix;
        mDrawMatrix.setRectToRect(mTempSrc, mTempDst,
            scaleTypeToScaleToFit(mScaleType));
      }
    }

    imageView.setDrawMatrix(mDrawMatrix);
  }

}

package com.stanfy.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

/**
 * Image view that slightly extends possibilities of a standard image view.
 * <p>
 *   Features:
 *   <ul>
 *     <li><b>rounded corners</b> - set cornersRadius > 0 (attribute {@code android:radius})</li>
 *   </ul>
 * </p>
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public class ImageView extends android.widget.ImageView {

  /** Round corners flag. */
  private int cornersRadius;

  /** Help color. */
  private static final int CORNERS_HELP_COLOR = 0xff434343;
  /** Xfermode. */
  private static final Xfermode XFERMODE = new PorterDuffXfermode(Mode.SRC_IN);

  /** Paint. */
  private final Paint paint = new Paint();

  public ImageView(final Context context) {
    super(context);
    init(context, null);
  }

  public ImageView(final Context context, final AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public ImageView(final Context context, final AttributeSet attrs, final int defStyle) {
    super(context, attrs, defStyle);
    init(context, attrs);
  }

  private void init(final Context context, final AttributeSet attrs) {
    paint.setAntiAlias(true);
    paint.setColor(CORNERS_HELP_COLOR);

    final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ImageView);
    cornersRadius = a.getDimensionPixelSize(R.styleable.ImageView_android_radius, 0);
    a.recycle();
  }

  /**
   * Set a radius of corners when drawing a bitmap image. Works for {@link BitmapDrawable} only.
   * @param r radius to set
   */
  public void setCornersRadius(final int r) { this.cornersRadius = r; }

  @Override
  protected void onDraw(final Canvas canvas) {
    final Drawable d = getDrawable();
    if (d == null) { return; }
    final int r = cornersRadius;
    if (r > 0 && d instanceof BitmapDrawable) {

      final int pl = getPaddingLeft(), pt = getPaddingTop();
      final int resultW = getMeasuredWidth() - pl - getPaddingRight(), resultH = getMeasuredHeight() - pt - getPaddingBottom();
      if (resultW <= 0 || resultH <= 0) {
        super.onDraw(canvas);
        return;
      }

      final Bitmap bitmap = Bitmap.createBitmap(resultW, resultH, Bitmap.Config.ARGB_8888);
      final Canvas c = new Canvas(bitmap);
      final RectF rectF = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());

      c.drawARGB(0, 0, 0, 0);
      final Paint p = paint;
      p.setXfermode(null);
      c.drawRoundRect(rectF, r, r, p);
      p.setXfermode(XFERMODE);
      c.drawBitmap(((BitmapDrawable)d).getBitmap(), null, rectF, p);

      final Rect finalRect = new Rect(pl, pt, pl + bitmap.getWidth(), pt + bitmap.getHeight());
      canvas.drawBitmap(bitmap, null, finalRect, null);
      bitmap.recycle();
    } else {
      super.onDraw(canvas);
    }
  }

}

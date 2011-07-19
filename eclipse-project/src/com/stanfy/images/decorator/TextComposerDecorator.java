/**
 * 
 */
package com.stanfy.images.decorator;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;

/**
 * @author Vladislav Lipskiy - Stanfy (http://www.stanfy.com)
 */
public class TextComposerDecorator extends ComposerDecorator {

  /** Text to draw. */
  private String drawText;
  
  /** Paint object ot define style. */
  private Paint paint;
  
  /** Offset by Y to center text. */
  private static final int Y_OFFSET = 5;
  
  public TextComposerDecorator(final Drawable drawable) {
    this(drawable, ComposerDecorator.CENTER);
  }
  
  public TextComposerDecorator(final Drawable drawable, final int justify) {
    super(drawable, justify);
    this.paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    paint.setTextAlign(Paint.Align.CENTER);
  }
  
  @Override
  public Bitmap processBitmap(final Bitmap bitmap, final Canvas canvas) {
    final Bitmap b = super.processBitmap(bitmap, canvas);
    if (drawText != null) {
      canvas.drawText(drawText, getBounds().centerX(), getBounds().centerY() + Y_OFFSET, paint);
    }
    return b;
  }
  
  public void setText(final String text) {
    this.drawText = text;
  }
  
  public void setTextSize(final float textSize) {
    paint.setTextSize(textSize);
  }
  
  public void setTextColor(final int textColor) {
    paint.setColor(textColor);
  }
  
  public void setTextTypeface(final Typeface typeface) {
    paint.setTypeface(typeface);
  }

}

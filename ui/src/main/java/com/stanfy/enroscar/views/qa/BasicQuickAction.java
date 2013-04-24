package com.stanfy.enroscar.views.qa;

import android.content.Context;
import android.graphics.drawable.Drawable;

/**
 * Simple quick action description.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class BasicQuickAction {

  /** Action image. */
  Drawable drawable;

  /** Action title. */
  CharSequence title;

  public BasicQuickAction() { /* nothing */ }

  public BasicQuickAction(final Drawable drawable, final CharSequence title) {
    setDrawable(drawable);
    setTitle(title);
  }

  public BasicQuickAction(final Context context, final int drawableId, final int titleId) {
    setDrawable(context.getResources().getDrawable(drawableId));
    setTitle(context.getText(titleId));
  }

  /** @param drawable action image */
  public void setDrawable(final Drawable drawable) { this.drawable = drawable; }
  /** @return the action image */
  public Drawable getDrawable() { return drawable; }

  /** @param title the title to set */
  public void setTitle(final CharSequence title) { this.title = title; }
  /** @return the title */
  public CharSequence getTitle() { return title; }

}

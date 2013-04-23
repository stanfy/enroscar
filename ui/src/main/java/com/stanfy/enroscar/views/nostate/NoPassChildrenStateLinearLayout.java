package com.stanfy.enroscar.views.nostate;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * {@link LinearLayout} that does not pass its state to children
 * after {@link #setPressed(boolean)}, {@link #setSelected(boolean)}.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class NoPassChildrenStateLinearLayout extends LinearLayout {

  public NoPassChildrenStateLinearLayout(final Context context) {
    super(context);
  }

  public NoPassChildrenStateLinearLayout(final Context context, final AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  public void dispatchSetSelected(final boolean selected) { /* nothing */ }

  @Override
  protected void dispatchSetPressed(final boolean pressed) { /* nothing */ }

}

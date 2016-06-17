package com.stanfy.enroscar.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Checkable;
import android.widget.RelativeLayout;

import static com.stanfy.enroscar.views.CheckableConsts.CHECKED_ATTRS;
import static com.stanfy.enroscar.views.CheckableConsts.CHECKED_STATE_SET;

/**
 * {@link RelativeLayout} that implements {@link Checkable}.
 * @see RelativeLayout
 */
public class CheckableRelativeLayout extends RelativeLayout implements Checkable {

  /** On checked change listener. */
  private OnCheckedChangeListener listener;

  /** Checked flag. */
  private boolean checked;

  public CheckableRelativeLayout(final Context context) {
    super(context);
  }

  public CheckableRelativeLayout(final Context context, final AttributeSet attrs, final int defStyle) {
    super(context, attrs, defStyle);
    init(context, attrs);
  }

  public CheckableRelativeLayout(final Context context, final AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  private void init(final Context context, final AttributeSet attrs) {
    final TypedArray a = context.obtainStyledAttributes(attrs, CHECKED_ATTRS);
    setChecked(a.getBoolean(0, false));
    a.recycle();
  }

  @Override
  public void setChecked(final boolean checked) {
    if (this.checked != checked) {
      this.checked = checked;
      refreshDrawableState();
      if (listener != null) { listener.onCheckedChanged(checked); }
    }
  }

  @Override
  public void toggle() {
    setChecked(!checked);
  }

  @Override
  public boolean isChecked() {
    return checked;
  }

  @Override
  public boolean dispatchPopulateAccessibilityEvent(final AccessibilityEvent event) {
      boolean populated = super.dispatchPopulateAccessibilityEvent(event);
      if (!populated) {
          event.setChecked(checked);
      }
      return populated;
  }

  @Override
  protected int[] onCreateDrawableState(final int extraSpace) {
    final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
    if (checked) {
      mergeDrawableStates(drawableState, CHECKED_STATE_SET);
    }
    return drawableState;
  }

  /** @return current on checked change listener instance */
  public OnCheckedChangeListener getOnCheckedChangeListener() { return listener; }
  /** @param listener on checked change listener to set */
  public void setOnCheckedChangeListener(final OnCheckedChangeListener listener) { this.listener = listener; }

  /**
   * Interface definition for a callback to be invoked when the checked state
   * is changed.
   */
  public interface OnCheckedChangeListener {
    /**
     * Called when the checked state has changed.
     * @param value checked value
     */
    void onCheckedChanged(final boolean value);
  }

}

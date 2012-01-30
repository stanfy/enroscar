package com.stanfy.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.Checkable;

/**
 * @author Vladislav Lipskiy - Stanfy (http://www.stanfy.com)
 * @author Olexandr Tereshchuk - Stanfy (http://www.stanfy.com)
 */
public class RelativeLayout extends android.widget.RelativeLayout implements Checkable {
  
  /** Background Drawable ID. */
  private int checkedBgDrawable;
  
  /** On checked change listener. */
  private OnCheckedChangeListener listener;
  
  /** Checked flag. */
  private boolean isChecked;
  
  /**
   * @param context
   */
  public RelativeLayout(final Context context) {
    super(context);
  }

  /**
   * @param context
   * @param attrs
   * @param defStyle
   */
  public RelativeLayout(final Context context, final AttributeSet attrs,
      final int defStyle) {
    super(context, attrs, defStyle);
    init(context, attrs);
  }

  /**
   * @param context
   * @param attrs
   */
  public RelativeLayout(final Context context, final AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }
  
  private void init(final Context context, final AttributeSet attrs) {
    final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RelativeLayout);
    checkedBgDrawable = a.getResourceId(R.styleable.RelativeLayout_checkedBgDrawable, 0);
    a.recycle();
  }
  
  @Override
  protected void onDraw(final Canvas canvas) {
    super.onDraw(canvas);
    if (isChecked) {
      setBackgroundResource(checkedBgDrawable);
    } else {
      setBackgroundResource(android.R.color.transparent);
    }
  }

  @Override
  public boolean isChecked() { return isChecked; }

  @Override
  public void setChecked(final boolean checked) {
    if (isChecked != checked) {
      isChecked = checked;
      if (listener != null) { listener.onCheckedChanged(isChecked); }
    }
  }

  @Override
  public void toggle() {
    setChecked(!isChecked);
  }
  
  /** @return current on checked change listener instance */
  public OnCheckedChangeListener getOnCheckedChangeListener() { return listener; }
  /** @param listener on checked change listener to set */
  public void setOnCheckedChangeListener(final OnCheckedChangeListener listener) { this.listener = listener; }



  /**
   * <p>
   * Interface definition for a callback to be invoked when the checked state
   * is changed.
   * </p>
   */
  public interface OnCheckedChangeListener {
    /**
     * <p>
     * Called when the checked state has changed.
     * </p>
     *
     * @param value
     *          checked value
     */
    void onCheckedChanged(final boolean value);
  }

}

package com.stanfy.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.text.Layout;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Checkable;
import android.widget.TextView;

/**
 * Workaround for an android <a href="http://code.google.com/p/android/issues/detail?id=2254">bug</a>.
 * Ellipsize settings are not respected!
 * Please use it only as a workaround.
 */
public class EllipsizingTextView extends TextView implements Checkable {

  /** How to ellipsize. */
  private static final String ELLIPSIS = "...";

  /** Checked state attributes. */
  private static final int[] CHECKED_ATTRS = {android.R.attr.checked};
  /** Checked state set. */
  private static final int[] CHECKED_STATE_SET = {android.R.attr.state_checked};

  /** Checked state. */
  private boolean checked;

  /** Affect flag. */
  private boolean isEllipsized;
  /** Stale flag. */
  private boolean isStale;
  /** Programmatic change. */
  private boolean programmaticChange;
  /** Full text flag. */
  private String fullText;
  /** Max lines count. */
  private int maxLines;
  /** Line spacing. */
  private float lineSpacingMultiplier;
  /** Padding. */
  private float lineAdditionalVerticalPadding;

  public EllipsizingTextView(final Context context) {
    super(context);
  }

  public EllipsizingTextView(final Context context, final AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  public EllipsizingTextView(final Context context, final AttributeSet attrs, final int defStyle) {
    super(context, attrs, defStyle);
    init(context, attrs);
  }

  private void init(final Context context, final AttributeSet attrs) {
    TypedArray a = context.obtainStyledAttributes(attrs, CHECKED_ATTRS);
    setChecked(a.getBoolean(0, false));
    a.recycle();
  }

  /**
   * <p>Changes the checked state of this text view.</p>
   *
   * @param checked true to check the text, false to uncheck it
   */
  @Override
  public void setChecked(final boolean checked) {
    if (this.checked != checked) {
      this.checked = checked;
      refreshDrawableState();
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
    if (isChecked()) {
      mergeDrawableStates(drawableState, CHECKED_STATE_SET);
    }
    return drawableState;
  }

  public boolean isEllipsized() { return isEllipsized; }

  @Override
  public void setMaxLines(final int maxLines) {
    super.setMaxLines(maxLines);
    this.maxLines = maxLines;
    isStale = true;
  }

  @Override
  public int getMaxLines() { return maxLines; }

  @Override
  public void setLineSpacing(final float add, final float mult) {
    this.lineAdditionalVerticalPadding = add;
    this.lineSpacingMultiplier = mult;
    super.setLineSpacing(add, mult);
  }

  @Override
  protected void onTextChanged(final CharSequence text, final int start, final int before, final int after) {
    super.onTextChanged(text, start, before, after);
    if (!programmaticChange) {
      fullText = text.toString();
      isStale = true;
    }
  }

  @Override
  protected void onDraw(final Canvas canvas) {
    if (isStale) {
      super.setEllipsize(null);
      resetText();
    }
    super.onDraw(canvas);
  }

  @SuppressLint({ "NewApi", "FieldGetter" })
  private void resetText() {
    final int maxLines = getMaxLines();
    String workingText = fullText;
    boolean ellipsized = false;
    if (maxLines != -1) {
      final Layout layout = createWorkingLayout(workingText);
      if (layout.getLineCount() > maxLines) {
        workingText = fullText.substring(0, layout.getLineEnd(maxLines - 1)).trim();
        while (createWorkingLayout(workingText + ELLIPSIS).getLineCount() > maxLines) {
          final int lastSpace = workingText.lastIndexOf(' ');
          if (lastSpace == -1) {
            break;
          }
          workingText = workingText.substring(0, lastSpace);
        }
        workingText = workingText + ELLIPSIS;
        ellipsized = true;
      }
    }
    if (!workingText.equals(getText())) {
      programmaticChange = true;
      try {
        setText(workingText);
      } finally {
        programmaticChange = false;
      }
    }
    isStale = false;
    if (ellipsized != isEllipsized) {
      isEllipsized = ellipsized;
      // can invoke some listeners here
    }
  }

  private Layout createWorkingLayout(final String workingText) {
    final int width = getWidth() - getPaddingLeft() - getPaddingRight();
    return new StaticLayout(workingText, getPaint(), width > 0 ? width : 0,
        Alignment.ALIGN_NORMAL, lineSpacingMultiplier, lineAdditionalVerticalPadding, false);
  }

  @Override
  public void setEllipsize(final TruncateAt where) {
    // Ellipsize settings are not respected!
  }

}

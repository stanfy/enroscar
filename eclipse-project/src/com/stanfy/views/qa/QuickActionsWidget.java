package com.stanfy.views.qa;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.stanfy.DebugFlags;
import com.stanfy.views.R;
import com.stanfy.views.qa.QuickActionsAdapter.ActionTag;

/**
 * Quick actions popup.
 * Greatly inspired by <a href="https://github.com/cyrilmottier/GreenDroid/blob/master/GreenDroid/src/greendroid/widget/QuickActionWidget.java">GreenDroid</a>.
 * @author Benjamin Fellous
 * @author Cyril Mottier
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public abstract class QuickActionsWidget extends PopupWindow {

  /** Debug flag. */
  protected static final boolean DEBUG = DebugFlags.DEBUG_GUI;
  /** Logging tag. */
  protected static final String TAG = "QA";

  /** Default arrow offset. */
  private static final int DEFAULT_ARROW_OFFSET = 5;
  /** Max widget width. */
  protected static final int MAX_WIDTH = 320;

  /** Measure performed flag. */
  private static final int SPECS_DONE = 1;

  /** Owning context. */
  private Context context;

  /** Private flags. */
  private int privateFlags;

  /** Buffer for anchor location. */
  private final int[] locationBuffer = new int[2];
  /** Anchor region. */
  private final Rect anchorRect = new Rect();

  /** Y position. */
  private int top;
  /** Relative position. */
  private boolean isAbove;

  /** Maximum width. */
  private int maxWidth;
  /** Screen width. */
  private int screenWidth;
  /** Screen height. */
  private int screenHeight;

  /** Arrow offset. */
  private int arrowOffset;

  /** Dismiss on click option. */
  private boolean dismissOnClick = true;

  /** Click listeners for views. */
  private final OnClickListener clickListener = new OnClickListener() {
    @Override
    public void onClick(final View v) {
      if (onQuickActionClickListener != null) {
        final ActionTag tag = (ActionTag)v.getTag(QuickActionsAdapter.TAG_INDEX);
        onQuickActionClickListener.onQuickActionClicked(QuickActionsWidget.this, tag.position);
      }
      if (dismissOnClick) { dismiss(); }
    }
  };

  /** 'Dirty' flag. */
  boolean actionsChanged;

  /** Click listener. */
  OnQuickActionClickListener onQuickActionClickListener;
  /** Actions adapter. */
  QuickActionsAdapter quickActionsAdapter;

  /**
   * @param context context instance
   */
  public QuickActionsWidget(final Context context) {
    super(context);
    this.context = context;

    actionsChanged = true;
    final float density = context.getResources().getDisplayMetrics().density;

    final WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
    screenWidth = windowManager.getDefaultDisplay().getWidth();
    screenHeight = windowManager.getDefaultDisplay().getHeight();
    final float half = 0.5f;
    maxWidth = Math.min(screenWidth, Math.min(screenHeight, (int)(MAX_WIDTH * density + half)));

    final TypedArray a = context.obtainStyledAttributes(R.styleable.QuickActionsWidget);
    final int defaultOffset = (int)(density * DEFAULT_ARROW_OFFSET);
    final int arrowOffset = a.getDimensionPixelOffset(R.styleable.QuickActionsWidget_qaArrowOffset, defaultOffset);
    a.recycle();

    setArrowOffset(arrowOffset);
    setFocusable(true);
    setTouchable(true);
    setOutsideTouchable(true);

    setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
    setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
  }

  /** @param onQuickActionClickListener the onQuickActionClickListener to set */
  public void setOnQuickActionClickListener(final OnQuickActionClickListener onQuickActionClickListener) {
    this.onQuickActionClickListener = onQuickActionClickListener;
  }

  /** @param quickActionsAdapter quick actions adapter instance */
  public void setQuickActionsAdapter(final QuickActionsAdapter quickActionsAdapter) {
    if (this.quickActionsAdapter != null) {
      this.quickActionsAdapter.setWidget(null);
    }
    if (quickActionsAdapter != null) {
      quickActionsAdapter.setWidget(this);
    }
    this.quickActionsAdapter = quickActionsAdapter;
  }
  /** @return the quickActionsAdapter */
  public QuickActionsAdapter getQuickActionsAdapter() { return quickActionsAdapter; }

  /** @param arrowOffset the arrowOffset to set */
  public void setArrowOffset(final int arrowOffset) { this.arrowOffset = arrowOffset; }
  /** @return the arrowOffset */
  public int getArrowOffset() { return arrowOffset; }

  /**
   * @param layoutId context view layout identifier
   */
  public void setContentView(final int layoutId) {
    setContentView(LayoutInflater.from(context).inflate(layoutId, null));
  }

  /** @param dismissOnClick dismiss on click option */
  public void setDismissOnClick(final boolean dismissOnClick) { this.dismissOnClick = dismissOnClick; }
  /** @return dismiss on click option */
  public boolean getDismissOnClick() { return dismissOnClick; }

  /** @return context instance */
  protected Context getContext() { return context; }
  /** @return the onQuickActionClickListener */
  protected OnQuickActionClickListener getOnQuickActionClickListener() { return onQuickActionClickListener; }
  /** @return the screenWidth */
  protected int getScreenWidth() { return screenWidth; }
  /** @return the screenHeight */
  protected int getScreenHeight() { return screenHeight; }
  /** @return the anchorRect */
  protected Rect getAnchorRect() { return anchorRect; }

  protected void configureAnimationStyle() {
    final boolean above = isAbove;
    final int center = anchorRect.centerX();
    final int c4 = 4, quoter = screenWidth / c4;

    if (center <= quoter) {                             // from left
      if (DEBUG) { Log.d(TAG, "anim: from left"); }
      setAnimationStyle(above
          // from left bottom to right top
          ? R.style.QA_Animation_Above_FromLeft
          // from left top to right bottom
          : R.style.QA_Animation_Below_FromLeft);
    } else if (center >= 3 * quoter) {                  // from right
      if (DEBUG) { Log.d(TAG, "anim: from right"); }
      setAnimationStyle(above
          // from right bottom to left top
          ? R.style.QA_Animation_Above_FromRight
          // from right top to left bottom
          : R.style.QA_Animation_Below_FromRight);
    } else {                                            // center
      if (DEBUG) { Log.d(TAG, "anim: center"); }
      setAnimationStyle(above
          // from bottom to top
          ? R.style.QA_Animation_Above_Center
          // from top to bottom
          : R.style.QA_Animation_Below_Center);
    }
  }

  protected abstract void onRedrawActions();

  /**
   * Anchor region is already specified at this point.
   */
  protected void onPositionInitialization() {
    final View content = getContentView();
    content.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    content.measure(
        MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.AT_MOST),
        MeasureSpec.makeMeasureSpec(-1, MeasureSpec.UNSPECIFIED)
    );

    final boolean above = anchorRect.top > screenHeight - anchorRect.bottom; // top space is bigger than bottom
    final int myTop = above
        ? anchorRect.top - content.getMeasuredHeight() + arrowOffset
        : anchorRect.bottom - arrowOffset;

    setWidgetSpecs(myTop, above);
  }

  void actionsChanged() {
    if (isShowing()) {
      dispatchActionsChange();
    } else {
      actionsChanged = true;
    }
  }

  private void dispatchActionsChange() {
    onRedrawActions();
    actionsChanged = false;
  }

  /**
   * @param anchor an anchor for out QA widget
   */
  public void show(final View anchor) {
    final View view = getContentView();
    if (view == null) { throw new IllegalStateException("Content view is not defined"); }
    setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

    final int[] loc = locationBuffer;
    anchor.getLocationOnScreen(loc);
    anchorRect.set(loc[0], loc[1], loc[0] + anchor.getWidth(), loc[1] + anchor.getHeight());

    if (actionsChanged) { dispatchActionsChange(); }
    onPositionInitialization();
    if ((privateFlags & SPECS_DONE) == 0) {
      throw new IllegalStateException("onPositionInitialization didn't call setWidgetSpecs");
    }
    configureArrow();

    configureAnimationStyle();
    showAtLocation(anchor, Gravity.NO_GRAVITY, 0, top);
  }

  protected void setWidgetSpecs(final int top, final boolean isAbove) {
    this.top = top;
    this.isAbove = isAbove;

    privateFlags |= SPECS_DONE;
  }

  private void configureArrow() {
    final View view = getContentView();
    final int arrowId = isAbove ? R.id.qa_arrow_down : R.id.qa_arrow_up;
    final View arrow = view.findViewById(arrowId);
    final View arrowUp = view.findViewById(R.id.qa_arrow_up);
    final View arrowDown = view.findViewById(R.id.qa_arrow_down);

    if (isAbove) {
      if (arrowUp != null) { arrowUp.setVisibility(View.INVISIBLE); }
      if (arrowDown != null) { arrowDown.setVisibility(View.VISIBLE); }
    } else {
      if (arrowUp != null) { arrowUp.setVisibility(View.VISIBLE); }
      if (arrowDown != null) { arrowDown.setVisibility(View.INVISIBLE); }
    }

    if (arrow != null) {
      final ViewGroup.MarginLayoutParams param = (ViewGroup.MarginLayoutParams) arrow.getLayoutParams();
      param.leftMargin = anchorRect.centerX() - (arrow.getMeasuredWidth()) / 2;
    }
  }

  protected void prepareListenersForActionView(final View view) {
    view.setOnClickListener(clickListener);
  }

  protected View obtainView(final int position) {
    final View v = quickActionsAdapter.getActionView(position);
    prepareListenersForActionView(v);
    return v;
  }

  /**
   * Quick action click listener.
   * @author Roman Mazur (Stanfy - http://www.stanfy.com)
   */
  public static interface OnQuickActionClickListener {
    void onQuickActionClicked(QuickActionsWidget widget, int position);
  }

}

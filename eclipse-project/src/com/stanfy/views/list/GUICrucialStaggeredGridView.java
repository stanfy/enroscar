package com.stanfy.views.list;

import com.stanfy.views.AnimatedViewHelper;
import com.stanfy.views.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.widget.StaggeredGridView;
import android.util.AttributeSet;
import android.util.Log;

/**
 * {@link android.support.v4.widget.StaggeredGridView} with support for {@link com.stanfy.app.CrucialGUIOperationManager}.
 * @author Vladislav Lipskiy - Stanfy (http://www.stanfy.com)
 */
public class GUICrucialStaggeredGridView extends StaggeredGridView {

  /** Notify about crucial GUI operations. */
  private boolean notifyCrucialGUIOperations = true;

  /** Helper object. */
  private AnimatedViewHelper animatedViewHelper;

  /** Scroll listener that notifies application about crucial GUI operation. */
  private final OnScrollListener myScrollListener = new OnScrollListener() {
    @Override
    public void onScrollStateChanged(final StaggeredGridView view, final int scrollState) {
      if (animatedViewHelper != null) {
        if (scrollState == SCROLL_STATE_FLING) {
          animatedViewHelper.notifyCrucialGuiStart();

          final int first = getFirstPosition();
          final int count = getChildCount();
          if (first + count >= getAdapter().getCount()) {
            animatedViewHelper.notifyCrucialGuiFinish();
          }

        } else {
          animatedViewHelper.notifyCrucialGuiFinish();
        }
      }
      if (userScrollListener != null) {
        userScrollListener.onScrollStateChanged(view, scrollState);
      }
    }
    @Override
    public void onScroll(final StaggeredGridView view, final int firstVisibleItem, final int visibleItemCount,
        final int totalItemCount) {
      if (userScrollListener != null) {
        userScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
      }
    }
  };

  /** User's scroll listener. */
  private OnScrollListener userScrollListener;

  public GUICrucialStaggeredGridView(final Context context) {
    super(context);
  }

  public GUICrucialStaggeredGridView(final Context context, final AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  public GUICrucialStaggeredGridView(final Context context, final AttributeSet attrs,
      final int defStyle) {
    super(context, attrs, defStyle);
    init(context, attrs);
  }

  private void init(final Context context, final AttributeSet attrs) {
    final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GridView);
    final boolean notifyCrucial = a.getBoolean(R.styleable.GridView_notifyCrucialGUIOperations, true);
    a.recycle();
    setNotifyCrucialGUIOperations(notifyCrucial);
  }

  /** @param notifyCrucialGUIOperations the notifyCrucialGUIOperations to set */
  public void setNotifyCrucialGUIOperations(final boolean notifyCrucialGUIOperations) {
    if (isInEditMode()) { return; }
    if (animatedViewHelper != null && !notifyCrucialGUIOperations && this.notifyCrucialGUIOperations) {
      animatedViewHelper.notifyCrucialGuiFinish();
    }
    if (animatedViewHelper == null && notifyCrucialGUIOperations) {
      this.animatedViewHelper = new AnimatedViewHelper(this);
    }
    this.notifyCrucialGUIOperations = notifyCrucialGUIOperations;
    if (DEBUG) { Log.v(VIEW_LOG_TAG, "set notifyCrucialGUIOperations=" + notifyCrucialGUIOperations); }
    setupScrollListener();
  }

  @Override
  public void setOnScrollListener(final OnScrollListener l) {
    userScrollListener = l;
    setupScrollListener();
  }

  private void setupScrollListener() {
    if (notifyCrucialGUIOperations) {
      super.setOnScrollListener(myScrollListener);
    } else {
      super.setOnScrollListener(userScrollListener);
    }
  }

}

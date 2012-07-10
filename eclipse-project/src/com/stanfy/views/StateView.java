package com.stanfy.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ViewAnimator;

/**
 * Displays different views according to the current state.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public class StateView extends ViewAnimator {

  /** State helper. */
  private final StateHelper stateHelper = new StateHelper();

  /** Current state. */
  private int state;

  /** Lasy child index. */
  private int lastChildIndex;

  /** State. */
  private boolean statesSet = true, trimming = false;

  /** Internal animation listener. */
  private AnimationListener animationsListener = new AnimationListener() {
    @Override
    public void onAnimationStart(final Animation animation) {
      // nothing
    }
    @Override
    public void onAnimationRepeat(final Animation animation) {
      // nothing
    }
    @Override
    public void onAnimationEnd(final Animation animation) {
      trimViews();
    }
  };

  public StateView(final Context context) {
    super(context);
  }

  public StateView(final Context context, final AttributeSet attrs) {
    super(context, attrs);
  }

  private void checkAddChild() {
    if (statesSet && getChildCount() > 0) {
      throw new IllegalStateException("StateView can have one main view only");
    }
  }

  @Override
  public void addView(final View child) {
    checkAddChild();
    super.addView(child);
  }

  @Override
  public void addView(final View child, final int index) {
    checkAddChild();
    super.addView(child, index);
  }

  @Override
  public void addView(final View child, final ViewGroup.LayoutParams params) {
    checkAddChild();
    super.addView(child, params);
  }

  @Override
  public void addView(final View child, final int index, final ViewGroup.LayoutParams params) {
    checkAddChild();
    super.addView(child, index, params);
  }

  @Override
  public void setDisplayedChild(final int whichChild) {
    if (!trimming) {
      super.setDisplayedChild(whichChild);
    }
  }

  @Override
  public void setInAnimation(final Animation inAnimation) {
    if (inAnimation != null) {
      inAnimation.setAnimationListener(animationsListener);
    }
    super.setInAnimation(inAnimation);
  }

  @Override
  public void setOutAnimation(final Animation outAnimation) {
    if (outAnimation != null) {
      outAnimation.setAnimationListener(animationsListener);
    }
    super.setOutAnimation(outAnimation);
  }

  public void setState(final int state, final Object lastDataObject) {
    if (this.state == state) { return; }
    this.state = state;
    if (state == StateHelper.STATE_NORMAL) {
      lastChildIndex = 0;
      setDisplayedChild(0);
    } else {
      statesSet = false;
      addView(stateHelper.getCustomStateView(state, getContext(), lastDataObject, this));
      statesSet = true;
      lastChildIndex = getChildCount() - 1;
      setDisplayedChild(lastChildIndex);
    }
  }

  public void setLoadingState() {
    setState(StateHelper.STATE_LOADING, null);
  }
  public void setNormalState() {
    setState(StateHelper.STATE_NORMAL, null);
  }
  public void setMessageState(final Object lastDataObject) {
    setState(StateHelper.STATE_MESSAGE, lastDataObject);
  }
  public void setEmptyState(final Object lastDataObject) {
    setState(StateHelper.STATE_EMPTY, lastDataObject);
  }

  protected void trimViews() {
    int currentChild = lastChildIndex;
    trimming = true;
    while (getChildCount() > 2) {
      if (currentChild == 1) {
        removeViewAt(2);
      } else {
        if (currentChild > 0) { currentChild--; }
        removeViewAt(1);
      }
    }
    trimming = false;
    lastChildIndex = currentChild;
  }

}

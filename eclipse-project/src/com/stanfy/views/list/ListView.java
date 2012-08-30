package com.stanfy.views.list;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListAdapter;

import com.stanfy.DebugFlags;
import com.stanfy.views.AnimatedViewHelper;
import com.stanfy.views.R;

/**
 * List view that can support saving the scroll position.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class ListView extends android.widget.ListView {

  /** Debug flag. */
  private static final boolean DEBUG = DebugFlags.DEBUG_GUI;

  /** Not set position. */
  private static final int NOT_SET = -10;

  /** Froze scroll position flag. */
  private boolean frozeScrollPosition = false;

  /** Notify about crucial GUI operations. */
  private boolean notifyCrucialGUIOperations = true;

  /** Last restored position. */
  private int lastRestoredPosition = NOT_SET;
  /** Last restored relative position. */
  private float lastRestoredRelativePosition = NOT_SET;
  /** Position value. */
  private int savedPosition = NOT_SET;
  /** Y relative position of the first child. */
  private float savedRelativePosition = NOT_SET;

  /** First child. */
  private View firstChild;

  /** Helper object. */
  private AnimatedViewHelper animatedViewHelper;

  /** Scroll listener that notifies application about crucial GUI operation. */
  private final OnScrollListener myScrollListener = new OnScrollListener() {
    @Override
    public void onScrollStateChanged(final AbsListView view, final int scrollState) {
      if (animatedViewHelper != null) {
        if (scrollState == SCROLL_STATE_FLING) {
          animatedViewHelper.notifyCrucialGuiStart();

          final int first = getFirstVisiblePosition();
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
    public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount, final int totalItemCount) {
      if (userScrollListener != null) {
        userScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
      }
    }
  };

  /** User's scroll listener. */
  private OnScrollListener userScrollListener;

  public ListView(final Context context) {
    super(context);
  }

  public ListView(final Context context, final AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  public ListView(final Context context, final AttributeSet attrs, final int defStyle) {
    super(context, attrs, defStyle);
    init(context, attrs);
  }

  private void init(final Context context, final AttributeSet attrs) {
    final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ListView);
    final boolean frozeSPos = a.getBoolean(R.styleable.ListView_frozeScrollPosition, false);
    final boolean notifyCrucial = a.getBoolean(R.styleable.ListView_notifyCrucialGUIOperations, true);
    a.recycle();
    setFrozeScrollPosition(frozeSPos);
    setNotifyCrucialGUIOperations(notifyCrucial);
  }

  /** @param frozeScrollPosition the frozeScrollPosition to set */
  public void setFrozeScrollPosition(final boolean frozeScrollPosition) {
    this.frozeScrollPosition = frozeScrollPosition;
    if (DEBUG) { Log.v(VIEW_LOG_TAG, "set frozeScrollPosition=" + frozeScrollPosition); }
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

  @Override
  protected void onLayout(final boolean changed, final int l, final int t, final int r, final int b) {
    super.onLayout(changed, l, t, r, b);
    if (getChildCount() > 0) {
      firstChild = getChildAt(0);
    } else {
      firstChild = null;
    }
  }

  @Override
  public void setAdapter(final ListAdapter adapter) {
    super.setAdapter(adapter);

    final int sp = savedPosition;
    if (sp != NOT_SET && sp < adapter.getCount()) {
      doSetPosition(sp, savedRelativePosition);
      if (DEBUG) { Log.v(VIEW_LOG_TAG, "restore/setAdapter " + sp + ", " + savedRelativePosition); }
    }

    savedPosition = NOT_SET;
  }

  @Override
  public Parcelable onSaveInstanceState() {
    if (!frozeScrollPosition) { return super.onSaveInstanceState(); }
    final Parcelable parent = super.onSaveInstanceState();
    return new SavedState(parent, getSavedPosition(), getRelativeSavedPosition());
  }

  @Override
  public void onRestoreInstanceState(final Parcelable state) {
    if (!(state instanceof SavedState)) {
      super.onRestoreInstanceState(state);
      return;
    }

    final SavedState ss = (SavedState)state;
    super.onRestoreInstanceState(ss.getSuperState());

    setSavedPosition(ss.position, ss.relativePosition);
  }

  @Override
  protected void onDetachedFromWindow() {
    if (animatedViewHelper != null && notifyCrucialGUIOperations) {
      animatedViewHelper.onDetach();
    }
    super.onDetachedFromWindow();
  }

  private void setSavedPosition(final int sp, final float relativeSp) {
    if (sp != NOT_SET && getAdapter() != null) {
      doSetPosition(sp, relativeSp);
      if (DEBUG) { Log.d(VIEW_LOG_TAG, "restore " + sp + " " + this); }
      savedPosition = NOT_SET;
    } else {
      savedPosition = sp;
      savedRelativePosition = relativeSp;
    }
  }

  private int getSavedPosition() {
    final int pos = savedPosition != NOT_SET
        ? savedPosition
        : lastRestoredPosition != NOT_SET
          ? lastRestoredPosition
          : getFirstVisiblePosition();
    if (DEBUG) { Log.d(VIEW_LOG_TAG, "save position " + pos + " " + this); }
    return pos;
  }

  private View resolveFirstChild() {
    View child = getChildAt(0);
    if (child == null) {
      child = firstChild;
      if (DEBUG) { Log.v(VIEW_LOG_TAG, "Use first child from onLayout"); }
    }
    return child;
  }

  private float getRelativeSavedPosition() {
    if (DEBUG) {
      Log.v(VIEW_LOG_TAG, "savedRelativePosition: " + savedRelativePosition);
      Log.v(VIEW_LOG_TAG, "lastRestoredRelativePosition: " + lastRestoredRelativePosition);
    }
    if (savedRelativePosition != NOT_SET) { return savedRelativePosition; }
    if (lastRestoredRelativePosition != NOT_SET) { return lastRestoredRelativePosition; }
    if (DEBUG) { Log.v(VIEW_LOG_TAG, "Child count: " + getChildCount()); }
    final View child = resolveFirstChild();
    if (child == null) {
      if (DEBUG) { Log.w(VIEW_LOG_TAG, "Cannot get the first child"); }
      return 0;
    }
    final float height = child.getHeight();
    final float p = height > 0 ? child.getTop() / height : 0;
    if (DEBUG) { Log.v(VIEW_LOG_TAG, "Saved percent " + p); }
    return p;
  }

  private void doSetPosition(final int position, final float value) {
    lastRestoredPosition = position;
    lastRestoredRelativePosition = value;
    post(new Runnable() {
      @Override
      public void run() {
        lastRestoredPosition = NOT_SET;
        lastRestoredRelativePosition = NOT_SET;
        final View child = resolveFirstChild();
        final int h = child != null ? child.getHeight() : 0;
        setSelectionFromTop(position, (int)(value * h));
      }
    });
  }

  /**
   * Saved state for our list view.
   * @author Roman Mazur (Stanfy - http://www.stanfy.com)
   */
  static class SavedState extends BaseSavedState {

    /** Creator. */
    public static final Creator<SavedState> CREATOR = new Creator<ListView.SavedState>() {
      @Override
      public SavedState createFromParcel(final Parcel source) { return new SavedState(source); }
      @Override
      public SavedState[] newArray(final int size) { return new SavedState[size]; }
    };

    /** Saved value. */
    final int position;
    /** Saved relative value. */
    final float relativePosition;

    SavedState(final Parcelable parent, final int position, final float relativePosition) {
      super(parent);
      this.position = position;
      this.relativePosition = relativePosition;
    }

    private SavedState(final Parcel in) {
      super(in);
      position = in.readInt();
      relativePosition = in.readFloat();
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
      super.writeToParcel(dest, flags);
      dest.writeInt(position);
      dest.writeFloat(relativePosition);
    }

  }

}

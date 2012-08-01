package com.stanfy.views.gallery;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.widget.AbsListView.RecyclerListener;
import android.widget.SpinnerAdapter;

/**
 * Copy {@link android.widget.AbsSpinner} methods to make them available for us.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
@SuppressLint("FieldGetter")
abstract class AbsSpinner extends AdapterView<SpinnerAdapter> {

  /** Adapter instamce. */
  SpinnerAdapter mAdapter;

  /** Scrap view indicator. */
  final boolean[] mIsScrap = new boolean[1];

  /** Size. */
  int mHeightMeasureSpec, mWidthMeasureSpec;
  /** Block layout requests flag. */
  boolean mBlockLayoutRequests;

  /** Paddings. */
  int mSelectionLeftPadding = 0, mSelectionTopPadding = 0, mSelectionRightPadding = 0, mSelectionBottomPadding = 0;
  /** Paddings. */
  final Rect mSpinnerPadding = new Rect();

  /** Recycle bin. */
  final RecycleBin mRecycler = new RecycleBin();
  /** Data observer. */
  private DataSetObserver mDataSetObserver;

  /** Temporary frame to hold a child View's frame rectangle. */
  private Rect mTouchFrame;

  /** Clickable items flag. */
  boolean clickableItems;

  public AbsSpinner(final Context context) {
    super(context);
    initAbsSpinner();
  }

  public AbsSpinner(final Context context, final AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public AbsSpinner(final Context context, final AttributeSet attrs, final int defStyle) {
    super(context, attrs, defStyle);
    initAbsSpinner();
  }

  /**
   * Common code for different constructor flavors.
   */
  private void initAbsSpinner() {
    setFocusable(true);
    setWillNotDraw(false);
  }

  /**
   * The Adapter is used to provide the data which backs this Spinner.
   * It also provides methods to transform spinner items based on their position
   * relative to the selected item.
   * @param adapter The SpinnerAdapter to use for this Spinner
   */
  @Override
  public void setAdapter(final SpinnerAdapter adapter) {
    if (DEBUG) { Log.v(VIEW_LOG_TAG, "!!! set adapter !!!"); }
    if (null != mAdapter) {
      mAdapter.unregisterDataSetObserver(mDataSetObserver);
      resetList();
    }

    mAdapter = adapter;

    mOldSelectedPosition = INVALID_POSITION;
    mOldSelectedRowId = INVALID_ROW_ID;

    if (mAdapter != null) {
      mOldItemCount = mItemCount;
      mItemCount = mAdapter.getCount();
      checkFocus();

      mDataSetObserver = new AdapterDataSetObserver();
      mAdapter.registerDataSetObserver(mDataSetObserver);

      final int position = mItemCount > 0 ? 0 : INVALID_POSITION;

      setSelectedPositionInt(position);
      setNextSelectedPositionInt(position);

      clickableItems = (mAdapter instanceof ClickableItemsAdapter) && !((ClickableItemsAdapter)mAdapter).allItemsNotClickable();

      if (mItemCount == 0) {
        // Nothing selected
        checkSelectionChanged();
      }

    } else {
      clickableItems = false;
      checkFocus();
      resetList();
      // Nothing selected
      checkSelectionChanged();
    }

    requestLayout();
  }

  /**
   * Clear out all children from the list.
   */
  void resetList() {
    if (DEBUG) { Log.v(VIEW_LOG_TAG, "reset list"); }
    mDataChanged = false;
    mNeedSync = false;

    mRecycler.clear();

    removeAllViewsInLayout();
    mOldSelectedPosition = INVALID_POSITION;
    mOldSelectedRowId = INVALID_ROW_ID;

    setSelectedPositionInt(INVALID_POSITION);
    setNextSelectedPositionInt(INVALID_POSITION);
    invalidate();
  }

  /**
   * Get a view and have it show the data associated with the specified
   * position. This is called when we have already discovered that the view is
   * not available for reuse in the recycle bin. The only choices left are
   * converting an old view or making a new one.
   *
   * @param position The position to display
   * @param isScrap Array of at least 1 boolean, the first entry will become true if
   *                the returned view was taken from the scrap heap, false if otherwise.
   *
   * @return A view displaying the data associated with the specified position
   */
  @SuppressWarnings("deprecation")
  View obtainView(final int position, final boolean[] isScrap) {
    isScrap[0] = false;
    View scrapView;

    scrapView = mRecycler.getScrapView(position);
    if (DEBUG) { Log.v(VIEW_LOG_TAG, "obtainView Scrap view: " + scrapView + " for " + position); }

    View child;
    if (scrapView != null) {
      if (ViewDebug.TRACE_RECYCLER) {
        ViewDebug.trace(scrapView, ViewDebug.RecyclerTraceType.RECYCLE_FROM_SCRAP_HEAP, position, -1);
      }

      child = mAdapter.getView(position, scrapView, this);
      if (DEBUG) { Log.d(VIEW_LOG_TAG, "obtainView child: " + child); }

      if (ViewDebug.TRACE_RECYCLER) {
        ViewDebug.trace(child, ViewDebug.RecyclerTraceType.BIND_VIEW, position, getChildCount());
      }

      if (child != scrapView) {
        if (DEBUG) { Log.d(VIEW_LOG_TAG, "obtainView scrap view not used, return it to recycle"); }
        mRecycler.addScrapView(scrapView);
      } else {
        isScrap[0] = true;
        child.onFinishTemporaryDetach();
      }
    } else {
      child = mAdapter.getView(position, null, this);
      if (DEBUG) { Log.d(VIEW_LOG_TAG, "obtainView child is new: " + child); }
      if (ViewDebug.TRACE_RECYCLER) {
        ViewDebug.trace(child, ViewDebug.RecyclerTraceType.NEW_VIEW, position, getChildCount());
      }
    }

    return child;
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    mRecycler.clear();
  }

  @Override
  protected final void onLayout(final boolean changed, final int left, final int top, final int right, final int bottom) {
    super.onLayout(changed, left, top, right, bottom);
    mInLayout = true;
    if (changed) {
      final int childCount = getChildCount();
      for (int i = 0; i < childCount; i++) { getChildAt(i).forceLayout(); }
      mRecycler.markChildrenDirty();
    }

    layout(0, false);
    mInLayout = false;
  }

  /**
   * @see android.view.View#measure(int, int)
   *
   * Figure out the dimensions of this Spinner. The width comes from
   * the widthMeasureSpec as Spinnners can't have their width set to
   * UNSPECIFIED. The height is based on the height of the selected item
   * plus padding.
   */
  @Override
  protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
    if (DEBUG) { Log.v(VIEW_LOG_TAG, "==== onMeasure ===="); }
    final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
    int widthSize;
    int heightSize;

    final int mPaddingLeft = getPaddingLeft();
    final int mPaddingTop = getPaddingTop();
    final int mPaddingRight = getPaddingRight();
    final int mPaddingBottom = getPaddingBottom();
    mSpinnerPadding.left = mPaddingLeft > mSelectionLeftPadding ? mPaddingLeft
        : mSelectionLeftPadding;
    mSpinnerPadding.top = mPaddingTop > mSelectionTopPadding ? mPaddingTop
        : mSelectionTopPadding;
    mSpinnerPadding.right = mPaddingRight > mSelectionRightPadding ? mPaddingRight
        : mSelectionRightPadding;
    mSpinnerPadding.bottom = mPaddingBottom > mSelectionBottomPadding ? mPaddingBottom
        : mSelectionBottomPadding;

    if (mDataChanged) {
      handleDataChanged();
    }

    int preferredHeight = 0;
    int preferredWidth = 0;
    boolean needsMeasuring = true;

    final int selectedPosition = getSelectedItemPosition();
    if (DEBUG) { Log.v(VIEW_LOG_TAG, "selectedPosition=" + selectedPosition); }
    if (selectedPosition >= 0 && mAdapter != null && selectedPosition < mAdapter.getCount()) {
      final View view = obtainView(selectedPosition, mIsScrap);

      if (view != null) {
        // Put in recycler for re-measuring and/or layout
        //mOldRecycler.put(selectedPosition, view);
        // Return to scrap
        mRecycler.addScrapView(view);

        if (view.getLayoutParams() == null) {
          mBlockLayoutRequests = true;
          view.setLayoutParams(generateDefaultLayoutParams());
          mBlockLayoutRequests = false;
        }
        if (DEBUG) { Log.v(VIEW_LOG_TAG, "measureChild " + view); }
        measureChild(view, widthMeasureSpec, heightMeasureSpec);

        preferredHeight = getChildHeight(view) + mSpinnerPadding.top + mSpinnerPadding.bottom;
        preferredWidth = getChildWidth(view) + mSpinnerPadding.left + mSpinnerPadding.right;

        needsMeasuring = false;
      }
    }

    if (needsMeasuring) {
      // No views -- just use padding
      preferredHeight = mSpinnerPadding.top + mSpinnerPadding.bottom;
      if (widthMode == MeasureSpec.UNSPECIFIED) {
        preferredWidth = mSpinnerPadding.left + mSpinnerPadding.right;
      }
    }
    if (DEBUG) { Log.v(VIEW_LOG_TAG, "preferredHeight=" + preferredHeight + ", preferredWidth=" + preferredWidth); }

    preferredHeight = Math.max(preferredHeight, getSuggestedMinimumHeight());
    preferredWidth = Math.max(preferredWidth, getSuggestedMinimumWidth());
    if (DEBUG) { Log.v(VIEW_LOG_TAG, "saturate preferredHeight=" + preferredHeight + ", preferredWidth=" + preferredWidth); }

    heightSize = resolveSize(preferredHeight, heightMeasureSpec);
    widthSize = resolveSize(preferredWidth, widthMeasureSpec);
    if (DEBUG) { Log.v(VIEW_LOG_TAG, "heightSize=" + heightSize + ", widthSize=" + widthSize); }

    setMeasuredDimension(widthSize, heightSize);
    mHeightMeasureSpec = heightMeasureSpec;
    mWidthMeasureSpec = widthMeasureSpec;
    if (DEBUG) { Log.v(VIEW_LOG_TAG, "=========================measure"); }
  }

  int getChildHeight(final View child) {
    return child.getMeasuredHeight();
  }

  int getChildWidth(final View child) {
    return child.getMeasuredWidth();
  }

  @Override
  protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
    return new ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT);
  }

  /**
   * Jump directly to a specific item in the adapter data.
   */
  public void setSelection(final int position, final boolean animate) {
    // Animate only if requested position is already on screen somewhere
    final boolean shouldAnimate = animate && mFirstPosition <= position
    && position <= mFirstPosition + getChildCount() - 1;
    setSelectionInt(position, shouldAnimate);
  }

  @Override
  public void setSelection(final int position) {
    setNextSelectedPositionInt(position);
    requestLayout();
    invalidate();
  }


  /**
   * Makes the item at the supplied position selected.
   *
   * @param position Position to select
   * @param animate Should the transition be animated
   *
   */
  void setSelectionInt(final int position, final boolean animate) {
    if (position != mOldSelectedPosition) {
      mBlockLayoutRequests = true;
      final int delta  = position - mSelectedPosition;
      setNextSelectedPositionInt(position);
      layout(delta, animate);
      mBlockLayoutRequests = false;
    }
  }

  /**
   * Creates and positions all views for this view.
   * <p>
   * We layout rarely, most of the time {@link #trackMotionScroll(int)} takes
   * care of repositioning, adding, and removing children.
   *
   * @param delta Change in the selected position. +1 means the selection is
   *            moving to the right, so views are scrolling to the left. -1
   *            means the selection is moving to the left.
   */
  protected abstract void layout(int delta, boolean animate);

  @Override
  public View getSelectedView() {
    if (mItemCount > 0 && mSelectedPosition >= 0) {
      return getChildAt(mSelectedPosition - mFirstPosition);
    } else {
      return null;
    }
  }

  /**
   * Override to prevent spamming ourselves with layout requests
   * as we place views.
   *
   * @see android.view.View#requestLayout()
   */
  @Override
  public void requestLayout() {
    if (!mBlockLayoutRequests) {
      super.requestLayout();
    }
  }

  /** @param mBlockLayoutRequests the mBlockLayoutRequests to set */
  public void setBlockLayoutRequests(final boolean mBlockLayoutRequests) {
    this.mBlockLayoutRequests = mBlockLayoutRequests;
  }

  @Override
  public SpinnerAdapter getAdapter() {
    return mAdapter;
  }

  @Override
  public int getCount() {
    return mItemCount;
  }

  /**
   * Maps a point to a position in the list.
   *
   * @param x X in local coordinate
   * @param y Y in local coordinate
   * @return The position of the item which contains the specified point, or
   *         {@link #INVALID_POSITION} if the point does not intersect an item.
   */
  public int pointToPosition(final int x, final int y) {
    Rect frame = mTouchFrame;
    if (frame == null) {
      mTouchFrame = new Rect();
      frame = mTouchFrame;
    }

    final int count = getChildCount();
    for (int i = count - 1; i >= 0; i--) {
      final View child = getChildAt(i);
      if (child.getVisibility() == View.VISIBLE) {
        child.getHitRect(frame);
        if (frame.contains(x, y)) {
          return mFirstPosition + i;
        }
      }
    }
    return INVALID_POSITION;
  }

  /** Saved state. */
  static class SavedState extends BaseSavedState {
    /** Selected ID. */
    long selectedId;
    /** Position. */
    int position;

    /**
     * Constructor called from {@link AbsSpinner#onSaveInstanceState()}.
     */
    SavedState(final Parcelable superState) {
      super(superState);
    }

    /**
     * Constructor called from {@link #CREATOR}.
     */
    private SavedState(final Parcel in) {
      super(in);
      selectedId = in.readLong();
      position = in.readInt();
    }

    @Override
    public void writeToParcel(final Parcel out, final int flags) {
      super.writeToParcel(out, flags);
      out.writeLong(selectedId);
      out.writeInt(position);
    }

    @Override
    public String toString() {
      return "AbsSpinner.SavedState{"
      + Integer.toHexString(System.identityHashCode(this))
      + " selectedId=" + selectedId
      + " position=" + position + "}";
    }

    /** Creator. */
    public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
      @Override
      public SavedState createFromParcel(final Parcel in) {
        return new SavedState(in);
      }

      @Override
      public SavedState[] newArray(final int size) {
        return new SavedState[size];
      }
    };
  }

  @Override
  public Parcelable onSaveInstanceState() {
    final Parcelable superState = super.onSaveInstanceState();
    final SavedState ss = new SavedState(superState);
    ss.selectedId = getSelectedItemId();
    if (ss.selectedId >= 0) {
      ss.position = getSelectedItemPosition();
    } else {
      ss.position = INVALID_POSITION;
    }
    return ss;
  }

  @Override
  public void onRestoreInstanceState(final Parcelable state) {
    final SavedState ss = (SavedState) state;

    super.onRestoreInstanceState(ss.getSuperState());

    if (ss.selectedId >= 0) {
      mDataChanged = true;
      mNeedSync = true;
      mSyncRowId = ss.selectedId;
      mSyncPosition = ss.position;
      mSyncMode = SYNC_SELECTED_POSITION;
      requestLayout();
    }
  }

  /**
   * The RecycleBin facilitates reuse of views across layouts. The RecycleBin has two levels of
   * storage: ActiveViews and ScrapViews. ActiveViews are those views which were onscreen at the
   * start of a layout. By construction, they are displaying current information. At the end of
   * layout, all views in ActiveViews are demoted to ScrapViews. ScrapViews are old views that
   * could potentially be used by the adapter to avoid allocating views unnecessarily.
   *
   * @see android.widget.AbsListView#setRecyclerListener(android.widget.AbsListView.RecyclerListener)
   * @see android.widget.AbsListView.RecyclerListener
   */
  class RecycleBin {
    /** Listener. */
    private RecyclerListener mRecyclerListener;

    /**
     * The position of the first view stored in mActiveViews.
     */
    private int mFirstActivePosition;

    /**
     * Views that were on screen at the start of layout. This array is populated at the start of
     * layout, and at the end of layout all view in mActiveViews are moved to mScrapViews.
     * Views in mActiveViews represent a contiguous range of Views, with position of the first
     * view store in mFirstActivePosition.
     */
    private View[] mActiveViews = new View[0];

    /** Scrap views. */
    private final ArrayList<View> mCurrentScrap = new ArrayList<View>();

    public void markChildrenDirty() {
      final ArrayList<View> scrap = mCurrentScrap;
      final int scrapCount = scrap.size();
      for (int i = 0; i < scrapCount; i++) {
        scrap.get(i).forceLayout();
      }
    }

    /**
     * Clears the scrap heap.
     */
    void clear() {
      final ArrayList<View> scrap = mCurrentScrap;
      final int scrapCount = scrap.size();
      for (int i = 0; i < scrapCount; i++) {
        removeDetachedView(scrap.remove(scrapCount - 1 - i), false);
      }
    }

    /**
     * Fill ActiveViews with all of the children of the AbsListView.
     *
     * @param childCount The minimum number of views mActiveViews should hold
     * @param firstActivePosition The position of the first view that will be stored in
     *        mActiveViews
     */
    void fillActiveViews(final int childCount, final int firstActivePosition) {
      if (DEBUG) { Log.v(VIEW_LOG_TAG, "fillActiveViews(childCount=" + childCount + ", firstActivePosition=" + firstActivePosition + "), current length " + mActiveViews.length); }
      if (mActiveViews.length < childCount) {
        mActiveViews = new View[childCount];
        if (DEBUG) { Log.v(VIEW_LOG_TAG, "new active views[]"); }
      }
      mFirstActivePosition = firstActivePosition;

      final View[] activeViews = mActiveViews;
      for (int i = 0; i < childCount; i++) {
        final View child = getChildAt(i);
        final LayoutParams lp = child.getLayoutParams();
        if (lp != null) { activeViews[i] = child; }
      }
//      for (int i = activeViews.length - 1; i >= childCount; i--) {
//        View child = activeViews[i];
//        if (child != null) {
//          addScrapView(child);
//          activeViews[i] = null;
//        }
//      }
    }

    /**
     * Get the view corresponding to the specified position. The view will be removed from
     * mActiveViews if it is found.
     *
     * @param position The position to look up in mActiveViews
     * @return The view if it is found, null otherwise
     */
    View getActiveView(final int position) {
      final int index = position - mFirstActivePosition;
      final View[] activeViews = mActiveViews;
      if (index >= 0 && index < activeViews.length) {
        final View match = activeViews[index];
        activeViews[index] = null;
        return match;
      }
      return null;
    }

    /**
     * @return A view from the ScrapViews collection. These are unordered.
     */
    View getScrapView(final int position) {
      final ArrayList<View> scrapViews = mCurrentScrap;
      final int size = scrapViews.size();
      if (DEBUG) { Log.d(TAG, "scrap views size " + size); }
      if (size > 0) {
        return scrapViews.remove(size - 1);
      } else {
        return null;
      }
    }

    /**
     * Put a view into the ScapViews list. These views are unordered.
     * @param scrap The view to add
     */
    void addScrapView(final View scrap) {
      scrap.onStartTemporaryDetach();
      mCurrentScrap.add(scrap);
      if (DEBUG) { Log.d(VIEW_LOG_TAG, "Add scrap view to " + mCurrentScrap.size() + " " + scrap); }
      if (mRecyclerListener != null) {
        mRecyclerListener.onMovedToScrapHeap(scrap);
      }
    }

    /**
     * Move all views remaining in mActiveViews to mScrapViews.
     */
    @SuppressWarnings("deprecation")
    void scrapActiveViews() {
      final View[] activeViews = mActiveViews;
      final boolean hasListener = mRecyclerListener != null;

      final ArrayList<View> scrapViews = mCurrentScrap;
      final int count = activeViews.length;
      for (int i = count - 1; i >= 0; i--) {
        final View victim = activeViews[i];
        if (victim != null) {
          activeViews[i] = null;
          victim.onStartTemporaryDetach();
          scrapViews.add(victim);

          if (hasListener) {
            mRecyclerListener.onMovedToScrapHeap(victim);
          }

          if (ViewDebug.TRACE_RECYCLER) {
            ViewDebug.trace(victim,
                ViewDebug.RecyclerTraceType.MOVE_FROM_ACTIVE_TO_SCRAP_HEAP,
                mFirstActivePosition + i, -1);
          }
          if (DEBUG) {
            Log.v(VIEW_LOG_TAG, "scrapActiveViews " + (mFirstActivePosition + i));
          }
        }
      }

      pruneScrapViews();
    }

    /**
     * Makes sure that the size of mScrapViews does not exceed the size of mActiveViews.
     * (This can happen if an adapter does not recycle its views).
     */
    private void pruneScrapViews() {
      final int maxViews = mActiveViews.length;
      final ArrayList<View> scrapPile = mCurrentScrap;
      int size = scrapPile.size();
      final int extras = size - maxViews;
      size--;
      for (int j = 0; j < extras; j++) {
        removeDetachedView(scrapPile.remove(size--), false);
      }
    }

  }

}

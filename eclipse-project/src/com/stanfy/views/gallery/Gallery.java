/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stanfy.views.gallery;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Transformation;
import android.widget.Adapter;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Scroller;

import com.stanfy.views.AnimatedViewHelper;
import com.stanfy.views.R;

/**
 * A view that shows items in a center-locked, horizontally scrolling list.
 * <p>
 * The default values for the Gallery assume you will be using
 * {@link android.R.styleable#Theme_galleryItemBackground} as the background for
 * each View given to the Gallery from the Adapter. If you are not doing this,
 * you may need to adjust some Gallery properties, such as the spacing.
 * <p>
 * Views given to the Gallery should use {@link Gallery.LayoutParams} as their
 * layout parameters type.
 *
 * <p>See the <a href="{@docRoot}resources/tutorials/views/hello-gallery.html">Gallery
 * tutorial</a>.</p>
 *
 * <p>Attributes:</p>
 * <ul>
 *   <li>android.R.styleable#Gallery_animationDuration</li>
 *   <li>android.R.styleable#Gallery_spacing</li>
 *   <li>android.R.styleable#Gallery_gravity</li>
 * </ul>
 */
public class Gallery extends AbsSpinner implements GestureDetector.OnGestureListener {

  /**
   * Duration in milliseconds from the start of a scroll during which we're
   * unsure whether the user is scrolling or flinging.
   */
  private static final int SCROLL_TO_FLING_UNCERTAINTY_TIMEOUT = 250;

  /** Default animation duration. */
  private static final int DEFAULT_ANIM_DURATION = 400;

  /**
   * Horizontal spacing between items.
   */
  private int mSpacing = 0;

  /**
   * How long the transition animation should run when a child view changes
   * position, measured in milliseconds.
   */
  private int mAnimationDuration = DEFAULT_ANIM_DURATION;

  /**
   * The alpha of items that are not selected.
   */
  private float mUnselectedAlpha;

  /**
   * Left most edge of a child seen so far during layout.
   */
  private int mLeftMost;

  /**
   * Right most edge of a child seen so far during layout.
   */
  private int mRightMost;

  /** Items gravity. */
  private int mGravity;

  /**
   * Helper for detecting touch gestures.
   */
  private GestureDetector mGestureDetector;

  /**
   * The position of the item that received the user's down touch.
   */
  private int mDownTouchPosition;

  /**
   * The view of the item that received the user's down touch.
   */
  private View mDownTouchView;

  /**
   * Executes the delta scrolls from a fling or scroll movement.
   */
  private FlingRunnable mFlingRunnable = new FlingRunnable();

  /**
   * Sets mSuppressSelectionChanged = false. This is used to set it to false
   * in the future. It will also trigger a selection changed.
   */
  private Runnable mDisableSuppressSelectionChangedRunnable = new Runnable() {
    @Override
    public void run() {
      mSuppressSelectionChanged = false;
      selectionChanged();
    }
  };

  /**
   * When fling runnable runs, it resets this to false. Any method along the
   * path until the end of its run() can set this to true to abort any
   * remaining fling. For example, if we've reached either the leftmost or
   * rightmost item, we will set this to true.
   */
  private boolean mShouldStopFling;

  /**
   * The currently selected item's child.
   */
  private View mSelectedChild;

  /**
   * Whether to continuously callback on the item selected listener during a
   * fling.
   */
  private boolean mShouldCallbackDuringFling = true;

  /**
   * Whether to callback when an item that is not selected is clicked.
   */
  private boolean mShouldCallbackOnUnselectedItemClick = true;

  /**
   * If true, do not callback to item selected listener.
   */
  private boolean mSuppressSelectionChanged;

  /**
   * If true, we have received the "invoke" (center or enter buttons) key
   * down. This is checked before we action on the "invoke" key up, and is
   * subsequently cleared.
   */
  private boolean mReceivedInvokeKeyDown;

  /** Context menu info. */
  private AdapterContextMenuInfo mContextMenuInfo;

  /**
   * If true, this onScroll is the first for this user's drag (remember, a
   * drag sends many onScrolls).
   */
  private boolean mIsFirstScroll;

  /** Notify about crucial GUI operations. */
  private boolean notifyCrucialGUIOperations = true;

  /** Helper object. */
  private AnimatedViewHelper animatedViewHelper;

  public Gallery(final Context context) {
    this(context, null);
  }

  public Gallery(final Context context, final AttributeSet attrs) {
    this(context, attrs, android.R.attr.galleryStyle);
  }

  public Gallery(final Context context, final AttributeSet attrs, final int defStyle) {
    super(context, attrs, defStyle);

    mGestureDetector = new GestureDetector(context, this);
    mGestureDetector.setIsLongpressEnabled(true);

    final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Gallery, defStyle, 0);

    final int index = a.getInt(R.styleable.Gallery_android_gravity, -1);
    if (index >= 0) { setGravity(index); }

    final int animationDuration = a.getInt(R.styleable.Gallery_android_animationDuration, -1);
    if (animationDuration > 0) { setAnimationDuration(animationDuration); }

    final int spacing = a.getDimensionPixelOffset(R.styleable.Gallery_android_spacing, 0);
    setSpacing(spacing);

    final float half = 0.5f;
    final float unselectedAlpha = a.getFloat(R.styleable.Gallery_android_unselectedAlpha, half);
    setUnselectedAlpha(unselectedAlpha);


    final boolean notifyCrucial = a.getBoolean(R.styleable.Gallery_notifyCrucialGUIOperations, true);
    setNotifyCrucialGUIOperations(notifyCrucial);

    a.recycle();

    // We draw the selected item last (because otherwise the item to the right overlaps it)
    setChildrenDrawingOrderEnabled(true);
    setStaticTransformationsEnabled(false);
  }

  /** @param notifyCrucialGUIOperations the notifyCrucialGUIOperations to set */
  public void setNotifyCrucialGUIOperations(final boolean notifyCrucialGUIOperations) {
    if (animatedViewHelper != null && !notifyCrucialGUIOperations && this.notifyCrucialGUIOperations) {
      animatedViewHelper.notifyCrucialGuiFinish();
    }
    if (animatedViewHelper == null && notifyCrucialGUIOperations) {
      animatedViewHelper = new AnimatedViewHelper(this);
    }
    this.notifyCrucialGUIOperations = notifyCrucialGUIOperations;
    if (DEBUG) { Log.v(VIEW_LOG_TAG, "set notifyCrucialGUIOperations=" + notifyCrucialGUIOperations); }
  }

  @Override
  public void setStaticTransformationsEnabled(final boolean enabled) {
    super.setStaticTransformationsEnabled(enabled);
    if (DEBUG) { Log.d(TAG, "Set static transformation enabled: " + enabled); }
  }

  /**
   * Whether or not to callback on any {@link #getOnItemSelectedListener()}
   * while the items are being flinged. If false, only the final selected item
   * will cause the callback. If true, all items between the first and the
   * final will cause callbacks.
   *
   * @param shouldCallback Whether or not to callback on the listener while
   *            the items are being flinged.
   */
  public void setCallbackDuringFling(final boolean shouldCallback) {
    mShouldCallbackDuringFling = shouldCallback;
  }

  /**
   * Whether or not to callback when an item that is not selected is clicked.
   * If false, the item will become selected (and re-centered). If true, the
   * {@link #getOnItemClickListener()} will get the callback.
   *
   * @param shouldCallback Whether or not to callback on the listener when a
   *            item that is not selected is clicked.
   * @hide
   */
  public void setCallbackOnUnselectedItemClick(final boolean shouldCallback) {
    mShouldCallbackOnUnselectedItemClick = shouldCallback;
  }

  /**
   * Sets how long the transition animation should run when a child view
   * changes position. Only relevant if animation is turned on.
   *
   * @param animationDurationMillis The duration of the transition, in
   *        milliseconds.
   *
   * @attr ref android.R.styleable#Gallery_animationDuration
   */
  public void setAnimationDuration(final int animationDurationMillis) {
    mAnimationDuration = animationDurationMillis;
  }

  /**
   * Sets the spacing between items in a Gallery.
   *
   * @param spacing The spacing in pixels between items in the Gallery
   *
   * @attr ref android.R.styleable#Gallery_spacing
   */
  public void setSpacing(final int spacing) {
    mSpacing = spacing;
  }

  /**
   * Sets the alpha of items that are not selected in the Gallery.
   *
   * @param unselectedAlpha the alpha for the items that are not selected.
   *
   * @attr ref android.R.styleable#Gallery_unselectedAlpha
   */
  public void setUnselectedAlpha(final float unselectedAlpha) {
    mUnselectedAlpha = unselectedAlpha;
  }

  @Override
  protected boolean getChildStaticTransformation(final View child, final Transformation t) {

    t.clear();
    t.setAlpha(child == mSelectedChild ? 1.0f : mUnselectedAlpha);

    return true;
  }

  @Override
  protected int computeHorizontalScrollExtent() {
    // Only 1 item is considered to be selected
    return 1;
  }

  @Override
  protected int computeHorizontalScrollOffset() {
    // Current scroll position is the same as the selected position
    return mSelectedPosition;
  }

  @Override
  protected int computeHorizontalScrollRange() {
    // Scroll range is the same as the item count
    return mItemCount;
  }

  @Override
  protected boolean checkLayoutParams(final ViewGroup.LayoutParams p) {
    return p instanceof LayoutParams;
  }

  @Override
  protected ViewGroup.LayoutParams generateLayoutParams(final ViewGroup.LayoutParams p) {
    return new LayoutParams(p);
  }

  @Override
  public ViewGroup.LayoutParams generateLayoutParams(final AttributeSet attrs) {
    return new LayoutParams(getContext(), attrs);
  }

  @Override
  protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
    /*
     * Gallery expects Gallery.LayoutParams.
     */
    return new Gallery.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT);
  }

  @Override
  int getChildHeight(final View child) { return child.getMeasuredHeight(); }

  /**
   * Tracks a motion scroll. In reality, this is used to do just about any
   * movement to items (touch scroll, arrow-key scroll, set an item as selected).
   *
   * @param deltaX Change in X from the previous event.
   */
  void trackMotionScroll(final int deltaX) {
    if (DEBUG) { Log.v(VIEW_LOG_TAG, "trackMotionScroll deltaX=" + deltaX); }

    if (getChildCount() == 0) {
      return;
    }

    final boolean toLeft = deltaX < 0;

    final int limitedDeltaX = getLimitedMotionScrollAmount(toLeft, deltaX);
    if (limitedDeltaX != deltaX) {
      // The above call returned a limited amount, so stop any scrolls/flings
      mFlingRunnable.endFling(false);
      onFinishedMovement();
    }

    offsetChildrenLeftAndRight(limitedDeltaX);

    detachOffScreenChildren(toLeft);

    if (toLeft) {
      // If moved left, there will be empty space on the right
      fillToGalleryRight();
    } else {
      // Similarly, empty space on the left
      fillToGalleryLeft();
    }

    setSelectionToCenterChild();

    invalidate();
  }

  protected int getLimitedMotionScrollAmount(final boolean motionToLeft, final int deltaX) {
    final int extremeItemPosition = (motionToLeft ? mItemCount - 1 : 0) - mFirstPosition;
    View extremeChild = null;
    if (extremeItemPosition >= 0 && extremeItemPosition < getChildCount()) {
      extremeChild = getChildAt(extremeItemPosition);
    }

    if (extremeChild == null) {
      return deltaX;
    }

    final int extremeChildCenter = getCenterOfView(extremeChild);
    final int galleryCenter = getCenterOfGallery();

    if (motionToLeft) {
      if (extremeChildCenter <= galleryCenter) {

        // The extreme child is past his boundary point!
        return 0;
      }
    } else {
      if (extremeChildCenter >= galleryCenter) {

        // The extreme child is past his boundary point!
        return 0;
      }
    }

    final int centerDifference = galleryCenter - extremeChildCenter;

    return motionToLeft
    ? Math.max(centerDifference, deltaX)
        : Math.min(centerDifference, deltaX);
  }

  /**
   * Offset the horizontal location of all children of this view by the
   * specified number of pixels.
   *
   * @param offset the number of pixels to offset
   */
  private void offsetChildrenLeftAndRight(final int offset) {
    for (int i = getChildCount() - 1; i >= 0; i--) {
      getChildAt(i).offsetLeftAndRight(offset);
    }
  }

  /**
   * @return The center of this Gallery.
   */
  protected int getCenterOfGallery() {
    return (getWidth() - getPaddingLeft() - getPaddingRight()) / 2 + getPaddingLeft();
  }

  /**
   * @return The center of the given view.
   */
  protected static int getCenterOfView(final View view) { return view.getLeft() + view.getWidth() / 2; }

  /**
   * Detaches children that are off the screen (i.e.: Gallery bounds).
   *
   * @param toLeft Whether to detach children to the left of the Gallery, or
   *            to the right.
   */
  private void detachOffScreenChildren(final boolean toLeft) {
    final int numChildren = getChildCount();
    if (numChildren <= 3) { return; }
    int start = 0;
    int count = 0;

    final RecycleBin recycleBin = mRecycler;

    if (toLeft) {
      final int galleryLeft = getPaddingLeft();
      for (int i = 0; i < numChildren; i++) {
        final View child = getChildAt(i);
        if (child.getRight() >= galleryLeft) {
          break;
        } else {
          count++;
          recycleBin.addScrapView(child);
        }
      }
    } else {
      final int galleryRight = getWidth() - getPaddingRight();
      for (int i = numChildren - 1; i >= 0; i--) {
        final View child = getChildAt(i);
        if (child.getLeft() <= galleryRight) {
          break;
        } else {
          start = i;
          count++;
          recycleBin.addScrapView(child);
        }
      }
    }

    detachViewsFromParent(start, count);

    if (toLeft) {
      mFirstPosition += count;
    }
  }

  /**
   * Scrolls the items so that the selected item is in its 'slot' (its center
   * is the gallery's center).
   */
  private void scrollIntoSlots() {

    if (getChildCount() == 0 || mSelectedChild == null) { return; }

    final int selectedCenter = getCenterOfView(mSelectedChild);
    final int targetCenter = getCenterOfGallery();

    final int scrollAmount = targetCenter - selectedCenter;
    if (scrollAmount != 0) {
      mFlingRunnable.startUsingDistance(scrollAmount);
    } else {
      onFinishedMovement();
    }
  }

  private void onFinishedMovement() {
    if (mSuppressSelectionChanged) {
      mSuppressSelectionChanged = false;

      // We haven't been callbacking during the fling, so do it now
      super.selectionChanged();
    }
    invalidate();
  }

  @Override
  void selectionChanged() {
    if (!mSuppressSelectionChanged) {
      super.selectionChanged();
    }
  }

  /**
   * Looks for the child that is closest to the center and sets it as the
   * selected child.
   */
  protected void setSelectionToCenterChild() {

    final View selView = mSelectedChild;
    if (mSelectedChild == null) { return; }

    final int galleryCenter = getCenterOfGallery();

    // Common case where the current selected position is correct
    if (selView.getLeft() <= galleryCenter && selView.getRight() >= galleryCenter) {
      return;
    }

    // TODO better search
    int closestEdgeDistance = Integer.MAX_VALUE;
    int newSelectedChildIndex = 0;
    for (int i = getChildCount() - 1; i >= 0; i--) {

      final View child = getChildAt(i);

      if (child.getLeft() <= galleryCenter && child.getRight() >=  galleryCenter) {
        // This child is in the center
        newSelectedChildIndex = i;
        break;
      }

      final int childClosestEdgeDistance = Math.min(Math.abs(child.getLeft() - galleryCenter),
          Math.abs(child.getRight() - galleryCenter));
      if (childClosestEdgeDistance < closestEdgeDistance) {
        closestEdgeDistance = childClosestEdgeDistance;
        newSelectedChildIndex = i;
      }
    }

    final int newPos = mFirstPosition + newSelectedChildIndex;

    if (newPos != mSelectedPosition) {
      setSelectedPositionInt(newPos);
      setNextSelectedPositionInt(newPos);
      checkSelectionChanged();
    }
  }

  @Override
  protected void layout(final int delta, final boolean animate) {
    if (DEBUG) { Log.v(VIEW_LOG_TAG, "==== Gallery layout ===="); }

    final int childrenLeft = mSpinnerPadding.left;
    final int childrenWidth = getRight() - getLeft() - mSpinnerPadding.left - mSpinnerPadding.right;
    if (DEBUG) { Log.v(VIEW_LOG_TAG, "childrenLeft=" + childrenLeft + ", childrenWidth=" + childrenWidth); }

    if (mDataChanged) {
      handleDataChanged();
    } else {
      mRecycler.fillActiveViews(getChildCount(), mFirstPosition);
    }

    // Handle an empty gallery by removing all views.
    if (mItemCount == 0) {
      resetList();
      if (DEBUG) { Log.v(VIEW_LOG_TAG, "============================"); }
      return;
    }

    // Update to the new selected position.
    if (mNextSelectedPosition >= 0) {
      if (DEBUG) { Log.v(VIEW_LOG_TAG, "setSelectedPositionInt(mNextSelectedPosition=" + mNextSelectedPosition + ")"); }
      setSelectedPositionInt(mNextSelectedPosition);
    }

    // Clear out old views removeAllViewsInLayout();
    detachAllViewsFromParent();

    /*
     * These will be used to give initial positions to views entering the
     * gallery as we scroll
     */
    mRightMost = 0;
    mLeftMost = 0;

    // Make selected view and center it

    /*
     * mFirstPosition will be decreased as we add views to the left later
     * on. The 0 for x will be offset in a couple lines down.
     */
    mFirstPosition = mSelectedPosition;
    final View sel = makeAndAddView(mSelectedPosition, 0, 0, true);
    if (DEBUG) { Log.v(VIEW_LOG_TAG, "selected view " + sel); }

    // Put the selected child in the center
    final int selectedOffset = childrenLeft + (childrenWidth / 2) - (sel.getWidth() / 2);
    if (DEBUG) { Log.v(VIEW_LOG_TAG, "selectedOffset=" + selectedOffset); }
    sel.offsetLeftAndRight(selectedOffset);

    fillToGalleryRight();
    fillToGalleryLeft();

    // Flush any cached views that did not get reused above
    mRecycler.scrapActiveViews();

    invalidate();
    checkSelectionChanged();

    mDataChanged = false;
    mNeedSync = false;
    setNextSelectedPositionInt(mSelectedPosition);

    updateSelectedItemMetadata();
    if (DEBUG) { Log.v(VIEW_LOG_TAG, "============================"); }
  }

  private void fillToGalleryLeft() {
    final int itemSpacing = mSpacing;
    final int galleryLeft = getPaddingLeft();

    // Set state for initial iteration
    View prevIterationView = getChildAt(0);
    int curPosition;
    int curRightEdge;

    if (prevIterationView != null) {
      curPosition = mFirstPosition - 1;
      curRightEdge = prevIterationView.getLeft() - itemSpacing;
    } else {
      // No children available!
      curPosition = 0;
      curRightEdge = getRight() - getLeft() - getPaddingRight();
      mShouldStopFling = true;
    }

    while (curRightEdge > galleryLeft && curPosition >= 0) {
      prevIterationView = makeAndAddView(curPosition, curPosition - mSelectedPosition,
          curRightEdge, false);

      // Remember some state
      mFirstPosition = curPosition;

      // Set state for next iteration
      curRightEdge = prevIterationView.getLeft() - itemSpacing;
      curPosition--;
    }
  }

  private void fillToGalleryRight() {
    final int itemSpacing = mSpacing;
    final int galleryRight = getRight() - getLeft() - getPaddingRight();
    final int numChildren = getChildCount();
    final int numItems = mItemCount;

    // Set state for initial iteration
    View prevIterationView = getChildAt(numChildren - 1);
    int curPosition;
    int curLeftEdge;

    if (prevIterationView != null) {
      curPosition = mFirstPosition + numChildren;
      curLeftEdge = prevIterationView.getRight() + itemSpacing;
    } else {
      curPosition = mItemCount - 1;
      mFirstPosition = curPosition;
      curLeftEdge = getPaddingLeft();
      mShouldStopFling = true;
    }

    while (curLeftEdge < galleryRight && curPosition < numItems) {
      prevIterationView = makeAndAddView(curPosition, curPosition - mSelectedPosition,
          curLeftEdge, true);

      // Set state for next iteration
      curLeftEdge = prevIterationView.getRight() + itemSpacing;
      curPosition++;
    }
  }

  /**
   * Obtain a view, either by pulling an existing view from the recycler or by
   * getting a new one from the adapter. If we are animating, make sure there
   * is enough information in the view's layout parameters to animate from the
   * old to new positions.
   *
   * @param position Position in the gallery for the view to obtain
   * @param offset Offset from the selected position
   * @param x X-coordintate indicating where this view should be placed. This
   *        will either be the left or right edge of the view, depending on
   *        the fromLeft paramter
   * @param fromLeft Are we posiitoning views based on the left edge? (i.e.,
   *        building from left to right)?
   * @return A view that has been added to the gallery
   */
  private View makeAndAddView(final int position, final int offset, final int x,
      final boolean fromLeft) {
    if (DEBUG) { Log.v(VIEW_LOG_TAG, "makeAndAddView(position=" + position + ",offset=" + offset + ",x=" + x + ", fromLeft=" + fromLeft); }
    View child;

    if (!mDataChanged) {
      child = mRecycler.getActiveView(position);
      if (child != null) {
        // Can reuse an existing view
        final int childLeft = child.getLeft();

        // Remember left and right edges of where views have been placed
        mRightMost = Math.max(mRightMost, childLeft
            + child.getMeasuredWidth());
        mLeftMost = Math.min(mLeftMost, childLeft);

        // Position the view
        setUpChild(child, offset, x, fromLeft);
        return child;
      }
    }

    // Nothing found in the recycler -- ask the adapter for a view
    mBlockLayoutRequests = true;
    child = obtainView(position, mIsScrap);
    mBlockLayoutRequests = false;

    if (mIsScrap[0]) {
      // Can reuse an existing view
      final int childLeft = child.getLeft();

      // Remember left and right edges of where views have been placed
      mRightMost = Math.max(mRightMost, childLeft
          + child.getMeasuredWidth());
      mLeftMost = Math.min(mLeftMost, childLeft);
    }

    // Position the view
    setUpChild(child, offset, x, fromLeft);

    return child;
  }

  /**
   * Helper for makeAndAddView to set the position of a view and fill out its
   * layout paramters.
   *
   * @param child The view to position
   * @param offset Offset from the selected position
   * @param x X-coordintate indicating where this view should be placed. This
   *        will either be the left or right edge of the view, depending on
   *        the fromLeft paramter
   * @param fromLeft Are we posiitoning views based on the left edge? (i.e.,
   *        building from left to right)?
   */
  private void setUpChild(final View child, final int offset, final int x, final boolean fromLeft) {

    // Respect layout params that are already in the view. Otherwise
    // make some up...
    Gallery.LayoutParams lp = (Gallery.LayoutParams)
    child.getLayoutParams();
    if (lp == null) {
      lp = (Gallery.LayoutParams) generateDefaultLayoutParams();
    }

    addViewInLayout(child, fromLeft ? -1 : 0, lp);

    child.setSelected(offset == 0);

    // Get measure specs
    final int childHeightSpec = ViewGroup.getChildMeasureSpec(mHeightMeasureSpec,
        mSpinnerPadding.top + mSpinnerPadding.bottom, lp.height);
    final int childWidthSpec = ViewGroup.getChildMeasureSpec(mWidthMeasureSpec,
        mSpinnerPadding.left + mSpinnerPadding.right, lp.width);

    // Measure child
    child.measure(childWidthSpec, childHeightSpec);

    int childLeft;
    int childRight;

    // Position vertically based on gravity setting
    final int childTop = calculateTop(child, true);
    final int childBottom = childTop + child.getMeasuredHeight();

    final int width = child.getMeasuredWidth();
    if (fromLeft) {
      childLeft = x;
      childRight = childLeft + width;
    } else {
      childLeft = x - width;
      childRight = x;
    }

    child.layout(childLeft, childTop, childRight, childBottom);
  }

  /**
   * Figure out vertical placement based on mGravity.
   * @param child Child to place
   * @return Where the top of the child should be
   */
  private int calculateTop(final View child, final boolean duringLayout) {
    final int myHeight = duringLayout ? getMeasuredHeight() : getHeight();
    final int childHeight = duringLayout ? child.getMeasuredHeight() : child.getHeight();

    int childTop = 0;

    switch (mGravity) {
    case Gravity.TOP:
      childTop = mSpinnerPadding.top;
      break;
    case Gravity.CENTER_VERTICAL:
      final int availableSpace = myHeight - mSpinnerPadding.bottom
      - mSpinnerPadding.top - childHeight;
      childTop = mSpinnerPadding.top + (availableSpace / 2);
      break;
    case Gravity.BOTTOM:
      childTop = myHeight - mSpinnerPadding.bottom - childHeight;
      break;
    default:
    }
    return childTop;
  }

  @Override
  public boolean onInterceptTouchEvent(final MotionEvent ev) { return true; }

  @Override
  public boolean onTouchEvent(final MotionEvent event) {

    // Give everything to the gesture detector
    final boolean retValue = mGestureDetector.onTouchEvent(event);

    final int action = event.getAction();
    if (action == MotionEvent.ACTION_UP) {
      // Helper method for lifted finger
      onUp();
    } else if (action == MotionEvent.ACTION_CANCEL) {
      onCancel(event);
    }

    return retValue;
  }

  private static void relocateChildEvent(final MotionEvent e, final View child) {
    if (e == null) { return; }
    final int scrolledX = (int)(e.getX() + child.getScrollX());
    final int scrolledY = (int)(e.getY() + child.getScrollY());
    final float xc = scrolledX - child.getLeft();
    final float yc = scrolledY - child.getTop();
    e.setLocation(xc, yc);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean onSingleTapUp(final MotionEvent e) {

    if (mDownTouchPosition >= 0) {
      // An item tap should make it selected, so scroll to this child.
      scrollToChild(mDownTouchPosition - mFirstPosition);

      // Also pass the click so the client knows, if it wants to.
      if (mShouldCallbackOnUnselectedItemClick || mDownTouchPosition == mSelectedPosition) {
        if (clickableItems) {
          final View child = mDownTouchView;
          relocateChildEvent(e, child);
          child.dispatchTouchEvent(e);
        } else {
          performItemClick(mDownTouchView, mDownTouchPosition, mAdapter
              .getItemId(mDownTouchPosition));
        }
      }

      return true;
    }

    return false;
  }

  private void checkAndSendCancelToChild(final MotionEvent e1) {
    if (mDownTouchPosition >= 0 && clickableItems) {
      final View child = mDownTouchView == null ? getChildAt(mDownTouchPosition) : mDownTouchView;
      relocateChildEvent(e1, child);
      e1.setAction(MotionEvent.ACTION_CANCEL);
      child.dispatchTouchEvent(e1);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean onFling(final MotionEvent e1, final MotionEvent e2, final float velocityX, final float velocityY) {

    checkAndSendCancelToChild(e1);

    if (!mShouldCallbackDuringFling) {
      // We want to suppress selection changes

      // Remove any future code to set mSuppressSelectionChanged = false
      removeCallbacks(mDisableSuppressSelectionChangedRunnable);

      // This will get reset once we scroll into slots
      if (!mSuppressSelectionChanged) { mSuppressSelectionChanged = true; }
    }

    // Fling the gallery!
    mFlingRunnable.startUsingVelocity((int) -velocityX);

    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean onScroll(final MotionEvent e1, final MotionEvent e2, final float distanceX, final float distanceY) {

    if (DEBUG) { Log.v(TAG, String.valueOf(e2.getX() - e1.getX())); }

    checkAndSendCancelToChild(e1);

    /*
     * Now's a good time to tell our parent to stop intercepting our events!
     * The user has moved more than the slop amount, since GestureDetector
     * ensures this before calling this method. Also, if a parent is more
     * interested in this touch's events than we are, it would have
     * intercepted them by now (for example, we can assume when a Gallery is
     * in the ListView, a vertical scroll would not end up in this method
     * since a ListView would have intercepted it by now).
     */
    getParent().requestDisallowInterceptTouchEvent(true);

    // As the user scrolls, we want to callback selection changes so related-
    // info on the screen is up-to-date with the gallery's selection
    if (!mShouldCallbackDuringFling) {
      if (mIsFirstScroll) {
        /*
         * We're not notifying the client of selection changes during
         * the fling, and this scroll could possibly be a fling. Don't
         * do selection changes until we're sure it is not a fling.
         */
        if (!mSuppressSelectionChanged) { mSuppressSelectionChanged = true; }
        postDelayed(mDisableSuppressSelectionChangedRunnable, SCROLL_TO_FLING_UNCERTAINTY_TIMEOUT);
      }
    } else {
      if (mSuppressSelectionChanged) { mSuppressSelectionChanged = false; }
    }

    // Track the motion
    trackMotionScroll(-1 * (int) distanceX);

    mIsFirstScroll = false;
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean onDown(final MotionEvent e) {

    // Kill any existing fling/scroll
    mFlingRunnable.stop(false);

    // Get the item's view that was touched
    mDownTouchPosition = pointToPosition((int) e.getX(), (int) e.getY());

    if (mDownTouchPosition >= 0) {
      mDownTouchView = getChildAt(mDownTouchPosition - mFirstPosition);
      if (!clickableItems) {
        mDownTouchView.setPressed(true);
      } else {
        relocateChildEvent(e, mDownTouchView);
        mDownTouchView.dispatchTouchEvent(e);
      }
    }

    // Reset the multiple-scroll tracking state
    mIsFirstScroll = true;

    // Must return true to get matching events for this down event.
    return true;
  }

  /**
   * Called when a touch event's action is MotionEvent.ACTION_UP.
   */
  void onUp() {

    if (DEBUG) { Log.v(TAG, "On up!"); }

    if (mFlingRunnable.mScroller.isFinished()) {
      scrollIntoSlots();
    }

    dispatchUnpress();
  }

  /**
   * Called when a touch event's action is MotionEvent.ACTION_CANCEL.
   * @param e event
   */
  void onCancel(final MotionEvent e) {
    if (DEBUG) { Log.v(TAG, "On cancel!"); }
    if (clickableItems && mDownTouchPosition >= 0 && (mShouldCallbackOnUnselectedItemClick || mDownTouchPosition == mSelectedPosition)) {
      final View child = mDownTouchView;
      if (child != null) {
        relocateChildEvent(e, child);
        child.dispatchTouchEvent(e);
      }
    }
    onUp();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onLongPress(final MotionEvent e) {

    if (mDownTouchPosition < 0) {
      return;
    }

    performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
    final long id = getItemIdAtPosition(mDownTouchPosition);
    dispatchLongPress(mDownTouchView, mDownTouchPosition, id);
  }

  // Unused methods from GestureDetector.OnGestureListener below

  /**
   * {@inheritDoc}
   */
  @Override
  public void onShowPress(final MotionEvent e) {
  }

  // Unused methods from GestureDetector.OnGestureListener above

  private void dispatchPress(final View child) {

    if (child != null && !clickableItems) {
      child.setPressed(true);
    }

    setPressed(true);
  }

  private void dispatchUnpress() {

    if (!clickableItems) {
      for (int i = getChildCount() - 1; i >= 0; i--) {
        getChildAt(i).setPressed(false);
      }
    }

    setPressed(false);
  }

  @Override
  public void dispatchSetSelected(final boolean selected) {
    /*
     * We don't want to pass the selected state given from its parent to its
     * children since this widget itself has a selected state to give to its
     * children.
     */
  }

  @Override
  protected void dispatchSetPressed(final boolean pressed) {

    // Show the pressed state on the selected child
    if (mSelectedChild != null && !clickableItems) {
      mSelectedChild.setPressed(pressed);
    }
  }

  @Override
  protected ContextMenuInfo getContextMenuInfo() {
    return mContextMenuInfo;
  }

  @Override
  public boolean showContextMenuForChild(final View originalView) {

    final int longPressPosition = getPositionForView(originalView);
    if (longPressPosition < 0) {
      return false;
    }

    final long longPressId = mAdapter.getItemId(longPressPosition);
    return dispatchLongPress(originalView, longPressPosition, longPressId);
  }

  @Override
  public boolean showContextMenu() {

    if (isPressed() && mSelectedPosition >= 0) {
      final int index = mSelectedPosition - mFirstPosition;
      final View v = getChildAt(index);
      return dispatchLongPress(v, mSelectedPosition, mSelectedRowId);
    }

    return false;
  }

  private boolean dispatchLongPress(final View view, final int position, final long id) {
    boolean handled = false;

    if (mOnItemLongClickListener != null) {
      handled = mOnItemLongClickListener.onItemLongClick(this, mDownTouchView,
          mDownTouchPosition, id);
    }

    if (!handled) {
      mContextMenuInfo = new AdapterContextMenuInfo(view, position, id);
      handled = super.showContextMenuForChild(this);
    }

    if (handled) {
      performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
    }

    return handled;
  }

  @Override
  public boolean dispatchKeyEvent(final KeyEvent event) {
    // Gallery steals all key events
    return event.dispatch(this, null, null);
  }

  /**
   * Handles left, right, and clicking.
   * @see android.view.View#onKeyDown
   */
  @Override
  public boolean onKeyDown(final int keyCode, final KeyEvent event) {
    switch (keyCode) {

    case KeyEvent.KEYCODE_DPAD_LEFT:
      if (movePrevious()) {
        playSoundEffect(SoundEffectConstants.NAVIGATION_LEFT);
      }
      return true;

    case KeyEvent.KEYCODE_DPAD_RIGHT:
      if (moveNext()) {
        playSoundEffect(SoundEffectConstants.NAVIGATION_RIGHT);
      }
      return true;

    case KeyEvent.KEYCODE_DPAD_CENTER:
    case KeyEvent.KEYCODE_ENTER:
      mReceivedInvokeKeyDown = true;
      // fallthrough to default handling
    default:
      return super.onKeyDown(keyCode, event);
    }

  }

  @Override
  public boolean onKeyUp(final int keyCode, final KeyEvent event) {
    switch (keyCode) {

    case KeyEvent.KEYCODE_DPAD_CENTER:
    case KeyEvent.KEYCODE_ENTER:

      if (mReceivedInvokeKeyDown && mItemCount > 0) {
        dispatchPress(mSelectedChild);
        postDelayed(new Runnable() {
          @Override
          public void run() {
            dispatchUnpress();
          }
        }, ViewConfiguration.getPressedStateDuration());

        final int selectedIndex = mSelectedPosition - mFirstPosition;
        performItemClick(getChildAt(selectedIndex), mSelectedPosition, mAdapter
            .getItemId(mSelectedPosition));
      }

      // Clear the flag
      mReceivedInvokeKeyDown = false;

      return true;

    default:
      return super.onKeyUp(keyCode, event);
    }

  }

  boolean movePrevious() {
    if (mItemCount > 0 && mSelectedPosition > 0) {
      scrollToChild(mSelectedPosition - mFirstPosition - 1);
      return true;
    } else {
      return false;
    }
  }

  boolean moveNext() {
    if (mItemCount > 0 && mSelectedPosition < mItemCount - 1) {
      scrollToChild(mSelectedPosition - mFirstPosition + 1);
      return true;
    } else {
      return false;
    }
  }

  private boolean scrollToChild(final int childPosition) {
    final View child = getChildAt(childPosition);

    if (child != null) {
      final int distance = getCenterOfGallery() - getCenterOfView(child);
      mFlingRunnable.startUsingDistance(distance);
      return true;
    }

    return false;
  }

  @Override
  void setSelectedPositionInt(final int position) {
    super.setSelectedPositionInt(position);

    // Updates any metadata we keep about the selected item.
    updateSelectedItemMetadata();
  }

  private void updateSelectedItemMetadata() {

    final View oldSelectedChild = mSelectedChild;

    mSelectedChild = getChildAt(mSelectedPosition - mFirstPosition);
    final View child = mSelectedChild;
    if (child == null) {
      return;
    }

    child.setSelected(true);
    child.setFocusable(true);

    if (hasFocus()) {
      child.requestFocus();
    }

    // We unfocus the old child down here so the above hasFocus check
    // returns true
    if (oldSelectedChild != null) {

      // Make sure its drawable state doesn't contain 'selected'
      oldSelectedChild.setSelected(false);

      // Make sure it is not focusable anymore, since otherwise arrow keys
      // can make this one be focused
      oldSelectedChild.setFocusable(false);
    }

  }

  /**
   * Describes how the child views are aligned.
   * @param gravity
   *
   * @attr ref android.R.styleable#Gallery_gravity
   */
  public void setGravity(final int gravity) {
    if (mGravity != gravity) {
      mGravity = gravity;
      requestLayout();
    }
  }

  @Override
  protected int getChildDrawingOrder(final int childCount, final int i) {
    final int selectedIndex = mSelectedPosition - mFirstPosition;

    // Just to be safe
    if (selectedIndex < 0) { return i; }

    if (i == childCount - 1) {
      // Draw the selected child last
      return selectedIndex;
    } else if (i >= selectedIndex) {
      // Move the children to the right of the selected child earlier one
      return i + 1;
    } else {
      // Keep the children to the left of the selected child the same
      return i;
    }
  }

  @Override
  protected void onFocusChanged(final boolean gainFocus, final int direction, final Rect previouslyFocusedRect) {
    super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);

    /*
     * The gallery shows focus by focusing the selected item. So, give
     * focus to our selected item instead. We steal keys from our
     * selected item elsewhere.
     */
    if (gainFocus && mSelectedChild != null) {
      mSelectedChild.requestFocus(direction);
    }

  }

  public void invalidateSelectedView() {
    final int sel = mSelectedPosition;
    if (sel < 0) { return; }
    final int count = getChildCount();
    View child = null;
    if (count == 1) {
      child = getChildAt(0);
    } else {
      for (int i = 0; i < count; i++) {
        final View c = getChildAt(i);
        if (c.isSelected()) {
          child = c;
          break;
        }
      }
    }
    if (child != null) {
      mBlockLayoutRequests = true;
      getAdapter().getView(sel, child, this);
      mBlockLayoutRequests = false;
    }
  }

  public void invalidateCurrentViews() {
    final int count = getChildCount();
    int position = mFirstPosition;
    final Adapter adapter = getAdapter();
    if (adapter == null) { return; }
    for (int i = 0; i < count; i++) {
      final View v = getChildAt(i);
      if (v == null) { break; }
      adapter.getView(position++, v, this);
    }
  }

  @Override
  protected void onDetachedFromWindow() {
    if (animatedViewHelper != null && notifyCrucialGUIOperations) {
      animatedViewHelper.onDetach();
    }
    super.onDetachedFromWindow();
  }

  /**
   * Responsible for fling behavior. Use {@link #startUsingVelocity(int)} to
   * initiate a fling. Each frame of the fling is handled in {@link #run()}.
   * A FlingRunnable will keep re-posting itself until the fling is done.
   *
   */
  private class FlingRunnable implements Runnable {
    /**
     * Tracks the decay of a fling scroll.
     */
    private Scroller mScroller;

    /**
     * X value reported by mScroller on the previous fling.
     */
    private int mLastFlingX;

    public FlingRunnable() {
      mScroller = new Scroller(getContext());
    }

    private void startCommon() {
      if (animatedViewHelper != null && notifyCrucialGUIOperations) { animatedViewHelper.notifyCrucialGuiStart(); }
      // Remove any pending flings
      removeCallbacks(this);
    }

    public void startUsingVelocity(final int initialVelocity) {
      if (initialVelocity == 0) { return; }

      startCommon();

      final int initialX = initialVelocity < 0 ? Integer.MAX_VALUE : 0;
      mLastFlingX = initialX;
      mScroller.fling(initialX, 0, initialVelocity, 0,
          0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
      post(this);
    }

    public void startUsingDistance(final int distance) {
      if (distance == 0) { return; }

      startCommon();

      mLastFlingX = 0;
      mScroller.startScroll(0, 0, -distance, 0, mAnimationDuration);
      post(this);
    }

    public void stop(final boolean scrollIntoSlots) {
      removeCallbacks(this);
      endFling(scrollIntoSlots);
    }

    private void endFling(final boolean scrollIntoSlots) {
      /*
       * Force the scroller's status to finished (without setting its
       * position to the end)
       */
      mScroller.forceFinished(true);

      if (scrollIntoSlots) { scrollIntoSlots(); }
      if (animatedViewHelper != null && notifyCrucialGUIOperations) { animatedViewHelper.notifyCrucialGuiFinish(); }
    }

    @Override
    public void run() {

      if (mItemCount == 0) {
        endFling(true);
        return;
      }

      mShouldStopFling = false;

      final Scroller scroller = mScroller;
      final boolean more = scroller.computeScrollOffset();
      final int x = scroller.getCurrX();

      // Flip sign to convert finger direction to list items direction
      // (e.g. finger moving down means list is moving towards the top)
      int delta = mLastFlingX - x;

      // Pretend that each frame of a fling scroll is a touch scroll
      if (delta > 0) {
        // Moving towards the left. Use first view as mDownTouchPosition
        mDownTouchPosition = mFirstPosition;

        // Don't fling more than 1 screen
        delta = Math.min(getWidth() - getPaddingLeft() - getPaddingRight() - 1, delta);
      } else {
        // Moving towards the right. Use last view as mDownTouchPosition
        final int offsetToLast = getChildCount() - 1;
        mDownTouchPosition = mFirstPosition + offsetToLast;

        // Don't fling more than 1 screen
        delta = Math.max(-(getWidth() - getPaddingRight() - getPaddingLeft() - 1), delta);
      }

      trackMotionScroll(delta);

      if (more && !mShouldStopFling) {
        mLastFlingX = x;
        post(this);
      } else {
        endFling(true);
      }
    }

  }

  /**
   * Gallery extends LayoutParams to provide a place to hold current
   * Transformation information along with previous position/transformation
   * info.
   *
   */
  public static class LayoutParams extends ViewGroup.LayoutParams {
    public LayoutParams(final Context c, final AttributeSet attrs) {
      super(c, attrs);
    }

    public LayoutParams(final int w, final int h) {
      super(w, h);
    }

    public LayoutParams(final ViewGroup.LayoutParams source) {
      super(source);
    }
  }
}

package com.stanfy.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

/**
 * <p>
 * This class is used to create a multiple-exclusion scope for a set of radio
 * buttons. Checking one radio button that belongs to a radio group unchecks any
 * previously checked radio button within the same group.
 * </p>
 *
 * <p>
 * Intially, all of the radio buttons are unchecked. While it is not possible to
 * uncheck a particular radio button, the radio group can be cleared to remove
 * the checked state.
 * </p>
 *
 * <p>
 * The selection is identified by the unique id of the radio button as defined
 * in the XML layout file.
 * </p>
 *
 * <p>
 * <strong>XML Attributes</strong>
 * </p>
 * <p>
 * See {@link android.R.styleable#RadioGroup RadioGroup Attributes},
 * {@link android.R.styleable#LinearLayout LinearLayout Attributes},
 * {@link android.R.styleable#ViewGroup ViewGroup Attributes},
 * {@link android.R.styleable#View View Attributes}
 * </p>
 * <p>
 * Also see {@link android.widget.RelativeLayout.LayoutParams
 * RelativeLayout.LayoutParams} for layout attributes.
 * </p>
 *
 * @see RadioButton
 *
 * Based on {@link RadioGroup}.
 * @author Olexandr Tereshchuk - Stanfy (http://www.stanfy.com)
 *
 */
public class RelativeRadioGroup extends RelativeLayout {

  /** Holds the checked id; the selection is empty by default. */
  private int mCheckedId = -1;
  /** Tracks children radio buttons checked state. */
  private CompoundButton.OnCheckedChangeListener mChildOnCheckedChangeListener;
  /** When true, mOnCheckedChangeListener discards events. */
  private boolean mProtectFromCheckedChange = false;
  /** Check change listener. */
  private OnCheckedChangeListener mOnCheckedChangeListener;
  /** Hierarchy listener. */
  private PassThroughHierarchyChangeListener mPassThroughListener;


  public RelativeRadioGroup(final Context context) {
    super(context);
    init();
  }

  public RelativeRadioGroup(final Context context, final AttributeSet attrs) {
    this(context, attrs, 0);
  }


  public RelativeRadioGroup(final Context context, final AttributeSet attrs,
      final int defStyle) {
    super(context, attrs, defStyle);

    // retrieve selected radio button as requested by the user in the
    // XML layout file
    final TypedArray attributes = context.obtainStyledAttributes(attrs,
        R.styleable.RelativeRadioGroup, defStyle, 0);

    final int value = attributes.getResourceId(
        R.styleable.RelativeRadioGroup_checkedButton, View.NO_ID);
    if (value != View.NO_ID) {
      mCheckedId = value;
    }

    attributes.recycle();
    init();
  }

  private void init() {
    mChildOnCheckedChangeListener = new CheckedStateTracker();
    mPassThroughListener = new PassThroughHierarchyChangeListener();
    super.setOnHierarchyChangeListener(mPassThroughListener);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setOnHierarchyChangeListener(final OnHierarchyChangeListener listener) {
    // the user listener is delegated to our pass-through listener
    mPassThroughListener.mOnHierarchyChangeListener = listener;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();

    // checks the appropriate radio button as requested in the XML file
    if (mCheckedId != -1) {
      mProtectFromCheckedChange = true;
      setCheckedStateForView(mCheckedId, true);
      mProtectFromCheckedChange = false;
      setCheckedId(mCheckedId);
    }
  }

  @Override
  public void addView(final View child, final int index, final ViewGroup.LayoutParams params) {
    if (child instanceof RadioButton) {
      final RadioButton button = (RadioButton) child;
      if (button.isChecked()) {
        mProtectFromCheckedChange = true;
        if (mCheckedId != -1) {
          setCheckedStateForView(mCheckedId, false);
        }
        mProtectFromCheckedChange = false;
        setCheckedId(button.getId());
      }
    }

    super.addView(child, index, params);
  }

  /**
   * <p>
   * Sets the selection to the radio button whose identifier is passed in
   * parameter. Using -1 as the selection identifier clears the selection; such
   * an operation is equivalent to invoking {@link #clearCheck()}.
   * </p>
   *
   * @param id
   *          the unique id of the radio button to select in this group
   *
   * @see #getCheckedRadioButtonId()
   * @see #clearCheck()
   */
  public void check(final int id) {
    // don't even bother
    if (id != -1 && (id == mCheckedId)) {
      return;
    }

    if (mCheckedId != -1) {
      setCheckedStateForView(mCheckedId, false);
    }

    if (id != -1) {
      setCheckedStateForView(id, true);
    }

    setCheckedId(id);
  }

  private void setCheckedId(final int id) {
    mCheckedId = id;
    if (mOnCheckedChangeListener != null) {
      mOnCheckedChangeListener.onCheckedChanged(this, mCheckedId);
    }
  }

  private void setCheckedStateForView(final int viewId, final boolean checked) {
    final View checkedView = findViewById(viewId);
    if (checkedView != null && checkedView instanceof RadioButton) {
      ((RadioButton) checkedView).setChecked(checked);
    }
  }

  /**
   * <p>
   * Returns the identifier of the selected radio button in this group. Upon
   * empty selection, the returned value is -1.
   * </p>
   *
   * @return the unique id of the selected radio button in this group
   *
   * @see #check(int)
   * @see #clearCheck()
   */
  public int getCheckedRadioButtonId() {
    return mCheckedId;
  }

  /**
   * <p>
   * Clears the selection. When the selection is cleared, no radio button in
   * this group is selected and {@link #getCheckedRadioButtonId()} returns null.
   * </p>
   *
   * @see #check(int)
   * @see #getCheckedRadioButtonId()
   */
  public void clearCheck() {
    check(-1);
  }

  /**
   * <p>
   * Register a callback to be invoked when the checked radio button changes in
   * this group.
   * </p>
   *
   * @param listener
   *          the callback to call on checked state change
   */
  public void setOnCheckedChangeListener(final OnCheckedChangeListener listener) {
    mOnCheckedChangeListener = listener;
  }

  /**
   * <p>
   * Interface definition for a callback to be invoked when the checked radio
   * button changed in this group.
   * </p>
   */
  public interface OnCheckedChangeListener {
    /**
     * <p>
     * Called when the checked radio button has changed. When the selection is
     * cleared, checkedId is -1.
     * </p>
     *
     * @param group
     *          the group in which the checked radio button has changed
     * @param checkedId
     *          the unique identifier of the newly checked radio button
     */
    void onCheckedChanged(final RelativeRadioGroup group, final int checkedId);
  }

  /** State tracker. */
  private class CheckedStateTracker implements
      CompoundButton.OnCheckedChangeListener {
    @Override
    public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
      // prevents from infinite recursion
      if (mProtectFromCheckedChange) {
        return;
      }

      mProtectFromCheckedChange = true;
      if (mCheckedId != -1) {
        setCheckedStateForView(mCheckedId, false);
      }
      mProtectFromCheckedChange = false;

      final int id = buttonView.getId();
      setCheckedId(id);
    }
  }

  /**
   * <p>
   * A pass-through listener acts upon the events and dispatches them to another
   * listener. This allows the table layout to set its own internal hierarchy
   * change listener without preventing the user to setup his.
   * </p>
   */
  private class PassThroughHierarchyChangeListener implements
      ViewGroup.OnHierarchyChangeListener {
    /** Hierarchy change listener. */
    private ViewGroup.OnHierarchyChangeListener mOnHierarchyChangeListener;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onChildViewAdded(final View parent, final View child) {
      if (parent == RelativeRadioGroup.this && child instanceof RadioButton) {
        int id = child.getId();
        // generates an id if it's missing
        if (id == View.NO_ID) {
          id = child.hashCode();
          child.setId(id);
        }
        ((RadioButton) child)
            .setOnCheckedChangeListener(mChildOnCheckedChangeListener);
      }

      if (mOnHierarchyChangeListener != null) {
        mOnHierarchyChangeListener.onChildViewAdded(parent, child);
      }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onChildViewRemoved(final View parent, final View child) {
      if (parent == RelativeRadioGroup.this && child instanceof RadioButton) {
        ((RadioButton) child).setOnCheckedChangeListener(null);
      }

      if (mOnHierarchyChangeListener != null) {
        mOnHierarchyChangeListener.onChildViewRemoved(parent, child);
      }
    }
  }
}

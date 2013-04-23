package com.stanfy.enroscar.views.qa;

import android.view.View;

import com.stanfy.enroscar.views.R;

/**
 * Adapter for quick actions.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public abstract class QuickActionsAdapter {

  /** Internal tags index. */
  public static final int TAG_INDEX = R.id.qa_tag;

  /** Observer. */
  private QuickActionsWidget widget;

  void setWidget(final QuickActionsWidget widget) { this.widget = widget; }

  /**
   * Notify quick actions widget about changes in the dataset.
   */
  public final void notifyActionsChanged() { widget.actionsChanged(); }

  /** @return count of actions */
  public abstract int getCount();

  /** @return action descriptor */
  public abstract Object getAction(final int position);

  /** @return view for quick action */
  protected abstract View createActionView(final int position);

  protected ActionTag createActionTag(final int position) { return new ActionTag(position); }

  public final View getActionView(final int position) {
    final View result = createActionView(position);
    if (result == null) { return null; }
    result.setTag(TAG_INDEX, createActionTag(position));
    return result;
  }

  /** Actions tag. */
  protected static class ActionTag {
    /** Action position. */
    int position;
    public ActionTag(final int position) { this.position = position; }
  }

}

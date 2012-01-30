package com.stanfy.views.qa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

/**
 * Adapter for simple actions.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class BasicQuickActionsAdapter extends QuickActionsAdapter {

  /** Quick actions list. */
  private final ArrayList<BasicQuickAction> actions;

  /** Context. */
  private final Context context;

  public BasicQuickActionsAdapter(final Context context) {
    this.context = context;
    this.actions = new ArrayList<BasicQuickAction>();
  }

  public BasicQuickActionsAdapter(final Context context, final Collection<? extends BasicQuickAction> actions) {
    this.context = context;
    this.actions = new ArrayList<BasicQuickAction>(actions);
  }

  public BasicQuickActionsAdapter(final Context context, final BasicQuickAction... actions) {
    this.context = context;
    this.actions = new ArrayList<BasicQuickAction>(Arrays.asList(actions));
  }

  @Override
  public int getCount() { return actions.size(); }
  @Override
  public BasicQuickAction getAction(final int position) { return actions.get(position); }

  @Override
  protected View createActionView(final int position) {
    final TextView result = new TextView(context);
    final BasicQuickAction action = getAction(position);
    result.setText(action.title);
    result.setCompoundDrawablesWithIntrinsicBounds(null, action.drawable, null, null);
    return result;
  }

  /** @param action new quick action */
  public void add(final BasicQuickAction action) {
    synchronized (actions) {
      if (action != null) {
        actions.add(action);
        notifyActionsChanged();
      }
    }
  }

  /** Clear all the actions. */
  public void clear() {
    synchronized (actions) {
      if (!actions.isEmpty()) {
        actions.clear();
        notifyActionsChanged();
      }
    }
  }

}

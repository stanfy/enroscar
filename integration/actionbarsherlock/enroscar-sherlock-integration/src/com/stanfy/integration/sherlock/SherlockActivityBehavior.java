package com.stanfy.integration.sherlock;

import android.app.Activity;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockExpandableListActivity;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.stanfy.app.ActivityBehaviorFactory;
import com.stanfy.app.BaseActivityBehavior;

/**
 * Behavior for sherlock activities.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 *
 */
public class SherlockActivityBehavior extends BaseActivityBehavior {

  public SherlockActivityBehavior(final Activity activity) {
    super(activity);
  }

  public static ActionBar getSupportActionBar(final Activity activity) {
    if (activity instanceof SherlockFragmentActivity) {
      return ((SherlockFragmentActivity)activity).getSupportActionBar();
    }
    if (activity instanceof SherlockActivity) {
      return ((SherlockActivity)activity).getSupportActionBar();
    }
    if (activity instanceof SherlockListActivity) {
      return ((SherlockListActivity)activity).getSupportActionBar();
    }
    if (activity instanceof SherlockExpandableListActivity) {
      return ((SherlockExpandableListActivity)activity).getSupportActionBar();
    }
    if (activity instanceof SherlockPreferenceActivity) {
      return ((SherlockPreferenceActivity)activity).getSupportActionBar();
    }

    return null;
  }

  public boolean onCreateOptionsMenu(final Menu menu) { return false; }

  public boolean onOptionsItemSelected(final MenuItem item) { return false; }

  /** Factory for {@link SherlockActivityBehavior}. */
  public static class Factory extends ActivityBehaviorFactory {

    @Override
    public SherlockActivityBehavior createActivityBehavior(final Activity activity) {
      return new SherlockActivityBehavior(activity);
    }

  }

}

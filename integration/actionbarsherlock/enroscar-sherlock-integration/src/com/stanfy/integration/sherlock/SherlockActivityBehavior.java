package com.stanfy.integration.sherlock;

import android.app.Activity;

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

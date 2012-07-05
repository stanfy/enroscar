package com.stanfy.app;

import android.app.Activity;

import com.stanfy.app.beans.BeansManager;
import com.stanfy.app.beans.EnroscarBean;

/**
 * Factory for {@link BaseActivityBehavior}.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
@EnroscarBean(ActivityBehaviorFactory.BEAN_NAME)
public class ActivityBehaviorFactory {

  /** Bean name. */
  public static final String BEAN_NAME = "ActivityBehaviorFactory";

  public static final BaseActivityBehavior createBehavior(final Activity activity) {
    return BeansManager.get(activity).getActivityBehaviorFactory().createActivityBehavior(activity);
  }

  public BaseActivityBehavior createActivityBehavior(final Activity activity) {
    return new BaseActivityBehavior(activity);
  }

}

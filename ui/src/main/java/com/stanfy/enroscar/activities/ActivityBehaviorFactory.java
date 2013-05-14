package com.stanfy.enroscar.activities;

import android.app.Activity;

import com.stanfy.enroscar.beans.Bean;
import com.stanfy.enroscar.beans.BeansManager;
import com.stanfy.enroscar.beans.EnroscarBean;

/**
 * Factory for {@link BaseActivityBehavior}.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
@EnroscarBean(ActivityBehaviorFactory.BEAN_NAME)
public class ActivityBehaviorFactory implements Bean {

  /** Bean name. */
  public static final String BEAN_NAME = "ActivityBehaviorFactory";

  public static final BaseActivityBehavior createBehavior(final Activity activity) {
    return BeansManager.get(activity).getContainer().getBean(ActivityBehaviorFactory.class).createActivityBehavior(activity);
  }

  public BaseActivityBehavior createActivityBehavior(final Activity activity) {
    return new BaseActivityBehavior(activity);
  }

}

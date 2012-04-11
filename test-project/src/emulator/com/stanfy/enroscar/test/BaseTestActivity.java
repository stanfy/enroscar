package com.stanfy.enroscar.test;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;

import com.jayway.android.robotium.solo.Solo;

/**
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 *
 */
public class BaseTestActivity<T extends Activity> extends ActivityInstrumentationTestCase2<T> {

  /** Solo tool. */
  private Solo solo;

  public BaseTestActivity(final Class<T> activityClass) {
    super("com.stanfy.enroscar.test", activityClass);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    solo = new Solo(getInstrumentation(), getActivity());
  }

  /** @return the solo */
  public Solo getSolo() { return solo; }

}

package com.stanfy.app.activities;

import android.os.Bundle;

import com.stanfy.app.BaseFragmentActivity;
import com.stanfy.utils.OneRequestModelBehavior;

/**
 * Activity that contains a request builder instance and
 * is oriented to processing a particular model type.
 * @param <MT> model type
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public abstract class OneRequestModelActivity<MT> extends BaseFragmentActivity implements OneRequestModelBehavior<MT> {

  /** Core. */
  private final OneRequestModelHelper<MT> core = new OneRequestModelHelper<MT>(this);

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    core.fetch();
  }

}

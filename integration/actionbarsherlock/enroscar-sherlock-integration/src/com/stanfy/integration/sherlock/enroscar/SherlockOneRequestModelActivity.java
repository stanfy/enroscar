package com.stanfy.integration.sherlock.enroscar;

import android.os.Bundle;

import com.stanfy.integration.sherlock.SherlockFragmentActivity;
import com.stanfy.utils.OneRequestModelBehavior;


public abstract class SherlockOneRequestModelActivity<MT> extends SherlockFragmentActivity implements OneRequestModelBehavior<MT> {

  /** Core. */
  private final OneRequestModelHelper<MT> core = new OneRequestModelHelper<MT>(this);

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    core.fetch();
  }

}

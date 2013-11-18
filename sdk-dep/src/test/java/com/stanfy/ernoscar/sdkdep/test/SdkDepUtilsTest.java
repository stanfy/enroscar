package com.stanfy.ernoscar.sdkdep.test;

import static org.fest.assertions.api.Assertions.*;

import com.stanfy.enroscar.sdkdep.SDKDependentUtilsFactory;
import com.stanfy.enroscar.sdkdep.SdkDependentUtils;
import com.stanfy.enroscar.sdkdep.SdkDepUtils;

import com.stanfy.enroscar.beans.BeansManager;

import org.junit.runner.RunWith;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Test for SdkDepUtils.
 */
@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18)
public class SdkDepUtilsTest {

  /** Instance. */
  private SdkDependentUtils utils;
  
  @Before
  public void create() {
    BeansManager.get(Robolectric.application).edit().put(SDKDependentUtilsFactory.class).commit();
    utils = SdkDepUtils.get(Robolectric.application);
  }
  
  @Test
  public void utilsShouldNotBeRecreated() {
    assertThat(utils).isSameAs(SdkDepUtils.get(Robolectric.application));
  }
  
}

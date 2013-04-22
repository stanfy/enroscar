package com.stanfy.enroscar.net.test;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;

import com.stanfy.enroscar.beans.BeansManager;
import com.stanfy.enroscar.net.EnroscarConnectionsEngineMode;
import com.stanfy.enroscar.sdkdep.SDKDependentUtilsFactory;

/**
 * Test strict mode is installed.
 */
@RunWith(Runner.class)
public class StrictModeInstallTest {

  @Test
  public void strictModeShouldBeInstalled() {
    BeansManager.get(Robolectric.application).edit().put(SDKDependentUtilsFactory.class).commit();
    EnroscarConnectionsEngineMode.installWithStrictMode();
    System.out.println("Current policy: " + StrictMode.getThreadPolicy());
    assertFalse(StrictMode.getThreadPolicy() == ThreadPolicy.LAX);
  }
  
}

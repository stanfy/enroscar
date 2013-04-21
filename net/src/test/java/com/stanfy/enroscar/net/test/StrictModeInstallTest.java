package com.stanfy.enroscar.net.test;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;

import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;

import com.stanfy.enroscar.net.EnroscarConnectionsEngineMode;

/**
 * Test strict mode is installed.
 */
@RunWith(Runner.class)
public class StrictModeInstallTest {

  @Test
  public void strictModeShouldBeInstalled() {
    EnroscarConnectionsEngineMode.installWithStrictMode();
    System.out.println("Current policy: " + StrictMode.getThreadPolicy());
    assertFalse(StrictMode.getThreadPolicy() == ThreadPolicy.LAX);
  }
  
}

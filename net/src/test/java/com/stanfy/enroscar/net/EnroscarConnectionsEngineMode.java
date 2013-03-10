package com.stanfy.enroscar.net;

import org.robolectric.Robolectric;

/**
 * Utility for setting up test mode for connections engine.
 */
public final class EnroscarConnectionsEngineMode {

  private EnroscarConnectionsEngineMode() { /* hidden */ }

  public static void testMode() {
    EnroscarConnectionsEngine.treatFileScheme = false;
  }

  public static void installWithStrictMode() {
    EnroscarConnectionsEngine.install(Robolectric.application, EnroscarConnectionsEngine.config(), true);
  }
  
}

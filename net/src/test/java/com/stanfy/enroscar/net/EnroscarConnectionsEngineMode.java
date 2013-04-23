package com.stanfy.enroscar.net;


/**
 * Utility for setting up test mode for connections engine.
 */
public final class EnroscarConnectionsEngineMode {

  private EnroscarConnectionsEngineMode() { /* hidden */ }

  public static void testMode() {
    EnroscarConnectionsEngine.treatFileScheme = false;
  }

}

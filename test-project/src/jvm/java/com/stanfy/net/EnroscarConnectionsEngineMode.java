package com.stanfy.net;

/**
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public final class EnroscarConnectionsEngineMode {

  private EnroscarConnectionsEngineMode() { /* hidden */ }

  public static void testMode() {
    EnroscarConnectionsEngine.treatFileScheme = false;
  }

}

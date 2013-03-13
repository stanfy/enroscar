package com.stanfy.enroscar.rest;

/**
 * Debugging flags.
 */
public final class DebugFlags {

  /** Debug networks-related classes. */
  public static final boolean DEBUG_IO = false; // @debug.io@

  // TODO: make it configuration based
  /** Debug REST operations. */
  public static final boolean DEBUG_REST = true; // @debug.rest@
  
  /** Flag whether to print response to Android log. */
  public static final boolean DEBUG_REST_RESPONSE = true; // @debug.rest.response@
  
  private DebugFlags() { }
  
}

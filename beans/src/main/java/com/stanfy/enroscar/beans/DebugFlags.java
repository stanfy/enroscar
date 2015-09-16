package com.stanfy.enroscar.beans;


/**
 * Debug flags for beans.
 * @deprecated Beans subsystem is deprecated now. Use some dependency injection framework (e.g. Dagger) instead.
 */
@Deprecated
final class DebugFlags {

  /** Beans debug flag. */
  public static final boolean DEBUG_BEANS = false; // @debug.beans@
  /** Beans creation debug flag. */
  public static final boolean DEBUG_BEANS_CREATE = false; // @debug.beans.create@

  /** Hidden constructor. */
  private DebugFlags() { /* hidden */ }

}

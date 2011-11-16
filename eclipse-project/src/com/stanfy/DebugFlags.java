package com.stanfy;

/**
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public final class DebugFlags {

  /** Private. */
  private DebugFlags() { /* hidden */ }

  /** Debug buffers flag. */
  public static final boolean DEBUG_BUFFERS = false; // @debug.buffers@

  /** IO debug flag. */
  public static final boolean DEBUG_IO = false; // @debug.io@

  /** Images debug flag. */
  public static final boolean DEBUG_IMAGES = true; // @debug.images@

  /** Debug services flag. */
  public static final boolean DEBUG_SERVICES = false; // @debug.services@

  /** Debug C2DM. */
  public static final boolean DEBUG_C2DM = true; // @debug.c2dm@

}

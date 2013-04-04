package com.stanfy;

/**
 * Debug flags. They are switched according to build configuration.
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public final class DebugFlags {

  /** Private. */
  private DebugFlags() { /* hidden */ }

  /** Debug buffers flag. */
  public static final boolean STRICT_MODE = true; // @debug.strict@

  /** Debug buffers flag. */
  public static final boolean DEBUG_BUFFERS = false; // @debug.buffers@
  /** DB utilities debug flag. */
  public static final boolean DEBUG_DB_UTILS = false; // @debug.dbutils@

  /** IO debug flag. */
  public static final boolean DEBUG_IO = false; // @debug.io@

  /** Images debug flag. */
  public static final boolean DEBUG_IMAGES = false; // @debug.images@

  /** GUI debug flag. */
  public static final boolean DEBUG_GUI = false; // @debug.gui@

  /** API debug flag. */
  public static final boolean DEBUG_API = false; // @debug.api@
  /** API response debug flag. */
  public static final boolean DEBUG_API_RESPONSE = false; // @debug.api.response@

  /** Utilities debug flag. */
  public static final boolean DEBUG_UTILS = false; // @debug.utils@

  /** Debug service flag. */
  public static final boolean DEBUG_SERVICES = false; // @debug.services@

}

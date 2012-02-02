package com.stanfy;

/**
 * Debug flags. They are switched according to build configuration.
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public final class DebugFlags {

  /** Private. */
  private DebugFlags() { /* hidden */ }

  /** Debug buffers flag. */
  public static final boolean STRICT_MODE = false; // @debug.strict@

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

  /** Utilities debug flag. */
  public static final boolean DEBUG_UTILS = false; // @debug.utils@

  /** Debug service flag. */
  public static final boolean DEBUG_SERVICES = false; // @debug.services@

  /** Debug parser flag. */
  public static final boolean DEBUG_PARSER = false; // @debug.parser@

  /** Debug location flag. */
  public static final boolean DEBUG_LOCATION = false; // @debug.location@

  /** Debug C2DM. */
  public static final boolean DEBUG_C2DM = false; // @debug.c2dm@

  /** Debug stats. */
  public static final boolean DEBUG_STATS = false; // @debug.stats@

}

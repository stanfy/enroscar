package com.stanfy.enroscar.async.internal;

/**
 * Utilities.
 */
public final class Utils {

  /** Generated class name suffix. */
  private static final String SUFFIX = "$$Loader";

  public static String getGeneratedClassName(final String packageName, final String baseClassName) {
    String base = baseClassName;
    if (packageName.length() > 0) {
      base = base.substring(packageName.length() + 1);
    }
    return base.replace(".", "$").concat(SUFFIX);
  }

}

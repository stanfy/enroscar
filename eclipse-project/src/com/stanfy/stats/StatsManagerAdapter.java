package com.stanfy.stats;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.stanfy.DebugFlags;
import com.stanfy.utils.AppUtils;

/**
 * Introduces some additional methods to {@link StatsManager} interface and implements them.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public abstract class StatsManagerAdapter implements StatsManager {

  /** Logging tag. */
  protected static final String TAG = "Stats";
  /** Debug flag. */
  protected static final boolean DEBUG = DebugFlags.DEBUG_STATS;

  /** ' at ' pattern. */
  private static final Pattern PATTERN_ST_AT = Pattern.compile("\\s*\\n?\\s*at\\s+");
  /** Exception class pattern. */
  private static final Pattern PATTERN_ST_EX_CLASS = Pattern.compile("[a-zA-Z0-9_.]+\\.(\\w+):?");

  public void event(final String tag) {
    event(tag, Collections.<String, String>emptyMap());
  }

  public void event(final String tag, final String[][] tuples) {
    event(tag, AppUtils.<String, String>tuples(tuples));
  }

  static String trimStackTrace(final String st) {
    Matcher m = PATTERN_ST_AT.matcher(st);
    String result = m.replaceAll(",");
    m = PATTERN_ST_EX_CLASS.matcher(result);
    result = m.replaceFirst("$1");
    return result;
  }

  /**
   * @param e exception instance
   * @param maxTotalLength maximum message length
   * @return trimmed version of error message
   */
  public String readException(final Throwable e, final int maxTotalLength) {
    if (e == null) { return "-NULL-"; }
    final StringWriter sw = new StringWriter();
    e.printStackTrace(new PrintWriter(sw));
    final String resultStr = trimStackTrace(sw.toString());
    return resultStr.length() > maxTotalLength ? resultStr.substring(0, maxTotalLength) : resultStr;
  }

}

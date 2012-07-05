package com.stanfy.stats;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;

import com.stanfy.DebugFlags;
import com.stanfy.app.beans.EnroscarBean;
import com.stanfy.utils.AppUtils;

/**
 * Statistics manager, instantiated by application object.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
@EnroscarBean(StatsManager.BEAN_NAME)
public abstract class StatsManager {

  /** Bean name. */
  public static final String BEAN_NAME = "StatsManager";

  /**
   * Start session routine.
   * @param activity activity instance
   */
  public abstract void onStartSession(final Activity activity);

  /**
   * On start screen.
   * @param activity activity instance
   */
  public abstract void onStartScreen(final Activity activity);

  /**
   * On come to screen.
   * @param activity activity instance
   */
  public abstract void onComeToScreen(final Activity activity);

  /**
   * On leave screen.
   * @param activity activity instance
   */
  public abstract void onLeaveScreen(final Activity activity);

  /**
   * Session end routine.
   * @param activity activity instance
   */
  public abstract void onEndSession(final Activity activity);

  /**
   * Report error.
   * @param tag error tag
   * @param e error instance
   */
  public abstract void error(final String tag, final Throwable e);

  /**
   * Report ecent.
   * @param tag event tag
   * @param params event parameters
   */
  public abstract void event(final String tag, final Map<String, String> params);

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

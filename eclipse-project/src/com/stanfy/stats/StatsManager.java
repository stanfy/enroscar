package com.stanfy.stats;

import java.util.Map;

import android.app.Activity;

/**
 * Statistics manager, instantiated by application object.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public interface StatsManager {

  /**
   * Start session routine.
   * @param activity activity instance
   */
  void onStartSession(final Activity activity);

  /**
   * On start screen.
   * @param activity activity instance
   */
  void onStartScreen(final Activity activity);

  /**
   * On come to screen.
   * @param activity activity instance
   */
  void onComeToScreen(final Activity activity);

  /**
   * On leave screen.
   * @param activity activity instance
   */
  void onLeaveScreen(final Activity activity);

  /**
   * Session end routine.
   * @param activity activity instance
   */
  void onEndSession(final Activity activity);

  /**
   * Report error.
   * @param tag error tag
   * @param e error instance
   */
  void error(final String tag, final Throwable e);

  /**
   * Report ecent.
   * @param tag event tag
   * @param params event parameters
   */
  void event(final String tag, final Map<String, String> params);

}

package com.stanfy.stats;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Map;

import android.app.Activity;

import com.flurry.android.FlurryAgent;

/**
 * Statistics manager based on Flurry SDK.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class FlurryStatsManager extends StatsManagerAdapter {

  /** API key. */
  private final String key;

  public FlurryStatsManager(final String key) {
    this.key = key;
    FlurryAgent.setLogEnabled(DEBUG);
    FlurryAgent.setCaptureUncaughtExceptions(false);
    if (!DEBUG) {
      Thread.setDefaultUncaughtExceptionHandler(new ExceptionsHandler(Thread.getDefaultUncaughtExceptionHandler()));
    }
  }

  @Override
  public void onStartScreen(final Activity activity) {
    FlurryAgent.onStartSession(activity, key);
  }

  @Override
  public void onComeToScreen(final Activity activity) {
    FlurryAgent.onPageView();
  }

  @Override
  public void onLeaveScreen(final Activity activity) {
    FlurryAgent.onEndSession(activity);
  }

  @Override
  public void event(final String tag, final Map<String, String> params) {
    FlurryAgent.logEvent(tag, params);
  }

  @Override
  public void error(final String tag, final Throwable e) {
    final int max = 255;
    FlurryAgent.onError(tag, readException(e, max), e.getClass().getName());
  }

  /**
   * @author Roman Mazur (Stanfy - http://www.stanfy.com)
   */
  private class ExceptionsHandler implements UncaughtExceptionHandler {

    /** Default handler. */
    private final UncaughtExceptionHandler handler;

    public ExceptionsHandler(final UncaughtExceptionHandler handler) { this.handler = handler; }

    @Override
    public void uncaughtException(final Thread thread, final Throwable ex) {
      error("FATAL", ex);
      handler.uncaughtException(thread, ex);
    }
  }

}

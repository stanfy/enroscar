package com.stanfy.stats;

import java.util.Map;

import android.app.Activity;

/**
 * Empty implementation of {@link StatsManager}.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class EmptyStatsManager implements StatsManager                                    {

  @Override
  public void onStartScreen(final Activity activity) { /* nothing */ }

  @Override
  public void onComeToScreen(final Activity activity) { /* nothing */ }

  @Override
  public void onLeaveScreen(final Activity activity) { /* nothing */ }

  @Override
  public void error(final String tag, final Throwable e) { /* nothing */ }

  @Override
  public void event(final String tag, final Map<String, String> params) { /* nothing */ }

}

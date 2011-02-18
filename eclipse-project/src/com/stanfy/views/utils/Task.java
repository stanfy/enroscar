package com.stanfy.views.utils;

import android.database.SQLException;
import android.util.Log;

/**
 * Task for a thread pool.
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public abstract class Task implements Runnable {

  /** Name. */
  private final String name;

  /** Task tag. */
  private Object tag;

  public Task(final String name) { this.name = name; }

  /**
   * @return the name
   */
  public String getName() { return name; }

  /**
   * @param tag the tag to set
   */
  public void setTag(final Object tag) { this.tag = tag; }
  /**
   * @return the tag
   */
  public Object getTag() { return tag; }

  @Override
  public final void run() {
    try {
      safeSQLRun();
    } catch (final SQLException e) {
      Log.e(name, "SQL error", e);
    }
  }

  protected abstract void safeSQLRun();

}

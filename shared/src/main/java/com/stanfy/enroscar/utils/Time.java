package com.stanfy.enroscar.utils;

/**
 * Time utilities.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public final class Time {

  /** Milliseconds in 1 second. */
  public static final long SECONDS = 1000;
  /** Milliseconds in 1 minute. */
  public static final long MINUTES = 60 * SECONDS;
  /** Milliseconds in 1 hour. */
  public static final long HOURS = 60 * MINUTES;
  /** Milliseconds in 1 day. */
  public static final long DAYS = 24 * HOURS;

  private Time() { /* hide */ }

  public static long asSeconds(final long millis) {
    return millis / SECONDS;
  }

}

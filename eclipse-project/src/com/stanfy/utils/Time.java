package com.stanfy.utils;

/**
 * Time utilities.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public final class Time {

  private Time() { /* hide */ }

  /** Milliseconds in 1 second. */
  public static final int SECONDS = 1000;
  /** Milliseconds in 1 minute. */
  public static final int MINUTES = 60 * SECONDS;
  /** Milliseconds in 1 hour. */
  public static final int HOURS = 60 * MINUTES;
  /** Milliseconds in 1 day. */
  public static final int DAYS = 24 * HOURS;

}

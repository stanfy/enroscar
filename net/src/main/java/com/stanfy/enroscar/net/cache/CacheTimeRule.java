package com.stanfy.enroscar.net.cache;

import java.util.regex.Pattern;

import com.stanfy.enroscar.utils.Time;

/**
 * Cache time to live rule.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public abstract class CacheTimeRule {

  /** Time to live. */
  final long time;

  public CacheTimeRule(final long time) {
    this.time = time;
  }
  
  public static boolean isUntilActual(final long createTime, final long untilTime) {
    final long day = Time.DAYS;
    final long current = System.currentTimeMillis();
    long margin = current / day * day + untilTime;
    if (createTime > margin) { margin += day; }
    return current < margin;
  }

  /**
   * @param pattern URI pattern
   * @param ttl time to live
   * @return cache rule that uses a regular expression for matching
   */
  public static CacheTimeRule ttlRuleForUri(final String pattern, final long ttl) {
    return new PatternBasedCacheTimeRule(pattern, ttl);
  }

  /**
   * @param pattern URI pattern
   * @param until count of millisecond from 00:00 of the current day to determine an hour when cache expires
   * @return cache rule that uses a regular expression for matching
   */
  public static CacheTimeRule untilRuleForUri(final String pattern, final long until) {
    return new PatternBasedCacheTimeRule(pattern, until) {
      @Override
      public boolean isActual(final long createTime) { return isUntilActual(createTime, time); }
    };
  }

  public boolean isActual(final long createTime) { return time > System.currentTimeMillis() - createTime; }

  public abstract boolean matches(CacheEntry cacheEntry);

  /**
   * @return string that describes a matcher; it can be a regexp string for pattern matcher
   */
  protected abstract String matcherToString();

  public long getTime() { return time; }

  @Override
  public String toString() { return getClass().getSimpleName() + ":" + matcherToString() + "/" + (time / Time.MINUTES) + "min"; }

  /**
   * Time rule that uses {@link Pattern} for matching.
   */
  public static class PatternBasedCacheTimeRule extends CacheTimeRule {

    /** Pattern. */
    private final Pattern regex;

    public PatternBasedCacheTimeRule(final String pattern, final long time) {
      super(time);
      regex = Pattern.compile(pattern);
    }

    @Override
    public boolean matches(final CacheEntry cacheEntry) {
      return regex.matcher(cacheEntry.getUri()).matches();
    }
    @Override
    protected String matcherToString() {
      return regex.pattern();
    }

  }

}

package com.stanfy.enroscar.net.cache;


/**
 * <p>File-based cache for remote API responses.</p>
 * Usage:
 * <pre>
 *   &#64;EnroscarBean("MyApiResponseCache")
 *   public class MyApiResponseCache extends ApiFileResponseCache {
 *
 *     private static final TimeRule[] TIME_RULES = {
 *       ...
 *     }
 *
 *     public MyApiResponseCache() {
 *       setWorkingDirectory(...);
 *       setMaxSize(...);
 *     }
 *
 *     protected TimeRule[] getTimeRules() {
 *       return TIME_RULES;
 *     }
 *   }
 * </pre>
 * How to use with request descriptions:
 * <pre>
 *
 *   // configure once
 *   BeansManager.get(context).edit().put(MyApiResponseCache.class);
 *
 *   // use with your request descriptions
 *   RequestDescription rd;
 *   ...
 *   rd.setCacheName("MyApiResponseCache");
 *
 * </pre>
 */
public abstract class ApiFileResponseCache extends BaseFileResponseCache {

  @Override
  protected CacheEntry createCacheEntry() {
    final CacheEntry entry = new CacheEntry();
    entry.setTimeRules(getTimeRules());
    return entry;
  }

  /**
   * @return array of rules that define how long cache records can be used
   */
  protected abstract CacheTimeRule[] getTimeRules();

}

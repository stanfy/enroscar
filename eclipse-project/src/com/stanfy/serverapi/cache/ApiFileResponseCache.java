/**
 *
 */
package com.stanfy.serverapi.cache;

import com.stanfy.net.cache.BaseFileResponseCache;
import com.stanfy.net.cache.CacheEntry;
import com.stanfy.net.cache.CacheTimeRule;

/**
 * <p>File-based cache for remote API responses.</p>
 * Usage:
 * <pre>
 *   @EnroscarBean("MyApiResponseCache")
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
 * @author Roman Mazur (Stanfy - http://stanfy.com)
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

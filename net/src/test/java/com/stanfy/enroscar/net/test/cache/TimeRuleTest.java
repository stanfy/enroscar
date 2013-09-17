package com.stanfy.enroscar.net.test.cache;

import static org.fest.assertions.api.Assertions.assertThat;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import com.stanfy.enroscar.net.cache.CacheEntry;
import com.stanfy.enroscar.net.cache.CacheTimeRule;
import com.stanfy.enroscar.shared.test.AbstractEnroscarTest;
import com.stanfy.enroscar.utils.Time;

/**
 * Tests for {@link CacheTimeRule}.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
@RunWith(RobolectricTestRunner.class)
public class TimeRuleTest extends AbstractEnroscarTest {

  /** Test URL. */
  private static final String URL = "/test/url/";

  /** Cache entry. */
  private CacheEntry entry;

  @Before
  public void createEntry() throws Exception {
    entry = new CacheEntry();
    entry.set(new URI(URL), "GET", Collections.<String, List<String>>emptyMap());
  }

  @Test
  public void cacheEntryShoudBeAbleToBeUsed() {
    final long ts = entry.getTimestamp();

    entry.setTimeRules(new CacheTimeRule[] {
      CacheTimeRule.ttlRuleForUri("ignored", 0),
      CacheTimeRule.ttlRuleForUri(URL, System.currentTimeMillis() - ts + Time.SECONDS),
    });
    assertThat(entry.canBeUsed()).isTrue();
    entry.setTimeRules(new CacheTimeRule[] {
      CacheTimeRule.untilRuleForUri(URL, ts / Time.HOURS * Time.HOURS - ts / Time.DAYS * Time.DAYS + Time.HOURS)
    });
    assertThat(entry.canBeUsed()).isTrue();
  }

  @Test
  public void cacheEntryShoudNotBeAbleToBeUsed() {
    entry.setTimeRules(new CacheTimeRule[] {
      CacheTimeRule.ttlRuleForUri(URL, 0)
    });
    assertThat(entry.canBeUsed()).isFalse();

    final long ts = entry.getTimestamp();
    entry.setTimeRules(new CacheTimeRule[] {
      CacheTimeRule.untilRuleForUri(URL, ts / Time.HOURS * Time.HOURS - ts / Time.DAYS * Time.DAYS - Time.DAYS)
    });
    assertThat(entry.canBeUsed()).isFalse();
  }

}

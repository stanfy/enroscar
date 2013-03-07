package com.stanfy.enroscar.net.test.cache;

import java.io.IOException;
import java.net.ResponseCache;

import org.junit.Before;

import com.stanfy.enroscar.beans.BeansManager;
import com.stanfy.enroscar.beans.BeansManager.Editor;
import com.stanfy.enroscar.net.test.AbstractMockServerTest;

/**
 * Abstract one cache test.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public abstract class AbstractOneCacheTest extends AbstractMockServerTest {

  /** Cache name. */
  protected static final String CACHE_NAME = "testCache";

  /** Cache instance. */
  SimpleFileCache cache;

  @Override
  protected void configureBeansManager(final Editor editor) {
    super.configureBeansManager(editor);
    editor.put(CACHE_NAME, new SimpleFileCache("test-base-cache"));
  }

  @Before
  public void setupCache() throws IOException {
    cache = BeansManager.get(null).getContainer().getBean("testCache", SimpleFileCache.class);
    ResponseCache.setDefault(cache);
  }

}

package com.stanfy.net.cache;

import java.io.IOException;
import java.net.ResponseCache;

import org.junit.Before;

import com.stanfy.app.beans.BeansManager;
import com.stanfy.app.beans.BeansManager.Editor;
import com.stanfy.test.AbstractMockServerTest;

/**
 * Abstract one cache test.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public abstract class AbstractOneCacheTest extends AbstractMockServerTest {

  /** Cache instance. */
  SimpleFileCache cache;

  @Override
  protected void configureBeansManager(final Editor editor) {
    super.configureBeansManager(editor);
    editor.put("testCache", new SimpleFileCache("test-base-cache"));
  }

  @Before
  public void setupCache() throws IOException {
    cache = (SimpleFileCache)BeansManager.get(null).getResponseCache("testCache");
    ResponseCache.setDefault(cache);
  }

}

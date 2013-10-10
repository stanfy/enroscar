package com.stanfy.enroscar.images;

import com.stanfy.enroscar.beans.BeansManager;
import com.stanfy.enroscar.images.cache.ImageFileCache;
import com.stanfy.enroscar.images.cache.SupportLruImageMemoryCache;
import com.stanfy.enroscar.net.UrlConnectionBuilderFactory;

/**
 * Set default beans.
 */
public class BeanSetup {

  private BeanSetup() { }

  public static void setup(final BeansManager.Editor editor) {
    editor.put(ImageFileCache.class);
    editor.put(SupportLruImageMemoryCache.class);
    editor.put(ImagesManager.CONNECTION_BUILDER_FACTORY_NAME, UrlConnectionBuilderFactory.DEFAULT);
    editor.put(ImagesManager.class);
  }

}

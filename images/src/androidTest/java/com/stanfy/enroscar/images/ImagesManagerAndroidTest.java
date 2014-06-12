package com.stanfy.enroscar.images;

import com.stanfy.enroscar.images.cache.ImageFileCache;

import java.net.ResponseCache;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests for Images manager.
 */
public class ImagesManagerAndroidTest extends BaseAndroidTest {

  public void testSetup() {

    ResponseCache cache = imagesManager.getImagesResponseCache();
    assertThat(cache).isInstanceOf(ImageFileCache.class);
    ImageFileCache imageFileCache = (ImageFileCache) cache;

    assertThat(imageFileCache.getWorkingDirectory()).isNotNull();
    assertThat(imageFileCache.getWorkingDirectory()).exists();
    assertThat(imageFileCache.getWorkingDirectory()).isDirectory();
    assertThat(imageFileCache.getMaxSize()).isGreaterThan(0);

  }

}

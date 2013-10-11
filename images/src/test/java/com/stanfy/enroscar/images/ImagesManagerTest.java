package com.stanfy.enroscar.images;

import android.graphics.Bitmap;

import com.stanfy.enroscar.beans.BeansManager;
import com.stanfy.enroscar.images.cache.SupportLruImageMemoryCache;

import org.junit.Test;
import org.robolectric.Robolectric;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests for {@link ImagesManager}.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public class ImagesManagerTest extends AbstractImagesTest {

  @Test
  public void getFromMemCacheShouldCheckConsumerSize() {
    final int size = 50;

    ImagesManager imagesManager = BeansManager.get(Robolectric.application).getContainer().getBean(ImagesManager.class);
    SupportLruImageMemoryCache memCache = BeansManager.get(Robolectric.application).getContainer().getBean(SupportLruImageMemoryCache.class);

    memCache.putElement("http://123.com", Bitmap.createBitmap(size - 1, size + 1, Bitmap.Config.ARGB_8888));
    assertThat(imagesManager.getMemCached("http://123.com", new Consumer(size, size))).isNotNull();

    memCache.putElement("http://123.com", Bitmap.createBitmap(size / 2, size + 1, Bitmap.Config.ARGB_8888));
    assertThat(imagesManager.getMemCached("http://123.com", new Consumer(size, size))).isNull();

    memCache.putElement("http://123.com", Bitmap.createBitmap(size / 2, size / 2, Bitmap.Config.ARGB_8888));
    assertThat(imagesManager.getMemCached("http://123.com", new Consumer(size, size))).isNull();

    memCache.putElement("http://123.com", Bitmap.createBitmap(size / 2, size / 2, Bitmap.Config.ARGB_8888));
    assertThat(imagesManager.getMemCached("http://123.com", new Consumer(0, 0))).isNotNull();

    memCache.putElement("http://123.com", Bitmap.createBitmap(size * 2, size * 2, Bitmap.Config.ARGB_8888));
    assertThat(imagesManager.getMemCached("http://123.com", new Consumer(0, size))).isNotNull();

    memCache.putElement("http://123.com", Bitmap.createBitmap(size / 2, size / 2, Bitmap.Config.ARGB_8888));
    assertThat(imagesManager.getMemCached("http://123.com", new Consumer(0, size))).isNull();
  }

  @Test
  public void calculateSampleFactorShouldRespectDynamicSize() {
    //CHECKSTYLE:OFF
    assertThat(ImagesManager.calculateSampleFactor(100, 100, 50, 0)).isEqualTo(2);
    assertThat(ImagesManager.calculateSampleFactor(100, 100, 0, 25)).isEqualTo(4);
    //CHECKSTYLE:ON
  }

  @Test
  public void calculateSampleFactorShouldRespectSmallInput() {
    //CHECKSTYLE:OFF
    assertThat(ImagesManager.calculateSampleFactor(50, 50, 100, 100)).isEqualTo(1);
    assertThat(ImagesManager.calculateSampleFactor(50, 50, 50, 100)).isEqualTo(1);
    assertThat(ImagesManager.calculateSampleFactor(50, 50, 100, 50)).isEqualTo(1);
    assertThat(ImagesManager.calculateSampleFactor(50, 50, 50, 50)).isEqualTo(1);
    assertThat(ImagesManager.calculateSampleFactor(50, 50, 20, 20)).isEqualTo(2);
    //CHECKSTYLE:ON
  }

  @Test
  public void isPresentInFileCacheShouldCheckUriScheme() {
    assertThat(manager.isPresentOnDisk("http://bla/bla")).isFalse();
    assertThat(manager.isPresentOnDisk("file://ahahaha")).isTrue();
    assertThat(manager.isPresentOnDisk("android.resource://ahahaha")).isTrue();
    assertThat(manager.isPresentOnDisk("content://ahahaha")).isTrue();
  }

}

package com.stanfy.enroscar.images;

import android.graphics.Bitmap;

import com.stanfy.enroscar.beans.BeansManager;
import com.stanfy.enroscar.images.cache.SupportLruImageMemoryCache;
import com.stanfy.enroscar.io.BuffersPool;
import com.stanfy.enroscar.sdkdep.SDKDependentUtilsFactory;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Tests for {@link ImagesManager}.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ImagesManagerTest {

  @Before
  public void init() {
    BeansManager.get(Robolectric.application).edit()
        .put(BuffersPool.class)
        .put(SDKDependentUtilsFactory.class)
        .put(SupportLruImageMemoryCache.class)
        .put(ImagesManager.class)
        .commit();
  }

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
  public void calculateSampleFactorShouldReturnNearestPowerOf2() {
    final Object[][] testTable = {
//      width    height  imageWidth imageHeight  useHeight
        {300,     500,    300 * 2,    500 * 2,     true},   // factor = 2
        {100,     50,     100 * 3,    50 * 2,      false},  // factor = 2
        {300,     500,    300 * 5,    500 * 5,     true},   // factor = 4
        {300,     500,    300 * 6,    500 * 6,     true},   // factor = 4
        // (skip '* 7', see #calculateSampleFactorShouldReturnExactDivisionResult)
        {300,     500,    300 * 8,    500 * 8,     true},   // factor = 8
        {300,     500,    300 * 9,    500 * 9,     true},   // factor = 8
        {300,     500,    300 * 16,   500 * 16,    true},   // factor = 16
    };

    testFactor(testTable, true);
  }

  @Test
  public void calculateSampleFactorShouldReturnExactDivisionResult() {
    final Object[][] testTable = {
//      width    height  imageWidth imageHeight  useHeight
        {100,     300,    100 * 7,    300 * 7,     true},    // factor = 7
        {300,     100,    300 * 11,   100 * 11,    false},   // factor = 11
        {300,     100,    300 * 12,   100 * 11,    false},   // factor = 12
    };

    testFactor(testTable, false);
  }

  @Test
  public void calculateSampleFactorShouldRespectDynamicSize() {
    //CHECKSTYLE:OFF
    assertThat(ImagesManager.calculateSampleFactor(100, 100, 50, 0)).isEqualTo(2);
    assertThat(ImagesManager.calculateSampleFactor(100, 100, 0, 25)).isEqualTo(4);
    //CHECKSTYLE:ON
  }

  private void testFactor(final Object[][] testTable, final boolean nearestPowerOf2) {
    for (final Object[] row : testTable) {
      final int width = (Integer) row[0], height = (Integer) row[1];
      final int imageWidth = (Integer) row[2], imageHeight = (Integer) row[3];
      final boolean useHeight = (Boolean) row[row.length - 1];

      final int factor = ImagesManager.calculateSampleFactor(imageWidth, imageHeight, width, height);
      final int f = useHeight ? imageHeight / height : imageWidth / width;

      if (nearestPowerOf2) {

        assertThat(factor).as("Not power of 2 for " + Arrays.toString(row))
            .isEqualTo(ImagesManager.nearestPowerOf2(f))
            .isGreaterThan(f - ImagesManager.MAX_POWER_OF_2_DISTANCE);

      } else {

        assertThat(factor).as("Not exact factor " + Arrays.toString(row)).isEqualTo(f);

      }
    }
  }

  @Test
  public void testNearestPowerOf2() {

    final int[] testTable = {
      // illegal values...
      -1, -1,
      -100, -1,
      0, -1,

      // ...and test set
      2, 2,
      3, 2,
      4, 4,
      5, 4,
      6, 4,
      7, 4,
      8, 8,
      9, 8,
      1024, 1024,
      1027, 1024,
      Integer.MAX_VALUE, ~Integer.MAX_VALUE >>> 1 // :) 0x7fffffff, 0x40000000
    };

    final int n = testTable.length / 2;
    for (int i = 0; i < n; i++) {
      final int x = testTable[2 * i];
      final int y = testTable[2 * i + 1];
      assertThat(ImagesManager.nearestPowerOf2(x)).isEqualTo(y);
    }
  }

}

package com.stanfy.enroscar.images;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.stanfy.enroscar.images.test.Runner;

/**
 * Tests for {@link ImagesManager}.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
@RunWith(Runner.class)
public class ImagesManagerTest {

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

package com.stanfy.enroscar.utils.test;

import static org.fest.assertions.api.Assertions.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import com.stanfy.enroscar.utils.AppUtils;

/**
 * Tests for {@link AppUtils}.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18)
public class AppUtilsTest {

  @Test
  public void md5Test() throws Exception {
    assertThat(AppUtils.getMd5("The quick brown fox jumps over the lazy dog")).isEqualTo("9e107d9d372bb6826bd81d3542a419d6");
    assertThat(AppUtils.getMd5("")).isEqualTo("d41d8cd98f00b204e9800998ecf8427e");

    final byte[] trailingZerosBytes = {
        26, -44, 25, -5, 57, -44, 114, -103, -114, -54, 59, 103, 120, 87, 5, -21, 97, 113, 30,
        -86, 81, -105, 46, -8, -6, -9, 72, -93, -81, 73, -25, -36, -126, -51, -78, -44, -39, 124,
        -75, 20, 9, -121, -84, 45, -23, 124, -65, -123, -34, -26, 127, -15, -71, -83, 110, 0, -82, 23,
        -40, 102, -49, 58, -118, 29, 98, 82, -83, 21, 14, 110, -76, -84, -32, -125, -94, -76, 50, 31, -62,
        67, 12, -80, -125, -88, 98, 81, 94, 61, -60, -47, -10, 9, 96, -119, -103, 99, -83, 99, 35, -3
    };
    assertThat(AppUtils.getMd5(new String(trailingZerosBytes, "UTF-8"))).isEqualTo("000445c7823faa427788454fc7f9a0fd");
  }

}

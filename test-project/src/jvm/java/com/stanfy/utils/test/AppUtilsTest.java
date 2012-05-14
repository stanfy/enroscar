package com.stanfy.utils.test;

import static junit.framework.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.stanfy.test.EnroscarTestRunner;
import com.stanfy.utils.AppUtils;

/**
 * Tests for {@link AppUtils}.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
@RunWith(EnroscarTestRunner.class)
public class AppUtilsTest {

  @Test
  public void md5Test() {
    assertEquals("9e107d9d372bb6826bd81d3542a419d6", AppUtils.getMd5("The quick brown fox jumps over the lazy dog"));
    assertEquals("d41d8cd98f00b204e9800998ecf8427e", AppUtils.getMd5(""));
  }

}

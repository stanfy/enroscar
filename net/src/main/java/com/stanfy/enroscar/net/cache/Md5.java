package com.stanfy.enroscar.net.cache;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static com.stanfy.enroscar.io.IoUtils.UTF_8_NAME;

/**
 * Utility for MD5 calculations.
 */
class Md5 {

  /**
   * Calculate 32 bytes length MD5 digest.
   * @param text input text
   * @return MD5 digest
   */
  public static String getMd5(final String text)  {
    try {

      final MessageDigest md = MessageDigest.getInstance("MD5");
      final byte[] utf8Bytes = text.getBytes(UTF_8_NAME);
      md.update(utf8Bytes, 0, utf8Bytes.length);
      final byte[] md5hash = md.digest();
      final int radix = 16;
      final int length = 32;

      final StringBuilder result = new StringBuilder(length).append(new BigInteger(1, md5hash).toString(radix));

      final int zeroBeginLen = length - result.length();
      if (zeroBeginLen > 0) {
        final char [] zeroBegin = new char[zeroBeginLen];
        Arrays.fill(zeroBegin, Character.forDigit(0, radix));
        result.insert(0, zeroBegin);
      }

      return result.toString();

    } catch (final NoSuchAlgorithmException|UnsupportedEncodingException e) {
      throw new AssertionError(e);
    }
  }

  private Md5() {
    throw new UnsupportedOperationException();
  }

}

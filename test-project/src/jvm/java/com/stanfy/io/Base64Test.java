package com.stanfy.io;

import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.util.Random;

import org.junit.Test;

import com.stanfy.test.AbstractEnroscarTest;

/**
 * Tests for {@link Base64}.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class Base64Test extends AbstractEnroscarTest {

  /** Sample. */
  private static final byte[] SAMPLE_BYTES = new byte[] {0x66, 0x6f, 0x6f},
                              SAMPLE_BYTES_ENCODED = new byte[] {0x5a, 0x6d, 0x39, 0x76};
  /** Sample - ASCII. */
  private static final String SAMPLE_STRING = "foo",
                              SAMPLE_STRING_ENCODED = "Zm9v";

  /** Random generator. */
  private final Random rand = new Random();

  private byte[] randomBytes(final int length) {
    final byte[] result = new byte[length];
    rand.nextBytes(result);
    return result;
  }

  private void testEncodeDecode(final int length) throws IOException {
    final byte[] source = randomBytes(length);
    assertThat(source, equalTo(Base64.decode(Base64.encodeBytes(source))));
  }

  @Test
  public void randomTest() throws IOException {
    final int iterationsCount = 10;
    for (int i = 0; i < iterationsCount; i++) {
      final int count8192 = 8192;
      testEncodeDecode(count8192);
      final int count4096 = 4096;
      testEncodeDecode(count4096);
      final int count1025 = 1025;
      testEncodeDecode(count1025);
      final int count33 = 33;
      testEncodeDecode(count33);
      final int count100 = 100;
      testEncodeDecode(count100);
    }
  }

  @Test
  public void sampleTestEncode() {
    assertThat(SAMPLE_BYTES_ENCODED, equalTo(Base64.encodeBytesToBytes(SAMPLE_BYTES)));
    assertThat(SAMPLE_STRING_ENCODED, equalTo(Base64.encodeBytes(SAMPLE_STRING.getBytes())));
  }

  @Test
  public void sampleTestDecode() throws IOException {
    assertThat(SAMPLE_BYTES, equalTo(Base64.decode(SAMPLE_BYTES_ENCODED)));
    assertThat(SAMPLE_STRING, equalTo(new String(Base64.decode(SAMPLE_STRING_ENCODED))));
  }

}

package com.stanfy.enroscar.io.sample;

import com.stanfy.enroscar.io.BuffersPool;
import com.stanfy.enroscar.io.IoUtils;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.fest.assertions.api.Assertions.assertThat;

public class IoSampleTest {

  @Test
  public void sample() throws Exception {
    BuffersPool pool = new BuffersPool();
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    IoUtils.transfer(new ByteArrayInputStream("test value".getBytes()), output, pool);
    assertThat(new String(output.toByteArray(), "UTF-8")).isEqualTo("test value");
  }

}

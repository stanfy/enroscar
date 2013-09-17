package com.stanfy.enroscar.utils.test;

import com.stanfy.enroscar.utils.Time;

import static org.fest.assertions.api.Assertions.*;

import org.junit.Test;

/**
 * Tests for Time.
 */
public class TimeTest {

  @Test
  public void shouldConvertMillis() {
    final long twoDays = 2 * 24 * 60 * 60 * 1000;
    // CHECKSTYLE:OFF
    assertThat(Time.asSeconds(twoDays)).isEqualTo(2 * 24 * 60 * 60);
    assertThat(Time.asMinutes(twoDays)).isEqualTo(2 * 24 * 60);
    assertThat(Time.asHours(twoDays))  .isEqualTo(2 * 24);
    assertThat(Time.asDays(twoDays))   .isEqualTo(2);
    // CHECKSTYLE:ON
  }

}

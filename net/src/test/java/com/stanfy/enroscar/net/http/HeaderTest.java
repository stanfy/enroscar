package com.stanfy.enroscar.net.http;

import com.stanfy.enroscar.net.MockUrlConnection;

import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests for Header.
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
public class HeaderTest {

  /** Header instance. */
  private Header header;

  @Before
  public void create() {
    header = new Header("name", "value");
  }

  @Test
  public void shouldAddRequestPropertyToUrlConnection() {
    MockUrlConnection connection = new MockUrlConnection();
    header.apply(connection);
    assertThat(connection.getRequestProperty("name")).isEqualTo("value");
  }

}

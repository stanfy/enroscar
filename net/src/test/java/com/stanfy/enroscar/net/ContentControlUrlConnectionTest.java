package com.stanfy.enroscar.net;

import com.stanfy.enroscar.rest.EntityTypeToken;

import org.junit.Before;
import org.junit.Test;

import java.net.ContentHandler;
import java.net.URLConnection;

import static org.mockito.Mockito.*;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests for ContentControlUrlConnection.
 */
public class ContentControlUrlConnectionTest {

  /** Connection. */
  private ContentControlUrlConnection connection;
  /** Handler instance. */
  private ContentHandler handler;

  @Before
  public void setup() throws Exception {
    handler = mock(ContentHandler.class);

    connection = new ContentControlUrlConnection(mock(URLConnection.class), handler,
        EntityTypeToken.fromEntityType(ContentControlUrlConnectionTest.class));

    doReturn("test").when(handler).getContent(connection);
  }

  @Test
  public void shouldDelegateToHandler() throws Exception {
    assertThat(connection.getContent()).isEqualTo("test");
    verify(handler).getContent(connection);
  }

}

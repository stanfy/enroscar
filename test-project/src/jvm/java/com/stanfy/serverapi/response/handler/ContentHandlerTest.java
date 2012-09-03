package com.stanfy.serverapi.response.handler;

import static org.hamcrest.Matchers.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ResponseCache;
import java.net.URLConnection;

import org.junit.Test;

import com.google.mockwebserver.MockResponse;
import com.stanfy.app.beans.BeansManager;
import com.stanfy.app.beans.BeansManager.Editor;
import com.stanfy.net.UrlConnectionWrapper;
import com.stanfy.serverapi.request.OperationType;
import com.stanfy.serverapi.request.SimpleRequestBuilder;
import com.stanfy.test.AbstractMockServerTest;
import com.xtremelabs.robolectric.Robolectric;


/**
 * Test for content handlers.
 */
public class ContentHandlerTest extends AbstractMockServerTest {

  @Override
  protected void configureBeansManager(final Editor editor) {
    editor.required().remoteServerApi(SimpleRequestBuilder.STRING);
  }

  private String scheduleBadMethodResponse() {
    final String text = "Bad request";
    getWebServer().enqueue(new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_METHOD).setBody(text));
    return text;
  }

  @Test
  public void gotErrorShourldReadErrorStream() throws Exception {
    assertThat(ResponseCache.getDefault(), is(nullValue()));

    scheduleBadMethodResponse();

    URLConnection connection = makeConnection(
        new MyRequestBuilder<String>(Robolectric.application) { }
          .setUrl(getWebServer().getUrl("/error").toString())
          .setOperationType(OperationType.SIMPLE_GET)
    );

    boolean failed = false;
    try {
      read(connection);
    } catch (final IOException e) {
      failed = true;
    }

    getWebServer().takeRequest();
    assertThat("Not a bad response was scheduled, IOException was not thrown", failed, equalTo(true));

    final String expected = scheduleBadMethodResponse();

    // send request once more
    connection = makeConnection(
        new MyRequestBuilder<String>(Robolectric.application) { }
          .setUrl(getWebServer().getUrl("/error").toString())
          .setOperationType(OperationType.SIMPLE_GET)
    );

    final StringContentHandler contentHandler = (StringContentHandler) BeansManager.get(getApplication()).getContentHandler(StringContentHandler.BEAN_NAME);
    // we should test base behavior
    assertThat(contentHandler, is(instanceOf(BaseContentHandler.class)));
    final String response = (String) contentHandler.getContent(connection);
    getWebServer().takeRequest();

    final HttpURLConnection http = (HttpURLConnection)UrlConnectionWrapper.unwrap(connection);
    assertThat(http.getResponseCode(), equalTo(HttpURLConnection.HTTP_BAD_METHOD));
    assertThat(response, equalTo(expected));
  }

}

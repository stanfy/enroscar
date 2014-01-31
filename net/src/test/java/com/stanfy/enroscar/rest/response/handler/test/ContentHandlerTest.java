package com.stanfy.enroscar.rest.response.handler.test;

import android.os.Build;

import com.google.mockwebserver.MockResponse;
import com.stanfy.enroscar.beans.BeansManager;
import com.stanfy.enroscar.beans.BeansManager.Editor;
import com.stanfy.enroscar.io.BuffersPool;
import com.stanfy.enroscar.net.UrlConnectionBuilderFactory;
import com.stanfy.enroscar.net.UrlConnectionWrapper;
import com.stanfy.enroscar.net.test.AbstractMockServerTest;
import com.stanfy.enroscar.rest.RemoteServerApiConfiguration;
import com.stanfy.enroscar.rest.request.OperationType;
import com.stanfy.enroscar.rest.request.net.BaseRequestDescriptionConverter;
import com.stanfy.enroscar.rest.response.handler.BaseContentHandler;
import com.stanfy.enroscar.rest.response.handler.StringContentHandler;

import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ResponseCache;
import java.net.URLConnection;

import static org.fest.assertions.api.Assertions.assertThat;


/**
 * Test for content handlers.
 */
@Config(emulateSdk = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class ContentHandlerTest extends AbstractMockServerTest {

  @Override
  protected void configureBeansManager(final Editor editor) {
    editor
      .put(BuffersPool.class)
      .put(RemoteServerApiConfiguration.class)
      .put(BaseRequestDescriptionConverter.CONNECTION_BUILDER_FACTORY_NAME, UrlConnectionBuilderFactory.DEFAULT)
      .put(StringContentHandler.class);
  }

  private String scheduleBadMethodResponse() {
    final String text = "Bad request";
    getWebServer().enqueue(new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_METHOD).setBody(text));
    return text;
  }

  @Test
  public void gotErrorShourldReadErrorStream() throws Exception {
    assertThat(ResponseCache.getDefault()).isNull();

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
    
    assertThat(failed).isTrue();

    final String expected = scheduleBadMethodResponse();

    // send request once more
    connection = makeConnection(
        new MyRequestBuilder<String>(Robolectric.application) {
          { setRequestContentHandler(StringContentHandler.BEAN_NAME); }
        }
          .setUrl(getWebServer().getUrl("/error").toString())
          .setOperationType(OperationType.SIMPLE_GET)
    );

    final StringContentHandler contentHandler = BeansManager.get(getApplication())
        .getContainer().getBean(StringContentHandler.BEAN_NAME, StringContentHandler.class);
    // we should test base behavior
    assertThat(contentHandler).isInstanceOf(BaseContentHandler.class);
    final String response = (String) contentHandler.getContent(connection);
    getWebServer().takeRequest();

    final HttpURLConnection http = (HttpURLConnection)UrlConnectionWrapper.unwrap(connection);
    assertThat(http.getResponseCode()).isEqualTo(HttpURLConnection.HTTP_BAD_METHOD);
    assertThat(response).isEqualTo(expected);
  }

}

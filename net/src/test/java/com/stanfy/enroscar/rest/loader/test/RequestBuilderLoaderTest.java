package com.stanfy.enroscar.rest.loader.test;

import android.support.v4.content.Loader;

import com.google.mockwebserver.MockResponse;
import com.google.mockwebserver.RecordedRequest;
import com.stanfy.enroscar.beans.BeansManager.Editor;
import com.stanfy.enroscar.content.loader.ResponseData;
import com.stanfy.enroscar.rest.response.handler.StringContentHandler;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests for {@link com.stanfy.enroscar.rest.loader.RequestBuilderLoader}.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public class RequestBuilderLoaderTest extends AbstractLoaderTest {

  @Override
  protected void configureBeansManager(final Editor editor) {
    super.configureBeansManager(editor);
    editor.put(StringContentHandler.class);
  }

  @Test
  public void doLoadShouldDeliverResults() throws Throwable {
    final String text = "doLoadShouldDeliverResults";
    getWebServer().enqueue(new MockResponse().setBody(text));

    final Loader<ResponseData<String>> loader = new MyRequestBuilder<String>(getApplication()) { }
      .setUrl(getWebServer().getUrl("/").toString())
      .setFormat(StringContentHandler.BEAN_NAME)
      .getLoader();

    loader.startLoading();

    waitAndAssertForLoader(loader, new Asserter<ResponseData<String>>() {
      @Override
      public void makeAssertions(final ResponseData<String> data) throws Exception {
        assertThat(data).isNotNull();
        assertThat(data.getModel()).isEqualTo(text);
      }
    });
  }

  @Test
  public void headersTest() throws Throwable {
    final String meta = "meta";
    getWebServer().enqueue(new MockResponse().setBody("headersTest"));

    @SuppressWarnings("deprecation")
    final Loader<ResponseData<String>> loader = new MyRequestBuilder<String>(getApplication()) { }
        .setUrl(getWebServer().getUrl("/").toString())
        .setFormat(StringContentHandler.BEAN_NAME)
        .addHeader("test", "value")
        .getLoader();

    loader.startLoading();

    waitAndAssertForLoader(loader, new Asserter<ResponseData<String>>() {
      @Override
      public void makeAssertions(final ResponseData<String> data) throws Exception {
        RecordedRequest request = getWebServer().takeRequest();
        assertThat(request.getHeader("test")).isEqualTo("value");
      }
    });
  }

}

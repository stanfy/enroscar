package com.stanfy.app.loader;

import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

import com.google.mockwebserver.MockResponse;
import com.stanfy.app.beans.BeansManager.Editor;
import com.stanfy.serverapi.request.SimpleRequestBuilder;
import com.stanfy.serverapi.response.ResponseData;
import com.stanfy.serverapi.response.handler.StringContentHandler;
import com.stanfy.test.AbstractApplicationServiceTest;

/**
 * Tests for {@link RequestBuilderLoader}.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public class RequestBuilderLoaderTest extends AbstractApplicationServiceTest {

  @Override
  protected void configureBeansManager(final Editor editor) {
    editor.required().remoteServerApi();
    editor.put(StringContentHandler.class);
  }

  @Test
  public void doLoadShouldDeliverResults() throws Exception {
    final String text = "doLoadShouldDeliverResults";
    getWebServer().enqueue(new MockResponse().setBody(text));

    final RequestBuilderLoader<String> loader = new SimpleRequestBuilder<String>(getApplication()) { }
        .setUrl(getWebServer().getUrl("/").toString())
        .setFormat(StringContentHandler.BEAN_NAME)
        .getLoader();

    loader.startLoading();

    waitAndAssertForLoader(loader, new Asserter<ResponseData<String>>() {
      @Override
      public void makeAssertions(final ResponseData<String> data) throws Exception {
        assertThat(data.getModel(), equalTo(text));
      }
    });
  }

}

package com.stanfy.app.loader;

import static org.hamcrest.Matchers.*;

import org.junit.Test;

import android.content.Context;

import com.google.mockwebserver.MockResponse;
import com.stanfy.app.beans.BeansManager.Editor;
import com.stanfy.serverapi.request.RequestDescription;
import com.stanfy.serverapi.response.ContentAnalyzer;
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
    editor.put("analyzer", new Analyzer());
  }

  @Test
  public void doLoadShouldDeliverResults() throws Throwable {
    final String text = "doLoadShouldDeliverResults";
    getWebServer().enqueue(new MockResponse().setBody(text));

    final RequestBuilderLoader<String> loader = new MyRequestBuilder<String>(getApplication()) { }
      .setStartedLoader(true)
      .setUrl(getWebServer().getUrl("/").toString())
      .setFormat(StringContentHandler.BEAN_NAME)
      .getLoader();

    directLoaderCall(loader).startLoading();

    waitAndAssertForLoader(loader, new Asserter<ResponseData<String>>() {
      @Override
      public void makeAssertions(final ResponseData<String> data) throws Exception {
        assertThat(data, is(notNullValue()));
        assertThat(data.getModel(), equalTo(text));
      }
    });
  }

  @Test
  public void metaInformationTest() throws Throwable {
    final String meta = "meta";
    getWebServer().enqueue(new MockResponse().setBody("doLoadShouldDeliverResults"));

    final RequestBuilderLoader<String> loader = new MyRequestBuilder<String>(getApplication()) { }
        .setStartedLoader(true)
        .setUrl(getWebServer().getUrl("/").toString())
        .setFormat(StringContentHandler.BEAN_NAME)
        .setMetaInfo(meta, meta)
        .setContentAnalyzer("analyzer")
        .getLoader();

    directLoaderCall(loader).startLoading();

    waitAndAssertForLoader(loader, new Asserter<ResponseData<String>>() {
      @Override
      public void makeAssertions(final ResponseData<String> data) throws Exception {
        assertThat(data.getModel(), equalTo(meta));
      }
    });
  }

  /**
   * @author Olexandr Tereshchuk (Stanfy - http://www.stanfy.com)
   */
  public static class Analyzer implements ContentAnalyzer<String, String> {

    @Override
    public ResponseData<String> analyze(final Context context,
        final RequestDescription description, final ResponseData<String> responseData) {
      if (description.hasMetaInfo("meta")) {
        responseData.setModel(description.getMetaInfo("meta").toString());
      }
      return responseData;
    }

  }


}

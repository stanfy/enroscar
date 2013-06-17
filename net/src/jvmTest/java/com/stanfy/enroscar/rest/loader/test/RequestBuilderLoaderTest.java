package com.stanfy.enroscar.rest.loader.test;

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.Test;

import android.content.Context;
import android.support.v4.content.Loader;

import com.google.mockwebserver.MockResponse;
import com.stanfy.enroscar.beans.BeansManager.Editor;
import com.stanfy.enroscar.content.loader.ResponseData;
import com.stanfy.enroscar.rest.request.RequestDescription;
import com.stanfy.enroscar.rest.response.ContentAnalyzer;
import com.stanfy.enroscar.rest.response.handler.StringContentHandler;

/**
 * Tests for {@link com.stanfy.enroscar.rest.loader.RequestBuilderLoader}.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public class RequestBuilderLoaderTest extends AbstractLoaderTest {

  @Override
  protected void configureBeansManager(final Editor editor) {
    super.configureBeansManager(editor);
    editor.put(StringContentHandler.class);
    editor.put("analyzer", new Analyzer());
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
  public void metaInformationTest() throws Throwable {
    final String meta = "meta";
    getWebServer().enqueue(new MockResponse().setBody("doLoadShouldDeliverResults"));

    @SuppressWarnings("deprecation")
    final Loader<ResponseData<String>> loader = new MyRequestBuilder<String>(getApplication()) { }
        .setUrl(getWebServer().getUrl("/").toString())
        .setFormat(StringContentHandler.BEAN_NAME)
        .setMetaInfo(meta, meta)
        .setContentAnalyzer("analyzer")
        .getLoader();

    loader.startLoading();

    waitAndAssertForLoader(loader, new Asserter<ResponseData<String>>() {
      @Override
      public void makeAssertions(final ResponseData<String> data) throws Exception {
        assertThat(data.getModel()).isEqualTo(meta);
      }
    });
  }

  /**
   * @author Olexandr Tereshchuk (Stanfy - http://www.stanfy.com)
   */
  public static class Analyzer implements ContentAnalyzer<String, String> {

    @SuppressWarnings("deprecation")
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

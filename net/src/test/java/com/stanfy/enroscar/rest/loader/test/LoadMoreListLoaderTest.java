package com.stanfy.enroscar.rest.loader.test;

import android.content.Context;
import android.support.v4.content.Loader;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.RecordedRequest;
import com.stanfy.enroscar.beans.BeansManager;
import com.stanfy.enroscar.content.loader.ResponseData;
import com.stanfy.enroscar.net.operation.ListRequestBuilderWrapper;
import com.stanfy.enroscar.net.operation.RequestDescription;
import com.stanfy.enroscar.rest.loader.LoadMoreListLoader;
import com.stanfy.enroscar.rest.loader.LoadMoreListLoader.ValueIncrementor;
import com.stanfy.enroscar.rest.response.ContentAnalyzer;
import com.stanfy.enroscar.rest.response.handler.StringContentHandler;

import org.assertj.core.util.introspection.FieldUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link LoadMoreListLoader}.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
@Config(emulateSdk = 18)
public class LoadMoreListLoaderTest extends AbstractLoaderTest {

  /** Analyzer. */
  private static final ContentAnalyzer<String, List<String>> STRING_LIST_ANALYZER = new ContentAnalyzer<String, List<String>>() {
    @Override
    public ResponseData<List<String>> analyze(final Context context, final RequestDescription description, final ResponseData<String> responseData) { //} throws RequestMethodException {
      final ResponseData<List<String>> result = new ResponseData<List<String>>(responseData, null);
      if (responseData.getModel() != null) {
        final ArrayList<String> list = new ArrayList<String>();
        list.add(responseData.getModel());
//        result.setEntity(list);
      }
      return result;
    }
  };

  @Override
  protected void configureBeansManager(final BeansManager.Editor editor) {
    super.configureBeansManager(editor);
    editor.put(StringContentHandler.class).put("sa", STRING_LIST_ANALYZER);
  }

  @Override
  protected void whenBeansConfigured() {
    super.whenBeansConfigured();
    initContentHandler(StringContentHandler.BEAN_NAME);
  }
  
  private ListRequestBuilderWrapper<List<String>, String> createListRb() throws IOException {
    return new MyRequestBuilder<List<String>>(getApplication()) { }
      .setUrl(getWebServer().getUrl("/").toString())
      .setFormat("string")
      .setContentAnalyzer("sa")
      .<String, List<String>>asLoadMoreList("o", "l");
  }

  private void makeAssert(final Loader<ResponseData<List<String>>> loader, final String response, final Asserter<List<String>> asserter) throws Throwable {
    assertWithLoader(loader, new Asserter<ResponseData<List<String>>>() {
      @Override
      public void makeAssertions(ResponseData<List<String>> data) throws Exception {
        System.out.println(data.getModel());
        assertThat(data.isSuccessful()).isTrue();
        assertThat(data.getModel().get(data.getModel().size() - 1)).isEqualTo(response);
        try {
          asserter.makeAssertions(data.getModel());
        } catch (Exception e) {
          throw new AssertionError(e);
        }
      }
    });
  }

  @Test @Ignore // FIXME: tests with Robolectric
  public void firstRequestWithoutOffsetLimit() throws Throwable {
    getWebServer().enqueue(new MockResponse().setBody("L1"));

    final Loader<ResponseData<List<String>>> loader = createListRb().getLoader();

    loader.startLoading();
    makeAssert(loader, "L1", new Asserter<List<String>>() {
      @Override
      public void makeAssertions(final List<String> data) throws Exception {
        final RecordedRequest request = getWebServer().takeRequest();
        final String path = request.getPath();
        assertThat(path).doesNotContain("o=");
        assertThat(path).doesNotContain("l=");
      }
    });
  }



}

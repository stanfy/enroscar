package com.stanfy.enroscar.rest.loader.test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.reflect.core.Reflection.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import org.robolectric.annotation.Config;

import android.content.Context;
import android.support.v4.content.Loader;

import com.google.mockwebserver.MockResponse;
import com.google.mockwebserver.RecordedRequest;
import com.stanfy.enroscar.beans.BeansManager;
import com.stanfy.enroscar.content.loader.ResponseData;
import com.stanfy.enroscar.rest.RequestMethod.RequestMethodException;
import com.stanfy.enroscar.rest.loader.LoadMoreListLoader;
import com.stanfy.enroscar.rest.loader.LoadMoreListLoader.ValueIncrementor;
import com.stanfy.enroscar.rest.request.ListRequestBuilderWrapper;
import com.stanfy.enroscar.rest.request.RequestDescription;
import com.stanfy.enroscar.rest.response.ContentAnalyzer;
import com.stanfy.enroscar.rest.response.handler.StringContentHandler;

/**
 * Tests for {@link LoadMoreListLoader}.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
@Config(emulateSdk = 18)
public class LoadMoreListLoaderTest extends AbstractLoaderTest {

  /** Analyzer. */
  private static final ContentAnalyzer<String, List<String>> STRING_LIST_ANALYZER = new ContentAnalyzer<String, List<String>>() {
    @Override
    public ResponseData<List<String>> analyze(final Context context, final RequestDescription description, final ResponseData<String> responseData) throws RequestMethodException {
      final ResponseData<List<String>> result = new ResponseData<List<String>>(responseData);
      if (responseData.getModel() != null) {
        final ArrayList<String> list = new ArrayList<String>();
        list.add(responseData.getModel());
        result.setModel(list);
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

  private void makeAsyncAssert(final Loader<ResponseData<List<String>>> loader, final String response, final Asserter<List<String>> asserter) throws Throwable {
    waitAndAssertForLoader(loader, new Asserter<ResponseData<List<String>>>() {
      @Override
      public void makeAssertions(final ResponseData<List<String>> data) throws Exception {
        if (data == null) { throw new IllegalStateException("null data"); }
        System.out.println(data.getModel());
        assertThat(data.isSuccessful()).isTrue();
        assertThat(data.getModel().get(data.getModel().size() - 1)).isEqualTo(response);

        asserter.makeAssertions(data.getModel());

      }
    });
  }

  @Test
  public void firstRequestWithoutOffsetLimit() throws Throwable {
    getWebServer().enqueue(new MockResponse().setBody("L1"));

    final Loader<ResponseData<List<String>>> loader = createListRb().getLoader();

    loader.startLoading();
    makeAsyncAssert(loader, "L1", new Asserter<List<String>>() {
      @Override
      public void makeAssertions(final List<String> data) throws Exception {
        final RecordedRequest request = getWebServer().takeRequest();
        final String path = request.getPath();
        assertThat(path).doesNotContain("o=");
        assertThat(path).doesNotContain("l=");
      }
    });
  }

  @Test
  public void firstRequestWithCustomOffsetLimit() throws Throwable {
    getWebServer().enqueue(new MockResponse().setBody("LCustom"));

    final Loader<ResponseData<List<String>>> loader = createListRb().setLimit(2).setOffset(1).getLoader();

    loader.startLoading();
    makeAsyncAssert(loader, "LCustom", new Asserter<List<String>>() {
      @Override
      public void makeAssertions(final List<String> data) throws Exception {
        final RecordedRequest request = getWebServer().takeRequest();
        final String path = request.getPath();
        assertThat(path).contains("o=1");
        assertThat(path).contains("l=2");
      }
    });
  }

  @Test
  public void nextRequestShouldIncrementOffsetLimit() throws Throwable {
    final int limit = 3, offset = 2;

    @SuppressWarnings("unchecked")
    final LoadMoreListLoader<String, List<String>> loader = (LoadMoreListLoader<String, List<String>>) createListRb().setLimit(limit).setOffset(offset).getLoader();

    field("mReset").ofType(boolean.class).in(loader).set(false);
    field("mStarted").ofType(boolean.class).in(loader).set(true);
    
    getWebServer().enqueue(new MockResponse().setBody("LNext"));
    loader.forceLoadMore();
    makeAsyncAssert(loader, "LNext", new Asserter<List<String>>() {
      @Override
      public void makeAssertions(final List<String> data) throws Exception {
        final RecordedRequest request = getWebServer().takeRequest();
        final String path = request.getPath();
        assertThat(path).contains("o=" + (offset + 1));
        assertThat(path).contains("l=" + limit);
      }
    });
  }

  @Test
  public void nextRequestShouldIncrementOffsetLimitCustomType() throws Throwable {
    final String limit = "abcd", offset = "dbca", nextLimit = "abc", nextOffset = "bdc";

    final ValueIncrementor oInc = new ValueIncrementor() {
      @Override
      public String nextValue(final String currentValue, final int lastLoadedCount, final int currentCount) {
        assertThat(currentValue).isEqualTo(offset);
        return nextOffset;
      }
    };
    final ValueIncrementor lInc = new ValueIncrementor() {
      @Override
      public String nextValue(final String currentValue, final int lastLoadedCount, final int currentCount) {
        assertThat(currentValue).isEqualTo(limit);
        return nextLimit;
      }
    };

    @SuppressWarnings("unchecked")
    final LoadMoreListLoader<String, List<String>> loader =
        ((LoadMoreListLoader<String, List<String>>)createListRb().setLimit(limit).setOffset(offset).getLoader())
        .setLimitIncrementor(lInc).setOffsetIncrementor(oInc);

    field("mReset").ofType(boolean.class).in(loader).set(false);
    field("mStarted").ofType(boolean.class).in(loader).set(true);
    
    getWebServer().enqueue(new MockResponse().setBody("LNextCustom"));
    loader.forceLoadMore();
    makeAsyncAssert(loader, "LNextCustom", new Asserter<List<String>>() {
      @Override
      public void makeAssertions(final List<String> data) throws Exception {
        final RecordedRequest request = getWebServer().takeRequest();
        final String path = request.getPath();
        assertThat(path).contains("o=" + nextOffset);
        assertThat(path).contains("l=" + nextLimit);
      }
    });
  }

}

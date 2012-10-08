package com.stanfy.app.loader;

import static org.hamcrest.Matchers.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import android.content.Context;
import android.support.v4.content.Loader;

import com.google.mockwebserver.MockResponse;
import com.google.mockwebserver.RecordedRequest;
import com.stanfy.app.beans.BeansManager;
import com.stanfy.app.loader.LoadMoreListLoader.ValueIncrementor;
import com.stanfy.serverapi.RequestMethod.RequestMethodException;
import com.stanfy.serverapi.request.ListRequestBuilderWrapper;
import com.stanfy.serverapi.request.RequestDescription;
import com.stanfy.serverapi.response.ContentAnalyzer;
import com.stanfy.serverapi.response.ResponseData;
import com.stanfy.test.AbstractApplicationServiceTest;

/**
 * Tests for {@link LoadMoreListLoader}.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public class LoadMoreListLoaderTest extends AbstractApplicationServiceTest {

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
    editor.required().remoteServerApi("string").put("sa", STRING_LIST_ANALYZER);
  }

  private ListRequestBuilderWrapper<List<String>, String> createListRb() throws IOException {
    return new MyRequestBuilder<List<String>>(getApplication()) { }
      .setStartedLoader(true)
      .setUrl(getWebServer().getUrl("/").toString())
      .setFormat("string")
      .setContentAnalyzer("sa")
      .<String, List<String>>asLoadMoreList("o", "l");
  }

  private void makeAsyncAssert(final Loader<ResponseData<List<String>>> loader, final String response, final Asserter<List<String>> asserter) throws Throwable {
    waitAndAssertForLoader(loader, new Asserter<ResponseData<List<String>>>() {
      @Override
      public void makeAssertions(final ResponseData<List<String>> data) throws Exception {
        assertThat(data.getModel().get(0), equalTo(response));

        asserter.makeAssertions(data.getModel());

      }
    });
  }

  @Test
  public void firstRequestWithoutOffsetLimit() throws Throwable {
    getWebServer().enqueue(new MockResponse().setBody("L1"));

    final Loader<ResponseData<List<String>>> loader = createListRb().getLoader();

    directLoaderCall(loader).startLoading();

    makeAsyncAssert(loader, "L1", new Asserter<List<String>>() {
      @Override
      public void makeAssertions(final List<String> data) throws Exception {
        final RecordedRequest request = getWebServer().takeRequest();
        final String path = request.getPath();
        assertThat(path, not(containsString("o=")));
        assertThat(path, not(containsString("l=")));
      }
    });
  }

  @Test
  public void firstRequestWithCustomOffsetLimit() throws Throwable {
    getWebServer().enqueue(new MockResponse().setBody("LCustom"));

    final Loader<ResponseData<List<String>>> loader = createListRb().setLimit(2).setOffset(1).getLoader();

    directLoaderCall(loader).startLoading();

    makeAsyncAssert(loader, "LCustom", new Asserter<List<String>>() {
      @Override
      public void makeAssertions(final List<String> data) throws Exception {
        final RecordedRequest request = getWebServer().takeRequest();
        final String path = request.getPath();
        assertThat(path, containsString("o=1"));
        assertThat(path, containsString("l=2"));
      }
    });
  }

  @Test
  public void nextRequestShouldIncrementOffsetLimit() throws Throwable {
    getWebServer().enqueue(new MockResponse().setBody("LNext"));

    final int limit = 3, offset = 2;

    final Loader<ResponseData<List<String>>> loader = createListRb().setLimit(limit).setOffset(offset).getLoader();

    ((LoadmoreLoader)directLoaderCall(loader)).forceLoadMore();

    makeAsyncAssert(loader, "LNext", new Asserter<List<String>>() {
      @Override
      public void makeAssertions(final List<String> data) throws Exception {
        final RecordedRequest request = getWebServer().takeRequest();
        final String path = request.getPath();
        assertThat(path, containsString("o=" + (offset + 1)));
        assertThat(path, containsString("l=" + limit));
      }
    });
  }

  @Test
  public void nextRequestShouldIncrementOffsetLimitCustomType() throws Throwable {
    getWebServer().enqueue(new MockResponse().setBody("LNextCustom"));

    final String limit = "abcd", offset = "dbca", nextLimit = "abc", nextOffset = "bdc";

    final ValueIncrementor oInc = new ValueIncrementor() {
      @Override
      public String nextValue(final String currentValue, final int lastLoadedCount, final int currentCount) {
        assertThat(currentValue, equalTo(offset));
        return nextOffset;
      }
    };
    final ValueIncrementor lInc = new ValueIncrementor() {
      @Override
      public String nextValue(final String currentValue, final int lastLoadedCount, final int currentCount) {
        assertThat(currentValue, equalTo(limit));
        return nextLimit;
      }
    };

    @SuppressWarnings("unchecked")
    final LoadMoreListLoader<String, List<String>> loader =
        ((LoadMoreListLoader<String, List<String>>)createListRb().setLimit(limit).setOffset(offset).getLoader())
        .setLimitIncrementor(lInc).setOffsetIncrementor(oInc);

    directLoaderCall(loader).forceLoadMore();

    makeAsyncAssert(loader, "LNextCustom", new Asserter<List<String>>() {
      @Override
      public void makeAssertions(final List<String> data) throws Exception {
        final RecordedRequest request = getWebServer().takeRequest();
        final String path = request.getPath();
        assertThat(path, containsString("o=" + nextOffset));
        assertThat(path, containsString("l=" + nextLimit));
      }
    });
  }

}

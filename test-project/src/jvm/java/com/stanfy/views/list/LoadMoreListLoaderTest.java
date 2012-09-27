package com.stanfy.views.list;

import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.TextView;

import com.google.mockwebserver.MockResponse;
import com.stanfy.app.beans.BeansManager;
import com.stanfy.app.fragments.list.RequestBuilderListFragment;
import com.stanfy.app.loader.LoadMoreListLoader;
import com.stanfy.content.UniqueObject;
import com.stanfy.enroscar.test.R;
import com.stanfy.serverapi.RequestMethod.RequestMethodException;
import com.stanfy.serverapi.request.ListRequestBuilderWrapper;
import com.stanfy.serverapi.request.RequestBuilder;
import com.stanfy.serverapi.request.RequestDescription;
import com.stanfy.serverapi.response.ContentAnalyzer;
import com.stanfy.serverapi.response.ResponseData;
import com.stanfy.test.AbstractApplicationServiceTest;
import com.stanfy.views.list.ModelListAdapter.ElementRenderer;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.shadows.ShadowActivity;

/**
 * Tests for {@link LoadMoreListLoader}.
 */
@SuppressWarnings("unused")
public class LoadMoreListLoaderTest extends AbstractApplicationServiceTest {

  /** Analyzer. */
  private static final ContentAnalyzer<String, ModelsList> STRING_LIST_ANALYZER = new ContentAnalyzer<String, ModelsList>() {
    @Override
    public ResponseData<ModelsList> analyze(final Context context, final RequestDescription description, final ResponseData<String> responseData) throws RequestMethodException {
      final ResponseData<ModelsList> result = new ResponseData<ModelsList>(responseData);
      if (responseData.getModel() != null) {
        final ModelsList list = new ModelsList();
        if (responseData.getModel().length() > 0) {
          final Model m = new Model();
          m.data = responseData.getModel();
          list.add(m);
        }
        result.setModel(list);
      }
      return result;
    }
  };

  @Override
  protected void configureBeansManager(final BeansManager.Editor editor) {
    editor.required().remoteServerApi("string").put("sa", STRING_LIST_ANALYZER);
  }

  private ListRequestBuilderWrapper<ModelsList, Model> createListRb() throws IOException {
    return new MyRequestBuilder<ModelsList>(getApplication()) {
      {
        setModelType(String.class);
      }
    }
    .setStartedLoader(true)
    .setUrl(getWebServer().getUrl("/").toString())
    .setFormat("string")
    .setContentAnalyzer("sa")
    .<Model, ModelsList>asLoadMoreList("o", "l");
  }

  private void makeAsyncAssert(final LoadMoreListLoader<String, List<String>> loader, final String response, final Asserter<List<String>> asserter) throws Throwable {
    waitAndAssertForLoader(loader, new Asserter<ResponseData<List<String>>>() {
      @Override
      public void makeAssertions(final ResponseData<List<String>> data) throws Exception {
        assertThat(data.getModel().get(0), equalTo(response));

        asserter.makeAssertions(data.getModel());

      }
    });
  }

  @Test
  public void testLoadMore() throws Throwable {
    getWebServer().enqueue(new MockResponse().setBody("L1"));
    getWebServer().enqueue(new MockResponse().setBody("L2"));
    getWebServer().enqueue(new MockResponse().setBody(""));

    final ShadowActivity a = Robolectric.shadowOf(new TestActivity());
    a.onCreate(null);

    //    Robolectric.visualize(view)

    //    final FetchableListView lv = new FetchableListView(null);
  }

  public final class TestActivity extends FragmentActivity {
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main_content);
      getSupportFragmentManager().beginTransaction().replace(android.R.id.content, new RBFrargment()).commitAllowingStateLoss();
    }

  }

  public static final class Model implements UniqueObject {

    String data = null;

    @Override
    public long getId() { return 0; }

  }

  public static final class ModelsList extends ArrayList<Model> {

  }

  public final class RBFrargment extends RequestBuilderListFragment<Model, ModelsList> {

    @Override
    protected RequestBuilder<ModelsList> createRequestBuilder() {
      try {
        return new MyRequestBuilder<ModelsList>(getActivity()) {
          {
            setModelType(String.class);
          }
        }
        .setStartedLoader(true)
        .setUrl(getWebServer().getUrl("/").toString())
        .setFormat("string")
        .setContentAnalyzer("sa");
      } catch (final MalformedURLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (final UnknownHostException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      return null;
    }

    @Override
    protected ElementRenderer<Model> createRenderer() {
      return new ElementRenderer<Model>(android.R.layout.simple_list_item_1) {
        @Override
        public void render(final Adapter adapter, final ViewGroup parent, final Model element,
            final View view, final Object holder, final int position) {
          ((TextView)view).setText(element.data);
        }
      };
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
      super.onActivityCreated(savedInstanceState);
      startLoad();
    }

  }


  //  @Test
  //  public void firstRequestWithoutOffsetLimit() throws Throwable {
  //    getWebServer().enqueue(new MockResponse().setBody("L1"));
  //
  //    final LoadMoreListLoader<String, List<String>> loader = createListRb().getLoader();
  //
  //    directLoaderCall(loader).startLoading();
  //
  //    makeAsyncAssert(loader, "L1", new Asserter<List<String>>() {
  //      @Override
  //      public void makeAssertions(final List<String> data) throws Exception {
  //        final RecordedRequest request = getWebServer().takeRequest();
  //        final String path = request.getPath();
  //        assertThat(path, not(containsString("o=")));
  //        assertThat(path, not(containsString("l=")));
  //      }
  //    });
  //  }
  //
  //  @Test
  //  public void firstRequestWithCustomOffsetLimit() throws Throwable {
  //    getWebServer().enqueue(new MockResponse().setBody("LCustom"));
  //
  //    final LoadMoreListLoader<String, List<String>> loader = createListRb().setLimit(2).setOffset(1).getLoader();
  //
  //    directLoaderCall(loader).startLoading();
  //
  //    makeAsyncAssert(loader, "LCustom", new Asserter<List<String>>() {
  //      @Override
  //      public void makeAssertions(final List<String> data) throws Exception {
  //        final RecordedRequest request = getWebServer().takeRequest();
  //        final String path = request.getPath();
  //        assertThat(path, containsString("o=1"));
  //        assertThat(path, containsString("l=2"));
  //      }
  //    });
  //  }
  //
  //  @Test
  //  public void nextRequestShouldIncrementOffsetLimit() throws Throwable {
  //    getWebServer().enqueue(new MockResponse().setBody("LNext"));
  //
  //    final int limit = 3, offset = 2;
  //
  //    final LoadMoreListLoader<String, List<String>> loader = createListRb().setLimit(limit).setOffset(offset).getLoader();
  //
  //    directLoaderCall(loader).forceLoadMore();
  //
  //    makeAsyncAssert(loader, "LNext", new Asserter<List<String>>() {
  //      @Override
  //      public void makeAssertions(final List<String> data) throws Exception {
  //        final RecordedRequest request = getWebServer().takeRequest();
  //        final String path = request.getPath();
  //        assertThat(path, containsString("o=" + (offset + 1)));
  //        assertThat(path, containsString("l=" + limit));
  //      }
  //    });
  //  }
  //
  //  @Test
  //  public void nextRequestShouldIncrementOffsetLimitCustomType() throws Throwable {
  //    getWebServer().enqueue(new MockResponse().setBody("LNextCustom"));
  //
  //    final String limit = "abcd", offset = "dbca", nextLimit = "abc", nextOffset = "bdc";
  //
  //    final ValueIncrementor oInc = new ValueIncrementor() {
  //      @Override
  //      public String nextValue(final String currentValue, final int lastLoadedCount, final int currentCount) {
  //        assertThat(currentValue, equalTo(offset));
  //        return nextOffset;
  //      }
  //    };
  //    final ValueIncrementor lInc = new ValueIncrementor() {
  //      @Override
  //      public String nextValue(final String currentValue, final int lastLoadedCount, final int currentCount) {
  //        assertThat(currentValue, equalTo(limit));
  //        return nextLimit;
  //      }
  //    };
  //
  //    final LoadMoreListLoader<String, List<String>> loader = createListRb().setLimit(limit).setOffset(offset)
  //        .getLoader().setLimitIncrementor(lInc).setOffsetIncrementor(oInc);
  //
  //    directLoaderCall(loader).forceLoadMore();
  //
  //    makeAsyncAssert(loader, "LNextCustom", new Asserter<List<String>>() {
  //      @Override
  //      public void makeAssertions(final List<String> data) throws Exception {
  //        final RecordedRequest request = getWebServer().takeRequest();
  //        final String path = request.getPath();
  //        assertThat(path, containsString("o=" + nextOffset));
  //        assertThat(path, containsString("l=" + nextLimit));
  //      }
  //    });
  //  }

}

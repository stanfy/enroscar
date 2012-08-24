package com.stanfy.app.loader;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.mockwebserver.MockResponse;
import com.stanfy.app.beans.BeansManager.Editor;
import com.stanfy.serverapi.request.SimpleRequestBuilder;
import com.stanfy.serverapi.response.ResponseData;
import com.stanfy.test.AbstractApplicationServiceTest;

/**
 * Test how JSON is parsed.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public class JsonRequestTest extends AbstractApplicationServiceTest {

  /** Test model. */
  public static class MyModel {
    /** Field. */
    String text;
    /** Field. */
    int id;

    @Override
    public boolean equals(final Object o) {
      final MyModel m = (MyModel)o;
      return m.text.equals(text) && m.id == id;
    }

    @Override
    public int hashCode() {
      return id;
    }
  }

  /** Format. */
  String format;

  @Before
  public void setFormat() {
    format = "json";
  }

  @Override
  protected void configureBeansManager(final Editor editor) {
    editor.required().remoteServerApi("json");
  }

  protected void enqueueResponse(final MyModel model) {
    final Gson gson = new GsonBuilder().create();
    final String text = gson.toJson(model);
    assertThat(text, containsString("shouldParseJson"));
    getWebServer().enqueue(new MockResponse().setBody(text));
  }

  @Test
  public void shouldParse() throws Exception {
    final MyModel model = new MyModel();
    model.id = 3;
    model.text = "shouldParseJson";

    enqueueResponse(model);

    final RequestBuilderLoader<MyModel> loader = new SimpleRequestBuilder<MyModel>(getApplication()) { }
        .setUrl(getWebServer().getUrl("/").toString())
        .setFormat(format)
        .getLoader();

    directLoaderCall(loader).startLoading();

    waitAndAssertForLoader(loader, new Asserter<ResponseData<MyModel>>() {
      @Override
      public void makeAssertions(final ResponseData<MyModel> data) throws Exception {
        assertThat(data.getModel(), equalTo(model));
      }
    });
  }

}

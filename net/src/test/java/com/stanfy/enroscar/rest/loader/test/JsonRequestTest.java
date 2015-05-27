package com.stanfy.enroscar.rest.loader.test;


import android.support.v4.content.Loader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.stanfy.enroscar.beans.BeansManager.Editor;
import com.stanfy.enroscar.content.loader.ResponseData;
import com.stanfy.enroscar.rest.response.handler.GsonContentHandler;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test how JSON is parsed.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
@Config(emulateSdk = 18)
public class JsonRequestTest extends AbstractLoaderTest {

  /** Format. */
  String format;

  @Before
  public void setFormat() {
    format = "json";
  }

  @Override
  protected void configureBeansManager(final Editor editor) {
    super.configureBeansManager(editor);
    editor.put(GsonContentHandler.class);
  }

  @Override
  protected void whenBeansConfigured() {
    super.whenBeansConfigured();
    initContentHandler(GsonContentHandler.BEAN_NAME);
  }
  
  void enqueueResponse(final MyModel model) {
    final Gson gson = new GsonBuilder().create();
    final String text = gson.toJson(model);
    assertThat(text).contains("shouldParseJson");
    getWebServer().enqueue(new MockResponse().setBody(text));
  }

  @Ignore // FIXME: testing with robolectric
  @Test
  public void shouldParse() throws Throwable {
    final MyModel model = new MyModel();
    model.id = 3;
    model.text = "shouldParseJson";

    enqueueResponse(model);

    final Loader<ResponseData<MyModel>> loader = new MyRequestBuilder<MyModel>(getApplication()) { }
        .setUrl(getWebServer().getUrl("/").toString())
        .setFormat(format)
        .getLoader();

    assertWithLoader(loader, new Asserter<ResponseData<MyModel>>() {
      @Override
      public void makeAssertions(ResponseData<MyModel> data) throws Exception {
        assertThat(data.isSuccessful()).isTrue();
        assertThat(data.getModel()).isEqualTo(model);
      }
    });

  }

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

}

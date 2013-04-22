package com.stanfy.enroscar.rest.test;

import static org.fest.assertions.api.Assertions.*;

import java.net.HttpURLConnection;
import java.net.ResponseCache;
import java.net.URLConnection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import com.google.mockwebserver.MockResponse;
import com.google.mockwebserver.RecordedRequest;
import com.stanfy.enroscar.beans.BeansManager.Editor;
import com.stanfy.enroscar.net.UrlConnectionWrapper;
import com.stanfy.enroscar.net.test.AbstractMockServerTest;
import com.stanfy.enroscar.net.test.Runner;
import com.stanfy.enroscar.rest.RemoteServerApiConfiguration;
import com.stanfy.enroscar.rest.request.OperationType;
import com.stanfy.enroscar.rest.request.RequestDescription;

/**
 * Tests for {@link com.stanfy.serverapi.request.RequestDescription}.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
@RunWith(Runner.class)
public class RequestDescriptionTest extends AbstractMockServerTest {

//  @Test
//  public void parcelTest() {
//    final RequestDescription rd = new RequestDescription();
//    final Parcel parcel = Parcel.obtain();
//    rd.writeToParcel(parcel, 0);
//
//  }

  @Override
  protected void configureBeansManager(final Editor editor) {
    super.configureBeansManager(editor);
    editor.put(RemoteServerApiConfiguration.class);
  }

  @Test
  public void shouldAutomaticallySetModelClass() {
    assertThat((new MyRequestBuilder<String>(getApplication()) { }).getResult().getModelType().getRawClass().getName())
      .isEqualTo(String.class.getName());
  }

  @Test
  public void makeGetConnectionShouldReceiveCorrectResponse() throws Exception {
    getWebServer().enqueue(new MockResponse().setBody("test response"));

    final URLConnection connection = makeConnection(
        new MyRequestBuilder<String>(Robolectric.application) { }
          .setUrl(getWebServer().getUrl("/r1").toString())
    );

    assertThat(ResponseCache.getDefault()).isNull();

    final String response = read(connection);
    getWebServer().takeRequest();

    final HttpURLConnection http = (HttpURLConnection)UrlConnectionWrapper.unwrap(connection);
    assertThat(http.getResponseCode()).isEqualTo(HttpURLConnection.HTTP_OK);
    assertThat(response).isEqualTo("test response");
  }

  @Test
  public void makeGetConnectionShouldSendGoodParametersAndHeaders() throws Exception {
    getWebServer().enqueue(new MockResponse().setBody("test response"));

    read(makeConnection(
        new MyRequestBuilder<String>(Robolectric.application) { }
          .setUrl(getWebServer().getUrl("/r1").toString())
          .addParam("p1", "v1")
          .addParam("p2", "v2")
    ));

    // check that request was as expected
    final RecordedRequest request = getWebServer().takeRequest();

    // url
    assertThat(request.getPath()).isEqualTo("/r1?p1=v1&p2=v2");
    // headers: language, gzip
    final String lang = Robolectric.application.getResources().getConfiguration().locale.getLanguage();
    assertThat(request.getHeaders()).contains("Accept-Language: " + lang, "Accept-Encoding: gzip");
  }

  @Test
  public void makePostConnectionShouldReceiveCorrectResponse() throws Exception {
    getWebServer().enqueue(new MockResponse().setBody("POST response"));

    final URLConnection connection = makeConnection(
        new MyRequestBuilder<String>(Robolectric.application) { }
          .setUrl(getWebServer().getUrl("/post").toString())
          .setOperationType(OperationType.SIMPLE_POST)
    );

    assertThat(ResponseCache.getDefault()).isNull();

    final String response = read(connection);
    final RecordedRequest request = getWebServer().takeRequest();
    assertThat(request.getMethod()).isEqualTo("POST");

    final HttpURLConnection http = (HttpURLConnection)UrlConnectionWrapper.unwrap(connection);
    assertThat(http.getResponseCode()).isEqualTo(HttpURLConnection.HTTP_OK);
    assertThat(response).isEqualTo("POST response");
  }

  @Test
  public void withUploadOperationTypeSimpleParametersShouldBeIncludedInTargetUrl() throws Exception {
    final URLConnection connection = makeConnection(
        new MyRequestBuilder<String>(Robolectric.application) { }
        .addParam("a", "b")
        .addParam("c", "d")
        .setUrl("http://example.com")
    );
    assertThat(connection.getURL().toString()).isEqualTo("http://example.com?a=b&c=d");
  }
  
}

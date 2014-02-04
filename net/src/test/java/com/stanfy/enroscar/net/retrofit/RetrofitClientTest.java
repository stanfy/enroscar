package com.stanfy.enroscar.net.retrofit;

import android.content.ContentResolver;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Build;

import com.google.gson.Gson;
import com.google.mockwebserver.MockResponse;
import com.google.mockwebserver.MockWebServer;
import com.stanfy.enroscar.net.ContentUriConnection;
import com.stanfy.enroscar.net.UrlConnectionBuilder;
import com.stanfy.enroscar.net.UrlConnectionBuilderFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Query;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests for RetrofitClient.
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class RetrofitClientTest {

  /** Instance under tests. */
  private RetrofitClient client;

  /** Expected object. */
  private Thing testThing;

  /** Test response. */
  private String mockResponse;

  /** Mock content resolver. */
  private ContentResolver mockResolver;

  /** Test input stream. */
  private FileInputStream testResponseInput;

  @Before
  public void init() throws Exception {
    testThing = new Thing();
    testThing.name = "foo!";
    mockResponse = new Gson().toJson(testThing);
    File f = new File(Robolectric.application.getFilesDir(), "resp");
    FileOutputStream out = new FileOutputStream(f);
    out.write(mockResponse.getBytes());
    out.close();
    testResponseInput = new FileInputStream(f);


    mockResolver = mock(ContentResolver.class);
    doReturn("application/json").when(mockResolver).getType(any(Uri.class));
    AssetFileDescriptor mockDescriptor = mock(AssetFileDescriptor.class);
    doReturn(testResponseInput)
        .when(mockDescriptor).createInputStream();
    doReturn(mockDescriptor)
        .when(mockResolver).openAssetFileDescriptor(any(Uri.class), any(String.class));

    client = new RetrofitClient(new UrlConnectionBuilderFactory() {
      @Override
      public UrlConnectionBuilder newUrlConnectionBuilder() {
        return new UrlConnectionBuilder() {
          @Override
          public URLConnection create() throws IOException {
            URL url = getUrl();
            if ("content".equals(url.getProtocol())) {
              return new ContentUriConnection(url, mockResolver);
            }
            return super.create();
          }
        };
      }
    });
  }

  @After
  public void close() throws IOException {
    testResponseInput.close();
  }

  private RestAdapter getRestAdapter(final String endpoint) {
    return new RestAdapter.Builder()
        .setClient(client)
        .setEndpoint(endpoint)
        .build();
  }

  private void fetchAndTest(final RestAdapter adapter) {
    ExampleService service = adapter.create(ExampleService.class);
    Thing responseThing = service.thing("test");
    assertThat(responseThing).isNotSameAs(testThing).isEqualTo(testThing);
  }

  // http
  @Test
  public void shouldWorkForHttpScheme() throws Exception {
    MockWebServer server = new MockWebServer();
    server.play();
    server.enqueue(new MockResponse().setResponseCode(200).setBody(mockResponse));
    RestAdapter adapter = getRestAdapter(server.getUrl("/").toString());
    fetchAndTest(adapter);
    server.shutdown();
  }

  // content
  @Test
  public void shouldWorkForContentScheme() throws Exception {
    String baseUri = "content://test.authority";
    RestAdapter adapter = getRestAdapter(baseUri);
    fetchAndTest(adapter);
    Uri uri = Uri.parse(baseUri + "/thing");
    verify(mockResolver).getType(uri);
    verify(mockResolver).openAssetFileDescriptor(uri, "r");
  }

  /** Test service. */
  interface ExampleService {
    @GET("/thing")
    Thing thing(@Query("name") String name);
  }

  /** What is parsed. */
  public static class Thing {
    /** Dummy field. */
    String name;

    @Override
    public boolean equals(final Object o) {
      if (!(o instanceof Thing)) {
        return false;
      }
      Thing another = (Thing) o;
      return another.name.equals(name);
    }

    @Override
    public int hashCode() {
      return name.hashCode();
    }
  }

}

package com.stanfy.utils;

import static org.hamcrest.Matchers.*;

import org.junit.After;
import org.junit.Test;

import com.google.mockwebserver.MockResponse;
import com.google.mockwebserver.RecordedRequest;
import com.stanfy.serverapi.request.SimpleRequestBuilder;
import com.stanfy.serverapi.response.ResponseData;
import com.stanfy.test.AbstractApplicationServiceTest;
import com.stanfy.test.Application.WaitApiCallback;

/**
 * Tests for {@link RequestPerformer}.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public class RequestPerformerTest extends AbstractApplicationServiceTest {

  /** Callback. */
  private WaitApiCallback callback;
  /** API methods support. */
  private ApiMethodsSupport support;

  @Override
  protected void whenBeansConfigured() {
    super.whenBeansConfigured();
    callback = getApplication().new WaitApiCallback();
    support = new ApiMethodsSupport(getApplication(), callback);
    support.bind();
    support.registerCallback();
  }

  @After
  public void destroySupport() {
    support.removeCallback();
    support.unbind();
  }

  @Test
  public void sendRequestShouldCallCallbacks() throws Exception {
    getWebServer().enqueue(new MockResponse().setBody("test response body"));

    final SimpleRequestBuilder<String> rb = new SimpleRequestBuilder<String>(getApplication()) { };
    rb.setExecutor(support);
    rb
      .addParam("test", 1)
      .setUrl(getWebServer().getUrl("/").toString())
      .execute();

    waitAndAssert(
        new Waiter<ResponseData<?>>() {
          @Override
          public ResponseData<?> waitForData() {
            return callback.waitData();
          }
        },
        new Asserter<ResponseData<?>>() {
          @Override
          public void makeAssertions(final ResponseData<?> data) throws Exception {
            assertThat(callback.isCanceled(), equalTo(false));
            assertThat(data, is(notNullValue()));

            final RecordedRequest request = getWebServer().takeRequest();
            assertThat(request.getPath(), equalTo("/?test=1"));
          }
        });
  }

}

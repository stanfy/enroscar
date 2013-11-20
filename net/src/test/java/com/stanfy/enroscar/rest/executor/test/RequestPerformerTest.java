package com.stanfy.enroscar.rest.executor.test;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Test;

import org.robolectric.annotation.Config;

import com.google.mockwebserver.MockResponse;
import com.google.mockwebserver.RecordedRequest;
import com.stanfy.enroscar.beans.BeansManager.Editor;
import com.stanfy.enroscar.content.loader.ResponseData;
import com.stanfy.enroscar.net.test.AbstractMockServerTest;
import com.stanfy.enroscar.rest.executor.ApiMethodCallback;
import com.stanfy.enroscar.rest.executor.ApiMethodsSupport;
import com.stanfy.enroscar.rest.request.RequestDescription;
import com.stanfy.enroscar.rest.request.SimpleRequestBuilder;
import com.stanfy.enroscar.rest.response.handler.StringContentHandler;
import com.stanfy.enroscar.shared.test.EnroscarConfiguration;

/**
 * Tests for {@link com.stanfy.enroscar.rest.executor.RequestPerformer}.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
@Config(emulateSdk = 18)
@EnroscarConfiguration(connectionEngineRequired = true)
public class RequestPerformerTest extends AbstractMockServerTest {

  /** Callback. */
  private WaitApiCallback callback;
  /** API methods support. */
  private ApiMethodsSupport support;

  @Override
  protected void configureBeansManager(final Editor editor) {
    super.configureBeansManager(editor);
    editor.put(StringContentHandler.class);
  }
  
  @Override
  protected void whenBeansConfigured() {
    super.whenBeansConfigured();
    initContentHandler(StringContentHandler.BEAN_NAME);
    
    callback = new WaitApiCallback();
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
  public void sendRequestShouldCallCallbacks() throws Throwable {
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
            assertThat(callback.isCanceled()).isFalse();
            assertThat(data).isNotNull();

            final RecordedRequest request = getWebServer().takeRequest();
            assertThat(request.getPath()).isEqualTo("/?test=1");
          }
        });
  }

  /** Callback for testing. */
  public class WaitApiCallback implements ApiMethodCallback {

    /** Latch. */
    private final CountDownLatch latch = new CountDownLatch(1);

    /** Data. */
    private ResponseData<?> data;

    /** Canceled. */
    private boolean canceled = false;

    @Override
    public void reportSuccess(final RequestDescription requestDescription, final ResponseData<?> responseData) {
      data = responseData;
      latch.countDown();
    }
    @Override
    public void reportError(final RequestDescription requestDescription, final ResponseData<?> responseData) {
      data = responseData;
      latch.countDown();
    }
    @Override
    public void reportCancel(final RequestDescription requestDescription, final ResponseData<?> responseData) {
      canceled = true;
      data = responseData;
      latch.countDown();
    }

    public boolean isCanceled() { return canceled; }

    public ResponseData<?> waitData() {
      try {
        latch.await(3, TimeUnit.SECONDS);
      } catch (final InterruptedException e) {
        throw new RuntimeException("Wait is interrupted!");
      }
      return data;
    }
  }
  
}

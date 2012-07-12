package com.stanfy.serverapi;

import static org.hamcrest.Matchers.equalTo;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.stanfy.app.beans.BeansManager.Editor;
import com.stanfy.serverapi.request.RequestBuilder;
import com.stanfy.serverapi.request.SimpleRequestBuilder;
import com.stanfy.test.AbstractEnroscarTest;
import com.xtremelabs.robolectric.Robolectric;

/**
 * Test for list wrapper.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public class ListRequestBuilderWrapperTest extends AbstractEnroscarTest {

  @Override
  protected void configureBeansManager(final Editor editor) {
    editor.required().remoteServerApi();
  }

  @Test
  public void shouldReturnCorrectExpectedModelType() {

    final SimpleRequestBuilder<String> rb = new SimpleRequestBuilder<String>(Robolectric.application) { };
    assertThat(rb.getExpectedModelType().getRawClass().toString(), equalTo(String.class.toString()));

    final RequestBuilder<List<String>> listRb = rb.<String, List<String>>asLoadMoreList();
    assertThat(listRb.getExpectedModelType().getRawClass().isInstance(new ArrayList<String>()), equalTo(true));

  }

}

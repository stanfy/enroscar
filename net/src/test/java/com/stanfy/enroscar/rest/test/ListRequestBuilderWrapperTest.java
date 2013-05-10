package com.stanfy.enroscar.rest.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import com.stanfy.enroscar.beans.BeansManager.Editor;
import com.stanfy.enroscar.rest.RemoteServerApiConfiguration;
import com.stanfy.enroscar.rest.request.RequestBuilder;
import com.stanfy.enroscar.rest.request.SimpleRequestBuilder;
import com.stanfy.enroscar.shared.test.AbstractEnroscarTest;

/**
 * Test for list wrapper.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
@RunWith(RobolectricTestRunner.class)
public class ListRequestBuilderWrapperTest extends AbstractEnroscarTest {

  @Override
  protected void configureBeansManager(final Editor editor) {
    editor.put(RemoteServerApiConfiguration.class);
  }

  @Test
  public void shouldReturnCorrectExpectedModelType() {

    final SimpleRequestBuilder<String> rb = new SimpleRequestBuilder<String>(Robolectric.application) { };
    assertThat(rb.getExpectedModelType().getRawClass().toString(), equalTo(String.class.toString()));

    final RequestBuilder<List<String>> listRb = rb.<String, List<String>>asLoadMoreList();
    assertThat(listRb.getExpectedModelType().getRawClass().isInstance(new ArrayList<String>()), equalTo(true));

  }

}

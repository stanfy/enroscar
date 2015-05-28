package com.stanfy.enroscar.rest.test;

import com.stanfy.enroscar.beans.BeansManager.Editor;
import com.stanfy.enroscar.net.operation.RequestBuilder;
import com.stanfy.enroscar.net.operation.SimpleRequestBuilder;
import com.stanfy.enroscar.rest.RemoteServerApiConfiguration;
import com.stanfy.enroscar.test.AbstractNetTest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for list wrapper.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18)
public class ListRequestBuilderWrapperTest extends AbstractNetTest {

  @Override
  protected void configureBeansManager(final Editor editor) {
    editor.put(RemoteServerApiConfiguration.class);
  }

  @Test
  public void shouldReturnCorrectExpectedModelType() {

    final SimpleRequestBuilder<String> rb = new SimpleRequestBuilder<String>(Robolectric.application) { };
    assertThat(rb.getExpectedModelType().getRawClass().toString()).isEqualTo(String.class.toString());

    final RequestBuilder<List<String>> listRb = rb.<String, List<String>>asLoadMoreList();
    assertThat(listRb.getExpectedModelType().getRawClass().isInstance(new ArrayList<String>())).isTrue();

  }

}

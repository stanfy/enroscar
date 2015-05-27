package com.stanfy.enroscar.rest.test;

import com.stanfy.enroscar.beans.BeansManager.Editor;
import com.stanfy.enroscar.net.operation.BaseRequestBuilder;
import com.stanfy.enroscar.net.operation.Parameter;
import com.stanfy.enroscar.net.operation.ParameterValue;
import com.stanfy.enroscar.net.operation.RequestDescription;
import com.stanfy.enroscar.rest.RemoteServerApiConfiguration;
import com.stanfy.enroscar.test.AbstractNetTest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link BaseRequestBuilder}.
 */
@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18)
public class BaseRequestBuilderTest extends AbstractNetTest {

  @Override
  protected void configureBeansManager(final Editor editor) {
    editor.put(RemoteServerApiConfiguration.class);
  }

  @Test
  public void testAddSimpleParameters() {
    final MyBuilder b = new MyBuilder();
    final int count = 5;
    b.addSimpleParameter("boolean1", true);
    b.addSimpleParameter("boolean2", false);
    b.addSimpleParameter("string", "abc");
    b.addSimpleParameter("int", 1);
    b.addSimpleParameter("long", 2L);

    final RequestDescription d = b.getResult();
    assertThat(d.getSimpleParameters().getChildren().size()).isEqualTo(count);

    final ArrayList<String> names = new ArrayList<String>(), values = new ArrayList<String>();
    for (final Parameter p : d.getSimpleParameters().getChildren()) {
      final ParameterValue pv = (ParameterValue)p;
      names.add(pv.getName());
      values.add(pv.getValue());
    }
    assertThat(names).isEqualTo(Arrays.asList("boolean1", "boolean2", "string", "int", "long"));
    assertThat(values).isEqualTo(Arrays.asList("1", "0", "abc", "1", "2"));
  }

  @Test
  public void testRemoveParameter() {
    final MyBuilder b = new MyBuilder();
    b.addSimpleParameter("p1", "v1");
    b.addSimpleParameter("p2", "v2");

    assertThat(b.getResult().getSimpleParameters().getChildren().size()).isEqualTo(2);

    final Parameter removed = b.removeParameter("p1");
    assertThat(removed).isNotNull();
    assertThat(removed.getName()).isEqualTo("p1");
    assertThat(((ParameterValue)removed).getValue()).isEqualTo("v1");

    assertThat(b.getResult().getSimpleParameters().getChildren().size()).isEqualTo(1);
    assertThat(b.getResult().getSimpleParameters().getChildren().get(0).getName()).isEqualTo("p2");
  }

  @Test
  public void clearShouldRemoveAllParameters() {
    final MyBuilder b = new MyBuilder();
    b.addSimpleParameter("p1", "v1");
    b.addSimpleParameter("p2", "v2");
    assertThat(b.getResult().getSimpleParameters().getChildren().size()).isEqualTo(2);

    b.clear();
    assertThat(b.getResult().getSimpleParameters().getChildren().size()).isEqualTo(0);
  }

  @Test
  public void clearShouldRemoveAllHeaders() {
    final MyBuilder b = new MyBuilder();
    b.addHeader("h1", "v1");
    b.addHeader("h2", "v2");
    assertThat(b.getResult().getHeader("h1")).isEqualTo("v1");
    assertThat(b.getResult().getHeader("h2")).isEqualTo("v2");

    b.clear();
    assertThat(b.getResult().getHeader("h1")).isNull();
    assertThat(b.getResult().getHeader("h2")).isNull();
  }

  @Test
  public void addRemoveHeaderShouldAffectDescription() {
    final MyBuilder b = new MyBuilder();
    b.addHeader("a", "b");
    assertThat(b.getResult().getHeader("a")).isEqualTo("b");
    b.removeHeader("a");
    assertThat(b.getResult().getHeader("a")).isNull();
  }

  /**
   * Builder for testing.
   */
  private static class MyBuilder extends BaseRequestBuilder<String> {

    public MyBuilder() {
      super(Robolectric.application);
    }

    @Override
    public RequestDescription getResult() { return super.getResult(); }

    @Override
    public ParameterValue addSimpleParameter(final String name, final String value) {
      return super.addSimpleParameter(name, value);
    }

    @Override
    public ParameterValue addSimpleParameter(final String name, final boolean value) {
      return super.addSimpleParameter(name, value);
    }

    @Override
    public ParameterValue addSimpleParameter(final String name, final int value) {
      return super.addSimpleParameter(name, value);
    }

    @Override
    public ParameterValue addSimpleParameter(final String name, final long value) {
      return super.addSimpleParameter(name, value);
    }

    @Override
    public Parameter removeParameter(final String name) {
      return super.removeParameter(name);
    }

    @Override
    protected MyBuilder addHeader(final String name, final String value) {
      super.addHeader(name, value);
      return this;
    }

    @Override
    protected void removeHeader(final String name) {
      super.removeHeader(name);
    }

  }

}

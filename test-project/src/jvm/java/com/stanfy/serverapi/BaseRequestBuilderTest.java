package com.stanfy.serverapi;

import static org.hamcrest.Matchers.*;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

import com.stanfy.app.beans.BeansManager.Editor;
import com.stanfy.serverapi.request.BaseRequestBuilder;
import com.stanfy.serverapi.request.Parameter;
import com.stanfy.serverapi.request.ParameterValue;
import com.stanfy.serverapi.request.RequestDescription;
import com.stanfy.test.AbstractEnroscarTest;
import com.xtremelabs.robolectric.Robolectric;

/**
 * Tests for {@link BaseRequestBuilder}.
 */

public class BaseRequestBuilderTest extends AbstractEnroscarTest {

  @Override
  protected void configureBeansManager(final Editor editor) {
    editor.required().remoteServerApi();
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
    assertThat(d.getSimpleParameters().getChildren().size(), equalTo(count));

    final ArrayList<String> names = new ArrayList<String>(), values = new ArrayList<String>();
    for (final Parameter p : d.getSimpleParameters().getChildren()) {
      final ParameterValue pv = (ParameterValue)p;
      names.add(pv.getName());
      values.add(pv.getValue());
    }
    assertThat(names, equalTo(Arrays.asList("boolean1", "boolean2", "string", "int", "long")));
    assertThat(values, equalTo(Arrays.asList("1", "0", "abc", "1", "2")));
  }

  @Test
  public void testRemoveParameter() {
    final MyBuilder b = new MyBuilder();
    b.addSimpleParameter("p1", "v1");
    b.addSimpleParameter("p2", "v2");

    assertThat(b.getResult().getSimpleParameters().getChildren().size(), equalTo(2));

    final Parameter removed = b.removeParameter("p1");
    assertThat(removed, is(notNullValue()));
    assertThat(removed.getName(), equalTo("p1"));
    assertThat(((ParameterValue)removed).getValue(), equalTo("v1"));

    assertThat(b.getResult().getSimpleParameters().getChildren().size(), equalTo(1));
    assertThat(b.getResult().getSimpleParameters().getChildren().get(0).getName(), equalTo("p2"));
  }

  @Test
  public void clearShouldRemoveAllParameters() {
    final MyBuilder b = new MyBuilder();
    b.addSimpleParameter("p1", "v1");
    b.addSimpleParameter("p2", "v2");
    assertThat(b.getResult().getSimpleParameters().getChildren().size(), equalTo(2));

    b.clear();
    assertThat(b.getResult().getSimpleParameters().getChildren().size(), equalTo(0));
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

  }

}

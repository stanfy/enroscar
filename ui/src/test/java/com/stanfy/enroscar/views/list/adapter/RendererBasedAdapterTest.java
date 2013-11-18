package com.stanfy.enroscar.views.list.adapter;

import com.stanfy.enroscar.content.UniqueObject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Arrays;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.reflect.core.Reflection.field;

/**
 * Tests for ModelListAdapter.
 */
@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18)
public class RendererBasedAdapterTest {

  /** Testing instance. */
  private RendererBasedAdapter<Model> adapter;

  /** First real item reference. */
  private Model firstModel;

  @Before
  public void init() {
    //CHECKSTYLE:OFF
    adapter = new RendererBasedAdapter<Model>(Robolectric.application, new ElementRenderer<Model>(0) { }) {
      @Override
      public long getItemId(final int position) {
        return position;
      }

      @Override
      public int getViewTypeCount() {
        return 2;
      }
    };
    firstModel = new Model(2);
    adapter.addAll(Arrays.asList(new Fiction(1), firstModel, new Model(3), new Fiction(4), new Model(5)));
    //CHECKSTYLE:ON
  }

  @Test
  public void shouldMergeFictions() {
    //CHECKSTYLE:OFF
    assertThat(adapter.getCount()).isEqualTo(5);
    adapter.add(new Fiction(4));
    adapter.add(new Model(10));
    assertThat(adapter.getCount()).isEqualTo(6);
    adapter.addAll(Arrays.asList(new Fiction(4), new Model(6), new Fiction(7)));
    assertThat(adapter.getCount()).isEqualTo(8);
    //CHECKSTYLE:ON
  }

  @Test
  public void removeShouldTrackFictionItems() {
    //CHECKSTYLE:OFF
    assertThat(adapter.getCount()).isEqualTo(5);
    adapter.remove(4);
    assertThat(adapter.getCount()).isEqualTo(3);
    Model m = new Model(6);
    adapter.addAll(Arrays.asList(new Fiction(1), m));
    assertThat(adapter.getCount()).isEqualTo(4);
    adapter.remove(m);
    assertThat(adapter.getCount()).isEqualTo(3);
    adapter.remove(2);
    adapter.remove(firstModel);
    assertThat(adapter.getCount()).isEqualTo(0);
    //CHECKSTYLE:ON
  }

  @Test
  public void shouldMakeElementsCopyWhenConstructedFromOtherAdapter() {
    RendererBasedAdapter<Model> another = new ModelListAdapter<Model>(adapter);
    assertThat(field("elements").ofType(ArrayList.class).in(another)).isNotSameAs(field("elements").ofType(ArrayList.class).in(adapter));
  }

  /** Model. */
  static class Model implements UniqueObject {
    /** ID. */
    private final long id;

    public Model(final long id) {
      this.id = id;
    }

    @Override
    public long getId() {
      return id;
    }

    @Override
    public boolean equals(final Object o) {
      Model m = (Model) o;
      return m.id == id;
    }

    @Override
    public int hashCode() {
      return Long.valueOf(id).hashCode();
    }
  }

  /** Fiction. */
  static class Fiction extends Model implements FictionObject {


    public Fiction(final long id) {
      super(id);
    }

    @Override
    public String getDisplayName() {
      return String.valueOf(getId());
    }
  }

}

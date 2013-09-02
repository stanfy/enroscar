package com.stanfy.enroscar.views.list.test;

import com.stanfy.enroscar.content.UniqueObject;
import com.stanfy.enroscar.views.list.adapter.FictionObject;
import com.stanfy.enroscar.views.list.adapter.ModelListAdapter;

import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import org.junit.runner.RunWith;
import org.junit.Test;
import org.junit.Before;

import java.util.ArrayList;
import java.util.Arrays;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.reflect.core.Reflection.*;

/**
 * Tests for ModelListAdapter.
 */
@RunWith(RobolectricTestRunner.class)
public class ModelListAdapterTest {

  /** Testing instance. */
  private ModelListAdapter<Model> adapter;

  /** First real item reference. */
  private Model firstModel;

  @Before
  public void init() {
    //CHECKSTYLE:OFF
    adapter = new ModelListAdapter<Model>(Robolectric.application, new ModelListAdapter.ElementRenderer<Model>(0) { }) {
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
    ModelListAdapter<Model> another = new ModelListAdapter<Model>(adapter);
    assertThat(field("elements").ofType(ArrayList.class).in(another)).isNotSameAs(field("elements").ofType(ArrayList.class).in(adapter));
  }

  /** Model. */
  private static class Model implements UniqueObject {
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
  private static class Fiction extends Model implements FictionObject {


    public Fiction(final long id) {
      super(id);
    }

    @Override
    public String getDisplayName() {
      return String.valueOf(getId());
    }
  }

}

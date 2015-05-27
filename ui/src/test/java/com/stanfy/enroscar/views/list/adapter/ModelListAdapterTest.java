package com.stanfy.enroscar.views.list.adapter;

import android.os.Build;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for ModelListAdapter.
 */
@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class ModelListAdapterTest {

  /** Adapter under test. */
  private ModelListAdapter<RendererBasedAdapterTest.Model> adapter;

  @Before
  public void init() {
    adapter = new ModelListAdapter<RendererBasedAdapterTest.Model>(Robolectric.application, null);
    adapter.addAll(Arrays.asList(new RendererBasedAdapterTest.Model(2), new RendererBasedAdapterTest.Model(1)));
  }

  @Test
  public void shouldGetItemIdFromUniqObject() throws Exception {
    assertThat(adapter.getItemId(0)).isEqualTo(2);
    assertThat(adapter.getItemId(1)).isEqualTo(1);
  }

  @Test
  public void shouldHasStableIds() throws Exception {
    assertThat(adapter.hasStableIds()).isTrue();
  }
}

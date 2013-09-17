package com.stanfy.enroscar.views.list.adapter;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Arrays;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests for TemporaryItemWrapper.
 */
@RunWith(RobolectricTestRunner.class)
public class TemporaryItemWrapperTest {

  /** Adapter under the test. */
  private TemporaryItemWrapper<String> adapter;

  /** Core adapter. */
  private BaseAdapter core;

  @Before
  public void create() {
    core = new BaseAdapter() {

      /** Items. */
      final List<String> items = Arrays.asList("str1", "str2", "str3");

      @Override
      public int getCount() {
        return items.size();
      }

      @Override
      public Object getItem(final int position) {
        return items.get(position);
      }

      @Override
      public long getItemId(final int position) {
        return position;
      }

      @Override
      public View getView(final int position, final View convertView, final ViewGroup parent) {
        return new TextView(parent.getContext());
      }
    };
    adapter = new TemporaryItemWrapper<String>(core) {
      @Override
      protected long getTemporaryItemId(final int position) {
        return Long.MAX_VALUE;
      }

      @Override
      protected View getTempItemView(final int position, final View convertView, final ViewGroup parent) {
        return new TextView(parent.getContext());
      }
    };
  }

  @Test
  public void shouldCorrespondToCoreWithoutTempItem() {
    assertThat(adapter.getCount()).isEqualTo(core.getCount()).isEqualTo(3);
    assertThat(adapter.getItem(1)).isEqualTo(core.getItem(1)).isEqualTo("str2");
    assertThat(adapter.getItemId(2)).isEqualTo(core.getItemId(2)).isEqualTo(2);
  }

  @Test
  public void shouldInjectTempItemAtTheEnd() {
    adapter.setTempItem("temp");
    assertThat(adapter.getCount()).isEqualTo(core.getCount() + 1);
    assertThat(adapter.getItem(core.getCount())).isEqualTo("temp");
    assertThat(adapter.getItemId(core.getCount())).isEqualTo(Long.MAX_VALUE);
    assertThat(adapter.isEnabled(core.getCount())).isTrue();
    assertThat(adapter.getItem(0)).isEqualTo("str1");
  }

  @Test
  public void shouldInjectTempItemAtTheBeginning() {
    adapter.setTempItem("temp");
    adapter.setAtTheEnd(false);
    assertThat(adapter.getCount()).isEqualTo(core.getCount() + 1);
    assertThat(adapter.getItem(0)).isEqualTo("temp");
    assertThat(adapter.getItemId(0)).isEqualTo(Long.MAX_VALUE);
    assertThat(adapter.isEnabled(0)).isTrue();
    assertThat(adapter.getItem(core.getCount())).isEqualTo("str3");
  }

  @Test
  public void shouldNotifyDatasetChanged() {
    final boolean[] called = {false, false};
    adapter.registerDataSetObserver(new DataSetObserver() {
      @Override
      public void onChanged() {
        called[0] = true;
      }

      @Override
      public void onInvalidated() {
        called[1] = true;
      }
    });
    adapter.setAtTheEnd(false);
    assertThat(called).isEqualTo(new boolean[] {true, false});
    adapter.notifyDataSetInvalidated();
    assertThat(called).isEqualTo(new boolean[] {true, true});

    called[0] = false;
    called[1] = false;
    core.notifyDataSetChanged();
    assertThat(called).isEqualTo(new boolean[] {true, false});
    core.notifyDataSetInvalidated();
    assertThat(called).isEqualTo(new boolean[] {true, true});
  }

}

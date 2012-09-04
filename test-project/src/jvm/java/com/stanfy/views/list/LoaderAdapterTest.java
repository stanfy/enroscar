package com.stanfy.views.list;

import static org.hamcrest.Matchers.*;

import java.util.Arrays;

import org.junit.Test;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.stanfy.enroscar.test.R;
import com.stanfy.test.AbstractEnroscarTest;
import com.stanfy.views.StateHelper;
import com.xtremelabs.robolectric.Robolectric;

/**
 * Test for {@link com.stanfy.views.list.LoaderAdapter}.
 * @author Vladislav Lipskiy - Stanfy (http://www.stanfy.com)
 *
 */
public class LoaderAdapterTest extends AbstractEnroscarTest {

  /**
   * Check that wrapper-adapter changes its state
   * consistently with its core adapter.
   */
  @Test
  public void changeStateWithCore() {
    final Context context = Robolectric.application;
    final CharSequence[] strings = context.getResources().getTextArray(R.array.adapter_test);
    final ListView listView = new ListView(context);
    final ArrayAdapter<CharSequence> core = new ArrayAdapter<CharSequence>(context, 0) {
      @Override
      public View getView(final int position, final View convertView, final ViewGroup parent) {
        return null;
      }
    };
    final MockLoaderAdapter<CharSequence> wrapper = new MockLoaderAdapter<CharSequence>(context, core);
    wrapper.onLoadFinished(null, Arrays.asList(strings));
    assertThat(wrapper.getCount(), is(strings.length));   // pre-test

    // 1. If adapter had some data, but then elements were removed through core adapter, wrapper must change its state too.
    core.clear();
    core.notifyDataSetChanged();

    // Key test. State should be set to empty.
    assertThat(wrapper.getState(), is(StateHelper.STATE_EMPTY));
    // StateHelper should create a state view.
    assertThat(wrapper.getView(0, null, listView), notNullValue());
    // And adapter count should be equal to 1
    assertThat(wrapper.getCount(), is(1));

    // 2. Same goes in case adapter received empty data, but then elements added to core adapter
    for (final CharSequence cs : strings) {
      core.add(cs);
    }
    core.notifyDataSetChanged();

    // Key test. State should be set to normal.
    assertThat(wrapper.getState(), is(StateHelper.STATE_NORMAL));
    // Delegate view creation to our ArrayAdapter which returns null.
    assertThat(wrapper.getView(0, null, listView), nullValue());
    // And adapter count should be equal to array length.
    assertThat(wrapper.getCount(), is(strings.length));
  }


}

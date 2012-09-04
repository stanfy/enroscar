package com.stanfy.views.list;

import static org.hamcrest.Matchers.*;

import java.util.Arrays;

import org.junit.Test;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.stanfy.enroscar.test.R;
import com.stanfy.test.AbstractEnroscarTest;
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
    final ArrayAdapter<CharSequence> core = new ArrayAdapter<CharSequence>(context, 0);
    final MockLoaderAdapter<CharSequence> wrapper = new MockLoaderAdapter<CharSequence>(context, core);
    // 1. If adapter received empty data, but then elements added to core adapter, wrapper must change its state too.
    wrapper.onLoadFinished(null, Arrays.asList(strings));
    assertThat(wrapper.getCount(), is(strings.length));   // pre-test

    core.clear();
    core.notifyDataSetChanged();

    // Key test, count must be equal to 1.
    // According to internal logic, it means that adapter is in an abnormal state, STATE_EMPTY to be precise.
    assertThat(wrapper.getCount(), is(1));

    // 2. Same goes in case adapter had some data, but then elements were removed through core adapter
    for (final CharSequence cs : strings) {
      core.add(cs);
    }
    core.notifyDataSetChanged();

    // Key test, count must be equal to length of array we added before.
    // It means that adapter is in normal state.
    assertThat(wrapper.getCount(), is(strings.length));
  }


}

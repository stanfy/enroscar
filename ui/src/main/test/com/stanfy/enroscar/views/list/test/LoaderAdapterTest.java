package com.stanfy.enroscar.views.list.test;

import static org.hamcrest.Matchers.*;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import android.content.Context;
import android.database.DataSetObserver;
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

  /** Aadapter to test. */
  private MockLoaderAdapter<CharSequence> loaderAdapter;

  /** Core adapter. */
  private ArrayAdapter<CharSequence> coreAdapter;

  /** Test data to add to adapter. */
  private CharSequence[] testData;

  /** Context instance to use. */
  private Context context;

  /** List view instance. */
  private ListView listView;

  /** Observers notified? */
  private boolean notifyChangedCalled;

  /** Adapter observer. */
  private final DataSetObserver notifyObserver = new DataSetObserver() {
    @Override
    public void onChanged() {
      notifyChangedCalled = true;
    }
  };

  @Before
  public void init() {
    context = Robolectric.application;
    testData = context.getResources().getTextArray(R.array.adapter_test);
    listView = new ListView(context);
    coreAdapter = new ArrayAdapter<CharSequence>(context, 0) {
      @Override
      public View getView(final int position, final View convertView, final ViewGroup parent) {
        return null;
      }
    };
    loaderAdapter = new MockLoaderAdapter<CharSequence>(context, coreAdapter);
  }

  /**
   * Check that wrapper-adapter changes its state
   * consistently with its core adapter.
   */
  @Test
  public void changeStateWithCore() {
    loaderAdapter.onLoadFinished(null, Arrays.asList(testData));
    assertThat(loaderAdapter.getCount(), is(testData.length));   // pre-test

    // 1. If adapter had some data, but then elements were removed through core adapter, wrapper must change its state too.
    coreAdapter.clear();
    coreAdapter.notifyDataSetChanged();

    // Key test. State should be set to empty.
    assertThat(loaderAdapter.getState(), is(StateHelper.STATE_EMPTY));
    // StateHelper should create a state view.
    assertThat(loaderAdapter.getView(0, null, listView), notNullValue());
    // And adapter count should be equal to 1
    assertThat(loaderAdapter.getCount(), is(1));

    // 2. Same goes in case adapter received empty data, but then elements added to core adapter
    for (final CharSequence cs : testData) {
      coreAdapter.add(cs);
    }
    coreAdapter.notifyDataSetChanged();

    // Key test. State should be set to normal.
    assertThat(loaderAdapter.getState(), is(StateHelper.STATE_NORMAL));
    // Delegate view creation to our ArrayAdapter which returns null.
    assertThat(loaderAdapter.getView(0, null, listView), nullValue());
    // And adapter count should be equal to array length.
    assertThat(loaderAdapter.getCount(), is(testData.length));
  }


  @Test
  public void successDataShouldCauseNotifyChanged() {
    loaderAdapter.registerDataSetObserver(notifyObserver);

    notifyChangedCalled = false;
    loaderAdapter.onLoadFinished(null, Arrays.asList(testData));
    assertThat(notifyChangedCalled, is(true));
    assertThat(loaderAdapter.onSuccessCalled, is(true));
  }

  @Test
  public void emptyDataShouldCauseNotifyChanged() {
    loaderAdapter.registerDataSetObserver(notifyObserver);

    notifyChangedCalled = false;
    loaderAdapter.onLoadFinished(null, Collections.<CharSequence>emptyList());
    assertThat(notifyChangedCalled, is(true));
    assertThat(loaderAdapter.onEmptyCalled, is(true));
  }

  @Test
  public void errorDataShouldCauseNotifyChanged() {
    loaderAdapter.registerDataSetObserver(notifyObserver);

    notifyChangedCalled = false;
    loaderAdapter.onLoadFinished(null, null);
    assertThat(notifyChangedCalled, is(true));
    assertThat(loaderAdapter.onErrorCalled, is(true));
  }

}

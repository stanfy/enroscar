package com.stanfy.enroscar.views.list.adapter;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.stanfy.enroscar.shared.test.AbstractEnroscarTest;
import com.stanfy.enroscar.views.StateHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.Collections;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Test for {@link LoaderAdapter}.
 * @author Vladislav Lipskiy - Stanfy (http://www.stanfy.com)
 *
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = "src/main/AndroidManifest.xml", emulateSdk = 18)
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
    testData = new CharSequence[] {"1", "2", "3", "4"};
    listView = new ListView(context);
    coreAdapter = new ArrayAdapter<CharSequence>(context, 0) {
      @Override
      public View getView(final int position, final View convertView, final ViewGroup parent) {
        View v = new View(parent.getContext());
        v.setTag("passed");
        return v;
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
    assertThat(loaderAdapter.getCount()).isEqualTo(testData.length);   // pre-test

    // 1. If adapter had some data, but then elements were removed through core adapter, wrapper must change its state too.
    coreAdapter.clear();
    coreAdapter.notifyDataSetChanged();

    // Key test. State should be set to empty.
    assertThat(loaderAdapter.getState()).isEqualTo(StateHelper.STATE_EMPTY);
    // StateHelper should create a state view.
    assertThat(loaderAdapter.getView(0, null, listView)).isNotNull();
    // And adapter count should be equal to 1
    assertThat(loaderAdapter.getCount()).isEqualTo(1);

    // 2. Same goes in case adapter received empty data, but then elements added to core adapter
    for (final CharSequence cs : testData) {
      coreAdapter.add(cs);
    }
    coreAdapter.notifyDataSetChanged();

    // Key test. State should be set to normal.
    assertThat(loaderAdapter.getState()).isEqualTo(StateHelper.STATE_NORMAL);
    // Delegate view creation to our ArrayAdapter.
    assertThat(loaderAdapter.getView(0, null, listView).getTag()).isEqualTo("passed");
    // And adapter count should be equal to array length.
    assertThat(loaderAdapter.getCount()).isEqualTo(testData.length);
  }


  @Test
  public void successDataShouldCauseNotifyChanged() {
    loaderAdapter.registerDataSetObserver(notifyObserver);

    notifyChangedCalled = false;
    loaderAdapter.onLoadFinished(null, Arrays.asList(testData));
    assertThat(notifyChangedCalled).isTrue();
    assertThat(loaderAdapter.onSuccessCalled).isTrue();
  }

  @Test
  public void emptyDataShouldCauseNotifyChanged() {
    loaderAdapter.registerDataSetObserver(notifyObserver);

    notifyChangedCalled = false;
    loaderAdapter.onLoadFinished(null, Collections.<CharSequence>emptyList());
    assertThat(notifyChangedCalled).isTrue();
    assertThat(loaderAdapter.onEmptyCalled).isTrue();
  }

  @Test
  public void errorDataShouldCauseNotifyChanged() {
    loaderAdapter.registerDataSetObserver(notifyObserver);

    notifyChangedCalled = false;
    loaderAdapter.onLoadFinished(null, null);
    assertThat(notifyChangedCalled).isTrue();
    assertThat(loaderAdapter.onErrorCalled).isTrue();
  }

  @Test
  public void shouldNotRecycleStateViews() {
    loaderAdapter.setState(StateHelper.STATE_LOADING);
    assertThat(loaderAdapter.getCount()).isEqualTo(1);
    assertThat(loaderAdapter.getItemViewType(0)).isEqualTo(ListAdapter.IGNORE_ITEM_VIEW_TYPE);
  }

  @Test
  public void shouldBeInLoadingStateByDefault() {
    assertThat(loaderAdapter.getState()).isEqualTo(StateHelper.STATE_LOADING);
  }

}

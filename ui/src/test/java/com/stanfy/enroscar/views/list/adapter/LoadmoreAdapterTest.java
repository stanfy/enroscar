package com.stanfy.enroscar.views.list.adapter;

import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Tests for {@link LoadmoreAdapter}.
 */
@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class LoadmoreAdapterTest {

  /** Adapter to test. */
  private LoadmoreAdapter loadmoreAdapter;

  /** Core adapter. */
  private Adapter coreAdapter;

  @Before
  public void setup() {
    coreAdapter = new Adapter();
    loadmoreAdapter = new LoadmoreAdapter(LayoutInflater.from(Robolectric.application), coreAdapter);
    loadmoreAdapter.setLoadView(new View(Robolectric.application));
  }

  @Test
  public void loadFlagShouldBeResetAfterChangesInCore() {
    assertThat(coreAdapter.getCount()).isGreaterThan(0);
    assertThat(loadmoreAdapter.getCount()).isEqualTo(coreAdapter.getCount());
    loadmoreAdapter.setLoadFlag(true);
    assertThat(loadmoreAdapter.getCount()).isEqualTo(coreAdapter.getCount() + 1); // we show footer
    coreAdapter.notifyDataSetChanged();
    assertThat(loadmoreAdapter.getCount()).isEqualTo(coreAdapter.getCount()); // we don't show footer
  }

  /** Adapter for tests. */
  static class Adapter extends ArrayAdapter<CharSequence> implements FetchableListAdapter {

    /** What was called? */
    boolean loadMoreCalled;

    /** State. */
    boolean moreElements, busy;

    public Adapter() {
      super(Robolectric.application, 0, new CharSequence[] {"1", "2", "3", "4"});
    }

    @Override
    public void loadMoreRecords() {
      loadMoreCalled = true;
    }

    @Override
    public boolean moreElementsAvailable() {
      return moreElements;
    }

    @Override
    public boolean isBusy() {
      return busy;
    }

  }

}

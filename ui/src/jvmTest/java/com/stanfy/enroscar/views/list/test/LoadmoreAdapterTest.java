package com.stanfy.enroscar.views.list.test;

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;

import com.stanfy.enroscar.shared.test.AbstractEnroscarTest;
import com.stanfy.enroscar.views.list.FetchableListAdapter;
import com.stanfy.enroscar.views.list.LoadmoreAdapter;


/**
 * Tests for {@link LoadmoreAdapter}.
 */
@RunWith(RobolectricTestRunner.class)
public class LoadmoreAdapterTest extends AbstractEnroscarTest {

  /** Adapter to test. */
  private LoadmoreAdapter loadmoreAdapter;

  /** Core adapter. */
  private Adapter coreAdapter;

  @Before
  public void setup() {
    coreAdapter = new Adapter();
    loadmoreAdapter = new LoadmoreAdapter(LayoutInflater.from(getApplication()), coreAdapter);
    loadmoreAdapter.setLoadView(new View(getApplication()));
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

package com.stanfy.views.list;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

import org.junit.Before;
import org.junit.Test;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;

import com.stanfy.enroscar.test.R;
import com.stanfy.test.AbstractEnroscarTest;
import com.xtremelabs.robolectric.Robolectric;


/**
 * Tests for {@link LoadmoreAdapter}.
 */
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
    assertThat(coreAdapter.getCount(), greaterThan(0));
    assertThat(loadmoreAdapter.getCount(), equalTo(coreAdapter.getCount()));
    loadmoreAdapter.setLoadFlag(true);
    assertThat(loadmoreAdapter.getCount(), equalTo(coreAdapter.getCount() + 1)); // we show footer
    coreAdapter.notifyDataSetChanged();
    assertThat(loadmoreAdapter.getCount(), equalTo(coreAdapter.getCount())); // we don't show footer
  }

  /** Adapter for tests. */
  static class Adapter extends ArrayAdapter<CharSequence> implements FetchableListAdapter {

    /** What was called? */
    boolean loadMoreCalled;

    /** State. */
    boolean moreElements, busy;

    public Adapter() {
      super(Robolectric.application, 0, Robolectric.application.getResources().getTextArray(R.array.adapter_test));
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

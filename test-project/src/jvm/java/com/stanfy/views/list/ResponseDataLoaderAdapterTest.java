package com.stanfy.views.list;

import static org.hamcrest.Matchers.is;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import android.database.DataSetObserver;

import com.stanfy.content.UniqueObject;
import com.stanfy.serverapi.response.ResponseData;
import com.stanfy.test.AbstractEnroscarTest;
import com.xtremelabs.robolectric.Robolectric;


/**
 * Tests for {@link ResponseDataLoaderAdapter}.
 */
public class ResponseDataLoaderAdapterTest extends AbstractEnroscarTest {

  /** Call flag. */
  private boolean notifyDataSetChanged = false;

  @Test
  public void replaceDataInCoreCausesNotifyDatasetChanged() {
    // setup
    ResponseDataLoaderAdapter<UniqueObject, List<UniqueObject>> rdAdapter
        = new ResponseDataLoaderAdapter<UniqueObject, List<UniqueObject>>(Robolectric.application, new ModelListAdapter<UniqueObject>(getApplication(), null));
    rdAdapter.registerDataSetObserver(new DataSetObserver() {
      @Override
      public void onChanged() {
        notifyDataSetChanged = true;
      }
    });


    // test
    notifyDataSetChanged = false;
    rdAdapter.replaceDataInCore(new ResponseData<List<UniqueObject>>(Collections.<UniqueObject>emptyList()));
    assertThat(notifyDataSetChanged, is(true));
  }

}

package com.stanfy.enroscar.views.list.adapter;

import android.database.DataSetObserver;
import android.os.Build;

import com.stanfy.enroscar.content.UniqueObject;
import com.stanfy.enroscar.content.loader.ResponseData;
import com.stanfy.enroscar.test.AbstractNetTest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Collections;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests for {@link com.stanfy.enroscar.views.list.adapter.ResponseDataLoaderAdapter}.
 */
@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class ResponseDataLoaderAdapterTest extends AbstractNetTest {

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
    assertThat(notifyDataSetChanged).isTrue();
  }

}

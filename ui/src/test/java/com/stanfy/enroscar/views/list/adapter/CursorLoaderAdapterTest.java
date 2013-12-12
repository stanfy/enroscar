package com.stanfy.enroscar.views.list.adapter;

import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.MatrixCursor;
import android.os.Build;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.stanfy.enroscar.test.AbstractNetTest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.fest.assertions.api.Assertions.assertThat;


/**
 * Tests for {@link com.stanfy.enroscar.views.list.adapter.CursorLoaderAdapter}.
 */
@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class CursorLoaderAdapterTest extends AbstractNetTest {

  /** Call flag. */
  private boolean notifyDataSetChanged = false;

  /** Adapter to test. */
  private CursorLoaderAdapter clAdapter;

  /** Test cursor. */
  private MatrixCursor cursor;

  @Before
  public void createAdapter() {
    cursor = new MatrixCursor(new String[] {"_id", "value"});
    cursor.addRow(new Object[] {1, "a"});
    cursor.addRow(new Object[] {2, "b"});
    cursor.addRow(new Object[] {3, "c"});
    clAdapter = new CursorLoaderAdapter(getApplication(), new MyCursorAdapter(getApplication(), null));
  }

  @Test
  public void replaceDataInCoreCausesNotifyDatasetChanged() {
    clAdapter.registerDataSetObserver(new DataSetObserver() {
      @Override
      public void onChanged() {
        notifyDataSetChanged = true;
      }
    });
    notifyDataSetChanged = false;
    clAdapter.replaceDataInCore(cursor);
    assertThat(notifyDataSetChanged).isTrue();
  }

  @Test
  public void replaceDataInCoreShouldSwapCursor() {
    assertThat(clAdapter.getCore().getCursor()).isNull();
    clAdapter.replaceDataInCore(cursor);
    assertThat(clAdapter.getCore().getCursor()).isSameAs(cursor);
    clAdapter.replaceDataInCore(null);
    assertThat(clAdapter.getCore().getCursor()).isNull();
    // swap, not change ;)
    assertThat(cursor.isClosed()).isFalse();
  }

  /** Adapter for testing. */
  private static class MyCursorAdapter extends CursorAdapter {

    public MyCursorAdapter(final Context context, final Cursor c) {
      super(context, c, 0);
    }

    @Override
    public View newView(final Context context, final Cursor cursor, final ViewGroup parent) {
      return new View(context);
    }

    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
    }

  }

}

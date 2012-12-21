package com.stanfy.views.list;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;

import com.stanfy.views.list.LoaderAdapter;

/** Results wrapper. */
// TODO move this class to Enroscar
class CursorLoaderAdapter extends LoaderAdapter<Cursor> {

  public CursorLoaderAdapter(final Context context, final CursorAdapter coreAdapter) {
    super(context, coreAdapter);
  }

  @Override
  protected CursorAdapter getCore() { return (CursorAdapter) super.getCore(); }

  @Override
  public void notifyDataSetChanged() {
    getCore().notifyDataSetChanged();
  }

  @Override
  protected boolean isResponseSuccessful(final Cursor data) {
    return data != null;
  }

  @Override
  protected boolean isResponseEmpty(final Cursor data) {
    return data.getCount() == 0;
  }

  @Override
  protected void replaceDataInCore(final Cursor data) {
    getCore().swapCursor(data);
  }

  @Override
  public void onLoaderReset(final Loader<Cursor> loader) {
    super.onLoaderReset(loader);
    getCore().swapCursor(null);
  }

}
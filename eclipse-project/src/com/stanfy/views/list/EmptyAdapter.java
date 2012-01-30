package com.stanfy.views.list;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class EmptyAdapter extends BaseAdapter implements FetchableListAdapter {

  /** State. */
  private final State state = new State();

  @Override
  public int getCount() { return 0; }
  @Override
  public Object getItem(final int position) { return null; }
  @Override
  public long getItemId(final int position) { return 0; }
  @Override
  public View getView(final int position, final View convertView, final ViewGroup parent) { return null; }
  @Override
  public void loadMoreRecords() { }
  @Override
  public boolean moreElementsAvailable() { return false; }
  @Override
  public boolean isBusy() { return true; }
  @Override
  public void clear() { }
  @Override
  public void restoreState(final State state) { }
  @Override
  public State getState() { return state; }

}

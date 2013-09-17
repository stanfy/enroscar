package com.stanfy.enroscar.views.list.adapter;

import android.widget.ListAdapter;

import java.util.Collection;

/**
 * List adapter that can replace its content with some collection of items.
 */
public interface ReplaceableListAdapter<T> extends ListAdapter {

  void replace(Collection<T> items);

}

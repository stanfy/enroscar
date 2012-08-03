package com.stanfy.views.list;

import android.widget.ListAdapter;

/**
 * 
 * @author Olexandr Tereshchuk (Stanfy - http://www.stanfy.com)
 */
public interface FetchableView {

  void setAdapter(ListAdapter listAdapter);

  ListAdapter getAdapter();

  void setSelection(int i);

}

package com.stanfy.enroscar.test;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;

/**
 * List view activity for testing a list view.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class ListViewActivity extends ListActivity {

  /** Items count. */
  public static final int ITEMS_COUNT = 25;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d("123123", "Create " + this);
    setContentView(R.layout.list);
    final String[] titles = new String[ITEMS_COUNT];
    for (int i = 0; i < titles.length; i++) { titles[i] = String.valueOf(i); }
    setListAdapter(new ArrayAdapter<String>(this, android.R.layout.activity_list_item, android.R.id.text1, titles));
  }

}

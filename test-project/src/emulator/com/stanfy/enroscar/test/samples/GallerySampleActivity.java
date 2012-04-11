package com.stanfy.enroscar.test.samples;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.stanfy.enroscar.test.R;
import com.stanfy.enroscar.test.R.drawable;
import com.stanfy.views.gallery.Gallery;

/**
 * Gallery test activity.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class GallerySampleActivity extends Activity {
  /** Elements count. */
  private static final int COUNT = 30;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    final Gallery gallery = new Gallery(this);
    gallery.setAdapter(new Adapter());
    setContentView(gallery);
    gallery.setSelection(0);
  }

  /**
   * Test adapter.
   * @author Roman Mazur (Stanfy - http://www.stanfy.com)
   */
  public class Adapter extends BaseAdapter {
    @Override
    public int getCount() { return COUNT; }
    @Override
    public String getItem(final int position) { return String.valueOf(position); }
    @Override
    public long getItemId(final int position) { return position; }
    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
      TextView view = null;
      if (convertView != null) {
        view = (TextView)convertView;
        Log.d("123123", "Use convert view " + position + " / " + view.getText());
      } else {
        view = new TextView(GallerySampleActivity.this);
        view.setLayoutParams(new Gallery.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        view.setGravity(Gravity.CENTER);
        Log.d("123123", "Create " + position);
      }
      view.setText(getItem(position));
      view.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.icon, 0, 0);
      return view;
    }

  }

}

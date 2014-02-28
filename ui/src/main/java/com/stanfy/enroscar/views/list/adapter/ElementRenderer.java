package com.stanfy.enroscar.views.list.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ListAdapter;

/**
 * Model element renderer.
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 * @param <MT> model type
 */
public abstract class ElementRenderer<MT> {

  /** Layout resource ID. */
  final int layoutId;

  public ElementRenderer(final int layoutId) {
    this.layoutId = layoutId;
  }

  /** @return the layoutId */
  public int getLayoutId() { return layoutId; }

  /**
   * @param adapter adapter instance
   * @param parent parent view
   * @param position position of the current view
   * @param element element to render
   * @param view row layout view
   * @param holder views holder
   */
  public void render(final Adapter adapter, final ViewGroup parent, final MT element, final View view, final Object holder, final int position) {
    /* empty */
  }

  /**
   * @param adapter adapter instance
   * @param parent parent view
   * @param position position of the current view
   * @param element fiction element to render
   * @param view row layout view
   * @param holder views holder
   * @param type view type ID
   */
  public void renderOtherType(final ListAdapter adapter, final ViewGroup parent, final FictionObject element, final View view, final Object holder, final int position, final int type) {
    /* empty */
  }

  /**
   * @param view view instance
   * @return additional holder for views
   */
  public Object createHolder(final View view) { return null; }

  /**
   * @param view section view
   * @return additional holder for section views
   */
  public Object createOtherTypeHolder(final View view) { return null; }

  /**
   * @param type view type ID
   * @param layoutInflater layout inflater instance
   * @param parent parent view
   * @return view instance for the specified type
   */
  public View createOtherTypeView(final int type, final LayoutInflater layoutInflater, final ViewGroup parent) { return null; }

}

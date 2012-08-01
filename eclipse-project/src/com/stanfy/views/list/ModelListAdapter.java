package com.stanfy.views.list;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;

import com.stanfy.content.FictionObject;
import com.stanfy.content.UniqueObject;

/**
 * @param <T> model type
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public class ModelListAdapter<T extends UniqueObject> extends BaseAdapter {

  /** Main type of views. */
  public static final int TYPE_MAIN = 0;

  /** Renderer. */
  private final ElementRenderer<T> renderer;

  /** Layout inflater. */
  private final LayoutInflater layoutInflater;

  /** Context instance. */
  final Context context;

  /** Elements. */
  private ArrayList<T> elements = new ArrayList<T>();

  /** Last fiction object. */
  private FictionObject lastFiction = null;

  /** Data lock for elements. */
  Object dataLock = new Object();

  public ModelListAdapter(final Context context, final ElementRenderer<T> renderer) {
    this.context = context;
    this.renderer = renderer;
    this.layoutInflater = LayoutInflater.from(context);
  }

  public ModelListAdapter(final ModelListAdapter<T> adapter) {
    this.context = adapter.context;
    this.renderer = adapter.renderer;
    this.layoutInflater = adapter.layoutInflater;
    this.elements = adapter.copyElements();
  }

  /** @return the layoutInflater */
  public LayoutInflater getLayoutInflater() { return layoutInflater; }
  /** @return the context */
  public Context getContext() { return context; }
  /** @return the renderer */
  public ElementRenderer<T> getRenderer() { return renderer; }

  @Override
  public int getViewTypeCount() { return 1; }
  @Override
  public int getItemViewType(final int position) { return TYPE_MAIN; }

  /**
   * @param position item position
   * @return whether this id item is {@link FictionObject}
   */
  protected boolean isFictionElement(final int position) { return getItem(position) instanceof FictionObject; }

  @Override
  public int getCount() { return elements.size(); }
  @Override
  public T getItem(final int position) { return elements.get(position); }
  @Override
  public long getItemId(final int position) {
    final T el = getItem(position);
    return el.getId();
  }

  @Override
  public View getView(final int position, final View convertView, final ViewGroup parent) {
    final int type = getItemViewType(position);
    View view = convertView;
    if (view == null) {
      view = createView(type, parent, layoutInflater);
    }

    final T item = getItem(position);
    final Object holder = view.getTag();

    switch (type) {
    case TYPE_MAIN:
      renderer.render(this, parent, item, view, holder, position);
      break;
    default:
      renderer.renderOtherType(this, parent, (FictionObject)item, view, holder, position, type);
    }

    return view;
  }

  protected View createView(final int type, final ViewGroup parent, final LayoutInflater layoutInflater) {
    final ElementRenderer<T> renderer = this.renderer;
    View view;
    Object holder;
    switch (type) {
    case TYPE_MAIN:
      view = layoutInflater.inflate(renderer.layoutId, parent, false);
      holder = renderer.createHolder(view);
      break;
    default:
      view = renderer.createOtherTypeView(type, layoutInflater, parent);
      holder = renderer.createOtherTypeHolder(view);
    }
    view.setTag(holder);
    return view;
  }

  @Override
  public boolean hasStableIds() { return true; }

  /**
   * @param position position of element to remove
   */
  public void remove(final int position) {
    synchronized (dataLock) {
      elements.remove(position);
      notifyDataSetChanged();
    }
  }

  /**
   * @param e element to add
   */
  public void add(final T e) {
    synchronized (dataLock) {
      elements.add(e);
      notifyDataSetChanged();
    }
  }

  /**
   * @param list list to add
   */
  public void addAll(final List<? extends T> list) {
    synchronized (dataLock) {
      if (lastFiction == null) {
        elements.addAll(list);
      } else {
        final UniqueObject item = list.get(0);
        if (item instanceof FictionObject && lastFiction.equals(item)) {
          // merge two fictions
          elements.addAll(list.subList(1, list.size()));
        } else {
          elements.addAll(list);
        }
      }
      notifyDataSetChanged();
    }
  }

  /**
   * Clear elements list.
   */
  public void clear() {
    synchronized (dataLock) {
      elements.clear();
      notifyDataSetChanged();
    }
  }

  private void resetElements(final ArrayList<T> list) {
    elements = list;
  }

  /**
   * Replace the list of elements.
   * @param list replacement list
   */
  public void replace(final ArrayList<T> list) {
    synchronized (dataLock) {
      resetElements(list);
      notifyDataSetChanged();
    }
  }

  /**
   * @return elements array copy
   */
  public ArrayList<T> copyElements() {
    synchronized (dataLock) {
      final ArrayList<T> list = elements;
      final ArrayList<T> result = list == null ? null : new ArrayList<T>(list);
      return result;
    }
  }


  @Override
  public boolean areAllItemsEnabled() { return getViewTypeCount() == 1; }
  @Override
  public boolean isEnabled(final int position) { return getItemViewType(position) == TYPE_MAIN; }

  @Override
  public void notifyDataSetChanged() {
    lastFiction = null;
    if (getViewTypeCount() > 1) {
      for (int i = elements.size() - 1; i >= 0; i--) {
        final UniqueObject item = elements.get(i);
        if (item instanceof FictionObject) {
          lastFiction = (FictionObject)item;
          break;
        }
      }
    }
    super.notifyDataSetChanged();
  }

  /**
   * Model element renderer.
   * @author Roman Mazur - Stanfy (http://www.stanfy.com)
   * @param <MT> model type
   */
  public abstract static class ElementRenderer<MT extends UniqueObject> {

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
     * @param root view
     * @param imagesManagerContext images context
     * @return additional holder for views
     */
    public Object createHolder(final View view) { return null; }

    /**
     * @param root section view
     * @param imagesManagerContext images context
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

}

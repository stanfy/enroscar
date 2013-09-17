package com.stanfy.enroscar.views.list.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.stanfy.enroscar.content.UniqueObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Base class for adapters that delegate views creation and population to ElementRenderer.
 * @param <T> element type
 */
public abstract class RendererBasedAdapter<T> extends BaseAdapter {

  /** Main type of views. */
  public static final int TYPE_MAIN = 0;

  /** Renderer. */
  private final ElementRenderer<T> renderer;
  /** Layout inflater. */
  private final LayoutInflater layoutInflater;
  /** Context instance. */
  private final Context context;

  /** Data lock for elements. */
  private final Object dataLock = new Object();
  /** Elements. */
  private ArrayList<T> elements = new ArrayList<T>();
  /** Last fiction object. */
  private FictionObject lastFiction = null;

  public RendererBasedAdapter(final Context context, final ElementRenderer<T> renderer) {
    this.context = context;
    this.renderer = renderer;
    this.layoutInflater = LayoutInflater.from(context);
  }

  public RendererBasedAdapter(final RendererBasedAdapter<T> adapter) {
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
   * @return whether this id item is {@link com.stanfy.enroscar.views.list.adapter.FictionObject}
   */
  protected boolean isFictionElement(final int position) { return elements.get(position) instanceof FictionObject; }

  @Override
  public int getCount() { return elements.size(); }

  @Override
  public T getItem(final int position) { return elements.get(position); }

  @SuppressWarnings("unchecked")
  @Override
  public View getView(final int position, final View convertView, final ViewGroup parent) {
    final int type = getItemViewType(position);
    View view = convertView;
    if (view == null) {
      view = createView(type, parent, layoutInflater);
    }

    renderView(position, type, view, parent, getItem(position), view.getTag());

    return view;
  }

  protected void renderView(final int position, final int type, final View view,
      final ViewGroup parent, final Object item, final Object holder) {
    switch (type) {
    case TYPE_MAIN:
      @SuppressWarnings("unchecked")
      T castedItem = (T)item;
      renderer.render(this, parent, castedItem, view, holder, position);
      break;
    default:
      renderer.renderOtherType(this, parent, (FictionObject)item, view, holder, position, type);
    }
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

  /**
   * @param position position of element to remove
   */
  public void remove(final int position) {
    synchronized (dataLock) {
      elements.remove(position);
      checkFictionRemove(position);
      notifyDataSetChanged();
    }
  }

  public void remove(final T e) {
    synchronized (dataLock) {
      int pos = -1;
      if (getViewTypeCount() > 1) {
        pos = elements.indexOf(e);
      }
      elements.remove(e);
      if (pos >= 0) {
        checkFictionRemove(pos);
      }
      notifyDataSetChanged();
    }
  }

  private void checkFictionRemove(final int position) {
    boolean lastRemoved = position >= elements.size() || elements.get(position) instanceof FictionObject;
    if (lastRemoved && position > 0 && elements.get(position - 1) instanceof FictionObject) {
      elements.remove(position - 1);
    }
  }

  public void removeAll(final Collection<T> e) {
    synchronized (dataLock) {
      elements.removeAll(e);
      notifyDataSetChanged();
    }
  }

  /**
   * @param e element to add
   */
  public void add(final T e) {
    synchronized (dataLock) {
      if (e instanceof FictionObject && lastFiction != null && e.equals(lastFiction)) {
        return;
      }
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
        final Object item = list.get(0);
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
      return list == null ? null : new ArrayList<T>(list);
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
        final Object item = elements.get(i);
        if (item instanceof FictionObject) {
          lastFiction = (FictionObject)item;
          break;
        }
      }
    }
    super.notifyDataSetChanged();
  }

}

package com.stanfy.serverapi.response;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import android.util.Log;

import com.stanfy.DebugFlags;

/**
 * @param <T> list element type
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public class ListResultContext<T> extends ParserContext {

  /** Checks flag. */
  private static final boolean CHECKS_ENABLE = DebugFlags.DEBUG_API;

  /** Properties. */
  private int limit, offset, count;

  /** Elements. */
  private ArrayList<T> elements;

  /** Elements class. */
  private final Class<T> elementClass;

  public ListResultContext(final Class<T> elementClass) {
    this.elementClass = elementClass;
    this.elements = createElementsList();
  }

  protected ArrayList<T> createElementsList() { return new ArrayList<T>(); }

  public void initializeList(final int limit, final int offset, final int count) {
    this.limit = limit;
    this.offset = offset;
    this.count = count;
  }

  public void addElements(final Collection<T> c) {
    if (c != elements) { elements.addAll(c); }
  }

  public void addElement(final T e) {
    elements.add(e);
  }

  protected void setElements(final ArrayList<T> elements) {
    this.elements = elements;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void postData(final Object data) {
    if (data == null) { return; }
    if (CHECKS_ENABLE && !elementClass.isAssignableFrom(data.getClass())) {
      Log.e(TAG, "Ignore data of type " + data.getClass() + " = " + data);
      return;
    }
    addElement((T)data);
  }

  /** @return the limit */
  public int getLimit() { return limit; }
  /** @return the offset */
  public int getOffset() { return offset; }
  /** @return the count */
  public int getCount() { return count; }
  /** @return the elements */
  public ArrayList<T> getElements() { return elements; }

  @Override
  public Serializable getModel() { return elements; }

}

package com.stanfy.enroscar.content;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Special array list that contains information about offset an limits.
 * @param <E> element type
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class TaggedArrayList<E extends UniqueObject> extends ArrayList<E> {

  /**serialVersionUID. */
  private static final long serialVersionUID = -2718881362985393198L;

  /** Additional info holder. */
  Serializable tag;

  public TaggedArrayList() { super(); }
  public TaggedArrayList(final int capacity) { super(capacity); }
  public TaggedArrayList(final Collection<? extends E> collection) {
    super(collection);
    if (collection instanceof TaggedArrayList) {
      final TaggedArrayList<?> list = (TaggedArrayList<?>)collection;
      tag = list.tag;
    }
  }

  /** @return additional info holder */
  public Serializable getTag() { return tag; }
  /** @param tag additional info holder */
  public void setTag(final Serializable tag) { this.tag = tag; }

}

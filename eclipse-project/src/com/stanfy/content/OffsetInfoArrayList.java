package com.stanfy.content;

import java.util.Collection;

/**
 * List with offset information.
 * @param <E> element type
 */
public class OffsetInfoArrayList<E extends UniqueObject> extends TaggedArrayList<E> implements OffsetInfoProvider {

  /** serialVersionUID. */
  private static final long serialVersionUID = -8169767168744868104L;

  public OffsetInfoArrayList() { super(); }
  public OffsetInfoArrayList(final int capacity) { super(capacity); }
  public OffsetInfoArrayList(final Collection<? extends E> collection) { super(collection); }

  @Override
  public int getCurrentOffset() {
    final OffsetInfoTag tag = (OffsetInfoTag)this.tag;
    return tag != null ? tag.getCurrentOffset() : -1;
  }
  @Override
  public int getMaxOffset() {
    final OffsetInfoTag tag = (OffsetInfoTag)this.tag;
    return tag != null ? tag.getMaxOffsetCount() : -1;
  }

}

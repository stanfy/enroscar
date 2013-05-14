package com.stanfy.enroscar.content;

import java.util.Collection;

import android.util.Log;

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
  public boolean moreElementsAvailable(final String currentOffset) {
    final OffsetInfoTag tag = (OffsetInfoTag)this.tag;
    if (tag == null) { return false; }

    try {

      final long currentO = Long.parseLong(currentOffset);
      return currentO < tag.getMaxOffsetCount();

    } catch (final NumberFormatException e) {

      Log.e("OffsetInfoProvider", "moreElementsAvailable: Cannot parse current offset value <" + currentOffset + ">, return false", e);
      return false;

    }
  }

}

package com.stanfy.enroscar.content;

import java.io.Serializable;

/**
 * Tag used in {@link TaggedArrayList} to hold offset information.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class OffsetInfoTag implements Serializable {

  /** serialVersionUID. */
  private static final long serialVersionUID = 6687606463138100347L;

  /** Max offset count. */
  private int maxOffsetCount;
  /** Current offset. */
  private int currentOffset;

  public OffsetInfoTag() { /* nothing */ }

  public OffsetInfoTag(final int currentOffset, final int maxOffsetCount) {
    this.maxOffsetCount = maxOffsetCount;
    this.currentOffset = currentOffset;
  }

  /** @return the maxOffsetCount */
  public int getMaxOffsetCount() { return maxOffsetCount; }
  /** @param maxOffsetCount the maxOffsetCount to set */
  public void setMaxOffsetCount(final int maxOffsetCount) { this.maxOffsetCount = maxOffsetCount; }

  /** @return the currentOffset */
  public int getCurrentOffset() { return currentOffset; }
  /** @param currentOffset the currentOffset to set */
  public void setCurrentOffset(final int currentOffset) { this.currentOffset = currentOffset; }

}

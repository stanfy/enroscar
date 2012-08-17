package com.stanfy.content;

/**
 * Interface for models that contain list of some elements and can provide extra information about
 * limit and offset parameters required for remote requests.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public interface OffsetInfoProvider {

  boolean moreElementsAvailable(final String currentOffset);

}

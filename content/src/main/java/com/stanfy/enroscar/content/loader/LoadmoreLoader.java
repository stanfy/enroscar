package com.stanfy.enroscar.content.loader;

/**
 * Interface for loaders that can load more data.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public interface LoadmoreLoader {

  /** Load more data. */
  void forceLoadMore();

  /** @return true if loader can load more data */
  boolean moreElementsAvailable();

  /** @return true if loader is currently working */
  boolean isBusy();

}

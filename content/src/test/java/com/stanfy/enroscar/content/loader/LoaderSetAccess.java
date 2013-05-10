package com.stanfy.enroscar.content.loader;

import com.stanfy.enroscar.content.loader.LoaderSet;

/**
 * Access to {@link LoaderSet}.
 */
public final class LoaderSetAccess {

  private LoaderSetAccess() { }
  
  public static Object[] getResults(final LoaderSet set) {
    return set.getResults();
  }
  
}

package com.stanfy.enroscar.content.loader;

/**
 * Access to {@link LoaderSet}.
 */
public final class LoaderSetAccess {

  private LoaderSetAccess() { }
  
  public static Object[] getResults(final LoaderSet set) {
    return set.getResults();
  }
  
}

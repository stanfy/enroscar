package com.stanfy.enroscar.rest.loader;


/**
 * Provides access to some methods.
 */
public final class LoaderAccess {

  private LoaderAccess() { /* hidden */ }

  public static void waitForLoader(final RequestBuilderLoader<?> loader) {
    final long timeout = 2000000;
    loader.waitForLoader(timeout);
  }

}

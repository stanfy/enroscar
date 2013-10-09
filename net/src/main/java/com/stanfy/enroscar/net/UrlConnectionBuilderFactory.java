package com.stanfy.enroscar.net;

/**
 * Factory for UrlConnectionBuilder.
 */
public interface UrlConnectionBuilderFactory {

  UrlConnectionBuilder newUrlConnectionBuilder();

  /** Default factory of URL connection builders. */
  public static final UrlConnectionBuilderFactory DEFAULT = new UrlConnectionBuilderFactory() {
    @Override
    public UrlConnectionBuilder newUrlConnectionBuilder() {
      return new UrlConnectionBuilder();
    }
  };

}

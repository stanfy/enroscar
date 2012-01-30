package com.stanfy.serverapi.response.xml;

/**
 * Lightweight processor.
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public final class EasyElementProcessor extends ElementProcessor {

  /** Descriptor. */
  public static final Descriptor<EasyElementProcessor> ROOT_DESCRITPOR = new Descriptor<EasyElementProcessor>() {
    @Override
    public EasyElementProcessor createProcessor() { return new EasyElementProcessor(); }
  };

  EasyElementProcessor() { /* hide */ }

}

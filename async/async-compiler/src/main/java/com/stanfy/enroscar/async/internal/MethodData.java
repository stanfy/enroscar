package com.stanfy.enroscar.async.internal;

import javax.lang.model.element.ExecutableElement;

/**
 * Contains information about method that is processed.
 */
public class MethodData {

  final ExecutableElement method;

  final TypeSupport typeSupport;

  public MethodData(ExecutableElement method, TypeSupport typeSupport) {
    this.method = method;
    this.typeSupport = typeSupport;
  }
}

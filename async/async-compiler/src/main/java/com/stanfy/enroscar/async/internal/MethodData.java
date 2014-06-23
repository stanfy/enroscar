package com.stanfy.enroscar.async.internal;

import javax.lang.model.element.ExecutableElement;

/**
 * Contains information about method that is processed.
 */
final class MethodData {

  final ExecutableElement method;

  final TypeSupport operatorTypeSupport;
  final TypeSupport loaderDescriptionTypeSupport;

  public MethodData(ExecutableElement method, TypeSupport typeSupport,
                    TypeSupport loaderDescriptionTypeSupport) {
    this.method = method;
    this.operatorTypeSupport = typeSupport;
    this.loaderDescriptionTypeSupport = loaderDescriptionTypeSupport;
  }
}

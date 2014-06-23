package com.stanfy.enroscar.async.internal;

import java.lang.annotation.Annotation;

/**
 * Rx safe accessor.
 */
final class Rx {

  static final String LOAD = "com.stanfy.enroscar.async.rx.RxLoad";
  static final String SEND = "com.stanfy.enroscar.async.rx.RxSend";

  static boolean hasRx() {
    try {
      return rxLoad() != null;
    } catch (Throwable e) {
      return false;
    }
  }

  @SuppressWarnings("unchecked")
  static Class<? extends Annotation> rxLoad() {
    return annotation(LOAD);
  }

  @SuppressWarnings("unchecked")
  static Class<? extends Annotation> rxSend() {
    return annotation(SEND);
  }

  @SuppressWarnings("unchecked")
  private static Class<? extends Annotation> annotation(final String name) {
    try {
      return (Class<? extends Annotation>) Class.forName(name);
    } catch (Throwable e) {
      throw new AssertionError(e);
    }
  }

}

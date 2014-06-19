package com.stanfy.enroscar.async.internal;

import com.stanfy.enroscar.async.Async;

/**
 * Provides Async instance.
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
public interface AsyncProvider<D> {
  Async<D> provideAsync();
}

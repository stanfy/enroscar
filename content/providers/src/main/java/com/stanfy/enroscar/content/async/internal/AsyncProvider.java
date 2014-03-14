package com.stanfy.enroscar.content.async.internal;

import com.stanfy.enroscar.content.async.Async;

/**
 *  Provides Async instance.
 *  @author Roman Mazur - Stanfy (http://stanfy.com)
 */
public interface AsyncProvider<D> {
  Async<D> provideAsync();
}

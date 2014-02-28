package com.stanfy.enroscar.content.async.internal;

import android.content.Context;

import com.stanfy.enroscar.content.async.Async;

/**
 * @param <D> data type
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
public interface AsyncContext<D> {

  Async<D> provideAsync();

  Context provideContext();

  void releaseData(D data);

}
